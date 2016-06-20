package org.fipro.inverter.command;

import org.apache.felix.service.command.CommandProcessor;
import org.fipro.inverter.StringInverter;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(
	property= {
		CommandProcessor.COMMAND_SCOPE + ":String=fipro",
		CommandProcessor.COMMAND_FUNCTION + ":String=invert"},
	service=StringInverterCommand.class
)
public class StringInverterCommand {

	private StringInverter inverter;
	
	@Reference
	void bindStringInverter(StringInverter inverter) {
		this.inverter = inverter;
	}

	void updatedStringInverter(StringInverter inverter) {
		this.inverter = inverter;
	}
	
	public void invert(String input) {
		System.out.println(inverter.invert(input));
	}
}
