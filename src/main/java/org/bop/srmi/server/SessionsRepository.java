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
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import org.bop.srmi.exception.InstantiationException;
import org.bop.srmi.exception.InvalidSessionIdException;

/**
 * @author Marco Ruiz
 * @since Dec 9, 2008
 */
public class SessionsRepository<SESS_ID_T extends Serializable, SESS_T> {
	
	private Map<SESS_ID_T, SessionHolder<SESS_ID_T, SESS_T>> sessions = new HashMap<SESS_ID_T, SessionHolder<SESS_ID_T, SESS_T>>();
	private final Object sessionCreationLock = new Object();

	private SessionHolderFactory<SESS_ID_T, SESS_T> holderFactory;
	
	public SessionsRepository(SessionHolderFactory<SESS_ID_T, SESS_T> holderFactory) {
		this.holderFactory = holderFactory;
    }
	
	public final boolean exists(SESS_ID_T sessionId) {
		return getSessionHolder(sessionId) != null;
	}
	
	public final SESS_T getSession(SESS_ID_T sessionId) {
		SessionHolder<SESS_ID_T, SESS_T> holder = getSessionHolder(sessionId, false);
		return (holder == null) ? null : holder.getSession();
	}
	
	public final SessionHolder<SESS_ID_T, SESS_T> getSessionHolder(SESS_ID_T sessionId) {
		return getSessionHolder(sessionId, false);
	}
	
	public final SessionHolder<SESS_ID_T, SESS_T> getSessionHolder(SESS_ID_T sessionId, boolean remove) {
        synchronized (sessionCreationLock) {
    		return remove ? sessions.remove(sessionId) : sessions.get(sessionId);
        }
	}
	
	public final SessionHolder<SESS_ID_T, SESS_T> getOrCreateSessionHolder(SESS_ID_T sessionId) throws InstantiationException, InvalidSessionIdException {
		SessionHolder<SESS_ID_T, SESS_T> holder;
        synchronized (sessionCreationLock) {
        	holder = sessions.get(sessionId);
        	if (holder == null) {
        		holder = holderFactory.createSessionHolder(sessionId, new DecaySessionTask(sessionId));
        	    sessions.put(sessionId, holder);
        	}
        }
        return holder;
    }

    public final void invokeHolderProcess(SESS_ID_T sessionId, SessionHolderProcess holderProcess) {
	    SessionHolder<SESS_ID_T, SESS_T> holder = getSessionHolder(sessionId, holderProcess.shallRemoveOnRetrieval());
	    if (holder != null) holderProcess.process(holder);
    }
    
	class DecaySessionTask implements Callable<Void> {
		private SESS_ID_T sessionId;
		
		public DecaySessionTask(SESS_ID_T sessionId) {
	        this.sessionId = sessionId;
        }

		public Void call() throws Exception {
			invokeHolderProcess(sessionId, SessionHolderProcess.DECAY);
	        return null;
        }
    }
}

