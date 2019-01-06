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
package org.jitsi.jicofo;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.jitsi.eventadmin.EventAdmin;
import org.jitsi.jicofo.util.DaemonThreadFactory;
import org.jitsi.jicofo.util.JingleOfferFactory;
import org.jitsi.osgi.OSGIServiceRef;
import org.jitsi.service.configuration.ConfigurationService;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import net.java.sip.communicator.impl.protocol.jabber.extensions.caps.EntityCapsManager;

/**
 * Activator of the Jitsi Meet Focus bundle.
 *
 * @author Pawel Domas
 */
public class FocusBundleActivator implements BundleActivator {
	/**
	 * The number of threads available in the thread pool shared through OSGi.
	 */
	private static final int SHARED_POOL_SIZE = 20;

	/**
	 * OSGi bundle context held by this activator.
	 */
	public static BundleContext bundleContext;

	/**
	 * {@link ConfigurationService} instance cached by the activator.
	 */
	private static OSGIServiceRef<ConfigurationService> configServiceRef;

	/**
	 * The Jingle offer factory to use in this bundle.
	 */
	private static JingleOfferFactory jingleOfferFactory;

	/**
	 * {@link EventAdmin} service reference.
	 */
	private static OSGIServiceRef<EventAdmin> eventAdminRef;

	/**
	 * Shared thread pool available through OSGi for other components that do not
	 * like to manage their own pool.
	 */
	private static ScheduledExecutorService sharedThreadPool;

	/**
	 * {@link org.jitsi.jicofo.FocusManager} instance created by this activator.
	 */
	private FocusManager focusManager;

	/**
	 * <tt>FocusManager</tt> service registration.
	 */
	private ServiceRegistration<FocusManager> focusManagerRegistration;

	/**
	 * Global configuration of Jitsi COnference FOcus
	 */
	private JitsiMeetGlobalConfig globalConfig;

	@Override
	public void start(BundleContext context) throws Exception {
		bundleContext = context;

		EntityCapsManager.setBundleContext(context);

		// Make threads daemon, so that they won't prevent from doing shutdown
		sharedThreadPool = Executors.newScheduledThreadPool(SHARED_POOL_SIZE, new DaemonThreadFactory());

		eventAdminRef = new OSGIServiceRef<>(context, EventAdmin.class);

		configServiceRef = new OSGIServiceRef<>(context, ConfigurationService.class);

		jingleOfferFactory = new JingleOfferFactory(configServiceRef.get());

		context.registerService(ExecutorService.class, sharedThreadPool, null);
		context.registerService(ScheduledExecutorService.class, sharedThreadPool, null);

		globalConfig = JitsiMeetGlobalConfig.startGlobalConfigService(context);

		focusManager = new FocusManager();
		focusManager.start();
		focusManagerRegistration = context.registerService(FocusManager.class, focusManager, null);
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		if (focusManagerRegistration != null) {
			focusManagerRegistration.unregister();
			focusManagerRegistration = null;
		}
		if (focusManager != null) {
			focusManager.stop();
			focusManager = null;
		}

		sharedThreadPool.shutdownNow();
		sharedThreadPool = null;

		configServiceRef = null;
		eventAdminRef = null;

		EntityCapsManager.setBundleContext(null);

		if (globalConfig != null) {
			globalConfig.stopGlobalConfigService();
			globalConfig = null;
		}
	}

	/**
	 * Returns the instance of <tt>ConfigurationService</tt>.
	 */
	public static ConfigurationService getConfigService() {
		return configServiceRef.get();
	}

	/**
	 * Gets the Jingle offer factory to use in this bundle.
	 *
	 * @return the Jingle offer factory to use in this bundle
	 */
	public static JingleOfferFactory getJingleOfferFactory() {
		return jingleOfferFactory;
	}

	/**
	 * Returns the <tt>EventAdmin</tt> instance, if any.
	 * 
	 * @return the <tt>EventAdmin</tt> instance, if any.
	 */
	public static EventAdmin getEventAdmin() {
		return eventAdminRef.get();
	}

	/**
	 * Returns shared thread pool service.
	 */
	public static ScheduledExecutorService getSharedThreadPool() {
		return sharedThreadPool;
	}
}
