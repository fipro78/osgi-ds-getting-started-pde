package org.fipro.ds.data.online;

import org.fipro.ds.data.DataService;
import org.osgi.service.component.annotations.Component;

@Component(
	property = {
			"fipro.connectivity=online",
			"service.ranking:Integer=7"
	}
)
public class OnlineDataService implements DataService {

    @Override
    public String getData(int id) {
        return "ONLINE data for id " + id;
    }
}