package org.fipro.inverter.http;

import java.io.IOException;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;
import org.osgi.service.servlet.whiteboard.propertytypes.HttpWhiteboardFilterPattern;

@Component(scope=ServiceScope.PROTOTYPE)
@HttpWhiteboardFilterPattern("/invert")
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