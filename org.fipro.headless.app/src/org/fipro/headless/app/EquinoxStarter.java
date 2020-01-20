package org.fipro.headless.app;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;

import org.eclipse.osgi.service.environment.EnvironmentInfo;
import org.fipro.inverter.StringInverter;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(immediate = true)
public class EquinoxStarter {

	/**
	 * Launcher arguments provided by the Equinox launcher.
	 */
	@Reference
	EnvironmentInfo environmentInfo;

	@Reference
	StringInverter inverter;

	@Activate
	void activate() {
		if (environmentInfo.getFrameworkArgs() != null
				&& environmentInfo.getNonFrameworkArgs() != null) {
			
			// check if -console was provided as argument
			boolean isInteractive = Arrays.stream(environmentInfo.getFrameworkArgs())
					.anyMatch(arg -> "-console".equals(arg));
			// check if -console was provided as argument
			boolean showConsoleLog = Arrays.stream(environmentInfo.getFrameworkArgs())
					.anyMatch(arg -> "-consoleLog".equals(arg));
			
			for (String arg : this.environmentInfo.getNonFrameworkArgs()) {
				System.out.println(inverter.invert(arg));
			}
			
			// If the -consoleLog parameter is used, a separate shell is opened. 
			// To avoid that it is closed immediately a simple input is requested to
			// close, so a user can inspect the outputs.
			if (showConsoleLog) {
				System.out.println();
				System.out.println("***** Press Enter to exit *****");
				// just wait for a Enter
				try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
					reader.readLine();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
			if (!isInteractive) {
				// shutdown the application if no console was opened
				// only needed if osgi.noShutdown=true is configured
				System.exit(0);
			}
		}
	}
}
