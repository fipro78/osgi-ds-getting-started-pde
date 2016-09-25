package org.fipro.ds.configurator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.fipro.ds.data.DataService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

@Component(
    property= {
        "osgi.command.scope:String=fipro",
        "osgi.command.function:String=retrieve"},
    service=DataRetriever.class
)
public class DataRetriever {

    private List<DataService> dataServices = new ArrayList<>();

    @Reference(
        cardinality=ReferenceCardinality.MULTIPLE,
        policy=ReferencePolicy.DYNAMIC,
        target="(fipro.connectivity=online)"
    )
    void addDataService(DataService service, Map<String, Object> properties) {
        this.dataServices.add(service);
        
        System.out.println("Added " + service.getClass().getName());
        properties.forEach((k, v) -> {
            System.out.println(k+"="+v);
        });
        System.out.println();
    }

    void removeDataService(DataService service) {
        this.dataServices.remove(service);
        System.out.println("Removed " + service.getClass().getName());
    }

    public void retrieve(int id) {
        for (DataService service : this.dataServices) {
            System.out.println(service.getData(id));
        }
    }
}