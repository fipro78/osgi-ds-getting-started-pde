package org.fipro.inverter.http.eclipse;

import org.osgi.service.component.annotations.Component;

@Component(
    service = EclipseImageResourceService.class,
    property = {
        "osgi.http.whiteboard.resource.pattern=/img/*",
        "osgi.http.whiteboard.resource.prefix=/eclipse",
        "osgi.http.whiteboard.context.select=(osgi.http.whiteboard.context.name=eclipse)"})
public class EclipseImageResourceService { }