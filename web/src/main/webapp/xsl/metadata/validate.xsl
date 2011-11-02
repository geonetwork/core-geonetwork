<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:geonet="http://www.fao.org/geonetwork" xmlns:xlink="http://www.w3.org/1999/xlink"
  xmlns:sch="http://www.ascc.net/xml/schematron" xmlns:gml="http://www.opengis.net/gml"
  xmlns:gmd="http://www.isotc211.org/2005/gmd" xmlns:srv="http://www.isotc211.org/2005/srv"
  xmlns:gco="http://www.isotc211.org/2005/gco" xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
  exclude-result-prefixes="geonet srv gco gmd xlink gml sch svrl">

  <xsl:include href="validate-fn.xsl"/>
  
  <xsl:template match="/">
    <rules>
      <xsl:call-template name="metadata-validation-report">
        <xsl:with-param name="report" select="/root/response/geonet:report"/>
      </xsl:call-template>
    </rules>
  </xsl:template>


  <xsl:template name="metadata-validation-report">
    <xsl:param name="report"/>

    <!-- Check if an error element exists. It could happen if XSD validation failed
		  when schema not found for example. -->
    <xsl:if test="$report/error">
      <rule id="validation-report">
        <msg>
          <xsl:value-of select="$report/error/message"/>
        </msg>
      </rule>
    </xsl:if>

    <xsl:apply-templates mode="validation-report" select="$report/geonet:xsderrors"/>
    <xsl:apply-templates mode="validation-report" select="$report/geonet:schematronerrors"/>
  </xsl:template>


  <!-- XSD validation report -->
  <xsl:template match="geonet:xsderrors" mode="validation-report">
    <xsl:variable name="count" select="count(geonet:error)"/>

    <xsl:for-each select="geonet:error">
      <rule group="xsd" type="error" id="xsd#{geonet:errorNumber}">
        <details>
          <xsl:value-of
            select="geonet:typeOfError"/>-XPath: <xsl:value-of select="geonet:xpath"/>
        </details>
        <title>
          <xsl:value-of select="geonet:parse-xsd-error(geonet:message, //response/schema, /root/gui)"/>
        </title>
      </rule>
    </xsl:for-each>
  </xsl:template>

  <xsl:template match="svrl:active-pattern" mode="validation-report">
    <xsl:variable name="preceding-ap" select="count(following-sibling::svrl:active-pattern)"/>


    <xsl:apply-templates mode="validation-report"
      select="following-sibling::*[(name(.)='svrl:failed-assert' or name(.)='svrl:successful-report')
				and count(following-sibling::svrl:active-pattern) = $preceding-ap]"
      >
      <xsl:with-param name="title" select="@name"/>
    </xsl:apply-templates>
  </xsl:template>


  <xsl:template match="svrl:failed-assert" mode="validation-report">
    <xsl:param name="title"/>
    
    <rule group="{ancestor::*[name(.)='geonet:report']/@geonet:rule}" type="error" id="{generate-id(.)}" ref="{@ref}">
      <title><xsl:value-of select="$title"/></title>
      <details><xsl:value-of select="@location"/></details>
      <msg>
        <!--<xsl:copy-of select="svrl:text/descendant::node()[name(.)='div']"/>
        <xsl:copy-of select="svrl:text/descendant::node()[contains(name(.), 'report')]/*|svrl:text/descendant::node()[contains(name(.), 'report')]/text()"/>-->
        <xsl:value-of select="normalize-space(svrl:text)"/>
      </msg>
    </rule>
  </xsl:template>

  <xsl:template match="svrl:successful-report" mode="validation-report">
    <xsl:param name="title"/>
    
    <rule group="{ancestor::*[name(.)='geonet:report']/@geonet:rule}" type="success" id="{generate-id(.)}" ref="{@ref}">
      <title><xsl:value-of select="$title"/></title>
      <details><xsl:value-of select="@location"/></details>
      <msg>
        <xsl:value-of select="normalize-space(svrl:text)"/>
      </msg>
    </rule>
  </xsl:template>

  <xsl:template match="geonet:schematronVerificationError" mode="validation-report">
    <xsl:param name="title"/>
    
    <rule group="{ancestor::*[name(.)='geonet:report']/@geonet:rule}" type="error" id="{generate-id(.)}" ref="">
      <title><xsl:value-of select="../@geonet:rule"/></title>
      <details><xsl:value-of select="../@geonet:rule"/></details>
      <msg>
        <xsl:value-of select="."/>
      </msg>
    </rule>
  </xsl:template>

  <xsl:template match="geonet:report" mode="validation-report">
    <xsl:variable name="rule" select="@geonet:rule"/>
    <xsl:variable name="count" select="count(svrl:schematron-output/svrl:failed-assert)"/>

    <xsl:apply-templates mode="validation-report"
      select="svrl:schematron-output/svrl:active-pattern|geonet:schematronVerificationError"/>

  </xsl:template>

  <xsl:template match="geonet:schematronerrors" mode="validation-report">
      <xsl:apply-templates select="*" mode="validation-report"/>
  </xsl:template>

</xsl:stylesheet>
