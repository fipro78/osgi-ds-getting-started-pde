package org.fipro.ds.configurable;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

@Component(
	configurationPid = {"$", "AdminConfiguredComponent"},
	properties="OSGI-INF/config.properties"
)
public class FileConfiguredComponent {

    @Activate
    void activate(MessageConfig config) {
        String msg = config.message();
        int iter = config.iteration();

        for (int i = 1; i <= iter; i++) {
            System.out.println("file - " + i + ": " + msg);
        }
        System.out.println();
    }
}