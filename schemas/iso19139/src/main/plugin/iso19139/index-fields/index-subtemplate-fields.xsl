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

<xsl:stylesheet version="2.0" xmlns:gmd="http://www.isotc211.org/2005/gmd"
  xmlns:gco="http://www.isotc211.org/2005/gco" xmlns:gml="http://www.opengis.net/gml"
  xmlns:srv="http://www.isotc211.org/2005/srv" xmlns:geonet="http://www.fao.org/geonetwork"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:gmx="http://www.isotc211.org/2005/gmx"
  xmlns:skos="http://www.w3.org/2004/02/skos/core#">

  <!-- Subtemplate indexing 
  
  Add the [count(ancestor::node()) =  1] to only match element at the root of the document.
  This is the method to identify a subtemplate.
  -->
  <xsl:template mode="index" match="gmd:CI_ResponsibleParty[count(ancestor::node()) =  1]">

    <xsl:variable name="org"
                  select="normalize-space(gmd:organisationName/gco:CharacterString)"/>
    <xsl:variable name="name"
                  select="normalize-space(gmd:individualName/gco:CharacterString)"/>
    <Field name="_title"
      string="{if ($name != '') then concat($org, ' (', $name, ')') else $org}"
      store="true" index="true"/>
    <Field name="orgName" string="{$org}" store="true" index="true"/>
    <Field name="orgNameTree" string="{$org}" store="true" index="true"/>
    <xsl:for-each select="gmd:contactInfo/*/gmd:address/*/gmd:electronicMailAddress/gco:CharacterString">
      <Field name="email" string="{.}" store="true" index="true"/>
    </xsl:for-each>
    <xsl:call-template name="subtemplate-common-fields"/>
  </xsl:template>


  <xsl:template mode="index" match="gmd:MD_Distribution[count(ancestor::node()) =  1]">
    <Field name="_title"
      string="{string-join(gmd:transferOptions/gmd:MD_DigitalTransferOptions/
        gmd:onLine/gmd:CI_OnlineResource/gmd:linkage/gmd:URL, ', ')}"
      store="true" index="true"/>
    
    <xsl:call-template name="subtemplate-common-fields"/>
  </xsl:template>


  <xsl:template mode="index" match="gmd:CI_OnlineResource[count(ancestor::node()) =  1]">
    <Field name="_title"
           string="{gmd:linkage/gmd:URL}"
           store="true" index="true"/>

    <xsl:call-template name="subtemplate-common-fields"/>
  </xsl:template>

  <xsl:template mode="index" match="gmd:EX_Extent[count(ancestor::node()) =  1]">
    <Field name="_title"
      string="{gmd:description/gco:CharacterString}"
      store="true" index="true"/>
    
    <xsl:call-template name="subtemplate-common-fields"/>
  </xsl:template>

  <xsl:template name="subtemplate-common-fields">
    <Field name="any" string="{normalize-space(string(.))}" store="false" index="true"/>
    <Field name="_root" string="{name(.)}" store="true" index="true"/>
  </xsl:template>

</xsl:stylesheet>
