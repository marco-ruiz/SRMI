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

import java.util.HashMap;
import java.util.Map;

/**
 * @author Marco Ruiz
 * @since Nov 19, 2007
 */
public class BlockingMap<KEY_TYPE, ELE_TYPE> {
	
	private Map<KEY_TYPE, ELE_TYPE> wrapee = new HashMap<KEY_TYPE, ELE_TYPE>();
	
	public synchronized ELE_TYPE get(KEY_TYPE id) {
		return wrapee.get(id);
	}
	
	public synchronized void put(KEY_TYPE id, ELE_TYPE value) {
		wrapee.put(id, value);
		this.notifyAll();
	}
	
	public synchronized ELE_TYPE remove(KEY_TYPE key) {
		return wrapee.remove(key);
	}

	public synchronized Map<KEY_TYPE, ELE_TYPE> takeAll() {
		waitUntilNotEmpty();
		Map<KEY_TYPE, ELE_TYPE> copy = new HashMap<KEY_TYPE, ELE_TYPE>();
		for (KEY_TYPE id : wrapee.keySet()) copy.put(id, wrapee.get(id));
		return copy;
	}

	public synchronized ELE_TYPE takeOne() {
		waitUntilNotEmpty();
		return wrapee.remove(wrapee.keySet().iterator().next());
	}

	private void waitUntilNotEmpty() {
		while (wrapee.isEmpty()) try { this.wait(); } catch (InterruptedException e) {}
	}

	public synchronized int size() {
		return wrapee.size();
	}
}
