# Configuring OSGi Declarative Services

In my blog post about [Getting Started with OSGi Declarative Services](getting-started-with-osgi-declarative-services.md) I provided an introduction to OSGi declarative services. How to create them, how they behave at runtime, how to reference other services, and so on. But I left out an important topic there: **configuring OSGi components**. Well to be precise I mentioned it, and one sort of configuration was also used in the examples, but it was not explained in detail. As there are multiple aspects with regards to component configuration I wanted to write a blog post that is dedicated to that topic, and here it is.

After reading this blog post you should have a deeper understanding of how OSGi components can be configured.

## Basics

A component can be configured via _Component Properties_. Properties are key-value-pairs that can be accessed via `Map<String, Object>`. With DS 1.3 the _Component Property Types_ are introduced for type safe access to _Component Properties_.

_Component Properties_ can be defined in different ways:

- inline
- via Java properties file
- via [OSGi Configuration Admin](https://docs.osgi.org/specification/osgi.cmpn/8.1.0/service.cm.html)
- via argument of the `ComponentFactory.newInstance` method (only for factory components, and as I didn't cover them in the previous blog post, I won't cover that topic here aswell)

_Component Properties_ that are defined inline or via properties file can be overridden by using the OSGi Configuration Admin or the `ComponentFactory.newInstance` argument. Basically the property propagation is executed sequentially. Therefore it is even possible to override inline properties with properties from a properties file, if the properties file is specified after the inline properties.

The SCR (_Service Component Runtime_) always adds the following _Component Properties_ that can't be overridden:

- _component.name_ - The component name.
- _component.id_ - A unique value (`Long`) that is larger than all previously assigned values. These values are not persistent across restarts.

In a life cycle method (_activate/modified/deactivate_) you can get the _Component Properties_ via method parameter. The properties that are retrieved in event methods for referencing other services (_bind/updated/unbind_) are called _Service Properties_. The SCR performs a property propagation in that case, which means that all non-private _Component Properties_ are propagated as _Service Properties_. To mark a property as private, the property name needs to be prefixed with a full stop ('.').

First I will explain how to specify _Component Properties_ in different ways. I will use a simple example that inspects the properties in a life cycle method. After that I will show some examples on the usage of properties of service references.

Let's start to create a new project for the configurable components:

- Create a new plug-in project
    - _Main Menu → File → New → Plug-in Project_
    - Set name to _org.fipro.ds.configurable_
    - In the _Target Platform_ section select
        - This plug-in is targeted to run with: __*an OSGi framework:*__
        - Select __*standard*__ in the combobox
        - Check __*Generate OSGi metadata automatically*__  
    - Click _Next_
    - Set _Name_ to _Configurable Services_
    - Select _Execution Environment JavaSE-17_
    - Ensure that _Generate an Activator_ and _This plug-in will make contributions to the UI_ are disabled
    - Click _Finish_
    - If you do not see the tabs at the bottom of the recently opened editor with name _org.fipro.ds.configurable_, close the editor and open the _pde.bnd_ file in the project _org.fipro.ds.configurable_.
        - Switch to the _pde.bnd_ tab
            - Add the `Bundle-ActivationPolicy` to get the bundle automatically started in an Equinox runtime
            - Add the `-runee` instruction to create the requirement on Java 17
            ```
            Bundle-Name: Configurable Services
            Bundle-SymbolicName: org.fipro.ds.configurable
            Bundle-Vendor: 
            Bundle-Version: 1.0.0.qualifier
            Bundle-ActivationPolicy: lazy
            -runee: JavaSE-17
            ```
    - Create the package _org.fipro.ds.configurable_

## Inline Component Properties

You can add _Component Properties_ to a declarative service component via the `@Component` annotation **property** type element. The value of that annotation type element is an array of Strings, which need to be given as key-value pairs in the format _<name>(:<type>)?=<value>_ where the type information is optional and defaults to String.

The following types are supported:

- `String (default)`
- `Boolean`
- `Byte`
- `Short`
- `Integer`
- `Long`
- `Float`
- `Double`
- `Character`

There are typically two use cases for specifying _Component Properties_ inline:

- Define default values for _Component Properties_
- Specify some sort of meta-data that is examined by referencing components

Of course the same applies for _Component Properties_ that are applied via Properties file, as they have an equal ranking.

- Create a new class `StaticConfiguredComponent`
  - It should be a simple _Immediate Component_  
  Remember that as an _Immediate Component_, it doesn't implement an interface and it doesn't specify the **service** type element.
  - It should have the following _Component Properties_ 
    - **message** to provide a String value
    - **iteration** to provide an Integer value 
  - In the `@Activate` method the _Component Properties_ should be inspected and the **message** will be printed out to the console as often as specified in **iteration**. 

``` java
package org.fipro.ds.configurable;

import java.util.Map;
import org.osgi.service.component.annotations.Activate; 
import org.osgi.service.component.annotations.Component;

@Component(
    property = {
         "message=Welcome to the inline configured service", 
         "iteration:Integer=3"
    }
) 
public class StaticConfiguredComponent {

    @Activate 
    void activate(Map<String, Object> properties) { 
        String msg = (String) properties.get("message"); 
        Integer iter = (Integer) properties.get("iteration");

        for (int i = 1; i <= iter; i++) { 
            System.out.println("static - " + i + ": " + msg); 
        } 
        System.out.println(); 
    } 
}
```

Now execute the example as a new OSGi Framework run configuration (please have a look at [Getting Started with OSGi Declarative Services - 6. Run](getting-started-with-osgi-declarative-services.md/#6-run) to see how to setup such a configuration). If you used the same property values as specified in the above example, you should see the welcome message printed out 3 times to the console.

It is for sure not a typical use case to inspect the inline specified properties at activation time. But it should give an idea on how to specify _Component Properties_ statically inline via `@Component`.

## Component Property Types

With the DS 1.3 specification the _Component Property Types_ where introduced. They can be used as alternative to the component property `Map<String, Object>` parameter for retrieving the _Configuration Properties_ in a life cycle method. Since 1.4 _Component Property Types_ can also be used as annotations on a component implementation class. 

The _Component Property Type_ is specified as a custom annotation type, that contains property names, property types and default values. To be used as an annotation on a component implementation class, it needs to be annotated with `@ComponentPropertyType`. Further information can be found in [112.8.2 Component Property Types](https://docs.osgi.org/specification/osgi.cmpn/8.1.0/service.component.html#service.component-component.property.types).

The following snippet shows the definition of such an annotation for the above example:

``` java
package org.fipro.ds.configurable;

import org.osgi.service.component.annotations.ComponentPropertyType;

@ComponentPropertyType
public @interface MessageConfig {
    String message() default ""; 
    int iteration() default 0;
}
```

_**Note:**_  
Many of the examples found in the web show the definition of the annotation inside the component class. But of course it is also possible to create a public annotation in a separate file so it is reusable in multiple components.

The following snippet shows the above examples, modified to use a _Component Property Type_:

``` java
package org.fipro.ds.configurable;

import org.osgi.service.component.annotations.Activate; 
import org.osgi.service.component.annotations.Component;

@Component
@MessageConfig(message = "Welcome to the inline configured service", iteration = 3)
public class StaticConfiguredComponent {

    @Activate 
    void activate(MessageConfig config) {
        String msg = config.message();
        int iter = config.iteration();

        for (int i = 1; i <= iter; i++) {
            System.out.println("static - " + i + ": " + msg);
        }
        System.out.println();
    }
}
```

**Note:**  
If properties are needed that are not specified in the _Component Property Type_, you can have both as method arguments. Since DS 1.3 there are different method signatures supported, including the combination of _Component Property Type_ and the component property `Map<String, Object>`.

The reasons for choosing annotation types are:

- Limitations on annotation type definitions match component property types (no-argument methods and limited return types supported)
- Support of default values

As _Component Property Types_ are intended to be type safe, an automatic conversion happens. This is also true for _Component Properties_ that are specified via Java Properties files.


## Component Properties from resource files

Another way specify _Component Properties_ statically is to use a _Java Properties File_ that is located inside the bundle. It can be specified via the `@Component` annotation **properties** type element, where the value needs to be an entry path relative to the root of the bundle.

- Create a simple properties file named _config.properties_ in the root folder of the _org.fipro.ds.configurable_ project.

    ```
    message=Welcome to the file configured service
    iteration=4
    ```
- Add the `-includeresource` instruction to the _pde.bnd_ file  
  This is necessary to include the _config.properties_ file to the resulting bundle jar file.
    ```
    -includeresource: OSGI-INF/config.properties=config.properties
    ```
    **Note:**  
    The **destination is on the left side** of the assignment and the **source is on the right**. If only the source is specified (that means no assignment), the file is added to the bundle root without the folder where it is included in the sources.

- Create a new class `FileConfiguredComponent` as a simple _Immediate Component_
  - Use the **properties** type element in the `@Component` annotation
  - Get the _Component Properties_ **message** and **iteration** from the properties file via `MessageConfig` _Component Property Type_.

``` java
package org.fipro.ds.configurable;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

@Component(
    properties="OSGI-INF/config.properties"
)
public class FileConfiguredComponent {

    @Activate
    void activate(MessageConfig config) {
        String msg = config.message();
        int iter = config.iteration();

        for (int i = 1; i <= iter; i++) {
            System.out.println("file - " + i + ": " + msg);
        }
        System.out.println();
    }
}
```


_**Note:**_  
With a PDE Plug-in project layout, you need to add the _OSGI-INF/config.properties_ file to the _build.properties_ to include it in the resulting bundle jar file. This is of course only necessary in case you haven't added the whole directory to the _build.properties_.

On executing the example you should now see the console outputs for both components.

I've noticed two things when playing around with the Java Properties File approach:

- Compared with the inline properties it is not possible to specify a type. You can only get Strings if you use the `Map<String, Object>` parameter, which leads to manual conversions. This can be solved by using _Component Property Types_
- The properties file needs to be located in the same bundle as the component. It can not be added via fragment.

Having these two facts in mind, there are not many use cases for this approach. IMHO this approach was intended to support client specific properties that are for example placed inside the bundle in the build process.


## Component Properties via OSGi Configuration Admin

Now let's have a look at the dynamic configuration by using the OSGi Configuration Admin. For this we create a new component, although it would not be necessary, as we could also use one of the examples before (remember that we could override the statically defined _Component Properties_ dynamically via the Configuration Admin). But I wanted to start with creating a new component, to have a class that can be directly compared with the previous ones.

To specify properties via Configuration Admin it is not required to use any additional type element. You only need to know the configuration PID of the component to be able to provide a configuration object for it. The configuration PID (Persistent IDentity) is used as a key for objects that need a configuration dictionary. With regards to the _Component Configuration_ this means, we need the configuration PID to be able to provide the configuration object for the component.

The PID can be specified via the **configurationPid** type element of the `@Component` annotation. If not specified explicitly it is the same as the component name, which is the fully qualified class name.

Via the **configurationPolicy** type element it is possible to configure the relationship between component and component configuration, e.g. whether there needs to be a configuration object provided via Configuration Admin to satisfy the component. The following values are available:

- `ConfigurationPolicy.OPTIONAL`  
  Use the corresponding configuration object if present, but allow the component to be satisfied even if the corresponding configuration object is not present. This is the default value.
- `ConfigurationPolicy.REQUIRE`  
  There must be a corresponding configuration object for the component configuration to become satisfied. This means that there needs to be a configuration object that is set via Configuration Admin before the component is satisfied and therefore can be activated. With this policy it is for example possible to control the startup order or component activation based on configurations.
- `ConfigurationPolicy.IGNORE`  
  Always allow the component configuration to be satisfied and do not use the corresponding configuration object even if it is present. This basically means that the _Component Properties_ can not be changed dynamically using the Configuration Admin.

If a configuration change happens at runtime, the SCR needs to take actions based on the configuration policy. Configuration changes can be creating, modifying or deleting configuration objects. Corresponding actions can be for example that a _Component Configuration_ becomes unsatisfied and therefore _Component Instances_ are deactivated, or to call the _modified_ life cycle method, so the component is able to react on a change.

To be able to react on a configuration change at runtime, a method to handle the _modified_ life cycle can be implemented. Using the DS annotations this can be done by using the `@Modified` annotation, where the method parameters can be the same as for the other life cycle methods (see the [Getting Started Tutorial](getting-started-with-osgi-declarative-services.md) for further information on that).

**Note:**  
If you do not specify a _modified_ life cycle method, the _Component Configuration_ is deactivated and afterwards activated again with the new configuration object. This is true for the configuration policy _REQUIRE_ as well as for the configuration policy _OPTIONAL_.

- Create a new class `AdminConfiguredComponent` as an _Immediate Component_
- Specify the configuration PID _AdminConfiguredComponent_ so it is not necessary to use the full qualified class name of the component when trying to configure it.
- Set the configuration policy _REQUIRE_, so the component will only be activated once a configuration object is set by the Configuration Admin.
- Add life cycle methods for _modified_ and _deactivate_ to be able to play around with different scenarios.

``` java
package org.fipro.ds.configurable;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;

@Component(
    configurationPid = "AdminConfiguredComponent",
    configurationPolicy = ConfigurationPolicy.REQUIRE
)
public class AdminConfiguredComponent {

    @Activate
    void activate(MessageConfig config) {
        System.out.println();
        System.out.println("AdminConfiguredComponent activated");
        printMessage(config);
    }

    @Modified
    void modified(MessageConfig config) {
        System.out.println();
        System.out.println("AdminConfiguredComponent modified");
        printMessage(config);
    }

    @Deactivate
        void deactivate() {
        System.out.println("AdminConfiguredComponent deactivated");
        System.out.println();
    }

    private void printMessage(MessageConfig config) {
        String msg = config.message();
        int iter = config.iteration();

        for (int i = 1; i <= iter; i++) {
            System.out.println(i + ": " + msg);
        }
        System.out.println();
    }
}
```

If we now execute our example, we will see nothing new. The reason is of course that there is no configuration object yet provided by the Configuration Admin.

Before we are able to do this we need to prepare our environment. That means that we need to add the Configuration Admin Service to the Eclipse IDE or the used Target Platform, as it is not part of the default installation / SDK.

If you followed the DS Getting Started Tutorial and the Component Testing blog post, you have a Target Platform Definition in place, which you need to extend by adding the _Equinox Compendium SDK_.

- Open the file _org.fipro.osgi.target.target_ in the project _org.fipro.osgi.target_
- Switch to the _Source_ tab
- Add the `unit` with id `org.eclipse.equinox.compendium.sdk.feature.group` to the Eclipse Release location

```xml
<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<?pde version="3.8"?>
<target name="org.fipro.osgi.target">
    <locations>
        <location 
            includeAllPlatforms="false" 
            includeConfigurePhase="false" 
            includeMode="planner" 
            includeSource="true" 
            type="InstallableUnit">

            <unit id="org.eclipse.sdk.feature.group" version="0.0.0"/>
            <unit id="org.eclipse.equinox.compendium.sdk.feature.group" version="0.0.0"/>

            <repository location="https://download.eclipse.org/releases/2024-09"/>
        </location>
        <location 
            includeDependencyDepth="infinite" 
            includeDependencyScopes="compile" 
            includeSource="true" 
            label="org.osgi.test" 
            missingManifest="generate" 
            type="Maven">
            <dependencies>
                <dependency>
                    <groupId>org.osgi</groupId>
                    <artifactId>org.osgi.test.junit5</artifactId>
                    <version>1.3.0</version>
                    <type>jar</type>
                </dependency>
            </dependencies>
        </location>
    </locations>
</target>
```
- Switch back to the _Definition_ tab
- Click _Reload Target Platform_


Now it is possible create a Gogo Shell command that will be used to change a configuration object at runtime.

- Open the _pde.bnd_ file of the _org.fipro.ds.configurable_ project
    - Add `-buildpath: org.osgi.service.cm`
- Create a new package _org.fipro.ds.configurable.command_
- Create a new class `ConfigureServiceCommand` in that package as _Delayed Component_ that will be registered as a service for the `ConfigureCommand` class. 
- It has a reference to the `ConfigurationAdmin` service, which is used to create/get the `Configuration` object for the PID _AdminConfiguredComponent_ and updates the configuration with the given values.

``` java
package org.fipro.ds.configurable.command;

import java.io.IOException; 
import java.util.Hashtable;

import org.osgi.service.cm.Configuration; 
import org.osgi.service.cm.ConfigurationAdmin; 
import org.osgi.service.component.annotations.Component; 
import org.osgi.service.component.annotations.Reference;

@Component(
    property = {
        "osgi.command.scope=fipro",
        "osgi.command.function=configure"
    },
    service=ConfigureCommand.class )
public class ConfigureCommand {

    @Reference
    ConfigurationAdmin cm;

    public void configure(String msg, int count) throws IOException {
        Configuration config = cm.getConfiguration("AdminConfiguredComponent");
        Hashtable<String, Object> props = new Hashtable<>();
        props.put("message", msg);
        props.put("iteration", count);
        config.update(props);
    }
}
```

To set configuration values via `ConfigurationAdmin` service you still need to operate on a `Dictionary`, which means you need to know the parameter names. You can not use _Component Property Types_ here, as it is not possible to create an instance of an annotation. But of course on setting the values you are type safe.

**Note:**  
The two _Component Properties_ **osgi.command.scope** and **osgi.command.function** are specified inline. These are necessary so the Apache Gogo Shell recognizes the component as a service that can be triggered by entering the corresponding values as a command to the console. This shows the usage of _Component Properties_ as additional meta-data that is examined by other components. Also note that we need to set the **service** type element, as only services can be referenced by other components.

To execute the example you need to add additional dependencies to the _Run Configuration_:
- Open the run configuration via _Run -> Run Configurations..._
- Select the previously created run configuration and add the following _Bundles_
  - _org.eclipse.equinox.cm_
  - _org.osgi.service.cm_
  - _org.osgi.service.coordinator_

On executing the example you should notice that the `AdminConfiguredComponent` is not activated on startup, although it is an _Immediate Component_. Now execute the following command on the console: `configure foo 2`

As a result you should get an output like this:

``` console
AdminConfiguredComponent activated
1: foo
2: foo
```

If you execute the command a second time with different parameters (e.g. `configure bar 3`), the output should change to this:

``` console
AdminConfiguredComponent modified
1: bar
2: bar
3: bar
```

The component gets activated after we created a configuration object via the Configuration Admin. The reason for this is `ConfigurationPolicy.REQUIRED` which means that there needs to be a configuration object for the component configuration in order to be satisfied. Subsequent executions change the configuration object, so the **`modified`** method is called then. Now you can play around with the implementation to get a better feeling. For example, remove the **`modified`** method and see how the component life cycle handling changes on configuration changes.

**Note:**  
To start from a clean state again you need to check the option _Clear the configuration area before launching_ in the _Settings_ tab of the _Run Configuration_.

Using the **`modified`** life cycle event enables to react on configuration changes inside the component itself. To be able to react to configuration changes inside components that reference the service, the **`updated`** event method can be used.

- Create a component that references the `AdminConfiguredComponent` via _Method Injection_ to test this.  

``` java
package org.fipro.ds.configurable;

import java.util.Map;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;

@Component
public class AdminReferencingComponent {

    @Reference(bind="setAdminConfiguredComponent")
    AdminConfiguredComponent component;

    @Activate
    void activate() {
        System.out.println("AdminReferencingComponent activated");
    }

    @Modified
    void modified() {
        System.out.println("AdminReferencingComponent modified");
    }

    @Deactivate
    void deactivate() {
        System.out.println("AdminReferencingComponent deactivated");
    }

    void setAdminConfiguredComponent(Map<String, Object> properties) {
        System.out.println("AdminReferencingComponent: set service");
        printMessage(properties);
    }

    void updatedAdminConfiguredComponent(Map<String, Object> properties) {
        System.out.println("AdminReferencingComponent: update service");
        printMessage(properties);
    }

    void unsetAdminConfiguredComponent(Map<String, Object> properties) {
        System.out.println("AdminReferencingComponent: unset service");
    }

    private void printMessage(Map<String, Object> properties) {
        String msg = properties.getOrDefault("message", "").toString();
        int iter = ((Number)properties.getOrDefault("iteration", 0)).intValue();
        System.out.println("[" + msg + "|" + iter + "]");
        System.out.println();
    }
}
```

_**Note:**_  
The event methods `bind|updated|unbind` do not accept _Component Property Types_ as parameter. To access the _Service Properties_ in event methods, the `Map<String, Object>` parameter needs to be used.

_**Note:**_  
We use a combination of _Field Injection_ and _Method Injection_ for the `AdminConfiguredComponent`. The reason is that we are only dynamically interested in the configuration changes. With the combined approach we can only get the `Map<String, Object>` parameter in the event method and react on the changes there.

- Configure the `AdminConfiguredComponent` to be a service component by adding the attribute **`service=AdminConfiguredComponent.class`** to the `@Component` annotation. Otherwise it can not be referenced.

``` java
@Component(
    configurationPid = "AdminConfiguredComponent",
    configurationPolicy = ConfigurationPolicy.REQUIRE,
    service=AdminConfiguredComponent.class)
public class AdminConfiguredComponent {
```

Now execute the example and call the _configure_ command two times. The result should look similar to this:

``` console
g! configure blubb 2
AdminConfiguredComponent activated
1: blubb
2: blubb

AdminReferencingComponent: set service 
[blubb|2]

AdminReferencingComponent activated

g! configure dingens 3
AdminConfiguredComponent modified
1: dingens
2: dingens 
3: dingens

AdminReferencingComponent: update service 
[dingens|3]
```

Calling the _configure_ command the first time triggers the activation of the `AdminConfiguredComponent`, which then can be bound to the `AdminReferencingComponent`, which is satisfied and therefore can be activated afterwards. The second execution of the _configure_ command triggers the **`modified`** life cycle event method of the `AdminConfiguredComponent` and the **`updated`** event method of the `AdminReferencingComponent`.

If you ask yourself why the `AdminConfiguredComponent` is still immediately activated, although we made it a service now, the answer is, because it is referenced by an _Immediate Component_. Therefore the _target services_ need to be bound, which means the referenced services need to be activated too.

This example is also helpful in getting a better understanding of the component life cycle. For example, if you remove the **`modified`** life cycle method from the `AdminConfiguredComponent` and call the _configure_ command subsequently, both components get deactivated and activated, which results in new instances. Modifying the `@Reference` attributes will also lead to different results then. Change the _cardinality_, the _policy_ and the _policyOption_ to see the different behavior. Making the service reference `OPTIONAL|DYNAMIC|GREEDY` results in only re-activating the `AdminConfiguredComponent` but keeping the `AdminReferencingComponent` in active state. Changing it to `OPTIONAL|STATIC|GREEDY` will lead to re-activation of both components, while setting it `OPTIONAL|STATIC|RELUCTANT` any changes will be ignored, and actually nothing happens as the `AdminReferencingComponent` never gets satisfied, and therefore the `AdminConfiguredComponent` never gets activated.

The correlation between _cardinality_, _reference policy_ and _reference policy option_ is explained in detail in the OSGi Compendium Specification (table 112.1 in chapter [112.3.8 Reference Policy Option](https://docs.osgi.org/specification/osgi.cmpn/8.1.0/service.component.html#d0e28542)).

### Location Binding

Some words about location binding here. The example above created a configuration object using the single parameter version of `ConfigurationAdmin#getConfiguration(String)`. The parameter specifies the PID for which a configuration object is requested or should be created. This means that the configuration is bound to the location of the calling bundle. It then can not be consumed by other bundles. So the method is used to ensure that only the components inside the same bundle are affected.

A so-called bound configuration object is sufficient for the example above, as all created components are located in the same bundle. But there are also other cases where for example a configuration service in another bundle should be used to configure the components in all bundles of the application. This can be done by creating an unbound configuration object using the two argument version of `ConfigurationAdmin#getConfiguration(String, String)`. The first parameter is the PID and the second parameter specifies the bundle location string.

**Note:**  
The location parameter only becomes important if a configuration object will be created. If a configuration for the given PID already exists in the ConfigurationAdmin service, the location parameter will be ignored and the existing object will be returned.

You can use different values for the location argument:

- Exact bundle location identifier  
  In this case you explicitly specify the location identifier of the bundle to which the configuration object should be bound. The location identifier is set when a bundle is installed and typically it is a file URL that points to the bundle jar. It is impossible to have that hard coded and work across multiple installations. But you could retrieve it via a snippet similar to this:

   ``` java
   Bundle adminBundle = FrameworkUtil.getBundle(AdminConfiguredComponent.class);
   String location = adminBundle.getLocation();
   ```

    But doing this introduces a dependency to the bundle that should be configured, which is typically not a good practice.
- `null`  
  The location value for the binding will be set when a service with the corresponding PID is registered the first time. Note that in older versions this might have caused issues if you have multiple services with the same PID in different bundles. In that case only the services in the first bundle that requests a configuration object were able to get it because of the binding. In current implementations this doesn't seem to be the case anymore, and the `null` value haves the same way as a multi-location binding.
- Multi-locations  
  By using a multi-location binding, the configurations are dispatched to any target that has visibility to the configuration. A multi-location is specified with a leading question mark. It is possible to use only the question mark or adding a _multi-location name_ behind the question mark, e.g.

    ``` java
    Configuration config = cm.getConfiguration("AdminConfiguredComponent", "?");
    ```

    ``` java
    Configuration config = cm.getConfiguration("AdminConfiguredComponent", "?org.fipro");
    ```

    **Note:**  
  The multi-location name only has importance in case security is turned on and a _ConfigurationPermission_ is specified. Otherwise it doesn't has an effect. That means, it can not be used to restrict the targets based on the bundle symbolic name without security turned on.

To get familiar with the location binding basics create two additional bundles:

- Create a new plug-in project
    - _Main Menu → File → New → Plug-in Project_
    - Set name to _org.fipro.ds.configurator_
    - In the _Target Platform_ section select
        - This plug-in is targeted to run with: __*an OSGi framework:*__
        - Select __*standard*__ in the combobox
        - Check __*Generate OSGi metadata automatically*__  
    - Click _Next_
    - Set _Name_ to _Service Configurator_
    - Select _Execution Environment JavaSE-17_
    - Ensure that _Generate an Activator_ and _This plug-in will make contributions to the UI_ are disabled
    - Click _Finish_
    - If you do not see the tabs at the bottom of the recently opened editor with name _org.fipro.ds.configurator_, close the editor and open the _pde.bnd_ file in the project _org.fipro.ds.configurator_.
        - Switch to the _pde.bnd_ tab
            - Add the `Bundle-ActivationPolicy` to get the bundle automatically started in an Equinox runtime
            - Add the `-runee` instruction to create the requirement on Java 17
            - Add the `-buildpath` instruction and add `org.osgi.service.cm`
            ```
            Bundle-Name: Service Configurator
            Bundle-SymbolicName: org.fipro.ds.configurator
            Bundle-Vendor: 
            Bundle-Version: 1.0.0.qualifier
            Bundle-ActivationPolicy: lazy
            -runee: JavaSE-17
            -buildpath: org.osgi.service.cm
            ```
    - Create the package _org.fipro.ds.configurator_
    - Create the class `ConfCommand`
        - Copy the `ConfigureCommand` implementation
        - Change the property value for _osgi.command.function_ to _conf_
        - Change the method name from `configure` to `conf` to match the _osgi.command.function_ property
- Create a new plug-in project
    - _Main Menu → File → New → Plug-in Project_
    - Set name to _org.fipro.ds.other_
    - In the _Target Platform_ section select
        - This plug-in is targeted to run with: __*an OSGi framework:*__
        - Select __*standard*__ in the combobox
        - Check __*Generate OSGi metadata automatically*__  
    - Click _Next_
    - Set _Name_ to _Other Service_
    - Select _Execution Environment JavaSE-17_
    - Ensure that _Generate an Activator_ and _This plug-in will make contributions to the UI_ are disabled
    - Click _Finish_
    - If you do not see the tabs at the bottom of the recently opened editor with name _org.fipro.ds.other_, close the editor and open the _pde.bnd_ file in the project _org.fipro.ds.other_.
        - Switch to the _pde.bnd_ tab
            - Add the `Bundle-ActivationPolicy` to get the bundle automatically started in an Equinox runtime
            - Add the `-runee` instruction to create the requirement on Java 17
            ```
            Bundle-Name: Other Service
            Bundle-SymbolicName: org.fipro.ds.other
            Bundle-Vendor: 
            Bundle-Version: 1.0.0.qualifier
            Bundle-ActivationPolicy: lazy
            -runee: JavaSE-17
            ```
    - Create the package _org.fipro.ds.other_
    - Create the class `OtherConfiguredComponent`
        - Copy the `AdminConfiguredComponent` implementation
        - Use `Map<String, Object>` as parameter in `@Activate` and `@Modified` instead of the `MessageConfig` _Component Property Type_. This is to verify that both variants are working in combination. If you want to use `MessageConfig`, you need to create a _package-info.java_ file in the package `org.fipro.ds.configurable` and mark the package to be exported.
        - Change the console outputs to show the new class name
        - Ensure that it is an _Immediate Component_ (i.e. remove the **service** property or add the **immediate** property)
        - Ensure that **configurationPID** and **configurationPolicy** are the same as in `AdminConfiguredComponent`
        ```java
        package org.fipro.ds.other;

        import java.util.Map;

        import org.osgi.service.component.annotations.Activate;
        import org.osgi.service.component.annotations.Component;
        import org.osgi.service.component.annotations.ConfigurationPolicy;
        import org.osgi.service.component.annotations.Deactivate;
        import org.osgi.service.component.annotations.Modified;

        @Component(
            configurationPid = "AdminConfiguredComponent",
            configurationPolicy = ConfigurationPolicy.REQUIRE
        )
        public class OtherConfiguredComponent {

            @Activate
            void activate(Map<String, Object> properties) {
                System.out.println();
                System.out.println("OtherConfiguredComponent activated");
                printMessage(properties);
            }

            @Modified
            void modified(Map<String, Object> properties) {
                System.out.println();
                System.out.println("OtherConfiguredComponent modified");
                printMessage(properties);
            }

            @Deactivate
                void deactivate() {
                System.out.println("OtherConfiguredComponent deactivated");
                System.out.println();
            }

            private void printMessage(Map<String, Object> properties) {
                String msg = properties.getOrDefault("message", "").toString();
                int iter = ((Number)properties.getOrDefault("iteration", 0)).intValue();

                for (int i = 1; i <= iter; i++) {
                    System.out.println(i + ": " + msg);
                }
            }
        }
        ```

Switch to the `ConfCommand` and use three different scenarios. Change the first line of the `conf()` method according to the below description and launch the application.

1. Use the single parameter `getConfiguration(String)`  
   Calling the _conf_ command on the console will result in nothing. As the configuration object is bound to the bundle of the command, the other bundles don't see it and the contained components don't get activated.
2. Use the double parameter `getConfiguration(String, String)` where _location == null_  
   Only the component(s) of one bundle will receive the configuration object, as it will be bound to the bundle that first registers a service for the corresponding PID.
3. Use the double parameter `getConfiguration(String, String)` where _location == "?"_  
   The components of both bundles will receive the configuration object, as it is dispatched to all bundles that have visibility to the configuration. And as we didn't mention and configure permissions, all our bundles receive it.

**Note:**  
Ensure that the _Run Configuration_ contains the two new bundles in the _Bundles_ section.

### Multiple Configuration PIDs

Since DS 1.3 there can be multiple configuration PIDs specified for a component. This way it is for example possible to specify configuration objects for multiple components that share a common PID, while at the same time having a specific configuration object for a single component. To specify multiple configuration PIDs and still keep the default (that is the component name), the placeholder **"$"** can be used. By adding the following property to the `StaticConfiguredComponent` and the `FileConfiguredComponent` created before, the execution of the _configure_ command will update all four components at once.

``` java
@Component(configurationPid = {"$", "AdminConfiguredComponent"}, ... )
```

_**Note:**_  
We don't update the _configurationPid_ value of `AdminConfiguredComponent`. The reason for this is that we use the configuration policy `REQUIRE`, which means that the component only gets satisfied if there are configuration objects available for **BOTH** configuration PIDs. And our example does not create a configuration object for the default PID of the `AdminConfiguredComponent`.

The order of the configuration PIDs matters with regards to property propagation. The configuration object for a PID at the end overrides values that were applied by another configuration object for a PID before. This is similar to the propagation of inline properties or property files. The processing is sequential and therefore later processed instructions override previous ones.


### `@RequireConfigurationAdmin` / `@RequireConfigurator`

Like with the standard component property types for services, several OSGi specification implementations contain specific standard component property types. Often there are dedicated `@RequireXxx` annotations, that can be used directly or as a meta-annotation to require an implementation in the runtime. For the ConfigurationAdmin we have a requirement on the service because we referenced the `ConfigurationAdmin` in the `ConfigureCommand`. This generates the following `Require-Capability` header:

```
Require-Capability: osgi.service;
  filter:="(objectClass=org.osgi.service.cm.ConfigurationAdmin)";effective:=active
```

In cases where the `ConfigurationAdmin` is not directly referenced but needed, the requirement for the runtime can be specified via the `@RequireConfigurationAdmin` annotation. Using this for example in a _package-info.java_ file or directly on a component implementation class, will add the following `Require-Capability` header:

```
Require-Capability: osgi.implementation;
  filter:="(&(osgi.implementation=osgi.cm)(version>=1.6.0)(!(version>=2.0.0)))"
```

If you want to use the **OSGi Configurator**, which is explained in the following section, there is no other way to define the requirement on it. You need to use the corresponding `@RequireConfigurator` annotation, which generates the following `Require-Capability` header:

```
Require-Capability: osgi.extender;
  filter := "(&(osgi.extender=osgi.configurator)
              (version>=1.0)(!(version>=2.0)))"
```

_**Note:**_  
If you only use the Configurator in combination with the ConfigurationAdmin, and don't use the programmatical approach for configuration like in the above example, there is no requirement on the `ConfigurationAdmin` service. In that case you should use `@RequireConfigurationAdmin` and `@RequireConfigurator` in combination.

### Bndtools vs. PDE

As we use the _Automatic Manifest Generation_ PDE project layout, the mechanisms are quite the same. The dependencies are added via the _bnd.bnd_ file in the `-buildpath` instruction. Additionally the launch configuration needs to be updated manually to include the Configuration Admin bundle.

- Open the _launch.bndrun_ file
- On the _Run_ tab click on _Resolve_
- Verify the values values shown in the opened dialog in the _Required Resources_ section
- Click _Finish_

If you change a component class while the example is running, you will notice that the OSGi framework automatically restarts and the values set before via Configuration Admin are gone. This is because the Bndtools OSGi Framework launch configuration has two options enabled by default on the _OSGi_ tab:

- Framework: _Update bundles during runtime._
- Framework: _Clean storage area before launch._

To test the behavior of components in case of persisted configuration values, you need to disable these settings.


## Configurator

In the above example the configurations were provided to bundles programmatically. In several use cases a configuration should be provided via configuration resources instead of a programmatical way, e.g. to configure a server bundle for different deployments. For this the OSGi R7 Compendium Specification introduced the [Configurator Specification](https://docs.osgi.org/specification/osgi.cmpn/8.1.0/service.configurator.html) which defines a mechanism to feed configurations into the Configuration Admin Service through configuration resources.

Configuration resources can be part of a bundle or provided to the Configurator at startup. To seperate the configuration resource from the bundle that consumes the configuration, you could also choose to use a fragment for that bundle. As we created a setup where two bundles consume the configuration, we create a new bundle that contains the configuration resource.

As there is no Configurator implementation available in Equinox, the first step is to update the _Target Definition_ to include the necessary bundles. 

- Open the file _org.fipro.osgi.target.target_ in the project _org.fipro.osgi.target_
- Switch to the _Source_ tab
- Add a new _Maven Location_ with the following GAVs
  - `org.apache.felix:org.apache.felix.cm.json:2.0.6`
  - `org.apache.felix:org.apache.felix.configurator:1.0.18`
  - `org.eclipse.parsson:jakarta.json:1.1.6`
  - `org.osgi:org.osgi.service.configurator:1.0.1`
  - `org.osgi:org.osgi.util.converter:1.0.9`

```xml
<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<?pde version="3.8"?>
<target name="org.fipro.osgi.target">
    <locations>
        <location 
            includeAllPlatforms="false" 
            includeConfigurePhase="false" 
            includeMode="planner" 
            includeSource="true" 
            type="InstallableUnit">

            <unit id="org.eclipse.sdk.feature.group" version="0.0.0"/>
            <unit id="org.eclipse.equinox.compendium.sdk.feature.group" version="0.0.0"/>

            <repository location="https://download.eclipse.org/releases/2024-09"/>
        </location>
        <location 
            includeDependencyDepth="infinite" 
            includeDependencyScopes="compile" 
            includeSource="true" 
            label="org.osgi.test" 
            missingManifest="generate" 
            type="Maven">
            <dependencies>
                <dependency>
                    <groupId>org.osgi</groupId>
                    <artifactId>org.osgi.test.junit5</artifactId>
                    <version>1.3.0</version>
                    <type>jar</type>
                </dependency>
            </dependencies>
        </location>
        <location 
            includeDependencyDepth="none" 
            includeSource="true" 
            label="OSGi Configurator" 
            missingManifest="generate" 
            type="Maven">
            <dependencies>
                <dependency>
                    <groupId>org.apache.felix</groupId>
                    <artifactId>org.apache.felix.cm.json</artifactId>
                    <version>2.0.6</version>
                    <type>jar</type>
                </dependency>
                <dependency>
                    <groupId>org.apache.felix</groupId>
                    <artifactId>org.apache.felix.configurator</artifactId>
                    <version>1.0.18</version>
                    <type>jar</type>
                </dependency>
                <dependency>
                    <groupId>org.eclipse.parsson</groupId>
                    <artifactId>jakarta.json</artifactId>
                    <version>1.1.6</version>
                    <type>jar</type>
                </dependency>
                <dependency>
                    <groupId>org.osgi</groupId>
                    <artifactId>org.osgi.service.configurator</artifactId>
                    <version>1.0.1</version>
                    <type>jar</type>
                </dependency>
                <dependency>
                    <groupId>org.osgi</groupId>
                    <artifactId>org.osgi.util.converter</artifactId>
                    <version>1.0.9</version>
                    <type>jar</type>
                </dependency>
            </dependencies>
        </location>
    </locations>
</target>
```
- Switch back to the _Definition_ tab
- Click _Reload Target Platform_

_**Note:**_  
[Eclipse Parsson](https://github.com/eclipse-ee4j/parsson) is an implementation of Jakarta JSON Processing specification and provides a `JsonProvider` implementation, which is needed by the Felix implementation to read the configuration JSON.

- Create a new plug-in project
    - _Main Menu → File → New → Plug-in Project_
    - Set name to _org.fipro.ds.config_
    - In the _Target Platform_ section select
        - This plug-in is targeted to run with: __*an OSGi framework:*__
        - Select __*standard*__ in the combobox
        - Check __*Generate OSGi metadata automatically*__  
    - Click _Next_
    - Set _Name_ to _Service Configuration_
    - Select _Execution Environment JavaSE-17_
    - Ensure that _Generate an Activator_ and _This plug-in will make contributions to the UI_ are disabled
    - Click _Finish_
    - If you do not see the tabs at the bottom of the recently opened editor with name _org.fipro.ds.config_, close the editor and open the _pde.bnd_ file in the project _org.fipro.ds.config_.
        - Switch to the _pde.bnd_ tab
            - Add the `Bundle-ActivationPolicy` to get the bundle automatically started in an Equinox runtime
            - Add the `-runee` instruction to create the requirement on Java 17
            - Add the `-buildpath` instruction and add `org.osgi.service.configurator`
            ```
            Bundle-Name: Service Configuration
            Bundle-SymbolicName: org.fipro.ds.config
            Bundle-Vendor: 
            Bundle-Version: 1.0.0.qualifier
            Bundle-ActivationPolicy: lazy
            -runee: JavaSE-17
            -buildpath: org.osgi.service.configurator
            ```
    - Create the package _org.fipro.ds.config_
      - In the wizard check _Create package-info.java_
    - Copy the following code into the _package-info.java_ and save the file.  
    This adds the `@RequireConfigurator` to specify the requirement on a Configurator implementation bundle
        ```java
        @org.osgi.service.configurator.annotations.RequireConfigurator
        package org.fipro.ds.config;
        ```
    - Create the configuration resource file with name _configurator.json_ in the project root
        ```json
        {
            "AdminConfiguredComponent": {
                "message": "Welcome to the Configurator configured service",
                "iteration": 3
            }
        }
        ```
    - Add the `-includeresource` instruction to the _pde.bnd_ so the _configurator.json_ file is included to the resulting jar
        ```
        -includeresource: OSGI-INF/configurator/configurator.json=configurator.json
        ```
- Update the launch configuration
  - Add the following bundles to the _Bundles_ of the _OSGi Configure_ launch configuration
    - _org.fipro.ds.config_
    - _org.apache.felix.cm.json_
    - _org.apache.felix.configurator_
    - _org.eclipse.parsson.jakarta.json_
    - _org.osgi.service.configurator_
    - _org.osgi.util.converter_

If you now launch the application you will see that the `AdminConfiguredComponent` and the `OtherConfiguredComponent` are activated with the configuration provided via the Configurator JSON resource file.

_**Note:**_  
If you wonder why the `Bundle-ActivationPolicy: lazy` is always used, this is only because of the Equinox Launcher, which by default does not automatically start all bundles in the runtime. The launch configuration is actually configured to auto-start all bundles. So the lazy activation is not really necessary here. And it is also not necessary, and sometimes even problematic to use it with Bndtools.

### Bndtools vs. PDE

The only noticable difference is with the project layout and how the resources are added to the project. In the PDE project layout the _configurator.json_ can be placed in the project root and put into the resulting jar at the correct position via the `-includeresource` instruction. The Bndtools project layout is based on default Java project layouts that are used by Maven and Gradle. Resources are placed in _src/main/resources_. With a Bndtools Workspace, you need to add the `-includeresource` instruction like this:

```
-includeresource: {src/main/resources}
```
which is explained in the _Preprocessing_ section of the [bndtools documentation](https://bnd.bndtools.org/instructions/includeresource.html).

In the Maven project layout the additional `-includeresource` instruction is not necessary.

### Configuration Resource

As already mentioned the configuration resource format is JSON and must be UTF-8 encoded. Comments in the form of [JSMin (The JavaScript Minifier)](https://www.crockford.com/jsmin.html) comments are supported. This will show errors in the Eclipse IDE, because JSON does not allow comments. But the Configurator implementation does. 

By default the configuration resources are in the _OSGI-INF/configurator_ directory in the bundle. This can be changed for example via the `configurations` attribute on the `Require-Capability` header. Via the `@RequireConfigurator` annotation, this can be done simply this way, where the given path is always relative to the root of the bundle:

```java
@RequireConfigurator("resources/configs")
```

There are several Configurator keys to modify the configuration resource and how the configurations are applied. They start with `:configurator:` and provide sufficient defaults when inside a bundle, which is the reason they were not used in the example above.

It is also possible to provide an initial configuration resource from outside a bundle. This can be done by setting a system property with name `configurator.initial` where the value needs to be a valid URL. For example, if you have a configurator resource in your host system in _C:\temp_, you need to start the application with the following parameter `-Dconfigurator.initial=file://<host>/C$/temp/configurator.json`, where `<host>` is the name of your computer.

When providing a configuration resource from outside a bundle, the `:configurator:` keys on resource level need to be set.

The following example shows a configuration file that can be used from outside a bundle. Save a _configurator.json_ with similar content on your disk and launch your application with the `-Dconfigurator.initial` parameter. You will notice that now the external configuration will be applied to the services.

```json
{
    // The version of the configuration resource format.
    ":configurator:resource-version": 1,

    // The symbolic name of the configuration resource
    ":configurator:symbolic-name": "org.fipro.ds.external.config",

    // The version of this configuration resource.
    ":configurator:version": "1.0.0",

    // Configuration Dictionary for the PID AdminConfiguredComponent
    "AdminConfiguredComponent": {

        // Specifies the overwrite policy on configurations set by non-Configurator sources. 
        // default or force
        ":configurator:policy": "force",

        // The ranking of this configuration. The higher the value, the higher the ranking.
        ":configurator:ranking": 100,

        // The configuration data
        "message": "Welcome to the external configured service",
        "iteration": 5
    }
}
```

Via the `:configurator:ranking` configurator key on PID level, you can specify the ranking of the configuration. In the above example the ranking is set to 100, which is higher than the default ranking of 0. Therefore the external configuration is loaded. If you change the value for testing to -1, you will see that the previous configuration in the bundle `org.fipro.ds.config` is used again.

## Service Properties

As initially explained there is a slight difference between _Component Properties_ and _Service Properties_. _Component Properties_ are all properties specified for a component that can be accessed in life cycle methods via method parameter. _Service Properties_ can be retrieved via _Method Injection_ (bind/updated/unbind event methods) or via _Field Injection_. They contain all public _Component Properties_, which means all excluding those whose property names start with a full stop. Additionally some service properties are added that are intended to give additional information about the service. These properties are prefixed with _service_, set by the framework and specified in the OSGi Core Specification (_service.id_, _service.scope_ and _service.bundeid_).

To play around with _Service Properties_ we set up another playground. For this create the following bundles to simulate a data provider service:

- Create a new plug-in project for the API
    - _Main Menu → File → New → Plug-in Project_
    - Set name to _org.fipro.ds.data.api_
    - In the _Target Platform_ section select
        - This plug-in is targeted to run with: __*an OSGi framework:*__
        - Select __*standard*__ in the combobox
        - Check __*Generate OSGi metadata automatically*__  
    - Click _Next_
    - Set _Name_ to _Data API_
    - Select _Execution Environment JavaSE-17_
    - Ensure that _Generate an Activator_ and _This plug-in will make contributions to the UI_ are disabled
    - Click _Finish_
    - If you do not see the tabs at the bottom of the recently opened editor with name _org.fipro.ds.data.api_, close the editor and open the  _pde.bnd_ file in the project _org.fipro.ds.data.api_.
        - Switch to the _pde.bnd_ tab
          - Add the `-runee` instruction to create the requirement on Java 17
            ```
            Bundle-Name: Data API
            Bundle-SymbolicName: org.fipro.ds.data.api
            Bundle-Vendor: 
            Bundle-Version: 1.0.0.qualifier
            -runee: JavaSE-17
            ```
    - Create the package _org.fipro.ds.data_
      - In the wizard check _Create package-info.java_
    - Copy the following code into the _package-info.java_ and save the file
        ```java
        @org.osgi.annotation.bundle.Export(substitution = org.osgi.annotation.bundle.Export.Substitution.NOIMPORT)
        @org.osgi.annotation.versioning.Version("1.0.0")
        package org.fipro.ds.data;
        ```
    - Add the following service interface

        ``` java
        package org.fipro.ds.data;

        public interface DataService {

            /**
             * @param id The id of the requested data value.
            * @return The data value for the given id.
            */
            String getData(int id); 
        }
        ```
    - Add the following _Component Property Type_
        ```java
        package org.fipro.ds.data;

        import org.osgi.service.component.annotations.ComponentPropertyType;

        @ComponentPropertyType
        public @interface FiproConnectivity {

            String value();
            
        }
        ```
        _**Note:**_  
        The above _Component Property Type_ is a single-element annotation. Therefore the property name for the `value` method is derived from the name of the component property type rather than the name of the method. Based on the rules described in [112.8.2.1 Component Property Mapping](https://docs.osgi.org/specification/osgi.cmpn/8.1.0/service.component.html#service.component-component.property.types), the property name is `fipro.connectivity`. 

- Create a new plug-in project for an Online Data Service Provider
    - _Main Menu → File → New → Plug-in Project_
    - Set name to _org.fipro.ds.data.online_
    - In the _Target Platform_ section select
        - This plug-in is targeted to run with: __*an OSGi framework:*__
        - Select __*standard*__ in the combobox
        - Check __*Generate OSGi metadata automatically*__  
    - Click _Next_
    - Set _Name_ to _Online Data Provider_
    - Select _Execution Environment JavaSE-17_
    - Ensure that _Generate an Activator_ and _This plug-in will make contributions to the UI_ are disabled
    - Click _Finish_
    - If you do not see the tabs at the bottom of the recently opened editor with name _org.fipro.ds.data.online_, close the editor and open the  _pde.bnd_ file in the project _org.fipro.ds.data.online_.
        - Switch to the _pde.bnd_ tab
            - Add the `Bundle-ActivationPolicy` to get the bundle automatically started in an Equinox runtime
            - Add the `-runee` instruction to create the requirement on Java 17
            - Add the `-buildpath` instruction to specify the dependency to the API bundle
            ```
            Bundle-Name: Online Data Provider
            Bundle-SymbolicName: org.fipro.ds.data.online
            Bundle-Vendor: 
            Bundle-Version: 1.0.0.qualifier
            Bundle-ActivationPolicy: lazy
            -runee: JavaSE-17
            -buildpath: \
                org.fipro.ds.data.api
            ```
    - Create the following `DataService` implementation, that specifies the property _fipro.connectivity=online_ via the `FiproConnectivity` _Component Property Type_ for further use

        ``` java
        package org.fipro.ds.data.online;

        import org.fipro.ds.data.DataService; 
        import org.fipro.ds.data.FiproConnectivity;
        import org.osgi.service.component.annotations.Component;

        @Component
        @FiproConnectivity("online")
        public class OnlineDataService implements DataService {

            @Override 
            public String getData(int id) { 
                return "ONLINE data for id " + id; 
            } 
        }
        ```

- Create a new plug-in project for an Offline Data Service Provider
    - _Main Menu → File → New → Plug-in Project_
    - Set name to _org.fipro.ds.data.offline_
    - In the _Target Platform_ section select
        - This plug-in is targeted to run with: __*an OSGi framework:*__
        - Select __*standard*__ in the combobox
        - Check __*Generate OSGi metadata automatically*__  
    - Click _Next_
    - Set _Name_ to _Offline Data Provider_
    - Select _Execution Environment JavaSE-17_
    - Ensure that _Generate an Activator_ and _This plug-in will make contributions to the UI_ are disabled
    - Click _Finish_
    - If you do not see the tabs at the bottom of the recently opened editor with name _org.fipro.ds.data.offline_, close the editor and open the  _pde.bnd_ file in the project _org.fipro.ds.data.online_.
        - Switch to the _pde.bnd_ tab
            - Add the `Bundle-ActivationPolicy` to get the bundle automatically started in an Equinox runtime
            - Add the `-runee` instruction to create the requirement on Java 17
            - Add the `-buildpath` instruction to specify the dependency to the API bundle
            ```
            Bundle-Name: Offline Data Provider
            Bundle-SymbolicName: org.fipro.ds.data.offline
            Bundle-Vendor: 
            Bundle-Version: 1.0.0.qualifier
            Bundle-ActivationPolicy: lazy
            -runee: JavaSE-17
            -buildpath: \
                org.fipro.ds.data.api
            ```
    - Create the following `DataService` implementation, that specifies the property _fipro.connectivity=offline_ via the `FiproConnectivity` _Component Property Type_ for further use

        ``` java
        package org.fipro.ds.data.offline;

        import org.fipro.ds.data.DataService;
        import org.fipro.ds.data.FiproConnectivity;
        import org.osgi.service.component.annotations.Component;

        @Component
        @FiproConnectivity("offline")
        public class OfflineDataService implements DataService {

            @Override 
            public String getData(int id) { 
                return "OFFLINE data for id " + id; 
            } 
        }
        ```

To be able to interact with the data provider services, we create an additional console command in the bundle that references the services and shows the retrieved data on the console on execution. Add it to the bundle _org.fipro.ds.configurator_ or create a new bundle if you skipped the location binding example.

_**Note:**_  
In case you add it to the _org.fipro.ds.configurator_, don't forget to add the _org.fipro.ds.data.api_ bundle to the `-buildpath` of the _pde.bnd_.

``` java
package org.fipro.ds.configurator;

import java.util.List; 
import java.util.Map;

import org.fipro.ds.data.DataService; 
import org.osgi.service.component.annotations.Component; 
import org.osgi.service.component.annotations.Reference; 

@Component(
    property= {
        "osgi.command.scope:String=fipro",
        "osgi.command.function:String=retrieve"
    },
    service=DataRetriever.class
)
public class DataRetriever {

    @Reference(bind = "addDataService")
    private volatile List<DataService> dataServices;

    void addDataService(Map<String, Object> properties) {
        System.out.println( "Added " + properties.get("component.name"));
    }

    void removeDataService(Map<String, Object> properties) {
        System.out.println( "Removed " + properties.get("component.name"));
    }

    public void retrieve(int id) {
        for (DataService service : this.dataServices) {
            System.out.println(service.getData(id));
        }
    }
}
```

_**Note:**_  
Short reminder on the _Field Injection_ statement:
- via the _bind_ attribute in the `@Reference` annotation, we connect the event methods
- via the keyword `volatile` the **DYNAMIC RELUCTANT** reference policy
- as the member is of type `List` the cardinality is **MULTIPLE**

Add the new bundles to an existing _Run Configuration_ and execute it. By calling the retrieve command on the console you should get an output similar to this:

``` console
g! retrieve 3
OFFLINE data for id 3
ONLINE data for id 3
```

Nothing special so far. Now let's modify the example to verify the _Service Properties_.

- Modify `DataRetriever#addDataService()` to print the given properties to the console

    ``` java
    void addDataService(Map<String, Object> properties) { 
        System.out.println( "Added " + properties.get("component.name"));
        properties.forEach((k, v) -> { 
            System.out.println(k+"="+v); 
        }); 
        System.out.println(); 
    }
    ```

- Start the example and execute the _retrieve_ command. The result should now look like this:

    ``` console
    g! retrieve 3
    Added org.fipro.ds.data.online.OnlineDataService
    service.id=36
    objectClass=[Ljava.lang.String;@261be3df
    osgi.ds.satisfying.condition.target=(osgi.condition.id=true)
    component.name=org.fipro.ds.data.online.OnlineDataService
    component.id=8
    service.scope=bundle
    fipro.connectivity=online
    service.bundleid=9

    Added org.fipro.ds.data.offline.OfflineDataService
    service.id=37
    objectClass=[Ljava.lang.String;@6e61df4b
    osgi.ds.satisfying.condition.target=(osgi.condition.id=true)
    component.name=org.fipro.ds.data.offline.OfflineDataService
    component.id=9
    service.scope=bundle
    fipro.connectivity=offline
    service.bundleid=10

    OFFLINE data for id 3
    ONLINE data for id 3
    ```

    The _Service Properties_ contain the _fipro.connectivity_ property specified by us, and additionally several properties that are set by the SCR.

    _**Note:**_  
  The `DataRetriever` is not in _Immediate Component_ and therefore gets activated when the _retrieve_ command is executed the first time. The target services are bound at activation time, therefore the setter is called at that time and not before.
- Modify the `OfflineDataService`

    - Add an _Activate_ life cycle method
    - Add a property with a property name that starts with a full stop

    ``` java
    package org.fipro.ds.data.offline;

    import java.util.Map;

    import org.fipro.ds.data.DataService;
    import org.fipro.ds.data.FiproConnectivity;
    import org.osgi.service.component.annotations.Activate;
    import org.osgi.service.component.annotations.Component;

    @Component(
        property = {
            ".private=private configuration"
        }
    )
    @FiproConnectivity("offline")
    public class OfflineDataService implements DataService {

        @Activate 
        void activate(Map<String, Object> properties) { 
            System.out.println("OfflineDataService activated"); 
            properties.forEach((k, v) -> {
                System.out.println(k+"="+v); 
            }); 
            System.out.println(); 
        }

        @Override 
        public String getData(int id) { 
            return "OFFLINE data for id " + id; 
        } 
    }
    ```

    Execute the retrieve command again and verify the console output. You will notice that the output from the _Activate_ life cycle method contains the _.private_ property but no properties with a _service_ prefix. The output from the _bind_ event method on the other hand does not contain the _.private_ property, as the leading full stop marks it as a private property.

    ``` console
    g! retrieve 3
    OfflineDataService activated
    osgi.ds.satisfying.condition.target=(osgi.condition.id=true)
    component.name=org.fipro.ds.data.offline.OfflineDataService
    component.id=9
    .private=private configuration
    fipro.connectivity=offline

    Added org.fipro.ds.data.offline.OfflineDataService
    service.id=37
    objectClass=[Ljava.lang.String;@7f5e3fce
    osgi.ds.satisfying.condition.target=(osgi.condition.id=true)
    component.name=org.fipro.ds.data.offline.OfflineDataService
    component.id=9
    service.scope=bundle
    fipro.connectivity=offline
    service.bundleid=10

    ...
    ```


### Service Ranking

In case multiple services of the same type are available, the service ranking is taken into account to determine which service will get bound. In case of multiple bindings the service ranking effects in which order the services are bound. The ranking order is defined as follows:

- Sorted on descending ranking order (highest first)
- If the ranking numbers are equal, sorted on ascending _service.id_ property (oldest first)

As service ids are never reused and handed out in order of their registration time, the ordering is always complete.

The property _service.ranking_ can be used to specify the ranking order and in case of OSGi components it can be specified as a _Component Property_ via `@Component` where the value needs to be of type `Integer`. The default ranking value is zero if the property is not specified explicitly. Since DS 1.4 there are some [Standard Component Property Types](https://docs.osgi.org/specification/osgi.cmpn/8.1.0/service.component.html#service.component-standard.component.property.types) for the standard component properties. Using the `@ServiceRanking` _Component Property Type_ it is possible to configure the _service.ranking_ property.

Modify the two `DataService` implementations to specify the initial _service.ranking_ property by using the `@ServiceRanking` _Component Property Type_. For this you need to add _org.osgi.service.component_ to the `-buildpath` instruction of the _pde.bnd_ files of the two bundle projects.

``` java
@Component
@FiproConnectivity("online")
@ServiceRanking(7)
public class OnlineDataService implements DataService { ...
```

``` java
@Component(
    property = {
        ".private=private configuration"
    }
)
@FiproConnectivity("offline")
@ServiceRanking(5)
public class OfflineDataService implements DataService { ...
```

If you start the application and execute the _retrieve_ command now, you will notice that the `OnlineDataService` is called first. Change the _service.ranking_ of the `OnlineDataService` to 3 and restart the application. Now executing the _retrieve_ command will first call the `OfflineDataService`.

To make this more obvious and show that the service ranking can also be changed dynamically, create a new `DataGetter` command in the _org.fipro.ds.configurator_ bundle:

``` java
package org.fipro.ds.configurator;

import org.fipro.ds.data.DataService;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferencePolicyOption;

@Component(
    property= {
        "osgi.command.scope:String=fipro",
        "osgi.command.function:String=get"
    },
    service=DataGetter.class
)
public class DataGetter {

    @Reference(policyOption=ReferencePolicyOption.GREEDY)
    private volatile DataService dataService;

    @Activate
    void activate() {
        System.out.println("DataGetter activated");
    }
    
    public void get(int id) {
        System.out.println(this.dataService.getData(id));
    }
}
```

This command has a **MANDATORY** reference to a `DataService`. The _policy option_ is set to **GREEDY** which is necessary to bind to a higher ranked service if available. The _policy_ is set to **DYNAMIC** by using the `volatile` keyword to avoid re-activation of the `DataGetter` component if a service changes. If you change the _policy_ to **STATIC** by removing the `volatile` keyword, the binding to the higher ranked service is done by re-activating the component.

Finally create a toggle command, which dynamically toggles the _service.ranking_ property of `OnlineDataService`.

``` java
package org.fipro.ds.configurator;

import java.io.IOException; 
import java.util.Dictionary; 
import java.util.Hashtable;

import org.osgi.service.cm.Configuration; 
import org.osgi.service.cm.ConfigurationAdmin; 
import org.osgi.service.component.annotations.Component; 
import org.osgi.service.component.annotations.Reference;

@Component(
    property= {
        "osgi.command.scope:String=fipro",
        "osgi.command.function:String=ranking"
    },
    service=ToggleRankingCommand.class
)
public class ToggleRankingCommand {

    ConfigurationAdmin admin;

    @Reference 
    void setConfigurationAdmin(ConfigurationAdmin admin) { 
        this.admin = admin; 
    }

    public void ranking() throws IOException { 
        Configuration configOnline =
                this.admin.getConfiguration("org.fipro.ds.data.online.OnlineDataService", null); 
        Dictionary<String, Object> propsOnline = null; 
        if (configOnline != null && configOnline.getProperties() != null) { 
            propsOnline = configOnline.getProperties(); 
        } 
        else { 
            propsOnline = new Hashtable<>(); 
        }

        int onlineRanking = 7; 
        if (configOnline != null && configOnline.getProperties() != null) { 
            Object rank = configOnline.getProperties().get("service.ranking"); 
            if (rank != null) { 
                onlineRanking = (Integer)rank;
            } 
        }

        // toggle between 3 and 7 
        onlineRanking = (onlineRanking == 7) ? 3 : 7;

        propsOnline.put("service.ranking", onlineRanking); 
        configOnline.update(propsOnline); 
    } 
}
```

Starting the example application the first time and executing the _get_ command will return the ONLINE data. After executing the _ranking_ command, the _get_ command will return the OFFLINE data (or vice versa dependent on the initial state).

## Reference Properties

_Reference Properties_ are special _Component Properties_ that are associated with specific component references. They are used to configure component references more specifically. Currently there are the _Target Property_ and the _Minimum Cardinality Property_ available as _Reference Property_. The reference property name needs to follow the pattern _<reference\_name>.<reference\_property>_ so it can be accessed dynamically. 

### Target Property

The _Target Property_ can be specified via the `@Reference` annotation via the _target_ annotation type element. The value needs to be an LDAP filter expression and is used to select target services for the reference. The following example specifies a _Target Property_ for the `DataService` reference of the `DataRetriever` command to only select target services which specify the _Service Property_ _fipro.connectivity_ with value _online_.

```java 
@Reference(
    bind = "addDataService",
    target="(fipro.connectivity=online)")
private volatile List<DataService> dataServices;
```

If you change that in the example and execute the _retrieve_ command in the console again, you will notice that only the `OnlineDataService` will be selected by the `DataRetriever`.

In a dynamic environment it needs to be possible to change the _Target Property_ at runtime aswell. This way it is possible to react on changes to the environment for example, like whether there is an active internet connection or not. To change the _Target Property_ dynamically you can use the ConfigurationAdmin service. For this the reference property name needs to be known. Following the pattern _<reference\_name>.<reference\_property>_ this means for our example where 
- _reference\_name = dataServices_
- _reference\_property = target_ 

the reference property name is _dataServices.target_

To test this we implement a new command component in _org.fipro.ds.configurator_ that allows us to toggle the connectivity state filter on the `dataServices` reference target property.

``` java
package org.fipro.ds.configurator;

import java.io.IOException; 
import java.util.Dictionary; 
import java.util.Hashtable;

import org.osgi.service.cm.Configuration; 
import org.osgi.service.cm.ConfigurationAdmin; 
import org.osgi.service.component.annotations.Component; 
import org.osgi.service.component.annotations.Reference;

@Component(
    property= {
        "osgi.command.scope:String=fipro",
        "osgi.command.function:String=toggle"
    },
    service=ToggleConnectivityCommand.class )
public class ToggleConnectivityCommand {

    ConfigurationAdmin admin;

    @Reference 
    void setConfigurationAdmin(ConfigurationAdmin admin) { 
        this.admin = admin; 
    }

    public void toggle() throws IOException { 
        Configuration config =
                    this.admin.getConfiguration("org.fipro.ds.configurator.DataRetriever");

        Dictionary<String, Object> props = null; 
        Object target = null; 
        if (config != null && config.getProperties() != null) { 
            props = config.getProperties(); 
            target = props.get("dataServices.target"); 
        } 
        else { 
            props = new Hashtable<String, Object>(); 
        }

        boolean isOnline = (target == null || target.toString().contains("online"));

        // toggle the state 
        StringBuilder filter = new StringBuilder("(fipro.connectivity="); 
        filter.append(isOnline ? "offline" : "online").append(")");

        props.put("dataServices.target", filter.toString()); 
        config.update(props); 
    } 
}
```

Some things to notice here:

1. We use the default PID _org.fipro.ds.data.configurator.DataRetriever_ to get a configuration object.
2. We check if there is already an existing configuration. If there is an existing configuration we operate on the existing `Dictionary`. Otherwise we create a new one.
3. We try to get the current state from the `Dictionary`.
4. We create an LDAP filter String based on the retrieved information (or default if the configuration is created) and set it as reference _target property_.
5. We update the configuration with the new values.

If you launch the application you can switch between online and offline by executing the _toggle_ command.

From my observation the _reference policy_ and _reference policy option_ doesn't matter in that case. On changing the reference _target property_ dynamically, the component gets re-activated to ensure a consistent state.

### `AnyService`

Specifying the _Target Property_ directly on the reference is a static way of defining the filter. The registering of custom commands to the Apache Gogo Shell seems to work that way, as you can register any service to become a console command when the necessary properties are specified. As there are several use cases where you want to select services without a service type (e.g. Jakarta REST Services), the [AnyService](https://docs.osgi.org/specification/osgi.cmpn/8.1.0/service.component.html#org.osgi.service.component.AnyService) was introduced in DS 1.5. To make use of the **Any Service Type** you need to:
- Use `java.lang.Object` as the reference member or parameter service type.
- The special interface name `org.osgi.service.component.AnyService` must be used.
- A _Target Property_ must be present to constrain the target services to some subset of all available services.

To demonstrate the usage we create a new Gogo Shell command, that references all services with the service property `osgi.command.scope=fipro`. As the command implementations do not use a dedicated service interface, we can use the **Any Service Type** mechanism to achieve this.

- Add the `org.osgi.service.component` to the `-buildpath` instruction of the _pde.bnd_ file of the _org.fipro.ds.configurator_ project
- Create the following class in the _org.fipro.ds.configurator_ project
```java
package org.fipro.ds.configurator;

import java.util.List;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.AnyService;

@Component(
        property= {
            "osgi.command.scope:String=fipro",
            "osgi.command.function:String=listcmd"
        },
        service=ListCommands.class
    )
public class ListCommands {

    @Reference(
        service = AnyService.class, 
        target="(osgi.command.scope=fipro)")
    volatile List<Object> commands;
    
    public void listcmd() {
        commands.forEach(cmd -> System.out.println(cmd.getClass()));
    }
}
```

If you launch the application now and call the _listcmd_ in the Gogo Shell, you will see a list of all command classes that were created in this tutorial.

In [Selecting Target Services](https://docs.osgi.org/specification/osgi.cmpn/8.1.0/service.component.html#service.component-selecting.target.services) you find some more information on the selection of target services and the **Any Service Type**..


### Minimum Cardinality Property

With DS 1.3 the _Minimum Cardinality Reference Property_ was introduced. Via this reference property it is possible to modify the minimum cardinality value at runtime. While it is only possible to specify the optionality via the `@Reference` _cardinality_ attribute (this means 0 or 1), you can specify any positive number for **MULTIPLE** or **AT\_LEAST\_ONE** references. So it can be used for example to specify that at least 2 services of a special type needs to be available in order to satisfy the _Component Configuration_.

The name of the minimum cardinality property is the name of the reference appended with _.cardinality.minimum_. In our example this would be _dataServices.cardinality.minimum_

**Note:**  
The minimum cardinality can only be specified via the cardinality attribute of the reference element. So it is only possible to specify the optionality to be 0 or 1. To specify the minimum cardinality in an extended way, the minimum cardinality reference property needs to be applied via configuration.

Create a command component in _org.fipro.ds.configurator_ to modify the minimum cardinality property dynamically. It should look like the following example:

``` java
package org.fipro.ds.configurator;

import java.io.IOException; 
import java.util.Dictionary; 
import java.util.Hashtable;

import org.osgi.service.cm.Configuration; 
import org.osgi.service.cm.ConfigurationAdmin; 
import org.osgi.service.component.annotations.Component; 
import org.osgi.service.component.annotations.Reference;

@Component(
    property = {
        "osgi.command.scope=fipro",
        "osgi.command.function=cardinality"
    },
    service=ToggleMinimumCardinalityCommand.class
)
public class ToggleMinimumCardinalityCommand {

    @Reference 
    ConfigurationAdmin admin;

    public void cardinality(int count) throws IOException { 
        Configuration config =
                    this.admin.getConfiguration("org.fipro.ds.configurator.DataRetriever");

        Dictionary<String, Object> props = null; 
        if (config != null && config.getProperties() != null) { 
            props = config.getProperties(); 
        } 
        else { 
            props = new Hashtable<>(); 
        }

        props.put("dataServices.cardinality.minimum", count); 
        config.update(props); 
    } 
}
```

Launch the example and execute _retrieve 3_. You should get a valid response like before from a single service (online or offline dependent on the target property that is set). Now if you execute _cardinality 2_ and afterwards _retrieve 3_ you should get a `CommandNotFoundException`. Checking the components on the console via _scr:list_ will show that `org.fipro.ds.configurator.DataRetriever` now has a **UNSATISFIED REFERENCE**. Calling _cardinality 1_ afterwards will resolve that again.

Now you can play around and create additional services to test if this is also working for values > 1.

## Condition Service & Satisfying Condition

The order in which services and therefore bundles are started in an OSGi runtime is influeced by dependencies. If components reference other components, the referenced components need to be available before the referencing component itself is satisfied. In most cases the definition of a reference via injection is sufficient. And with the use of the _Minimum Cardinality Reference Property_ it is even possible to define the minimum number of necessary service instances to become satisfied. But there are scenarios where such a direct dependency is not possible, or hard to achieve. Especially the usage of the whiteboard pattern can make such dependencies challenging to configure. To solve this the [Condition Service](https://docs.osgi.org/specification/osgi.core/8.0.0/service.condition.html) was introduced with OSGi R8.

_**Note:**_  
The following example is intended to show the usage of the _Condition Service_. The use case could be also solved in different ways, e.g. using an extended LDAP filter or direcly referenced services.

- Add `org.osgi:org.osgi.service.condition:1.0.0` to the _Target Platform_
  ```xml
    <location 
        includeDependencyDepth="infinite" 
        includeDependencyScopes="compile" 
        includeSource="true" 
        missingManifest="generate" 
        type="Maven">
        <dependencies>
            <dependency>
                <groupId>org.osgi</groupId>
                <artifactId>org.osgi.service.condition</artifactId>
                <version>1.0.0</version>
                <type>jar</type>
            </dependency>
        </dependencies>
    </location>
  ```
- Add `org.osgi.service.condition` to the `-buildpath` instruction in the _pde.bnd_ file in the _org.fipro.ds.configurator_ project  
  _**Note:**_  
  If the dependency is not resolved directly, execute _Main Menu -> Project -> Clean..._ for that project only to update the _Plug-in Dependencies_
- Create a service of type `Condition`
  - Class name is `OnlineOfflineCondition`
  - Annotate it with `@Component`
  - Specify the component property `osgi.condition.id` with value _onoff_
  - Add references for the online `DataService` and the offline `DataService` by using the corresponding _Target Property_
  ```java
    package org.fipro.ds.configurator;

    import org.fipro.ds.data.DataService;
    import org.osgi.service.component.annotations.Component;
    import org.osgi.service.component.annotations.Reference;
    import org.osgi.service.condition.Condition;

    @Component(property="osgi.condition.id=onoff")
    public class OnlineOfflineCondition implements Condition {

        @Reference(target="(fipro.connectivity=online)")
        private DataService onlineDataService;
        
        @Reference(target="(fipro.connectivity=offline)")
        private DataService offlineDataService;
    }
  ```
- Create a new `DataRetrieverOnOff` command similar to the `DataRetriever` 
  - Add a reference to the  `OnlineOfflineCondition` by using the _Target Property_ `osgi.condition.id=onoff`
  - Remove the _Target Property_ from the `dataServices` and the event methods
  ```java
  package org.fipro.ds.configurator;

  import java.util.List;

  import org.fipro.ds.data.DataService;
  import org.osgi.service.component.annotations.Component;
  import org.osgi.service.component.annotations.Reference;
  import org.osgi.service.condition.Condition;

  @Component(
      property= {
          "osgi.command.scope:String=fipro",
          "osgi.command.function:String=onoff"},
      service=DataRetrieverOnOff.class
  )
  public class DataRetrieverOnOff {

      @Reference(target="(osgi.condition.id=onoff)")
      Condition onOffCondition;
        
      @Reference
      private volatile List<DataService> dataServices;

      public void onoff(int id) {
          for (DataService service : this.dataServices) {
              System.out.println(service.getData(id));
          }
      }
  }
  ```

The idea of this example is, that the `DataRetrieverOnOff` is only satisfied, if there is an online `DataService` and an offline `DataService` available in the runtime. So there is a dependency that both services are available before the `DataRetrieverOnOff` command can be instantiated.

Launch the application and verify that the _onoff_ command works. Enter `onoff 2` in the Gogo shell and verify that you get an output for ONLINE and OFFLINE.

Now dowable for example the `OfflineDataService` via 
```
scr:disable org.fipro.ds.data.offline.OfflineDataService
```

If you now try to call the _onoff_ command, you will see the `CommandNotFoundException`. Checking the services via `scr:list` will show that the `OnlineOfflineCondition` and therefore also the `DataRetrieverOnOff` have the **State:UNSATISFIED REFERENCE**.

If you enable the `OfflineDataService` again via 
```
scr:enable org.fipro.ds.data.offline.OfflineDataService
```
the _onoff_ command should work again.

With DS 1.5 the [Satisfying Condition](https://docs.osgi.org/specification/osgi.cmpn/8.1.0/service.component.html#service.component-satisfying.condition) was introduced, to make the configuration of a _satisfying condition_ more convenient. Instead of the reference to the `Condition` service, you can use the `@SatisfyingConditionTarget` _Component Property Type_.

Change the `DataRetrieverOnOff` to use the `@SatisfyingConditionTarget` _Component Property Type_ and execute the disable/enable test again.

```java
package org.fipro.ds.configurator;

import java.util.List;

import org.fipro.ds.data.DataService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.propertytypes.SatisfyingConditionTarget;

@Component(
    property= {
        "osgi.command.scope:String=fipro",
        "osgi.command.function:String=onoff"},
    service=DataRetrieverOnOff.class
)
@SatisfyingConditionTarget("(osgi.condition.id=onoff)")
public class DataRetrieverOnOff {
	
	@Reference
    private volatile List<DataService> dataServices;

    public void onoff(int id) {
        for (DataService service : this.dataServices) {
            System.out.println(service.getData(id));
        }
    }
}
```

That's if for this blog post. It again got much longer than I intended. But on the way writing the blog post I again learned a lot that wasn't clear to me before. I hope you also could take something out of it to use declarative services even more in your projects.

Of course you can find the sources of this tutorial in my GitHub account:

- [OSGi DS Getting Started (PDE)](https://github.com/fipro78/osgi-ds-getting-started-pde)  
  This repository contains the sources in PDE project layout.
- [OSGi DS Getting Started (Bndtools)](https://github.com/fipro78/osgi-ds-getting-started-bndtools)  
  This repository contains the sources in Bndtools project layout using a Bndtools workspace.
- [OSGi DS Gettings Started (Bnd with Maven)](https://github.com/fipro78/osgi-ds-getting-started-bnd-maven)  
  This repository contains the sources in a Maven project layout that uses the bnd Maven plugins.
