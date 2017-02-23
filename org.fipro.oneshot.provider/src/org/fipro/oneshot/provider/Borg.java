package org.fipro.oneshot.provider;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.fipro.oneshot.OneShot;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Modified;

@Component(
    configurationPid="org.fipro.oneshot.Borg",
    configurationPolicy=ConfigurationPolicy.REQUIRE)
public class Borg implements OneShot {

    private static AtomicInteger instanceCounter = new AtomicInteger();

    private final int instanceNo;
    private String name;

    public Borg() {
        instanceNo = instanceCounter.incrementAndGet();
    }

    @Activate
    void activate(Map<String, Object> properties) {
        this.name = (String) properties.get("name");
    }

    @Modified
    void modified(Map<String, Object> properties) {
    	this.name = (String) properties.get("name");
    }

    @Override
    public void shoot(String target) {
        System.out.println("Borg " + name
            + " #" + instanceNo + " of "+ instanceCounter.get()
            + " took orders and executed " + target);
    }

}