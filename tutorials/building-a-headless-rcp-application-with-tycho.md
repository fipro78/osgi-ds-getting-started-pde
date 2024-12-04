# Building a headless RCP application with Tycho

Recently I got the request to create a "headless RCP" application from an existing Eclipse project. I was reading several posts on that and saw that a lot of people using the term "headless RCP". First of all I have to say that "headless RCP" is a contradiction in itself. RCP means _Rich Client Platform_. And a rich client is typically characterized by having a graphical user interface. A headless application means to have an application with a command line interface. So the characteristic here is to have no graphical user interface. When people are talking about a "headless RCP" application, they mean to create a command line application based on code that is created for a RCP application, but without the GUI. And that actually means they want to create an OSGi application based on Equinox.

For such a scenario I typically would recommend to use [bndtools](https://bndtools.org/) or at least plain Java with the [bnd Maven plugins](https://github.com/bndtools/bnd/tree/master/maven-plugins). But there are scenarios where this is not possible, e.g. if your whole project is an Eclipse RCP project which currently forces you to use PDE tooling, and you only want to extract some parts/services to a command line tool. Well, one could also suggest to separate those parts to a separate workspace where bndtools is used and consume those parts in the RCP workspace. But that increases the complexity in the development environment, as you need to deal with two different toolings for one project.

In this blog post I will explain how to create a headless product out of an Eclipse RCP project (PDE based) and how to build it automatically with Tycho. And I will also show a nice benefit provided by the bnd Maven plugins on top of it.

Let's start with the basics. A headless application provides functionality via command line. In an OSGi application that means to have some services that can be triggered on the command line. If your functionality is based on Eclipse Extension Points, I suggest to convert them to OSGi Declarative Services. This has several benefits, one of them is that the creation of a headless application is much easier. That said this tutorial is based on using OSGi Declarative Services. If you are not yet familiar with that, give my [Getting Started with OSGi Declarative Services](getting-started-with-osgi-declarative-services.md) a try. I will use the basic bundles from the PDE variant for the headless product here.

## Product Definition

For the automated product build with Tycho we need a product definition. Of course with some special configuration parameters as we actually do not have a product in Eclipse RCP terms.

- Create the product project
    - Main Menu -> File -> New -> Project -> General -> Project
    - Set name to _org.fipro.headless.product_
    - Ensure that the project is created in the same location as the other projects.
    - Click _Finish_
- Create a new product configuration
    - Right click on project -> New -> Product Configuration
    - Set the filename to _org.fipro.headless.product_
    - Select _Create configuration file with basic settings_
    - Click _Finish_
- Configure the product
    - _Overview_ tab  
        - ID = org.fipro.headless
        - Version = 1.0.0.qualifier
        - Uncheck _The product includes native launcher artifacts_
        - Leave _Product_ and _Application_ empty  
Product and Application are used in RCP products, and therefore not needed for a headless OSGi command line application.
    - This product configuration is based on: _plug-ins_  
_**Note:**_  
You can also create a product configuration that is based on features. For simplicity we use the simple plug-ins variant.
    - _Contents_ tab
        - Add the following bundles/plug-ins:
        - Custom functionality
            - _org.fipro.inverter.api_
            - _org.fipro.inverter.command_
            - _org.fipro.inverter.provider_
        - OSGi console
            - _org.apache.felix.gogo.command_
            - _org.apache.felix.gogo.runtime_
            - _org.apache.felix.gogo.shell_
            - _org.eclipse.equinox.console_
        - Equinox OSGi Framework with Felix SCR for Declarative Services support
            - _org.eclipse.osgi_
            - _org.apache.felix.scr_
            - _org.osgi.service.component_
            - _org.osgi.util.function_
            - _org.osgi.util.promise_
    - _Configuration_ tab
        - _Start Levels_
            - `org.apache.felix.scr`, StartLevel = 0, Auto-Start = true  
This is necessary because Equinox has the policy to not automatically activate any bundle. Bundles are only activated if a class is directly requested from it. But the Service Component Runtime is never required directly, so without that setting, `org.apache.felix.scr` will never get activated.
        - _Properties_
            - `eclipse.ignoreApp = true`  
Tells Equinox to to skip trying to start an Eclipse application.
            - `osgi.noShutdown = true`  
The OSGi framework will not be shut down after the Eclipse application has ended. You can find further information about these properties in the [Equinox Framework QuickStart Guide](https://www.eclipse.org/equinox/documents/quickstart-framework.php) and the [Eclipse Platform Help](https://help.eclipse.org/2019-12/index.jsp?topic=%2Forg.eclipse.platform.doc.isv%2Freference%2Fmisc%2Fruntime-options.html).

_**Note:**_  
If you want to launch the application from within the IDE via the _Overview tab -> Launch an Eclipse application_, you need to provide the parameters as launching arguments instead of configuration properties. 
But running a command line application from within the IDE doesn't make much sense. Either you need to pass the same command line parameter to process, or activate the OSGi console to be able to interact with the application. 
This should not be part of the final build result. But to verify the setup in advance you can add the following to the _Launching_ tab:

- Program Arguments
    - `-console`
- VM Arguments
    - `-Declipse.ignoreApp=true -Dosgi.noShutdown=true`  
When adding the parameters in the _Launching_ tab instead of the _Configuration_ tab, the configurations are added to the _eclipse.ini_ in the root folder, not to the _config.ini_ in the _configuration_ folder. 
When starting the application via jar, the _eclipse.ini_ in the root folder is not inspected.

## Tycho build

To build the product with Tycho, you don't need any specific configuration. You simply build it by using the `tycho-p2-repository-plugin` and `the tycho-p2-director-plugin`, like you do with an Eclipse product. 
This is for example explained [here](https://wiki.eclipse.org/Tycho/eclipse-repository).

Create a _pom.xml_ file in _org.fipro.headless.app_:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">

  <modelVersion>4.0.0</modelVersion>
  <parent>
    <groupId>org.fipro</groupId>
    <artifactId>org.fipro.parent</artifactId>
    <version>1.0.0-SNAPSHOT</version>
  </parent>

  <groupId>org.fipro</groupId>
  <artifactId>org.fipro.headless</artifactId>
  <packaging>eclipse-repository</packaging>
  <version>1.0.0-SNAPSHOT</version>

  <build>
    <plugins>
      <plugin>
        <groupId>org.eclipse.tycho</groupId>
        <artifactId>tycho-p2-repository-plugin</artifactId>
        <version>${tycho.version}</version>
        <configuration>
          <includeAllDependencies>true</includeAllDependencies>
        </configuration>
      </plugin>
      <plugin>
        <groupId>org.eclipse.tycho</groupId>
        <artifactId>tycho-p2-director-plugin</artifactId>
        <version>${tycho.version}</version>
        <executions>
          <execution>
            <id>materialize-products</id>
            <goals>
              <goal>materialize-products</goal>
            </goals>
          </execution>
          <execution>
            <id>archive-products</id>
            <goals>
              <goal>archive-products</goal>
            </goals>
          </execution>
        </executions>
      </plugin>
    </plugins>
  </build>
</project>
```

For more information about building with Tycho, have a look at the [vogella Tycho tutorial](https://www.vogella.com/tutorials/EclipseTycho/article.html).

Running the build via `mvn clean verify` should create the resulting product in the folder _org.fipro.headless/target/products_. 
The archive file _org.fipro.headless-1.0.0-SNAPSHOT.zip_ contains the product artifacts and the p2 related artifacts created by the build process. 
For the headless application only the folders _configuration_ and _plugins_ are relevant, where _configuration_ contains the _config.ini_ with the necessary configuration attributes, 
and in the _plugins_ folder you find all bundles that are part of the product.

Since we did not add a native launcher, the application can be started with the java command. 
Additionally we need to open the OSGi console, as we have no starter yet. 
From the parent folder above _configuration_ and _plugins_ execute the following command to start the application with a console (update the filename of _org.eclipse.osgi_ bundle as this changes between Eclipse versions):

```console
java -jar plugins/org.eclipse.osgi_3.21.0.v20240717-2103.jar -configuration ./configuration -console
```

The `-configuration` parameter tells the framework where it should look for the _config.ini_, the `-console` parameter opens the OSGi console.

You can now interact with the OSGi console and even start the "invert" command implemented in the [Getting Started tutorial](getting-started-with-osgi-declarative-services.md).

## Native launcher

While the variant without a native launcher is better exchangeable between operating systems, it is not very comfortable to start from a users perspective. 
Of course you can also add a batch file for simplification, but Equinox also provides native launchers. So we will add native launchers to our product. 
This is fairly easy because you only need to check _The product includes native launcher artifacts_ on the _Overview_ tab of the product file and execute the build again.

The resulting product now also contains the following files:

- _eclipse.exe_  
Eclipse executable.
- _eclipse.ini_  
Configuration pointing to the launcher artifacts.
- _eclipsec.exe_  
Console optimized executable.
- _org.eclipse.equinox.launcher_ artifacts in the plugins directory  
Native launcher artifacts.

You can find some more information on those files in the [FAQ](https://wiki.eclipse.org/FAQ_How_do_I_run_Eclipse%3F).

To start the application you can use the added executables:

```console
eclipse.exe -console
```

or

```console
eclipsec.exe -console
```

The main difference in first place is that _eclipse.exe_ operates in a new shell, while _eclipsec.exe_ stays in the same shell when opening the OSGi console. 
The FAQ says "On Windows, the eclipsec.exe console executable can be used for improved command line behavior.".

_**Note:**_  
You can change the name of the _eclipse.exe_ file in the product configuration on the _Launching_ tab by setting a _Launcher Name_. But this will not affect the _eclipsec.exe_.

## Command line parameter

Starting a command line tool with an interactive OSGi console is typically not what people want. This is nice for debugging purposes, but not for productive use. 
In productive use you usually want to use some parameters on the command line and then process the inputs. 
In plain Java you take the arguments from the `main()` method and process them. But in an OSGi application you do not write a `main()` method. 
The framework launcher has the `main()` method. To start your application directly you therefore need to create some kind of starter that can inspect the launch arguments.

With OSGi Declarative Services the starter is an _Immediate Component_. That is a component that gets activated directly once all references are satisfied. 
To be able to inspect the command line parameters in an OSGi application, you need to know how the launcher that started it provides this information. 
The Equinox launcher for example provides this information via `org.eclipse.osgi.service.environment.EnvironmentInfo` which is provided as a service. 
That means you can add a `@Reference` for `EnvironmentInfo` in your declarative service, and once it is available the immediate component gets activated and the application starts.

### Create new project _org.fipro.headless.app_

- Create the app project
    - _Main Menu → File → New → Plug-in Project_
    - Set name to _org.fipro.headless.app_
    - In the _Target Platform_ section select
        - This plug-in is targeted to run with: __*an OSGi framework:*__
        - Select __*standard*__ in the combobox
        - Check __*Generate OSGi metadata automatically*__  
    - Click _Next_
    - Set _Name_ to _Headless App_
    - Select _Execution Environment JavaSE-17_
    - Ensure that _Generate an Activator_ and _This plug-in will make contributions to the UI_ are disabled
    - Click _Finish_
    - If you do not see the tabs at the bottom of the recently opened editor with name _org.fipro.headless.app_, close the editor and open the  _pde.bnd_ file in the project _org.fipro.headless.app_.
        - Switch to the _pde.bnd_ tab
            - Add the `Bundle-ActivationPolicy` to get the bundle automatically started in an Equinox runtime
            - Add the `-runee` instruction to create the requirement on Java 17
            - Add the `-buildpath` instruction to specify the dependency to
              - `org.fipro.inverter.api` to consume the services to use
              - `org.eclipse.osgi` to be able to consume the Equinox `org.eclipse.osgi.service.environment.EnvironmentInfo`.
            ```
            Bundle-Name: Headless App
            Bundle-SymbolicName: org.fipro.headless.app
            Bundle-Vendor: 
            Bundle-Version: 1.0.0.qualifier
            Bundle-ActivationPolicy: lazy
            -runee: JavaSE-17
            -buildpath: \
                org.fipro.inverter.api,\
                org.eclipse.osgi.service.environment
            ```

  - Create an immediate component with the name `EquinoxStarter`.

  ```java
  @Component(immediate = true)
  public class EquinoxStarter {

      @Reference
      EnvironmentInfo environmentInfo;

      @Reference
      StringInverter inverter;

      @Activate
      void activate() {
          for (String arg : this.environmentInfo.getNonFrameworkArgs()) {
              System.out.println(inverter.invert(arg));
          }
      }
  }
  ```
- Add _org.fipro.headless.app_ to the _Contents_ of the product definition.
- Add _org.fipro.headless.app_ to the `modules` section of the _pom.xml_.
- Run the build and start the resulting application as before.

With the simple version above you will notice some issues if you are not specifying the `-console` parameter:

1. If you start the application via eclipse.exe with an additional parameter, the code will be executed, but you will not see any output.
2. If you start the application via eclipsec.exe with an additional parameter, you will see an output but the application will not finish.
   
If you pass the `-console` parameter, the output will be seen in both cases and the OSGi console opens immediately afterwards.

First let's have a look why the application seem to hang when started via _eclipsec.exe_. 
The reason is simply that we configured `osgi.noShutdown=true`, which means the OSGi framework will not be shut down after the Eclipse application has ended. 
So the simple solution would be to specify `osgi.noShutdown=false`. 
The downside is that now using the `-console` parameter will not keep the OSGi console open, but close the application immediately. 
Also using _eclipse.exe_ with the `-console` parameter will not keep the OSGi console open. 
So the configuration parameter `osgi.noShutdown` should be set dependent on whether an interactive mode via OSGi console should be supported or not.

If both variants should be supported `osgi.noShutdown` should be set to `true` and a check for the `-console` parameter in code needs to be added. 
If that parameter is not set, close the application via `System.exit(0);`.

`-console` is an Equinox framework parameter, so the check and the handling looks like this:

```java
boolean isInteractive = Arrays
    .stream(environmentInfo.getFrameworkArgs())
    .anyMatch(arg -> "-console".equals(arg));

if (!isInteractive) {
    System.exit(0);
}
```

With the additional handling above, the application will stay open with an active OSGi console if `-console` is set, and it will close immediately if `-console` is not set.

The other issue we faced was that we did not see any output when using _eclipse.exe_. The reason is that the outputs are not sent to the executing command shell. And without specifying an additional parameter, the used command shell is not even opened. One option to handle this is to open the command shell and keep it open as long as a user input closes it again. The framework parameter is `-consoleLog`. And the check could be as simple as the following for example:

```java
boolean showConsoleLog = Arrays
    .stream(environmentInfo.getFrameworkArgs())
    .anyMatch(arg -> "-consoleLog".equals(arg));

if (showConsoleLog) {
    System.out.println();
    System.out.println("***** Press Enter to exit *****");
    // just wait for a Enter
    try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
        reader.readLine();
    } catch (IOException e) {
        e.printStackTrace();
    }
}
```

With the `-consoleLog` handling, the following call will open a new shell that shows the result and waits for the user to press ENTER to close the shell and finish the application.

```console
eclipse.exe test -consoleLog
```

## bnd export

Although these results are already pretty nice, it can be even better. 
With bnd you are able to create a single executable jar that starts the OSGi application. 
This makes it easier to distribute the command line application. 
And the call of the application is similar easy compared with the native executable, while there is no native stuff inside and therefore it is easy exchangeable between operating systems.

Using the [bnd-export-maven-plugin](https://github.com/bndtools/bnd/tree/master/maven-plugins/bnd-export-maven-plugin) you can achieve the same result even with a PDE-Tycho based build. 
But of course you need to prepare things to make it work.

The first thing to know is that the [bnd-export-maven-plugin](https://github.com/bndtools/bnd/tree/master/maven-plugins/bnd-export-maven-plugin) needs a [bndrun](https://bnd.bndtools.org/chapters/300-launching.html) file as input. 
So now create a file _headless.bndrun_ in _org.fipro.headless.product_ project that looks similar to this:

```
-runee: JavaSE-17
-runfw: org.eclipse.osgi
-runsystemcapabilities: ${native_capability}

-resolve.effective: active;skip:="osgi.service"

-runrequires: \
  osgi.identity;filter:='(osgi.identity=org.fipro.headless.app)'

-runbundles: \
  org.fipro.inverter.api,\
  org.fipro.inverter.command,\
  org.fipro.inverter.provider,\
  org.fipro.headless.app,\
  org.eclipse.equinox.console,\
  org.apache.felix.gogo.runtime,\
  org.apache.felix.gogo.shell,\
  org.apache.felix.gogo.command,\
  org.apache.felix.scr,\
  org.osgi.service.component,\
  org.osgi.util.function,\
  org.osgi.util.promise

-runproperties:	\
  osgi.console=
```

- As we want our Eclipse Equinox based application to be bundled as a single executable jar, we specify Equinox as our OSGi framework via `-runfw: org.eclipse.osgi`.
- Via `-runbundles` we specify the bundles that should be added to the runtime.
- The settings below `-runproperties` are needed to handle the Equinox OSGi console correctly.

Unfortunately there is no automatic way to transform a PDE product definition to a _bndrun_ file, at least I am not aware of it. 
And yes there is some duplication involved here, but compared to the result it is acceptable IMHO. 
Anyhow, with some experience in scripting it should be easy to automatically create the _bndrun_ file out of the product definition at build time.

Now enable the [bnd-export-maven-plugin](https://github.com/bndtools/bnd/tree/master/maven-plugins/bnd-export-maven-plugin) for the product build in the _pom.xml_ of _org.fipro.headless.product_. 
Note that even with a pomless build it is possible to specify a specific _pom.xml_ in a project if something additionally to the default build is needed (which is the case here).
If you face the error `Default handler for Launcher-Plugin not found in biz.aQute.launcher`, which happended to me after I updated to version 7.0.0, you need to add the dependency to `biz.aQute.bnd:biz.aQute.launcher`.

```xml
<properties>
  <bnd.version>7.0.0</bnd.version>
</properties>

<dependencies>
  <dependency>
    <groupId>biz.aQute.bnd</groupId>
    <artifactId>biz.aQute.launcher</artifactId>
    <version>${bnd.version}</version>
  </dependency>
</dependencies>

<plugin>
  <groupId>biz.aQute.bnd</groupId>
  <artifactId>bnd-export-maven-plugin</artifactId>
  <version>${bnd.version}</version>
  <configuration>
    <failOnChanges>false</failOnChanges>
    <bndruns>
      <bndrun>headless.bndrun</bndrun>
    </bndruns>
    <bundles>
      <include>${project.build.directory}/repository/plugins/*</include>
    </bundles>
  </configuration>
  <executions>
    <execution>
      <goals>
        <goal>export</goal>
      </goals>
    </execution>
  </executions>
</plugin>
```

The `bndruns` configuration property points to the _headless.bndrun_ we created before. 
In the `bundles` configuration property we point to the build result of the `tycho-p2-repository-plugin` to build up the implicit repository. 
This way we are sure that all required bundles are available without the need to specify any additional repository.

After a new build you will find the file _headless.jar_ in _org.fipro.headless.product/target_. You can start the command line application via

```console
java -jar headless.jar
```

You will notice that the OSGi console is started, anyhow which parameters are added to the command line. 
And all the command line parameters are not evaluated, because not the Equinox launcher started the application. 
Instead the bnd launcher started it. 
Therefore the `EnvironmentInfo` is not initialized correctly.

Unfortunately Equinox will anyhow publish the `EnvironmentInfo` as a service even if it is not initialized. 
Therefore the `EquinoxStarter` will be satisfied and activated. But we will get a `NullPointerException` (that is silently catched) when it is tried to access the framework and/or non-framework args. 
For good coding standards the `EquinoxStarter` needs to check if `EnvironmentInfo` is correctly initialized, otherwise it should do nothing. The code could look similar to this snippet:

```java
@Component(immediate = true)
public class EquinoxStarter {

  @Reference
  EnvironmentInfo environmentInfo;

  @Reference
  StringInverter inverter;

  @Activate
  void activate() {
    if (environmentInfo.getFrameworkArgs() != null
      && environmentInfo.getNonFrameworkArgs() != null) {

      // check if -console was provided as argument
      boolean isInteractive = Arrays
        .stream(environmentInfo.getFrameworkArgs())
        .anyMatch(arg -> "-console".equals(arg));
      // check if -console was provided as argument
      boolean showConsoleLog = Arrays
        .stream(environmentInfo.getFrameworkArgs())
        .anyMatch(arg -> "-consoleLog".equals(arg));

      for (String arg : this.environmentInfo.getNonFrameworkArgs()) {
        System.out.println(inverter.invert(arg));
      }

      // If the -consoleLog parameter is used, a separate shell is opened. 
      // To avoid that it is closed immediately a simple input is requested to
      // close, so a user can inspect the outputs.
      if (showConsoleLog) {
        System.out.println();
        System.out.println("***** Press Enter to exit *****");
        // just wait for a Enter
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
          reader.readLine();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }

      if (!isInteractive) {
        // shutdown the application if no console was opened
        // only needed if osgi.noShutdown=true is configured
        System.exit(0);
      }
    }
  }
}
```

This way we avoid that the `EquinoxStarter` is executing any code. So despite component instance creation and destruction, nothing happens.

To handle launching via bnd launcher, we need another starter. We create a new immediate component named `BndStarter`.

```java
@Component(immediate = true)
public class BndStarter {
    ...
}
```

The bnd launcher provides the command line parameters in a different way. 
Instead of `EnvironmentInfo` you need to get the `aQute.launcher.Launcher` injected with its service properties. 
Inside the service properties map, there is an entry for `launcher.arguments` whose value is a `String[]`. 
To avoid the dependency to aQute classes in our code, we reference `Object` and use a target filter for `launcher.arguments` which works fine as `Launcher` is published also as `Object` to the `ServiceRegistry`.

```java
String[] launcherArgs;

@Reference(target = "(launcher.arguments=*)")
void setLauncherArguments(Object object, Map<String, Object> map) {
    this.launcherArgs = (String[]) map.get("launcher.arguments");
}
```

This will create another issue, as the above reference adds the following requirement:
```
Require-Capability: osgi.service;
    filter:="(objectClass=java.lang.Object)";effective:=active
```

And this can't be resolved. To solve this issue there are two possible strategies:
- Using a _.bndrun_ launch configuration, you can skip the resolution of `osgi.service` via the following instruction  
```
-resolve.effective: active;skip:="osgi.service"
```
- You can also avoid the generation of the Require-Capability header for `osgi.service` by adding the following [`dsannotations-options`](https://bnd.bndtools.org/releases/7.0.0/instructions/dsannotations-options.html) instruction to the _pde.bnd_ file  
```
-dsannotations-options: norequirements
```


Although not necessary, we add some code to align the behavior when started via bnd launcher with the behavior when started with the Equinox launcher. 
That means we check for the `-console` parameter and stop the application if that parameter is missing. 
The check for `-consoleLog` would also not be needed, as the bnd launcher stays in the same command shell like _eclipsec.exe_, but for processing we also remove it. 
Just in case someone tries it out.

The complete code of `BndStarter` would then look like this:

```java
@Component(immediate = true)
public class BndStarter {

  String[] launcherArgs;

  @Reference(target = "(launcher.arguments=*)")
  void setLauncherArguments(Object object, Map<String, Object> map) {
    this.launcherArgs = (String[]) map.get("launcher.arguments");
  }

  @Reference
  StringInverter inverter;

  @Activate
  void activate() {
    boolean isInteractive = Arrays
      .stream(launcherArgs)
      .anyMatch(arg -> "-console".equals(arg));

    // clear launcher arguments from possible framework parameter
    String[] args = Arrays
      .stream(launcherArgs)
      .filter(arg -> !"-console".equals(arg) && !"-consoleLog".equals(arg))
      .toArray(String[]::new);

    for (String arg : args) {
      System.out.println(inverter.invert(arg));
    }

    if (!isInteractive) {
      // shutdown the application if no console was opened
      // only needed if osgi.noShutdown=true is configured
      System.exit(0);
    }
  }
}
```

After building again, the application will directly close without the `-console` parameter. And if `-console` is used, the OSGi console stays open.

The above handling was simply done to have something similar to the Eclipse product build. 
As the Equinox launcher does not automatically start all bundles the `-console` parameter triggers a process to start the necessary Gogo Shell bundles. 
The bnd launcher on the other hand always starts all installed bundles. 
The OSGi console always comes up and can be seen in the command shell even before the `BndStarter` kills it. 
If that behavior does no satisfy your needs, you could also easily build two application variants: one with a console and one without. 
You simply need to create another _bndrun_ file that does not contain the console bundles and no console configuration properties.

```
-runee: JavaSE-1.8
-runfw: org.eclipse.osgi
-runsystemcapabilities: ${native_capability}

-resolve.effective: active;skip:="osgi.service"

-runrequires: \
    osgi.identity;filter:='(osgi.identity=org.fipro.headless.app)'

-runbundles: \
    org.fipro.inverter.api,\
    org.fipro.inverter.provider,\
    org.fipro.headless.app,\
    org.eclipse.osgi.services,\
    org.eclipse.osgi.util,\
    org.apache.felix.scr
```

If you add that additional _bndrun_ file to the bndruns section of the [bnd-export-maven-plugin](https://github.com/bndtools/bnd/tree/master/maven-plugins/bnd-export-maven-plugin) the build will create two exports.

```xml
<plugin>
  <groupId>biz.aQute.bnd</groupId>
  <artifactId>bnd-export-maven-plugin</artifactId>
  <version>${bnd.version}</version>
  <configuration>
    <failOnChanges>false</failOnChanges>
    <bndruns>
      <bndrun>headless.bndrun</bndrun>
      <bndrun>headless_console.bndrun</bndrun> 
    </bndruns>
    <bundles>
      <include>target/repository/plugins/*</include>
    </bundles>
  </configuration>
  <executions>
    <execution>
      <goals>
        <goal>export</goal>
      </goals>
    </execution>
  </executions>
</plugin>
```

To check if the application should be stopped or not, you then need to check for the system property `osgi.console`.

```java
boolean hasConsole = System.getProperty("osgi.console") != null;
```

If a console is configured to not stop the application. If there is no configuration for `osgi.console` call `System.exit(0)`.

This tutorial showed a pretty simple example to explain the basic concepts on how to build a command line application from an Eclipse project. A real-world example can be seen in the [APP4MC](https://www.eclipse.org/app4mc/) [Model Migration addon](https://git.eclipse.org/c/app4mc/org.eclipse.app4mc.addon.migration.git/tree/?h=develop), where the above approach is used to create a standalone model migration command line tool. This tool can be used in other environments like in build servers for example, while the integration in the Eclipse IDE remains in the same project structure.

The sources of this tutorial are available on [GitHub](https://github.com/fipro78/osgi-ds-getting-started-pde).

If you are interested in finding out more about the [Maven plugins from bnd](https://enroute.osgi.org/about/115-bnd-plugins.html) you might want to watch this talk from [EclipseCon Europe 2019](https://www.youtube.com/watch?v=zE3zDwlXNok&list=PLy7t4z5SYNaT_yo5Dhajb9i-Pf0LbQ3z8&index=97&t=0s). As you can see they are helpful in several situations when building OSGi applications.

## Update: configurable console with bnd launcher

I tried to make the executable jar behavior similar to the Equinox one. That means, I wanted to create an application where I am able to configure via command line parameter if the console should be activated or not. Achieving this took me quite a while, as I needed to find out what causes the console to start with Equinox or not. The important thing is that the property `osgi.console` needs to be set to an empty String. The value is actually the port to connect to, and with that value set to an empty String, the current shell is used. In the _bndrun_ files this property is set via `-runproperties`. If you remove it from the _bndrun_ file, the console actually never starts, even if passed as system property on the command line.

Section 19.4.6 in [Launching \| bnd](https://bnd.bndtools.org/chapters/300-launching.html) explains why. It simply says that you are able to override a launcher property via system property. But you can not add a launcher property via system property. Knowing this I solved the issue by setting the `osgi.console` property to an invalid value in the `-runproperties` section.

```
-runproperties: \
    osgi.console=xxx
```

This way the application can be started with or without a console, dependent on whether `osgi.console` is provided as system parameter via command line or not.

Of course the check for the `-console` parameter should be removed from the `BndStarter` to avoid that users need to provide both arguments to open a console!

I added the _headless_configurable.bndrun_ file to the repository to show this:

Launch without console:

```console
java -jar headless_configurable.jar Test
```

Launch with console:

```console
java -jar -Dosgi.console= headless_configurable.jar
```

## Update: bnd-indexer-maven-plugin

I got a pull request that showed an interesting extension to my approach. It uses the [bnd-indexer-maven-plugin](https://github.com/bndtools/bnd/tree/master/maven-plugins/bnd-indexer-maven-plugin) to create an index that can then be used in the _bndrun_ files to make it editable with bndtools.

```xml
<plugin>
  <groupId>biz.aQute.bnd</groupId>
  <artifactId>bnd-indexer-maven-plugin</artifactId>
  <version>${bnd.version}</version>
  <configuration>
    <inputDir>${project.build.directory}/repository/plugins/</inputDir>
  </configuration>
  <executions>
    <execution>
      <phase>package</phase>
      <id>index</id>
      <goals>
        <goal>local-index</goal>
      </goals>
    </execution>
  </executions>
</plugin>
```

To make use of this you first need to execute the build without the [bnd-export-maven-plugin](https://github.com/bndtools/bnd/tree/master/maven-plugins/bnd-export-maven-plugin) so the index is created out of the product build. After that you can create or edit a _bndrun_ file by adding these lines on top:

```
index: target/index.xml;name="org.fipro.headless.product"

-standalone: ${index}
```

I am personally not a big fan of such dependencies in the build timeline. But it is surely helpful for creating the _bndrun_ file.