package org.fipro.inverter.http.eclipse;

import java.net.MalformedURLException;
import java.net.URL;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;
import org.osgi.service.http.context.ServletContextHelper;

@Component(
    service = ServletContextHelper.class,
    scope = ServiceScope.BUNDLE,
    property = {
        "osgi.http.whiteboard.context.name=eclipse",
        "osgi.http.whiteboard.context.path=/eclipse" })
public class EclipseServletContextHelper extends ServletContextHelper {

    public URL getResource(String name) {
        // remove the path from the name
        name = name.replace("/eclipse", "");
        try {
            return new URL("http://www.eclipse.org/" + name);
        } catch (MalformedURLException e) {
            return null;
        }
    }
}