package org.fipro.inverter.provider;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.fipro.inverter.StringInverter;
import org.junit.jupiter.api.Test;

public class StringInverterImplTest {

	@Test
	public void shouldInvertText() {
		StringInverter inverter = new StringInverterImpl();
		assertEquals("nospmiS", inverter.invert("Simpson"));
	}
}
