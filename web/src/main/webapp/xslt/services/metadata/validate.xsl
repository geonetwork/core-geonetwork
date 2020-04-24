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

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:geonet="http://www.fao.org/geonetwork"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                xmlns:sch="http://www.ascc.net/xml/schematron"
                xmlns:gml="http://www.opengis.net/gml/3.2"
                xmlns:gml320="http://www.opengis.net/gml"
                xmlns:gmd="http://www.isotc211.org/2005/gmd"
                xmlns:srv="http://www.isotc211.org/2005/srv"
                xmlns:gco="http://www.isotc211.org/2005/gco"
                xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
                version="2.0"
                exclude-result-prefixes="geonet srv gco gmd xlink gml sch svrl">

  <xsl:include href="validate-fn.xsl"/>
  <xsl:param name="rootTag" select="'rules'"/>

  <!-- Retrieve GUI language first 2 letters
  for multilingual schematron using xml:lang attribute. -->
  <xsl:variable name="language" select="substring(/root/language, 1, 2)"/>
  <xsl:variable name="metadataSchema" select="/root/schema"/>

  <xsl:template match="/">
    <xsl:element name="{$rootTag}">
      <xsl:call-template name="metadata-validation-report">
        <xsl:with-param name="report" select="/root/geonet:report"/>
      </xsl:call-template>
    </xsl:element>
  </xsl:template>


  <xsl:template name="metadata-validation-report">
    <xsl:param name="report"/>

    <!-- Check if an error element exists. It could happen if XSD validation failed
          when schema not found for example. -->
    <xsl:if test="$report/error">
      <report>
        <id>Error</id>
        <displayPriority>-100</displayPriority>
        <error>1</error>
        <success>0</success>
        <total>1</total>
        <requirement>REQUIRED</requirement>
        <patterns>
          <pattern>
            <rules>
              <rule id="validation-report">
                <msg>
                  <xsl:value-of select="$report/error/message"/>
                </msg>
              </rule>
            </rules>
          </pattern>
        </patterns>
      </report>
    </xsl:if>

    <report>
      <xsl:call-template name="xsdErrors">
        <xsl:with-param name="xsdErrors" select="$report/geonet:xsderrors"/>
      </xsl:call-template>
    </report>
    <xsl:for-each select="$report/geonet:schematronerrors/geonet:report">
      <xsl:sort select="@geonet:displayPriority"/>
      <xsl:call-template name="schematron-report"/>
    </xsl:for-each>
  </xsl:template>

  <xsl:template name="xsdErrors">
    <xsl:param name="xsdErrors"/>
    <id>xsd</id>
    <displayPriority>0</displayPriority>
    <error>
      <xsl:value-of select="count($xsdErrors/geonet:error)"/>
    </error>
    <success>?</success>
    <total>?</total>
    <requirement>REQUIRED</requirement>
    <patterns>
      <xsl:for-each select="$xsdErrors/geonet:error">
        <pattern>
          <title>
            <xsl:value-of select="geonet:parse-xsd-error(geonet:message,
                        $metadataSchema,
                        /root/*[name() = $metadataSchema]/labels,
                        /root/*[name() = $metadataSchema]/strings)"/>
          </title>
          <rules>
            <rule group="xsd" type="error" id="xsd#{geonet:errorNumber}">
              <details>
                <xsl:value-of select="geonet:typeOfError"/>
                <xsl:text>-XPath:</xsl:text>
                <xsl:value-of select="geonet:xpath"/>
              </details>
            </rule>
          </rules>
        </pattern>
      </xsl:for-each>
    </patterns>
  </xsl:template>

  <xsl:template name="schematron-report">
    <xsl:variable name="rulename" select="string(@geonet:rule)"/>
    <xsl:variable name="errors" select="count(.//svrl:failed-assert)"/>
    <xsl:variable name="successes" select="count(.//svrl:successful-report)"/>
    <xsl:variable name="schematronVerificationError" select="./geonet:schematronVerificationError"/>
    
    <report>
      <id>
        <xsl:value-of select="$rulename"/>
      </id>
      <displayPriority>
        <xsl:value-of select="@geonet:displayPriority"/>
      </displayPriority>
      <label>
        <xsl:variable name="translatedTitle"
                      select="/response/schematronTranslations/*[name() = $rulename]/strings/schematron.title"/>
        <xsl:variable name="defaultTitle"
                      select="svrl:schematron-output/@title"/>
        <xsl:choose>
          <xsl:when test="$translatedTitle">
            <xsl:value-of select="$translatedTitle"/>
          </xsl:when>
          <xsl:when test="$defaultTitle">
            <xsl:value-of select="$defaultTitle"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="$rulename"/>
          </xsl:otherwise>
        </xsl:choose>
      </label>
      <!-- If the schematron failed to compile or during the validation it should have a `geonet:schematronVerificationError` element -->
      <xsl:if test="$schematronVerificationError">
        <schematronVerificationError>
          <xsl:value-of select="$schematronVerificationError"/>
        </schematronVerificationError>
      </xsl:if>
      <error>
        <xsl:value-of select="$errors"/>
      </error>
      <success>
        <xsl:value-of select="$successes"/>
      </success>
      <total>
        <xsl:value-of select="$errors + $successes"/>
      </total>
      <requirement>
        <xsl:value-of select="@geonet:required"/>
      </requirement>
      <patterns>
        <xsl:for-each select="svrl:schematron-output/svrl:active-pattern">
          <xsl:call-template name="pattern"/>
        </xsl:for-each>
      </patterns>
    </report>

  </xsl:template>

  <xsl:template name="pattern">
    <pattern>
      <title>
        <xsl:variable name="attributeName"
                      select="concat('name_', $language)"/>
        <xsl:choose>
          <xsl:when test="attribute::*[name() = $attributeName] != ''">
            <xsl:value-of select="attribute::*[name() = $attributeName]"/>
          </xsl:when>
          <xsl:when test="attribute::*[name() = 'name_en'] != ''">
            <xsl:value-of select="attribute::*[name() = 'name_en']"/>
          </xsl:when>
          <xsl:when test="normalize-space(@name) != ''">
            <xsl:value-of select="@name"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:text>--</xsl:text>
          </xsl:otherwise>
        </xsl:choose>
      </title>
      <rules>
        <xsl:for-each select="svrl:fired-rule/*">
          <xsl:call-template name="successAndFailure"/>
        </xsl:for-each>
      </rules>
    </pattern>
  </xsl:template>

  <xsl:template name="successAndFailure">
    <xsl:variable name="type">
      <xsl:choose>
        <xsl:when test="name() = 'svrl:successful-report'">success</xsl:when>
        <xsl:otherwise>error</xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <rule type="{$type}" id="{generate-id(.)}" ref="{@ref}">
      <test>
        <xsl:value-of select="@test"/>
      </test>
      <details>
        <xsl:value-of select="@location"/>
      </details>
      <msg>
        <xsl:choose>
          <xsl:when test="svrl:diagnostic-reference[@xml:lang = $language]">
            <xsl:value-of select="svrl:diagnostic-reference[@xml:lang = $language]"/>
          </xsl:when>
          <xsl:when test="svrl:diagnostic-reference[@xml:lang = 'en']">
            <xsl:value-of select="svrl:diagnostic-reference[@xml:lang = 'en']"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="normalize-space(svrl:text)"/>
          </xsl:otherwise>
        </xsl:choose>
      </msg>
    </rule>
  </xsl:template>

</xsl:stylesheet>
