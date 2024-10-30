package org.fipro.ds.configurable;

import org.osgi.service.component.annotations.ComponentPropertyType;

@ComponentPropertyType
public @interface MessageConfig {
    String message() default ""; 
    int iteration() default 0;
}
