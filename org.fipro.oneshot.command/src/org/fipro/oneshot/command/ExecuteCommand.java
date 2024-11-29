package org.fipro.oneshot.command;

import java.util.List;
import java.util.ListIterator;

import org.fipro.oneshot.OneShot;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(
    property= {
        "osgi.command.scope=fipro",
        "osgi.command.function=execute"
    },
    service=ExecuteCommand.class ) 
public class ExecuteCommand {

    @Reference(target="(service.factoryPid=Borg)")
    private volatile List<OneShot> borgs;

    public void execute(String target) { 
        for (ListIterator<OneShot> it = borgs.listIterator(borgs.size()); it.hasPrevious(); ) { 
            it.previous().shoot(target); 
        } 
    } 
}