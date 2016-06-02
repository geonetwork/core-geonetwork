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

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:gmd="http://www.isotc211.org/2005/gmd"
                xmlns:gco="http://www.isotc211.org/2005/gco"
                xmlns:geonet="http://www.fao.org/geonetwork"
                version="2.0"
                exclude-result-prefixes="gmd gco geonet">

  <!-- Compute title for all type of subtemplates. If none defined,
  the title from the metadata title column is used. -->
  <xsl:template name="iso19139-subtemplate">

    <xsl:variable name="subTemplateTitle">
      <xsl:apply-templates mode="iso19139-subtemplate" select="."/>
    </xsl:variable>

    <title>
      <xsl:choose>
        <xsl:when test="normalize-space($subTemplateTitle)!=''">
          <xsl:value-of select="$subTemplateTitle"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="geonet:info/title"/>
        </xsl:otherwise>
      </xsl:choose>
    </title>
  </xsl:template>

  <!-- Subtemplate mode -->
  <xsl:template mode="iso19139-subtemplate" match="gmd:CI_ResponsibleParty">
    <!-- TODO : multilingual subtemplate are not supported. There is
      no gmd:language element or gmd:locales -->
    <xsl:variable name="langId">
      <xsl:call-template name="getLangId">
        <xsl:with-param name="langGui" select="/root/gui/language"/>
        <xsl:with-param name="md" select="."/>
      </xsl:call-template>
    </xsl:variable>

    <xsl:apply-templates mode="localised" select="gmd:organisationName">
      <xsl:with-param name="langId" select="$langId"/>
    </xsl:apply-templates>

    <!-- Concatenate email or name or position -->
    <xsl:choose>
      <xsl:when
        test="count(gmd:contactInfo/gmd:CI_Contact/gmd:address/gmd:CI_Address/gmd:electronicMailAddress/gco:CharacterString[normalize-space(.)!='']) > 0">
        <xsl:text> > </xsl:text>
        <xsl:value-of
          select="string-join(gmd:contactInfo/gmd:CI_Contact/gmd:address/gmd:CI_Address/gmd:electronicMailAddress/gco:CharacterString, ',')"/>
      </xsl:when>
      <xsl:when test="normalize-space(gmd:individualName/gco:CharacterString)!=''">
        <xsl:text> > </xsl:text>
        <xsl:apply-templates mode="localised" select="gmd:individualName">
          <xsl:with-param name="langId" select="$langId"/>
        </xsl:apply-templates>
      </xsl:when>
      <xsl:when test="normalize-space(gmd:positionName/gco:CharacterString)!=''">
        <xsl:text> > </xsl:text>
        <xsl:apply-templates mode="localised" select="gmd:positionName">
          <xsl:with-param name="langId" select="$langId"/>
        </xsl:apply-templates>
      </xsl:when>
      <xsl:otherwise>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template mode="iso19139-subtemplate" match="gmd:EX_Extent">
    <!-- TODO : multilingual subtemplate are not supported. There is
      no gmd:language element or gmd:locales -->
    <xsl:variable name="langId">
      <xsl:call-template name="getLangId">
        <xsl:with-param name="langGui" select="/root/gui/language"/>
        <xsl:with-param name="md" select="."/>
      </xsl:call-template>
    </xsl:variable>

    <xsl:apply-templates mode="localised" select="gmd:description">
      <xsl:with-param name="langId" select="$langId"/>
    </xsl:apply-templates>
  </xsl:template>

  <xsl:template mode="iso19139-subtemplate" match="gmd:MD_Keywords">
    <xsl:variable name="langId">
      <xsl:call-template name="getLangId">
        <xsl:with-param name="langGui" select="/root/gui/language"/>
        <xsl:with-param name="md" select="."/>
      </xsl:call-template>
    </xsl:variable>

    <xsl:for-each select="gmd:keyword">
      <xsl:apply-templates mode="localised" select=".">
        <xsl:with-param name="langId" select="$langId"/>
      </xsl:apply-templates>
      <xsl:if test="position() != last()">
        <xsl:text>, </xsl:text>
      </xsl:if>
    </xsl:for-each>
  </xsl:template>

  <xsl:template mode="iso19139-subtemplate" match="gmd:MD_Distribution">
    <xsl:value-of
      select="string-join(gmd:transferOptions/gmd:MD_DigitalTransferOptions/gmd:onLine/gmd:CI_OnlineResource/gmd:linkage/gmd:URL, ' ,')"
    />
  </xsl:template>

  <xsl:template mode="iso19139-subtemplate" match="gmd:MD_LegalConstraints">
    <xsl:value-of
      select="if (gmd:useLimitation) then gmd:useLimitation/* else gmd:otherConstraints/*"
    />
  </xsl:template>


  <xsl:template mode="iso19139-subtemplate" match="*"/>

</xsl:stylesheet>
