package org.fipro.inverter.http.eclipse;

import java.net.MalformedURLException;
import java.net.URL;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;
import org.osgi.service.servlet.context.ServletContextHelper;
import org.osgi.service.servlet.whiteboard.propertytypes.HttpWhiteboardContext;

@Component(
    service = ServletContextHelper.class,
    scope = ServiceScope.BUNDLE)
@HttpWhiteboardContext(name = "eclipse", path = "/eclipse")
public class EclipseServletContextHelper extends ServletContextHelper {

    public URL getResource(String name) {
        // remove the path from the name
        name = name.replace("/eclipse", "");
        try {
            return new URL("https://eclipse.dev/" + name);
        } catch (MalformedURLException e) {
            return null;
        }
    }
}