package org.fipro.mafia.soldier;

import org.fipro.mafia.common.MafiaBossConstants;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;

@Component(
    property = {
        EventConstants.EVENT_TOPIC + "=" + MafiaBossConstants.TOPIC_CONVINCE,
        EventConstants.EVENT_TOPIC + "=" + MafiaBossConstants.TOPIC_SOLVE })
public class Pete implements EventHandler {

    @Override
    public void handleEvent(Event event) {
        System.out.println("Pete: I took care of "
        + event.getProperty(MafiaBossConstants.PROPERTY_KEY_TARGET));
    }

}