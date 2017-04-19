package org.fipro.inverter.http;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;

@Component(
    property = "osgi.http.whiteboard.filter.pattern=/invert",
    scope=ServiceScope.PROTOTYPE)
public class SimpleServletFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig)
            throws ServletException { }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        response.setContentType("text/html");
        response.getWriter().write("<b>Inverter Servlet</b><p>");
        chain.doFilter(request, response);
        response.getWriter().write("</p><i>Powered by fipro</i>");
    }

    @Override
    public void destroy() { }

}