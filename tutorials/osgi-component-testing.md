# OSGi Component Testing

In my last blog post I talked about [Getting Started with OSGi Declarative Services](getting-started-with-osgi-declarative-services.md). In this blog post I want to show how to test OSGi service components. This is an update to my [2016 version of this blog post about OSGi Component testing](https://vogella.com/blog/osgi-component-testing/).

## Unit testing / "white-box testing"

The first approach for testing components is to write unit tests. In a plain Java project such tests are added in an additional source folder (typically named _test_). They can then be executed from the IDE and at build time, but left out in the resulting JAR. With Bndtools the same approach is used for unit or _white-box-testing_.

In Eclipse RCP development you typically create a test fragment for the bundle that should be tested. This way the tests can be executed automatically via [Tycho Surefire Plugin](https://tycho.eclipseprojects.io/doc/latest/tycho-surefire-plugin/plugin-info.html), when running the build process via Maven.

**Note:**  
In [one of my previous blog posts](https://vogella.com/blog/osgi-bundles-fragments-dependencies/) I wrote about the wrong usage of fragments in various projects. Also about when fragments should be used and when they shouldn't. As I got feedback that I did not mention testing with Tycho, I want to add that with this post. Using a fragment for unit testing is also a valid approach. Compared with the typical Java approach to have the test code located in the same project in a separate source folder, using a fragment is similar and gives the same opportunities. The classes are in the same classpath and therefore share the same visibility (e.g. access to package private or protected methods).

Execute the following steps to create a test fragment for unit testing the `StringInverterImpl` of the [Getting Started Tutorial](getting-started-with-osgi-declarative-services.md):

- Create a new fragment project 
  - _File -> New -> Other -> Plug-in Development -> Fragment Project_
    - Set the name to _org.fipro.inverter.provider.tests_  
      It is important that the name ends with **_.tests_** so we can later use pom-less Tycho for building.
    - Click _Next_
    - Set the host plug-in to _org.fipro.inverter.provider_
- Open the _MANIFEST.MF_ file and switch to the _Dependencies_ section
    - Add _org.junit.jupiter.api_ to the _Imported Packages_
- Create a new package _org.fipro.inverter.provider_
- Create a new JUnit 5 based test class

``` java
package org.fipro.inverter.provider;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.fipro.inverter.StringInverter;
import org.junit.jupiter.api.Test;

public class StringInverterImplTest {

    @Test 
    public void shouldInvertText() { 
        StringInverter inverter = new StringInverterImpl(); 
        assertEquals("nospmiS", inverter.invert("Simpson")); 
    } 
}
```

The test can be executed via _right click -> Run As -> JUnit Test_

_**Note:**_  
It is currently not possible to use the _Automatic Manifest Generation_ PDE project layout for a fragment project. 

### Bndtools vs. PDE

To add a unit test to a _Bnd OSGi Project_, you don't have to create a test fragment. The project layout is similar to a Maven project. Test classes can therefore be placed in the source folder _src/test/java_.

_**Note:**_  
If you have trouble to configure _src/test/java_ as a source folder for test classes, try to open the _.classpath_ file of the project and add the following entry manually:
```
<classpathentry kind="src" output="generated/test-classes" path="src/test/java">
    <attributes>
        <attribute name="test" value="true"/>
    </attributes>
</classpathentry>
```

To use JUnit 5 for writing the test cases, you will need to prepare the workspace and add the necessary dependencies.

- Open the file _cnf/ext/build.mvn_
  - Add the following dependencies
    ```
    org.opentest4j:opentest4j:1.3.0
    org.apiguardian:apiguardian-api:1.1.2
    org.junit.platform:junit-platform-commons:1.11.2
    org.junit.platform:junit-platform-engine:1.11.2
    org.junit.platform:junit-platform-launcher:1.11.2
    org.junit.jupiter:junit-jupiter-api:5.11.2
    org.junit.jupiter:junit-jupiter-engine:5.11.2
    org.junit.jupiter:junit-jupiter-params:5.11.2
    ```
  - In the _Repositories_ view click _Refresh Repositories Tree_
- Open the file _org.fipro.inverter.provider/bnd.bnd_
  - Add the `-testpath` with the necessary dependencies
    ```
    -testpath: \
        org.opentest4j,\
        org.apiguardian.api,\
        junit-jupiter-api,\
        junit-platform-commons,\
        junit-platform-engine,\
        junit-jupiter-engine
      ```
- Add the class `org.fipro.inverter.provider.StringInverterImplTest` to _src/test/java_

Now the unit test can be executed via _right click -> Run As -> JUnit Test_

_**Note:**_  
In a Maven project layout, adding a unit test is as simple as with any other Maven project. You need to add `junit-jupiter` as `test` dependency to the project and then add the test class to _src/test/java_.

## Integration testing / "black-box-testing"

Integration tests or _black-box-tests_ are used to test if our bundle and the provided services behave correctly in an OSGi environment. This is especially necessary if the services to test reference other services or OSGi features are used, like the `EventAdmin` for event processing or the `ConfigurationAdmin` to configure components at runtime. Integration tests are contained in a test bundle, so also the bundle wiring is tested accordingly.

As the test is executed in the JUnit test runtime, we can not simply make use of the service binding mechanisms and injections. In 2016 it was necessary to find and access the service in a programmatical way by using a `ServiceTracker`. Nowadays you can use the [OSGi Testing Support](https://github.com/osgi/osgi-test) framework, which makes the creation of tests for OSGi components much easier.

We will use [org.osgi.test.junit5](https://github.com/osgi/osgi-test/blob/main/org.osgi.test.junit5/README.md) that uses the [JUnit 5 Extension Model](https://junit.org/junit5/docs/snapshot/user-guide/#extensions). This enables the use of OSGi API in test classes similar to the usage in production code.

The OSGi Testing Support bundles are not available via a p2 update site. Currently they are also not included in the Eclipse Orbit Update Site. However it is possible to consume them via Maven Locations in a _Target Platform_. Therefore the first step is to create a _Target Platform_ that contains the _Eclipse Projekt SDK_ for the Equinox OSGi bundles and the JUnit Support and the Maven Location for the `osgi-test` bundles.

- Create the target platform project
    - _Main Menu → File → New → Project → General → Project_
    - Set name to _org.fipro.osgi.target_
    - Click _Finish_
- Create a new target definition
    - _Right click on project → New → Target Definition_
    - Set the filename to _org.fipro.osgi.target.target_
    - Initialize the target definition with: _Nothing: Start with an empty target definition_
    - Click _Finish_
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

    - Alternative B
      - By clicking _Add..._ in the _Locations_ section
        - Select _Software Site_
        - Software Site _https://download.eclipse.org/releases/2024-09_
        - Disable _Group by Category_ and filter for _Eclipse_
        - Select _Eclipse Project SDK_
        - Click _Finish_
      - Click _Add..._ in the _Locations_ section
        - Select _Maven_
        - Add the GAV to org.osgi.test.junit5
          - _Group Id: **org.osgi**_
          - _Artifact Id: **org.osgi.test.junit5**_
          - _Version: **1.3.0**_
          - _Type: **jar**_
        - Set a _Label_: _**org.osgi.test**_
        - _Dependencies depth_: _**Infinite**_
        - Click _Finish_
- Switch to the _Definition_ tab
    - Wait until the Target Definition is completely resolved (check the progress at the bottom right)
    - Activate the target platform by clicking _Set as Target Platform_ in the upper right corner of the Target Definition Editor

Execute the following steps to create a test bundle / plug-in for integration testing of the _org.fipro.inverter.provider_ bundle from the [Getting Started Tutorial](getting-started-with-osgi-declarative-services.md):

- Create a new plug-in project 
  - _File -> New -> Other -> Plug-in Development -> Plug-in Project_
    - Set the name to _org.fipro.inverter.integration.tests_  
      It is important that the name ends with **_.tests_** so we can later use pom-less Tycho for building.
    - Click _Next_
    - Set _Name_ to _Inverter Integration Tests_
    - Select _Execution Environment JavaSE-17_
    - Ensure that _Generate an Activator_ and _This plug-in will make contributions to the UI_ are disabled
    - Click _Finish_
- Open the _MANIFEST.MF_ file and switch to the _Dependencies_ section
    - Add the following entries to the _Imported Packages_
        - _org.fipro.inverter_
        - _org.junit.jupiter.api_
        - _org.junit.jupiter.api.extension_
        - _org.osgi.test.common.annotation_
        - _org.osgi.test.junit5.service_
- Create a new package _org.fipro.inverter.integration.tests_

_**Note:**_  
The _Automatic Manifest Generation_ project layout can not be used for integration test bundles. The bundle is not recognized correctly and the option _Run As -> JUnit Plug-in Test_ is not available. I tried to manually create the _JUnit Plug-in Test_ run configuration. But at some point even the _Run As -> JUnit Test_ option vanishes and there are no tests found in that class anymore.

_**Note:**_  
We could also add _org.fipro.inverter.provider_ to the _Require-Bundle_ section, to make the integration test explicitly dependent on that provider bundle. And surely there are cases where this makes sense. In that case I would suggest to name the test bundle _org.fipro.inverter.**provider**.integration.test_ to make that clear. But the explained approach in this tutorial simulates a real usage example of the service in other bundles, so IMHO that is a real integration test.

- Create a new JUnit 5 based test class that uses `osgi-test`

``` java
package org.fipro.inverter.integration.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.fipro.inverter.StringInverter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.junit5.service.ServiceExtension;

@ExtendWith(ServiceExtension.class)
public class IntegrationTest {

    @Test
    public void shouldInvertWithService(@InjectService StringInverter inverter) {
        assertNotNull(inverter, "No StringInverter service found");
        assertEquals("nospmiS", inverter.invert("Simpson"));
    }
}
```

- Open the _MANIFEST.MF_ file and switch to the _Overview_ section
    - Check _Activate this plug-in when one of its classes is loaded_ which generates the `Bundle-ActivationPolicy: lazy` header in the _MANIFEST.MF_ file. This is necessary so the test bundle is started.

The test can be executed via _right click -> Run As -> JUnit Plug-in Test_

If you are interested in learning more about `osgi-test`, watch the recording of the following talk be Stefan Bischof: [Testing OSGi with OSGi-Test - OCX 2024](https://youtu.be/3YvvXGoOZAs?si=UHj9lka5MCQIEZmO)

### Dealing with implicit dependencies

When executing the integration tests in the IDE via PDE tooling, a launch configuration will be created, that automatically adds all bundles from the workspace and the target platform to the test runtime. This way all necessary bundles are available, even the implicit dependencies.

When I wrote the initial version of this blog post in 2016, executing the tests with Tycho Surefire hat some issues in that area. It was necessary to make implicit dependencies explicit. And it was necessary to configure dependencies to the _Service Component Runtime (SCR)_ and the service provider bundle.

When executing a test bundle/fragment via Tycho Surefire, the OSGi runtime for the test execution consists of the test bundle/fragment and its dependencies. There is no explicit launch configuration. Because of that, the implicit dependencies need to be specified in another way to add them to the test runtime. In general you need to make the implicit dependencies explicit. This can be done in different ways. The most obvious is to add a bundle requirement to the test bundle dependencies. But as explained above, this is more a workaround than a solution. The suggested way in various wiki entries and blog posts is to configure the additional dependencies for the test runtime in the _pom.xml_. More information on that can be found in the following documentations and blogs:

- [Tycho Packaging Types - eclipse-test-plugin](https://wiki.eclipse.org/Tycho/Packaging_Types#eclipse-test-plugin)
- [Tycho FAQ - How to test OSGi declarative services?](https://wiki.eclipse.org/Tycho/FAQ#How_to_test_OSGi_declarative_services.3F)
- [Testing with Surefire](https://wiki.eclipse.org/Tycho/Testing_with_Surefire)
- [Tycho Surefire Plugin](https://tycho.eclipseprojects.io/doc/latest/tycho-surefire-plugin/plugin-info.html)

With the rise of pom-less Tycho builds the usage of explicit _pom.xml_ files for test bundles and test fragments is not needed and wanted anymore. It is of course still possible to specify an explicit _pom.xml_ to add special configurations. But if it is not necessary, it should be avoided to let the pom-less extension derive the necessary build information.

In the given example the integration test bundle has two implicit dependencies:

- The _Service Component Runtime_ that is needed to manage components and their life cycle.
- The service provider bundle that contains the service implementation to test (e.g. `org.fipro.inverter.provider`). As we only specified the package dependency on the service interface, there is no direct dependency on the provider.

Such dependencies can be specified via OSGi capabilities. For the _Service Component Runtime_ you need to specify the _osgi.extender_ capability for _osgi.component_. For the service provider you need to specify the _osgi.service_ capability for the `StringInverter` service interface. The corresponding _Require-Capability_ header that needs to be added to the _MANIFEST.MF_ file looks like the following snippet:

```
Require-Capability: osgi.extender;
  filter:="(&(osgi.extender=osgi.component)(version>=1.3)(!(version>=2.0)))",
 osgi.service;
  filter:="(objectClass=org.fipro.inverter.StringInverter)"
```

Actually the `org.fipro.inverter.provider` bundle already requires the _osgi.extender_ capability. Therefore it is already present in the test runtime. But our integration test bundle needs to require the _osgi.service_ capability in order to make the integration tests work.

- Open the _MANIFEST.MF_ file of the _org.fipro.inverter.integration.test_ project
- Switch to the _MANIFEST.MF_ tab
- Add the following header to the file (remember that there needs to be an empty new line at the end of the file)  
```
Require-Capability: osgi.service;
  filter:="(objectClass=org.fipro.inverter.StringInverter)"
```

_**Note:**_  
As the _MANIFEST.MF_ is not generated for the integration test bundle, the usage of a _package-info.java_ does not work here. For Bndtools the _package-info.java_ is the recommended way to configure the requirements, which is explained in the Bndtools section later.

At last we setup a pom-less Tycho build to proof that everything is working as expected. 

- Create a _.mvn/extensions.xml_ descriptor file in the root of the project directory
```xml
<?xml version="1.0" encoding="UTF-8"?>
<extensions>
  <extension>
    <groupId>org.eclipse.tycho</groupId>
    <artifactId>tycho-build</artifactId>
    <version>4.0.10</version>
  </extension>
</extensions>
```

- Create a parent _pom.xml_ file in the root of the project directory to configure the build.
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project
    xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd"
    xmlns="http://maven.apache.org/POM/4.0.0"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">

    <modelVersion>4.0.0</modelVersion>
    <groupId>org.fipro</groupId>
    <artifactId>org.fipro.parent</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    <packaging>pom</packaging>

    <properties>
        <tycho.version>4.0.10</tycho.version>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <java.version>17</java.version>
    </properties>

    <build>
        <plugins>
            <plugin>
                <groupId>org.eclipse.tycho</groupId>
                <artifactId>tycho-maven-plugin</artifactId>
                <version>${tycho.version}</version>
                <extensions>true</extensions>
            </plugin>

            <plugin>
                <groupId>org.eclipse.tycho</groupId>
                <artifactId>tycho-packaging-plugin</artifactId>
                <version>${tycho.version}</version>
                <configuration>
                    <archive>
                        <addMavenDescriptor>false</addMavenDescriptor>
                    </archive>
                </configuration>
            </plugin>

            <plugin>
                <groupId>org.eclipse.tycho</groupId>
                <artifactId>target-platform-configuration</artifactId>
                <version>${tycho.version}</version>
                <configuration>
                    <target>
                        <artifact>
                            <groupId>org.fipro</groupId>
                            <artifactId>org.fipro.osgi.target</artifactId>
                            <version>${project.version}</version>
                        </artifact>
                    </target>
                    <targetDefinitionIncludeSource>honor</targetDefinitionIncludeSource>
                    <executionEnvironment>JavaSE-17</executionEnvironment>
                    <environments>
                        <environment>
                            <os>win32</os>
                            <ws>win32</ws>
                            <arch>x86_64</arch>
                        </environment>
                        <environment>
                            <os>linux</os>
                            <ws>gtk</ws>
                            <arch>x86_64</arch>
                        </environment>
                        <environment>
                            <os>macosx</os>
                            <ws>cocoa</ws>
                            <arch>x86_64</arch>
                        </environment>
                    </environments>
                </configuration>
            </plugin>
        </plugins>
        <pluginManagement>
            <plugins>
                <!-- 
                    Add this to avoid the warning: 
                    'build.plugins.plugin.version' for org.eclipse.tycho:tycho-p2-director-plugin
                is missing. 
                -->
                <plugin>
                    <groupId>org.eclipse.tycho</groupId>
                    <artifactId>tycho-p2-director-plugin</artifactId>
                    <version>${tycho.version}</version>
                </plugin>
            </plugins>
        </pluginManagement>
    </build>

    <modules>
        <module>org.fipro.osgi.target</module>
        <module>org.fipro.inverter.api</module>
        <module>org.fipro.inverter.command</module>
        <module>org.fipro.inverter.provider</module>
        <module>org.fipro.inverter.provider.tests</module>
        <module>org.fipro.inverter.integration.tests</module>
    </modules>
</project> 
```
 
For further information on setting up a build with Tycho, have a look at the [vogella Tycho Tutorial](http://www.vogella.com/tutorials/EclipseTycho/article.html) or the [Eclipse RCP Cookbook – The Thermomix Recipe (Automated build with Maven Tycho)](https://github.com/fipro78/e4-cookbook-basic-recipe/blob/master/tutorials/Eclipse_RCP_Cookbook_Tycho.md).

After the two files are in place and configured correctly, the build can be startet via

```
mvn clean verify
```

If everything is setup correctly, the build should run the unit test fragment and the integration test bundle, and the build should succeed.


### Bndtools vs. PDE

Bndtools has a similar but slightly different approach than PDE. You need to configure the test runtime in a _.bndrun_ file and do some additional configuration related to the test execution. Some background details can be found in [bnd -Testing](https://bnd.bndtools.org/chapters/310-testing.html).

- Open the file _cnf/ext/build.mvn_
  - Add the following dependencies
    ```
    org.osgi:org.osgi.test.common:1.3.0
    org.osgi:org.osgi.test.junit5:1.3.0
    ```
- Create a new _Bnd OSGi Project_
- Configure _src/test/java_ as a source folder for test classes
- Edit the _bnd.bnd_ file and configure the dependencies
  ```
  Test-Cases: ${classes;HIERARCHY_INDIRECTLY_ANNOTATED;org.junit.platform.commons.annotation.Testable;CONCRETE}

  -dependson: \
    org.fipro.inverter.provider

  -buildpath:  \
    osgi.annotation,\
    org.osgi.namespace.service,\
    org.osgi.service.component.annotations,\
    org.fipro.inverter.api;version=latest,\
    org.opentest4j,\
    org.apiguardian.api,\
    junit-jupiter-api,\
    junit-jupiter-engine,\
    junit-jupiter-params,\
    junit-platform-commons,\
    junit-platform-engine,\
    junit-platform-launcher,\
    org.osgi.test.common,\
    org.osgi.test.junit5
  ```
- Create a test class
  ```java
  package org.fipro.inverter.integration.tests;

  import static org.junit.jupiter.api.Assertions.assertEquals;
  import static org.junit.jupiter.api.Assertions.assertNotNull;

  import org.fipro.inverter.StringInverter;
  import org.junit.jupiter.api.Test;
  import org.junit.jupiter.api.extension.ExtendWith;
  import org.osgi.test.common.annotation.InjectService;
  import org.osgi.test.junit5.service.ServiceExtension;

  @ExtendWith(ServiceExtension.class)
  public class IntegrationTest {

    @Test
    public void shouldInvertWithService(@InjectService StringInverter inverter) {
        assertNotNull(inverter, "No StringInverter service found");
        assertEquals("nospmiS", inverter.invert("Simpson"));
    }
  }
  ```
- Create a _package-info.java_ in the package where the test class resides  
  This way we configure the requirement on a service implementation of type `org.fipro.inverter.StringInverter`, which is necessary to tell the resolver that the service provider implementation is needded for the test runtime.
  ```java
  // We require a StringInverter service to test
  @Requirement(
      namespace = ServiceNamespace.SERVICE_NAMESPACE, 
      filter = "(" + ServiceNamespace.CAPABILITY_OBJECTCLASS_ATTRIBUTE + "=org.fipro.inverter.StringInverter)")
  package org.fipro.inverter.integration.tests;

  import org.osgi.annotation.bundle.Requirement;
  import org.osgi.namespace.service.ServiceNamespace;
  ```
- Create a _test.bndrun_ test launch configuration  
  Via the `-tester` header we configure the tester bundle for JUnit 5.  
  Via `-runrequires` we specify the requirement on this test bundle and its dependencies.
  ```
  -tester: biz.aQute.tester.junit-platform 

  -runfw: org.apache.felix.framework;version='[7.0.5,7.0.5]'
  -runee: JavaSE-17
  -resolve.effective: active

  -runrequires: \
      bnd.identity;id='${basename;${.}}'
  ``` 

To run the integration test from within the IDE, you first need to manually resolve the `-runbundles` by clicking on _Resolve_ in the _test.bndrun_ editor. If you now right click on the `IntegrationTest` class and select _Run As -> Bnd OSGi Test Launcher (JUnit)_ you will see an exception like
```
org.osgi.framework.BundleException: Unable to resolve biz.aQute.tester [2](R 2.0): missing requirement [biz.aQute.tester [2](R 2.0)] osgi.wiring.package; (&(osgi.wiring.package=junit.framework)(version>=3.8.0)(!(version>=5.0.0))) Unresolved requirements: [[biz.aQute.tester [2](R 2.0)] osgi.wiring.package; (&(osgi.wiring.package=junit.framework)(version>=3.8.0)(!(version>=5.0.0)))]
    at org.apache.felix.framework.Felix.resolveBundleRevision(Felix.java:4398)
```

This can be solved by modifing the _Run Configuration_
- Open _Run -> Run Configurations..._
- In the tree view select _OSGi Framework JUnit Tests -> org.fipro.inverter.integration.tests_
- On the tab _OSGi Tests_ click _Browse Run Files_
- Select the _test.bndrun_ in the _org.fipro.inverter.integration.tests_ project
- Click _Run_

Now it should be possible to launch the integration test within the IDE.

If the integration test should be executed in the Gradle build that comes with the Bndtools project wizards, you also need to create/modify the _build.gradle_ file:
```gradle
def resolveTask = tasks.named("resolve.test") {
    outputBndrun = layout.buildDirectory.file("test.bndrun")
}

tasks.named("testOSGi") {
    bndrun = resolveTask.flatMap { it.outputBndrun }
}
```

#### Bndtools with Maven

With a Bndtools Maven project setup, you can use the [`bnd-testing-maven-plugin`](https://github.com/bndtools/bnd/blob/master/maven-plugins/bnd-testing-maven-plugin/README.md). It basically creates a test application jar that defines the runtime and the test cases, and executes the test in the build process.

Perform the following steps to enable the execution of OSGi integration tests:

- Add the necessary dependencies to the parent _pom.xml_ file
  ```xml
    <!-- OSGi Namespace Service -->
    <dependency>
        <groupId>org.osgi</groupId>
        <artifactId>org.osgi.namespace.service</artifactId>
        <version>1.0.0</version>
        <scope>provided</scope>
    </dependency>

    <!-- Test Dependencies -->
    <dependency>
        <groupId>org.junit</groupId>
        <artifactId>junit-bom</artifactId>
        <version>5.11.2</version>
        <type>pom</type>
        <scope>import</scope>
    </dependency>
    <dependency>
        <groupId>org.osgi</groupId>
        <artifactId>org.osgi.test.bom</artifactId>
        <version>1.3.0</version>
        <type>pom</type>
        <scope>import</scope>
    </dependency>
  ```
- Add additional configurations to the `bnd-maven-plugin`, `bnd-resolve-maven-plugin` and the `bnd-testing-maven-plugin`
  ```xml
    <plugin>
        <groupId>biz.aQute.bnd</groupId>
        <artifactId>bnd-maven-plugin</artifactId>
        <version>${bnd.version}</version>
        <extensions>true</extensions>
        ...
    </plugin>
    <plugin>
        <groupId>biz.aQute.bnd</groupId>
        <artifactId>bnd-resolver-maven-plugin</artifactId>
        <version>${bnd.version}</version>
        <configuration>
            <failOnChanges>false</failOnChanges>
            <bndruns></bndruns>
        </configuration>
        <executions>
            <!-- Integration Test Configuration -->
            <execution>
                <id>resolve-test</id>
                <phase>pre-integration-test</phase>
                <goals>
                    <goal>resolve</goal>
                </goals>
                <configuration>
                    <outputBndrunDir>${project.build.directory}</outputBndrunDir>
                    <bndruns>
                        <include>test.bndrun</include>
                    </bndruns>
                    <failOnChanges>false</failOnChanges>
                    <includeDependencyManagement>true</includeDependencyManagement>
                    <reportOptional>false</reportOptional>
                    <scopes>
                        <scope>compile</scope>
                        <scope>runtime</scope>
                        <scope>test</scope>
                    </scopes>
                </configuration>
            </execution>
        </executions>
    </plugin>
    <plugin>
        <groupId>biz.aQute.bnd</groupId>
        <artifactId>bnd-testing-maven-plugin</artifactId>
        <version>${bnd.version}</version>
        <executions>
            <!-- OSGi integration tests execution -->
            <execution>
                <goals>
                    <goal>testing</goal>
                </goals>
                <configuration>
                    <bndrunDir>${project.build.directory}</bndrunDir>
                    <bndruns>
                        <include>test.bndrun</include>
                    </bndruns>
                    <failOnChanges>false</failOnChanges>
                    <includeDependencyManagement>true</includeDependencyManagement>
                    <resolve>false</resolve>
                    <scopes>
                        <scope>compile</scope>
                        <scope>runtime</scope>
                        <scope>test</scope>
                    </scopes>
                </configuration>
            </execution>
        </executions>
    </plugin>
    ```
- Create a new Maven submodule for the integration test and configure the _pom.xml_ as follows
    - Define the dependencies of the test runtime
    - Disable the `maven-surefire-plugin`
    - Configure the `bnd-maven-plugin` to create a test jar
    - Enable the `bnd-resolver-maven-plugin` and the `bnd-testing-maven-plugin`
    ```xml
    <project xmlns="http://maven.apache.org/POM/4.0.0"
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
        <modelVersion>4.0.0</modelVersion>
        <parent>
            <groupId>org.fipro.osgi.ds</groupId>
            <artifactId>getting-started</artifactId>
            <version>1.0.0-SNAPSHOT</version>
        </parent>

        <artifactId>org.fipro.inverter.integration.tests</artifactId>
        <name>Inverter Integration Tests</name>

        <dependencies>
            <dependency>
                <groupId>org.osgi</groupId>
                <artifactId>osgi.core</artifactId>
            </dependency>
            <dependency>
                <groupId>org.osgi</groupId>
                <artifactId>osgi.annotation</artifactId>
                <scope>provided</scope>
            </dependency>
            <dependency>
                <groupId>org.osgi</groupId>
                <artifactId>org.osgi.namespace.service</artifactId>
            </dependency>

            <dependency>
                <groupId>org.junit.jupiter</groupId>
                <artifactId>junit-jupiter</artifactId>
                <scope>test</scope>
            </dependency>

            <dependency>
                <groupId>org.osgi</groupId>
                <artifactId>org.osgi.test.common</artifactId>
            </dependency>
            <dependency>
                <groupId>org.osgi</groupId>
                <artifactId>org.osgi.test.junit5</artifactId>
            </dependency>

            <dependency>
                <groupId>org.fipro.osgi.ds</groupId>
                <artifactId>org.fipro.inverter.api</artifactId>
                <version>${project.version}</version>
            </dependency>
            <dependency>
                <groupId>org.fipro.osgi.ds</groupId>
                <artifactId>org.fipro.inverter.provider</artifactId>
                <version>${project.version}</version>
            </dependency>
        </dependencies>

        <build>
            <plugins>
                <plugin>
                    <groupId>org.apache.maven.plugins</groupId>
                    <artifactId>maven-surefire-plugin</artifactId>
                    <!-- We let bnd-testing-maven-plugin do all the testing -->
                    <configuration>
                        <skipTests>true</skipTests>
                    </configuration>
                </plugin>
                <plugin>
                    <groupId>biz.aQute.bnd</groupId>
                    <artifactId>bnd-maven-plugin</artifactId>
                    <executions>
                        <execution>
                            <id>test-jar</id>
                            <goals>
                                <goal>test-jar</goal>
                            </goals>
                            <configuration>
                                <bnd><![CDATA[
                                -noextraheaders: true
                                -noimportjava: true
                                ]]></bnd>
                                <testCases>junit5</testCases>
                            </configuration>
                        </execution>
                    </executions>
                </plugin>
                <plugin>
                    <groupId>biz.aQute.bnd</groupId>
                    <artifactId>bnd-resolver-maven-plugin</artifactId>
                </plugin>
                <plugin>
                    <groupId>biz.aQute.bnd</groupId>
                    <artifactId>bnd-testing-maven-plugin</artifactId>
                </plugin>
            </plugins>
        </build>
    </project>
    ```
- Create a test class
  ```java
  package org.fipro.inverter.integration.tests;

  import static org.junit.jupiter.api.Assertions.assertEquals;
  import static org.junit.jupiter.api.Assertions.assertNotNull;

  import org.fipro.inverter.StringInverter;
  import org.junit.jupiter.api.Test;
  import org.junit.jupiter.api.extension.ExtendWith;
  import org.osgi.test.common.annotation.InjectService;
  import org.osgi.test.junit5.service.ServiceExtension;

  @ExtendWith(ServiceExtension.class)
  public class IntegrationTest {

    @Test
    public void shouldInvertWithService(@InjectService StringInverter inverter) {
        assertNotNull(inverter, "No StringInverter service found");
        assertEquals("nospmiS", inverter.invert("Simpson"));
    }
  }
  ```
- Create a _package-info.java_ in the package where the test class resides  
  This way we configure the requirement on a service implementation of type `org.fipro.inverter.StringInverter`, which is necessary to tell the resolver that the service provider implementation is needded for the test runtime.
  ```java
  // We require a StringInverter service to test
  @Requirement(
      namespace = ServiceNamespace.SERVICE_NAMESPACE, 
      filter = "(" + ServiceNamespace.CAPABILITY_OBJECTCLASS_ATTRIBUTE + "=org.fipro.inverter.StringInverter)")
  package org.fipro.inverter.integration.tests;

  import org.osgi.annotation.bundle.Requirement;
  import org.osgi.namespace.service.ServiceNamespace;
  ```
- Create a _test.bndrun_ test launch configuration  
  Via the `-tester` header we configure the tester bundle for JUnit 5.  
  Via `-runrequires` we specify the requirement on the generated test jar, and let the `bnd-resolver-maven-plugin` calculate the `-runbundles`
  ```
  -tester: biz.aQute.tester.junit-platform

  -runfw: org.eclipse.osgi
  -resolve.effective: active

  -runrequires: \
    bnd.identity;id='${project.artifactId}-tests'
  ```

Now you can execute the build via 

```
mvn clean verify
```

If everything is setup correctly, the build should run and execute the unit test and the integration test, and the build should succeed.

The sources of the Getting Started Tutorial are hosted on GitHub:

- [OSGi DS Getting Started (PDE)](https://github.com/fipro78/osgi-ds-getting-started-pde)  
  This repository contains the sources in PDE project layout.
- [OSGi DS Getting Started (Bndtools)](https://github.com/fipro78/osgi-ds-getting-started-bndtools)  
  This repository contains the sources in Bndtools project layout using a Bndtools workspace.
- [OSGi DS Gettings Started (Bnd with Maven)](https://github.com/fipro78/osgi-ds-getting-started-bnd-maven)  
  This repository contains the sources in a Maven project layout that uses the bnd Maven plugins.

They are updated for the contents of this _OSGi Component Testing Tutorial_.