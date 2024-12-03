package org.fipro.inverter.http;

import java.io.IOException;

import jakarta.servlet.Servlet;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.fipro.inverter.StringInverter;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;
import org.osgi.service.servlet.whiteboard.propertytypes.HttpWhiteboardServletPattern;

@Component(
    service=Servlet.class,
    scope=ServiceScope.PROTOTYPE)
@HttpWhiteboardServletPattern("/invert")
public class InverterServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Reference
    private StringInverter inverter;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String input = req.getParameter("value");
        if (input == null) {
            throw new IllegalArgumentException("input can not be null");
        }
        
        String output = inverter.invert(input);

        resp.setContentType("text/html");
        resp.getWriter().write(
            "<html><body>Result is " + output + "</body></html>");
    }

}