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

import java.util.Hashtable;

import org.jitsi.impl.protocol.xmpp.extensions.ConferenceIqProvider;
import org.jitsi.impl.protocol.xmpp.extensions.LoginUrlIqProvider;
import org.jitsi.impl.protocol.xmpp.extensions.LogoutIqProvider;
import org.jitsi.impl.protocol.xmpp.extensions.RegionPacketExtension;
import org.jitsi.impl.protocol.xmpp.extensions.UserInfoPacketExt;
import org.jitsi.jicofo.discovery.VersionIqProvider;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.parsing.ExceptionLoggingCallback;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smackx.bytestreams.socks5.Socks5Proxy;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import net.java.sip.communicator.impl.protocol.jabber.extensions.DefaultPacketExtensionProvider;
import net.java.sip.communicator.impl.protocol.jabber.extensions.colibri.ColibriIQProvider;
import net.java.sip.communicator.impl.protocol.jabber.extensions.health.HealthCheckIQProvider;
import net.java.sip.communicator.impl.protocol.jabber.extensions.jibri.JibriIq;
import net.java.sip.communicator.impl.protocol.jabber.extensions.jibri.JibriIqProvider;
import net.java.sip.communicator.impl.protocol.jabber.extensions.jibri.JibriStatusPacketExt;
import net.java.sip.communicator.impl.protocol.jabber.extensions.jitsimeet.StatsId;
import net.java.sip.communicator.impl.protocol.jabber.extensions.jitsimeet.TranscriptionRequestExtension;
import net.java.sip.communicator.impl.protocol.jabber.extensions.jitsimeet.TranscriptionStatusExtension;
import net.java.sip.communicator.impl.protocol.jabber.extensions.jitsimeet.VideoMutedExtension;
import net.java.sip.communicator.service.protocol.ProtocolNames;
import net.java.sip.communicator.service.protocol.ProtocolProviderFactory;

/**
 * Bundle activator for {@link XmppProtocolProvider}.
 *
 * @author Pawel Domas
 */
public class XmppProtocolActivator implements BundleActivator {
	private ServiceRegistration<?> focusRegistration;

	static BundleContext bundleContext;

	/**
	 * Registers PacketExtension providers used by Jicofo
	 */
	static public void registerXmppExtensions() {
		// Constructors called to register extension providers
		new ConferenceIqProvider();
		new LoginUrlIqProvider();
		new LogoutIqProvider();
		// Colibri
		new ColibriIQProvider();
		// HealthChecks
		HealthCheckIQProvider.registerIQProvider();
		// Jibri IQs
		ProviderManager.addIQProvider(JibriIq.ELEMENT_NAME, JibriIq.NAMESPACE, new JibriIqProvider());
		JibriStatusPacketExt.registerExtensionProvider();
		// User info
		ProviderManager.addExtensionProvider(UserInfoPacketExt.ELEMENT_NAME, UserInfoPacketExt.NAMESPACE,
				new DefaultPacketExtensionProvider<>(UserInfoPacketExt.class));
		// <videomuted> element from jitsi-meet presence
		ProviderManager.addExtensionProvider(VideoMutedExtension.ELEMENT_NAME, VideoMutedExtension.NAMESPACE,
				new DefaultPacketExtensionProvider<>(VideoMutedExtension.class));
		ProviderManager.addExtensionProvider(RegionPacketExtension.ELEMENT_NAME, RegionPacketExtension.NAMESPACE,
				new DefaultPacketExtensionProvider<>(RegionPacketExtension.class));
		ProviderManager.addExtensionProvider(StatsId.ELEMENT_NAME, StatsId.NAMESPACE, new StatsId.Provider());

		// Add the extensions used for handling the inviting of transcriber
		ProviderManager.addExtensionProvider(TranscriptionRequestExtension.ELEMENT_NAME,
				TranscriptionRequestExtension.NAMESPACE,
				new DefaultPacketExtensionProvider<>(TranscriptionRequestExtension.class));
		ProviderManager.addExtensionProvider(TranscriptionStatusExtension.ELEMENT_NAME,
				TranscriptionStatusExtension.NAMESPACE,
				new DefaultPacketExtensionProvider<>(TranscriptionStatusExtension.class));

		// Override original Smack Version IQ class
		ProviderManager.addIQProvider(org.jivesoftware.smackx.iqversion.packet.Version.ELEMENT,
				org.jivesoftware.smackx.iqversion.packet.Version.NAMESPACE, new VersionIqProvider());
	}

	@Override
	public void start(BundleContext bundleContext) throws Exception {
		XmppProtocolActivator.bundleContext = bundleContext;

		SmackConfiguration.setDefaultReplyTimeout(15000);
		// if there is a parsing error, do not break the connection to
		// the server(the default behaviour) as we need it for
		// the other conferences
		SmackConfiguration.setDefaultParsingExceptionCallback(new ExceptionLoggingCallback());

		Socks5Proxy.setLocalSocks5ProxyEnabled(false);

		registerXmppExtensions();

		XmppProviderFactory focusFactory = new XmppProviderFactory(bundleContext, ProtocolNames.JABBER);
		Hashtable<String, String> hashtable = new Hashtable<>();

		// Register XMPP
		hashtable.put(ProtocolProviderFactory.PROTOCOL, ProtocolNames.JABBER);

		focusRegistration = bundleContext.registerService(ProtocolProviderFactory.class.getName(), focusFactory,
				hashtable);
	}

	@Override
	public void stop(BundleContext bundleContext) throws Exception {
		if (focusRegistration != null)
			focusRegistration.unregister();
	}
}
