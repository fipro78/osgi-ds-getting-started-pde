package org.fipro.headless.app;

import java.util.Arrays;
import java.util.Map;

import org.fipro.inverter.StringInverter;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(immediate = true)
public class BndStarter {

	/**
	 * Launcher arguments provided by the bnd launcher.
	 */
	String[] launcherArgs;

	@Reference(target = "(launcher.arguments=*)")
	void setLauncherArguments(Object object, Map<String, Object> map) {
		this.launcherArgs = (String[]) map.get("launcher.arguments");
	}

	@Reference
	StringInverter inverter;
	
	@Activate
	void activate() {
		String console = System.getProperty("osgi.console");
		boolean isConsoleConfigured =  console != null && console.length() == 0;

		// clear launcher arguments from possible framework parameter
		String[] args = Arrays.stream(launcherArgs)
				.filter(arg -> !"-console".equals(arg) && !"-consoleLog".equals(arg))
				.toArray(String[]::new);

		for (String arg : args) {
			System.out.println(inverter.invert(arg));
		}

		if (!isConsoleConfigured) {
			// shutdown the application if no console is configured
			// only needed if osgi.noShutdown=true is configured
			System.exit(0);
		}
	}
}