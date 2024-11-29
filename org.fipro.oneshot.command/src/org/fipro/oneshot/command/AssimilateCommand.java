package org.fipro.oneshot.command;

import java.io.IOException;
import java.util.Dictionary;
import java.util.Hashtable;

import org.apache.felix.service.command.CommandProcessor;
import org.apache.felix.service.command.Descriptor;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(
    property= {
		CommandProcessor.COMMAND_SCOPE + "=fipro",
		CommandProcessor.COMMAND_FUNCTION + "=assimilate"},
    service=AssimilateCommand.class
)
public class AssimilateCommand {

	@Reference
    ConfigurationAdmin configAdmin;

    @Descriptor("assimilates the given soldier to the Borg")
    public void assimilate(
    		@Descriptor("the name of the soldier to assimilate") String soldier) {
        assimilate(soldier, null);
    }

    @Descriptor("assimilates the given soldier to the Borg with the given name")
    public void assimilate(
    		@Descriptor("the name of the soldier to assimilate") String soldier,
    		@Descriptor("the new name for the assimilated Borg") String newName) {
        try {
            // filter to find the Borg created by the
            // Managed Service Factory with the given name
            String filter = "(&(name=" + soldier + ")" + "(service.factoryPid=Borg))";
            Configuration[] configurations = this.configAdmin.listConfigurations(filter);

            if (configurations == null || configurations.length == 0) {
                //create a new configuration
                Configuration config = this.configAdmin.createFactoryConfiguration("Borg", "?");
                Hashtable<String, Object> map = new Hashtable<>();
                if (newName == null) {
                    map.put("name", soldier);
                    System.out.println("Assimilated " + soldier);
                } else {
                    map.put("name", newName);
                    System.out.println("Assimilated " + soldier + " and named it " + newName);
                }
                config.update(map);
            } else if (newName != null) {
                // update the existing configuration
                Configuration config = configurations[0];
                // it is guaranteed by listConfigurations() that only
                // Configuration objects are returned with non-null properties
                Dictionary<String, Object> map = config.getProperties();
                map.put("name", newName);
                config.update(map);
                System.out.println(soldier + " already assimilated and renamed to " + newName);
            }
        } catch (IOException | InvalidSyntaxException e1) {
            e1.printStackTrace();
        }
    }
}