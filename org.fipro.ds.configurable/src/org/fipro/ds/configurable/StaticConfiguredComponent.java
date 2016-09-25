package org.fipro.ds.configurable;

import java.util.Map;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

@Component(
    property = {
        "message=Welcome to the inline configured service",
        "iteration:Integer=3"
    }
)
public class StaticConfiguredComponent {

    @Activate
    void activate(Map<String, Object> properties) {
        String msg = (String) properties.get("message");
        Integer iter = (Integer) properties.get("iteration");

        for (int i = 1; i <= iter; i++) {
            System.out.println(i + ": " + msg);
        }
        System.out.println();
    }
}