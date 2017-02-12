package org.fipro.oneshot.provider;

import java.util.concurrent.atomic.AtomicInteger;

import org.fipro.oneshot.OneShot;
import org.osgi.service.component.annotations.Component;

@Component(factory="fipro.oneshot.factory")
public class Shooter implements OneShot {

    private static AtomicInteger instanceCounter = new AtomicInteger(); 

    private final int instanceNo;

    public Shooter() {
        instanceNo = instanceCounter.incrementAndGet();
    }

    @Override
    public void shoot(String target) {
        System.out.println("PEW PEW! I am shooter #"
            + instanceNo + ". And I hit " + target);
    }

}