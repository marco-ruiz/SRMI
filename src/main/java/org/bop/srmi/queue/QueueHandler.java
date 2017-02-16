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

/**
 * @author Marco Ruiz
 * @since Dec 8, 2009
 */
public class QueueHandler<QUEUE_ID_T extends Serializable, SESS_ID_T extends Serializable> {
	
	private QUEUE_ID_T queueId;
	private SessionQueue<QUEUE_ID_T, SESS_ID_T> session;
	
	public QueueHandler(QUEUE_ID_T queueId, SessionQueue<QUEUE_ID_T, SESS_ID_T> session) {
	    this.queueId = queueId;
	    this.session = session;
    }

	public void offer(Serializable element) throws QueueNotFoundException {
		session.offer(queueId, element);
	}
}

