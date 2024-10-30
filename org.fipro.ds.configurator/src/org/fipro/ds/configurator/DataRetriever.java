package org.fipro.ds.configurator;

import java.util.List;
import java.util.Map;

import org.fipro.ds.data.DataService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(
    property= {
        "osgi.command.scope:String=fipro",
        "osgi.command.function:String=retrieve"},
    service=DataRetriever.class
)
public class DataRetriever {

	@Reference(
		bind = "addDataService",
		target="(fipro.connectivity=online)")
    private volatile List<DataService> dataServices;

    void addDataService(Map<String, Object> properties) {
        System.out.println("Added " + properties.get("component.name"));
        properties.forEach((k, v) -> {
            System.out.println(k+"="+v);
        });
        System.out.println();
    }

    void removeDataService(Map<String, Object> properties) {
        System.out.println("Removed " + properties.get("component.name"));
    }

    public void retrieve(int id) {
        for (DataService service : this.dataServices) {
            System.out.println(service.getData(id));
        }
    }
}