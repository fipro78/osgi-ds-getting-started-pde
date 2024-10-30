package org.fipro.ds.data.online;

import org.fipro.ds.data.DataService;
import org.fipro.ds.data.FiproConnectivity;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.propertytypes.ServiceRanking;

@Component
@FiproConnectivity("online")
@ServiceRanking(7)
public class OnlineDataService implements DataService {

    @Override
    public String getData(int id) {
        return "ONLINE data for id " + id;
    }
}