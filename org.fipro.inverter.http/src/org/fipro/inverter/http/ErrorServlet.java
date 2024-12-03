package org.fipro.inverter.http;

import java.io.IOException;

import jakarta.servlet.Servlet;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;
import org.osgi.service.servlet.whiteboard.propertytypes.HttpWhiteboardServletErrorPage;

@Component(
    service=Servlet.class,
    scope=ServiceScope.PROTOTYPE)
@HttpWhiteboardServletErrorPage(errorPage = { "java.lang.IllegalArgumentException" , "500"} )
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