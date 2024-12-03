package org.fipro.inverter.http;

import jakarta.servlet.ServletRequestEvent;
import jakarta.servlet.ServletRequestListener;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.servlet.whiteboard.propertytypes.HttpWhiteboardListener;

@Component
@HttpWhiteboardListener
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