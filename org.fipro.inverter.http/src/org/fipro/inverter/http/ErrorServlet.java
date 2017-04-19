package org.fipro.inverter.http;

import java.io.IOException;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;

@Component(
    service=Servlet.class,
    property= {
        "osgi.http.whiteboard.servlet.errorPage=java.lang.IllegalArgumentException",
        "osgi.http.whiteboard.servlet.errorPage=500"
    },
    scope=ServiceScope.PROTOTYPE)
public class ErrorServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("text/html");
        resp.getWriter().write(
        "<html><body>You need to provide an input!</body></html>");
    }
}