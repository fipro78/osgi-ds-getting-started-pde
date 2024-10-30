package org.fipro.ds.configurable;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

@Component(configurationPid = {"$", "AdminConfiguredComponent"})
@MessageConfig(message = "Welcome to the inline configured service", iteration = 3)
public class StaticConfiguredComponent {

    @Activate
    void activate(MessageConfig config) {
        String msg = config.message();
        int iter = config.iteration();

        for (int i = 1; i <= iter; i++) {
            System.out.println("static - " + i + ": " + msg);
        }
        System.out.println();
    }
}