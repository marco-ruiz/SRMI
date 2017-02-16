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

package org.bop.srmi.server;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;
import java.rmi.server.RMIClientSocketFactory;

import org.bop.srmi.Bridge;
import org.bop.srmi.exception.AccessNotAuthorizedException;
import org.bop.srmi.exception.BridgeBindingException;
import org.bop.srmi.exception.InstantiationException;
import org.bop.srmi.exception.InternalReflectionException;
import org.bop.srmi.exception.InvalidSessionIdException;
import org.bop.srmi.exception.UnresolvableRegistryException;

/**
 * @author Marco Ruiz
 * @since Nov 5, 2008
 */
public class SessionsManager<SESS_ID_T extends Serializable, SESS_T> implements Serializable {
	
	protected SessionRegistry registry = new SessionRegistry();
	private RMIClientSocketFactory csf = null;
	private SessionsRepository<SESS_ID_T, SESS_T> repo;
	private IBridgeFactory<SESS_ID_T> bridgeFactory;
	private ISessionAuthorizer<SESS_ID_T> authorizer = new TrueAuthorizer<SESS_ID_T>();
	private Class<SESS_T> sessionsInterface;
	private String name = null; // To discriminate among same session types when registering onto the RMI registry
	
	// CONSTRUCTORS
	public SessionsManager(AbstractSessionFactory<SESS_ID_T, SESS_T> factory) throws UnresolvableRegistryException, BridgeBindingException {
		this(factory, null, null);
    }
	
	public SessionsManager(AbstractSessionFactory<SESS_ID_T, SESS_T> factory, ISessionAuthenticator<SESS_ID_T> authenticator) throws UnresolvableRegistryException, BridgeBindingException {
		this(factory, authenticator, null);
    }
	
	public SessionsManager(AbstractSessionFactory<SESS_ID_T, SESS_T> factory, ISessionDecayPeriodResolver<SESS_ID_T> decayPeriodResolver) throws UnresolvableRegistryException, BridgeBindingException {
		this(factory, null, decayPeriodResolver);
    }
	
	public SessionsManager(AbstractSessionFactory<SESS_ID_T, SESS_T> factory, ISessionAuthenticator<SESS_ID_T> authenticator, ISessionDecayPeriodResolver<SESS_ID_T> decayPeriodResolver) throws UnresolvableRegistryException, BridgeBindingException {
		setBridgeFactory(new DefaultBridgeFactory<SESS_ID_T>());
		this.sessionsInterface = factory.getSessionInterface();
		SessionHolderFactory<SESS_ID_T, SESS_T> holderFactory = new SessionHolderFactory<SESS_ID_T, SESS_T>(factory, authenticator, decayPeriodResolver);
		this.repo = new SessionsRepository<SESS_ID_T, SESS_T>(holderFactory);
    }
	
	// SETTERS
	public void setName(String name) {
		this.name = name;
	}
	
	public void setRegistry(SessionRegistry registry) {
    	this.registry = registry;
    }

	public void setBridgeFactory(IBridgeFactory<SESS_ID_T> bridgeFactory) {
	    this.bridgeFactory = bridgeFactory;
    }
	
	public void setClientSocketFactoryToConnectToBridge(RMIClientSocketFactory csf) {
		this.csf = csf;
	}

	public void setAuthorizer(ISessionAuthorizer<SESS_ID_T> authorizer) {
    	this.authorizer = authorizer;
    }
	
	// PUBLISH (START)
	public Bridge<SESS_ID_T> publish() throws UnresolvableRegistryException, BridgeBindingException {
		BridgeImpl<SESS_ID_T> exportedBridge;
		try {
			exportedBridge = bridgeFactory.createExportableBridge(csf);
        } catch (RemoteException e) {
			throw new BridgeBindingException("Error encountered while creating exportable bridge for " + repo + " to RMI registry", e);
        }
		
		exportedBridge.setSessionsManager(this);
		registry.bind(sessionsInterface, name, exportedBridge);
		return exportedBridge;
	}

	// BRIDGE API
	public SessionsRepository<SESS_ID_T, SESS_T> getRepo() {
    	return repo;
    }

	public Object invokeSessionMethod(SESS_ID_T sessionId, String methodName, Class[] argsClasses, Object[] argsValues) 
		throws RemoteException, InvalidSessionIdException, AccessNotAuthorizedException, InstantiationException, InternalReflectionException, InvocationTargetException {
		
        try {
        	if (!authorizer.authorizeInvocation(sessionId, sessionsInterface.getMethod(methodName, argsClasses))) 
        		throw new AccessNotAuthorizedException(sessionId, sessionsInterface, methodName);
        } catch (Exception e) {
        	throw new AccessNotAuthorizedException(sessionId, sessionsInterface, methodName, e);
        }
        
        SessionHolder<SESS_ID_T, SESS_T> holder = getRepo().getOrCreateSessionHolder(sessionId);
        return (holder != null) ? holder.invoke(methodName, argsClasses, argsValues) : null;
	}

	public void invokeHolderProcess(SESS_ID_T sessionId, SessionHolderProcess holderProcess) throws AccessNotAuthorizedException {
		if (!authorizer.authorizeHolderProcess(sessionId, holderProcess)) 
			throw new AccessNotAuthorizedException(sessionId, sessionsInterface, "holder process '" + holderProcess.getProcessName() + "'");
		
	    repo.invokeHolderProcess(sessionId, holderProcess);
	}
}

