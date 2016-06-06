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
                xmlns:gco="http://www.isotc211.org/2005/gco"
                xmlns:gmd="http://www.isotc211.org/2005/gmd"
                xmlns:saxon="http://saxon.sf.net/"
                version="2.0"
                extension-element-prefixes="saxon"
                exclude-result-prefixes="#all">


  <!-- Example of replacements parameter:

     <replacements>
      <replacement>
        <field>id.contact.individualName</field>
        <searchValue>John Doe</searchValue>
        <replaceValue>Jennifer Smith</replaceValue>
      </replacement>
      <replacement>
        <field>id.contact.organisationName</field>
        <searchValue>Acme</searchValue>
        <replaceValue>New Acme</replaceValue>
      </replacement>
    </replacements>
  -->
  <xsl:param name="replacements"/>

  <!-- Flags http://www.w3.org/TR/xpath-functions/#flags -->
  <xsl:variable name="flags"
                select="$replacements/replacements/flags"/>


  <xsl:template match="@*|node()">
    <!--
    Element key is generic and based on ancestors local name.
    Class element are ignored - maybe this only works for ISO* standards.
    For example, gmd:abstract is identified by identification.abstract.
    -->
    <xsl:variable name="elementKey"
                  select="concat('iso19139.',
                            string-join(
                              ./ancestor-or-self::*[count(ancestor::*) mod 2 != 0 ]/
                              local-name(), '.')
                            )"/>

    <!--
    A field match a replacer when its key match a field expression
    eg.
    * iso19139\\.*\\.individualName will replace all individualName
    -->
    <xsl:variable name="hasReplacement"
                  select="count($replacements/replacements/
                            replacement[
                              matches($elementKey, field) and
                              string(searchValue) != '']) > 0"/>
    <xsl:choose>
      <xsl:when test="$hasReplacement">
        <xsl:call-template name="replaceField">
          <xsl:with-param name="fieldId" select="$elementKey"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:copy>
          <xsl:apply-templates select="@*|node()" mode="copy"/>
        </xsl:copy>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="geonet:*" priority="2"/>
  <xsl:template match="geonet:*" priority="2" mode="copy"/>
  <xsl:template match="@*|node()" mode="copy">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>


  <!--
    Field replacement template.
    Checks if a replacement for the field is defined to apply it,
    otherwise copies the field value.
  -->
  <xsl:template name="replaceField">
    <xsl:param name="fieldId"/>

    <xsl:copy>
      <xsl:copy-of select="@*"/>
      <xsl:choose>
        <!-- gmd:URL -->
        <xsl:when test="name() = 'gmd:URL'">
          <xsl:call-template name="replaceValueForField">
            <xsl:with-param name="fieldId" select="$fieldId"/>
            <xsl:with-param name="value" select="."/>
          </xsl:call-template>
        </xsl:when>

        <!-- Fields with gco:CharacterString -->
        <xsl:when test="gco:CharacterString or gmd:PT_FreeText">

          <xsl:for-each select="gco:CharacterString">
            <xsl:copy>
              <xsl:call-template name="replaceValueForField">
                <xsl:with-param name="fieldId" select="$fieldId"/>
                <xsl:with-param name="value" select="."/>
              </xsl:call-template>
            </xsl:copy>
          </xsl:for-each>

          <xsl:for-each select="gmd:PT_FreeText">
            <xsl:copy>
              <xsl:copy-of select="@*"/>
              <xsl:for-each select="gmd:textGroup">
                <xsl:copy>
                  <xsl:copy-of select="@*"/>
                  <xsl:for-each select="gmd:LocalisedCharacterString">
                    <gmd:LocalisedCharacterString locale="{@locale}">
                      <xsl:call-template name="replaceValueForField">
                        <xsl:with-param name="fieldId" select="$fieldId"/>
                        <xsl:with-param name="value" select="."/>
                      </xsl:call-template>
                    </gmd:LocalisedCharacterString>
                  </xsl:for-each>
                </xsl:copy>
              </xsl:for-each>
            </xsl:copy>
          </xsl:for-each>
        </xsl:when>

        <!-- Other type, replace the value -->
        <xsl:otherwise>
          <!--<xsl:copy-of select="saxon:parse($contactAsXML)"/>-->
          <xsl:call-template name="replaceValueForField">
            <xsl:with-param name="fieldId" select="$fieldId"/>
            <xsl:with-param name="value" select="."/>
          </xsl:call-template>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:copy>
  </xsl:template>


  <xsl:template name="replaceValueForField">
    <xsl:param name="fieldId"/>
    <xsl:param name="value"/>

    <xsl:variable name="replacementDetails"
                  select="$replacements/replacements/
                            replacement[matches($fieldId, field)]"/>


    <!-- Match all replacements define for this type of fields. -->
    <xsl:variable name="changes">
      <xsl:for-each select="$replacementDetails">

        <!-- If the value match the search value -->
        <xsl:if test="if ($flags)
                            then matches(
                              $value,
                              searchValue,
                              $flags)
                            else matches(
                              $value,
                              searchValue)">

          <!-- Replace content -->
          <xsl:variable name="newValue"
                        select="if ($flags)
                          then replace(
                            $value,
                            searchValue,
                            replaceValue,
                            $flags)
                          else replace(
                            $value,
                            searchValue,
                            replaceValue)"/>

          <change original="{$value}" new="{$newValue}"/>
        </xsl:if>
      </xsl:for-each>
    </xsl:variable>

    <!-- Report the changes to the record.
    If more than one match, only the first one is take into account. -->
    <xsl:choose>
      <xsl:when test="$changes/change[@original != @new]">
        <xsl:variable name="newValue" select="$changes/change[1]/@new/string()"/>
        <xsl:attribute name="geonet:change" select="$fieldId"/>
        <xsl:attribute name="geonet:original" select="$value"/>
        <xsl:attribute name="geonet:new" select="$newValue"/>
        <xsl:value-of select="$newValue"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$value"/>
      </xsl:otherwise>
    </xsl:choose>


  </xsl:template>

</xsl:stylesheet>
