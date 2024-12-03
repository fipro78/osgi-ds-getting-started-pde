package org.fipro.inverter.http.eclipse;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.servlet.whiteboard.propertytypes.HttpWhiteboardContextSelect;
import org.osgi.service.servlet.whiteboard.propertytypes.HttpWhiteboardResource;

@Component(service = EclipseImageResourceService.class)
@HttpWhiteboardResource(pattern = "/img/*", prefix = "/eclipse")
@HttpWhiteboardContextSelect("(osgi.http.whiteboard.context.name=eclipse)")
public class EclipseImageResourceService { }