package org.fipro.ds.configurator;

import java.io.IOException; 
import java.util.Dictionary; 
import java.util.Hashtable;

import org.osgi.service.cm.Configuration; 
import org.osgi.service.cm.ConfigurationAdmin; 
import org.osgi.service.component.annotations.Component; 
import org.osgi.service.component.annotations.Reference;

@Component(
    property = {
        "osgi.command.scope=fipro",
        "osgi.command.function=cardinality"
    },
    service=ToggleMinimumCardinalityCommand.class
)
public class ToggleMinimumCardinalityCommand {

	@Reference 
	ConfigurationAdmin admin;

	public void cardinality(int count) throws IOException { 
		Configuration config =
                    this.admin.getConfiguration("org.fipro.ds.configurator.DataRetriever");

		Dictionary<String, Object> props = null; 
		if (config != null && config.getProperties() != null) { 
		    props = config.getProperties(); 
		} 
		else { 
		    props = new Hashtable<>(); 
		}

		props.put("dataServices.cardinality.minimum", count); 
		config.update(props); 
	} 
}