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

package org.bop.srmi.exception;

import java.io.Serializable;

/**
 * @author Marco Ruiz
 * @since Nov 7, 2008
 */
public class AccessNotAuthorizedException extends InvocationException {

	public AccessNotAuthorizedException(Serializable sessionId, Class sessionClass, String methodName) {
		this(sessionId, sessionClass, methodName, null);
    }

	public AccessNotAuthorizedException(Serializable sessionId, Class sessionClass, String methodName, Exception e) {
	    super("Could not authorize session with id " + sessionId + " to execute ", sessionClass, methodName, new Object[] {}, e);
    }
}
