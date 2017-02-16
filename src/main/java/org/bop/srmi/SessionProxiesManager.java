/*
 * Copyright 2007-2008 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.bop.srmi;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.bop.srmi.exception.AccessNotAuthorizedException;
import org.bop.srmi.exception.BridgeConnectivityException;
import org.bop.srmi.exception.InstantiationException;
import org.bop.srmi.exception.InvalidSessionIdException;
import org.bop.srmi.exception.SessionRemoteException;
import org.bop.srmi.exception.UnresolvableRegistryException;
import org.bop.srmi.server.SessionHolderProcess;
import org.bop.srmi.server.SessionRegistry;
import org.bop.utils.concurrent.timer.IRestartableTimer;
import org.bop.utils.concurrent.timer.NullRestartableTimer;
import org.bop.utils.concurrent.timer.RestartableTimer;

/**
 * @author Marco Ruiz
 * @since Nov 4, 2008
 */
public abstract class SessionProxiesManager<SESS_ID_T extends Serializable, SESS_T> {

	private Map<SESS_ID_T, SESS_T> sessionProxies = new HashMap<SESS_ID_T, SESS_T>();
	private Class<SESS_T> sessionInterface;
	protected SessionRegistry sessionReg;
	private String registryId;
	private String name = null;

	public SessionProxiesManager()                         { this(null, null); }
	public SessionProxiesManager(String name)              { this(name, null); }
	public SessionProxiesManager(SessionRegistry registry) { this(null, registry); }
	
	public static Type[] getParametrizedClasses(Object obj) {
	    ParameterizedType genericSuperclass = (ParameterizedType)obj.getClass().getGenericSuperclass();
	    return genericSuperclass.getActualTypeArguments();
    }

	public SessionProxiesManager(String name, SessionRegistry registry) {
		this.name = name;
		if (registry == null) registry = new SessionRegistry();
		setSessionReg(registry);
		// TODO: Fix THIS!!!
	    List<Class<?>> types = ReflectionUtils.getTypeArguments(SessionProxiesManager.class, getClass());
	    if (types != null && types.size() > 1) {
	        setSessionInterface((Class<SESS_T>)types.get(1));
        } else {
        	// TODO: Handle this situation
//        	throw new UnresolvableSessionTypeException("SessionProxiesManager cann");
        }
	}
	
	public String getName() {
    	return name;
    }
	
	private void setSessionInterface(Class<SESS_T> sessionInterface) {
    	this.sessionInterface = sessionInterface;
    	this.registryId = SessionRegistry.getRegistryId(sessionInterface, name);
    }

	public void setSessionReg(SessionRegistry registry) {
    	this.sessionReg = registry;
    }

	protected Registry locateRegistry() throws UnresolvableRegistryException {
       	return sessionReg.getRegistryForClient();
	}

	protected Bridge<SESS_ID_T> getBridge() throws UnresolvableRegistryException, BridgeConnectivityException {
		try {
			return (Bridge<SESS_ID_T>) locateRegistry().lookup(registryId);
        } catch (RemoteException e) {
        	throw new BridgeConnectivityException("Network connectivity issues while trying to connect to host " + sessionReg.getHost(), e);
        } catch (NotBoundException e) {
        	throw new BridgeConnectivityException("Problems encountered while trying to connect to SRMI session manager at " + sessionReg.getHost(), e);
        }
	}

	public boolean existsSession(SESS_ID_T sessionId) throws SessionRemoteException, UnresolvableRegistryException, BridgeConnectivityException {
		Bridge<SESS_ID_T> bridge = getBridge();
		try {
			return bridge.existsSession(sessionId);
        } catch (RemoteException e) {
        	throw new SessionRemoteException(e);
        }
	}

	public SESS_ID_T getSessionId(SESS_T session) {
		return ((ISessionProxy<SESS_ID_T>) session).getId();
	}

	public void destroySession(SESS_T session) throws AccessNotAuthorizedException, SessionRemoteException, UnresolvableRegistryException, BridgeConnectivityException, InvalidSessionIdException, InstantiationException {
		SESS_ID_T sessionId = getSessionId(session);
		synchronized (sessionProxies) {
			runSessionHolderProcess(sessionId, SessionHolderProcess.DESTROY);
			sessionProxies.remove(sessionId);
        }
	}

	private void runSessionHolderProcess(SESS_ID_T sessionId, SessionHolderProcess process) throws SessionRemoteException, UnresolvableRegistryException, BridgeConnectivityException, AccessNotAuthorizedException, InvalidSessionIdException, InstantiationException {
        Bridge<SESS_ID_T> bridge = getBridge();
		try {
	        bridge.runSessionHolderProcess(sessionId, process);
        } catch (RemoteException e) {
        	throw new SessionRemoteException(e);
        }
    }

	public SESS_T getSession(SESS_ID_T sessionId) throws SessionRemoteException, UnresolvableRegistryException, BridgeConnectivityException, InvalidSessionIdException, InstantiationException {
		SESS_T session;
		synchronized (sessionProxies) {
			session = sessionProxies.get(sessionId);
			if (session == null) {
				session = createSession(sessionId);
				sessionProxies.put(sessionId, session);
			}
        }
		
		return session;
	}

	private SESS_T createSession(SESS_ID_T sessionId) throws SessionRemoteException, UnresolvableRegistryException, BridgeConnectivityException, InvalidSessionIdException, InstantiationException {
		SessionProxy target = new SessionProxy(sessionId);
		return (SESS_T) Proxy.newProxyInstance(target.getClass().getClassLoader(), new Class[]{sessionInterface, ISessionProxy.class}, target);
    }

	//===============
	// SESSION PROXY
	//===============
	class SessionProxy implements ISessionProxy<SESS_ID_T>, InvocationHandler {
		private SESS_ID_T sessionId;
	    private IRestartableTimer joltTimer;

		private SessionProxy(SESS_ID_T sessionId) throws SessionRemoteException, UnresolvableRegistryException, BridgeConnectivityException, InvalidSessionIdException, InstantiationException {
			this.sessionId = sessionId;

			Bridge<SESS_ID_T> bridge = getBridge();
			try {
				int decayPeriod = bridge.createSession(sessionId);
				joltTimer = createJoltTimer(decayPeriod);
				if (decayPeriod > 0) 
					runSessionHolderProcess(sessionId, SessionHolderProcess.JOLT);
	        } catch (RemoteException e) {
	        	throw new SessionRemoteException(e);
	        } catch (AccessNotAuthorizedException e) {
	        	// TODO: Ignore for now. Reflect on how to handle this weird situation
            }
	    }

		private IRestartableTimer createJoltTimer(int decayPeriod) {
	        return decayPeriod > 0 ? 
		        new RestartableTimer(decayPeriod, new Callable<Void>() {
		        	public Void call() throws Exception {
		        		runSessionHolderProcess(SessionProxy.this.sessionId, SessionHolderProcess.JOLT);
		                return null;
		            }
		        }) :
		        new NullRestartableTimer();
        }
		
		public Object invoke(Object target, Method method, Object[] arguments) throws Throwable {
			if (method.getName().equals("getId") && method.getParameterTypes().length == 0) 
				return getId();
			
	        Bridge<SESS_ID_T> bridge = getBridge();
	    	try {
	    		joltTimer.pause();
				Object result = bridge.invokeSessionMethod(sessionId, method.getName(), method.getParameterTypes(), arguments);
				joltTimer.restart();
				return result;
	        } catch (RemoteException e) {
	        	throw new SessionRemoteException(e);
	        } catch (InvocationTargetException e) {
	    		throw e.getCause();
	        }
	    }

		public SESS_ID_T getId() {
	        return sessionId;
        }
	}
	
	interface ISessionProxy<SESS_ID_T> {
		public SESS_ID_T getId();
	}
}

