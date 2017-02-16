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
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

/**
 * @author Marco Ruiz
 * @since Dec 4, 2009
 */
public class SessionQueueImpl<QUEUE_ID_T extends Serializable, SESS_ID_T extends Serializable> implements SessionQueue<QUEUE_ID_T, SESS_ID_T> {

	private SESS_ID_T id;
	private final QueuesRepository<QUEUE_ID_T, SESS_ID_T> queues;
	private String serverName;

	public SessionQueueImpl(String serverName, SESS_ID_T id, QueuesRepository<QUEUE_ID_T, SESS_ID_T> queues) {
		this.serverName = serverName;
    	this.id = id;
    	this.queues = queues;
    }

	public SESS_ID_T getId() {
    	return id;
    }

	public int getNumberOfQueues() {
		return queues.size();
	}

	public List<QUEUE_ID_T> getQueuesIds() {
	    return new ArrayList<QUEUE_ID_T>(queues.keySet());
    }

	public final <ELE_T extends Serializable> void offer(QUEUE_ID_T queueId, ELE_T element) throws QueueNotFoundException {
		getQueue(queueId).offer(new ElementWrapper<SESS_ID_T, ELE_T>(id, element));
	}

	public final ElementWrapper<SESS_ID_T, ?> poll(QUEUE_ID_T queueId) throws QueueNotFoundException {
		return getQueue(queueId).poll();
	}
	
	private Queue<ElementWrapper<SESS_ID_T, ?>> getQueue(QUEUE_ID_T queueId) throws QueueNotFoundException {
	    Queue<ElementWrapper<SESS_ID_T, ?>> queue = queues.get(queueId);
	    if (queue == null) 
	    	throw new QueueNotFoundException(serverName, (Serializable)queueId);
		return queue;
    }
}

