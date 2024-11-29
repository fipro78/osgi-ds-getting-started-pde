package org.fipro.oneshot.provider;

import java.util.concurrent.atomic.AtomicInteger;

import org.fipro.oneshot.OneShot;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.ServiceScope;

@Component(scope = ServiceScope.PROTOTYPE)
public class Hitman implements OneShot {

    private static AtomicInteger instanceCounter = new AtomicInteger(); 

    private final int instanceNo;

    public Hitman() {
        instanceNo = instanceCounter.incrementAndGet();
    }

    @Activate
    void activate() {
    	System.out.println(
    			getClass().getSimpleName() + " activated");
    }
    
    @Deactivate
    void deactivate() {
    	System.out.println(
    			getClass().getSimpleName() + " deactivated");
    }

    @Override
    public void shoot(String target) {
        System.out.println("BAM! I am hitman #"
            + instanceNo + ". And I killed " + target);
    }

}