package org.fipro.oneshot.provider;

import java.util.Map;

import org.fipro.oneshot.OneShot;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

@Component(
    factory="fipro.oneshot.factory",
    factoryProperty = "organization=marine",
    property = "shooter.name=Bob Lee Swagger") 
public class Sniper implements OneShot {
    
    @Activate
    private Map<String, Object> properties;
    
    @Override 
    public void shoot(String target) { 
        System.out.println("I hit " + target + " and you will never see me!");
        System.out.println("My name is " + properties.get("shooter.name"));
    }
}