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
package mock.jvb;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jitsi.impl.neomedia.rtp.MediaStreamTrackDesc;
import org.jitsi.impl.neomedia.rtp.RTPEncodingDesc;
import org.jitsi.protocol.xmpp.XmppConnection;
import org.jitsi.util.ArrayUtils;
import org.jitsi.videobridge.Conference;
import org.jitsi.videobridge.Content;
import org.jitsi.videobridge.VideoChannel;
import org.jitsi.videobridge.Videobridge;
import org.jitsi.videobridge.VideobridgeBundleActivator;
import org.jivesoftware.smack.iqrequest.AbstractIqRequestHandler;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.XMPPError;
import org.jxmpp.jid.Jid;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import net.java.sip.communicator.impl.protocol.jabber.extensions.colibri.ColibriConferenceIQ;
import net.java.sip.communicator.impl.protocol.jabber.extensions.health.HealthCheckIQ;
import net.java.sip.communicator.util.Logger;
import net.java.sip.communicator.util.ServiceUtils;

/**
 *
 * @author Pawel Domas
 */
public class MockVideobridge implements BundleActivator {
	/**
	 * The logger
	 */
	private static final Logger logger = Logger.getLogger(MockVideobridge.class);

	private final XmppConnection connection;

	private final Jid bridgeJid;

	private Videobridge bridge;

	private boolean returnServerError = false;

	private VideobridgeBundleActivator jvbActivator;

	private ColibriConferenceIqHandler confIqGetHandler = new ColibriConferenceIqHandler(IQ.Type.get);

	private ColibriConferenceIqHandler confIqSetHandler = new ColibriConferenceIqHandler(IQ.Type.set);

	private HealthCheckIqHandler healthCheckIqHandler = new HealthCheckIqHandler();

	public MockVideobridge(XmppConnection connection, Jid bridgeJid) {
		this.connection = connection;
		this.bridgeJid = bridgeJid;
	}

	@Override
	public void start(BundleContext bc) throws Exception {
		this.jvbActivator = new VideobridgeBundleActivator();

		jvbActivator.start(bc);

		bridge = ServiceUtils.getService(bc, Videobridge.class);

		connection.registerIQRequestHandler(confIqGetHandler);
		connection.registerIQRequestHandler(confIqSetHandler);
		connection.registerIQRequestHandler(healthCheckIqHandler);
	}

	@Override
	public void stop(BundleContext bundleContext) throws Exception {
		connection.unregisterIQRequestHandler(confIqGetHandler);
		connection.unregisterIQRequestHandler(confIqSetHandler);
		connection.unregisterIQRequestHandler(healthCheckIqHandler);

		jvbActivator.stop(bundleContext);
	}

	private class ColibriConferenceIqHandler extends AbstractIqRequestHandler {
		ColibriConferenceIqHandler(IQ.Type type) {
			super(ColibriConferenceIQ.ELEMENT_NAME, ColibriConferenceIQ.NAMESPACE, type, Mode.sync);
		}

		@Override
		public IQ handleIQRequest(IQ iqRequest) {
			if (isReturnServerError()) {
				return IQ.createErrorResponse(iqRequest,
						XMPPError.getBuilder(XMPPError.Condition.internal_server_error));
			}

			try {
				IQ confResult = bridge.handleColibriConferenceIQ((ColibriConferenceIQ) iqRequest,
						Videobridge.OPTION_ALLOW_ANY_FOCUS);
				confResult.setTo(iqRequest.getFrom());
				confResult.setStanzaId(iqRequest.getStanzaId());
				return confResult;
			} catch (Exception e) {
				logger.error("JVB internal error!", e);
				return null;
			}
		}
	}

	private class HealthCheckIqHandler extends AbstractIqRequestHandler {
		HealthCheckIqHandler() {
			super(HealthCheckIQ.ELEMENT_NAME, HealthCheckIQ.NAMESPACE, IQ.Type.get, Mode.sync);
		}

		@Override
		public IQ handleIQRequest(IQ iqRequest) {
			if (isReturnServerError()) {
				return IQ.createErrorResponse(iqRequest,
						XMPPError.getBuilder(XMPPError.Condition.internal_server_error));
			}

			try {
				IQ healthResult = bridge.handleHealthCheckIQ((HealthCheckIQ) iqRequest);
				healthResult.setTo(iqRequest.getFrom());
				healthResult.setStanzaId(iqRequest.getStanzaId());
				return healthResult;
			} catch (Exception e) {
				logger.error("JVB internal error!", e);
				return null;
			}
		}
	}

	public List<RTPEncodingDesc> getSimulcastLayers(String confId, String channelId) {
		Conference conference = bridge.getConference(confId, null);
		Content videoContent = conference.getOrCreateContent("video");
		VideoChannel videoChannel = (VideoChannel) videoContent.getChannel(channelId);

		MediaStreamTrackDesc[] tracks = videoChannel.getStream().getMediaStreamTrackReceiver().getMediaStreamTracks();

		if (ArrayUtils.isNullOrEmpty(tracks))
			return new ArrayList<>();

		RTPEncodingDesc[] layers = tracks[0].getRTPEncodings();
		if (ArrayUtils.isNullOrEmpty(layers))
			return new ArrayList<>();

		return Arrays.asList(layers);
	}

	public int getChannelsCount() {
		int count = 0;
		for (Conference conference : bridge.getConferences()) {
			for (Content content : conference.getContents()) {
				count += content.getChannelCount();
			}
		}
		return count;
	}

	public int getChannelCountByContent(String contentName) {
		int count = 0;
		boolean any = false;

		for (Conference conference : bridge.getConferences()) {
			for (Content content : conference.getContents()) {
				if (contentName.equals(content.getName())) {
					any = true;
					count += content.getChannelCount();
				}
			}
		}
		return any ? count : -1;
	}

	public Jid getBridgeJid() {
		return bridgeJid;
	}

	public int getConferenceCount() {
		// Filter out conferences created for health checks
		return (int) Arrays.stream(bridge.getConferences()).filter(Conference::includeInStatistics).count();
	}

	public boolean isReturnServerError() {
		return returnServerError;
	}

	public void setReturnServerError(boolean returnServerError) {
		this.returnServerError = returnServerError;
	}
}
