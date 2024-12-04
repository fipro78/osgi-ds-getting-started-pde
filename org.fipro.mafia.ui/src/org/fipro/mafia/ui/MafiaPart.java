package org.fipro.mafia.ui;

import java.util.Dictionary;
import java.util.Hashtable;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.inject.Inject;

import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.fipro.mafia.common.MafiaBossConstants;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;

public class MafiaPart {

	private Label handlerLabel;
	private Label e4HandlerLabel;
	private ServiceRegistration<?> eventHandler;

	@PostConstruct
	public void postConstruct(Composite parent) {
		parent.setLayout(new GridLayout(2, false));
		
		Label l1 = new Label(parent, SWT.NONE);
		l1.setText("Received via handler:");
		GridDataFactory.defaultsFor(l1).applyTo(l1);
		
		handlerLabel = new Label(parent, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(handlerLabel);
		
		Label l2 = new Label(parent, SWT.NONE);
		l2.setText("Received via E4 handler:");
		GridDataFactory.defaultsFor(l2).applyTo(l2);
		
		e4HandlerLabel = new Label(parent, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(e4HandlerLabel);
		
		
		// retrieve the bundle of the calling class
		Bundle bundle = FrameworkUtil.getBundle(getClass());
		BundleContext bc = (bundle != null) ? bundle.getBundleContext() : null;
		if (bc != null) {
		    // create the service properties instance
		    Dictionary<String, Object> properties = new Hashtable<>();
		    properties.put(EventConstants.EVENT_TOPIC, MafiaBossConstants.TOPIC_ALL);
		    // register the EventHandler service
		    eventHandler = bc.registerService(
		        EventHandler.class.getName(),
		        new EventHandler() {

		            @Override
		            public void handleEvent(Event event) {
		            	// ensure to update the UI in the UI thread
		            	Display.getDefault().asyncExec(() -> handlerLabel.setText(
		            			"Received boss command " 
		            					+ event.getTopic()
		            					+ " for target "
		            					+ event.getProperty(MafiaBossConstants.PROPERTY_KEY_TARGET)));
		            }
		        },
		        properties);
		}
	}

	@PreDestroy
	void preDestroy() {
		if (eventHandler != null) {
			eventHandler.unregister();
		}
	}
	
	@Inject
	@Optional
	void handleConvinceEvent(
			@UIEventTopic(MafiaBossConstants.TOPIC_CONVINCE) String target) {
		e4HandlerLabel.setText("Received boss CONVINCE command for " + target); 
	}
	
	@Inject
	@Optional
	void handleEncashEvent(
			@UIEventTopic(MafiaBossConstants.TOPIC_ENCASH) String target) {
		e4HandlerLabel.setText("Received boss ENCASH command for " + target); 
	}
	
	@Inject
	@Optional
	void handleSolveEvent(
			@UIEventTopic(MafiaBossConstants.TOPIC_SOLVE) String target) {
		e4HandlerLabel.setText("Received boss SOLVE command for " + target); 
	}
	
}