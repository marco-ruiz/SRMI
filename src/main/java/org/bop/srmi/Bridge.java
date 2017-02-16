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
import java.lang.reflect.InvocationTargetException;
import java.rmi.Remote;
import java.rmi.RemoteException;

import org.bop.srmi.exception.AccessNotAuthorizedException;
import org.bop.srmi.exception.InstantiationException;
import org.bop.srmi.exception.InternalReflectionException;
import org.bop.srmi.exception.InvalidSessionIdException;
import org.bop.srmi.server.SessionHolderProcess;

/**
 * @author Marco Ruiz
 * @since Nov 21, 2007
 */
public interface Bridge<SESSION_ID_TYPE extends Serializable> extends Remote {

	public boolean existsSession(SESSION_ID_TYPE sessionId) throws RemoteException;

	public int createSession(SESSION_ID_TYPE sessionId) throws RemoteException, InvalidSessionIdException, InstantiationException;

	public Object invokeSessionMethod(SESSION_ID_TYPE sessionId, String methodName, Class[] argsClasses, Object[] argsValues) 
		throws RemoteException, InvalidSessionIdException, AccessNotAuthorizedException, InstantiationException, InternalReflectionException, InvocationTargetException;

	public void runSessionHolderProcess(SESSION_ID_TYPE sessionId, SessionHolderProcess process) throws RemoteException, InvalidSessionIdException, AccessNotAuthorizedException, InstantiationException;
}
