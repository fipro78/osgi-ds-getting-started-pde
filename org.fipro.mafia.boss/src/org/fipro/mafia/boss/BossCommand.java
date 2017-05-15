package org.fipro.mafia.boss;

import java.util.HashMap;
import java.util.Map;

import org.apache.felix.service.command.Descriptor;
import org.fipro.mafia.common.MafiaBossConstants;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;

@Component(
    property = {
        "osgi.command.scope=fipro",
        "osgi.command.function=boss" },
    service = BossCommand.class)
public class BossCommand {

    @Reference
    EventAdmin eventAdmin;

    @Descriptor("As a mafia boss you want something to be done")
    public void boss(
        @Descriptor("the command that should be executed. "
            + "possible values are: convince, encash, solve")
        String command,
        @Descriptor("who should be 'convinced', "
            + "'asked for protection money' or 'finally solved'")
        String target) {

        // create the event properties object
        Map<String, Object> properties = new HashMap<>();
        properties.put(MafiaBossConstants.PROPERTY_KEY_TARGET, target);
        properties.put("org.eclipse.e4.data", target);
        Event event = null;

        switch (command) {
            case "convince":
                event = new Event(MafiaBossConstants.TOPIC_CONVINCE, properties);
                break;
            case "encash":
                event = new Event(MafiaBossConstants.TOPIC_ENCASH, properties);
                break;
            case "solve":
                event = new Event(MafiaBossConstants.TOPIC_SOLVE, properties);
                break;
            default:
                System.out.println("Such a command is not known!");
        }

        if (event != null) {
            eventAdmin.postEvent(event);
        }
    }
}