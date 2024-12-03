package org.fipro.inverter.http.eclipse;

import java.io.IOException;

import jakarta.servlet.Servlet;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;
import org.osgi.service.servlet.whiteboard.propertytypes.HttpWhiteboardContextSelect;
import org.osgi.service.servlet.whiteboard.propertytypes.HttpWhiteboardServletPattern;

@Component(
    service=Servlet.class,
    scope=ServiceScope.PROTOTYPE)
@HttpWhiteboardServletPattern("/image")
@HttpWhiteboardContextSelect("(osgi.http.whiteboard.context.name=eclipse)")
public class ImageServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("text/html");
        resp.getWriter().write("Show an image from https://eclipse.dev");
        resp.getWriter().write(
            "<p><img src='img/nattable/FeatureScreenShot.png'/></p>");
    }

}