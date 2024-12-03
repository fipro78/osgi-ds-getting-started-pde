package org.fipro.inverter.http;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.servlet.whiteboard.propertytypes.HttpWhiteboardResource;

@Component(service = ResourceService.class)
@HttpWhiteboardResource(pattern = "/files/*", prefix = "/resources")
public class ResourceService { }