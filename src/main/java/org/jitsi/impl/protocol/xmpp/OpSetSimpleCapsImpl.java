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
package org.jitsi.impl.protocol.xmpp;

import java.util.List;
import java.util.Set;

import org.jitsi.jicofo.discovery.DiscoveryUtil;
import org.jitsi.protocol.xmpp.OperationSetSimpleCaps;
import org.jivesoftware.smack.SmackException.NoResponseException;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.XMPPException;
import org.jxmpp.jid.Jid;

import net.java.sip.communicator.util.Logger;

/**
 *
 */
public class OpSetSimpleCapsImpl implements OperationSetSimpleCaps {
	/**
	 * The logger.
	 */
	private final static Logger logger = Logger.getLogger(OpSetSimpleCapsImpl.class);

	private final XmppProtocolProvider xmppProvider;

	public OpSetSimpleCapsImpl(XmppProtocolProvider xmppProtocolProvider) {
		this.xmppProvider = xmppProtocolProvider;
	}

	@Override
	public Set<Jid> getItems(Jid node) {
		try {
			return xmppProvider.discoverItems(node);
		} catch (XMPPException | InterruptedException | NoResponseException | NotConnectedException e) {
			logger.error("Error while discovering the services of " + node + " , error msg: " + e.getMessage());

			return null;
		}
	}

	@Override
	public boolean hasFeatureSupport(Jid node, String[] features) {
		List<String> itemFeatures = getFeatures(node);

		return itemFeatures != null && DiscoveryUtil.checkFeatureSupport(features, itemFeatures);

	}

	@Override
	public List<String> getFeatures(Jid node) {
		return xmppProvider.getEntityFeatures(node);
	}

	// @Override
	public boolean hasFeatureSupport(Jid node, String subnode, String[] features) {
		return xmppProvider.checkFeatureSupport(node, subnode, features);
	}
}
