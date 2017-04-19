package org.fipro.inverter.http;

import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;

import org.osgi.service.component.annotations.Component;

@Component(property = "osgi.http.whiteboard.listener=true")
public class SimpleServletRequestListener
    implements ServletRequestListener {

    public void requestInitialized(ServletRequestEvent sre) {
        System.out.println("Request initialized for client: "
            + sre.getServletRequest().getRemoteAddr());
    }

    public void requestDestroyed(ServletRequestEvent sre) {
        System.out.println("Request destroyed for client: "
            + sre.getServletRequest().getRemoteAddr());
    }

}