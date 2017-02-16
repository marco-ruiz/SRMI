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

import java.util.Timer;
import java.util.TimerTask;

/**
 * @author Marco Ruiz
 * @since Apr 14, 2009
 */
public class TooLateTimer {
	
    private Timer timer = null;
    private Object lock = new Object();
	
	public TooLateTimer(long delay) {
		if (delay <=0) return;
		this.timer = new Timer();
		this.timer.schedule(new TooLateTask(), delay);
    }

	public void shutdown() {
	    synchronized (lock) {
	    	timer.cancel();
	    }
    }

	class TooLateTask extends TimerTask {
	    public void run() {
	    	shutdown();
	    	tooLate();
	    }
    };

	public void tooLate() {}
}
