<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

    <xsl:param name="nodeId"/>

    <xsl:param name="user"/>
    <xsl:param name="password"/>
    <xsl:param name="dbUrl"/>
    <xsl:param name="dbDriver"/>
    <xsl:param name="poolSize"/>


    <xsl:template match="/">
        <xsl:message>Generate spring config for node <xsl:value-of select="$nodeId"/></xsl:message>

        <beans default-lazy-init="true" xmlns="http://www.springframework.org/schema/beans"
            xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
            xmlns:context="http://www.springframework.org/schema/context"
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
