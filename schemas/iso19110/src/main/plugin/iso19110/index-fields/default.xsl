<?xml version="1.0" encoding="UTF-8" ?>
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

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:gfc="http://www.isotc211.org/2005/gfc"
                xmlns:gmd="http://www.isotc211.org/2005/gmd"
                xmlns:gmx="http://www.isotc211.org/2005/gmx"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                xmlns:gco="http://www.isotc211.org/2005/gco"
                xmlns:util="java:org.fao.geonet.util.XslUtil"
                xmlns:gn-fn-index="http://geonetwork-opensource.org/xsl/functions/index"
                version="1.0">

  <!-- This file defines what parts of the metadata are indexed by Lucene
        Searches can be conducted on indexes defined here.
        The Field@name attribute defines the name of the search variable.
        If a variable has to be maintained in the user session, it needs to be
        added to the GeoNetwork constants in the Java source code.
        Please keep indexes consistent among metadata standards if they should
        work accross different metadata resources -->
  <!-- ========================================================================================= -->


  <xsl:import href="common/index-utils.xsl"/>

  <xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes"/>

  <!-- ========================================================================================= -->

  <xsl:template match="/">
    <!-- TODO I don't know what tag the language is in so this needs to be done -->
    <xsl:variable name="langCode" select="''"/>
    <Document locale="{$langCode}">

      <!-- locale information -->
      <Field name="_locale" string="{$langCode}" store="true" index="true"/>
      <Field name="_docLocale" string="{$langCode}" store="true" index="true"/>

      <!-- For multilingual docs it is good to have a title in the default locale.  In this type of metadata we don't have one but in the general case we do so we need to add it to all -->
      <Field name="_defaultTitle"
             string="{/gfc:FC_FeatureCatalogue/gmx:name/gco:CharacterString|
        /gfc:FC_FeatureCatalogue/gfc:name/gco:CharacterString|
        /gfc:FC_FeatureType/gfc:typeName/gco:LocalName}"
             store="true" index="true"/>

      <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
      <!-- === Title === -->
      <xsl:apply-templates
        select="/gfc:FC_FeatureCatalogue/gmx:name/gco:CharacterString|
        /gfc:FC_FeatureCatalogue/gfc:name/gco:CharacterString|
        /gfc:FC_FeatureType/gfc:typeName/gco:LocalName">
        <xsl:with-param name="name" select="'title'"/>
        <xsl:with-param name="store" select="'true'"/>
      </xsl:apply-templates>

      <!-- not tokenized title for sorting -->
      <Field name="_title"
             string="{string(/gfc:FC_FeatureCatalogue/gmx:name/gco:CharacterString|
        /gfc:FC_FeatureCatalogue/gfc:name/gco:CharacterString|
        /gfc:FC_FeatureType/gfc:typeName/gco:LocalName)}"
             store="false" index="true"/>


      <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
      <!-- === Abstract === -->
      <xsl:apply-templates
        select="/gfc:FC_FeatureCatalogue/gmx:scope/gco:CharacterString|
        /gfc:FC_FeatureCatalogue/gfc:scope/gco:CharacterString|
        /gfc:FC_FeatureType/gfc:definition/gco:CharacterString">
        <xsl:with-param name="name" select="'abstract'"/>
        <xsl:with-param name="store" select="'true'"/>
      </xsl:apply-templates>

      <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
      <!-- === Version identifier === -->
      <Field name="versionIdentifier"
             string="{string(/gfc:FC_FeatureCatalogue/gmx:versionNumber/gco:CharacterString|
        /gfc:FC_FeatureCatalogue/gfc:versionNumber/gco:CharacterString)}"
             store="true" index="true"/>

      <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
      <!-- === Revision date === -->
      <xsl:for-each select="/gfc:FC_FeatureCatalogue/gmx:versionDate/gco:Date|
        /gfc:FC_FeatureCatalogue/gfc:versionDate/gco:Date">
        <Field name="revisionDate" string="{string(.)}" store="true" index="true"/>
      </xsl:for-each>

      <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
      <!-- === Metadata file identifier (GUID in GeoNetwork) === -->
      <xsl:apply-templates select="/gfc:FC_FeatureCatalogue/@uuid|/gfc:FC_FeatureType/@uuid">
        <xsl:with-param name="name" select="'fileId'"/>
      </xsl:apply-templates>

      <xsl:variable name="jsonFeatureTypes">[

      <xsl:for-each select="/gfc:FC_FeatureCatalogue/gfc:featureType">{

        "typeName" : "<xsl:value-of select="gfc:FC_FeatureType/gfc:typeName/*/text()"/>",
        "definition" :"<xsl:value-of select="gn-fn-index:json-escape(gfc:FC_FeatureType/gfc:definition/*/text())"/>",
        "code" :"<xsl:value-of select="gfc:FC_FeatureType/gfc:code/*/text()"/>",
        "isAbstract" :"<xsl:value-of select="gfc:FC_FeatureType/gfc:isAbstract/*/text()"/>",
        "aliases" : "<xsl:value-of select="gfc:FC_FeatureType/gfc:aliases/*/text()"/>",
        <!--"inheritsFrom" : "<xsl:value-of select="gfc:FC_FeatureType/gfc:inheritsFrom/*/text()"/>",
        "inheritsTo" : "<xsl:value-of select="gfc:FC_FeatureType/gfc:inheritsTo/*/text()"/>",
        "constrainedBy" : "<xsl:value-of select="gfc:FC_FeatureType/gfc:constrainedBy/*/text()"/>",
        "definitionReference" : "<xsl:value-of select="gfc:FC_FeatureType/gfc:definitionReference/*/text()"/>",-->
        <!-- Index attribute table as JSON object -->
        <xsl:variable name="attributes"
                      select="*/gfc:carrierOfCharacteristics"/>
        <xsl:if test="count($attributes) > 0">
            "attributeTable" : [
            <xsl:for-each select="$attributes">
              {"name": "<xsl:value-of select="*/gfc:memberName/*/text()"/>",
              "definition": "<xsl:value-of select="*/gfc:definition/*/text()"/>",
              "code": "<xsl:value-of select="*/gfc:code/*/text()"/>",
              "link": "<xsl:value-of select="*/gfc:code/*/@xlink:href"/>",
              "type": "<xsl:value-of select="*/gfc:valueType/gco:TypeName/gco:aName/*/text()"/>"
              <xsl:if test="*/gfc:listedValue">
                ,"values": [
                <xsl:for-each select="*/gfc:listedValue">{
                  "label": "<xsl:value-of select="*/gfc:label/*/text()"/>",
                  "code": "<xsl:value-of select="*/gfc:code/*/text()"/>",
                  "definition": "<xsl:value-of select="*/gfc:definition/*/text()"/>"}
                  <xsl:if test="position() != last()">,</xsl:if>
                </xsl:for-each>
                ]
              </xsl:if>
              }
              <xsl:if test="position() != last()">,</xsl:if>
            </xsl:for-each>
            ]
        </xsl:if>
        }
        <xsl:if test="position() != last()">,</xsl:if>
      </xsl:for-each>
      ]

      </xsl:variable>

      <Field name="featureTypes" index="true" store="true"
             string="{$jsonFeatureTypes}"/>

      <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
      <!-- === Responsible organization === -->
      <xsl:for-each select="/gfc:FC_FeatureCatalogue/gfc:producer">
        <xsl:apply-templates mode="index-contact"
                             select="gmd:CI_ResponsibleParty|*[@gco:isoType = 'gmd:CI_ResponsibleParty']">
          <xsl:with-param name="type" select="'resource'"/>
          <xsl:with-param name="fieldPrefix" select="'responsibleParty'"/>
          <xsl:with-param name="position" select="position()"/>
        </xsl:apply-templates>
      </xsl:for-each>

      <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
      <!-- === all text === -->
      <Field name="any" store="false" index="true">
        <xsl:attribute name="string">
          <xsl:value-of
            select="normalize-space(string(/gfc:FC_FeatureCatalogue|/gfc:FC_FeatureType))"/>
          <xsl:text> </xsl:text>
          <xsl:for-each select="//@codeListValue">
            <xsl:value-of select="concat(., ' ')"/>
          </xsl:for-each>
        </xsl:attribute>
      </Field>

      <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
      <!-- === Metadata type (iso19110 is used to describe attribute datasets) === -->
      <Field name="type" string="featureCatalog" store="true" index="true"/>

    </Document>
  </xsl:template>

  <!-- ========================================================================================= -->

  <!-- text element, by default indexed, not stored nor tokenized -->
  <xsl:template match="*">
    <xsl:param name="name" select="name(.)"/>
    <xsl:param name="store" select="'false'"/>
    <xsl:param name="index" select="'true'"/>

    <Field name="{$name}" string="{string(.)}" store="{$store}" index="{$index}"/>
  </xsl:template>

  <xsl:template mode="index-contact" match="gmd:CI_ResponsibleParty|*[@gco:isoType = 'gmd:CI_ResponsibleParty']">
    <xsl:param name="type"/>
    <xsl:param name="fieldPrefix"/>
    <xsl:param name="position" select="'0'"/>

    <xsl:variable name="orgName" select="gmd:organisationName/(gco:CharacterString|gmx:Anchor)"/>

    <Field name="orgName" string="{string($orgName)}" store="true" index="true"/>
    <Field name="orgNameTree" string="{string($orgName)}" store="true" index="true"/>

    <xsl:variable name="uuid" select="@uuid"/>
    <xsl:variable name="role" select="gmd:role/*/@codeListValue"/>
    <xsl:variable name="roleTranslation"
                  select="util:getCodelistTranslation('gmd:CI_RoleCode', string($role), 'en')"/>
    <xsl:variable name="logo" select=".//gmx:FileName/@src"/>
    <xsl:variable name="email"
                  select="gmd:contactInfo/*/gmd:address/*/gmd:electronicMailAddress/gco:CharacterString"/>
    <xsl:variable name="phone"
                  select="gmd:contactInfo/*/gmd:phone/*/gmd:voice[normalize-space(.) != '']/*/text()"/>
    <xsl:variable name="individualName"
                  select="gmd:individualName/gco:CharacterString/text()"/>
    <xsl:variable name="positionName"
                  select="gmd:positionName/gco:CharacterString/text()"/>
    <xsl:variable name="address" select="string-join(gmd:contactInfo/*/gmd:address/*/(
                                        gmd:deliveryPoint|gmd:postalCode|gmd:city|
                                        gmd:administrativeArea|gmd:country)/gco:CharacterString/text(), ', ')"/>

    <Field name="{$fieldPrefix}"
           string="{concat($roleTranslation, '|', $type,'|',
                             $orgName, '|',
                             $logo, '|',
                             string-join($email, ','), '|',
                             $individualName, '|',
                             $positionName, '|',
                             $address, '|',
                             string-join($phone, ','), '|',
                             $uuid, '|',
                             $position)}"
           store="true" index="false"/>

    <xsl:for-each select="$email">
      <Field name="{$fieldPrefix}Email" string="{string(.)}" store="true" index="true"/>
      <Field name="{$fieldPrefix}RoleAndEmail" string="{$role}|{string(.)}" store="true" index="true"/>
    </xsl:for-each>
    <xsl:for-each select="@uuid">
      <Field name="{$fieldPrefix}Uuid" string="{string(.)}" store="true" index="true"/>
      <Field name="{$fieldPrefix}RoleAndUuid" string="{$role}|{string(.)}" store="true" index="true"/>
    </xsl:for-each>
  </xsl:template>

</xsl:stylesheet>
