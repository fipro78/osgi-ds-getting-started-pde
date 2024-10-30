package org.fipro.ds.configurable;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;

@Component(
    configurationPid = "AdminConfiguredComponent",
    configurationPolicy = ConfigurationPolicy.REQUIRE,
    service=AdminConfiguredComponent.class
)
public class AdminConfiguredComponent {

    @Activate
    void activate(MessageConfig config) {
        System.out.println();
        System.out.println("AdminConfiguredComponent activated");
        printMessage(config);
    }

    @Modified
    void modified(MessageConfig config) {
        System.out.println();
        System.out.println("AdminConfiguredComponent modified");
        printMessage(config);
    }

    @Deactivate
        void deactivate() {
        System.out.println("AdminConfiguredComponent deactivated");
        System.out.println();
    }

    private void printMessage(MessageConfig config) {
        String msg = config.message();
        int iter = config.iteration();

        for (int i = 1; i <= iter; i++) {
            System.out.println(i + ": " + msg);
        }
        System.out.println();
    }
}