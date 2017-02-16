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
import java.lang.reflect.Method;

import org.bop.srmi.ISessionDecayable;
import org.bop.srmi.ISessionDestroyable;
import org.bop.srmi.exception.InternalReflectionException;
import org.bop.utils.concurrent.timer.IRestartableTimer;
import org.bop.utils.concurrent.timer.NullRestartableTimer;

/**
 * @author Marco Ruiz
 * @since Nov 8, 2008
 */
public class SessionHolder<SESSION_ID_TYPE extends Serializable, SESSION_TYPE> {

	private IRestartableTimer decayTimer;

	private Object invocationLock = new Object();
	protected SESSION_ID_TYPE sessionId;
	protected SESSION_TYPE session;
	private boolean destroyed = false;

	public SessionHolder(SESSION_ID_TYPE sessionId, SESSION_TYPE session, IRestartableTimer decayTimer) {
		this.sessionId  = sessionId;
		this.session    = session;
		this.decayTimer = (decayTimer != null) ? decayTimer : new NullRestartableTimer();
	}
	
	public int getDecayPeriod() {
    	return decayTimer.getPeriod();
    }

	public SESSION_TYPE getSession() {
		return session;
	}
	
	public final Object invoke(String methodName, Class[] argsClasses, Object[] argsValues) throws InvocationTargetException, InternalReflectionException {
		synchronized (invocationLock) {
			if (destroyed) return null;
			Method method = getMethod(methodName, argsClasses);
	        try {
	        	decayTimer.pause();
	    		Object result = method.invoke(session, argsValues);
	    		decayTimer.restart();
	    		return result;
	        } catch (InvocationTargetException e) {
	        	throw e;
	        } catch (Exception e) {
	    		throw new InternalReflectionException(session.getClass(), method.getName(), argsValues, e);
	        }
        }
	}
	
	private Method getMethod(String methodName, Class[] argsClasses) throws InternalReflectionException {
        try {
	        return session.getClass().getMethod(methodName, argsClasses);
        } catch (Exception e) {
    		throw new InternalReflectionException(session.getClass(), methodName, null, e);
        }
    }

	public void jolt() {
		decayTimer.reset();
		synchronized (invocationLock) {
			if (!destroyed && ISessionDecayable.class.isAssignableFrom(session.getClass())) 
				((ISessionDecayable)session).onJolt();
		}
	}
	
	public void decay() {
		synchronized (invocationLock) {
			if (!destroyed) {
				if (ISessionDecayable.class.isAssignableFrom(session.getClass())) 
					((ISessionDecayable)session).onDecay();
				destroy();
			}
		}
    }

	public void destroy() {
		synchronized (invocationLock) {
			if (!destroyed) {
				if (ISessionDestroyable.class.isAssignableFrom(session.getClass())) 
					((ISessionDestroyable)session).onDestroy();
				destroyed = true;
			}
		}
	}
}

