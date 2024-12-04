package org.fipro.mafia.soldier;

import org.fipro.mafia.common.MafiaBossConstants;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.osgi.service.event.propertytypes.EventDelivery;
import org.osgi.service.event.propertytypes.EventFilter;
import org.osgi.service.event.propertytypes.EventTopics;

@Component
@EventTopics(MafiaBossConstants.TOPIC_ALL)
@EventFilter("(!(target=Sonny))")
@EventDelivery(EventConstants.DELIVERY_ASYNC_UNORDERED)
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