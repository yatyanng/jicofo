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
package org.jitsi.jicofo.xmpp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.jitsi.xmpp.util.IQUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import net.java.sip.communicator.impl.protocol.jabber.extensions.jibri.JibriIq;
import net.java.sip.communicator.impl.protocol.jabber.extensions.jibri.JibriIqProvider;

/**
 * Few basic tests for parsing JibriIQ
 *
 * @author Pawel Domas
 */
@RunWith(JUnit4.class)
public class JibriIqProviderTest {
	@Test
	public void testParseIQ() throws Exception {
		JibriIqProvider provider = new JibriIqProvider();

		// JibriIq
		String iqXml = "<iq to='t' from='f' type='set'>" + "<jibri xmlns='http://jitsi.org/protocol/jibri'"
				+ "   status='off' action='stop' failure_reason='error'" + "   session_id='abcd'" + "/>" +

				"</iq>";

		JibriIq jibriIq = IQUtils.parse(iqXml, provider);

		assertNotNull(jibriIq);

		assertEquals(JibriIq.Status.OFF, jibriIq.getStatus());
		assertEquals(JibriIq.Action.STOP, jibriIq.getAction());
		assertEquals(JibriIq.FailureReason.ERROR, jibriIq.getFailureReason());
		assertTrue(jibriIq.getSessionId().equalsIgnoreCase("abcd"));

		assertNull(jibriIq.getError());
	}
}
