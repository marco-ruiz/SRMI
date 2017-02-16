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
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * @author Marco Ruiz
 * @since Aug 26, 2007
 */
public class ThreadPoolUtils {
	
	public static ExecutorService createThreadPool(final String name) {
		return createThreadPool(name, false);	
	}

	public static ExecutorService createThreadPool(final String name, final boolean daemon) {
		return Executors.newCachedThreadPool(new CounterThreadFactory(name, daemon));	
	}
}

class CounterThreadFactory implements ThreadFactory {
	private String name;
	private ThreadGroup group;
	private boolean daemon;
	private int count = 0;
	
	public CounterThreadFactory(String name, boolean daemon) {
		this.name = name;
		this.group = new ThreadGroup("Thread Pool: " + name);
		this.daemon = daemon;
    }

	public Thread newThread(Runnable r) {
		count++;
		Thread result = new Thread(group, r, "TP: " + name + " # " + count);
		result.setDaemon(daemon);
		return result;
	}
};
