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

import org.jitsi.eventadmin.EventAdmin;
import org.jitsi.protocol.xmpp.AbstractOperationSetJingle;
import org.jitsi.protocol.xmpp.OperationSetDirectSmackXmpp;
import org.jitsi.protocol.xmpp.OperationSetJingle;
import org.jitsi.protocol.xmpp.OperationSetSimpleCaps;
import org.jitsi.protocol.xmpp.OperationSetSubscription;
import org.jitsi.protocol.xmpp.XmppConnection;
import org.jitsi.protocol.xmpp.colibri.OperationSetColibriConference;
import org.jxmpp.jid.EntityFullJid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.stringprep.XmppStringprepException;

import mock.muc.MockMultiUserChatOpSet;
import mock.xmpp.MockOperationSetJingle;
import mock.xmpp.MockSetSimpleCapsOpSet;
import mock.xmpp.MockSmackXmppOpSet;
import mock.xmpp.MockXmppConnection;
import mock.xmpp.colibri.MockColibriOpSet;
import mock.xmpp.pubsub.MockSubscriptionOpSetImpl;
import net.java.sip.communicator.service.protocol.AbstractProtocolProviderService;
import net.java.sip.communicator.service.protocol.AccountID;
import net.java.sip.communicator.service.protocol.OperationFailedException;
import net.java.sip.communicator.service.protocol.OperationSetBasicTelephony;
import net.java.sip.communicator.service.protocol.OperationSetJitsiMeetTools;
import net.java.sip.communicator.service.protocol.OperationSetMultiUserChat;
import net.java.sip.communicator.service.protocol.ProtocolIcon;
import net.java.sip.communicator.service.protocol.RegistrationState;
import net.java.sip.communicator.service.protocol.SecurityAuthority;
import net.java.sip.communicator.service.protocol.TransportProtocol;
import net.java.sip.communicator.service.protocol.event.RegistrationStateChangeEvent;
import net.java.sip.communicator.util.Logger;

/**
 *
 * @author Pawel Domas
 */
public class MockProtocolProvider extends AbstractProtocolProviderService {
	/**
	 * The logger.
	 */
	private final static Logger logger = Logger.getLogger(MockProtocolProvider.class);

	private final MockAccountID accountId;

	private final EventAdmin eventAdmin;

	private RegistrationState registrationState = RegistrationState.UNREGISTERED;

	private MockXmppConnection connection;

	private AbstractOperationSetJingle jingleOpSet;

	public MockProtocolProvider(MockAccountID accountId, EventAdmin eventAdmin) {
		this.accountId = accountId;
		this.eventAdmin = eventAdmin;
	}

	@Override
	public void register(SecurityAuthority authority) throws OperationFailedException {
		if (jingleOpSet != null) {
			connection.registerIQRequestHandler(jingleOpSet);
		}

		setRegistrationState(RegistrationState.REGISTERED, RegistrationStateChangeEvent.REASON_NOT_SPECIFIED, null);
	}

	private void setRegistrationState(RegistrationState newState, int reasonCode, String reason) {
		RegistrationState oldState = getRegistrationState();

		this.registrationState = newState;

		fireRegistrationStateChanged(oldState, newState, reasonCode, reason);
	}

	@Override
	public void unregister() throws OperationFailedException {
		if (jingleOpSet != null) {
			connection.unregisterIQRequestHandler(jingleOpSet);
		}

		setRegistrationState(RegistrationState.UNREGISTERED, RegistrationStateChangeEvent.REASON_NOT_SPECIFIED, null);
	}

	@Override
	public RegistrationState getRegistrationState() {
		return registrationState;
	}

	@Override
	public String getProtocolName() {
		return accountId.getProtocolName();
	}

	@Override
	public ProtocolIcon getProtocolIcon() {
		return null;
	}

	@Override
	public void shutdown() {
		try {
			unregister();
		} catch (OperationFailedException e) {
			logger.error(e, e);
		}
	}

	@Override
	public AccountID getAccountID() {
		return accountId;
	}

	@Override
	public boolean isSignalingTransportSecure() {
		return false;
	}

	@Override
	public TransportProtocol getTransportProtocol() {
		return null;
	}

	public void includeBasicTeleOpSet() {
		addSupportedOperationSet(OperationSetBasicTelephony.class, new MockBasicTeleOpSet(this));
	}

	public void includeMultiUserChatOpSet() {
		addSupportedOperationSet(OperationSetMultiUserChat.class, new MockMultiUserChatOpSet(this));
	}

	public void includeColibriOpSet() {
		addSupportedOperationSet(OperationSetColibriConference.class, new MockColibriOpSet(this, eventAdmin));
	}

	public void includeJingleOpSet() {
		this.jingleOpSet = new MockOperationSetJingle(this);

		addSupportedOperationSet(OperationSetJingle.class, jingleOpSet);
	}

	public void includeSimpleCapsOpSet() {
		try {
			addSupportedOperationSet(OperationSetSimpleCaps.class,
					new MockSetSimpleCapsOpSet(JidCreate.from(accountId.getServerAddress())));
		} catch (XmppStringprepException e) {
			throw new RuntimeException(e);
		}
	}

	public void includeDirectXmppOpSet() {
		addSupportedOperationSet(OperationSetDirectSmackXmpp.class, new MockSmackXmppOpSet(this));
	}

	public void includeJitsiMeetTools() {
		addSupportedOperationSet(OperationSetJitsiMeetTools.class, new MockJitsiMeetTools(this));
	}

	public void includeSubscriptionOpSet() {
		addSupportedOperationSet(OperationSetSubscription.class, new MockSubscriptionOpSetImpl());
	}

	public OperationSetBasicTelephony getTelephony() {
		return getOperationSet(OperationSetBasicTelephony.class);
	}

	public XmppConnection getXmppConnection() {
		if (this.connection == null) {
			this.connection = new MockXmppConnection(getOurJID());
		}
		return connection;
	}

	public EntityFullJid getOurJID() {
		try {
			return JidCreate.entityFullFrom("mock-" + accountId.getAccountAddress());
		} catch (XmppStringprepException e) {
			throw new RuntimeException(e);
		}
	}

	public MockMultiUserChatOpSet getMockChatOpSet() {
		return (MockMultiUserChatOpSet) getOperationSet(OperationSetMultiUserChat.class);
	}

	public MockSubscriptionOpSetImpl getMockSubscriptionOpSet() {
		return (MockSubscriptionOpSetImpl) getOperationSet(OperationSetSubscription.class);
	}

	public MockSetSimpleCapsOpSet getMockCapsOpSet() {
		return (MockSetSimpleCapsOpSet) getOperationSet(OperationSetSimpleCaps.class);
	}

	public MockColibriOpSet getMockColibriOpSet() {
		return (MockColibriOpSet) getOperationSet(OperationSetColibriConference.class);
	}
}
