# Using Events {#tuto-hookcustomizations-events}

From GeoNetwork 3.0.x on, there are a number of events you can listen to on your Java code.

## Enabling Event Listeners

To enable this on your Maven project, you have to add the event dependencies. Edit the file **`custom/pom.xml`** and add the dependencies tag:

``` xml
<dependencies>
   <dependency>
     <groupId>${project.groupId}</groupId>
     <artifactId>events</artifactId>
     <version>${project.version}</version>
   </dependency>
   <dependency>
     <groupId>${project.groupId}</groupId>
     <artifactId>core</artifactId>
     <version>${project.version}</version>
   </dependency>
</dependencies>
```

Then create the file custom/src/main/resources/config-spring-geonetwork.xml to tell Spring to load your custom beans adding the following content:

``` xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:util="http://www.springframework.org/schema/util"
       xmlns:context="http://www.springframework.org/schema/context"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xsi:schemaLocation=
          "http://www.springframework.org/schema/beans 
           http://www.springframework.org/schema/beans/spring-beans.xsd
           http://www.springframework.org/schema/util
           http://www.springframework.org/schema/util/spring-util.xsd
           http://www.springframework.org/schema/context
           http://www.springframework.org/schema/context/spring-context-3.2.xsd">
  <bean class="org.fao.geonet.events.listeners.MyCustomListener" ></bean>
</beans>
```

This file should contain a list of all the classes that listen to events inside GeoNetwork scope.

## Simple Example

We can add a simple example listener like this one, which will print a string every time a metadata gets removed.

``` java
package org.fao.geonet.events.listeners;

import org.fao.geonet.domain.*;

import org.fao.geonet.events.md.MetadataRemove;

import org.springframework.context.ApplicationListener;

import org.springframework.stereotype.Component;

@Component
public class MyCustomListener implements ApplicationListener<MetadataRemove> { 
   @Override
   public void onApplicationEvent(MetadataRemove event) {
      System.out.println("REMOVED");
   }
}
```

For example, we can call an external REST API that gets triggered every time a Metadata gets removed or updated.

## GeoNetwork API

There is also a new API you can use to interact with GeoNetwork from an external script. See more on [API Guide](../../../api/index.md).
