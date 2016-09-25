package org.fipro.ds.other;

import java.util.Map;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;

@Component(
    configurationPid = "AdminConfiguredComponent",
    configurationPolicy = ConfigurationPolicy.REQUIRE
)
public class OtherConfiguredComponent {

    @Activate
    void activate(Map<String, Object> properties) {
        System.out.println();
        System.out.println("OtherConfiguredComponent activated");
        printMessage(properties);
    }

    @Modified
    void modified(Map<String, Object> properties) {
        System.out.println();
        System.out.println("OtherConfiguredComponent modified");
        printMessage(properties);
    }

    @Deactivate
        void deactivate() {
        System.out.println("OtherConfiguredComponent deactivated");
        System.out.println();
    }

    private void printMessage(Map<String, Object> properties) {
        String msg = (String) properties.get("message");
        Integer iter = (Integer) properties.get("iteration");

        if (msg != null && iter != null) {
            for (int i = 1; i <= iter; i++) {
                System.out.println(i + ": " + msg);
            }
        }
    }
}