package org.fipro.oneshot.command;

import java.io.IOException;
import java.util.Dictionary;
import java.util.Hashtable;

import org.apache.felix.service.command.Descriptor;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(
    property= {
        "osgi.command.scope:String=fipro",
        "osgi.command.function:String=change"
    },
    service=ChangeShooterCommand.class
)
public class ChangeShooterCommand {

    @Reference
    ConfigurationAdmin admin;

    @Descriptor("change the organization that will send the shooter")
    public void change(
            @Descriptor("the name of the organization, can be 'army' or 'marine'") String org) throws IOException {
        
        Configuration config =
            this.admin.getConfiguration("org.fipro.oneshot.command.ShootCommand");

        Dictionary<String, Object> props = null;
        if (config != null && config.getProperties() != null) {
        	props = config.getProperties();
        } else {
            props = new Hashtable<>();
        }

        // change the organization
        StringBuilder filter = 
            new StringBuilder("(&(component.factory=fipro.oneshot.factory)(organization=" + org + "))");

        props.put("factory.target", filter.toString());
        config.update(props);
    }
}