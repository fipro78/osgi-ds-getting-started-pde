package org.fipro.inverter.http;

import org.osgi.service.component.annotations.Component;

@Component(
    service = ResourceService.class,
    property = {
        "osgi.http.whiteboard.resource.pattern=/files/*",
        "osgi.http.whiteboard.resource.prefix=/resources"})
public class ResourceService { }