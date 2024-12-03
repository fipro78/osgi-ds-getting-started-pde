# Access OSGi Services via web interface

In this blog post I want to share a simple approach to make OSGi services available via web interface. I will show a simple approach that includes the following:

- Embedding a [Jetty](https://www.eclipse.org/jetty/) Webserver in an OSGi application
- Registering a Servlet via OSGi DS using the [Whiteboard Specification for Jakarta™ Servlet](https://docs.osgi.org/specification/osgi.cmpn/8.1.0/service.servlet.html)

_**Note:**_  
This blog post is an update to the [version from 2017](https://vogella.com/blog/access-osgi-services-via-web-interface/). 
In the current OSGi Compendium Specification 8.1 the HTTP Whiteboard Specification was replaced with the Whiteboard Specification for Jakarta™ Servlet because of the switch from the Java Servlet API to the Jakarta Servlet API.
Additionally the HTTP Service Specification was removed.

This blog post covers the usage of the Servlets and focus on embedding a Jetty Server and deploy some resources. It will not cover accessing OSGi services via REST interface. 
If you are interested in creating REST services have a look at the [Whiteboard Specification for Jakarta™ RESTful Web Services](https://docs.osgi.org/specification/osgi.cmpn/8.1.0/service.jakartars.html), which I described in my blog post [Build REST services with the OSGi Whiteboard Specification for Jakarta™ RESTful Web Services](https://vogella.com/blog/build-rest-services-with-osgi-jakarta-rs-whiteboard/).

I will skip the introduction on OSGi DS and extend the examples from my [Getting Started with OSGi Declarative Services](getting-started-with-osgi-declarative-services.md) blog. 
It is easier to follow this post when done the other tutorial first, but it is not required if you adapt the contents here to your environment.

First we need to ensure that the necessary dependencies are available for our project.

## PDE - Target Platform

In PDE it is best practice to create a Target Definition so the work is based on a specific set of bundles and we don't need to install bundles in our IDE. 

If you already have a Target Definition, ensure that the following items are part of it:
- `org.eclipse.equinox.compendium.sdk.feature.group`
- `org.osgi.service.servlet`
- `org.apache.felix.http.jetty`
- `org.apache.felix.http.servlet-api`

The reason for using `org.apache.felix.http.jetty` instead of the Jetty from the Eclipse Update Site is, that Equinox does not provide an implementation of the Whiteboard Specification for Jakarta™ Servlet that is using the Jakarta Servlet API (`jakarta.servlet`). Equinox has only an implementation of the Http Service and the Http Whiteboard Specification, which are using the Java Servlet API (`javax.servlet`). The `org.apache.felix.http.jetty` bundle contains Jetty bundles and the implementation of the R8.1 OSGi Jakarta Servlet Whiteboard, the R7 OSGi Http Service and the R7 OSGi Http Whiteboard Specification.

The reason for using `org.apache.felix.http.servlet-api` is that it wraps the Java Servlet API and the Jakarta Servlet API in one bundle, which is needed by `org.apache.felix.http.jetty`, as it currently supports both implementations in one bundle.
It also provides several versions of the APIs, which helps in resolving the OSGi bundle dependencies.

If you don't have a Target Definition in your project, create a new Target Definition with the following steps:

- Create the target platform project
    - _Main Menu → File → New → Project → General → Project_
    - Set name to _org.fipro.osgi.target_
    - Click _Finish_
- Create a new target definition
    - _Right click on project → New → Target Definition_
    - Set the filename to _org.fipro.osgi.target.target_
    - Initialize the target definition with: _Nothing: Start with an empty target definition_
    - Click _Finish_

After you created a new Target Definition, or opening an existing Target Definition, add the required dependencies for this tutorial:

- Add a new Software Site in the opened _Target Definition Editor_ 
    - Alternative A
        - Switch to the _Source_ tab and add the following snippet to the editor  
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
            <location includeDependencyDepth="none" includeSource="true" label="Servlet" missingManifest="generate" type="Maven">
                <dependencies>
                    <dependency>
                        <groupId>org.osgi</groupId>
                        <artifactId>org.osgi.service.servlet</artifactId>
                        <version>2.0.0</version>
                        <type>jar</type>
                    </dependency>
                    <dependency>
                        <groupId>org.apache.felix</groupId>
                        <artifactId>org.apache.felix.http.jetty</artifactId>
                        <version>5.1.26</version>
                        <type>jar</type>
                    </dependency>
                    <dependency>
                        <groupId>org.apache.felix</groupId>
                        <artifactId>org.apache.felix.http.servlet-api</artifactId>
                        <version>3.0.0</version>
                        <type>jar</type>
                    </dependency>
                </dependencies>
            </location>
        </locations>
    </target>
    ```
    - Alternative B
      - By clicking _Add..._ in the _Locations_ section
        - Select _Software Site_
        - Software Site _https://download.eclipse.org/releases/2024-09_
        - Disable _Group by Category_
        - Select the following entries
          - _Eclipse Project SDK_
          - _Equinox Compendium SDK_
        - Click _Finish_
      - Click _Add..._ in the _Locations_ section
        - Select _Maven_
        - Add the GAVs to 
          - _**org.osgi:org.osgi.service.servlet:2.0.0**_
          - _**org.apache.felix:org.apache.felix.http.jetty:5.1.26**_
          - _**org.apache.felix:org.apache.felix.http.servlet-api:3.0.0**_
        - Set a _Label_: _**Servlet**_
        - _Dependencies depth_: _**none**_
        - Click _Finish_
- Switch to the _Definition_ tab
    - Wait until the Target Definition is completely resolved (check the progress at the bottom right)
    - Activate the target platform by clicking _Set as Target Platform_ in the upper right corner of the Target Definition Editor


## Bndtools - Repository

Using the Bndtools project setup, the dependencies are configured via repositories. The dependencies needed for this tutorial are already included in the predefined repositories, so there are no additional actions needed here.
In case you want to update the dependencies or add new entries, have a look at the file _cnf/ext/runtime.mvn_.

For the Bndtools Maven project setup, of course the dependencies need to be added in the pom.xml files.

## Servlet Provider Bundle

The next step is to create a new plug-in / bundle project _org.fipro.inverter.http_ in which we will add the resources that we create in this tutorial. 

- Create a new plug-in project
    - _Main Menu → File → New → Plug-in Project_
    - Set name to _org.fipro.inverter.http_
    - In the _Target Platform_ section select
        - This plug-in is targeted to run with: __*an OSGi framework:*__
        - Select __*standard*__ in the combobox
        - Check __*Generate OSGi metadata automatically*__  
    - Click _Next_
    - Set _Name_ to _Inverter Servlet_
    - Select _Execution Environment JavaSE-17_
    - Ensure that _Generate an Activator_ and _This plug-in will make contributions to the UI_ are disabled
    - Click _Finish_
    - If you do not see the tabs at the bottom of the recently opened editor with name _org.fipro.inverter.http_, close the editor and open the  _pde.bnd_ file in the project _org.fipro.inverter.http_.
        - Switch to the _pde.bnd_ tab
            - Add the `Bundle-ActivationPolicy` to get the bundle automatically started in an Equinox runtime
            - Add the `-runee` instruction to create the requirement on Java 17
            - Add the `-buildpath` instruction to specify the dependency to the Inverter API and the Jakarta Servlet bundles.
            ```
            Bundle-Name: Inverter Servlet
            Bundle-SymbolicName: org.fipro.inverter.http
            Bundle-Vendor: 
            Bundle-Version: 1.0.0.qualifier
            Bundle-ActivationPolicy: lazy
            -runee: JavaSE-17
            -buildpath: \
                org.fipro.inverter.api, \
                org.apache.felix.http.servlet-api,\
                org.osgi.service.servlet
            ```

## Bndtools

With Bndtools create a new _Bnd OSGi Project_ using the _Component Development_ template.
Since we use the _Automatic Manifest Generation_ PDE project layout, the depenency management is actually quite the same.

- Open the _bnd.bnd_ file of the _org.fipro.inverter.http_ project and switch to the _Build_ tab
- Add the following bundles to the _Build Path_
    - _org.apache.http.felix.jetty_
    - _org.apache.http.felix.servlet-api_
    - _org.fipro.inverter.api_
    - _org.osgi.service.servlet_

## Create a `Servlet` implementation

- Create a new package `org.fipro.inverter.http`
- Create a new class `InverterServlet`
  - It should be a typical `Servlet` implementation that extends `jakarta.servlet.http.HttpServlet`
  - It should also be an OSGi Declarative Service that is registered as service of type `jakarta.servlet.Servlet`
  - The service should have `ServiceScope.PROTOTYPE`
  - A special property `osgi.http.whiteboard.servlet.pattern` needs to be set to _/invert_. 
  This configures the context path of the `Servlet`. 
  Instead of using the `propery` attribute of the `@Component` annotation, use the `@HttpWhiteboardServletPattern` component property type.
  - Add a references to the `StringInverter` OSGi service from the _Getting Started_ tutorial via field reference.

``` java
package org.fipro.inverter.http;

import java.io.IOException;

import jakarta.servlet.Servlet;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.fipro.inverter.StringInverter;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;
import org.osgi.service.servlet.whiteboard.propertytypes.HttpWhiteboardServletPattern;

@Component(
    service=Servlet.class,
    scope=ServiceScope.PROTOTYPE)
@HttpWhiteboardServletPattern("/invert")
public class InverterServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Reference
    private StringInverter inverter;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String input = req.getParameter("value");
        if (input == null) {
            throw new IllegalArgumentException("input can not be null");
        }
        String output = inverter.invert(input);

        resp.setContentType("text/html");
        resp.getWriter().write( "<html><body>Result is " + output + "</body></html>");
    }
}
```

## PDE - Launch the example

Before explaining the details further, launch the example to see if our servlet is available via standard web browser. For this we create a launch configuration, so we can start directly from the IDE.

- Select the menu entry _Run -> Run Configurations…_
- In the tree view, right click on the _OSGi Framework_ node and select _New_ from the context menu
- Specify a name, e.g. _OSGi Inverter Http_
- _Deselect All_
- Select the following bundles
    - **Application bundles**
        - _org.fipro.inverter.api_
        - _org.fipro.inverter.http_
        - _org.fipro.inverter.provider_
    - **OSGi framework and DS bundles**
        - _org.apache.felix.scr_
        - _org.eclipse.osgi_
        - _org.osgi.service.component_
        - _org.osgi.util.function_
        - _org.osgi.util.promise_
    - **Jetty**
        - _org.osgi.service.servlet_
        - _org.apache.felix.http.jetty_
        - _org.apache.felix.http.servlet-api_
        - _slf4j.api_
        - _slf4j.nop_
    - **SPI Fly**
        - _org.apache.aries.spifly.dynamic.bundle_
        - _org.objectweb.asm_
        - _org.objectweb.asm.commons_
        - _org.objectweb.asm.tree_
        - _org.objectweb.asm.tree.analysis_
        - _org.objectweb.asm.util_
- Ensure that _Default Auto-Start_ is set to _true_
- Switch to the Arguments tab
    - Remove `-consoleLog -console` from the _Program arguments_
    - Remove `-Dorg.eclipse.swt.graphics.Resource.reportNonDisposed=true` from the _VM arguments_
    - Add `-Dorg.osgi.service.http.port=8080` to the _VM arguments_
- Click _Run_

_**Note:**_  
The [**SPI Fly**](https://aries.apache.org/documentation/modules/spi-fly.html) bundles add support for JRE SPI mechanisms and are required by SLF4J 2.x.

_**Note:**_  
If you include the above bundles in an Eclipse RCP application, ensure that you auto-start the _org.apache.aries.spifly.dynamic.bundle_ and the _org.apache.felix.http.jetty_ bundle to automatically start the Jetty server. This can be done on the _Configuration_ tab of the Product Configuration Editor.

If you now open a browser and go to the URL [http://localhost:8080/invert?value=Eclipse](http://localhost:8080/invert?value=Eclipse) you should get a response with the inverted output.

## Bndtools - Launch the example

- Open the _launch.bndrun_ file in the _org.fipro.inverter.http_ project
- On the _Run_ tab add the following bundles to the _Run Requirements_
    - _org.fipro.inverter.http_
    - _org.fipro.inverter.provider_
- Click _Resolve_ to ensure all required bundles are added to the _Run Bundles_ via auto-resolve
- Add `-Dorg.osgi.service.http.port=8080` to the _JVM Arguments_
- Click _Run OSGi_

## Interlude: `@RequireHttpWhiteboard`

As with most of the OSGi specifications, it is possible to specify a requirement on a specification implementation. For the Jakarta Servlet Whiteboard this means the following entry in the MANIFEST.MF:

```
Require-Capability: osgi.implementation;
       filter:="(&(osgi.implementation=osgi.http)
                 (version>=2.0)(!(version>=2.0)))"
```

To simplify the creation of the requirement, the `@RequireHttpWhiteboard` annotation can be used. 
If you use one of the Jakarta Servlet Whiteboard Component Property Type annotations, it is not necessary to explicitly add the `@RequireHttpWhiteboard`.
It is used by those annotations already and therefore added transitively.

## Jakarta Servlet Whiteboard

Now why is this simply working? We only implemented a `Servlet` and provided it as OSGi DS. And it is "magically" available via web interface. 
The answer to this is the OSGi [Whiteboard Specification for Jakarta™ Servlet](https://docs.osgi.org/specification/osgi.cmpn/8.1.0/service.servlet.html).
It provides the same functionality as its predecessor, [Http Whiteboard Specification](https://docs.osgi.org/specification/osgi.cmpn/8.0.0/service.http.whiteboard.html) in the OSGi Compendium Specification 8.0, with the switch from the Java Servlet API to the Jakarta Servlet API.

Servlets are used to provide dynamic content on the Internet using Java. 
The Jakarta Servlet Whiteboard Specification allows to register servlets and resources via the [Whiteboard Pattern](http://www.osgi.org/wiki/uploads/Links/whiteboard.pdf), without the need to know the how this is done in detail. 
I always think about the whiteboard pattern as a "_don't call us, we will call you_" pattern. 
That means you don't need to register servlets with a web application server directly, you will provide it as a service to the service registry, and the Jakarta Servlet Whiteboard implementation will take it and register it.

Via Jakarta Servlet Whiteboard it is possible to register:

- Servlets
- Servlet Filters
- Resources
- Servlet Listeners

I will show some examples to be able to play around with the Jakarta Servlet Whiteboard.

### Register Servlets

An example on how to register a servlet via the Servlet Whiteboard is shown above. The main points are:

- The servlet needs to be registered as OSGi service of type `jakarta.servlet.Servlet`.
- The component property _osgi.http.whiteboard.servlet.pattern_ needs to be set to specify the request mappings.
- The service scope should be `PROTOTYPE`.

For registering servlets the following component properties are supported. (see [OSGi Compendium Specification - Table 140.4](https://docs.osgi.org/specification/osgi.cmpn/8.1.0/service.servlet.html#service.servlet-i21223311)):

| **Component Property** | **Description** | **Component Property Type** |
| --- | --- | --- |
| _osgi.http.whiteboard.servlet.asyncSupported_ | Declares whether the servlet supports the asynchronous operation mode. Allowed values are `true` and `false` independent of case. Defaults to `false`. | `@HttpWhiteboardServletAsyncSupported` |
| _osgi.http.whiteboard.servlet.errorPage_ | Register the servlet as an error page for the error code and/or exception specified; the value may be a fully qualified exception type name or a three-digit HTTP status code in the range 400-599. Special values 4xx and 5xx can be used to match value ranges. Any value not being a three-digit number is assumed to be a fully qualified exception class name. | `@HttpWhiteboardServletErrorPage` |
| _osgi.http.whiteboard.servlet.name_ | The name of the servlet. This name is used as the value of the `jakarta.servlet.ServletConfig.getServletName()` method and defaults to the fully qualified class name of the service object. | `@HttpWhiteboardServletName` |
| _osgi.http.whiteboard.servlet.pattern_ | Registration pattern(s) for the servlet. | `@HttpWhiteboardServletPattern` | 
| _servlet.init.\*_ | Properties starting with this prefix are provided as init parameters to the `jakarta.servlet.Servlet.init(ServletConfig)` method. The _servlet.init._ prefix is removed from the parameter name. | - |

The Jakarta Servlet Whiteboard service needs to call `jakarta.servlet.Servlet.init(ServletConfig)` to initialize the servlet before it starts to serve requests, and when it is not needed anymore `jakarta.servlet.Servlet.destroy()` to shut down the servlet. 
If more than one Servlet Whiteboard implementation is available in a runtime, the `init()` and `destroy()` calls would be executed multiple times, which violates the Servlet specification. 
It is therefore recommended to use the `PROTOTYPE` service scope for servlets to ensure that every Servlet Whiteboard implementation gets its own service instance.

_**Note:**_  
In a controlled runtime, like an RCP application that is delivered with one Jakarta Whiteboard implementation and that does not support installing bundles at runtime, the usage of the `PROTOTYPE` scope is not required. 
Actually such a runtime ensures that the servlet is only instantiated and initialized once. But if possible it is recommended that the `PROTOTYPE` scope is used.

### Register Error Pages

To register a servlet as an error page, the service property _osgi.http.whiteboard.servlet.errorPage_ needs to be set.
The value can be either a three-digit  HTTP error code, the special codes 4xx or 5xx to specify a range or error codes, or a fully qualified exception class name. 
To configure the service property _osgi.http.whiteboard.servlet.errorPage_ you can use the `@HttpWhiteboardServletErrorPage` component property type.
The service property _osgi.http.whiteboard.servlet.pattern_ is not required for servlets that provide error pages.

The following snippet shows an error page servlet that deals with `IllegalArgumentExceptions` and the HTTP error code 500. It can be tested by calling the inverter servlet without a query parameter.

``` java
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
        resp.getWriter().write("<html><body>You need to provide an input!</body></html>");
    } 
}
```

### Register Filters

Via servlet filters it is possible to intercept servlet invocations. They are used to modify the `ServletRequest` and `ServletResponse` to perform common tasks before and after the servlet invocation.

The example below shows a servlet filter that adds a simple header and footer on each request to the servlet with the _/invert_ pattern:

``` java
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
    public void init(FilterConfig filterConfig) throws ServletException { }

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
```

To register a servlet filter the following criteria must match:

- It needs to be registered as OSGi service of type `jakarta.servlet.Filter`.
- One of the given component properties needs to be set:
    - _osgi.http.whiteboard.filter.pattern_
    - _osgi.http.whiteboard.filter.regex_
    - _osgi.http.whiteboard.filter.servlet_
- The service scope should be `PROTOTYPE`.

For registering servlet filters the following service properties are supported. (see [OSGi Compendium Specification - Table 140.5](https://docs.osgi.org/specification/osgi.cmpn/8.1.0/service.servlet.html#d0e87922)):

| **Service Property** | **Description** | **Component Property Type** |
| --- | --- | --- |
| _osgi.http.whiteboard.filter.asyncSupported_ | Declares whether the servlet filter supports asynchronous operation mode. Allowed values are `true` and `false` independent of case. Defaults to `false`. | `@HttpWhiteboardFilterAsyncSupported` |
| _osgi.http.whiteboard.filter.dispatcher_ | Select the dispatcher configuration when the servlet filter should be called. Allowed string values are REQUEST, ASYNC, ERROR, INCLUDE, and FORWARD. The default for a filter is REQUEST. | `@HttpWhiteboardFilterDispatcher` |
| _osgi.http.whiteboard.filter.name_ | The name of a servlet filter. This name is used as the value of the `FilterConfig.getFilterName()` method and defaults to the fully qualified class name of the service object. | `@HttpWhiteboardFilterName` |
| _osgi.http.whiteboard.filter.pattern_ | Apply this servlet filter to the specified URL path patterns. The format of the patterns is specified in the servlet specification. | `@HttpWhiteboardFilterPattern` |
| _osgi.http.whiteboard.filter.regex_ | Apply this servlet filter to the specified URL paths. The paths are specified as regular expressions following the syntax defined in the `java.util.regex.Pattern` class. | `@HttpWhiteboardFilterRegex` |
| _osgi.http.whiteboard.filter.servlet_ | Apply this servlet filter to the referenced servlet(s) by name. | `@HttpWhiteboardFilterServlet` |
| _filter.init.\*_ | Properties starting with this prefix are passed as init parameters to the `Filter.init()` method. The _filter.init._ prefix is removed from the parameter name. | - |

### Register Resources

It is also possible to register a service that informs the Jakarta Servlet Whiteboard service about static resources like HTML files, images, CSS- or Javascript-files. 
For this a simple service can be registered that only needs to have the following two mandatory service properties set:

| **Service Property** | **Description** | **Component Property Type** |
| --- | --- | --- |
| _osgi.http.whiteboard.resource.pattern_ | The pattern(s) to be used to serve resources. As defined by the [Jakarta Servlet 5.0 Specification in section 12.2, Specification of Mappings](https://jakarta.ee/specifications/servlet/5.0/jakarta-servlet-spec-5.0#specification-of-mappings). This property marks the service as a resource service. | `@HttpWhiteboardResource#pattern()` |
| _osgi.http.whiteboard.resource.prefix_ | The prefix used to map a requested resource to the bundle's entries. If the request's path info is not null, it is appended to this prefix. The resulting string is passed to the `getResource(String)` method of the associated Servlet Context Helper. | `@HttpWhiteboardResource#prefix()` |

The service does not need to implement any specific interface or function. All required information is provided via the component properties.

To create a resource service follow these steps:

- Create a folder _resources_ in the project _org.fipro.inverter.http_
- Add an image in that folder, e.g. _eclipse\_logo.png_
- Open the _.bnd_ file (either _pde.bnd_ or _bnd.bnd_)
  - Switch to the _Source_ tab (_pde.bnd_ tab for PDE Tools) and add the following line  
  `-includeresource: resources=resources`  
  _**Note:**_  
  The `includeresource` instruction is not necessary with the Bndtools Maven project setup. Place the _resource_ in _src/main/resources_ and the folder will be automatially included to the resulting bundle.
- Create resource service

``` java
package org.fipro.inverter.http;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.servlet.whiteboard.propertytypes.HttpWhiteboardResource;

@Component(service = ResourceService.class)
@HttpWhiteboardResource(pattern = "/files/*", prefix = "/resources")
public class ResourceService { }
```

After starting the application the static resources located in the _resources_ folder are available via the _/files_ path in the URL, e.g. [http://localhost:8080/files/eclipse\_logo.png](http://localhost:8080/files/eclipse_logo.png)

_**Note:**_  
While writing this blog post I came across a very nasty issue. Because I initially registered the servlet filter for the _/\*_ pattern, the simple header and footer where always added. This also caused setting the content type, that didn't match the content type of the image of course. And so the static content was never shown correctly. So if you want to use servlet filters to add common headers and footers, you need to take care of the pattern so the servlet filter is not applied to static resources.

### Register Servlet Listeners

It is also possible to register different servlet listeners as whiteboard services. The following listeners are supported according to the servlet specification:

- `ServletContextListener`  
Receive notifications when Servlet Contexts are initialized and destroyed.
- `ServletContextAttributeListener`  
Receive notifications for Servlet Context attribute changes.
- `ServletRequestListener`  
Receive notifications for servlet requests coming in and being destroyed.
- `ServletRequestAttributeListener`  
Receive notifications when servlet Request attributes change.
- `HttpSessionListener`  
Receive notifications when Http Sessions are created or destroyed.
- `HttpSessionAttributeListener`  
Receive notifications when Http Session attributes change.
- `HttpSessionIdListener`  
Receive notifications when Http Session ID changes.

There is only one component property needed to be set so the Jakarta Servlet Whiteboard implementation is handling the listener.

| **Service Property** | **Description** | **Component Property Type** |
| --- | --- | --- |
| _osgi.http.whiteboard.listener_ | When set to `true` this listener service is handled by the Jakarta Servlet Whiteboard implementation. When not set or set to `false` the service is ignored. Any other value is invalid. | `@HttpWhiteboardListener` |

The following example shows a simple `ServletRequestListener` that prints out the client address on the console for each request (borrowed from the OSGi Compendium Specification):

``` java
package org.fipro.inverter.http;

import jakarta.servlet.ServletRequestEvent;
import jakarta.servlet.ServletRequestListener;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.servlet.whiteboard.propertytypes.HttpWhiteboardListener;

@Component
@HttpWhiteboardListener
public class SimpleServletRequestListener implements ServletRequestListener {

    public void requestInitialized(ServletRequestEvent sre) {
        System.out.println("Request initialized for client: "
            + sre.getServletRequest().getRemoteAddr());
    }

    public void requestDestroyed(ServletRequestEvent sre) {
        System.out.println("Request destroyed for client: "
            + sre.getServletRequest().getRemoteAddr());
    }
}
```

### Servlet Context and Common Whiteboard Properties

The `ServletContext` is specified in the servlet specification and provided to the servlets at runtime by the container. 
By default there is one `ServletContext` and without additional information the servlets are registered to that default `ServletContext` via the Jakarta Servlet Whiteboard implementation. 
This could lead to scenarios where different bundles provide servlets for the same request mapping. 
In that case the _service.ranking_ will be inspected to decide which servlet should be delivered. 
If the servlets belong to different applications, it is possible to specify different contexts. 
This can be done by registering a custom `ServletContextHelper` as whiteboard service and associate the servlets to the corresponding context. 
The `ServletContextHelper` can be used to customize the behavior of the `ServletContext` (e.g. handle security, provide resources, ...) and to support multiple web-applications via different context paths.

A custom `ServletContextHelper` it needs to be registered as service of type `ServletContextHelper` and needs to have the following two service properties set:

- _osgi.http.whiteboard.context.name_
- _osgi.http.whiteboard.context.path_

To make the configuration more convenient, you can use the `@HttpWhiteboardContext` component property type.

| **Service Property** | **Description** | **Component Property Type** |
| --- | --- | --- |
| _osgi.http.whiteboard.context.name_ | Name of the Servlet Context Helper. This name can be referred to by Whiteboard services via the _osgi.http.whiteboard.context.select_ property. The syntax of the name is the same as the syntax for a Bundle Symbolic Name. The default Servlet Context Helper is named default. To override the default, register a custom `ServletContextHelper` service with the name default. If multiple Servlet Context Helper services are registered with the same name, the one with the highest Service Ranking is used. In case of a tie, the service with the lowest service ID wins. In other words, the normal OSGi service ranking applies. | `@HttpWhiteboardContext#name` |
| _osgi.http.whiteboard.context.path_ | Additional prefix to the context path for servlets. This property is mandatory. Valid characters are specified in IETF RFC 3986, section 3.3. The context path of the default Servlet Context Helper is /. A custom default Servlet Context Helper may use an alternative path. | `@HttpWhiteboardContext#path` |
| _context.init.\*_ | Properties starting with this prefix are provided as init parameters through the `ServletContext.getInitParameter()` and `ServletContext.getInitParameterNames()` methods. The _context.init._ prefix is removed from the parameter name. | - |

The following example will register a `ServletContextHelper` for the context path _/eclipse_ and will retrieve resources from _http://eclipse.dev_. 
It is registered with `BUNDLE` service scope to ensure that every bundle gets its own instance, which is for example important to resolve resources from the correct bundle.

- Create a new package `org.fipro.inverter.http.eclipse` in the _org.fipro.inverter.http_ project
- Add the following class to the new package

``` java
package org.fipro.inverter.http.eclipse;

import java.net.MalformedURLException;
import java.net.URL;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;
import org.osgi.service.servlet.context.ServletContextHelper;
import org.osgi.service.servlet.whiteboard.propertytypes.HttpWhiteboardContext;

@Component(
    service = ServletContextHelper.class,
    scope = ServiceScope.BUNDLE)
@HttpWhiteboardContext(name = "eclipse", path = "/eclipse")
public class EclipseServletContextHelper extends ServletContextHelper {

    public URL getResource(String name) {
        // remove the path from the name
        name = name.replace("/eclipse", "");
        try {
            return new URL("https://eclipse.dev/" + name);
        } catch (MalformedURLException e) {
            return null;
        }
    }
}
```

To associate servlets, servlet filter, resources and listeners to a `ServletContextHelper`, they share common service properties (see [OSGi Compendium Specification - Table 140.3](https://docs.osgi.org/specification/osgi.cmpn/8.1.0/service.servlet.html#service.http.whiteboard.common.properties)) additional to the service specific properties:

| **Service Property** | **Description** | **Component Property Type** |
| --- | --- | --- |
| _osgi.http.whiteboard.context.select_ | An LDAP-style filter to select the associated `ServletContextHelper` service to use. Any service property of the Servlet Context Helper can be filtered on. If this property is missing the default Servlet Context Helper is used.<br><br>For example, to select a Servlet Context Helper with name myCTX provide the following value:<br>`(osgi.http.whiteboard.context.name=myCTX)`<br><br>To select all Servlet Context Helpers provide the following value:<br>`(osgi.http.whiteboard.context.name=*)` | `@HttpWhiteboardContextSelect` |
| _osgi.http.whiteboard.target_ | The value of this service property is an LDAP style filter expression to select the Jakarta Whiteboard implementation(s) to handle this Whiteboard service. The LDAP filter is used to match `HttpServiceRuntime` services. Each Servlet Whiteboard _implementation_ exposes exactly one `HttpServiceRuntime` service. This property is used to associate the Whiteboard service with the Jakarta Whiteboard implementation that registered the `HttpServiceRuntime` service. If this property is not specified, all Jakarta Whiteboard implementations can handle the service. | `@HttpWhiteboardTarget` |

The following example will register a servlet only for the introduced _/eclipse_ context:

``` java
package org.fipro.inverter.http.eclipse;

import java.io.IOException;

import jakarta.servlet.Servlet;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;
import org.osgi.service.servlet.whiteboard.propertytypes.HttpWhiteboardContextSelect;
import org.osgi.service.servlet.whiteboard.propertytypes.HttpWhiteboardServletPattern;

@Component(
    service=Servlet.class,
    scope=ServiceScope.PROTOTYPE) 
@HttpWhiteboardServletPattern("/image")
@HttpWhiteboardContextSelect("(osgi.http.whiteboard.context.name=eclipse)")
public class ImageServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("text/html");
        resp.getWriter().write("Show an image from https://eclipse.dev");
        resp.getWriter().write( "<p><img src='img/nattable/FeatureScreenShot.png'/></p>");
    }
}
```

And to make this work in combination with the introduced `ServletContextHelper` we need to additionally register the resources for the _/img_ context, which is also only assigned to the _/eclipse_ context:

``` java
package org.fipro.inverter.http.eclipse;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.servlet.whiteboard.propertytypes.HttpWhiteboardContextSelect;
import org.osgi.service.servlet.whiteboard.propertytypes.HttpWhiteboardResource;

@Component(service = EclipseImageResourceService.class)
@HttpWhiteboardResource(pattern = "/img/*", prefix = "/eclipse")
@HttpWhiteboardContextSelect("(osgi.http.whiteboard.context.name=eclipse)")
public class EclipseImageResourceService { }
```

If you start the application and browse to [http://localhost:8080/eclipse/image](http://localhost:8080/eclipse/image) you will see an output from the servlet together with an image that is loaded from _https://eclipse.dev_.

_**Note:**_  
The component properties and predefined values are available via `org.osgi.service.http.whiteboard.HttpWhiteboardConstants`. 
So you don't need to remember them all and can also retrieve some additional information about the properties via the corresponding Javadoc.

The sources for this tutorial are hosted on GitHub in the already existing projects:

- [OSGi DS Getting Started (PDE)](https://github.com/fipro78/osgi-ds-getting-started-pde)  
  This repository contains the sources in PDE project layout.
- [OSGi DS Getting Started (Bndtools)](https://github.com/fipro78/osgi-ds-getting-started-bndtools)  
  This repository contains the sources in Bndtools project layout using a Bndtools workspace.
- [OSGi DS Gettings Started (Bnd with Maven)](https://github.com/fipro78/osgi-ds-getting-started-bnd-maven)  
  This repository contains the sources in a Maven project layout that uses the bnd Maven plugins.
