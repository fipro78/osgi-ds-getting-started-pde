package org.fipro.ds.data.offline;

import java.util.Map;

import org.fipro.ds.data.DataService;
import org.fipro.ds.data.FiproConnectivity;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.propertytypes.ServiceRanking;

@Component(
	property = {
		".private=private configuration"
	}
)
@FiproConnectivity("offline")
@ServiceRanking(5)
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