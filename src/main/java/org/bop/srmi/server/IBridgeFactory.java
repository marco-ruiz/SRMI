/*
 * Copyright 2007-2008 the original author or authors. Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or agreed to in
 * writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package org.bop.srmi.server;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.rmi.server.RMIClientSocketFactory;


/*
 * @author Marco Ruiz
 * @since Feb 10, 2009
 */
public interface IBridgeFactory<SESSION_ID_TYPE extends Serializable> {
	
	public BridgeImpl<SESSION_ID_TYPE> createExportableBridge(RMIClientSocketFactory csf) throws RemoteException ;
}