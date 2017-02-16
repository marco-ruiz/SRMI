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
import java.util.concurrent.Callable;

import org.bop.srmi.exception.InstantiationException;
import org.bop.srmi.exception.InvalidSessionIdException;
import org.bop.utils.concurrent.timer.IRestartableTimer;
import org.bop.utils.concurrent.timer.NullRestartableTimer;
import org.bop.utils.concurrent.timer.RestartableTimer;

/**
 * @author Marco Ruiz
 * @since Dec 4, 2009
 */
public final class SessionHolderFactory<SESS_ID_T extends Serializable, SESS_T> {
	
	private ISessionAuthenticator<SESS_ID_T> authenticator = new ISessionAuthenticator<SESS_ID_T>() {
        public boolean authenticate(SESS_ID_T authToken) { return true; }
	};
	
	private ISessionDecayPeriodResolver<SESS_ID_T> decayPeriodResolver = new ConstantSessionDecayPeriodResolver(-1);

	protected AbstractSessionFactory<SESS_ID_T, SESS_T> factory;

	public SessionHolderFactory(AbstractSessionFactory<SESS_ID_T, SESS_T> factory, ISessionAuthenticator<SESS_ID_T> authenticator, ISessionDecayPeriodResolver<SESS_ID_T> decayPeriodResolver) {
		this.factory = factory;
    	
		if (authenticator != null) 
    		this.authenticator = authenticator;
    	
    	if (decayPeriodResolver != null) 
    		this.decayPeriodResolver = decayPeriodResolver;
	}
	
	public SessionHolder<SESS_ID_T, SESS_T> createSessionHolder(SESS_ID_T sessionId, Callable<Void> decaySessionTask) throws InvalidSessionIdException, InstantiationException {
	    if (!authenticator.authenticate(sessionId))
        	throw new InvalidSessionIdException("Could not authenticate session id " + sessionId + ".");
	    
	    SESS_T session = factory.createSession(sessionId, true);
	    int decayPeriod = decayPeriodResolver.getSessionDecayPeriod(sessionId);
		IRestartableTimer decayTimer = (decayPeriod > 0) ?  
				new RestartableTimer(decayPeriod + 10000, decaySessionTask, 0) : 
				new NullRestartableTimer();
	    return new SessionHolder<SESS_ID_T, SESS_T>(sessionId, session, decayTimer);
    }
}

