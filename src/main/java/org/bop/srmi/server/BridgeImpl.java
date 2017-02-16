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
import java.rmi.server.RMIServerSocketFactory;
import java.rmi.server.UnicastRemoteObject;

import org.bop.srmi.Bridge;
import org.bop.srmi.exception.AccessNotAuthorizedException;
import org.bop.srmi.exception.InstantiationException;
import org.bop.srmi.exception.InternalReflectionException;
import org.bop.srmi.exception.InvalidSessionIdException;

/**
 * @author Marco Ruiz
 * @since Nov 4, 2008
 */
public class BridgeImpl<SESSION_ID_TYPE extends Serializable> extends UnicastRemoteObject implements Bridge<SESSION_ID_TYPE> {

	private SessionsManager<SESSION_ID_TYPE, ?> sessionsManager;
	
	public BridgeImpl() throws RemoteException {
	    super();
    }

	public BridgeImpl(int port) throws RemoteException {
	    super(port);
    }

	public BridgeImpl(int port, RMIClientSocketFactory csf, RMIServerSocketFactory ssf) throws RemoteException {
	    super(port, csf, ssf);
    }

	public BridgeImpl(RMIClientSocketFactory csf) throws RemoteException {
	    super(0, csf, null);
    }

	public void setSessionsManager(SessionsManager<SESSION_ID_TYPE, ?> sessionsManager) {
	    this.sessionsManager = sessionsManager;
    }
	
	public boolean existsSession(SESSION_ID_TYPE sessionId) throws RemoteException {
	    return sessionsManager.getRepo().exists(sessionId);
    }

	public int createSession(SESSION_ID_TYPE sessionId) throws RemoteException, InvalidSessionIdException, InstantiationException {
		SessionHolder<SESSION_ID_TYPE, ?> holder = sessionsManager.getRepo().getOrCreateSessionHolder(sessionId);
		return holder.getDecayPeriod();
    }

	public Object invokeSessionMethod(SESSION_ID_TYPE sessionId, String methodName, Class[] argsClasses, Object[] argsValues)
            throws RemoteException, InvalidSessionIdException, AccessNotAuthorizedException, InstantiationException, InternalReflectionException, InvocationTargetException {
		return sessionsManager.invokeSessionMethod(sessionId, methodName, argsClasses, argsValues);
    }

	public void runSessionHolderProcess(SESSION_ID_TYPE sessionId, SessionHolderProcess process) throws RemoteException, InvalidSessionIdException, AccessNotAuthorizedException, InstantiationException {
	    sessionsManager.invokeHolderProcess(sessionId, process);
    }
}

