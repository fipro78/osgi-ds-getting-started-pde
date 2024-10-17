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

	@Reference
	private StringInverter inverter;
	
	public void invert(String input) {
		System.out.println(inverter.invert(input));
	}
}
