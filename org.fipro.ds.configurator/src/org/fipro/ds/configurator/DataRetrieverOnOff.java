package org.fipro.ds.configurator;

import java.util.List;

import org.fipro.ds.data.DataService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.propertytypes.SatisfyingConditionTarget;

@Component(
    property= {
        "osgi.command.scope:String=fipro",
        "osgi.command.function:String=onoff"},
    service=DataRetrieverOnOff.class
)
@SatisfyingConditionTarget("(osgi.condition.id=onoff)")
public class DataRetrieverOnOff {
	
	@Reference
    private volatile List<DataService> dataServices;

    public void onoff(int id) {
        for (DataService service : this.dataServices) {
            System.out.println(service.getData(id));
        }
    }
}