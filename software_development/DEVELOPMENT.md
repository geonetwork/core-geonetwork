# Development

As described in the [overview](OVERVIEW.md) GeoNetwork is written is a Java application written using the spring framework to tie together a wide range of technologies.

This page looks at how these technologies are used in GeoNetwork. Where possible a code example is provided to cut-and-paste from, followed by a discussion.

## Java Formatting

```
    /**
     * Transformed harvest node.
     *
     * @param node harvest node
     * @return transformed harvest node
     * @throws Exception hmm
     */
    private Element transform(Element node) throws Exception {
        String type = node.getChildText("value");
        node = (Element) node.clone();
        return Xml.transform(node, xslPath.resolve(type + ".xsl"));
    }
```

The eclipse formatter `code_quality/formatter.xml` may be used by both Eclipse and IntelliJ:

* Classic Java code style
* Use 4 spaces for indenting (not tabs)
* `140` character line width

## Header

Generic Java header:

```
/*
 * Copyright (C) 2021 Food and Agriculture Organization of the
 * United Nations (FAO-UN), United Nations World Food Programme (WFP),
 * United Nations Environment Programme (UNEP), and others.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
 *
 * Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
 * Rome - Italy. email: geonetwork@osgeo.org
 */
```

Generic XML header
```
<!--
  ~ Copyright (C) 2021 Food and Agriculture Organization of the
  ~ United Nations (FAO-UN), United Nations World Food Programme (WFP),
  ~ United Nations Environment Programme (UNEP), and others.
  ~
  ~ This program is free software; you can redistribute it and/or modify
  ~ it under the terms of the GNU General Public License as published by
  ~ the Free Software Foundation; either version 2 of the License, or (at
  ~ your option) any later version.
  ~
  ~ This program is distributed in the hope that it will be useful, but
  ~ WITHOUT ANY WARRANTY; without even the implied warranty of
  ~ MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  ~ General Public License for more details.
  ~
  ~ You should have received a copy of the GNU General Public License
  ~ along with this program; if not, write to the Free Software
  ~ Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
  ~
  ~ Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
  ~ Rome - Italy. email: geonetwork@osgeo.org
  -->
```

The eclipse code template `code_quality/code_tempalte.xml` provided for Eclipse.

When creatinga new file:

* Indicate copyright with current year (when you are creating the file)/
  
  ```
  * Copyright (C) 2021 Food and Agriculture Organization of the
  * United Nations (FAO-UN), United Nations World Food Programme (WFP)
  * and United Nations Environment Programme (UNEP), and others.
  ```

* The party contributing (you or your employer) retain copyright:

  Represented as `,and others` in the copyright header above.
  
* Optional: As creator of the new file may explicitly state the party creating.
  
  ```
  * Copyright (C) 2021 Camptocamp, and others.
  ```
  
  Keep in mind most file copy-and-paste from existing GeoNetwork code, making the provided heaer apprpraite.

When updating a file no header change is required:

* The party contributing (you or your employer) retain copyright:

  Represented as `, and others` in the copyright boilerplate above.

* Optional:  Provide a date range when a file is updated over time:
  
  ```
  Copyright (C) 2019-2021
  ```

As an open source project GeoNetwork requires headers out of an abudance of caution, primarily to communicate GPL license on a file by file basis.

## Jeeves Framework

The origional core-geonetwork engine is called ``Jeeves`` and is responsible for the core request/response dispatch system in the applicaiton.

* The approach is based around using XML ``Element`` parameters and return values
* This approach can be lots of fun, but you are warned that is is not strictly structured and type checked.
* Use of ``XmlUtls.toString(element)`` can be used when debugging to check method parameters and return values.

There is an explicit ``JeevesEngine`` class:

* Responsible for application startup
* Access to a configuration directory, and a configuration database
* Lifecycle of core components including their ``init`` and ``cleanup``

Core components configured and managed by ``JeevesEngine``:

* ``AppHandler``, for individual applications such as ``Geonetwork``
* ``ServiceManager``, configured with serivces for ``Service`` dispatch
* ``MonitorManager``, configured with ``Monitors`` to record statistics and monitor application services 

Each level of ``Jeeves`` is provided a context to interact with the rest of the application:

* ``Logger``
* ``BasicContext`` application access and bean discouvery
* ``ServiceContext`` complete application access, in addition details of the current request, and user session

## Spring Framework

While the Element based Jeeves Framework forms the core application, a bridge is provided to the popular Spring Framework structured around inversion of control:

* ``JeevesDispatcherServlet`` takes care of dispatching requests to the spring-framework, from a ``TransactionTask`` allowing commit / rollback in the event of failure.
* ``ApplicationContextHolder`` provides a thread local for the current spring `ApplicationContext` 

## Geonetwork

The ``Geonetwork`` AppHandler:

* Takes responsibility for setting up a wide range of low-level application components
* A lot of the low-level GeoNetwork components (like ``SvnManager``) are configured here

Jeeves provides a shared ``AppHandlerSerivceContext`` to ``GeoNetwork`` during initalization.

## MonitorManager 

MoniorManager uses the ``yammer`` (now ``DropWizard``) metrics library, health checks and timers are scheduled to keeping tabs on the running GeoNetwork applicaiton.

During initalziation ``MonitorManager`` defines its own ``monitorContext`` AppHandlerSerivceContext, which is used by Monitor tasks needing to make use of a ``ServiceContext`` to access utility methods. This context is reclaimed during ``shutdown`` after all the monitors have been stopped.

## Transaction Manager

``TransactionManager`` defines an execution mechaism for safely performing actions in the context of a long runnning transaction.

* ``TransactionManager.runInTransaction(name, applicationContext, transactionRequirement, commitBehavior, transactionTask)``
  
During the executation of the ``TransactionTasks`` event notifications are sent out for:

* ``NewTransactionListener``
* ``BeforeCommitTransactionListener``
* ``AfterCommitTransactionListener``
* ``BeforeRollbackTransactionListener``
* ``AfterRollbackTransactionListener``

## Working with metadata

Previously ``DataManager`` was used to handle all operations on metadata, this is in the process of being replaced with:

* ``IMetadataManager``, ``IMetadataUtils``
* ``IMetadataIndexer``
* ``IMetadataValidator``
* ``IMetadataOperations``
* ``IMetadataStatus``
* ``IMetadataSchemaUtils``
* ``IMetadataCategory``

The common theme for all of these classes is illustrated with ``IMetadataManager``:

* ``IMetadataManager`` acts as a facade defining utility methods to perform common tasks.
* ``BaseMetadataManager`` provides an implementation for performing common with metadata records.
* Additional implementations are possible, either created to support testing, or in this case ``DraftMetadataManager`` used for performing common tasks with a draft record.

## HarvestManager

Uses the Quartz scheduling library to run harvesting activities in background tasks.

## ServiceManager and Service

The Jeeves ``Service`` instances are xml focused and  direct:

* ``Service.exec(Element, ServiceContext): Element``
  
  Very direct indeed with an XML Element parameter producing an XML Element result.

The vast majority of the GeoNetwork application is implemented as services, and many utility methods expect a ``ServiceContext`` to be provided as a parameter or available as a thread locale.

### ServiceContext

``ServiceContext`` provides access to detail on the current request, and the user session.

When creating a ServiceContext you are responsible for managing its use on the current thread and any cleanup.

Using auto-closable:
```
try(ServiceContext context = serviceMan.createServiceContext("md.thumbnail.upload", lang, request)){
    ...
}
```

Or manually:
```
ServiceContext context = serviceMan.createServiceContext("md.thumbnail.upload", lang, request);
try {
    context.setAsThreadLocal();
    ...
} finally {
    context.clearAsThreadLocal();
    context.clear();
}
```

Many utility classes and methods expect a service context to be provide as a parameter, or to be available for the current thread.

```
context = ServiceContext.get();
```

ServiceContext is intended to be created using the current `HTTPRequest`:

```
try (ServiceContext context = serviceManager.createServiceContext("md.thumbnail.upload", lang, request)) {
   DataManager dataMan = context.getBean(DataManager.class);
   String version = dataMan.getVersion(id);
   ...
}
```

This pattern is so commonly used for API methods that ``ApiUtils`` provides a helper:
```
try (ServiceContext context = ApiUtils.createServiceContext(request)) {
   // the context is alread set on the thread locale
   ...
}
```

When implementing event handlers, such as ``BeforeCommitTransactionListener`` there is no direct access to the current `HTTPRequest`. The ``createServiceContext( name, userId)`` method obtains the ``HTTPRequest`` from the ServletRequest holder if used during a request, or uses the provided  userId to look up user session when running in a background thread:

```
try (ServiceContext context = serviceManager.createServiceContext("approve_record", userId)) {
   draftUtilities.replaceMetadataWithDraft(event.getMd());
}
```

It can also be a challenge to provide a service context to a background job or activity. Use your existing service context to create a context for the background job or activity to use:

```
ServiceContext context = serviceManager.createServiceContext( "harvester."+type, serviceContext );
harvester.initContext( context );
```

The background job or activity is responsible eventual cleanup:
```
public void shutdown() throws SchedulerException {
   getScheduler().deleteJob(jobKey(getParams().getUuid(), HARVESTER_GROUP_NAME));
   context.clear();
   context = null;
}
```

Here is the same approach using a Runnable:
```
final context = serviceManager.createServiceContext("later", serviceContext);
return new Runnable(){
    public void run() {
        context.setAsThreadLocal();
        try {
            DataManager dataManager = context.getBean(DataManager.class);
            ...
        }
        finally {
          context.clearAsThreadLocal();
          context.clear();
        }
};
```

#### ServiceContext.AppHandlerServiceContext

Originally an anonymous class `AppHandlerServiceContext` is an adapter allowing code, such as initialization and shutdown, to make use of functionality that assumes a real service context is available.

Jeeves and other initialization code makes use of an AppHandlerServiceContext during initlization:

```
this.serviceContext = serviceManager.createAppHandlerServiceContext("manager", appContext);
```

This implementation overrides ``setUserSession()``, ``setIpAddress()`` and ``clear()`` to warn of inappropriate use by utility methods.

This does require a cast during during shutdown:

```
if (harvesterContext != null){
    // Call superclass cleanup to avoid AppHandlerServiceContext protections
    ((ServiceContext)harvesterContext).clear();
}
```

#### ServiceContext.ThreadLocalPolicy

The use of `setAsThreadLocal()` assigns the instance to a thread locale, allowing discovery by any of the methods in the following try block. Because we assigned this thread local, we are responsible for carefully calling ``clearAsThreadLocal()`` in the finally block.

Care is required as leaving objects in a thread locale is easy way to leak memory (which can be difficult to debug). As an example a `Runnable`  used in a `ThreadPool` may accidentally leave objects in a thread local, which would confuse the next `Runnable` to be scheduled on the same physical thread.

* If you create the service context you are responsible for seeing that is cleared up, either directly as part of of your method execution, or later as part of an object init / cleanup lifecycle.

* If you call `setAsThreadLocal()` be sure to call `clearAsThreadLocal()` to avoid leaking memory

* Methods like `ApiUtils.createServiceContext` both create a service context AND call `setAsThreadLocal()` to the new service context to the current therad.
  
  These methods are designed to be for use with try-with-resource `AutoClosable`.

* There is no naming convention to determine which create methods have the `setAsThreadLocal()` side effect, this is documented in the javadocs only (usually with a code example).

* When in doubt call `close()` in a finally block, this is safe and takes care of both `setAsThreadLocal()` and `clear()` if needed (without any errors or warnings).

* Calling `setAsThreadLocal()` when a thread locale is already provided for the thread ... is in appropriate and will result in a warning or exception depending on thread local policy described below.
  
  As a workaround consider backing up the current service context:
  
  ```
  ServiceContext check = ServiceContext.get();
  if( previous != null ) previous.clearAsThreadLocal();
  try (ServiceContext context = serviceManger.createServiceContext("index",userId)){
      ...
  }
  finally {
    if( previous != null ) previous.setAsThreadLocale();
  }
  ```

* The method ``setAsThreadLocal()`` will also double check that ``ApplicationContextHolder`` is configured correctly (with the spring application context provided for the service context).

To aid with debugging ``ThreadLocalPolicy`` can be defined as:

* `-Djeeves.server.context.service.policy=direct`
  
  Direct management of thread locale with no checking (matching 3.10.x functionality)
  
* `-Djeeves.server.context.service.policy=trace`
   
  Trace functionality produes some log messages if thread local used incorrectly.

* `-Djeeves.server.context.service.policy=strict`
  
  Raise an illegal state exception when thread local used incorrectly.

The log (or exception) note where `setAsThreadLocal()` was called from, and where `clearAsThreadLocal()` is being called from to aid in debugging. 

#### ServiceContext.ServiceDetails

Parameter object used to share ``ServiceContext`` details (service, language, ipAddress) to another thread.

Primiarly used to log service details the data structure may be useful for your own work.

