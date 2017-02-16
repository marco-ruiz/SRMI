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

package org.bop.srmi.server;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.RMIClientSocketFactory;
import java.util.HashSet;
import java.util.Set;

import org.bop.srmi.Bridge;
import org.bop.srmi.exception.BridgeBindingException;
import org.bop.srmi.exception.UnresolvableRegistryException;

/**
 * @author Marco Ruiz
 * @since Nov 7, 2008
 */
public class SessionRegistry {
	
	private static Set<Bridge> exportedBridges = new HashSet<Bridge>();

	public static String getRegistryId(Class<?> sessionClass, String name) {
		String suffix =  (name == null) ? "" : "?name=" + name; 
	    return "SRMI:" + sessionClass.getName().replace('.', '/') + suffix;
    }

	public static String getLocalhost() {
		return System.getProperty("java.rmi.server.hostname", "127.0.0.1");
	}
	
	private String host;
	private int port = Registry.REGISTRY_PORT;
	private RMIClientSocketFactory csf = null;
	private Registry registry;

	public SessionRegistry() {
		this(getLocalhost(), Registry.REGISTRY_PORT);
	}

	public SessionRegistry(String host) {
	    this(host, Registry.REGISTRY_PORT);
    }

	public SessionRegistry(int port) {
	    this(getLocalhost(), port);
    }

	public SessionRegistry(String host, int port) {
	    setHost(host);
	    setPort(port);
    }

	public String getHost() {
    	return host;
    }

	public void setHost(String host) {
    	this.host = host;
    }
	
	public int getPort() {
    	return port;
    }

	public void setPort(int port) {
    	this.port = port;
    }

	public void setClientSocketFactory(RMIClientSocketFactory clientSocketFactory) {
    	this.csf = clientSocketFactory;
    }

	public void bind(Class<?> sessionInterface, String name, Bridge<?> bridge) throws UnresolvableRegistryException, BridgeBindingException {
		try {
			Registry registry = getRegistryForServer();
			registry.rebind(getRegistryId(sessionInterface, name), bridge);
			exportedBridges.add(bridge);
		} catch (RemoteException e) {
			throw new BridgeBindingException("Problems binding bridge for '" + sessionInterface + "' into RMI registry", e);
        }
	}

	private Registry getRegistryForServer() throws UnresolvableRegistryException {
		return getRegistry(true);
    }
	
	public Registry getRegistryForClient() throws UnresolvableRegistryException {
		return getRegistry(false);
    }

	private Registry getRegistry(boolean server) throws UnresolvableRegistryException {
		if (registry == null)  
		    try {
		    	registry = server ? 
	    			LocateRegistry.createRegistry(port, csf, null) : 
	    			LocateRegistry.getRegistry(host, port, csf);
		    } catch (RemoteException e) {
		    	String msg = server ? 
	    			"RMI registry could not be created on port [" + port + "] with client socket factory [" + csf + "]" :
	    			"RMI registry could not be located @ [" + host + ":" + port + "] with client socket factory [" + csf + "]";
		    	throw new UnresolvableRegistryException(msg, e);
		    }
		
		return registry;
    }

	public String toString() {
        return (registry == null) ? null : registry.toString();
	}
}

