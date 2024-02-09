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

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:gmd="http://www.isotc211.org/2005/gmd"
                xmlns:srv="http://www.isotc211.org/2005/srv"
                xmlns:gco="http://www.isotc211.org/2005/gco"
                xmlns:geonet="http://www.fao.org/geonetwork"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:digestUtils="java:org.apache.commons.codec.digest.DigestUtils"
                xmlns:exslt="http://exslt.org/common"
                exclude-result-prefixes="#all"
                version="2.0">

  <!--
      Usage:
        thumbnail-from-url-add?thumbnail_url=http://geonetwork.org/thumbnails/image.png
    -->

  <!-- Thumbnail base url (mandatory) -->
  <xsl:param name="thumbnail_url"/>
  <!-- Element to use for the file name. -->
  <xsl:param name="thumbnail_desc" select="''"/>
  <xsl:param name="thumbnail_type" select="''"/>

  <!-- Target element to update.
    updateKey is used to identify the resource name to be updated - it is for backwards compatibility.  Will not be used if resourceHash is set.
              The key is based on the concatenation of URL+Name
    resourceHash is hash value of the object to be removed which will ensure the correct value is removed. It will override the usage of updateKey
    resourceIdx is the index location of the object to be removed - can be used when duplicate entries exists to ensure the correct one is removed.
-->
  <xsl:param name="updateKey" select="''"/>
  <xsl:param name="resourceHash" select="''"/>
  <xsl:param name="resourceIdx" select="''"/>

  <xsl:variable name="update_flag">
    <xsl:value-of select="boolean($updateKey != '' or $resourceHash != '' or $resourceIdx != '')"/>
  </xsl:variable>

  <!--  Add new gmd:graphicOverview -->
  <xsl:template match="gmd:identificationInfo/*[$update_flag = false()]">
    <xsl:copy>
      <xsl:copy-of select="@*"/>
      <xsl:apply-templates select="gmd:citation"/>
      <xsl:apply-templates select="gmd:abstract"/>
      <xsl:apply-templates select="gmd:purpose"/>
      <xsl:apply-templates select="gmd:credit"/>
      <xsl:apply-templates select="gmd:status"/>
      <xsl:apply-templates select="gmd:pointOfContact"/>
      <xsl:apply-templates select="gmd:resourceMaintenance"/>

      <xsl:call-template name="fill"/>

      <xsl:apply-templates select="gmd:graphicOverview"/>

      <xsl:apply-templates select="gmd:resourceFormat"/>
      <xsl:apply-templates select="gmd:descriptiveKeywords"/>
      <xsl:apply-templates select="gmd:resourceSpecificUsage"/>
      <xsl:apply-templates select="gmd:resourceConstraints"/>
      <xsl:apply-templates select="gmd:aggregationInfo"/>
      <xsl:apply-templates select="gmd:spatialRepresentationType"/>
      <xsl:apply-templates select="gmd:spatialResolution"/>
      <xsl:apply-templates select="gmd:language"/>
      <xsl:apply-templates select="gmd:characterSet"/>
      <xsl:apply-templates select="gmd:topicCategory"/>
      <xsl:apply-templates select="gmd:environmentDescription"/>
      <xsl:apply-templates select="gmd:extent"/>
      <xsl:apply-templates select="gmd:supplementalInformation"/>

      <xsl:apply-templates select="srv:*"/>

      <xsl:apply-templates select="*[namespace-uri()!='http://www.isotc211.org/2005/gmd' and
                                     namespace-uri()!='http://www.isotc211.org/2005/srv']"/>
    </xsl:copy>
  </xsl:template>

  <!-- Updating the gmd:graphicOverview based on update parameters -->
  <!-- Note: first part of the match needs to match the xsl:for-each select from extract-relations.xsl in order to get the position() to match -->
  <xsl:template
    priority="2"
    match="*//gmd:graphicOverview
         [$resourceIdx = '' or position() = xs:integer($resourceIdx)]
         [    ($resourceHash != '' or ($updateKey != '' and normalize-space($updateKey) = concat(
                           */gmd:fileName/gco:CharacterString,
                           */gmd:fileName/gmd:PT_FreeText/gmd:textGroup/gmd:LocalisedCharacterString[@locale = '#DE'],
                           */gmd:fileDescription/gmd:PT_FreeText/gmd:textGroup/gmd:LocalisedCharacterString[@locale = '#DE'],
                           */gmd:fileDescription/gco:CharacterString)))
          and ($resourceHash = '' or digestUtils:md5Hex(string(exslt:node-set(.))) = $resourceHash)]">
    <xsl:call-template name="fill"/>
  </xsl:template>

  <!-- TMP TO REMOVE when gco:characterString is added in multilingual elements
    <xsl:template match="gmd:graphicOverview[concat(
                            */gmd:fileName/gmd:PT_FreeText/gmd:textGroup/gmd:LocalisedCharacterString[@locale = '#DE'],
                            */gmd:fileDescription/gmd:PT_FreeText/gmd:textGroup/gmd:LocalisedCharacterString[@locale = '#DE']) = normalize-space($updateKey)]">
      <xsl:call-template name="fill"/>
    </xsl:template>
  -->

  <xsl:template name="fill">
    <xsl:if test="$thumbnail_url != ''">

      <xsl:variable name="useOnlyPTFreeText"
                    select="count(//*[gmd:PT_FreeText and
                                      not(gco:CharacterString)]) > 0"/>
      <xsl:variable name="separator" select="'\|'"/>
      <xsl:variable name="mainLang"
                    select="//gmd:MD_Metadata/gmd:language/gmd:LanguageCode/@codeListValue"/>

      <gmd:graphicOverview>
        <gmd:MD_BrowseGraphic>
          <gmd:fileName>
            <xsl:choose>

              <!--Multilingual-->
              <xsl:when test="contains($thumbnail_url, '|')">
                <xsl:for-each select="tokenize($thumbnail_url, $separator)">
                  <xsl:variable name="nameLang"
                                select="substring-before(., '#')"></xsl:variable>
                  <xsl:variable name="nameValue"
                                select="substring-after(., '#')"></xsl:variable>

                  <xsl:if test="$useOnlyPTFreeText = false() and $nameLang = $mainLang">
                    <gco:CharacterString>
                      <xsl:value-of select="$nameValue"/>
                    </gco:CharacterString>
                  </xsl:if>
                </xsl:for-each>

                <gmd:PT_FreeText>
                  <xsl:for-each select="tokenize($thumbnail_url, $separator)">
                    <xsl:variable name="nameLang"
                                  select="substring-before(., '#')"></xsl:variable>
                    <xsl:variable name="nameValue"
                                  select="substring-after(., '#')"></xsl:variable>

                    <xsl:if test="$useOnlyPTFreeText = true() or
                                    $nameLang != $mainLang">
                      <gmd:textGroup>
                        <gmd:LocalisedCharacterString locale="{concat('#', $nameLang)}">
                          <xsl:value-of select="$nameValue"/>
                        </gmd:LocalisedCharacterString>
                      </gmd:textGroup>
                    </xsl:if>
                  </xsl:for-each>
                </gmd:PT_FreeText>
              </xsl:when>
              <xsl:otherwise>
                <gco:CharacterString>
                  <xsl:value-of select="$thumbnail_url"/>
                </gco:CharacterString>
              </xsl:otherwise>
            </xsl:choose>
          </gmd:fileName>

          <xsl:if test="$thumbnail_desc!=''">
            <gmd:fileDescription>

              <xsl:choose>

                <!--Multilingual-->
                <xsl:when test="contains($thumbnail_desc, '|')">
                  <xsl:for-each select="tokenize($thumbnail_desc, $separator)">
                    <xsl:variable name="nameLang"
                                  select="substring-before(., '#')"></xsl:variable>
                    <xsl:variable name="nameValue"
                                  select="substring-after(., '#')"></xsl:variable>

                    <xsl:if test="$useOnlyPTFreeText = false() and $nameLang = $mainLang">
                      <gco:CharacterString>
                        <xsl:value-of select="$nameValue"/>
                      </gco:CharacterString>
                    </xsl:if>
                  </xsl:for-each>

                  <gmd:PT_FreeText>
                    <xsl:for-each select="tokenize($thumbnail_desc, $separator)">
                      <xsl:variable name="nameLang"
                                    select="substring-before(., '#')"></xsl:variable>
                      <xsl:variable name="nameValue"
                                    select="substring-after(., '#')"></xsl:variable>

                      <xsl:if test="$useOnlyPTFreeText = true() or
                                    $nameLang != $mainLang">
                        <gmd:textGroup>
                          <gmd:LocalisedCharacterString locale="{concat('#', $nameLang)}">
                            <xsl:value-of select="$nameValue"/>
                          </gmd:LocalisedCharacterString>
                        </gmd:textGroup>
                      </xsl:if>
                    </xsl:for-each>
                  </gmd:PT_FreeText>
                </xsl:when>
                <xsl:otherwise>
                  <gco:CharacterString>
                    <xsl:value-of select="$thumbnail_desc"/>
                  </gco:CharacterString>
                </xsl:otherwise>
              </xsl:choose>
            </gmd:fileDescription>
          </xsl:if>
          <xsl:if test="$thumbnail_type!=''">
            <gmd:fileType>
              <gco:CharacterString>
                <xsl:value-of select="$thumbnail_type"/>
              </gco:CharacterString>
            </gmd:fileType>
          </xsl:if>
        </gmd:MD_BrowseGraphic>
      </gmd:graphicOverview>
    </xsl:if>
  </xsl:template>


  <!-- Do a copy of every nodes and attributes -->
  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

  <!-- Always remove geonet:* elements. -->
  <xsl:template match="geonet:*" priority="2"/>

</xsl:stylesheet>
