package org.fipro.inverter.provider;

import static org.junit.Assert.assertEquals;

import org.fipro.inverter.StringInverter;
import org.junit.Test;

public class StringInverterImplTest {

	@Test
	public void shouldInvertText() {
		StringInverter inverter = new StringInverterImpl();
		assertEquals("nospmiS", inverter.invert("Simpson"));
	}
}
