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

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:gmd="http://www.isotc211.org/2005/gmd"
                xmlns:gts="http://www.isotc211.org/2005/gts"
                xmlns:gco="http://www.isotc211.org/2005/gco"
                xmlns:gmx="http://www.isotc211.org/2005/gmx"
                xmlns:srv="http://www.isotc211.org/2005/srv"
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                xmlns:gml="http://www.opengis.net/gml"
                xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:geonet="http://www.fao.org/geonetwork"
                xmlns:exslt="http://exslt.org/common" xmlns:saxon="http://saxon.sf.net/"
                version="2.0" extension-element-prefixes="saxon"
                exclude-result-prefixes="gmx xsi gmd gco gml gts srv xlink exslt geonet">


  <xsl:template name="view-with-header-iso19139">
    <xsl:param name="tabs"/>

    <xsl:call-template name="md-content">
      <xsl:with-param name="title">
        <xsl:apply-templates mode="localised"
                             select="gmd:identificationInfo/*/gmd:citation/gmd:CI_Citation/gmd:title">
          <xsl:with-param name="langId" select="$langId"/>
        </xsl:apply-templates>
      </xsl:with-param>
      <xsl:with-param name="exportButton"/>
      <xsl:with-param name="abstract">
        <xsl:call-template name="addLineBreaksAndHyperlinks">
          <xsl:with-param name="txt">
            <xsl:apply-templates mode="localised" select="gmd:identificationInfo/*/gmd:abstract">
              <xsl:with-param name="langId" select="$langId"/>
            </xsl:apply-templates>
          </xsl:with-param>
        </xsl:call-template>
      </xsl:with-param>
      <xsl:with-param name="logo">
        <img src="../../images/logos/{//geonet:info/source}.gif" alt="logo" class="logo"/>
      </xsl:with-param>
      <xsl:with-param name="relatedResources">
        <xsl:apply-templates mode="relatedResources"
                             select="."
        />
      </xsl:with-param>
      <xsl:with-param name="tabs" select="$tabs"/>

    </xsl:call-template>


  </xsl:template>

  <!-- View templates are available only in view mode and does not provide editing
  capabilities. Template MUST start with "view". -->
  <!-- ===================================================================== -->
  <!-- iso19139-simple -->
  <xsl:template name="metadata-iso19139view-simple" match="metadata-iso19139view-simple">
    <!--<xsl:apply-templates mode="iso19139-simple" select="*"/>-->

    <xsl:call-template name="view-with-header-iso19139">
      <xsl:with-param name="tabs">
        <xsl:call-template name="complexElementSimpleGui">
          <xsl:with-param name="title"
                          select="/root/gui/schemas/iso19139/strings/understandResource"/>
          <xsl:with-param name="content">
            <xsl:apply-templates mode="block"
                                 select="
                gmd:identificationInfo/*/gmd:citation/gmd:CI_Citation/gmd:date[1]
                |gmd:identificationInfo/*/gmd:extent/*/gmd:temporalElement
                "/>
            <xsl:apply-templates mode="block"
                                 select="
                  gmd:identificationInfo/*/gmd:language
                  |gmd:identificationInfo/*/gmd:citation/gmd:CI_Citation/gmd:edition
                  |gmd:identificationInfo/*/gmd:topicCategory
                  |gmd:identificationInfo/*/gmd:descriptiveKeywords
                  |gmd:identificationInfo/*/gmd:graphicOverview[1]
                  |gmd:identificationInfo/*/gmd:extent/gmd:EX_Extent/gmd:geographicElement
                  "/>
            <xsl:apply-templates mode="block"
                                 select="gmd:referenceSystemInfo/*/gmd:referenceSystemIdentifier"/>
          </xsl:with-param>
        </xsl:call-template>


        <xsl:call-template name="complexElementSimpleGui">
          <xsl:with-param name="title" select="/root/gui/schemas/iso19139/strings/contactInfo"/>
          <xsl:with-param name="content">
            <xsl:apply-templates mode="block"
                                 select="gmd:identificationInfo/*/gmd:pointOfContact"/>
          </xsl:with-param>
        </xsl:call-template>

        <xsl:call-template name="complexElementSimpleGui">
          <xsl:with-param name="title" select="/root/gui/schemas/iso19139/strings/mdContactInfo"/>
          <xsl:with-param name="content">
            <xsl:apply-templates mode="block"
                                 select="gmd:contact"/>
          </xsl:with-param>
        </xsl:call-template>

        <xsl:call-template name="complexElementSimpleGui">
          <xsl:with-param name="title" select="/root/gui/schemas/iso19139/strings/techInfo"/>
          <xsl:with-param name="content">
            <xsl:apply-templates mode="block"
                                 select="
              gmd:identificationInfo/*/gmd:spatialResolution[1]
              |gmd:identificationInfo/*/gmd:spatialRepresentationType
              |gmd:dataQualityInfo/gmd:DQ_DataQuality/gmd:lineage
              |gmd:identificationInfo/*/gmd:resourceConstraints[1]
              "
            ></xsl:apply-templates>
          </xsl:with-param>
        </xsl:call-template>

        <xsl:variable name="modifiedDate" select="gmd:dateStamp/*[1]"/>
        <span class="madeBy">
          <xsl:value-of select="/root/gui/strings/changeDate"/>&#160;<xsl:value-of
          select="if (contains($modifiedDate, 'T')) then substring-before($modifiedDate, 'T') else $modifiedDate"/>
          |
          <xsl:value-of select="/root/gui/strings/uuid"/>&#160;<xsl:value-of
          select="gmd:fileIdentifier"/>
        </span>

      </xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <xsl:template mode="block"
                match="gmd:identificationInfo/*/gmd:citation/gmd:CI_Citation/gmd:date[1]"
                priority="100">
    <xsl:call-template name="simpleElementSimpleGUI">
      <xsl:with-param name="title" select="/root/gui/schemas/iso19139/strings/refDate"/>
      <xsl:with-param name="helpLink">
        <xsl:call-template name="getHelpLink">
          <xsl:with-param name="schema" select="$schema"/>
          <xsl:with-param name="name" select="name(.)"/>
        </xsl:call-template>
      </xsl:with-param>
      <xsl:with-param name="content">
        <xsl:apply-templates mode="iso19139-simple"
                             select=".|following-sibling::node()[name(.)='gmd:date']"/>
      </xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <xsl:template mode="block" match="gmd:identificationInfo/*/gmd:extent/*/gmd:temporalElement"
                priority="100">
    <xsl:call-template name="simpleElementSimpleGUI">
      <xsl:with-param name="title" select="/root/gui/schemas/iso19139/strings/temporalRef"/>
      <xsl:with-param name="helpLink">
        <xsl:call-template name="getHelpLink">
          <xsl:with-param name="schema" select="$schema"/>
          <xsl:with-param name="name" select="name(.)"/>
        </xsl:call-template>
      </xsl:with-param>
      <xsl:with-param name="content">
        <xsl:apply-templates mode="iso19139-simple" select="*/gmd:extent/*/gml:beginPosition
                                                            |*/gmd:extent/*/gml:endPosition
                                                            |*/gmd:extent//gml:timePosition"/>
      </xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <xsl:template mode="block" match="gmd:identificationInfo/*/gmd:resourceConstraints[1]"
                priority="100">
    <xsl:call-template name="simpleElementSimpleGUI">
      <xsl:with-param name="title" select="/root/gui/schemas/iso19139/strings/constraintInfo"/>
      <xsl:with-param name="helpLink">
        <xsl:call-template name="getHelpLink">
          <xsl:with-param name="schema" select="$schema"/>
          <xsl:with-param name="name" select="name(.)"/>
        </xsl:call-template>
      </xsl:with-param>
      <xsl:with-param name="content">
        <xsl:apply-templates mode="iso19139-simple"
                             select="*|following-sibling::node()[name(.)='gmd:resourceConstraints']/*"/>
      </xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <xsl:template mode="block" match="gmd:contact|gmd:pointOfContact" priority="100">
    <xsl:call-template name="simpleElementSimpleGUI">
      <xsl:with-param name="title">
        <xsl:value-of
          select="geonet:getCodeListValue(/root/gui/schemas, 'iso19139', 'gmd:CI_RoleCode', */gmd:role/gmd:CI_RoleCode/@codeListValue)"
        />
      </xsl:with-param>
      <xsl:with-param name="helpLink">
        <xsl:call-template name="getHelpLink">
          <xsl:with-param name="schema" select="$schema"/>
          <xsl:with-param name="name" select="name(.)"/>
        </xsl:call-template>
      </xsl:with-param>
      <xsl:with-param name="content">
        <xsl:apply-templates mode="iso19139-simple"
                             select="
          gmd:CI_ResponsibleParty/descendant::node()[(gco:CharacterString and normalize-space(gco:CharacterString)!='')]
          "/>

        <xsl:for-each
          select="gmd:CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:onlineResource/gmd:CI_OnlineResource">

          <xsl:call-template name="simpleElement">
            <xsl:with-param name="id" select="generate-id(.)"/>
            <xsl:with-param name="title">
              <xsl:call-template name="getTitle">
                <xsl:with-param name="name" select="'gmd:onlineResource'"/>
                <xsl:with-param name="schema" select="$schema"/>
              </xsl:call-template>
            </xsl:with-param>
            <xsl:with-param name="help"></xsl:with-param>
            <xsl:with-param name="content">
              <a href="{gmd:linkage/gmd:URL}" target="_blank">
                <xsl:value-of select="gmd:name/gco:CharacterString"/>
              </a>
            </xsl:with-param>
          </xsl:call-template>
        </xsl:for-each>

        <xsl:if test="descendant::gmx:FileName">
          <img src="{descendant::gmx:FileName/@src}" alt="logo" class="logo orgLogo"
               style="float:right;"/>
          <!-- FIXME : css -->
        </xsl:if>
      </xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <xsl:template mode="block"
                match="gmd:identificationInfo/*/gmd:descriptiveKeywords/gmd:MD_Keywords"
                priority="90">
    <xsl:call-template name="simpleElementSimpleGUI">
      <xsl:with-param name="title">
        <xsl:call-template name="getTitle">
          <xsl:with-param name="name" select="name(.)"/>
          <xsl:with-param name="schema" select="$schema"/>
        </xsl:call-template>

        <xsl:if test="gmd:thesaurusName/gmd:CI_Citation/gmd:title/gco:CharacterString">
          (<xsl:value-of
          select="gmd:thesaurusName/gmd:CI_Citation/gmd:title/gco:CharacterString"/>)
        </xsl:if>
      </xsl:with-param>
      <xsl:with-param name="helpLink">
        <xsl:call-template name="getHelpLink">
          <xsl:with-param name="schema" select="$schema"/>
          <xsl:with-param name="name" select="name(.)"/>
        </xsl:call-template>
      </xsl:with-param>
      <xsl:with-param name="content">
        <xsl:for-each select="gmd:keyword">
          <xsl:if test="position() &gt; 1">
            <xsl:text>, </xsl:text>
          </xsl:if>


          <xsl:choose>
            <xsl:when test="gmx:Anchor">
              <a href="{gmx:Anchor/@xlink:href}">
                <xsl:value-of
                  select="if (gmx:Anchor/text()) then gmx:Anchor/text() else gmx:Anchor/@xlink:href"/>
              </a>
            </xsl:when>
            <xsl:otherwise>
              <xsl:call-template name="translatedString">
                <xsl:with-param name="schema" select="$schema"/>
                <xsl:with-param name="langId">
                  <xsl:call-template name="getLangId">
                    <xsl:with-param name="langGui" select="/root/gui/language"/>
                    <xsl:with-param name="md"
                                    select="ancestor-or-self::*[name(.)='gmd:MD_Metadata' or @gco:isoType='gmd:MD_Metadata']"/>
                  </xsl:call-template>
                </xsl:with-param>
              </xsl:call-template>
            </xsl:otherwise>
          </xsl:choose>

        </xsl:for-each>


        <xsl:variable name="type" select="gmd:type/gmd:MD_KeywordTypeCode/@codeListValue"/>
        <xsl:if test="$type != ''">
          (<xsl:value-of
          select="/root/gui/schemas/*[name(.)='iso19139']/codelists/codelist[@name = 'gmd:MD_KeywordTypeCode']/
            entry[code = $type]/label"/>)
        </xsl:if>

      </xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <xsl:template mode="block" match="gmd:referenceSystemInfo/*/gmd:referenceSystemIdentifier">
    <xsl:call-template name="simpleElementSimpleGUI">
      <xsl:with-param name="title">
        <xsl:call-template name="getTitle">
          <xsl:with-param name="name" select="name(.)"/>
          <xsl:with-param name="schema" select="$schema"/>
        </xsl:call-template>
      </xsl:with-param>
      <xsl:with-param name="helpLink">
        <xsl:call-template name="getHelpLink">
          <xsl:with-param name="schema" select="$schema"/>
          <xsl:with-param name="name" select="name(.)"/>
        </xsl:call-template>
      </xsl:with-param>
      <xsl:with-param name="content">
        <xsl:value-of select="gmd:RS_Identifier/gmd:code/gco:CharacterString"/>
        <xsl:if test="gmd:RS_Identifier/gmd:codeSpace/gco:CharacterString != ''">
          <xsl:value-of
            select="concat(' (', gmd:RS_Identifier/gmd:codeSpace/gco:CharacterString, ')')"/>
        </xsl:if>
      </xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <xsl:template mode="block" match="gmd:dataQualityInfo/gmd:DQ_DataQuality/gmd:lineage"
                priority="90">
    <xsl:call-template name="simpleElementSimpleGUI">
      <xsl:with-param name="title">
        <xsl:call-template name="getTitle">
          <xsl:with-param name="name" select="name(.)"/>
          <xsl:with-param name="schema" select="$schema"/>
        </xsl:call-template>
      </xsl:with-param>
      <xsl:with-param name="helpLink">
        <xsl:call-template name="getHelpLink">
          <xsl:with-param name="schema" select="$schema"/>
          <xsl:with-param name="name" select="name(.)"/>
        </xsl:call-template>
      </xsl:with-param>
      <xsl:with-param name="content">
        <xsl:apply-templates mode="iso19139-simple"
                             select="gmd:LI_Lineage/gmd:statement"/>

        <xsl:if test=".//gmd:source[@uuidref]">

          <xsl:call-template name="simpleElement">
            <xsl:with-param name="id" select="generate-id(.)"/>
            <xsl:with-param name="title">
              <xsl:call-template name="getTitle">
                <xsl:with-param name="name" select="'gmd:source'"/>
                <xsl:with-param name="schema" select="$schema"/>
              </xsl:call-template>
            </xsl:with-param>
            <xsl:with-param name="help"></xsl:with-param>
            <xsl:with-param name="content">
              <xsl:for-each select=".//gmd:source[@uuidref]">
                <br/>
                <a href="#" onclick="javascript:catalogue.metadataShow('{@uuidref}');">
                  <xsl:call-template name="getMetadataTitle">
                    <xsl:with-param name="uuid" select="@uuidref"/>
                  </xsl:call-template>
                </a>
              </xsl:for-each>
            </xsl:with-param>
          </xsl:call-template>
        </xsl:if>
      </xsl:with-param>
    </xsl:call-template>
  </xsl:template>


  <xsl:template mode="block" match="gmd:identificationInfo/*/gmd:language" priority="99">
    <xsl:call-template name="simpleElementSimpleGUI">
      <xsl:with-param name="title">
        <xsl:call-template name="getTitle">
          <xsl:with-param name="name" select="name(.)"/>
          <xsl:with-param name="schema" select="$schema"/>
        </xsl:call-template>
      </xsl:with-param>
      <xsl:with-param name="helpLink">
        <xsl:call-template name="getHelpLink">
          <xsl:with-param name="schema" select="$schema"/>
          <xsl:with-param name="name" select="name(.)"/>
        </xsl:call-template>
      </xsl:with-param>
      <xsl:with-param name="content">
        <xsl:call-template name="iso19139GetIsoLanguage">
          <xsl:with-param name="schema" select="$schema"/>
          <xsl:with-param name="edit" select="false()"/>
          <xsl:with-param name="value"
                          select="gco:CharacterString|gmd:LanguageCode/@codeListValue"/>
        </xsl:call-template>
      </xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <xsl:template mode="block" match="gmd:topicCategory
    " priority="98">
    <xsl:call-template name="simpleElementSimpleGUI">
      <xsl:with-param name="title">
        <xsl:call-template name="getTitle">
          <xsl:with-param name="name" select="name(.)"/>
          <xsl:with-param name="schema" select="$schema"/>
        </xsl:call-template>
      </xsl:with-param>
      <xsl:with-param name="helpLink">
        <xsl:call-template name="getHelpLink">
          <xsl:with-param name="schema" select="$schema"/>
          <xsl:with-param name="name" select="name(.)"/>
        </xsl:call-template>
      </xsl:with-param>
      <xsl:with-param name="content">
        <xsl:variable name="choiceValue" select="gmd:MD_TopicCategoryCode"/>
        <xsl:variable name="name" select="'gmd:MD_TopicCategoryCode'"/>
        <xsl:variable name="schemaLabel"
                      select="/root/gui/schemas/*[name(.)=$schema]/codelists/codelist[@name = $name]/entry[code = $choiceValue]/label"/>

        <xsl:variable name="label">
          <xsl:choose>
            <xsl:when
              test="normalize-space($schemaLabel) = '' and starts-with($schema, 'iso19139.')">
              <!-- Check iso19139 label -->
              <xsl:value-of
                select="/root/gui/schemas/*[name(.)='iso19139']/codelists/codelist[@name = $name]/entry[code = $choiceValue]/label"/>
            </xsl:when>
            <xsl:when test="$schemaLabel">
              <xsl:value-of select="$schemaLabel"/>
            </xsl:when>
            <xsl:otherwise>
              <xsl:value-of select="$choiceValue"/>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:variable>

        <xsl:value-of select="$label"/>
      </xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <xsl:template mode="block"
                match="gmd:identificationInfo/*/gmd:extent/gmd:EX_Extent/gmd:geographicElement"
                priority="99">
    <xsl:apply-templates mode="iso19139" select="gmd:EX_GeographicBoundingBox">
      <xsl:with-param name="schema" select="$schema"/>
      <xsl:with-param name="edit" select="false()"/>
    </xsl:apply-templates>
  </xsl:template>


  <xsl:template mode="block" match="gmd:graphicOverview" priority="98">
    <xsl:call-template name="simpleElementSimpleGUI">
      <xsl:with-param name="title">
        <xsl:call-template name="getTitle">
          <xsl:with-param name="name" select="name(.)"/>
          <xsl:with-param name="schema" select="$schema"/>
        </xsl:call-template>
      </xsl:with-param>
      <xsl:with-param name="helpLink">
        <xsl:call-template name="getHelpLink">
          <xsl:with-param name="schema" select="$schema"/>
          <xsl:with-param name="name" select="name(.)"/>
        </xsl:call-template>
      </xsl:with-param>
      <xsl:with-param name="content">
        <!-- FIXME template name or move to generic layout -->
        <xsl:apply-templates mode="logo"
                             select=".|following-sibling::node()[name(.)='gmd:graphicOverview']"/>
      </xsl:with-param>
    </xsl:call-template>
  </xsl:template>


  <xsl:template mode="block" match="*[*/@codeList]" priority="100">
    <xsl:call-template name="simpleElementSimpleGUI">
      <xsl:with-param name="title">
        <xsl:call-template name="getTitle">
          <xsl:with-param name="name" select="name(.)"/>
          <xsl:with-param name="schema" select="$schema"/>
        </xsl:call-template>
      </xsl:with-param>
      <xsl:with-param name="helpLink">
        <xsl:call-template name="getHelpLink">
          <xsl:with-param name="schema" select="$schema"/>
          <xsl:with-param name="name" select="name(.)"/>
        </xsl:call-template>
      </xsl:with-param>
      <xsl:with-param name="content">
        <xsl:apply-templates mode="iso19139GetAttributeText" select="*/@codeListValue">
          <xsl:with-param name="schema" select="$schema"/>
          <xsl:with-param name="edit" select="false()"/>
        </xsl:apply-templates>
      </xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <xsl:template mode="block" match="*[gco:Integer]
    " priority="99">
    <xsl:call-template name="simpleElementSimpleGUI">
      <xsl:with-param name="title">
        <xsl:call-template name="getTitle">
          <xsl:with-param name="name" select="name(.)"/>
          <xsl:with-param name="schema" select="$schema"/>
        </xsl:call-template>
      </xsl:with-param>
      <xsl:with-param name="helpLink">
        <xsl:call-template name="getHelpLink">
          <xsl:with-param name="schema" select="$schema"/>
          <xsl:with-param name="name" select="name(.)"/>
        </xsl:call-template>
      </xsl:with-param>
      <xsl:with-param name="content">
        <xsl:value-of select="gco:Integer"/>
      </xsl:with-param>
    </xsl:call-template>
  </xsl:template>
  <xsl:template mode="block" match="*[gco:CharacterString]
    " priority="98">
    <xsl:call-template name="simpleElementSimpleGUI">
      <xsl:with-param name="title">
        <xsl:call-template name="getTitle">
          <xsl:with-param name="name" select="name(.)"/>
          <xsl:with-param name="schema" select="$schema"/>
        </xsl:call-template>
      </xsl:with-param>
      <xsl:with-param name="helpLink">
        <xsl:call-template name="getHelpLink">
          <xsl:with-param name="schema" select="$schema"/>
          <xsl:with-param name="name" select="name(.)"/>
        </xsl:call-template>
      </xsl:with-param>
      <xsl:with-param name="content">
        <!-- TODO multilingual -->
        <xsl:value-of select="gco:CharacterString"/>
      </xsl:with-param>
    </xsl:call-template>
  </xsl:template>


  <xsl:template mode="block" match="gmd:spatialResolution" priority="100">
    <xsl:call-template name="simpleElementSimpleGUI">
      <xsl:with-param name="title">
        <xsl:call-template name="getTitle">
          <xsl:with-param name="name" select="'gmd:spatialResolution'"/>
          <xsl:with-param name="schema" select="$schema"/>
        </xsl:call-template>
      </xsl:with-param>
      <xsl:with-param name="helpLink">
        <xsl:call-template name="getHelpLink">
          <xsl:with-param name="schema" select="$schema"/>
          <xsl:with-param name="name" select="name(.)"/>
        </xsl:call-template>
      </xsl:with-param>
      <xsl:with-param name="content">
        <xsl:apply-templates mode="iso19139-simple"
                             select="
          gmd:MD_Resolution/gmd:equivalentScale/gmd:MD_RepresentativeFraction/gmd:denominator
          |gmd:MD_Resolution/gmd:distance
          |following-sibling::node()[name(.)='gmd:spatialResolution']/gmd:MD_Resolution/gmd:equivalentScale/gmd:MD_RepresentativeFraction/gmd:denominator
          |following-sibling::node()[name(.)='gmd:spatialResolution']/gmd:MD_Resolution/gmd:distance
          "/>
      </xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <xsl:template mode="block" match="*|@*">
    <xsl:apply-templates mode="block" select="*"/>
  </xsl:template>


  <!-- List of related resources defined in the online resource section of the metadata record.
-->
  <xsl:template mode="relatedResources"
                match="*">
    <table class="related">
      <tbody>
        <tr
          style="display:none;"><!-- FIXME needed by JS to append other type of relation from xml.relation service -->
          <td class="main"></td>
          <td></td>
        </tr>
        <xsl:for-each-group
          select="gmd:distributionInfo/descendant::gmd:onLine[gmd:CI_OnlineResource/gmd:linkage/gmd:URL!='']"
          group-by="gmd:CI_OnlineResource/gmd:protocol">
          <tr>
            <td class="main">
              <!-- Usually, protocole format is OGC:WMS-version-blahblah, remove ':' and get
              prefix of the protocol to set the CSS icon class-->
              <xsl:variable name="protocolIcon">
                <xsl:choose>
                  <xsl:when
                    test="translate(substring-before(current-grouping-key(), '-'), ':', '') = ''">
                    WWWLINK
                  </xsl:when>
                  <xsl:otherwise>
                    <xsl:value-of
                      select="translate(substring-before(current-grouping-key(), '-'), ':', '')"/>
                  </xsl:otherwise>
                </xsl:choose>
              </xsl:variable>
              <span class="{$protocolIcon} icon">
                <xsl:value-of
                  select="/root/gui/schemas/iso19139/labels/element[@name = 'gmd:protocol']/helper/option[@value=normalize-space(current-grouping-key())]"/>
              </span>
            </td>
            <td>
              <ul>
                <xsl:for-each select="current-group()">
                  <xsl:variable name="desc">
                    <xsl:apply-templates mode="localised"
                                         select="gmd:CI_OnlineResource/gmd:description">
                      <xsl:with-param name="langId" select="$langId"/>
                    </xsl:apply-templates>
                  </xsl:variable>
                  <li>
                    <a href="{gmd:CI_OnlineResource/gmd:linkage/gmd:URL}">
                      <xsl:choose>
                        <xsl:when
                          test="contains(current-grouping-key(), 'OGC') or contains(current-grouping-key(), 'DOWNLOAD')">
                          <!-- Name contains layer, feature type, coverage ... -->
                          <xsl:choose>
                            <xsl:when test="normalize-space($desc)!=''">
                              <xsl:value-of select="$desc"/>
                              <xsl:if test="gmd:CI_OnlineResource/gmd:name/gmx:MimeFileType/@type">
                                (<xsl:value-of
                                select="gmd:CI_OnlineResource/gmd:name/gmx:MimeFileType/@type"/>)
                              </xsl:if>
                            </xsl:when>
                            <xsl:when
                              test="normalize-space(gmd:CI_OnlineResource/gmd:name/gco:CharacterString)!=''">
                              <xsl:value-of
                                select="gmd:CI_OnlineResource/gmd:name/gco:CharacterString"/>
                            </xsl:when>
                            <xsl:otherwise>
                              <xsl:value-of select="gmd:CI_OnlineResource/gmd:linkage/gmd:URL"/>
                            </xsl:otherwise>
                          </xsl:choose>
                        </xsl:when>
                        <xsl:otherwise>
                          <xsl:if test="normalize-space($desc)!=''">
                            <xsl:attribute name="title">
                              <xsl:value-of select="$desc"/>
                            </xsl:attribute>
                          </xsl:if>
                          <xsl:choose>
                            <xsl:when
                              test="normalize-space(gmd:CI_OnlineResource/gmd:name/gco:CharacterString)!=''">
                              <xsl:value-of
                                select="gmd:CI_OnlineResource/gmd:name/gco:CharacterString"/>
                            </xsl:when>
                            <xsl:otherwise>
                              <xsl:value-of select="gmd:CI_OnlineResource/gmd:linkage/gmd:URL"/>
                            </xsl:otherwise>
                          </xsl:choose>
                        </xsl:otherwise>
                      </xsl:choose>
                    </a>

                    <!-- Display add to map action for WMS -->
                    <xsl:if test="contains(current-grouping-key(), 'WMS')">
                      &#160;
                      <a href="#" class="md-mn addLayer"
                         onclick="app.switchMode('1', true);app.getIMap().addWMSLayer([[
                              '{gmd:CI_OnlineResource/gmd:description/gco:CharacterString}',
                              '{gmd:CI_OnlineResource/gmd:linkage/gmd:URL}',
                              '{gmd:CI_OnlineResource/gmd:name/gco:CharacterString}', '{generate-id()}']]);">
                        &#160;</a>
                    </xsl:if>
                    <xsl:if test="contains(current-grouping-key(), 'WMC')">
                      &#160;
                      <a href="#" class="md-mn addLayer"
                         onclick="app.switchMode('1', true);app.getIMap().addWMC('{gmd:CI_OnlineResource/gmd:linkage/gmd:URL}');">
                        &#160;</a>
                    </xsl:if>
                  </li>
                </xsl:for-each>
              </ul>
            </td>
          </tr>
        </xsl:for-each-group>
      </tbody>
    </table>
  </xsl:template>


  <!-- Extract logo -->
  <xsl:template mode="logo" match="gmd:graphicOverview">
    <xsl:variable name="fileName" select="gmd:MD_BrowseGraphic/gmd:fileName/gco:CharacterString"/>
    <xsl:if test="normalize-space($fileName)!=''">
      <xsl:variable name="url"
                    select="if (contains($fileName, '://'))
        then $fileName
        else geonet:get-thumbnail-url($fileName, //geonet:info, /root/gui/locService)"/>

      <a href="{$url}" rel="lightbox-viewset">
        <img class="thumbnail" src="{$url}" alt="thumbnail"
             title="{gmd:MD_BrowseGraphic/gmd:fileDescription/gco:CharacterString}"/>
      </a>
    </xsl:if>
  </xsl:template>


  <!-- Hide them -->
  <xsl:template mode="iso19139-simple" match="
    geonet:*|*[@gco:nilReason='missing']|@gco:isoType" priority="99"/>
  <!-- Don't display -->

  <!-- these elements should be boxed -->
  <xsl:template mode="iso19139-simple"
                match="gmd:identificationInfo|gmd:distributionInfo
    |gmd:descriptiveKeywords|gmd:thesaurusName
    |gmd:spatialRepresentationInfo
    |gmd:pointOfContact|gmd:contact
    |gmd:dataQualityInfo
    |gmd:MD_Constraints|gmd:MD_LegalConstraints|gmd:MD_SecurityConstraints
    |gmd:referenceSystemInfo|gmd:equivalentScale|gmd:projection|gmd:ellipsoid
    |gmd:extent|gmd:geographicBox|gmd:EX_TemporalExtent
    |gmd:MD_Distributor
    |srv:containsOperations|srv:SV_CoupledResource|gmd:metadataConstraints"
                priority="2">
    <xsl:call-template name="complexElement">
      <xsl:with-param name="id" select="generate-id(.)"/>
      <xsl:with-param name="title">
        <xsl:call-template name="getTitle">
          <xsl:with-param name="name" select="name(.)"/>
          <xsl:with-param name="schema" select="$schema"/>
        </xsl:call-template>
      </xsl:with-param>
      <xsl:with-param name="helpLink">
        <xsl:call-template name="getHelpLink">
          <xsl:with-param name="name" select="name(.)"/>
          <xsl:with-param name="schema" select="$schema"/>
        </xsl:call-template>
      </xsl:with-param>
      <xsl:with-param name="content">
        <xsl:apply-templates mode="iso19139-simple" select="@*|*">
          <xsl:with-param name="schema" select="$schema"/>
        </xsl:apply-templates>
      </xsl:with-param>
    </xsl:call-template>
  </xsl:template>


  <xsl:template mode="iso19139-simple"
                match="
    gmd:*[gco:Integer|gco:Decimal|gco:Boolean|gco:Real|gco:Measure|gco:Length|gco:Distance|gco:Angle|gco:Scale|gco:RecordType|gmx:MimeFileType]|
    srv:*[gco:Integer|gco:Decimal|gco:Boolean|gco:Real|gco:Measure|gco:Length|gco:Distance|gco:Angle|gco:Scale|gco:RecordType|gmx:MimeFileType]"
                priority="2">

    <xsl:call-template name="simpleElement">
      <xsl:with-param name="id" select="generate-id(.)"/>
      <xsl:with-param name="title">
        <xsl:call-template name="getTitle">
          <xsl:with-param name="name" select="name(.)"/>
          <xsl:with-param name="schema" select="$schema"/>
        </xsl:call-template>
      </xsl:with-param>
      <xsl:with-param name="help"></xsl:with-param>
      <xsl:with-param name="content">
        <xsl:value-of
          select="gco:Integer|gco:Decimal|gco:Boolean|gco:Real|gco:Measure
          |gco:Length|gco:Distance|gco:Angle|gco:Scale|gco:RecordType|gmx:MimeFileType"
        />
        <xsl:if test="gco:Distance/@uom">
          <xsl:text>&#160;</xsl:text>
          <xsl:choose>
            <xsl:when test="contains(gco:Distance/@uom, '#')">
              <a href="{gco:Distance/@uom}">
                <xsl:value-of select="substring-after(gco:Distance/@uom, '#')"/>
              </a>
            </xsl:when>
            <xsl:otherwise>
              <xsl:value-of select="gco:Distance/@uom"/>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:if>
      </xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <xsl:template mode="iso19139-simple"
                match="
    gmd:date|
    srv:date"
                priority="99">

    <xsl:call-template name="simpleElement">
      <xsl:with-param name="id" select="generate-id(.)"/>
      <xsl:with-param name="title">
        <xsl:call-template name="getTitle">
          <xsl:with-param name="name" select="' '"/>
          <xsl:with-param name="schema" select="$schema"/>
        </xsl:call-template>
      </xsl:with-param>
      <xsl:with-param name="help"></xsl:with-param>
      <xsl:with-param name="content">
        <xsl:value-of
          select="./gmd:CI_Date/gmd:date/gco:Date|gmd:CI_Date/gmd:date/gco:DateTime"
        />
        <xsl:if
          test="normalize-space(gmd:CI_Date/gmd:dateType/gmd:CI_DateTypeCode/@codeListValue)!=''">
          (
          <xsl:apply-templates mode="iso19139GetAttributeText"
                               select="gmd:CI_Date/gmd:dateType/gmd:CI_DateTypeCode/@codeListValue">
            <xsl:with-param name="schema" select="$schema"/>
            <xsl:with-param name="edit" select="false()"/>
          </xsl:apply-templates>
          )
        </xsl:if>
      </xsl:with-param>
    </xsl:call-template>
  </xsl:template>


  <!-- gco:CharacterString are swallowed -->
  <!-- TODO : PT_FreeText -->
  <xsl:template mode="iso19139-simple" match="*[gco:CharacterString]" priority="2">

    <xsl:call-template name="simpleElement">
      <xsl:with-param name="id" select="generate-id(.)"/>
      <xsl:with-param name="title">
        <xsl:call-template name="getTitle">
          <xsl:with-param name="name" select="name(.)"/>
          <xsl:with-param name="schema" select="$schema"/>
        </xsl:call-template>
      </xsl:with-param>
      <xsl:with-param name="help"></xsl:with-param>
      <xsl:with-param name="content">
        <xsl:call-template name="addLineBreaksAndHyperlinks">
          <xsl:with-param name="txt">
            <xsl:apply-templates mode="localised" select=".">
              <xsl:with-param name="langId" select="$langId"/>
            </xsl:apply-templates>
          </xsl:with-param>
        </xsl:call-template>
      </xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <!-- TODO other gml time information may be used. -->
  <xsl:template mode="iso19139-simple" match="gml:endPosition|gml:beginPosition|gml:timePosition"
                priority="2">
    <xsl:call-template name="simpleElement">
      <xsl:with-param name="id" select="generate-id(.)"/>
      <xsl:with-param name="title">
        <xsl:call-template name="getTitle">
          <xsl:with-param name="name" select="name(.)"/>
          <xsl:with-param name="schema" select="$schema"/>
        </xsl:call-template>
      </xsl:with-param>
      <xsl:with-param name="help"></xsl:with-param>
      <xsl:with-param name="content">
        <xsl:value-of select="."/>

        <xsl:for-each select="@*">
          <xsl:variable name="label">
            <xsl:call-template name="getTitle">
              <xsl:with-param name="name" select="name(.)"/>
              <xsl:with-param name="schema" select="$schema"/>
            </xsl:call-template>
          </xsl:variable>
          |
          <xsl:value-of select="concat($label, ': ', .)"/>
        </xsl:for-each>
      </xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <xsl:template mode="iso19139-simple" match="gmd:*[*/@codeList]|srv:*[*/@codeList]">

    <xsl:call-template name="simpleElement">
      <xsl:with-param name="id" select="generate-id(.)"/>
      <xsl:with-param name="type" select="*/@codeListValue"/>
      <xsl:with-param name="title">
        <xsl:call-template name="getTitle">
          <xsl:with-param name="name" select="name(.)"/>
          <xsl:with-param name="schema" select="$schema"/>
        </xsl:call-template>
      </xsl:with-param>
      <xsl:with-param name="help"></xsl:with-param>
      <xsl:with-param name="content">
        <xsl:apply-templates mode="iso19139GetAttributeText" select="*/@codeListValue">
          <xsl:with-param name="schema" select="$schema"/>
          <xsl:with-param name="edit" select="false()"/>
        </xsl:apply-templates>
      </xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <!-- All others
   -->
  <xsl:template mode="iso19139-simple" match="*|@*">
    <xsl:call-template name="simpleElement">
      <xsl:with-param name="id" select="generate-id(.)"/>
      <xsl:with-param name="title">
        <xsl:call-template name="getTitle">
          <xsl:with-param name="name" select="name(.)"/>
          <xsl:with-param name="schema" select="$schema"/>
        </xsl:call-template>
      </xsl:with-param>
      <xsl:with-param name="help"></xsl:with-param>
      <xsl:with-param name="content">
        <xsl:variable name="empty">
          <xsl:apply-templates mode="iso19139IsEmpty" select="."/>
        </xsl:variable>
        <xsl:if test="$empty!=''">
          <xsl:apply-templates mode="iso19139-simple" select="*|@*"/>
        </xsl:if>
      </xsl:with-param>
    </xsl:call-template>

  </xsl:template>

</xsl:stylesheet>
