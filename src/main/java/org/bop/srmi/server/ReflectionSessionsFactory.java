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
import java.lang.reflect.Constructor;

import org.bop.srmi.ReflectionUtils;
import org.bop.srmi.exception.InstantiationException;

/**
 * @author Marco Ruiz
 * @since Dec 9, 2008
 */
public class ReflectionSessionsFactory<SESS_ID_T extends Serializable, SESS_T, SESS_IMPL_T extends SESS_T> extends AbstractSessionFactory<SESS_ID_T, SESS_T> {
	
	private boolean emptyConstructor = true;
	private Constructor<? extends SESS_T> sessionConstructor;
	private Class<SESS_IMPL_T> sessionsClass;
	
	public ReflectionSessionsFactory() throws NoSuchMethodException {
		super();
		sessionsClass = (Class<SESS_IMPL_T>) ReflectionUtils.getTypeArguments(ReflectionSessionsFactory.class, getClass()).get(2);
		for (Constructor construct : sessionsClass.getConstructors()) {
	        Class[] paramTypes = construct.getParameterTypes();
			if (paramTypes != null && paramTypes.length == 1 && paramTypes[0].equals(sessionIdClass)) {
				emptyConstructor = false;
				sessionConstructor = construct;
				return;
			}
        }
		
		sessionConstructor = sessionsClass.getConstructor();
    }
	
	public SESS_T createSession(SESS_ID_T id, boolean create) throws InstantiationException {
	    try {
		    return (emptyConstructor) ? sessionConstructor.newInstance() : sessionConstructor.newInstance(id);
	    } catch (Exception e) {
	    	throw new InstantiationException("Could not instantiate " + sessionsClass + ".", e);
	    }
    }
}

