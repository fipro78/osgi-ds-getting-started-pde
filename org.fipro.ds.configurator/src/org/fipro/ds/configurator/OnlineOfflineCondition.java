package org.fipro.ds.configurator;

import org.fipro.ds.data.DataService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.condition.Condition;

@Component(property="osgi.condition.id=onoff")
public class OnlineOfflineCondition implements Condition {

	@Reference(target="(fipro.connectivity=online)")
    private DataService onlineDataService;
	
	@Reference(target="(fipro.connectivity=offline)")
	private DataService offlineDataService;

}
