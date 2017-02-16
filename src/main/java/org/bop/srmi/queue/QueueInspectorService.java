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

import java.io.Serializable;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Marco Ruiz
 * @since Dec 7, 2009
 */
public class QueueInspectorService<SESS_ID_T extends Serializable> {
	
	private static ExecutorService inspectorThreads = Executors.newCachedThreadPool();
	
	private Queue<ElementWrapper<SESS_ID_T, ?>> queue;
	private IQueueElementProcessor<SESS_ID_T> processor;
	
	public QueueInspectorService(Queue<ElementWrapper<SESS_ID_T, ?>> queue, IQueueElementProcessor<SESS_ID_T> processor) {
		this.queue = queue;
		this.processor = processor;
		inspectorThreads.submit(new Inspector());
	}
	
	//==========================
	// INSPECTOR RUNNABLE CLASS
	//==========================
	class Inspector implements Runnable {

		public void run() {
			while (true) {
				try {
					processor.processElement(queue.poll());
	            } catch (Exception e) {
	            	// Ignore! All exceptions should be handle by the processElement method
	            }
			}
        }
	}
}
