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
                xmlns:exslt="http://exslt.org/common" xmlns:geonet="http://www.fao.org/geonetwork"
                xmlns:skos="http://www.w3.org/2004/02/skos/core#"
                xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
                xmlns:dc="http://purl.org/dc/elements/1.1/"
                xmlns:dcterms="http://purl.org/dc/terms/"
                xmlns:gco="http://www.isotc211.org/2005/gco"
                xmlns:gmd="http://www.isotc211.org/2005/gmd"
                xmlns:srv="http://www.isotc211.org/2005/srv"
                xmlns:util="java:org.fao.geonet.util.XslUtil"
                version="2.0" exclude-result-prefixes="#all">
  <!--
      Analyze topic categories and INSPIRE themes in the metadata record and suggest to add matching :

      * missing topic category for current INSPIRE Themes
      * missing INSPIRE theme for current topic category.
      It makes easier to fill topic category according to INSPIRE themes and vice versa.

      The process is disabled by default in GeoNetwork because related to INSPIRE only. See iso19139/suggest.xsl file for configuration

      Mapping has been proposed by "Guide de saisie des éléments de métadonnées INSPIRE"

  TODO : Add INSPIRE themes translations when metadata record is multilingual
  -->
  <xsl:import href="process-utility.xsl"/>

  <xsl:variable name="inspire-th"
                select="document(concat('file:///', replace(util:getConfigValue('codeListDir'), '\\', '/'), '/external/thesauri/theme/httpinspireeceuropaeutheme-theme.rdf'))"/>

  <xsl:variable name="itheme-topiccat-map">
    <!-- <entry>
        <itheme>http://rdfdata.eionet.europa.eu/inspirethemes/themes/1</itheme>
        All
        </entry>-->
    <!--<entry>
        <itheme>http://rdfdata.eionet.europa.eu/inspirethemes/themes/2</itheme>
        All
        </entry>-->
    <entry>
      <itheme>http://rdfdata.eionet.europa.eu/inspirethemes/themes/3</itheme>
      <topiccat>location</topiccat>
    </entry>
    <entry>
      <itheme>http://rdfdata.eionet.europa.eu/inspirethemes/themes/4</itheme>
      <topiccat>boundaries</topiccat>
    </entry>
    <entry>
      <itheme>http://rdfdata.eionet.europa.eu/inspirethemes/themes/5</itheme>
      <topiccat>location</topiccat>
    </entry>
    <entry>
      <itheme>http://rdfdata.eionet.europa.eu/inspirethemes/themes/6</itheme>
      <topiccat>planningCadastre</topiccat>
    </entry>
    <entry>
      <itheme>http://rdfdata.eionet.europa.eu/inspirethemes/themes/7</itheme>
      <topiccat>transportation</topiccat>
    </entry>
    <entry>
      <itheme>http://rdfdata.eionet.europa.eu/inspirethemes/themes/8</itheme>
      <topiccat>inlandWaters</topiccat>
    </entry>
    <entry>
      <itheme>http://rdfdata.eionet.europa.eu/inspirethemes/themes/9</itheme>
      <topiccat>environment</topiccat>
    </entry>
    <entry>
      <itheme>http://rdfdata.eionet.europa.eu/inspirethemes/themes/10</itheme>
      <topiccat>elevation</topiccat>
    </entry>
    <entry>
      <itheme>http://rdfdata.eionet.europa.eu/inspirethemes/themes/11</itheme>
      <topiccat>imageryBaseMapsEarthCover</topiccat>
    </entry>
    <entry>
      <itheme>http://rdfdata.eionet.europa.eu/inspirethemes/themes/12</itheme>
      <topiccat>imageryBaseMapsEarthCover</topiccat>
    </entry>
    <entry>
      <itheme>http://rdfdata.eionet.europa.eu/inspirethemes/themes/13</itheme>
      <topiccat>geoscientificInformation</topiccat>
    </entry>
    <entry>
      <itheme>http://rdfdata.eionet.europa.eu/inspirethemes/themes/14</itheme>
      <topiccat>boundaries</topiccat>
    </entry>
    <entry>
      <itheme>http://rdfdata.eionet.europa.eu/inspirethemes/themes/15</itheme>
      <topiccat>structure</topiccat>
    </entry>
    <entry>
      <itheme>http://rdfdata.eionet.europa.eu/inspirethemes/themes/16</itheme>
      <topiccat>geoscientificInformation</topiccat>
    </entry>
    <entry>
      <itheme>http://rdfdata.eionet.europa.eu/inspirethemes/themes/17</itheme>
      <topiccat>planningCadastre</topiccat>
    </entry>
    <entry>
      <itheme>http://rdfdata.eionet.europa.eu/inspirethemes/themes/18</itheme>
      <topiccat>health</topiccat>
    </entry>
    <entry>
      <itheme>http://rdfdata.eionet.europa.eu/inspirethemes/themes/19</itheme>
      <topiccat>utilitiesCommunication</topiccat>
    </entry>
    <entry>
      <itheme>http://rdfdata.eionet.europa.eu/inspirethemes/themes/20</itheme>
      <topiccat>structure</topiccat>
    </entry>
    <entry>
      <itheme>http://rdfdata.eionet.europa.eu/inspirethemes/themes/21</itheme>
      <topiccat>structure</topiccat>
    </entry>
    <entry>
      <itheme>http://rdfdata.eionet.europa.eu/inspirethemes/themes/22</itheme>
      <topiccat>farming</topiccat>
    </entry>
    <entry>
      <itheme>http://rdfdata.eionet.europa.eu/inspirethemes/themes/23</itheme>
      <topiccat>society</topiccat>
    </entry>
    <entry>
      <itheme>http://rdfdata.eionet.europa.eu/inspirethemes/themes/24</itheme>
      <topiccat>planningCadastre</topiccat>
    </entry>
    <entry>
      <itheme>http://rdfdata.eionet.europa.eu/inspirethemes/themes/25</itheme>
      <topiccat>geoscientificInformation</topiccat>
    </entry>
    <entry>
      <itheme>http://rdfdata.eionet.europa.eu/inspirethemes/themes/26</itheme>
      <topiccat>climatologyMeteorologyAtmosphere</topiccat>
    </entry>
    <entry>
      <itheme>http://rdfdata.eionet.europa.eu/inspirethemes/themes/27</itheme>
      <topiccat>climatologyMeteorologyAtmosphere</topiccat>
    </entry>
    <entry>
      <itheme>http://rdfdata.eionet.europa.eu/inspirethemes/themes/28</itheme>
      <topiccat>oceans</topiccat>
    </entry>
    <entry>
      <itheme>http://rdfdata.eionet.europa.eu/inspirethemes/themes/29</itheme>
      <topiccat>oceans</topiccat>
    </entry>
    <entry>
      <itheme>http://rdfdata.eionet.europa.eu/inspirethemes/themes/30</itheme>
      <topiccat>biota</topiccat>
    </entry>
    <entry>
      <itheme>http://rdfdata.eionet.europa.eu/inspirethemes/themes/31</itheme>
      <topiccat>biota</topiccat>
    </entry>
    <entry>
      <itheme>http://rdfdata.eionet.europa.eu/inspirethemes/themes/32</itheme>
      <topiccat>biota</topiccat>
    </entry>
    <entry>
      <itheme>http://rdfdata.eionet.europa.eu/inspirethemes/themes/33</itheme>
      <topiccat>economy</topiccat>
    </entry>
    <entry>
      <itheme>http://rdfdata.eionet.europa.eu/inspirethemes/themes/34</itheme>
      <topiccat>economy</topiccat>
    </entry>
  </xsl:variable>

  <!-- i18n information -->
  <xsl:variable name="itheme-topiccat-loc">
    <msg id="a" xml:lang="eng">INSPIRE Themes and/or topic categories found with missing matching
      items. Run this suggestion to add all corresponding
      values.
    </msg>
    <msg id="a" xml:lang="dut">INSPIRE Thema's gevonden met ontbrekende overeenkomstige
       items. Voer deze suggestie uit om alle overeenkomstige items toe te voegen
       waarden.
    </msg>
    <msg id="a" xml:lang="fre">Thèmes INSPIRE et/ou des catégories ont été trouvés avec des
      correspondances manquantes. Lancer ce processus pour ajouter les correspondances.
    </msg>
  </xsl:variable>


  <xsl:template name="list-inspire-themes-and-topiccategory">
    <suggestion process="inspire-themes-and-topiccategory"/>
  </xsl:template>

  <!-- Analyze the metadata record and return available suggestion
      for that process -->
  <xsl:template name="analyze-inspire-themes-and-topiccategory">
    <xsl:param name="root"/>
    <xsl:variable name="lang" select="if (normalize-space($root//gmd:MD_Metadata/gmd:language/gco:CharacterString
            |$root//gmd:MD_Metadata/gmd:language/gmd:LanguageCode/@codeListValue)='')
            then 'en'
            else
            substring($root//gmd:MD_Metadata/gmd:language/gco:CharacterString
            |$root//gmd:MD_Metadata/gmd:language/gmd:LanguageCode/@codeListValue, 1, 2)
            "/>
    <xsl:variable name="mappingAvailable">
      <!-- For all theme in metadata language -->
      <xsl:for-each
        select="$inspire-th//skos:Concept[skos:prefLabel/@xml:lang = normalize-space($lang)]">
        <xsl:variable name="themeLabel"
                      select="skos:prefLabel[@xml:lang = normalize-space($lang)]"/>

        <!-- if in metadata -->
        <xsl:if test="$root//gmd:keyword[gco:CharacterString = $themeLabel]">
          <xsl:variable name="themeId" select="@rdf:about"/>
          <!-- and corresponding topic cat does not exist in metadata -->
          <xsl:for-each select="$itheme-topiccat-map/entry[itheme=$themeId]/topiccat">
            <xsl:variable name="tcId" select="."/>
            <xsl:if
              test="count($root//gmd:topicCategory[gmd:MD_TopicCategoryCode = normalize-space($tcId)]) != 1">
              YES
            </xsl:if>
          </xsl:for-each>
        </xsl:if>
      </xsl:for-each>
      <xsl:for-each select="$root//gmd:topicCategory/gmd:MD_TopicCategoryCode">
        <xsl:variable name="tcId" select="."/>

        <xsl:for-each select="$itheme-topiccat-map/entry[topiccat = $tcId]/itheme">
          <xsl:variable name="themeId" select="."/>
          <xsl:variable name="themeLabel"
                        select="$inspire-th//skos:Concept[@rdf:about = $themeId]/skos:prefLabel[@xml:lang = normalize-space($lang)]"/>

          <xsl:if test="count($root//gmd:keyword[gco:CharacterString = $themeLabel]) != 1">
            YES
          </xsl:if>
        </xsl:for-each>
      </xsl:for-each>
    </xsl:variable>


    <xsl:if test="normalize-space($mappingAvailable)!=''">
      <suggestion process="inspire-themes-and-topiccategory" category="keyword" target="keyword">
        <name>
          <xsl:value-of select="geonet:i18n($itheme-topiccat-loc, 'a', $guiLang)"/>
        </name>
        <operational>true</operational>
        <form/>
      </suggestion>
    </xsl:if>
  </xsl:template>


  <xsl:template
    match="gmd:identificationInfo/*"
    priority="2">

    <xsl:copy>
      <xsl:copy-of select="@*"/>
      <!-- Copy all elements from AbstractMD_IdentificationType-->
      <xsl:copy-of
        select="gmd:citation
                |gmd:abstract
                |gmd:purpose
                |gmd:credit
                |gmd:status
                |gmd:pointOfContact
                |gmd:resourceMaintenance
                |gmd:graphicOverview
                |gmd:resourceFormat
                |gmd:descriptiveKeywords[not(contains(*/gmd:thesaurusName[1]/gmd:CI_Citation/gmd:title/gco:CharacterString, 'INSPIRE'))]"/>

      <!-- Add INSPIRE themes according to topic category. -->
      <xsl:variable name="existingInspireThemes"
                    select="gmd:descriptiveKeywords[contains(*/gmd:thesaurusName/gmd:CI_Citation/gmd:title/gco:CharacterString, 'INSPIRE')]"/>

      <!-- Default language is english if not set
          else use the 2 first letter of the language code (skos xml:lang attribute is ISO 2 letters code)
      -->
      <xsl:variable name="lang" select="if (normalize-space(//gmd:MD_Metadata/gmd:language/gco:CharacterString
                |//gmd:MD_Metadata/gmd:language/gmd:LanguageCode/@codeListValue)='')
                then 'en'
                else
                substring(//gmd:MD_Metadata/gmd:language/gco:CharacterString
                |//gmd:MD_Metadata/gmd:language/gmd:LanguageCode/@codeListValue, 1, 2)
                "/>
      <xsl:variable name="mdKeywords" select="gmd:descriptiveKeywords"/>
      <xsl:variable name="missing-inspirethemes">
        <xsl:for-each select="gmd:topicCategory/gmd:MD_TopicCategoryCode">
          <xsl:variable name="tcId" select="."/>

          <xsl:for-each select="$itheme-topiccat-map/entry[topiccat = $tcId]/itheme">
            <xsl:variable name="themeId" select="."/>
            <xsl:variable name="themeLabel"
                          select="$inspire-th//skos:Concept[@rdf:about = $themeId]/skos:prefLabel[@xml:lang = normalize-space($lang)]"/>

            <xsl:if test="count($mdKeywords//gmd:keyword[gco:CharacterString = $themeLabel]) != 1">
              <elem>
                <xsl:value-of select="$themeLabel"/>
              </elem>
            </xsl:if>
          </xsl:for-each>
        </xsl:for-each>
      </xsl:variable>

      <xsl:choose>
        <xsl:when test="normalize-space($missing-inspirethemes) != ''">
          <gmd:descriptiveKeywords>
            <gmd:MD_Keywords>
              <xsl:copy-of select="$existingInspireThemes/gmd:MD_Keywords/gmd:keyword"/>
              <xsl:for-each-group select="exslt:node-set($missing-inspirethemes)//elem"
                                  group-by=".">
                <gmd:keyword>
                  <!-- TODO : add translation according to gmd:locale-->
                  <gco:CharacterString>
                    <xsl:value-of select="."/>
                  </gco:CharacterString>
                </gmd:keyword>
              </xsl:for-each-group>

              <gmd:type>
                <gmd:MD_KeywordTypeCode
                  codeList="http://standards.iso.org/iso/19139/resources/gmxCodelists.xml#MD_KeywordTypeCode"
                  codeListValue="theme"/>
              </gmd:type>
              <xsl:choose>
                <xsl:when test="$existingInspireThemes/gmd:MD_Keywords/gmd:thesaurusName">
                  <xsl:copy-of
                    select="$existingInspireThemes[1]/gmd:MD_Keywords/gmd:thesaurusName"/>
                </xsl:when>
                <xsl:otherwise>
                  <gmd:thesaurusName>
                    <gmd:CI_Citation>
                      <gmd:title>
                        <gco:CharacterString>
                          <xsl:value-of select="$inspire-th//skos:ConceptScheme/dc:title"/>
                        </gco:CharacterString>
                      </gmd:title>
                      <gmd:date>
                        <gmd:CI_Date>
                          <gmd:date>
                            <gco:Date>
                              <xsl:value-of
                                select="$inspire-th//skos:ConceptScheme/dcterms:issued"/>
                            </gco:Date>
                          </gmd:date>
                          <gmd:dateType>
                            <gmd:CI_DateTypeCode
                              codeList="http://standards.iso.org/iso/19139/resources/gmxCodelists.xml#CI_DateTypeCode"
                              codeListValue="publication"/>
                          </gmd:dateType>
                        </gmd:CI_Date>
                      </gmd:date>
                    </gmd:CI_Citation>
                  </gmd:thesaurusName>
                </xsl:otherwise>
              </xsl:choose>
            </gmd:MD_Keywords>
          </gmd:descriptiveKeywords>
        </xsl:when>
        <xsl:otherwise>
          <xsl:copy-of select="$existingInspireThemes"/>
        </xsl:otherwise>
      </xsl:choose>

      <xsl:copy-of
        select="gmd:resourceSpecificUsage
                |gmd:resourceConstraints
                |gmd:aggregationInfo"/>
      <xsl:copy-of
        select="gmd:spatialRepresentationType
                |gmd:spatialResolution
                |gmd:language
                |gmd:characterSet
                |gmd:topicCategory"/>

      <!-- Add topic categories according to INSPIRE themes. -->
      <xsl:variable name="ident" select="."/>
      <xsl:variable name="missing-topiccat">
        <xsl:for-each
          select="$inspire-th//skos:Concept[skos:prefLabel/@xml:lang = normalize-space($lang)]">
          <xsl:variable name="themeLabel"
                        select="skos:prefLabel[@xml:lang = normalize-space($lang)]"/>
          <!-- if in metadata -->
          <xsl:if test="$ident//gmd:keyword[gco:CharacterString = $themeLabel]">
            <xsl:variable name="themeId" select="@rdf:about"/>
            <!-- and corresponding topic cat does not exist in metadata -->
            <xsl:for-each select="$itheme-topiccat-map/entry[itheme=$themeId]/topiccat">
              <xsl:variable name="tcId" select="."/>
              <xsl:if
                test="count($ident//gmd:topicCategory[gmd:MD_TopicCategoryCode = normalize-space($tcId)]) != 1">
                <elem>
                  <xsl:value-of select="$tcId"/>
                </elem>
              </xsl:if>
            </xsl:for-each>
          </xsl:if>
        </xsl:for-each>
      </xsl:variable>

      <xsl:for-each-group select="exslt:node-set($missing-topiccat)//elem" group-by=".">
        <gmd:topicCategory>
          <gmd:MD_TopicCategoryCode>
            <xsl:value-of select="."/>
          </gmd:MD_TopicCategoryCode>
        </gmd:topicCategory>
      </xsl:for-each-group>

      <xsl:copy-of
        select="gmd:environmentDescription
                |gmd:extent
                |gmd:supplementalInformation"/>


      <!-- Service -->
      <xsl:copy-of
        select="srv:*
                "/>
    </xsl:copy>
  </xsl:template>


  <!-- Do a copy of every nodes and attributes -->
  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

  <!-- Remove geonet:* elements. -->
  <xsl:template match="geonet:*" priority="2"/>


</xsl:stylesheet>
