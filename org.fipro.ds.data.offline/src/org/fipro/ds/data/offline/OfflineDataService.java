package org.fipro.ds.data.offline;

import java.util.Map;

import org.fipro.ds.data.DataService;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

@Component(
    property= {
        "fipro.connectivity=offline",
        "service.ranking:Integer=5",
        ".private=private configuration"
    }
)
public class OfflineDataService implements DataService {

	@Activate
    void activate(Map<String, Object> properties) {
        System.out.println("OfflineDataService activated");
        properties.forEach((k, v) -> {
            System.out.println(k+"="+v);
        });
        System.out.println();
    }
	
    @Override
    public String getData(int id) {
        return "OFFLINE data for id " + id;
    }
}