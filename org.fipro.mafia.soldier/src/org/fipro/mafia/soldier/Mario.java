package org.fipro.mafia.soldier;

import org.fipro.mafia.common.MafiaBossConstants;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;
import org.osgi.service.event.propertytypes.EventTopics;

@Component
@EventTopics(MafiaBossConstants.TOPIC_ENCASH)
public class Mario implements EventHandler {

    @Override
    public void handleEvent(Event event) {
        System.out.println("Mario: "
        + event.getProperty(MafiaBossConstants.PROPERTY_KEY_TARGET)
        + " payed for protection");
    }

}