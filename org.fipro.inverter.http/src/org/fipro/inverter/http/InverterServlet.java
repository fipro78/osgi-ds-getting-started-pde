package org.fipro.inverter.http;

import java.io.IOException;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.fipro.inverter.StringInverter;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;

@Component(
    service=Servlet.class,
    property= "osgi.http.whiteboard.servlet.pattern=/invert",
    scope=ServiceScope.PROTOTYPE)
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