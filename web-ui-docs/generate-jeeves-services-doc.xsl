<?xml version="1.0" encoding="UTF-8"?>
<!--
  Generate documentation from Jeeves configuration files
-->
<xsl:stylesheet xmlns:sec="http://www.springframework.org/schema/security"
                xmlns:beans="http://www.springframework.org/schema/beans"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                version="2.0">

  <xsl:output method="text" version="1.0" encoding="utf-8" indent="no"/>


  <xsl:variable name="baseConfigPath" select="'../web/src/main/webapp/WEB-INF/'"/>


  <xsl:variable name="javadocBaseURL"
                select="'http://geonetwork-opensource.org/manuals/trunk/eng/developer/apidocs/geonetwork/'"/>

  <xsl:variable name="githubBaseURL"
                select="'https://github.com/geonetwork/core-geonetwork/blob/develop/web/src/main/webapp/'"/>

  <xsl:variable name="configSecurity"
                select="document(concat($baseConfigPath, 'config-security/config-security-mapping.xml'))/beans:beans"/>
  <xsl:variable name="package" select="'org.fao.geonet'"/>

  <xsl:variable name="fileName" select="tokenize(document-uri(/), '/')[last()]"/>

  <xsl:variable name="newLine">
<xsl:text>
</xsl:text>
  </xsl:variable>


  <xsl:template match="/">

    @ngdoc overview
    @name
    <xsl:value-of select="substring-after(substring-before($fileName, '.xml'), 'config-')"/>
    @description

    # Services documentation
    <xsl:value-of select="$fileName"/>


    <xsl:call-template name="doc-for-file">
      <xsl:with-param name="fileName" select="$fileName"/>
      <xsl:with-param name="configFile" select="//services"/>
    </xsl:call-template>


  </xsl:template>

  <xsl:template name="doc-for-file">
    <xsl:param name="fileName"/>
    <xsl:param name="configFile"/>

    Configuration file [<xsl:value-of select="substring-before($fileName, '.xml')"/>](<xsl:value-of
    select="concat($githubBaseURL, 'WEB-INF/config/', $fileName)"/>)

    List of services:

    <xsl:for-each select="$configFile//service">
      <xsl:sort select="@name"/>
      * Service
      <xsl:value-of select="@name"/>
    </xsl:for-each>

    <xsl:apply-templates mode="doc" select="$configFile//service">
      <xsl:with-param name="fileName" select="$fileName"/>
    </xsl:apply-templates>
  </xsl:template>


  <xsl:template mode="doc" match="service">
    <xsl:param name="fileName"/>
    <xsl:variable name="serviceName" select="@name"/>

    ##
    <xsl:text>&lt;a id="</xsl:text><xsl:value-of select="@name"/><xsl:text>"&gt;&lt;/a&gt;</xsl:text>
    Service
    <xsl:value-of select="@name"/>

<xsl:text>

</xsl:text>

    <xsl:value-of select="replace(documentation, '\n\s*', $newLine)"/>

<xsl:text>

</xsl:text>


    <xsl:for-each select="class">
      <xsl:variable name="class" select="concat($package, @name)"/>
      <xsl:variable name="javadocURL"
                    select="concat($javadocBaseURL, translate($class, '.', '/'), '.html')"/>
      * Javadoc for class: [<xsl:value-of select="$class"/>](<xsl:value-of select="$javadocURL"/>)
    </xsl:for-each>

    <xsl:if test="output">
      <xsl:variable name="xslURL" select="concat($githubBaseURL, 'WEB-INF/', output/@sheet)"/>
      * Output XSLT: [<xsl:value-of select="substring-after(output/@sheet, '../')"/>](<xsl:value-of
      select="$xslURL"/>)

      <xsl:if test="output/xml">
        <xsl:variable name="locURL" select="concat($githubBaseURL, 'loc/eng/', output/xml/@file)"/>
        * i18n resource: [<xsl:value-of select="output/xml/@name"/>](<xsl:value-of
        select="$locURL"/>)
      </xsl:if>
    </xsl:if>

    <xsl:variable name="configURL" select="concat($githubBaseURL, 'WEB-INF/config/', $fileName)"/>
    * Configuration file: [<xsl:value-of select="$fileName"/>](<xsl:value-of select="$configURL"/>)

    * Security: ** <xsl:value-of
    select="$configSecurity//sec:intercept-url[contains(@pattern, concat('/', $serviceName))]/@access"/>**

    <!--```-->
    <!--<xsl:copy-of select="." />-->
    <!--```-->

  </xsl:template>
</xsl:stylesheet>

