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
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
                xmlns:dct="http://purl.org/dc/terms/"
                xmlns:dcat="http://www.w3.org/ns/dcat#"
                xmlns:schema="http://schema.org/"
                xmlns:foaf="http://xmlns.com/foaf/0.1/"
                xmlns:skos="http://www.w3.org/2004/02/skos/core#"
                xmlns:vcard="http://www.w3.org/2006/vcard/ns#"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                xmlns:gn-fn-index="http://geonetwork-opensource.org/xsl/functions/index"
                xmlns:index="java:org.fao.geonet.kernel.search.EsSearchManager"
                xmlns:date-util="java:org.fao.geonet.utils.DateUtil"
                xmlns:util="java:org.fao.geonet.util.XslUtil"
                xmlns:saxon="http://saxon.sf.net/"
                extension-element-prefixes="saxon"
                exclude-result-prefixes="#all"
                version="2.0">

  <xsl:import href="common/index-utils.xsl"/>
  <xsl:import href="../layout/utility-tpl-multilingual.xsl"/>

  <xsl:output method="xml" indent="yes"/>

  <xsl:output name="default-serialize-mode"
              indent="no"
              omit-xml-declaration="yes"
              encoding="utf-8"
              escape-uri-attributes="yes"/>

  <xsl:variable name="metadata"
                select="//rdf:RDF"/>

  <xsl:variable name="allLanguages">
    <xsl:variable name="listOfLanguages">
      <xsl:call-template name="get-dcat2-other-languages"/>
    </xsl:variable>

    <xsl:for-each select="$listOfLanguages/*">
      <lang value="{@code}">
        <xsl:if test="position() = 1">
          <xsl:attribute name="id"
                         select="'default'"/>
        </xsl:if>
      </lang>
    </xsl:for-each>
  </xsl:variable>

  <xsl:template match="/">
    <xsl:variable name="dateStamp" select=".//dcat:CatalogRecord/dct:modified" />

    <xsl:for-each select=".//(dcat:Dataset|dcat:DataService|dcat:Catalog)">
      <doc>
        <xsl:copy-of select="gn-fn-index:add-field('docType', 'metadata')"/>

        <xsl:variable name="dateStamp"
                      select="date-util:convertToISOZuluDateTime(normalize-space($dateStamp))"/>
        <xsl:if test="$dateStamp != ''">
          <dateStamp><xsl:value-of select="$dateStamp"/></dateStamp>
        </xsl:if>

        <metadataIdentifier>
          <xsl:value-of select="@rdf:about"/>
        </metadataIdentifier>

        <xsl:variable name="isService" as="xs:boolean" select="name() = 'dcat:DataService'" />

        <!-- # Resource type -->
        <xsl:choose>
          <xsl:when test="$isService">
            <resourceType>service</resourceType>
          </xsl:when>
          <xsl:otherwise>
            <resourceType>dataset</resourceType>
          </xsl:otherwise>
        </xsl:choose>

        <xsl:copy-of select="gn-fn-index:add-multilingual-field('resourceTitle', dct:title, $allLanguages)"/>
        <xsl:copy-of select="gn-fn-index:add-multilingual-field('resourceAbstract', dct:description, $allLanguages)"/>

        <indexingDate>
          <xsl:value-of select="format-dateTime(current-dateTime(), $dateFormat)"/>
        </indexingDate>

        <xsl:for-each select="dct:language">
          <xsl:if test="position() = 1">
            <mainLanguage>
              <xsl:value-of select="replace(@rdf:resource, 'http://id.loc.gov/vocabulary/iso639-2/', '')"/>
            </mainLanguage>

            <!-- TODO: Review -->
            <!--<resourceLanguage>
              <xsl:value-of select="replace(@rdf:resource, 'http://id.loc.gov/vocabulary/iso639-2/', '')"/>
            </resourceLanguage>-->
          </xsl:if>

          <otherLanguage>
            <xsl:value-of select="replace(@rdf:resource, 'http://id.loc.gov/vocabulary/iso639-2/', '')"/>
          </otherLanguage>
        </xsl:for-each>

        <xsl:apply-templates mode="index-date" select="dct:issued" />
        <xsl:apply-templates mode="index-date" select="dct:modified" />

        <xsl:for-each select="dct:temporal/dct:PeriodOfTime">
          <xsl:variable name="start"
                        select="schema:startDate"/>
          <xsl:variable name="end"
                        select="schema:endDate"/>

          <xsl:variable name="zuluStartDate"
                        select="date-util:convertToISOZuluDateTime($start)"/>
          <xsl:variable name="zuluEndDate"
                        select="date-util:convertToISOZuluDateTime($end)"/>

          <xsl:if test="$zuluStartDate != '' and $zuluEndDate != ''">
            <resourceTemporalDateRange type="object">{
              "gte": "<xsl:value-of select="$zuluStartDate"/>"
              <xsl:if test="$start &lt; $end">
                ,"lte": "<xsl:value-of select="$zuluEndDate"/>"
              </xsl:if>
              }</resourceTemporalDateRange>
            <resourceTemporalExtentDateRange type="object">{
              "gte": "<xsl:value-of select="$zuluStartDate"/>"
              <xsl:if test="$start &lt; $end">
                ,"lte": "<xsl:value-of select="$zuluEndDate"/>"
              </xsl:if>
              }</resourceTemporalExtentDateRange>
          </xsl:if>
          <xsl:if test="$start &gt; $end">
            <indexingErrorMsg>Warning / Field resourceTemporalDateRange /
              Lower range bound '<xsl:value-of select="."/>' can not be
              greater than upper bound '<xsl:value-of select="$end"/>'.
              Date range not indexed.</indexingErrorMsg>
          </xsl:if>
        </xsl:for-each>

        <xsl:if test="dct:accrualPeriodicity">
          <xsl:element name="cl_maintenanceAndUpdateFrequency">
            <xsl:attribute name="type" select="'object'"/>
            <xsl:value-of select="gn-fn-index:add-multilingual-field-dcat2('accrualPeriodicity', dct:accrualPeriodicity/skos:Concept, $allLanguages, true())"/>
          </xsl:element>
        </xsl:if>

        <xsl:apply-templates mode="index-contact" select="dct:creator">
          <xsl:with-param name="fieldSuffix" select="''" />
        </xsl:apply-templates>

        <xsl:apply-templates mode="index-contact" select="dct:publisher">
          <xsl:with-param name="fieldSuffix" select="''" />
        </xsl:apply-templates>

        <xsl:for-each select="dcat:contactPoint">
          <xsl:apply-templates mode="index-contact" select=".">
            <xsl:with-param name="fieldSuffix" select="''" />
          </xsl:apply-templates>
        </xsl:for-each>

        <xsl:variable name="editorConfig"
                      select="document('../layout/config-editor.xml')"/>

        <xsl:variable name="thesaurusId" select="$editorConfig/editor/fields/for[@name='dcat:theme']/directiveAttributes/@thesaurus" />

        <xsl:variable name="key">
          <xsl:if test="$thesaurusId != ''">
            <xsl:value-of select="tokenize($thesaurusId, '\.')[last()]"/>
          </xsl:if>
        </xsl:variable>

        <!-- Index keyword characterString including multilingual ones
         and element like gmx:Anchor including the href attribute
         which may contains keyword identifier. -->
        <xsl:variable name="thesaurusField"
                      select="concat('th_', replace($key, '[^a-zA-Z0-9-_]', ''))"/>

        <xsl:element name="{$thesaurusField}Number">
          <xsl:value-of select="count(dcat:theme[skos:Concept])"/>
        </xsl:element>

        <xsl:for-each select="dcat:theme[skos:Concept]">
          <xsl:if test="normalize-space($key) != ''">
            <!-- TODO: Fix language -->
            <xsl:variable name="mainLanguage"
                          select="'eng'"/>

            <!-- TODO: Fix language -->
            <xsl:variable name="keywords"
                          select="skos:Concept"/>


            <xsl:call-template name="build-thesaurus-fields-dcat">
              <xsl:with-param name="thesaurus" select="$key"/>
              <xsl:with-param name="thesaurusId" select="$thesaurusId"/>
              <xsl:with-param name="keywords" select="$keywords"/>
              <xsl:with-param name="mainLanguage" select="$mainLanguage"/>
              <xsl:with-param name="allLanguages" select="$allLanguages"/>
            </xsl:call-template>
          </xsl:if>
        </xsl:for-each>

        <xsl:if test="dcat:theme[skos:Concept]">
          <allKeywords type="object">{
              <xsl:if test="normalize-space($key) != ''">
                <xsl:variable name="thesaurusField"
                              select="replace($key, '[^a-zA-Z0-9]', '')"/>

                "<xsl:value-of select="$thesaurusField"/>": {
                "id": "<xsl:value-of select="gn-fn-index:json-escape($thesaurusId)"/>",
                "link": "<xsl:value-of select="gn-fn-index:json-escape(@xlink:href)"/>",
                "keywords": [
                <xsl:for-each select="dcat:theme/skos:Concept">
                  <xsl:value-of select="gn-fn-index:add-multilingual-field-dcat2('keyword', ., $allLanguages)/text()"/>
                  <xsl:if test="position() != last()">,</xsl:if>
                </xsl:for-each>
                ]}
                <xsl:if test="position() != last()">,</xsl:if>
              </xsl:if>
            }</allKeywords>
        </xsl:if>

        <xsl:for-each select="dcat:spatialResolutionInMeters[. castable as xs:decimal]">
          <resolutionScaleDenominator>
            <xsl:value-of select="."/>
          </resolutionScaleDenominator>
        </xsl:for-each>




        <xsl:variable name="overviews"
                      select="dcat:distribution/*[dct:format/*/skos:prefLabel = 'WWW:OVERVIEW' and dcat:accessURL != '']"/>
        <xsl:copy-of select="gn-fn-index:add-field('hasOverview', if (count($overviews) > 0) then 'true' else 'false')"/>


        <xsl:variable name="isStoringOverviewInIndex"
                      select="true()"/>
        <xsl:for-each select="$overviews">
          <overview type="object">{
            "url": "<xsl:value-of select="normalize-space(dcat:accessURL)"/>"
            <xsl:if test="$isStoringOverviewInIndex">
              <xsl:variable name="data"
                            select="util:buildDataUrl(dcat:accessURL, 140)"/>
              <xsl:if test="$data != ''">,
                "data": "<xsl:value-of select="$data"/>"
              </xsl:if>
            </xsl:if>
            <xsl:if test="normalize-space(dct:title) != ''">,
              "text": <xsl:value-of select="gn-fn-index:add-multilingual-field('name', dct:title, $allLanguages, true())"/>
            </xsl:if>
            }</overview>
        </xsl:for-each>


        <xsl:for-each select="dcat:distribution/*[dct:format/*/skos:prefLabel != 'WWW:OVERVIEW' and (dcat:accessURL != '' or dcat:downloadURL != '')]">
          <xsl:variable name="transferGroup"
                        select="count(ancestor::dcat:distribution/preceding-sibling::dcat:distribution)"/>

          <xsl:variable name="protocol"
                        select="dct:format/*/skos:prefLabel/text()"/>

          <xsl:variable name="url"
                        select="(dcat:accessURL|dcat:downloadURL)"/>

          <linkUrl>
            <xsl:value-of select="$url"/>
          </linkUrl>
          <xsl:if test="normalize-space($protocol) != ''">
            <linkProtocol>
              <xsl:value-of select="$protocol"/>
            </linkProtocol>
          </xsl:if>
          <xsl:element name="linkUrlProtocol{replace($protocol[1], '[^a-zA-Z0-9]', '')}">
            <xsl:value-of select="$url"/>
          </xsl:element>

          <link type="object">{
            "protocol":"<xsl:value-of select="gn-fn-index:json-escape(($protocol)[1])"/>",
            "urlObject":{"default": "<xsl:value-of select="gn-fn-index:json-escape($url)"/>"},
            <xsl:if test="normalize-space(dct:title[1]) != ''">
              "nameObject": <xsl:value-of select="gn-fn-index:add-multilingual-field(
                                'name', dct:title[1], $allLanguages)"/>,
            </xsl:if>
            <xsl:if test="(normalize-space(dct:title[1]) = '') and (normalize-space(dct:description[1]) != '')">
              "nameObject": <xsl:value-of select="gn-fn-index:add-multilingual-field(
                                'name', dct:description[1], $allLanguages)"/>,
            </xsl:if>
            <xsl:if test="normalize-space(dct:description[1]) != ''">
              "descriptionObject": <xsl:value-of select="gn-fn-index:add-multilingual-field(
                                'description', dct:description[1], $allLanguages)"/>,
            </xsl:if>
            "function":"",
            "group": <xsl:value-of select="$transferGroup"/>
            }
            <!--Link object in Angular used to be
            //     name: linkInfos[0],
            //     title: linkInfos[0],
            //     url: linkInfos[2],
            //     desc: linkInfos[1],
            //     protocol: linkInfos[3],
            //     contentType: linkInfos[4],
            //     group: linkInfos[5] ? parseInt(linkInfos[5]) : undefined,
            //     applicationProfile: linkInfos[6]-->
          </link>
        </xsl:for-each>


      </doc>
    </xsl:for-each>
  </xsl:template>


  <xsl:template mode="index-date" match="dct:modified|dct:issued">
    <xsl:variable name="dateType" select="if (name() = 'dct:issued') then 'publication' else 'revision'" />

    <xsl:variable name="date"
                  select="string(.)"/>

    <xsl:variable name="zuluDateTime" as="xs:string?">
      <xsl:if test="gn-fn-index:is-isoDate(.)">
        <xsl:value-of select="date-util:convertToISOZuluDateTime(normalize-space($date))"/>
      </xsl:if>
    </xsl:variable>

    <xsl:choose>
      <xsl:when test="$zuluDateTime != ''">
        <xsl:element name="{$dateType}DateForResource">
          <xsl:value-of select="$zuluDateTime"/>
        </xsl:element>

        <resourceDate type="object">
          {"type": "<xsl:value-of select="$dateType"/>", "date": "<xsl:value-of select="$zuluDateTime"/>"}
        </resourceDate>

        <xsl:element name="{$dateType}YearForResource">
          <xsl:value-of select="substring($zuluDateTime, 0, 5)"/>
        </xsl:element>
        <xsl:element name="{$dateType}MonthForResource">
          <xsl:value-of select="substring($zuluDateTime, 0, 8)"/>
        </xsl:element>
      </xsl:when>
      <xsl:otherwise>
        <indexingErrorMsg>Warning / Date <xsl:value-of select="$dateType"/> with value '<xsl:value-of select="$date"/>' was not a valid date format.</indexingErrorMsg>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template mode="index-contact" match="*[foaf:Agent]">
    <xsl:param name="fieldSuffix" select="''" as="xs:string"/>

    <xsl:variable name="organisationName"
                  select="foaf:Agent/foaf:name"
                  as="xs:string*"/>

    <!-- TODO: Use language -->
    <xsl:variable name="role"
                  select="foaf:Agent/dct:type/skos:Concept/skos:prefLabel[1]"
                  as="xs:string?"/>
    <xsl:variable name="logo" select="''"/>
    <xsl:variable name="website" select="''"/>
    <xsl:variable name="email"
                  select="replace(foaf:Agent/foaf:mbox/@rdf:resource, 'mailto:', '')"/>
    <xsl:variable name="phone"
                  select="foaf:Agent/foaf:phone/@rdf:resource"/>
    <xsl:variable name="individualName"
                  select="''"/>
    <xsl:variable name="positionName"
                  select="''"/>
    <xsl:variable name="address" select="''"/>

    <xsl:if test="normalize-space($organisationName) != ''">
      <xsl:element name="Org{$fieldSuffix}">
        <xsl:value-of select="$organisationName"/>
      </xsl:element>
      <xsl:element name="{replace($role, '[^a-zA-Z0-9-]', '')}Org{$fieldSuffix}">
        <xsl:value-of select="$organisationName"/>
      </xsl:element>
    </xsl:if>
    <xsl:element name="contact{$fieldSuffix}">
      <!-- TODO: Can be multilingual -->
      <xsl:attribute name="type" select="'object'"/>{
      "organisation":"<xsl:value-of
      select="gn-fn-index:json-escape($organisationName)"/>",
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

  <xsl:template mode="index-contact" match="*[vcard:Organization]">
    <xsl:param name="fieldSuffix" select="''" as="xs:string"/>

    <xsl:variable name="organisationName"
                  select="vcard:Organization/vcard:organization-name"
                  as="xs:string*"/>

    <!-- TODO -->
    <xsl:variable name="role"
                  select="''"
                  as="xs:string?"/>
    <xsl:variable name="logo" select="''"/>
    <xsl:variable name="website" select="vcard:Organization/vcard:hasURL/@rdf:resource"/>
    <xsl:variable name="email"
                  select="vcard:Organization/vcard:hasEmail/@rdf:resource"/>
    <xsl:variable name="phone"
                  select="vcard:Organization/vcard:hasTelephone"/>
    <xsl:variable name="individualName"
                  select="vcard:Organization/vcard:fn"/>
    <xsl:variable name="positionName"
                  select="''"/>
    <xsl:variable name="address" select="''"/>

    <xsl:if test="normalize-space($organisationName) != ''">
      <xsl:element name="Org{$fieldSuffix}">
        <xsl:value-of select="$organisationName"/>
      </xsl:element>
      <xsl:element name="{replace($role, '[^a-zA-Z0-9-]', '')}Org{$fieldSuffix}">
        <xsl:value-of select="$organisationName"/>
      </xsl:element>
    </xsl:if>
    <xsl:element name="contact{$fieldSuffix}">
      <!-- TODO: Can be multilingual -->
      <xsl:attribute name="type" select="'object'"/>{
      "organisation":"<xsl:value-of
      select="gn-fn-index:json-escape($organisationName)"/>",
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


  <xsl:template name="build-thesaurus-fields-dcat">
    <xsl:param name="thesaurus" as="xs:string"/>
    <xsl:param name="thesaurusId" as="xs:string"/>
    <xsl:param name="keywords" as="node()*"/>
    <xsl:param name="mainLanguage" as="xs:string?"/>
    <xsl:param name="allLanguages" as="node()?"/>

    <!-- Index keyword characterString including multilingual ones
     and element like gmx:Anchor including the href attribute
     which may contains keyword identifier. -->
    <xsl:variable name="thesaurusField"
                  select="concat('th_', replace($thesaurus, '[^a-zA-Z0-9-_]', ''))"/>

    <xsl:if test="count($keywords) > 0">
      <xsl:element name="{$thesaurusField}">
        <xsl:attribute name="type" select="'object'"/>
        [<xsl:for-each select="$keywords">
        <xsl:value-of select="gn-fn-index:add-multilingual-field-dcat2('keyword', ., $allLanguages)/text()"/>
        <xsl:if test="position() != last()">,</xsl:if>
      </xsl:for-each>]
      </xsl:element>

      <xsl:variable name="thesaurusTree" as="node()">
        <values>
          <xsl:for-each select="$keywords">
            <xsl:variable name="nodes" as="node()*">
              <xsl:copy-of select="."/>

              <!-- TODO: Language -->
              <!--<xsl:variable name="uri"
                            select="util:getKeywordUri((skos:prefLabel[1])[1], $thesaurusId, $mainLanguage)"/>
              <xsl:if test="$uri != ''">
                <xsl:attribute name="xlink:href" select="$uri"/>
              </xsl:if>-->

            </xsl:variable>


            <xsl:for-each select="$nodes">
              <xsl:variable name="keywordTree" as="node()*">
                <xsl:call-template name="get-keyword-tree-values-dcat">
                  <!-- TODO: Language -->
                  <xsl:with-param name="keyword"
                                  select="skos:prefLabel[1]"/>
                  <xsl:with-param name="thesaurus"
                                  select="$thesaurusId"/>
                  <xsl:with-param name="language"
                                  select="$mainLanguage"/>
                </xsl:call-template>
              </xsl:variable>

              <xsl:variable name="type"
                            select="'default'"/>
              <xsl:for-each select="$keywordTree[. != '']">
                <xsl:element name="{$type}">
                  <xsl:value-of select="concat($doubleQuote, gn-fn-index:json-escape(.), $doubleQuote)"/>
                </xsl:element>
              </xsl:for-each>
            </xsl:for-each>
          </xsl:for-each>
        </values>
      </xsl:variable>


      <xsl:if test="count($thesaurusTree/*) > 0">
        <xsl:element name="{$thesaurusField}_tree">
          <xsl:attribute name="type" select="'object'"/>{
          <xsl:variable name="defaults"
                        select="distinct-values($thesaurusTree/default)"/>
          <xsl:variable name="keys"
                        select="distinct-values($thesaurusTree/key)"/>

          <xsl:if test="count($defaults) > 0">"default": [
            <xsl:for-each select="$defaults">
              <xsl:sort select="."/>
              <xsl:value-of select="."/><xsl:if test="position() != last()">,</xsl:if>
            </xsl:for-each>
            ]<xsl:if test="count($keys) > 0">,</xsl:if>
          </xsl:if>
          <xsl:if test="count($keys) > 0">"key": [
            <xsl:for-each select="$keys">
              <xsl:sort select="."/>
              <xsl:value-of select="."/><xsl:if test="position() != last()">,</xsl:if>
            </xsl:for-each>
            ]
          </xsl:if>
          }</xsl:element>
      </xsl:if>
    </xsl:if>
  </xsl:template>


  <xsl:template name="get-keyword-tree-values-dcat" as="node()*">
    <xsl:param name="keyword" as="xs:string"/>
    <xsl:param name="thesaurus" as="xs:string"/>
    <xsl:param name="language" as="xs:string?" select="'eng'"/>

    <xsl:variable name="paths" as="node()*">
      <xsl:variable name="keywordsWithHierarchy"
                    select="util:getKeywordHierarchy(
                              normalize-space($keyword), $thesaurus, $language)"/>

      <xsl:for-each select="$keywordsWithHierarchy">
        <xsl:variable name="path" select="tokenize(., '\^')"/>

        <xsl:for-each select="$path">
          <xsl:variable name="position"
                        select="position()"/>
          <value><xsl:value-of select="string-join($path[position() &lt;= $position], '^')"/></value>
        </xsl:for-each>
      </xsl:for-each>
    </xsl:variable>

    <xsl:copy-of select="$paths"/>
  </xsl:template>


  <xsl:function name="gn-fn-index:add-multilingual-field-dcat2" as="node()*">
    <xsl:param name="fieldName" as="xs:string"/>
    <xsl:param name="elements" as="node()*"/>
    <xsl:param name="languages" as="node()?"/>
    <xsl:copy-of select="gn-fn-index:add-multilingual-field-dcat2($fieldName, $elements, $languages, false())"/>
  </xsl:function>

  <xsl:function name="gn-fn-index:add-multilingual-field-dcat2" as="node()*">
    <xsl:param name="fieldName" as="xs:string"/>
    <xsl:param name="elements" as="node()*"/>
    <xsl:param name="languages" as="node()?"/>
    <xsl:param name="asJson" as="xs:boolean?"/>

    <xsl:variable name="mainLanguage"
                  select="$languages/lang[@id='default']/@value"/>

    <xsl:variable name="isArray"
                  select="count($elements[not(@xml:lang)]) > 1"/>

    <xsl:for-each select="$elements/skos:prefLabel">
      <xsl:variable name="element" select="."/>
      <xsl:variable name="textObject" as="node()*">
        <xsl:if test="position() = 1">
          <value><xsl:value-of select="concat($doubleQuote, 'default', $doubleQuote, ':',
                                             $doubleQuote, gn-fn-index:json-escape(.), $doubleQuote)"/></value>
          <xsl:for-each select="$elements/skos:prefLabel">
            <value><xsl:value-of select="concat($doubleQuote, 'lang', @xml:lang, $doubleQuote, ':',
                                             $doubleQuote, gn-fn-index:json-escape(.), $doubleQuote)"/></value>
          </xsl:for-each>
        </xsl:if>
      </xsl:variable>

      <xsl:if test="$textObject != ''">
        <xsl:choose>
          <xsl:when test="$asJson">
            <xsl:if test="$isArray and position() = 1">[</xsl:if>
            {<xsl:value-of select="string-join($textObject/text(), ', ')"/>}
            <xsl:if test="$isArray and position() != last()">,</xsl:if>
            <xsl:if test="$isArray and position() = last()">]</xsl:if>
          </xsl:when>
          <xsl:otherwise>
            <xsl:element name="{$fieldName}Object">
              <xsl:attribute name="type" select="'object'"/>
              {<xsl:value-of select="string-join($textObject/text(), ', ')"/>}
            </xsl:element>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:if>
    </xsl:for-each>
  </xsl:function>
</xsl:stylesheet>
