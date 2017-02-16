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

package org.bop.srmi.test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.sql.Timestamp;

import org.bop.srmi.SessionProxiesManager;
import org.bop.srmi.exception.BridgeConnectivityException;
import org.bop.srmi.exception.InstantiationException;
import org.bop.srmi.exception.InvalidSessionIdException;
import org.bop.srmi.exception.SRMIException;
import org.bop.srmi.exception.SessionRemoteException;
import org.bop.srmi.exception.UnresolvableRegistryException;
import org.bop.srmi.queue.QueueHandlerRepository;
import org.bop.srmi.queue.QueueNotFoundException;
import org.bop.srmi.queue.SessionQueue;
import org.bop.srmi.server.SessionRegistry;


/**
 * @author Marco Ruiz
 * @since Nov 4, 2008
 */
public class Client {
	
	public static void main(String[] args) throws Exception {

//		String host = "132.239.131.71";
		String host = SessionRegistry.getLocalhost();
//		String host = "birn-cluster0.nbirn.net";
		
		SessionRegistry registry = new SessionRegistry(host);
		
//		int sessionId = readSessionId();
//		testSessionQueue("masterQueueServer", sessionId, registry);
//		testSessionQueue("backupQueueServer", sessionId, registry);
		testSessionTests(registry);
	}

	//=======
	// QUEUE
	//=======
	private static <SESS_ID_T extends Serializable> void testSessionQueue(String serverName, SESS_ID_T sessionId, SessionRegistry registry) throws QueueNotFoundException, SessionRemoteException, UnresolvableRegistryException, BridgeConnectivityException, InvalidSessionIdException, InstantiationException {
		
		QueueHandlerRepository<String, SESS_ID_T> handlerRepo = new QueueHandlerRepository<String, SESS_ID_T>(sessionId, serverName, registry);
		SessionQueue<String, SESS_ID_T> session = handlerRepo.getSession();
		
		int count = 0;
		System.out.println("Server: [" + serverName + "]. " + " Queues: [" + session.getNumberOfQueues() + "]. " + "Queue Names: " + session.getQueuesIds() + ".");

		queueMsg(handlerRepo, "events", count++);
		queueMsg(handlerRepo, "events", count++);
		queueMsg(handlerRepo, "events", count++);
		queueMsg(handlerRepo, "events", count++);
		queueMsg(handlerRepo, "events", count++);
		
		queueMsg(handlerRepo, "log", count++);
		queueMsg(handlerRepo, "processResult", count++);
		queueMsg(handlerRepo, "log", count++);
		queueMsg(handlerRepo, "processResult", count++);
		queueMsg(handlerRepo, "log", count++);
		queueMsg(handlerRepo, "processResult", count++);

		queueMsg(handlerRepo, "events", count++);
		queueMsg(handlerRepo, "events", count++);
		queueMsg(handlerRepo, "events", count++);

		queueMsg(handlerRepo, "log", count++);
		queueMsg(handlerRepo, "log", count++);
		queueMsg(handlerRepo, "log", count++);
		queueMsg(handlerRepo, "log", count++);
		
		queueMsg(handlerRepo, "events", count++);
		queueMsg(handlerRepo, "events", count++);
		queueMsg(handlerRepo, "events", count++);
		queueMsg(handlerRepo, "events", count++);
    }
	
	private static <SESS_ID_T extends Serializable, QUEUE_ID_T extends Serializable> 
		void queueMsg(QueueHandlerRepository<QUEUE_ID_T, SESS_ID_T> repo, QUEUE_ID_T queueId, int count) {
		try {
	        repo.getQueue(queueId).offer(count  + " @ " + new Timestamp(System.currentTimeMillis()) + " by " + repo.getSession().getId());
        } catch (QueueNotFoundException e) {
	        e.printStackTrace();
        }
	}

	private static int readSessionId() {
		System.out.print("Input Session Id > ");
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		try {
	        return Integer.parseInt(in.readLine());
        } catch (Exception e) {
        	return 0;
        }
	}
	
	//==============
	// SESSION TEST
	//==============
	private static void testSessionTests(SessionRegistry registry) {
	    // Create session proxys
		SessionProxiesManager<Integer, SessionTest> sessionProxiesManager = new SessionProxiesManager<Integer, SessionTest>(registry) {};
		
		SessionTest session5_1 = createSession(sessionProxiesManager, 5);
		testSessionProxy(session5_1,  1, 2, 4, 8);
		SessionTest session10 = createSession(sessionProxiesManager, 10);
		testSessionProxy(session10,  1, 2, 4, 8);
		SessionTest session11 = createSession(sessionProxiesManager, -1);
		testSessionProxy(session11,  1, 2, 4, 8);
//		test(sessionProxiesManager, 0, 15, 6, 3);
		SessionTest session5_2 = createSession(sessionProxiesManager, 5);
		testSessionProxy(session5_1,  1, 2, 4, 8);
		testSessionProxy(session5_2,  1, 2, 4, 8);
/*
        try {
        	System.out.println(session20_1.divideSessionIdBy(100));
        	System.out.println(session20_2.divideSessionIdBy(50));
	        sessionProxiesManager.destroySession(session20_1);
	        testExistance(sessionProxiesManager, 20);
        	System.out.println(session20_1.divideSessionIdBy(100));
        	System.out.println(session20_2.divideSessionIdBy(50));
        } catch (Exception e) { e.printStackTrace(); }
*/
		SessionTest session1 = createSession(sessionProxiesManager, 1);
		testSessionProxy(session1, 1, 2, 4, 8);
/*
        try {
        	System.out.println(session20_1.divideSessionIdBy(100));
        	System.out.println(session20_2.divideSessionIdBy(50));
        } catch (Exception e) { e.printStackTrace(); }
*/
    }
	
	public static <ID_T extends Serializable, SESS_T> SESS_T createSession(SessionProxiesManager<ID_T, SESS_T> sessionsManager, ID_T sessionId) {
        try {
        	System.out.println("Exists session '" + sessionId + "'? " + sessionsManager.existsSession(sessionId));
    		SESS_T result = sessionsManager.getSession(sessionId);
    		System.out.println("Id:" + sessionsManager.getSessionId(result));
			return result;
        } catch (Exception e) { e.printStackTrace(); }
		return null;
	}
	
	private static void testSessionProxy(SessionTest sessionProxy, int... divisors) {
	    try {
	        System.out.println(sessionProxy.getTime());
        } catch (SRMIException e) { e.printStackTrace(); }
        
	    for (int divisor : divisors) {
	        try {
	    		System.out.println(sessionProxy.divideSessionIdBy(divisor));
	        } catch (Exception e) { e.printStackTrace(); }
	    }
    }
}

