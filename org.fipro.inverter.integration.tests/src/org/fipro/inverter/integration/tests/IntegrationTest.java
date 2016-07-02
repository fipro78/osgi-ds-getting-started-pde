package org.fipro.inverter.integration.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.fipro.inverter.StringInverter;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.osgi.util.tracker.ServiceTracker;

public class IntegrationTest {

	@Test
	public void shouldInvertWithService() {
		StringInverter inverter = getService(StringInverter.class);
		assertNotNull("No StringInverter service found", inverter);
		assertEquals("nospmiS", inverter.invert("Simpson"));
	}
	
	static <T> T getService(Class<T> clazz) {
		Bundle bundle = FrameworkUtil.getBundle(IntegrationTest.class);
		if (bundle != null) {
			ServiceTracker<T, T> st = new ServiceTracker<T, T>(bundle.getBundleContext(), clazz, null);
			st.open();
			if (st != null) {
				try {
					return st.waitForService(500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		return null;
	}
}
