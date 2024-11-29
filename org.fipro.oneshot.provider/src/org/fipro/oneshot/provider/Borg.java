package org.fipro.oneshot.provider;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.fipro.oneshot.OneShot;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Modified;

@Component(
    configurationPid="Borg",
    configurationPolicy=ConfigurationPolicy.REQUIRE) 
public class Borg implements OneShot {

    @interface BorgConfig { 
        String name() default ""; 
    }

    @Activate
    BorgConfig config;
    
    private static AtomicInteger instanceCounter = new AtomicInteger();
    private final int instanceNo;

    public Borg() { 
        instanceNo = instanceCounter.incrementAndGet(); 
    }
    
    @Activate
    void activate(Map<String, Object> properties) {
        properties.forEach((k, v) -> {
            System.out.println(k+"="+v);
        });
        System.out.println();
    }
    
    @Modified
    void modified(BorgConfig config) { 
        this.config = config; 
    }

    @Override
    public void shoot(String target) { 
        System.out.println("Borg " + config.name() + " #" + instanceNo + " of "+ instanceCounter.get()
            + " took orders and executed " + target); 
    }
}