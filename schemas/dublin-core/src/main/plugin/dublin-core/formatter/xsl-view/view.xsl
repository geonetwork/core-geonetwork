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
                xmlns:dc="http://purl.org/dc/elements/1.1/"
                xmlns:dct="http://purl.org/dc/terms/"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:tr="java:org.fao.geonet.api.records.formatters.SchemaLocalizations"
                xmlns:gn-fn-render="http://geonetwork-opensource.org/xsl/functions/render"
                version="2.0"
                exclude-result-prefixes="#all">
 <!-- tr is defined at  core-geonetwork/services/src/main/java/org/fao/geonet/api/records/formatters/SchemaLocalizations.java -->
  <!-- Load the editor configuration to be able
  to render the different views -->
  <xsl:variable name="configuration"
                select="document('../../layout/config-editor.xml')"/>

  <!-- Some utility -->
  <xsl:include href="../../layout/evaluate.xsl"/>
  <xsl:include href="../../layout/utility-tpl.xsl"/>

  <!-- The core formatter XSL layout based on the editor configuration -->
  <xsl:include href="sharedFormatterDir/xslt/render-layout.xsl"/>
  <!--<xsl:include href="../../../../../data/formatter/xslt/render-layout.xsl"/>-->

  <!-- Define the metadata to be loaded for this schema plugin-->
  <xsl:variable name="metadata"
                select="/root/simpledc"/>


  <!-- Specific schema rendering -->
  <xsl:template mode="getMetadataTitle" match="simpledc">
    <xsl:value-of select="dc:title"/>
  </xsl:template>

  <xsl:template mode="getMetadataHierarchyLevel" match="simpledc">
    <xsl:value-of select="'dataset'"/>
  </xsl:template>

  <xsl:template mode="getMetadataAbstract" match="simpledc">
    <xsl:call-template name="addLineBreaksAndHyperlinks">
      <xsl:with-param name="txt" select="dc:description"/>
    </xsl:call-template>
  </xsl:template>

  <xsl:template mode="getMetadataHeader" match="simpledc">
    <xsl:if test="normalize-space(dc:description) != ''">
      <div class="gn-abstract">
        <xsl:call-template name="addLineBreaksAndHyperlinks">
          <xsl:with-param name="txt" select="dc:description"/>
        </xsl:call-template>
      </div>
    </xsl:if>
  </xsl:template>

  <xsl:template mode="getOverviews" match="simpledc">
    <section class="gn-md-side-overview">
      <h2>
        <i class="fa fa-fw fa-image"><xsl:comment select="'image'"/></i>
        <span>
          <xsl:value-of select="$schemaStrings/overviews"/>
        </span>
      </h2>

      <!-- In Sextant, only the first one is displayed-->
      <xsl:variable name="overviews"
                    select="(dct:references|dc:relation)[
                              normalize-space(.) != ''
                              and matches(., '.*(.gif|.png|.jpeg|.jpg)$', 'i')]"/>

      <xsl:variable name="imgOnError" as="xs:string?"
                    select="if (count($overviews) > 1)
                            then 'this.onerror=null; this.style.display=''none'';'
                            else 'this.onerror=null; $(''.gn-md-side-overview'').hide();'"/>

      <xsl:for-each select="$overviews">
        <img data-gn-img-modal="md"
             class="gn-img-thumbnail"
             alt="{$schemaStrings/overview}"
             src="{.}"
             onerror="{$imgOnError}"/>
      </xsl:for-each>
    </section>
  </xsl:template>


  <xsl:template mode="getExtent" match="simpledc">
    <section class="gn-md-side-extent">
      <h2>
        <i class="fa fa-fw fa-map-marker"><xsl:comment select="'image'"/></i>
        <span><xsl:comment select="name()"/>
          <xsl:value-of select="$schemaStrings/spatialExtent"/>
        </span>
      </h2>

      <xsl:apply-templates mode="render-field"
                           select=".//dc:coverage"/>
    </section>
  </xsl:template>


  <xsl:template mode="getTags" match="simpledc">
    <xsl:param name="byThesaurus" select="false()"/>

    <section class="gn-md-side-social">
      <h2>
        <i class="fa fa-fw fa-tag"><xsl:comment select="'image'"/></i>
        <span>
          <xsl:value-of select="$schemaStrings/noThesaurusName"/>
        </span>
      </h2>
      <xsl:for-each select="$metadata/dc:subject[. != '']">
        <tag thesaurus="">
          <a href='#/search?query_string=%7B"tag.\\*":%7B"{.}":true%7D%7D'>
            <span class="badge"><xsl:copy-of select="."/></span>
          </a>
        </tag>
      </xsl:for-each>
    </section>
  </xsl:template>


  <!-- Most of the elements are ... -->
  <xsl:template mode="render-field" match="*">
    <xsl:param name="fieldName" select="''" as="xs:string"/>

    <dl>
      <dt>
        <xsl:value-of select="if ($fieldName)
                                then $fieldName
                                else tr:nodeLabel(tr:create($schema), name(), null)"/>
      </dt>
      <dd>
        <xsl:apply-templates mode="render-value" select="."/>
      </dd>
    </dl>
  </xsl:template>


  <!-- Bbox is displayed with an overview and the geom displayed on it
  and the coordinates displayed around -->
  <xsl:template mode="render-field"
                match="dc:coverage">

    <xsl:variable name="coverage" select="."/>

    <xsl:variable name="n" select="substring-after($coverage,'North ')"/>
    <xsl:variable name="north" select="substring-before($n,',')"/>
    <xsl:variable name="s" select="substring-after($coverage,'South ')"/>
    <xsl:variable name="south" select="substring-before($s,',')"/>
    <xsl:variable name="e" select="substring-after($coverage,'East ')"/>
    <xsl:variable name="east" select="substring-before($e,',')"/>
    <xsl:variable name="w" select="substring-after($coverage,'West ')"/>
    <xsl:variable name="west" select="if (contains($w, '. '))
                                      then substring-before($w,'. ') else $w"/>
    <xsl:variable name="place" select="substring-after($coverage,'. ')"/>

    <xsl:copy-of select="gn-fn-render:bbox(
                            xs:double($west),
                            xs:double($south),
                            xs:double($east),
                            xs:double($north))"/>
  </xsl:template>

  <!-- Traverse the tree -->
  <xsl:template mode="render-field" match="simpledc">
    <xsl:apply-templates mode="render-field"/>
  </xsl:template>


  <!-- ########################## -->
  <!-- Render values for text ... -->
  <xsl:template mode="render-value" match="*">
    <xsl:value-of select="."/>
  </xsl:template>

  <!-- ... URL -->
  <xsl:template mode="render-value" match="*[starts-with(., 'http')]">
    <a href="{.}">
      <xsl:value-of select="."/>
    </a>
  </xsl:template>

  <!-- ... Dates  -->
  <xsl:template mode="render-value" match="*[matches(., '^[0-9]{4}-[0-9]{2}-[0-9]{2}$')]">
    <span data-gn-humanize-time="{.}">
      <xsl:value-of select="."/>
    </span>
  </xsl:template>

  <xsl:template mode="render-value"
                match="*[matches(., '[0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9]{2}:[0-9]{2}:[0-9]{2}$')]">
    <span data-gn-humanize-time="{.}">
      <xsl:value-of select="."/>
    </span>
  </xsl:template>

</xsl:stylesheet>
