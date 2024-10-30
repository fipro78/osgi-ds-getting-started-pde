package org.fipro.ds.configurator;

import org.fipro.ds.data.DataService;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferencePolicyOption;

@Component(
    property= {
        "osgi.command.scope:String=fipro",
        "osgi.command.function:String=get"
    },
    service=DataGetter.class
)
public class DataGetter {

	@Reference(policyOption=ReferencePolicyOption.GREEDY)
    private volatile DataService dataService;

	@Activate
	void activate() {
		System.out.println("DataGetter activated");
	}
	
    public void get(int id) {
        System.out.println(this.dataService.getData(id));
    }
}