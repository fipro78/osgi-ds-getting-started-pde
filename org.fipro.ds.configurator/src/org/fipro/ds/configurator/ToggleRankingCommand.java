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
        "osgi.command.function:String=ranking"
    },
    service=ToggleRankingCommand.class
)
public class ToggleRankingCommand {

    ConfigurationAdmin admin;

    @Reference
    void setConfigurationAdmin(ConfigurationAdmin admin) {
        this.admin = admin;
    }

    public void ranking() throws IOException {
        Configuration configOnline = 
        		this.admin.getConfiguration( "org.fipro.ds.data.online.OnlineDataService", null);
        Dictionary<String, Object> propsOnline = null;
        if (configOnline != null && configOnline.getProperties() != null) {
            propsOnline = configOnline.getProperties();
        } else {
            propsOnline = new Hashtable<>();
        }

        int onlineRanking = 7;
        if (configOnline != null && configOnline.getProperties() != null) {
            Object rank = configOnline.getProperties().get("service.ranking");
            if (rank != null) {
                onlineRanking = (Integer)rank;
            }
        }

        // toggle between 3 and 7
        onlineRanking = (onlineRanking == 7) ? 3 : 7;

        propsOnline.put("service.ranking", onlineRanking);
        configOnline.update(propsOnline);
    }
}