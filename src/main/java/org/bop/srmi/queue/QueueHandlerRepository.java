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

package org.bop.srmi.queue;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.bop.srmi.SessionProxiesManager;
import org.bop.srmi.exception.BridgeConnectivityException;
import org.bop.srmi.exception.InstantiationException;
import org.bop.srmi.exception.InvalidSessionIdException;
import org.bop.srmi.exception.SessionRemoteException;
import org.bop.srmi.exception.UnresolvableRegistryException;
import org.bop.srmi.server.SessionRegistry;

/**
 * @author Marco Ruiz
 * @since Dec 8, 2009
 */
public class QueueHandlerRepository<QUEUE_ID_T extends Serializable, SESS_ID_T extends Serializable> {

	private Map<QUEUE_ID_T, QueueHandler<QUEUE_ID_T,SESS_ID_T>> repo;
	private SessionQueue<QUEUE_ID_T, SESS_ID_T> session;
	private SessionProxiesManager<SESS_ID_T, SessionQueue<QUEUE_ID_T, SESS_ID_T>> proxiesMgr;

	public QueueHandlerRepository(SESS_ID_T sessionId, String serverName, SessionRegistry registry) 
		throws SessionRemoteException, UnresolvableRegistryException, BridgeConnectivityException, InvalidSessionIdException, InstantiationException {

		this(sessionId, serverName, new SessionProxiesManager<SESS_ID_T, SessionQueue<QUEUE_ID_T, SESS_ID_T>>(serverName, registry) {});
	}

	public QueueHandlerRepository(SESS_ID_T sessionId, String serverName, SessionProxiesManager<SESS_ID_T, SessionQueue<QUEUE_ID_T, SESS_ID_T>> sessionsProxiesMgr) 
			throws SessionRemoteException, UnresolvableRegistryException, BridgeConnectivityException, InvalidSessionIdException, InstantiationException {
				
			repo = new HashMap<QUEUE_ID_T, QueueHandler<QUEUE_ID_T,SESS_ID_T>>();
		    proxiesMgr = sessionsProxiesMgr;
			session = proxiesMgr.getSession(sessionId);
		
			for (QUEUE_ID_T queueId : session.getQueuesIds()) 
		        repo.put(queueId, new QueueHandler<QUEUE_ID_T, SESS_ID_T>(queueId, session));
	}

	public QueueHandler<QUEUE_ID_T, SESS_ID_T> getQueue(QUEUE_ID_T id) throws QueueNotFoundException {
		QueueHandler<QUEUE_ID_T, SESS_ID_T> result = repo.get(id);
		if (result == null) throw new QueueNotFoundException(proxiesMgr.getName(), id);
		return result;
	}
	
	public SessionQueue<QUEUE_ID_T, SESS_ID_T> getSession() {
    	return session;
    }

	public SessionProxiesManager<SESS_ID_T, SessionQueue<QUEUE_ID_T, SESS_ID_T>> getProxiesMgr() {
    	return proxiesMgr;
    }
}
