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

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * @author Marco Ruiz
 * @since Nov 29, 2009
 */
public class WaitingConcurrentLinkedQueue<ELE_TYPE> extends ConcurrentLinkedQueue<ELE_TYPE> {
	
	public final boolean offer(ELE_TYPE element) {
		synchronized (this) {
			boolean result = super.offer(element);
			this.notifyAll();
			return result;
        }
	}
	
	public final ELE_TYPE poll() {
		synchronized (this) {
			while (isEmpty()) {
				try {
	                this.wait();
                } catch (InterruptedException e) {
	                // TODO Auto-generated catch block
	                e.printStackTrace();
                }
			}
			return super.poll();
        }
	}
}

