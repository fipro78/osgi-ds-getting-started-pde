package org.fipro.ds.configurable;

import java.util.Map;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;

@Component
public class AdminReferencingComponent {

	@Reference(bind="setAdminConfiguredComponent")
    AdminConfiguredComponent component;

    @Activate
    void activate() {
        System.out.println("AdminReferencingComponent activated");
    }

    @Modified
    void modified() {
        System.out.println("AdminReferencingComponent modified");
    }

    @Deactivate
    void deactivate() {
        System.out.println("AdminReferencingComponent deactivated");
    }

    void setAdminConfiguredComponent(Map<String, Object> properties) {
        System.out.println("AdminReferencingComponent: set service");
        printMessage(properties);
    }

    void updatedAdminConfiguredComponent(Map<String, Object> properties) {
        System.out.println("AdminReferencingComponent: update service");
        printMessage(properties);
    }

    void unsetAdminConfiguredComponent(Map<String, Object> properties) {
        System.out.println("AdminReferencingComponent: unset service");
    }

    private void printMessage(Map<String, Object> properties) {
        String msg = properties.getOrDefault("message", "").toString();
        int iter = ((Number)properties.getOrDefault("iteration", 0)).intValue();
        System.out.println("[" + msg + "|" + iter + "]");
        System.out.println();
    }
}