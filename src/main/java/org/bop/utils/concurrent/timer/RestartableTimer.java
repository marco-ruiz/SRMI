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

package org.bop.utils.concurrent.timer;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bop.utils.concurrent.BooleanLock;
import org.bop.utils.concurrent.ThreadPoolUtils;


/**
 * 
 * @author Marco Ruiz
 * @since April 6, 2009
 */
public class RestartableTimer implements Runnable, IRestartableTimer {

	private static Log log = LogFactory.getLog(RestartableTimer.class);

	public static int INFINITE_TIMES = -1;
	
	private static ExecutorService timersThreadPool = ThreadPoolUtils.createThreadPool("Restartable Timers", true);

	private Callable<Void> task;
	private int maxReschedules;
	private int period;

	private long lastDueEvent = System.currentTimeMillis();
	private int countReschedules = 0;
	private BooleanLock pauseLock = new BooleanLock();
	private boolean mustStop = false;
	
	public RestartableTimer(int period, Callable<Void> task) {
		this(period, task, INFINITE_TIMES);
	}
	
	public RestartableTimer(int period, Callable<Void> task, int maxReschedules) {
		this.task = task;
		this.period = period;
		this.maxReschedules = maxReschedules;
		timersThreadPool.submit(this);
	}
	
    public int getPeriod() {
    	return period;
    }

	public void stop() {
    	synchronized (pauseLock) {
    		mustStop  = true;
    	}
    }

    public void pause() {
    	synchronized (pauseLock) {
    		pauseLock.applyLock(); 
    	}
    }

    public void restart() {
    	synchronized (pauseLock) { 
	    	pauseLock.releaseLock();
	    	reset();
    	}
    }
    
    public void reset() {
    	synchronized (pauseLock) {
    		lastDueEvent = System.currentTimeMillis();
    	}
    }
    
	public void run() {
		long timeToNextDueEvent = 0;
		while (true) {
			synchronized (pauseLock) {
				if (mustStop) return;
				pauseLock.waitUntilConditionTrue();
				timeToNextDueEvent = lastDueEvent + period - System.currentTimeMillis();
				if (timeToNextDueEvent <= 0) 
					timeToNextDueEvent = taskDueEvent();
			}
			
			try { Thread.sleep(timeToNextDueEvent); } catch (InterruptedException e) {}
		}
	}

	private long taskDueEvent() {
    	try {
        	log.debug("Executing timer's task '" + task + "'");
	        task.call();
        } catch (Exception e) {
        	log.warn("Exception encountered trying to execute timer's task '" + task + "'", e);
        }
	    if (maxReschedules < 0 || maxReschedules > countReschedules) {
	    	reset();
	    	countReschedules++;
		    return period;
	    }
	    
    	stop();
    	return 0;
    }
}

