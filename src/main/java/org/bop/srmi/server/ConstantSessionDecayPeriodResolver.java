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


/**
 * @author Marco Ruiz
 * @since Dec 4, 2009
 */
public class ConstantSessionDecayPeriodResolver<SESSION_ID_TYPE> implements ISessionDecayPeriodResolver<SESSION_ID_TYPE> {

	private int period;

	public ConstantSessionDecayPeriodResolver(int period) {
		this.period = period;
	}
	
	public int getPeriod() {
    	return period;
    }

	public void setPeriod(int period) {
    	this.period = period;
    }

	public int getSessionDecayPeriod(SESSION_ID_TYPE sessionId) {
	    return period;
    }
}

