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

import java.util.Queue;

import org.bop.srmi.exception.BridgeBindingException;
import org.bop.srmi.exception.UnresolvableRegistryException;
import org.bop.srmi.queue.ElementWrapper;
import org.bop.srmi.queue.IQueueElementProcessor;
import org.bop.srmi.queue.QueueInspectorService;
import org.bop.srmi.queue.QueueServer;
import org.bop.srmi.queue.WaitingConcurrentLinkedQueue;
import org.bop.srmi.server.ConstantSessionDecayPeriodResolver;
import org.bop.srmi.server.ISessionAuthenticator;
import org.bop.srmi.server.ReflectionSessionsFactory;
import org.bop.srmi.server.SessionRegistry;
import org.bop.srmi.server.SessionsManager;

/**
 * @author Marco Ruiz
 * @since Nov 4, 2008
 */
public class Server {
	public static void main(String[] args) throws BridgeBindingException, UnresolvableRegistryException, NoSuchMethodException {

//		String host = "132.239.131.71";
//		String host = "birn-cluster0.nbirn.net";
		String host = SessionRegistry.getLocalhost();

		// SESSION TEST
		//--------------
		SessionsManager<Integer, SessionTest> sessionsManager = new SessionsManager<Integer, SessionTest>(
				new ReflectionSessionsFactory<Integer, SessionTest, SessionTestImpl>() {}, 
				new ISessionAuthenticator<Integer>() { public boolean authenticate(Integer id) { return (id != 0); } }, 
				new ConstantSessionDecayPeriodResolver<Integer>(2000));
		sessionsManager.publish();
		
		// QUEUE SERVER
		//--------------
		createSystemQueueServer("masterQueueServer");
		createSystemQueueServer("backupQueueServer");

		System.out.println("Server Running...");
	}

	private static void createSystemQueueServer(String serverName) throws UnresolvableRegistryException, BridgeBindingException {
	    QueueServer<String, Integer> queueServer = new QueueServer<String, Integer>(serverName);
		queueServer.registerQueue("events",         createWaitingQueue());
		queueServer.registerQueue("log",            createWaitingQueue());
//		queueServer.registerQueue("processResults", createWaitingQueue());

		queueServer.start();
		
		runQueueInspector(queueServer, "events");
		runQueueInspector(queueServer, "log");
		runQueueInspector(queueServer, "processResults");
    }

	private static Queue<ElementWrapper<Integer, ?>> createWaitingQueue() {
	    return new WaitingConcurrentLinkedQueue<ElementWrapper<Integer, ?>>();
    }

	private static void runQueueInspector(final QueueServer<String, Integer> server, final String queueId) {
	    new QueueInspectorService<Integer>(server.getQueue(queueId), new IQueueElementProcessor<Integer>() {
	    	public void processElement(ElementWrapper<Integer, ?> ele) {
	    		String output = "Session # [" + ele.getSessionId() + "] sent a new message to queue [" + server.getName() + "." + queueId + "] : [" + ele.getElement() + "]";
				System.out.println(output);
	        }
		});
    }
}

