package org.fipro.mafia.soldier;

import org.fipro.mafia.common.MafiaBossConstants;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;

@Component(
	property = {
		EventConstants.EVENT_TOPIC + "=" + MafiaBossConstants.TOPIC_ALL,
		EventConstants.EVENT_FILTER + "=" + "(!(target=Sonny))"})
public class Ray implements EventHandler {

    @Override
    public void handleEvent(Event event) {
        String topic = event.getTopic();
        Object target = event.getProperty(MafiaBossConstants.PROPERTY_KEY_TARGET);

        switch (topic) {
            case MafiaBossConstants.TOPIC_CONVINCE:
                System.out.println("Ray: I helped in punching the shit out of" + target);
                break;
            case MafiaBossConstants.TOPIC_ENCASH:
                System.out.println("Ray: I helped getting the money from " + target);
                break;
            case MafiaBossConstants.TOPIC_SOLVE:
                System.out.println("Ray: I helped killing " + target);
                break;
            default: System.out.println("Ray: I helped with whatever was requested!");
        }
    }

}