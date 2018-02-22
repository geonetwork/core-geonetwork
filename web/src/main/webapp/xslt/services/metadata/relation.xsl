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

<!--
  Create XML document containing all related items
  following relatedResponse.xsd.
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:geonet="http://www.fao.org/geonetwork" 
                xmlns:util="java:org.fao.geonet.util.XslUtil"
                xmlns:gfc="http://www.isotc211.org/2005/gfc" 
                xmlns:gmd="http://www.isotc211.org/2005/gmd"
                xmlns:gmx="http://www.isotc211.org/2005/gmx" 
                xmlns:gco="http://www.isotc211.org/2005/gco"
                xmlns:xlink="http://www.w3.org/1999/xlink" 
                version="2.0" exclude-result-prefixes="#all">

  <xsl:include href="../../common/profiles-loader-tpl-brief.xsl"/>
  <xsl:include href="../../common/profiles-loader-relations.xsl"/>

  <xsl:template match="/">
    <related>
      <!-- online and thumbnail are extracted from schema extract-relations.xsl -->
      <!--<xsl:message><xsl:copy-of select="."/></xsl:message> -->
      <xsl:apply-templates mode="relation" select="/root/relations/*"/>
    </related>
  </xsl:template>


  <!-- Bypass summary elements -->
  <xsl:template mode="relation" match="summary" priority="99"/>


  <!-- In Lucene only mode, metadata are retrieved from the index and pass 
    as a simple XML with one level element. Make a simple copy here. -->
  <xsl:template mode="superBrief" match="metadata">
    <xsl:copy>
      <xsl:copy-of select="*|@*"/>
    </xsl:copy>
  </xsl:template>


  <xsl:template mode="relation" match="fcats">
    <fcats>
    <xsl:for-each select="response/metadata/gfc:FC_FeatureCatalogue">
      <xsl:variable name="type" select="name(.)" />
      <xsl:variable name="uuid"
        select="if (./@uuid != '') then ./@uuid else geonet:info/source/uuid" />
      <xsl:variable name="id" select="geonet:info/id" />
      <xsl:variable name="title"
        select="gmx:name/gco:CharacterString|
        gfc:name/gco:CharacterString|
        gfc:typeName/gco:LocalName" />
      <xsl:variable name="abstract" select="gfc:scope" />
      <xsl:variable name="featureTypes" select="gfc:featureType" />

      
        <item>
          <id>
            <xsl:value-of select="$uuid" />
          </id>
          <mdid>
            <xsl:value-of select="$id" />
          </mdid>
          <title>
            <value lang="{$lang}">
              <xsl:value-of select="normalize-space($title)" />
            </value>
          </title>
          <url>
            <value lang="{$lang}">
              <xsl:value-of
                select="concat(util:getSettingValue('nodeUrl'), 'api/records/', $uuid)" />
            </value>
          </url>
          <description>
            <value lang="{$lang}">
              <xsl:value-of select="normalize-space($abstract)" />
            </value>
          </description>
          <mdType>featureCatalog</mdType>
          <featureType>
            <xsl:for-each select="$featureTypes">
              <!-- Index attribute table as JSON object -->
              <xsl:variable name="attributes" select=".//gfc:carrierOfCharacteristics" />
              <xsl:if test="count($attributes) > 0">
                <attributeTable>
                  <xsl:for-each select="$attributes">
                    <element>
                      <name>
                        <xsl:value-of select="*/gfc:memberName/*/text()" />
                      </name>
                      <definition>
                        <xsl:value-of select="*/gfc:definition/*/text()" />
                      </definition>
                      <code>
                        <xsl:value-of select="*/gfc:code/*/text()" />
                      </code>
                      <link>
                        <xsl:value-of select="*/gfc:code/gmx:Anchor/@xlink:href" />
                      </link>
                      <type>
                        <xsl:value-of
                          select="*/gfc:valueType/gco:TypeName/gco:aName/*/text()" />
                      </type>
                      <xsl:if test="*/gfc:listedValue">
                        <values>
                          <xsl:for-each select="*/gfc:listedValue">
                            <label>
                              <xsl:value-of select="*/gfc:label/*/text()" />
                            </label>
                            <code>
                              <xsl:value-of select="*/gfc:code/*/text()" />
                            </code>
                            <definition>
                              <xsl:value-of select="*/gfc:definition/*/text()" />
                            </definition>
                          </xsl:for-each>
                        </values>
                      </xsl:if>
                    </element>
                  </xsl:for-each>
                </attributeTable>
              </xsl:if>
            </xsl:for-each>
          </featureType>
        </item>      
    </xsl:for-each>
    </fcats>
  </xsl:template>


  <xsl:template mode="relation" match="related|services|datasets|children|
                      parent|sources|hasfeaturecats|
                      siblings|associated|sources|hassources">
    <xsl:variable name="type" select="name(.)"/>

    <xsl:if test="response/metadata|response/sibling">
      <xsl:element name="{$type}">
        <xsl:for-each select="response/metadata|response/sibling/*">

          <!-- Fast output doesn't produce a full metadata record -->
          <xsl:variable name="metadata">
            <xsl:apply-templates mode="superBrief" select="."/>
          </xsl:variable>

          <xsl:variable name="uuid"
                        select="if ($metadata/uuid != '') then $metadata/uuid else geonet:info/uuid"/>
          <xsl:variable name="id"
                        select="if ($metadata/id != '') then $metadata/id else geonet:info/id"/>

          <item>
            <id>
              <xsl:value-of select="$uuid"/>
            </id>
            <mdid>
              <xsl:value-of select="$id"/>
            </mdid>
            <title>
              <value lang="{$lang}">
                <xsl:value-of select="$metadata/title"/>
              </value>
            </title>
            <url>
              <xsl:value-of
                select="concat(util:getSettingValue('nodeUrl'), 'api/records/', $uuid)"/>
            </url>
            <logo>
              <xsl:value-of select="$metadata/logo"/>
            </logo>
            <description>
              <value lang="{$lang}">
                <xsl:value-of select="$metadata/abstract"/>
              </value>
            </description>

            <xsl:for-each select="$metadata/type">
              <mdType>
                <xsl:value-of select="."/>
              </mdType>
            </xsl:for-each>

            <xsl:if test="$type = 'siblings'">
              <associationType>
                <xsl:value-of select="../@association"/>
              </associationType>
              <initiativeType>
                <xsl:value-of select="../@initiative"/>
              </initiativeType>
            </xsl:if>
            <xsl:if test="$type = 'associated'">
              <xsl:copy-of select="$metadata/*[starts-with(name(), 'agg_')]"/>
            </xsl:if>
          </item>
        </xsl:for-each>
      </xsl:element>
    </xsl:if>
  </xsl:template>

  <!-- Add the default title as title. This may happen when title is retrieve 
    from index and the record is not available in current language. eg. iso19110 
    records are only indexed with no language info. -->
  <xsl:template mode="superBrief" match="metadata">
    <xsl:copy-of select="*"/>
    <xsl:if test="not(title)">
      <title>
        <xsl:value-of select="defaultTitle"/>
      </title>
    </xsl:if>
  </xsl:template>
</xsl:stylesheet>
