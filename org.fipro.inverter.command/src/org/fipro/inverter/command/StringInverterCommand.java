package org.fipro.inverter.command;

import org.fipro.inverter.StringInverter;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(
	property= {
		"osgi.command.scope:String=fipro",
		"osgi.command.function:String=invert"},
	service=StringInverterCommand.class
)
public class StringInverterCommand {

	private StringInverter inverter;
	
	@Reference
	void bindStringInverter(StringInverter inverter) {
		this.inverter = inverter;
	}

	void unbindStringInverter(StringInverter inverter) {
		this.inverter = null;
	}
	
	public void invert(String input) {
		System.out.println(inverter.invert(input));
	}
}
