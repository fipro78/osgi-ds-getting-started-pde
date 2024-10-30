package org.fipro.ds.configurable.command;

import java.io.IOException;
import java.util.Hashtable;

import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(
    property = {
        "osgi.command.scope=fipro",
        "osgi.command.function=configure"
    },
    service=ConfigureCommand.class
)
public class ConfigureCommand {

	@Reference
    ConfigurationAdmin cm;

    public void configure(String msg, int count) throws IOException {
        Configuration config = cm.getConfiguration("AdminConfiguredComponent");
        Hashtable<String, Object> props = new Hashtable<>();
        props.put("message", msg);
        props.put("iteration", count);
        config.update(props);
    }
}