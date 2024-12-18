package org.fipro.ds.configurator;

import java.io.IOException;
import java.util.Dictionary;
import java.util.Hashtable;

import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(
    property= {
        "osgi.command.scope:String=fipro",
        "osgi.command.function:String=toggle"
    },
    service=ToggleConnectivityCommand.class
)
public class ToggleConnectivityCommand {

	@Reference
    ConfigurationAdmin admin;

    public void toggle() throws IOException {
        Configuration config =
            this.admin.getConfiguration("org.fipro.ds.configurator.DataRetriever");

        Dictionary<String, Object> props = null;
        Object target = null;
        if (config != null && config.getProperties() != null) {
        	props = config.getProperties();
        	target = props.get("dataServices.target");
        } else {
            props = new Hashtable<>();
        }

        boolean isOnline = (target == null || target.toString().contains("online"));

        // toggle the state
        StringBuilder filter = new StringBuilder("(fipro.connectivity=");
        filter.append(isOnline ? "offline" : "online").append(")");

        props.put("dataServices.target", filter.toString());
        config.update(props);
    }
}