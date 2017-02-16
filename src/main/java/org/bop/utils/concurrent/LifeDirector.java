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

package org.bop.utils.concurrent;

import java.util.concurrent.ExecutorService;


/**
 * @author Marco Ruiz
 * @since Apr 16, 2009
 */
public class LifeDirector {

	private static ExecutorService deathDealerThreadPool  = ThreadPoolUtils.createThreadPool("Killers");

	private long idleTimeout;

	private long idleDeathTime = -1;
	private long oldDeathTime;
	
	private boolean shutdown = false;
	
	private Object lock = new Object();

	public LifeDirector(long ageTimeout, long idleTimeout) {
		this(ageTimeout, idleTimeout, System.currentTimeMillis());
    }

	public LifeDirector(long ageTimeout, long idleTimeout, long birthTime) {
		// Only execute if there is a target and it has limits of age (old) and/or lazyness (idle)
		if (ageTimeout <= 0 && idleTimeout <= 0) return;
		
		this.idleTimeout = idleTimeout;
		this.oldDeathTime = birthTime + ageTimeout;
		deathDealerThreadPool.submit(new Slayer());
    }

	public void startIdleCountdown()  {
		setIdleDeathTime(System.currentTimeMillis() + idleTimeout);
	}
	
	public void cancelIdleCountdown() {
		setIdleDeathTime(-1);
	}
	
	private void setIdleDeathTime(long time) {
		synchronized (lock) {
			idleDeathTime = time;
			lock.notifyAll();
		}
	}
	
	public void shutdownTimers() {
		synchronized (lock) {
			shutdown = true;
			lock.notifyAll();			
		}
	}
	
	//====================================
	// CALL BACKS - Do nothing by default
	//====================================
	public void tooIdle() {}
	public void tooOld()  {}

	//========
	// SLAYER
	//========
	class Slayer implements Runnable {
		public void run() {
			boolean isIdleCutTime = false; 
	
			synchronized (lock) {
	            while (!shutdown) {
	            	isIdleCutTime = (0 <= idleDeathTime && idleDeathTime <= oldDeathTime);
	            	long remaining = (isIdleCutTime ? idleDeathTime : oldDeathTime) - System.currentTimeMillis();
	            	if (remaining <= 0) break; // Too idle or too old
	            	
	            	try { 
	            		lock.wait(remaining); 
	            	} catch (InterruptedException e) {
	            		// TODO: Release allocation due to interruption sent by daemon shutting down sequence ?
	            		// Or do this in "dispose" method from the allocation?
	            	}
	            }
				if (shutdown) return;
			}
			
			// Killing callback
			if (isIdleCutTime) 
				tooIdle();
			else
				tooOld();
		}
	}
}

