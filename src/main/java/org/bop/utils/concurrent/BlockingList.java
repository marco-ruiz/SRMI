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

import java.util.ArrayList;
import java.util.List;

/**
 * @author Marco Ruiz
 * @since Nov 16, 2007
 */
public class BlockingList<ELE_TYPE> {
	
	private List<ELE_TYPE> wrapee = new ArrayList<ELE_TYPE>();
	private boolean wakeUp = false;
	
	public synchronized List<ELE_TYPE> copy() {
		return new ArrayList<ELE_TYPE>(wrapee);
	}
	
	public synchronized void forceWakeUp() {
		wakeUp = true;
		this.notifyAll();
	}
	
	public synchronized void add(ELE_TYPE element) {
		wrapee.add(element);
		this.notifyAll();
	}
	
	public synchronized void remove(ELE_TYPE element) {
		wrapee.remove(element);
	}
	
	public synchronized List<ELE_TYPE> takeAll() {
		waitUntilNotEmpty();
		if (wakeUp) {
			wakeUp = false;
			return null;
		}
		List<ELE_TYPE> containerCopy = copy();
		wrapee.clear();
		return containerCopy;
	}

	public synchronized ELE_TYPE takeOne() {
		ELE_TYPE result;
		do {
			waitUntilNotEmpty();
			result = wrapee.remove(0);
		} while (result == null);
		return result;
	}

	private void waitUntilNotEmpty() {
		while (wrapee.isEmpty() && !wakeUp) 
			try { this.wait(); } catch (InterruptedException e) {}
	}

	public int size() {
		return wrapee.size();
	}
}
