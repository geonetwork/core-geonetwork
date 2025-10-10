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
                xmlns:gco="http://www.isotc211.org/2005/gco"
                xmlns:gmx="http://www.isotc211.org/2005/gmx"
                xmlns:gts="http://www.isotc211.org/2005/gts"
                xmlns:gml="http://www.opengis.net/gml/3.2"
                xmlns:gml320="http://www.opengis.net/gml"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                xmlns:srv="http://www.isotc211.org/2005/srv"
                xmlns:tr="java:org.fao.geonet.api.records.formatters.SchemaLocalizations"
                xmlns:gn-fn-render="http://geonetwork-opensource.org/xsl/functions/render"
                xmlns:gn-fn-metadata="http://geonetwork-opensource.org/xsl/functions/metadata"
                xmlns:gn-fn-iso19139="http://geonetwork-opensource.org/xsl/functions/profiles/iso19139"
                xmlns:xslUtils="java:org.fao.geonet.util.XslUtil"
                xmlns:saxon="http://saxon.sf.net/"
                version="2.0"
                extension-element-prefixes="saxon"
                exclude-result-prefixes="#all">

  <!-- This formatter render an ISO19139 record based on the
  editor configuration file.

  The layout is made in 2 modes:
  * render-field taking care of elements (eg. sections, label)
  * render-value taking care of element values (eg. characterString, URL)

  3 levels of priority are defined: 100, 50, none

  -->
 <!-- tr is defined at  core-geonetwork/services/src/main/java/org/fao/geonet/api/records/formatters/SchemaLocalizations.java -->

  <!-- Load the editor configuration to be able
  to render the different views -->
  <xsl:variable name="configuration"
                select="document('../../layout/config-editor.xml')"/>

 <!-- Required for utility-fn.xsl -->
  <xsl:variable name="editorConfig"
                select="document('../../layout/config-editor.xml')"/>

  <!-- Some utility -->
  <xsl:include href="../../layout/evaluate.xsl"/>
  <xsl:include href="../../layout/utility-tpl-multilingual.xsl"/>
  <xsl:include href="../../layout/utility-fn.xsl"/>
  <xsl:include href="../../formatter/jsonld/iso19139-to-jsonld.xsl"/>
  <xsl:include href="../../formatter/citation/base.xsl"/>
  <xsl:include href="../../../iso19115-3.2018/formatter/citation/common.xsl"/>
  <xsl:include href="../../../iso19115-3.2018/formatter/xsl-view/common.xsl"/>

  <!-- The core formatter XSL layout based on the editor configuration -->
  <xsl:include href="sharedFormatterDir/xslt/render-layout.xsl"/>
  <!--<xsl:include href="../../../../../data/formatter/xslt/render-layout.xsl"/>-->

  <!-- Define the metadata to be loaded for this schema plugin-->
  <xsl:variable name="metadata"
                select="/root/(gmd:MD_Metadata|*[@gco:isoType = 'gmd:MD_Metadata'])"/>

  <xsl:variable name="langId" select="gn-fn-iso19139:getLangId($metadata, $language)"/>

  <xsl:variable name="allLanguages">
    <xsl:call-template name="get-iso19139-other-languages"/>
  </xsl:variable>

  <!-- Ignore some fields displayed in header or in right column -->
  <xsl:template mode="render-field"
                match="gmd:graphicOverview|gmd:abstract|gmd:identificationInfo/*/gmd:citation/*/gmd:title"
                priority="2000"/>

  <!-- Specific schema rendering -->
  <xsl:template mode="getMetadataTitle" match="gmd:MD_Metadata|*[@gco:isoType = 'gmd:MD_Metadata']">
    <xsl:for-each select="gmd:identificationInfo/*/gmd:citation/*/gmd:title">
      <xsl:call-template name="localised">
        <xsl:with-param name="langId" select="$langId"/>
      </xsl:call-template>
    </xsl:for-each>
  </xsl:template>

  <xsl:template mode="getMetadataAbstract" match="gmd:MD_Metadata|*[@gco:isoType = 'gmd:MD_Metadata']">
    <xsl:for-each select="gmd:identificationInfo/*/gmd:abstract">

      <xsl:variable name="txt">
        <xsl:call-template name="localised">
          <xsl:with-param name="langId" select="$langId"/>
        </xsl:call-template>
      </xsl:variable>
      <xsl:call-template name="addLineBreaksAndHyperlinks">
        <xsl:with-param name="txt" select="$txt"/>
      </xsl:call-template>
    </xsl:for-each>
  </xsl:template>


  <xsl:template mode="getTags" match="gmd:MD_Metadata|*[@gco:isoType = 'gmd:MD_Metadata']">
    <xsl:param name="byThesaurus" select="false()"/>

    <xsl:variable name="tags">
      <xsl:for-each select="$metadata/gmd:identificationInfo/*/gmd:descriptiveKeywords/
                                          *[
                                          gmd:type/*/@codeListValue != 'place'
                                            and normalize-space(string-join(gmd:keyword//text(), '')) != ''
                                            and (not(gmd:thesaurusName/*/gmd:identifier/*/gmd:code)
                                            or gmd:thesaurusName/*/gmd:identifier/*/gmd:code/*/
                                                text() != '')]">
        <xsl:variable name="thesaurusTitle">
          <xsl:for-each select="gmd:thesaurusName/*/gmd:title">
            <xsl:call-template name="localised">
              <xsl:with-param name="langId" select="$langId"/>
            </xsl:call-template>
          </xsl:for-each>
        </xsl:variable>
        <xsl:for-each select="gmd:keyword">
          <tag thesaurus="{$thesaurusTitle}">
            <xsl:call-template name="localised">
              <xsl:with-param name="langId" select="$langId"/>
            </xsl:call-template>
          </tag>
        </xsl:for-each>
      </xsl:for-each>
    </xsl:variable>

    <xsl:if test="count($tags/tag) > 0">
      <section class="gn-md-side-social">
        <h2>
          <i class="fa fa-fw fa-tag"></i>
          <span>
            <xsl:value-of select="$schemaStrings/noThesaurusName"/>
          </span>
        </h2>

        <xsl:choose>
          <xsl:when test="$byThesaurus">
            <xsl:for-each-group select="$tags/tag" group-by="@thesaurus">
              <xsl:sort select="@thesaurus"/>
              <xsl:if test="current-grouping-key() != ''">
                <xsl:value-of select="current-grouping-key()"/><br/>
              </xsl:if>

              <xsl:for-each select="current-group()">
                <xsl:sort select="."/>
                <a class="btn btn-default btn-xs"
                   href='#/search?query_string=%7B"tag.\\*":%7B"{.}":true%7D%7D'>
                  <xsl:copy-of select="."/>
                </a>
              </xsl:for-each>
              <xsl:if test="position() != last()">
                <hr/>
              </xsl:if>
            </xsl:for-each-group>
          </xsl:when>
          <xsl:otherwise>
            <xsl:for-each select="$tags/tag">
              <xsl:sort select="."/>
              <a class="btn btn-default btn-xs"
                 href='#/search?query_string=%7B"tag.\\*":%7B"{.}":true%7D%7D'>
                <xsl:copy-of select="."/>
              </a>
            </xsl:for-each>
          </xsl:otherwise>
        </xsl:choose>

      </section>
    </xsl:if>
  </xsl:template>

  <xsl:template mode="getMetadataHierarchyLevel" match="gmd:MD_Metadata|*[@gco:isoType = 'gmd:MD_Metadata']">
    <xsl:value-of select="gmd:hierarchyLevel/gmd:MD_ScopeCode/@codeListValue"/>
  </xsl:template>

  <xsl:template mode="getMetadataThumbnail" match="gmd:MD_Metadata">
    <xsl:value-of select="gmd:identificationInfo/*/gmd:graphicOverview[1]/gmd:MD_BrowseGraphic/gmd:fileName/gco:CharacterString"/>
  </xsl:template>

  <xsl:template mode="getExtent" match="gmd:MD_Metadata|*[@gco:isoType = 'gmd:MD_Metadata']">
    <xsl:if test=".//gmd:identificationInfo/*/gmd:extent/*/gmd:geographicElement[gmd:EX_GeographicBoundingBox or gmd:EX_BoundingPolygon]">
      <section class="gn-md-side-extent">
        <h2>
          <i class="fa fa-fw fa-map-marker"></i>
          <span>
            <xsl:value-of select="$schemaStrings/spatialExtent"/>
          </span>
        </h2>

        <xsl:choose>
          <xsl:when test=".//gmd:EX_BoundingPolygon">
            <xsl:copy-of select="gn-fn-render:extent($metadataUuid)"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:copy-of select="gn-fn-render:bboxes(.//gmd:EX_GeographicBoundingBox)"/>
          </xsl:otherwise>
        </xsl:choose>
      </section>
    </xsl:if>
  </xsl:template>

  <xsl:template mode="getOverviews" match="gmd:MD_Metadata|*[@gco:isoType = 'gmd:MD_Metadata']">
    <section class="gn-md-side-overview">
      <h2>
        <i class="fa fa-fw fa-image"></i>
        <span>
          <xsl:value-of select="$schemaStrings/overviews"/>
        </span>
      </h2>

      <xsl:variable name="imgOnError" as="xs:string?"
                    select="if (count(gmd:identificationInfo/*/gmd:graphicOverview/*) > 1)
                            then 'this.onerror=null; this.parentElement.style.display=''none'';'
                            else 'this.onerror=null; $(''.gn-md-side-overview'').hide();'"/>

      <xsl:for-each select="gmd:identificationInfo/*/gmd:graphicOverview/*">
        <div>
          <img data-gn-img-modal="md"
               class="gn-img-thumbnail"
               alt="{$schemaStrings/overview}"
               src="{gmd:fileName/*}"
               onerror="{$imgOnError}" />

          <xsl:for-each select="gmd:fileDescription">
            <div class="gn-img-thumbnail-caption">
              <xsl:call-template name="localised">
                <xsl:with-param name="langId" select="$langId"/>
              </xsl:call-template>
            </div>
          </xsl:for-each>
        </div>
      </xsl:for-each>
    </section>
  </xsl:template>

  <xsl:template mode="getMetadataHeader" match="gmd:MD_Metadata|*[@gco:isoType = 'gmd:MD_Metadata']">
    <div class="gn-abstract">
      <xsl:for-each select="gmd:identificationInfo/*/gmd:abstract">
        <xsl:variable name="txt">
          <xsl:call-template name="localised">
            <xsl:with-param name="langId" select="$langId"/>
          </xsl:call-template>
        </xsl:variable>
         <xsl:call-template name="addLineBreaksAndHyperlinks">
            <xsl:with-param name="txt" select="$txt"/>
        </xsl:call-template>
      </xsl:for-each>
    </div>

    <xsl:if test="$withJsonLd = 'true'">
      <script type="application/ld+json">
        <xsl:apply-templates mode="getJsonLD"
                             select="$metadata"/>
      </script>
    </xsl:if>
  </xsl:template>


  <xsl:template mode="getMetadataCitation" match="gmd:MD_Metadata|*[@gco:isoType = 'gmd:MD_Metadata']">
    <xsl:variable name="displayCitation"
                  select="true()"/>
    <xsl:variable name="doiUrl"
                  select=".//gmd:onLine/*[gmd:protocol/* = ('WWW:LINK-1.0-http--metadata-URL', 'WWW:LINK-1.0-http--publication-URL', 'DOI')]/gmd:linkage/gmd:URL"/>
    <xsl:variable name="landingPageUrl"
                  select="if ($doiUrl != '') then $doiUrl else concat($nodeUrl, 'api/records/', $metadataUuid)"/>

    <xsl:if test="$displayCitation">
      <xsl:variable name="forcedCitation"
                    select="notexist"/>
      <!--
      Some catalogue want to be able to override default generated citation
      using a metadata field.
      select="gmd:identificationInfo/*/gmd:citation/*/gmd:otherCitationDetails"/>
      -->
      <xsl:choose>
        <!-- Landing page case -->
        <xsl:when test="$language = 'all'">
          <xsl:variable name="citationInfo">
            <xsl:call-template name="get-iso19139-citation">
              <xsl:with-param name="metadata" select="$metadata"/>
              <xsl:with-param name="language" select="$language"/>
            </xsl:call-template>
          </xsl:variable>
          <blockquote>
            <div class="row">
              <div class="col-md-1">
                <i class="fa fa-quote-left"></i>
              </div>
              <div class="col-md-11">
                <h2 title="{$schemaStrings/citationProposal-help}">
                  <xsl:value-of select="$schemaStrings/citationProposal"/>
                </h2>

                <xsl:choose>
                  <xsl:when test="count($forcedCitation) > 0">
                    <xsl:for-each select="$forcedCitation">
                      <xsl:call-template name="localised">
                        <xsl:with-param name="langId" select="$langId"/>
                      </xsl:call-template>
                    </xsl:for-each>
                  </xsl:when>
                  <xsl:otherwise>
                    <xsl:apply-templates mode="citation" select="$citationInfo"/>
                  </xsl:otherwise>
                </xsl:choose>
              </div>
            </div>
          </blockquote>
        </xsl:when>
        <xsl:when test="count($forcedCitation) > 0">
          <xsl:variable name="txt">
            <xsl:for-each select="$forcedCitation">
              <xsl:call-template name="localised">
                <xsl:with-param name="langId" select="$langId"/>
              </xsl:call-template>
            </xsl:for-each>
          </xsl:variable>
          <xsl:variable name="citation">
            <xsl:call-template name="addLineBreaksAndHyperlinks">
              <xsl:with-param name="txt" select="$txt"/>
            </xsl:call-template>
          </xsl:variable>

          <div data-ng-if="showCitation"
               data-gn-metadata-citation=""
               data-text="{$citation}">
          </div>
        </xsl:when>
        <xsl:otherwise>
          <div data-ng-if="showCitation"
               data-gn-metadata-citation="md">

          </div>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:if>
  </xsl:template>

  <!-- Most of the elements are ... -->
  <xsl:template mode="render-field"
                match="*[gco:Integer|gco:Decimal|
       gco:Boolean|gco:Real|gco:Measure|gco:Length|gco:Distance|
       gco:Angle|gmx:FileName|
       gco:Scale|gco:Record|gco:RecordType|gmx:MimeFileType|gmd:URL|
       gco:LocalName|gmd:PT_FreeText|
       gco:Date|gco:DateTime|gts:TM_PeriodDuration != '']"
                priority="50">
    <xsl:param name="fieldName" select="''" as="xs:string"/>

    <xsl:if test="normalize-space(string-join(*, '')) != ''">
      <dl>
        <dt>
          <xsl:call-template name="render-field-label">
            <xsl:with-param name="fieldName" select="$fieldName"/>
            <xsl:with-param name="languages" select="$allLanguages"/>
          </xsl:call-template>
        </dt>
        <dd>
          <xsl:apply-templates mode="render-value" select="*|*/@codeListValue"/>
          <xsl:apply-templates mode="render-value" select="@*"/>
        </dd>
      </dl>
    </xsl:if>
  </xsl:template>

  <!-- Codelist elements -->
  <xsl:template mode="render-field"
                match="*[*/@codeListValue]"
                priority="50">
    <xsl:param name="fieldName" select="''" as="xs:string"/>

    <xsl:if test="normalize-space(string-join(*|*/@codeListValue, '')) != ''">
      <dl>
        <dt>
          <xsl:call-template name="render-field-label">
            <xsl:with-param name="fieldName" select="$fieldName"/>
            <xsl:with-param name="languages" select="$allLanguages"/>
          </xsl:call-template>
        </dt>
        <dd>
          <xsl:choose>
            <xsl:when test="normalize-space(*/@codeListValue) != ''">
              <xsl:apply-templates mode="render-value" select="*/@codeListValue"/>
            </xsl:when>
            <xsl:otherwise>
              <xsl:apply-templates mode="render-value" select="*"/>
            </xsl:otherwise>
          </xsl:choose>
          <xsl:apply-templates mode="render-value" select="@*"/>
        </dd>
      </dl>
    </xsl:if>
  </xsl:template>

  <!-- Template for boolean fields that can be empty: no gco:Boolean subelement and @gco:nilReason attribute -->
  <!-- Uncomment and add required fields to be handled in the match clause -->
  <!--<xsl:template mode="render-field" match="gmd:pass[@gco:nilReason and not(gco:Boolean)]"
                priority="100">
    <xsl:param name="fieldName" select="''" as="xs:string"/>

    <dl>
      <dt>
        <xsl:value-of select="if ($fieldName)
                                  then $fieldName
                                  else tr:node-label(tr:create($schema), name(), null)"/>
      </dt>
      <dd>
        <xsl:value-of select="$schemaStrings/nilValue"/>
      </dd>
    </dl>
  </xsl:template>-->

  <xsl:template mode="render-field"
                match="*[gmx:Anchor]|*[normalize-space(gco:CharacterString) != '']|
                       gml:beginPosition[. != '']|gml:endPosition[. != '']|
                       gml320:beginPosition[. != '']|gml320:endPosition[. != '']|
                       gml:begin[. != '']|gml320:begin[. != '']|
                       gml:end[. != '']|gml320:end[. != '']"
                priority="50">
    <xsl:param name="fieldName" select="''" as="xs:string"/>

    <dl>
      <dt>
        <xsl:call-template name="render-field-label">
          <xsl:with-param name="fieldName" select="$fieldName"/>
        </xsl:call-template>
      </dt>
      <dd>

        <xsl:apply-templates mode="render-value" select="."/>
        <xsl:apply-templates mode="render-value" select="@*"/>
      </dd>
    </dl>
  </xsl:template>


  <xsl:template name="render-field-label">
    <xsl:param name="fieldName" select="''" as="xs:string" required="no"/>
    <xsl:param name="languages" as="node()*" required="no"/>
    <xsl:param name="contextLabel" as="attribute()?" required="no"/>

    <xsl:variable name="name"
                  select="name()"/>

    <xsl:variable name="contextXpath"
                  select="gn-fn-metadata:getXPath(.)" />
    <xsl:variable name="contextParent"
                  select="name(..)"/>

    <xsl:choose>
      <!-- eg. for codelist, display label in all record languages -->
      <xsl:when test="$fieldName = '' and $language = 'all' and count($languages/lang) > 0">
        <xsl:for-each select="$languages/lang">
          <div xml:lang="{@code}">
            <xsl:value-of select="tr:nodeLabel(tr:create($schema, @code), $name, $contextParent, $contextXpath)"/>
            <xsl:if test="$contextLabel">
              <xsl:variable name="extraLabel">
                <xsl:apply-templates mode="render-value"
                                     select="$contextLabel">
                  <xsl:with-param name="forcedLanguage" select="@code"/>
                </xsl:apply-templates>
              </xsl:variable>
              <xsl:value-of select="concat(' (', $extraLabel, ')')"/>
            </xsl:if>
          </div>
        </xsl:for-each>
      </xsl:when>
      <!-- eg. for multilingual element, display label in all translations -->
      <xsl:when test="$fieldName = '' and $language = 'all' and gmd:PT_FreeText">
        <xsl:for-each select="gmd:PT_FreeText/gmd:textGroup/gmd:LocalisedCharacterString[. != '']">
          <xsl:variable name="id"
                        select="replace(@locale, '#', '')"/>
          <xsl:variable name="lang3"
                        select="$metadata//gmd:locale/*[@id = $id]/gmd:languageCode/*/@codeListValue"/>
          <div xml:lang="{$lang3}">
            <xsl:value-of select="tr:nodeLabel(tr:create($schema, $lang3), $name, $contextParent, $contextXpath)"/>
          </div>
        </xsl:for-each>
      </xsl:when>
      <xsl:otherwise>
        <!-- Overriden label or element name in current UI language. -->
        <xsl:value-of select="if ($fieldName)
                                then $fieldName
                                else  tr:nodeLabel(tr:create($schema), $name, $contextParent, $contextXpath)"/>
        <xsl:if test="$contextLabel">
          <xsl:variable name="extraLabel">
            <xsl:apply-templates mode="render-value"
                                 select="$contextLabel"/>
          </xsl:variable>
          <xsl:value-of select="concat(' (', $extraLabel, ')')"/>
        </xsl:if>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>


  <!-- Some elements are only containers so bypass them
       Excluded gmd:MD_DigitalTransferOptions, otherwise is rendered differently
       when contains 1 or more gmd:onLine elements. Probably template affects
       other similar container elements.
  -->
  <xsl:template mode="render-field"
                match="*[
                          name() != 'gmd:MD_DigitalTransferOptions' and
                          count(gmd:*[name() != 'gmd:PT_FreeText']) = 1 and
                          count(*/@codeListValue) = 0
                        ]"
                priority="50">
    <xsl:apply-templates mode="render-value" select="@*"/>
    <xsl:apply-templates mode="render-field" select="*"/>
  </xsl:template>

  <!-- Some major sections are boxed -->
  <xsl:template mode="render-field"
                match="*[name() = $configuration/editor/fieldsWithFieldset/name
    or @gco:isoType = $configuration/editor/fieldsWithFieldset/name]|
      gmd:report/*|
      gmd:result/*|
      gmd:extent[name(..)!='gmd:EX_TemporalExtent']|
      gmd:transferOptions/*|
      *[$isFlatMode = false() and gmd:* and
        not(gco:CharacterString) and not(gmd:URL)]">
    <div class="entry name">
      <h2>
        <xsl:call-template name="render-field-label">
          <xsl:with-param name="languages" select="$allLanguages"/>
        </xsl:call-template>
        <xsl:apply-templates mode="render-value"
                             select="@*"/>
      </h2>
      <div class="target">
        <xsl:choose>
          <xsl:when test="count(*) > 0">
            <xsl:apply-templates mode="render-field" select="*"/>
          </xsl:when>
          <xsl:otherwise>
            No information provided.
          </xsl:otherwise>
        </xsl:choose>
      </div>
    </div>

  </xsl:template>


  <!-- Bbox is displayed with an overview and the geom displayed on it
  and the coordinates displayed around -->
  <xsl:template mode="render-field"
                match="gmd:EX_GeographicBoundingBox[gmd:eastBoundLongitude/gco:Decimal castable as xs:double
                                                   and gmd:southBoundLatitude/gco:Decimal castable as xs:double
                                                   and gmd:westBoundLongitude/gco:Decimal castable as xs:double
                                                   and gmd:northBoundLatitude/gco:Decimal castable as xs:double]">
    <xsl:copy-of select="gn-fn-render:bbox(
                            xs:double(gmd:westBoundLongitude/gco:Decimal),
                            xs:double(gmd:southBoundLatitude/gco:Decimal),
                            xs:double(gmd:eastBoundLongitude/gco:Decimal),
                            xs:double(gmd:northBoundLatitude/gco:Decimal))"/>

    <br/>
    <br/>
  </xsl:template>




  <xsl:template mode="render-field"
                match="gmd:EX_BoundingPolygon/gmd:polygon">
    <xsl:copy-of select="gn-fn-render:extent($metadataUuid,
        count(ancestor::gmd:extent/preceding-sibling::gmd:extent/*/*[local-name() = 'geographicElement']/*) +
        count(../../preceding-sibling::gmd:geographicElement) + 1)"/>
    <br/>
    <br/>
  </xsl:template>


  <!-- Display spatial extents containing bounding polygons on a map -->
  <xsl:template mode="render-field"
                match="gmd:EX_Extent[gmd:geographicElement/*/gmd:polygon]"
                priority="100">
    <div class="entry name">
      <h2>
        <xsl:call-template name="render-field-label">
          <xsl:with-param name="languages" select="$allLanguages"/>
        </xsl:call-template>
        <xsl:apply-templates mode="render-value"
                             select="@*"/>
      </h2>
      <div class="target">

        <xsl:apply-templates mode="render-field"
                             select="gmd:description"/>

        <xsl:apply-templates mode="render-field"
                             select="gmd:geographicElement[not(gmd:EX_GeographicDescription)]"/>

        <!-- Display any included geographic descriptions separately after displayed map -->
        <xsl:apply-templates mode="render-field"
                             select="gmd:geographicElement[gmd:EX_GeographicDescription]"/>

        <xsl:apply-templates mode="render-field"
                             select="gmd:temporalElement"/>
        <xsl:apply-templates mode="render-field"
                             select="gmd:verticalElement"/>

      </div>
    </div>
  </xsl:template>

  <!-- A contact is displayed with its role as header -->
  <xsl:template mode="render-field"
                match="*[gmd:CI_ResponsibleParty]"
                priority="100">
    <xsl:param name="layout"
               required="no"/>


    <xsl:variable name="email">
      <xsl:for-each select="*/gmd:contactInfo/
                                      */gmd:address/*/gmd:electronicMailAddress">
        <xsl:apply-templates mode="render-value-no-breaklines"
                             select="."/><xsl:if test="position() != last()">, </xsl:if>
      </xsl:for-each>
    </xsl:variable>

    <xsl:variable name="role" select="*/gmd:role/gmd:CI_RoleCode/@codeListValue" />

    <!-- Display name is <org name> - <individual name> (<position name>) -->
    <!-- with separator/parentheses as required -->
    <xsl:variable name="displayName">
      <xsl:if test="*/gmd:organisationName[normalize-space(.) != '']">
        <xsl:apply-templates mode="render-value-no-breaklines" select="*/gmd:organisationName"/>
      </xsl:if>
      <xsl:if test="*/gmd:organisationName[normalize-space(.) != ''] and */gmd:individualName[normalize-space(.) != '']|*/gmd:positionName[normalize-space(.) != '']"> - </xsl:if>
      <xsl:if test="*/gmd:individualName[normalize-space(.) != '']">
        <xsl:apply-templates mode="render-value-no-breaklines" select="*/gmd:individualName"/>
      </xsl:if>
      <xsl:if test="*/gmd:positionName[normalize-space(.) != '']">
        <xsl:choose>
          <xsl:when test="*/gmd:individualName[normalize-space(.) != '']">
            (<xsl:apply-templates mode="render-value-no-breaklines" select="*/gmd:positionName"/>)
          </xsl:when>
          <xsl:otherwise>
            <xsl:apply-templates mode="render-value-no-breaklines" select="*/gmd:positionName"/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:if>
    </xsl:variable>

    <xsl:choose>
      <xsl:when test="$layout = 'short'">
        <xsl:copy-of select="$displayName"/>
      </xsl:when>
      <xsl:otherwise>
        <div class="gn-contact">
          <strong>

            <xsl:apply-templates mode="render-value"
                                 select="*/gmd:role/*/@codeListValue"/>
          </strong>
          <address>
              <xsl:choose>
                <xsl:when test="$email">
                  <i class="fa fa-fw fa-envelope">&#160;</i>
                  <a href="mailto:{normalize-space($email)}">
                    <xsl:copy-of select="$displayName"/>
                  </a>
                </xsl:when>
                <xsl:otherwise>
                  <xsl:copy-of select="$displayName"/>
                </xsl:otherwise>
              </xsl:choose>
            <br/>
            <xsl:for-each select="*/gmd:contactInfo/*">
              <xsl:for-each select="gmd:address/*[
                  gmd:deliveryPoint[normalize-space(.) != ''] or
                  gmd:city[normalize-space(.) != ''] or
                  gmd:administrativeArea[normalize-space(.) != ''] or
                  gmd:postalCode[normalize-space(.) != ''] or
                  gmd:country[normalize-space(.) != '']]">
                <div>
                <i class="fa fa-fw fa-map-marker"></i>
                  <xsl:for-each select="gmd:deliveryPoint[normalize-space(.) != '']">
                    <xsl:apply-templates mode="render-value-no-breaklines" select="."/>,
                  </xsl:for-each>
                  <xsl:for-each select="gmd:city[normalize-space(.) != '']">
                    <xsl:apply-templates mode="render-value-no-breaklines" select="."/>,
                  </xsl:for-each>
                  <xsl:for-each select="gmd:administrativeArea[normalize-space(.) != '']">
                    <xsl:apply-templates mode="render-value-no-breaklines" select="."/>,
                  </xsl:for-each>
                  <xsl:for-each select="gmd:postalCode[normalize-space(.) != '']">
                    <xsl:apply-templates mode="render-value-no-breaklines" select="."/>,
                  </xsl:for-each>
                  <xsl:for-each select="gmd:country[normalize-space(.) != '']">
                    <xsl:apply-templates mode="render-value-no-breaklines" select="."/>
                  </xsl:for-each>
                </div>
              </xsl:for-each>
            </xsl:for-each>
            <xsl:for-each select="*/gmd:contactInfo/*">
              <xsl:for-each select="gmd:phone/*/gmd:voice[normalize-space(.) != '']">
                  <xsl:variable name="phoneNumber">
                    <xsl:apply-templates mode="render-value-no-breaklines" select="."/>
                  </xsl:variable>
                  <i class="fa fa-fw fa-phone"></i>
                  <a href="tel:{translate($phoneNumber,' ','')}">
                    <xsl:value-of select="$phoneNumber"/>
                  </a>
                  <br/>
              </xsl:for-each>
              <xsl:for-each select="gmd:phone/*/gmd:facsimile[normalize-space(.) != '']">
                <xsl:variable name="phoneNumber">
                  <xsl:apply-templates mode="render-value" select="."/>
                </xsl:variable>
                <i class="fa fa-fw fa-fax"></i>
                <a href="tel:{translate($phoneNumber,' ','')}">
                  <xsl:value-of select="normalize-space($phoneNumber)"/>
                </a>
                <br/>
              </xsl:for-each>
              <xsl:for-each select="gmd:onlineResource/*/gmd:linkage/gmd:URL[normalize-space(.) != '']">
                <xsl:variable name="web">
                  <xsl:apply-templates mode="render-value" select="."/></xsl:variable>
                <i class="fa fa-fw fa-link"></i>
                <a href="{normalize-space($web)}">
                  <xsl:value-of select="normalize-space($web)"/>
                </a>
              </xsl:for-each>
              <xsl:for-each select="gmd:hoursOfService[normalize-space(.) != '']">
                  <xsl:apply-templates mode="render-field"
                                        select="."/>
              </xsl:for-each>
            </xsl:for-each>
          </address>
        </div>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- Metadata linkage -->
  <xsl:template mode="render-field"
                match="gmd:fileIdentifier"
                priority="100">
    <dl>
      <dt>
        <xsl:call-template name="render-field-label">
          <xsl:with-param name="languages" select="$allLanguages"/>
        </xsl:call-template>
      </dt>
      <dd>
        <xsl:apply-templates mode="render-value" select="*"/>
        <xsl:apply-templates mode="render-value" select="@*"/>
        <a class="btn btn-default" href="{$nodeUrl}api/records/{$metadataUuid}/formatters/xml">
          <i class="fa fa-fw fa-file-code-o"></i>
          <span><xsl:value-of select="$schemaStrings/metadataInXML"/></span>
        </a>
      </dd>
    </dl>
  </xsl:template>

  <!-- Linkage -->
  <xsl:template mode="render-field"
                match="*[gmd:CI_OnlineResource]"
                priority="100">
    <dl class="gn-link">
      <dt>
        <xsl:call-template name="render-field-label">
          <xsl:with-param name="languages" select="$allLanguages"/>
        </xsl:call-template>
      </dt>
      <dd>
        <xsl:variable name="linkUrl"
                      select="*/gmd:linkage/gmd:URL"/>
        <xsl:variable name="linkName">
          <xsl:choose>
            <xsl:when test="*/gmd:name[* != '']">
              <xsl:apply-templates mode="render-value-no-breaklines"
                                   select="*/gmd:name"/>
            </xsl:when>
            <xsl:when test="*/gmd:description[* != '']">
              <xsl:apply-templates mode="render-value-no-breaklines"
                                   select="*/gmd:description"/>
            </xsl:when>
            <xsl:otherwise>
              <xsl:value-of select="$linkUrl"/>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:variable>

        <xsl:choose>
          <xsl:when test="string($linkUrl)">
        <a href="{$linkUrl}" title="{$linkName}">
          <span>
            <xsl:value-of select="$linkName"/>
          </span>
        </a>
          </xsl:when>
          <xsl:otherwise>
            <span>
              <xsl:value-of select="$linkName"/>
            </span>
          </xsl:otherwise>
        </xsl:choose>

        <xsl:if test="*/gmd:protocol[normalize-space(gco:CharacterString|gmx:Anchor) != '']">
          (<span>
          <xsl:apply-templates mode="render-value-no-breaklines"
                               select="*/gmd:protocol"/>
        </span>)</xsl:if>
        <xsl:if test="*/gmd:description[normalize-space(gco:CharacterString|gmx:Anchor) != '' and * != $linkName]">
          <p>
            <xsl:apply-templates mode="render-value"
                                 select="*/gmd:description"/>
          </p>
        </xsl:if>
      </dd>
    </dl>
  </xsl:template>

  <!-- Identifier -->
  <xsl:template mode="render-field"
                match="*[(gmd:RS_Identifier or gmd:MD_Identifier) and
                  */gmd:code/gco:CharacterString != '']"
                priority="100">
    <dl class="gn-code">
      <dt>
        <xsl:call-template name="render-field-label">
          <xsl:with-param name="languages" select="$allLanguages"/>
        </xsl:call-template>
      </dt>
      <dd>

        <xsl:if test="*/gmd:codeSpace">
          <xsl:apply-templates mode="render-value-no-breaklines"
                               select="*/gmd:codeSpace"/>
          /
        </xsl:if>
        <xsl:apply-templates mode="render-value-no-breaklines"
                             select="*/gmd:code"/>
        <xsl:if test="*/gmd:version">
          /
          <xsl:apply-templates mode="render-value-no-breaklines"
                               select="*/gmd:version"/>
        </xsl:if>
        <xsl:if test="*/gmd:authority">
          <p>
            <xsl:apply-templates mode="render-value-no-breaklines"
                                 select="*/gmd:authority"/>
          </p>
        </xsl:if>
      </dd>
    </dl>
  </xsl:template>

 <!-- Display thesaurus name and the list of keywords if at least one keyword is set -->
  <xsl:template mode="render-field"
                match="gmd:descriptiveKeywords[*/gmd:thesaurusName/gmd:CI_Citation/gmd:title and
                count(*/gmd:keyword/*[. != '']) = 0]"
                priority="100"/>
  <xsl:template mode="render-field"
                match="gmd:descriptiveKeywords[*/gmd:thesaurusName/gmd:CI_Citation/gmd:title and
                count(*/gmd:keyword/*[. != '']) > 0]"
                priority="100">
    <xsl:param name="fieldName" select="''" as="xs:string"/>

    <dl class="gn-keyword">
      <dt>
        <xsl:choose>
          <xsl:when test="$fieldName != ''">
            <xsl:value-of select="$fieldName"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:apply-templates mode="render-value"
                                 select="*/gmd:thesaurusName/gmd:CI_Citation/gmd:title"/>
          </xsl:otherwise>
        </xsl:choose>
      </dt>
      <dd>
        <div>
          <ul>
            <xsl:for-each select="*/gmd:keyword">
              <li>
                <xsl:apply-templates mode="render-value"
                                     select="."/>
              </li>
            </xsl:for-each>
          </ul>
        </div>
      </dd>
    </dl>
  </xsl:template>


  <xsl:template mode="render-field"
                match="gmd:descriptiveKeywords[not(*/gmd:thesaurusName/gmd:CI_Citation/gmd:title)]"
                priority="100">
    <xsl:param name="fieldName" select="''" as="xs:string"/>

    <dl class="gn-keyword">
      <dt>
        <xsl:variable name="thesaurusType">
          <xsl:apply-templates mode="render-value"
                               select="*/gmd:type/*/@codeListValue[. != '']"/>
        </xsl:variable>

        <xsl:choose>
          <xsl:when test="$fieldName != ''">
            <xsl:value-of select="$fieldName"/>
          </xsl:when>
          <xsl:when test="$thesaurusType != ''">
            <xsl:copy-of select="$thesaurusType"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="$schemaStrings/noThesaurusName"/>
          </xsl:otherwise>
        </xsl:choose>
      </dt>
      <dd>
        <div>
          <ul>
            <xsl:for-each select="*/gmd:keyword">
              <li>
                <xsl:apply-templates mode="render-value"
                                     select="."/>
              </li>
            </xsl:for-each>
          </ul>
        </div>
      </dd>
    </dl>
  </xsl:template>

  <xsl:template mode="render-field"
                match="gmd:distributionFormat[1]"
                priority="100">
    <dl class="gn-format">
      <dt>
        <xsl:call-template name="render-field-label">
          <xsl:with-param name="languages" select="$allLanguages"/>
        </xsl:call-template>
      </dt>
      <dd>
        <ul>
          <xsl:for-each select="parent::node()/gmd:distributionFormat">
            <xsl:if test="*/gmd:name[. != '']">
              <li>
                <xsl:apply-templates mode="render-value-no-breaklines"
                                    select="*/gmd:name"/>
                (<xsl:apply-templates mode="render-value-no-breaklines"
                                      select="*/gmd:version"/>)
                <p>
                  <xsl:apply-templates mode="render-field"
                                      select="*/(gmd:amendmentNumber|gmd:specification|
                                gmd:fileDecompressionTechnique|gmd:formatDistributor)"/>
                </p>
              </li>
            </xsl:if>
          </xsl:for-each>
        </ul>
      </dd>
    </dl>
  </xsl:template>


  <xsl:template mode="render-field"
                match="gmd:distributionFormat[position() > 1]"
                priority="100"/>

  <!-- Date -->
  <xsl:template mode="render-field"
                match="gmd:date"
                priority="100">
    <dl class="gn-date">
      <dt>
        <xsl:call-template name="render-field-label">
          <xsl:with-param name="languages" select="$allLanguages"/>
          <xsl:with-param name="contextLabel" select="*/gmd:dateType/*/@codeListValue[. != '']"/>
        </xsl:call-template>
      </dt>
      <dd>
        <xsl:apply-templates mode="render-value"
                             select="*/gmd:date/*"/>
      </dd>
    </dl>
  </xsl:template>


  <!-- Enumeration -->
  <xsl:template mode="render-field"
                match="gmd:topicCategory[1]|gmd:obligation[1]|gmd:pointInPixel[1]"
                priority="100">
    <dl class="gn-date">
      <dt>
        <xsl:call-template name="render-field-label">
          <xsl:with-param name="languages" select="$allLanguages"/>
        </xsl:call-template>
      </dt>
      <dd>
        <ul>
          <xsl:for-each select="parent::node()/(gmd:topicCategory|gmd:obligation|gmd:pointInPixel)">
            <li>
              <xsl:apply-templates mode="render-value"
                                   select="*"/>
            </li>
          </xsl:for-each>
        </ul>
      </dd>
    </dl>
  </xsl:template>
  <xsl:template mode="render-field"
                match="gmd:topicCategory[position() > 1]|
                        gmd:obligation[position() > 1]|
                        gmd:pointInPixel[position() > 1]"
                priority="100"/>


  <!-- Link to other metadata records -->
  <xsl:template mode="render-field"
                match="srv:operatesOn[@uuidref]|gmd:featureCatalogueCitation[@uuidref]|gmd:source[@uuidref]|gmd:aggregateDataSetIdentifier/*/gmd:code[@uuidref]"
                priority="100">
    <xsl:variable name="nodeName" select="name()"/>

    <!-- Only render the first element of this kind and render a list of
    following siblings.   -->
    <xsl:variable name="isFirstOfItsKind"
                  select="count(preceding-sibling::node()[name() = $nodeName]) = 0"/>
    <xsl:if test="$isFirstOfItsKind">
      <dl class="gn-md-associated-resources">
        <dt>
          <xsl:call-template name="render-field-label">
            <xsl:with-param name="languages" select="$allLanguages"/>
          </xsl:call-template>
        </dt>
        <dd>
          <ul>
            <xsl:for-each select="parent::node()/*[name() = $nodeName]">
              <li>
                <a data-gn-api-link=""
                   href="{$nodeUrl}api/records/{@uuidref}">
                  <i class="fa fa-fw fa-link"></i>
                  <span>
                    <xsl:value-of select="gn-fn-render:getMetadataTitle(@uuidref, $langId)"/>
                  </span>
                </a>
              </li>
            </xsl:for-each>
          </ul>
        </dd>
      </dl>
    </xsl:if>
  </xsl:template>

 <!-- Elements to avoid render -->
  <xsl:template mode="render-field" match="gmd:PT_Locale" priority="100"/>


  <!-- Traverse the tree -->
  <xsl:template mode="render-field"
                match="*">
    <xsl:apply-templates mode="render-field"/>
  </xsl:template>


  <!-- ########################## -->
  <!-- Render values for text ... -->
  <xsl:template mode="render-value"
                match="*[gco:CharacterString]">
     <xsl:variable name="txt">
       <xsl:apply-templates mode="localised" select=".">
         <xsl:with-param name="langId" select="$langId"/>
       </xsl:apply-templates>
     </xsl:variable>
     <span>
      <xsl:if test="name() = 'gmd:parentIdentifier'">
        <a href="{$nodeUrl}api/records/{./gco:CharacterString}">
          <i class="fa fa-fw fa-link"></i>
          <xsl:value-of select="gn-fn-render:getMetadataTitle(./gco:CharacterString, $langId)"/>
        </a>
      </xsl:if>

      <xsl:call-template name="addLineBreaksAndHyperlinks">
        <xsl:with-param name="txt" select="$txt"/>
      </xsl:call-template>
     </span>
  </xsl:template>

  <xsl:template mode="render-value-no-breaklines"
                match="*[gco:CharacterString]">
    <span>
      <xsl:if test="name() = 'gmd:parentIdentifier'">
        <a href="{$nodeUrl}api/records/{./gco:CharacterString}">
          <i class="fa fa-fw fa-link"></i>
          <xsl:value-of select="gn-fn-render:getMetadataTitle(./gco:CharacterString, $langId)"/>
        </a>
      </xsl:if>

      <xsl:apply-templates mode="localised" select=".">
        <xsl:with-param name="langId" select="$langId"/>
      </xsl:apply-templates>
    </span>
  </xsl:template>



  <xsl:template mode="render-value"
                match="*[gmx:Anchor|gmd:URL]">
    <xsl:apply-templates mode="render-value"
                         select="gmx:Anchor|gmd:URL"/>
  </xsl:template>

  <xsl:template mode="render-value"
                match="gmx:Anchor">
    <xsl:variable name="link"
                  select="@xlink:href"/>
    <xsl:variable name="txt">
      <xsl:apply-templates mode="localised" select="..">
        <xsl:with-param name="langId" select="$langId"/>
      </xsl:apply-templates>
    </xsl:variable>

    <xsl:choose>
      <xsl:when test="$link != ''">
        <a href="{$link}">
          <xsl:copy-of select="$txt"/>
        </a>
      </xsl:when>
      <xsl:otherwise>
        <xsl:copy-of select="$txt"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>



  <xsl:template mode="render-value"
                match="gco:Integer|gco:Decimal|
                       gco:Real|gco:Measure|gco:Length|gco:Distance|gco:Angle|gmx:FileName|
                       gco:Scale|gco:Record|gco:RecordType|gmx:MimeFileType|gmd:URL|
                       gco:LocalName|
                       gml:beginPosition|gml:endPosition|gml:being|gml:end|
                       gml320:beginPosition|gml320:endPosition|gml320:being|gml320:end">

    <xsl:choose>
      <xsl:when test="contains(., 'http')">
        <!-- Replace hyperlink in text by an hyperlink -->
        <xsl:variable name="textWithLinks"
                      select="replace(., '([a-z][\w-]+:/{1,3}[^\s()&gt;&lt;]+[^\s`!()\[\]{};:'&apos;&quot;.,&gt;&lt;?«»“”‘’])',
                                    '&lt;a href=''$1''&gt;$1&lt;/a&gt;')"/>

        <xsl:if test="$textWithLinks != ''">
          <xsl:copy-of select="saxon:parse(
                          concat('&lt;p&gt;',
                          replace($textWithLinks, '&amp;', '&amp;amp;'),
                          '&lt;/p&gt;'))"/>
        </xsl:if>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="normalize-space(.)"/>

        <xsl:if test="@uom">
          <!-- Display the unit value only -->
          &#160;<xsl:value-of select="if (contains(@uom, '#'))
                                    then tokenize(@uom, '#')[2]
                                    else @uom"/>
        </xsl:if>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template mode="render-value"
                match="gts:TM_PeriodDuration">
    <div data-gn-field-duration-div="{.}"><xsl:value-of select="."/></div>
  </xsl:template>

  <!-- Translate boolean values -->
  <xsl:template mode="render-value"
                match="gco:Boolean">

    <xsl:choose>
      <xsl:when test=". = 'true'">
        <xsl:value-of select="$schemaStrings/trueValue"/>
      </xsl:when>
      <xsl:when test=". = 'false'">
        <xsl:value-of select="$schemaStrings/falseValue"/>
      </xsl:when>

    </xsl:choose>
  </xsl:template>

  <!-- filename -->
  <xsl:template mode="render-value"
                match="gmx:FileName[@src != '']">
    <xsl:variable name="href" select="@src"/>
    <xsl:variable name="label" select="."/>

    <xsl:choose>
      <xsl:when test="matches($href, $imageExtensionsRegex, 'i')">
        <img src="{$href}" title="{$label}" alt="{$label}"/>
      </xsl:when>
      <xsl:otherwise>
        <a href="{$href}">
          <xsl:value-of select="$label"/>
        </a>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- ... URL -->
  <xsl:template mode="render-value"
                match="gmd:URL">
    <a href="{.}">
      <xsl:value-of select="."/>
    </a>
  </xsl:template>

  <!-- ... Dates - formatting is made on the client side by the directive  -->
  <xsl:template mode="render-value"
                match="gco:Date[matches(., '[0-9]{4}')]">
    <span data-gn-humanize-time="{.}" data-format="YYYY">
      <xsl:value-of select="."/>
    </span>
  </xsl:template>

  <xsl:template mode="render-value"
                match="gco:Date[matches(., '[0-9]{4}-[0-9]{2}')]">
    <span data-gn-humanize-time="{.}" data-format="MMM YYYY">
      <xsl:value-of select="."/>
    </span>
  </xsl:template>

  <xsl:template mode="render-value"
                match="gco:Date[matches(., '[0-9]{4}-[0-9]{2}-[0-9]{2}')]
                      |gml:beginPosition[matches(., '[0-9]{4}-[0-9]{2}-[0-9]{2}')]
                      |gml:endPosition[matches(., '[0-9]{4}-[0-9]{2}-[0-9]{2}')]
                      |gml:begin[matches(., '[0-9]{4}-[0-9]{2}-[0-9]{2}')]
                      |gml:end[matches(., '[0-9]{4}-[0-9]{2}-[0-9]{2}')]">
    <span data-gn-humanize-time="{.}">
      <xsl:value-of select="."/>
    </span>
  </xsl:template>

  <xsl:template mode="render-value"
                match="gco:DateTime[matches(., '[0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9]{2}:[0-9]{2}:[0-9]{2}')]">
    <span data-gn-humanize-time="{.}">
      <xsl:value-of select="."/>
    </span>
  </xsl:template>

  <xsl:template mode="render-value"
                match="gco:Date|gco:DateTime">
    <span data-gn-humanize-time="{.}">
      <xsl:value-of select="."/>
    </span>
  </xsl:template>

  <xsl:template mode="render-value"
                match="gmd:language/gmd:LanguageCode/@codeListValue
                       |gmd:language/gco:CharacterString">
      <xsl:variable name="label"
                    select="xslUtils:getIsoLanguageLabel(., .)"/>
      <xsl:value-of select="if ($label != '') then $label else ."/>
  </xsl:template>

  <!-- ... Codelists and enumeration -->
  <xsl:template mode="render-value"
                match="@codeListValue|
                       @indeterminatePosition|
                       gmd:MD_TopicCategoryCode|
                       gmd:MD_ObligationCode|
                       gmd:MD_PixelOrientationCode">
    <xsl:param name="forcedLanguage" select="''" required="no"/>

    <xsl:variable name="id" select="."/>
    <xsl:variable name="name" select="name()"/>
    <xsl:variable name="parentName"
                  select="if (. instance of attribute())
                          then parent::node()/local-name()
                          else local-name()"/>

    <xsl:variable name="codelistTranslation"
                  select="tr:codelist-value-label(
                            if ($forcedLanguage = '') then tr:create($schema) else tr:create($schema, $forcedLanguage),
                            if ($name = 'indeterminatePosition') then 'indeterminatePosition' else $parentName,
                            $id)"/>
    <xsl:choose>
      <!-- eg. for codelist, display label in all record languages -->
      <xsl:when test="$forcedLanguage = '' and $language = 'all' and count($allLanguages/lang) > 0">
        <xsl:for-each select="$allLanguages/lang">
          <xsl:variable name="codelistTranslation"
                        select="tr:codelist-value-label(
                            tr:create($schema, @code),
                            if ($name = 'indeterminatePosition') then 'indeterminatePosition' else $parentName,
                            $id)"/>

          <div xml:lang="{@code}"
               title="{if ($codelistTranslation != '') then tr:codelist-value-desc(
                            tr:create($schema),
                            $parentName, $id) else ''}">
            <xsl:value-of select="if ($codelistTranslation != '') then $codelistTranslation else $id"/>
          </div>
        </xsl:for-each>
      </xsl:when>
      <xsl:when test="$codelistTranslation != ''">
        <xsl:variable name="codelistDesc"
                      select="tr:codelist-value-desc(
                            if ($forcedLanguage = '') then tr:create($schema) else tr:create($schema, $forcedLanguage),
                            $parentName, $id)"/>
        <span title="{$codelistDesc}">
          <xsl:value-of select="$codelistTranslation"/>
        </span>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$id"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template mode="render-value"
                match="@gco:nilReason[. = 'withheld']"
                priority="100">
    <i class="fa fa-fw fa-lock text-warning" title="{{{{'withheld' | translate}}}}"></i>
  </xsl:template>

  <xsl:template mode="render-value"
                match="@*"/>

</xsl:stylesheet>
