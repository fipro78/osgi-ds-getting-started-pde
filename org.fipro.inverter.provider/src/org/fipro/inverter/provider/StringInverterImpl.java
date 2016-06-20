package org.fipro.inverter.provider;

import org.fipro.inverter.StringInverter;
import org.osgi.service.component.annotations.Component;

@Component
public class StringInverterImpl implements StringInverter {

	@Override
	public String invert(String input) {
		return new StringBuilder(input).reverse().toString();
	}

}
