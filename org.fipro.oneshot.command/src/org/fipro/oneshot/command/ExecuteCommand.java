package org.fipro.oneshot.command;

import java.util.ArrayList;
import java.util.List;

import org.fipro.oneshot.OneShot;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

@Component(
    property= {
        "osgi.command.scope=fipro",
        "osgi.command.function=execute"},
    service=ExecuteCommand.class
)
public class ExecuteCommand {

    private List<OneShot> borgs = new ArrayList<>();

    @Reference(
    		cardinality=ReferenceCardinality.MULTIPLE,
    		policy=ReferencePolicy.DYNAMIC,
    		target="(service.factoryPid=org.fipro.oneshot.Borg)")
    void addBorg(OneShot borg) {
    	this.borgs.add(borg);
    }
    
    void removeBorg(OneShot borg) {
    	this.borgs.remove(borg);
    }
    
    public void execute(String target) {
    	this.borgs.forEach(s -> s.shoot(target));
    }
}