/*
 * Jicofo, the Jitsi Conference Focus.
 *
 * Copyright @ 2015 Atlassian Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jitsi.jicofo.util;

import net.java.sip.communicator.service.protocol.OperationFailedException;
import net.java.sip.communicator.service.protocol.ProtocolProviderService;
import net.java.sip.communicator.util.Logger;

/**
 * Thread does the job of registering given <tt>ProtocolProviderService</tt>.
 *
 * @author Pawel Domas
 */
public class RegisterThread extends Thread {
	/**
	 * The logger.
	 */
	private final static Logger logger = Logger.getLogger(RegisterThread.class);

	private final ProtocolProviderService pps;

	public RegisterThread(ProtocolProviderService pps) {
		this.pps = pps;
	}

	@Override
	public void run() {
		try {
			pps.register(new ServerSecurityAuthority());
		} catch (OperationFailedException e) {
			logger.error(e, e);
		}
	}
}
