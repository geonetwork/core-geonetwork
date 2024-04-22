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
                xmlns:skos="http://www.w3.org/2004/02/skos/core#"
                xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
                xmlns:dct="http://purl.org/dc/terms/"
                xmlns:dcat="http://www.w3.org/ns/dcat#"
                xmlns:locn="http://www.w3.org/ns/locn#"
                xmlns:gn="http://www.fao.org/geonetwork"
                xmlns:gn-fn-metadata="http://geonetwork-opensource.org/xsl/functions/metadata"
                xmlns:gn-fn-dcat2="http://geonetwork-opensource.org/xsl/functions/profiles/dcat2"
                xmlns:saxon="http://saxon.sf.net/"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                extension-element-prefixes="saxon"
                version="2.0"
                exclude-result-prefixes="#all">

  <xsl:include href="layout-custom-fields-keywords.xsl"/>

  <!-- Experiment using gn-bounding-polygon
  Issues:
  * Lack of spatial thesaurus helper
  * Editor error on save
  {"message":"IllegalAddException",
   "code":"unsatisfied_request_parameter",
    "description":"The namespace xmlns=\"\" could not be added as a namespace
    to \"Polygon\": The namespace prefix \"\" collides with the element namespace prefix"}

  -->
  <!--&lt;!&ndash; WKT geom is ignored &ndash;&gt;
  <xsl:template mode="mode-dcat2"
                match="dct:spatial/dct:Location/locn:geometry[ends-with(./@rdf:datatype,'#wktLiteral')]"
                priority="2000"/>

  &lt;!&ndash; GML geom is used &ndash;&gt;
  <xsl:template mode="mode-dcat2"
                match="dct:spatial/dct:Location/locn:geometry[ends-with(./@rdf:datatype,'#gmlLiteral')]"
                priority="2000">

    <xsl:param name="schema" select="$schema" required="no"/>
    <xsl:param name="labels" select="$labels" required="no"/>
    <xsl:param name="refToDelete" required="no"/>

    <xsl:variable name="xpath"
                  select="gn-fn-metadata:getXPath(.)"/>
    <xsl:variable name="labelConfig"
                  select="gn-fn-metadata:getLabel($schema, name(), $labels, name(..), '', $xpath)"/>

    <xsl:call-template name="render-boxed-element">
      <xsl:with-param name="label" select="$labelConfig/label"/>
      <xsl:with-param name="editInfo" select="$refToDelete"/>
      <xsl:with-param name="cls" select="local-name()"/>
      <xsl:with-param name="subTreeSnippet">
        <xsl:variable name="geometry">
          <xsl:value-of select="."/>
        </xsl:variable>

        <xsl:variable name="identifier"
                      select="concat('_X', ./gn:element/@ref, '_replace')"/>
        <xsl:variable name="readonly" select="false()"/>

        <br />
        <gn-bounding-polygon polygon-xml="{$geometry}"
                             identifier="{$identifier}"
                             geomwrapper="${{geom}}"
                             read-only="{$readonly}">
        </gn-bounding-polygon>
      </xsl:with-param>
    </xsl:call-template>
  </xsl:template>-->



  <xsl:template mode="mode-dcat2"
                match="dct:spatial"
                priority="2000">
    <xsl:param name="schema" select="$schema" required="no"/>
    <xsl:param name="labels" select="$labels" required="no"/>
    <xsl:param name="refToDelete" required="no"/>

    <xsl:variable name="xpath"
                  select="gn-fn-metadata:getXPath(.)"/>
    <xsl:variable name="labelConfig"
                  select="gn-fn-metadata:getLabel($schema, name(), $labels, name(..), '', $xpath)"/>

    <xsl:call-template name="render-boxed-element">
      <xsl:with-param name="label" select="$labelConfig/label"/>
      <xsl:with-param name="editInfo" select="$refToDelete"/>
      <xsl:with-param name="cls" select="local-name()"/>
      <xsl:with-param name="subTreeSnippet">

        <!-- Update fixed info always create a locn:geometry. -->
        <xsl:variable name="geometry" as="node()?">
          <xsl:variable name="gmlGeom"
                        select="dct:Location/locn:geometry[
                                ends-with(@rdf:datatype, '#gmlLiteral')]"/>
          <xsl:variable name="wktGeom"
                        select="dct:Location/locn:geometry[
                                ends-with(@rdf:datatype, '#wktLiteral')]"/>
          <xsl:choose>
            <xsl:when test="count($gmlGeom) > 0">
              <xsl:copy-of select="$gmlGeom[1]"/>
            </xsl:when>
            <xsl:when test="count($wktGeom) > 0">
              <xsl:copy-of select="$wktGeom[1]"/>
            </xsl:when>
            <xsl:when test="dct:Location and dct:Location/locn:geometry[not(@rdf:datatype)]">
              <xsl:copy-of select="./dct:Location/locn:geometry[not(@rdf:datatype)]"/>
            </xsl:when>
          </xsl:choose>
        </xsl:variable>

        <xsl:variable name="cleanedGeometry" as="node()?">
          <xsl:apply-templates select="$geometry" mode="gn-element-cleaner"/>
        </xsl:variable>

        <xsl:variable name="bbox"
                      select="gn-fn-dcat2:getBboxCoordinates($cleanedGeometry)"/>

        <xsl:variable name="bboxCoordinates"
                      select="tokenize(replace($bbox, ',', '.'), '\|')"/>

        <!--<xsl:if test="count($bboxCoordinates) > 4">
          <div class="alert alert-danger">
            <p data-translate="invalidGeometryValue"/>
          </div>
        </xsl:if>-->

        <xsl:variable name="identifier">
          <xsl:choose>
            <xsl:when test="count($bboxCoordinates) > 5">
              <xsl:value-of select="$bboxCoordinates[6]"/>
            </xsl:when>
            <xsl:otherwise>
              <xsl:value-of select="./dct:Location/@rdf:about"/>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:variable>

        <xsl:variable name="description">
          <xsl:choose>
            <xsl:when test="count($bboxCoordinates) > 4">
              <xsl:value-of select="$bboxCoordinates[5]"/>
            </xsl:when>
            <xsl:otherwise>
              <xsl:value-of select="./dct:Location/skos:prefLabel[1]"/>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:variable>


        <div gn-draw-bbox=""
             data-dc-ref="{concat('_', $geometry/gn:element/@ref)}"
             data-lang="lang"
             data-read-only="false">
          <xsl:attribute name="data-hleft"
                         select="$bboxCoordinates[1]"/>
          <xsl:attribute name="data-hright"
                         select="$bboxCoordinates[3]"/>
          <xsl:attribute name="data-hbottom"
                         select="$bboxCoordinates[2]"/>
          <xsl:attribute name="data-htop"
                         select="$bboxCoordinates[4]"/>
          <!--TODO: KISS or not?
          <xsl:attribute name="data-identifier" select="$identifier"/>

          <xsl:attribute name="data-identifier-ref"
                         select="concat('_', ./dct:Location/gn:element/@ref, '_rdfCOLONabout')"/>
          <xsl:attribute name="data-identifier-tooltip"
                         select="concat($schema, '|rdf:about|dct:Location|', $xpath, '/dct:Location/@rdf:about')"/>
          <xsl:attribute name="data-description" select="$description"/>
          <xsl:attribute name="data-description-ref"
                         select="concat('_', ./dct:Location/skos:prefLabel[1]/gn:element/@ref)"/>
          <xsl:attribute name="data-description-tooltip"
                         select="concat($schema, '|', 'skos:prefLabel|dcat:Dataset|', $xpath, '/dct:Location/skos:prefLabel')"/>-->
        </div>
      </xsl:with-param>
    </xsl:call-template>
  </xsl:template>
</xsl:stylesheet>
