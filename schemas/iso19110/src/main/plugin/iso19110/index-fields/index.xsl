<?xml version="1.0" encoding="UTF-8" ?>
<!--
  ~ Copyright (C) 2001-2020 Food and Agriculture Organization of the
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

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:gfc="http://www.isotc211.org/2005/gfc"
                xmlns:gmd="http://www.isotc211.org/2005/gmd"
                xmlns:gmx="http://www.isotc211.org/2005/gmx"
                xmlns:gco="http://www.isotc211.org/2005/gco"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:util="java:org.fao.geonet.util.XslUtil"
                xmlns:date-util="java:org.fao.geonet.utils.DateUtil"
                xmlns:gn-fn-index="http://geonetwork-opensource.org/xsl/functions/index"
                version="2.0">

  <xsl:import href="common/index-utils.xsl"/>

  <xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes"/>

  <xsl:template match="/">
    <doc>
      <docType>metadata</docType>
      <resourceType>featureCatalog</resourceType>

      <xsl:for-each select="/gfc:FC_FeatureCatalogue/gmx:name/gco:CharacterString|
      /gfc:FC_FeatureCatalogue/gfc:name/gco:CharacterString|
      /gfc:FC_FeatureType/gfc:typeName/gco:LocalName">

        <xsl:variable name="resourceTitleObject" as="xs:string"
                      select="concat('{',
                          $doubleQuote, 'default', $doubleQuote, ':',
                          $doubleQuote, gn-fn-index:json-escape(.) ,$doubleQuote,
                        '}')"/>

        <xsl:copy-of select="gn-fn-index:add-object-field(
                               'resourceTitleObject', $resourceTitleObject)"/>
      </xsl:for-each>

      <resourceAbstract>
        <xsl:value-of select="/gfc:FC_FeatureCatalogue/gmx:scope/gco:CharacterString|
        /gfc:FC_FeatureCatalogue/gfc:scope/gco:CharacterString|
        /gfc:FC_FeatureType/gfc:definition/gco:CharacterString"/>
      </resourceAbstract>

      <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
      <!-- === Version identifier === -->
      <versionIdentifier>
        <xsl:value-of select="string(/gfc:FC_FeatureCatalogue/gmx:versionNumber/gco:CharacterString|
        /gfc:FC_FeatureCatalogue/gfc:versionNumber/gco:CharacterString)"/>
      </versionIdentifier>

      <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
      <!-- === Responsible organization === -->
      <xsl:for-each select="/gfc:FC_FeatureCatalogue/gfc:producer">
        <xsl:apply-templates mode="index-contact"
                             select=".">
          <xsl:with-param name="type" select="'resource'"/>
          <xsl:with-param name="fieldPrefix" select="'responsibleParty'"/>
          <xsl:with-param name="position" select="position()"/>
        </xsl:apply-templates>
      </xsl:for-each>

      <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
      <!-- === Revision date === -->
      <xsl:for-each select="/gfc:FC_FeatureCatalogue/gmx:versionDate/gco:Date|
        /gfc:FC_FeatureCatalogue/gfc:versionDate/gco:Date">
        <revisionDate><xsl:value-of select="date-util:convertToISOZuluDateTime(string(.))"/></revisionDate>
      </xsl:for-each>

      <xsl:variable name="jsonFeatureTypes">[
        <xsl:for-each select=".//gfc:featureType">{
          "typeName" : "<xsl:value-of select="gn-fn-index:json-escape(gfc:FC_FeatureType/gfc:typeName/*/text())"/>",
          "definition" :"<xsl:value-of select="gn-fn-index:json-escape(gfc:FC_FeatureType/gfc:definition/*/text())"/>",
          "code" :"<xsl:value-of select="gn-fn-index:json-escape(gfc:FC_FeatureType/gfc:code/*/text())"/>",
          "isAbstract" :"<xsl:value-of select="gfc:FC_FeatureType/gfc:isAbstract/*/text()"/>",
          "aliases" : "<xsl:value-of select="gn-fn-index:json-escape(gfc:FC_FeatureType/gfc:aliases/*/text())"/>"
          <!--"inheritsFrom" : "<xsl:value-of select="gfc:FC_FeatureType/gfc:inheritsFrom/*/text()"/>",
          "inheritsTo" : "<xsl:value-of select="gfc:FC_FeatureType/gfc:inheritsTo/*/text()"/>",
          "constrainedBy" : "<xsl:value-of select="gfc:FC_FeatureType/gfc:constrainedBy/*/text()"/>",
          "definitionReference" : "<xsl:value-of select="gfc:FC_FeatureType/gfc:definitionReference/*/text()"/>",-->
          <!-- Index attribute table as JSON object -->
          <xsl:variable name="attributes"
                        select="*/gfc:carrierOfCharacteristics"/>
          <xsl:if test="count($attributes) > 0">
            ,"attributeTable" : [
            <xsl:for-each select="$attributes">
              {"name": "<xsl:value-of select="gn-fn-index:json-escape(*/gfc:memberName/*/text())"/>",
              "definition": "<xsl:value-of select="gn-fn-index:json-escape(*/gfc:definition/*/text())"/>",
              "code": "<xsl:value-of select="gn-fn-index:json-escape(*/gfc:code/*/text())"/>",
              "link": "<xsl:value-of select="*/gfc:code/*/@xlink:href"/>",
              "type": "<xsl:value-of select="*/gfc:valueType/gco:TypeName/gco:aName/*/text()"/>"
              <xsl:if test="*/gfc:listedValue">
                ,"values": [
                <xsl:for-each select="*/gfc:listedValue">{
                  "label": "<xsl:value-of select="gn-fn-index:json-escape(*/gfc:label/*/text())"/>",
                  "code": "<xsl:value-of select="gn-fn-index:json-escape(*/gfc:code/*/text())"/>",
                  "definition": "<xsl:value-of select="gn-fn-index:json-escape(*/gfc:definition/*/text())"/>"}
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

      <featureTypes type="object">
        <xsl:value-of select="$jsonFeatureTypes"/>
      </featureTypes>
    </doc>
  </xsl:template>

  <xsl:template mode="index-contact" match="*[gmd:CI_ResponsibleParty]">
    <xsl:param name="fieldSuffix" select="''" as="xs:string"/>
    <xsl:param name="languages" as="node()?"/>

    <!-- Select the first child which should be a CI_ResponsibleParty.
    Some records contains more than one CI_ResponsibleParty which is
    not valid and they will be ignored.
     Same for organisationName eg. de:b86a8604-bf78-480f-a5a8-8edff5586679 -->
    <xsl:variable name="organisationName"
                  select="*[1]/gmd:organisationName[1]"
                  as="node()?"/>
    <xsl:variable name="uuid" select="@uuid"/>

    <xsl:variable name="role"
                  select="replace(*[1]/gmd:role/*/@codeListValue, ' ', '')"
                  as="xs:string?"/>
    <xsl:variable name="logo" select=".//gmx:FileName/@src"/>
    <xsl:variable name="website" select=".//gmd:onlineResource/*/gmd:linkage/gmd:URL"/>
    <xsl:variable name="email"
                  select="*[1]/gmd:contactInfo/*/gmd:address/*/gmd:electronicMailAddress/gco:CharacterString"/>
    <xsl:variable name="phone"
                  select="*[1]/gmd:contactInfo/*/gmd:phone/*/gmd:voice[normalize-space(.) != '']/*/text()"/>
    <xsl:variable name="individualName"
                  select="*[1]/gmd:individualName/gco:CharacterString/text()"/>
    <xsl:variable name="positionName"
                  select="*[1]/gmd:positionName/gco:CharacterString/text()"/>
    <xsl:variable name="address" select="string-join(*[1]/gmd:contactInfo/*/gmd:address/*/(
                                        gmd:deliveryPoint|gmd:postalCode|gmd:city|
                                        gmd:administrativeArea|gmd:country)/gco:CharacterString/text(), ', ')"/>

    <xsl:variable name="roleField"
                  select="concat(replace($role, '[^a-zA-Z0-9-]', ''),
                                 'Org', $fieldSuffix)"/>
    <xsl:variable name="orgField"
                  select="concat('Org', $fieldSuffix)"/>


    <xsl:if test="normalize-space($organisationName) != ''">
      <xsl:copy-of select="gn-fn-index:add-multilingual-field(
                            $orgField, $organisationName, $languages)"/>
      <xsl:copy-of select="gn-fn-index:add-multilingual-field(
                            $roleField, $organisationName, $languages)"/>
    </xsl:if>
    <xsl:element name="contact{$fieldSuffix}">
      <xsl:attribute name="type" select="'object'"/>{
      <xsl:if test="$organisationName">
        "organisationObject": <xsl:value-of select="gn-fn-index:add-multilingual-field(
                              'organisation', $organisationName, $languages)"/>,
      </xsl:if>
      "role":"<xsl:value-of select="$role"/>",
      "email":"<xsl:value-of select="gn-fn-index:json-escape($email[1])"/>",
      "website":"<xsl:value-of select="$website"/>",
      "logo":"<xsl:value-of select="$logo"/>",
      "individual":"<xsl:value-of select="gn-fn-index:json-escape($individualName)"/>",
      "position":"<xsl:value-of select="gn-fn-index:json-escape($positionName)"/>",
      "phone":"<xsl:value-of select="gn-fn-index:json-escape($phone[1])"/>",
      "address":"<xsl:value-of select="gn-fn-index:json-escape($address)"/>"
      }
    </xsl:element>
  </xsl:template>
</xsl:stylesheet>
