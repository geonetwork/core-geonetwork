<?xml version="1.0" encoding="UTF-8"?>
<!--
  ~ Copyright (C) 2001-2016 Food and Agriculture Organization of the
  ~ United Nations (FAO-UN), United Nations World Food Programme (WFP)
  ~ and United Nations Environment Programme (UNEP)
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

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0">

  <xsl:param name="nodeId"/>

  <xsl:param name="user"/>
  <xsl:param name="password"/>
  <xsl:param name="dbUrl"/>
  <xsl:param name="dbDriver"/>
  <xsl:param name="poolSize"/>


  <xsl:template match="/">
    <xsl:message>Generate spring config for node <xsl:value-of select="$nodeId"/></xsl:message>

    <beans xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:context="http://www.springframework.org/schema/context"
           default-lazy-init="true"
           xmlns="http://www.springframework.org/schema/beans"
           xsi:schemaLocation="
            http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd
            http://www.springframework.org/schema/context http://www.springframework.org/schema/context/spring-context.xsd">
      <import resource="classpath*:/config-spring-geonetwork.xml"/>
      <import resource="../config-db/database_migration.xml"/>

      <context:property-override properties-ref="{$nodeId}-configuration-overrides"/>

      <bean id="nodeInfo" class="org.fao.geonet.NodeInfo">
        <property name="id" value="{$nodeId}"/>
        <property name="defaultNode" value="false"/>
      </bean>

      <bean id="{$nodeId}-configuration-overrides"
            class="org.springframework.beans.factory.config.PropertiesFactoryBean">
        <property name="properties">
          <props>
            <prop key="jdbcDataSource.username">
              <xsl:value-of select="$user"/>
            </prop>
            <prop key="jdbcDataSource.password">
              <xsl:value-of select="$password"/>
            </prop>
            <prop key="jdbcDataSource.maxActive">
              <xsl:value-of select="$poolSize"/>
            </prop>
            <prop key="jdbcDataSource.maxIdle">
              <xsl:value-of select="$poolSize"/>
            </prop>
            <prop key="jdbcDataSource.initialSize">0</prop>
            <prop key="jdbcDataSource.Url">
              <xsl:value-of select="$dbUrl"/>
            </prop>
          </props>
        </property>
      </bean>
      <import resource="../config-db/{$dbDriver}.xml"/>
    </beans>

  </xsl:template>

</xsl:stylesheet>
