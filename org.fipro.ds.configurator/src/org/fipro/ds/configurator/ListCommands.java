package org.fipro.ds.configurator;

import java.util.List;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.AnyService;

@Component(
	    property= {
	        "osgi.command.scope:String=fipro",
	        "osgi.command.function:String=listcmd"
	    },
	    service=ListCommands.class
	)
public class ListCommands {

	@Reference(
		service = AnyService.class, 
		target="(osgi.command.scope=fipro)")
	volatile List<Object> commands;
	
	public void listcmd() {
		commands.forEach(cmd -> System.out.println(cmd.getClass()));
	}
}
