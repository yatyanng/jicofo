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
package mock.muc;

import java.util.List;

import org.jitsi.jicofo.discovery.DiscoveryUtil;
import org.jitsi.protocol.xmpp.OperationSetSimpleCaps;
import org.jitsi.protocol.xmpp.XmppChatMember;
import org.jivesoftware.smack.packet.Presence;
import org.jxmpp.jid.EntityFullJid;
import org.jxmpp.jid.Jid;
import org.jxmpp.jid.parts.Resourcepart;

import mock.xmpp.MockCapsNode;
import mock.xmpp.MockSetSimpleCapsOpSet;
import net.java.sip.communicator.service.protocol.ChatRoom;
import net.java.sip.communicator.service.protocol.ChatRoomMemberRole;
import net.java.sip.communicator.service.protocol.Contact;
import net.java.sip.communicator.service.protocol.PresenceStatus;
import net.java.sip.communicator.service.protocol.ProtocolProviderService;
import net.java.sip.communicator.service.protocol.globalstatus.GlobalStatusEnum;

/**
 * @author Pawel Domas
 */
public class MockRoomMember implements XmppChatMember {
	private final Resourcepart name;

	private final EntityFullJid address;

	private final MockMultiUserChat room;

	private ChatRoomMemberRole role = ChatRoomMemberRole.MEMBER;

	MockRoomMember(EntityFullJid address, MockMultiUserChat chatRoom) {
		this.address = address;
		this.name = address.getResourceOrThrow();
		this.room = chatRoom;
	}

	public void setupFeatures(boolean useBundle) {
		OperationSetSimpleCaps caps = room.getParentProvider().getOperationSet(OperationSetSimpleCaps.class);

		MockSetSimpleCapsOpSet mockCaps = (MockSetSimpleCapsOpSet) caps;

		List<String> features = DiscoveryUtil.getDefaultParticipantFeatureSet();
		if (!useBundle) {
			features.remove(DiscoveryUtil.FEATURE_RTCP_MUX);
			features.remove(DiscoveryUtil.FEATURE_RTP_BUNDLE);
		}

		MockCapsNode myNode = new MockCapsNode(address, features.toArray(new String[features.size()]));

		mockCaps.addChildNode(myNode);
	}

	@Override
	public ChatRoom getChatRoom() {
		return room;
	}

	@Override
	public ProtocolProviderService getProtocolProvider() {
		return room.getParentProvider();
	}

	@Override
	public String getContactAddress() {
		return address.toString();
	}

	@Override
	public EntityFullJid getOccupantJid() {
		return address;
	}

	@Override
	public String getName() {
		return name.toString();
	}

	@Override
	public byte[] getAvatar() {
		return new byte[0];
	}

	@Override
	public Contact getContact() {
		return null;
	}

	@Override
	public ChatRoomMemberRole getRole() {
		return role;
	}

	public void leave() {
		room.mockLeave(getName());
	}

	@Override
	public void setRole(ChatRoomMemberRole role) {
		this.role = role;
	}

	@Override
	public PresenceStatus getPresenceStatus() {
		return GlobalStatusEnum.ONLINE;
	}

	@Override
	public String getDisplayName() {
		return null;
	}

	@Override
	public String toString() {
		return "Member@" + hashCode() + "[" + address + "]";
	}

	@Override
	public Jid getJid() {
		return address;
	}

	@Override
	public int getJoinOrderNumber() {
		// FIXME: implement in order to test start muted feature
		return 0;
	}

	@Override
	public Boolean hasVideoMuted() {
		// FIXME: not implemented
		return null;
	}

	@Override
	public boolean isRobot() {
		return false;
	}

	@Override
	public Presence getPresence() {
		// FIXME: not implemented
		return null;
	}

	@Override
	public String getRegion() {
		return null;
	}

	@Override
	public String getStatsId() {
		return null;
	}
}
