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

import java.io.Serializable;


/**
 * @author Marco Ruiz
 * @since Apr 11, 2009
 */
public interface SessionHolderProcess extends Serializable {
	
	public static final SessionHolderProcess JOLT = new SessionHolderProcess() {

		public String getProcessName() {
	        return "jolt";
        }

		public boolean shallRemoveOnRetrieval() {
			return false;
        }

		public void process(SessionHolder<?, ?> holder) {
			holder.jolt();
        }
	};
	
	
	public static final SessionHolderProcess DECAY = new SessionHolderProcess() {

		public String getProcessName() {
	        return "decay";
        }

		public boolean shallRemoveOnRetrieval() {
			return true;
        }

		public void process(SessionHolder<?, ?> holder) {
			holder.decay();
        }
	};
	
	public static final SessionHolderProcess DESTROY = new SessionHolderProcess() {

		public String getProcessName() {
	        return "destroy";
        }

		public boolean shallRemoveOnRetrieval() {
			return true;
        }

		public void process(SessionHolder<?, ?> holder) {
			holder.destroy();
        }
	};
	
	public String getProcessName();
	public boolean shallRemoveOnRetrieval();
	public void process(SessionHolder<?, ?> holder);
}
