package org.fipro.inverter.integration.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.fipro.inverter.StringInverter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.junit5.service.ServiceExtension;

@ExtendWith(ServiceExtension.class)
public class IntegrationTest {

	@Test
	public void shouldInvertWithService(@InjectService StringInverter inverter) {
		assertNotNull(inverter, "No StringInverter service found");
		assertEquals("nospmiS", inverter.invert("Simpson"));
	}
}
