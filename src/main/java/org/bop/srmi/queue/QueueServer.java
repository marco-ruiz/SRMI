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
import java.util.Queue;

import org.bop.srmi.exception.BridgeBindingException;
import org.bop.srmi.exception.InstantiationException;
import org.bop.srmi.exception.UnresolvableRegistryException;
import org.bop.srmi.server.AbstractSessionFactory;
import org.bop.srmi.server.ISessionAuthenticator;
import org.bop.srmi.server.ISessionDecayPeriodResolver;
import org.bop.srmi.server.SessionsManager;

/**
 * @author Marco Ruiz
 * @since Dec 7, 2009
 */
public class QueueServer<QUEUE_ID_T extends Serializable, SESS_ID_T extends Serializable> {
	
	private final String name;

	private QueuesRepository<QUEUE_ID_T, SESS_ID_T> queues = new QueuesRepository<QUEUE_ID_T, SESS_ID_T>();
	private ISessionAuthenticator<SESS_ID_T> authenticator;
	private ISessionDecayPeriodResolver<SESS_ID_T> decayPeriodResolver;

	private SessionsManager<SESS_ID_T, SessionQueue<QUEUE_ID_T, SESS_ID_T>> sessionsManager;

	public QueueServer(String name) throws UnresolvableRegistryException, BridgeBindingException {
	    this(name, null, null);
    }

	public QueueServer(String name, ISessionAuthenticator<SESS_ID_T> authenticator) throws UnresolvableRegistryException, BridgeBindingException {
	    this(name, authenticator, null);
    }

	public QueueServer(String name, ISessionDecayPeriodResolver<SESS_ID_T> decayPeriodResolver) throws UnresolvableRegistryException, BridgeBindingException {
	    this(name, null, decayPeriodResolver);
    }

	public QueueServer(String name, ISessionAuthenticator<SESS_ID_T> authenticator, ISessionDecayPeriodResolver<SESS_ID_T> decayPeriodResolver) throws UnresolvableRegistryException, BridgeBindingException {
		this.name = name;
		this.authenticator = authenticator;
		this.decayPeriodResolver = decayPeriodResolver;
    }
	
	public String getName() {
    	return name;
    }

	public void registerQueue(QUEUE_ID_T queueId, Queue<ElementWrapper<SESS_ID_T, ?>> queue) {
		queues.put(queueId, queue);
	}
	
	public void start() throws UnresolvableRegistryException, BridgeBindingException {
	    sessionsManager = new SessionsManager<SESS_ID_T, SessionQueue<QUEUE_ID_T, SESS_ID_T>>(
	    		new SessionQueueFactory(), authenticator, decayPeriodResolver);
	    sessionsManager.setName(name);
		sessionsManager.publish();
    }

	public Queue<ElementWrapper<SESS_ID_T, ?>> getQueue(QUEUE_ID_T queueId) {
		return queues.get(queueId);
	}
	
	//=============================
	// SESSION QUEUE FACTORY CLASS
	//=============================
	class SessionQueueFactory extends AbstractSessionFactory<SESS_ID_T, SessionQueue<QUEUE_ID_T, SESS_ID_T>> {
		public final SessionQueue<QUEUE_ID_T, SESS_ID_T> createSession(SESS_ID_T id, boolean createAsNeeded) throws InstantiationException {
		    return new SessionQueueImpl<QUEUE_ID_T, SESS_ID_T>(name, id, queues);
	    }
	}
}

