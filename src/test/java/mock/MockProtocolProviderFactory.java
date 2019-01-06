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
package mock;

import java.util.Map;
import java.util.Objects;

import org.jitsi.eventadmin.EventAdmin;
import org.osgi.framework.BundleContext;

import net.java.sip.communicator.service.protocol.AccountID;
import net.java.sip.communicator.service.protocol.ProtocolNames;
import net.java.sip.communicator.service.protocol.ProtocolProviderFactory;
import net.java.sip.communicator.service.protocol.ProtocolProviderService;
import net.java.sip.communicator.util.ServiceUtils;

/**
 *
 */
public class MockProtocolProviderFactory extends ProtocolProviderFactory {

	/**
	 * Creates a new <tt>ProtocolProviderFactory</tt>.
	 *
	 * @param bundleContext the bundle context reference of the service
	 * @param protocolName  the name of the protocol
	 */
	public MockProtocolProviderFactory(BundleContext bundleContext, String protocolName) {
		super(bundleContext, protocolName);
	}

	@Override
	public AccountID installAccount(String userID, Map<String, String> accountProperties)
			throws IllegalArgumentException, IllegalStateException, NullPointerException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void modifyAccount(ProtocolProviderService protocolProvider, Map<String, String> accountProperties)
			throws NullPointerException {
		throw new UnsupportedOperationException();
	}

	@Override
	protected AccountID createAccountID(String userID, Map<String, String> accountProperties) {
		return new MockAccountID(userID, accountProperties, getProtocolName());
	}

	@Override
	protected ProtocolProviderService createService(String userID, AccountID accountID) {
		EventAdmin eventAdmin = Objects.requireNonNull(ServiceUtils.getService(getBundleContext(), EventAdmin.class),
				"eventAdmin");

		MockProtocolProvider protocolProvider = new MockProtocolProvider((MockAccountID) accountID, eventAdmin);

		protocolProvider.includeBasicTeleOpSet();

		if (ProtocolNames.JABBER.equals(getProtocolName())) {
			protocolProvider.includeMultiUserChatOpSet();
			protocolProvider.includeJitsiMeetTools();
			protocolProvider.includeColibriOpSet();
			protocolProvider.includeJingleOpSet();
			protocolProvider.includeSimpleCapsOpSet();
			protocolProvider.includeDirectXmppOpSet();
			protocolProvider.includeSubscriptionOpSet();
		}

		return protocolProvider;
	}
}
