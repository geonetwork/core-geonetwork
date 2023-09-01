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


<!-- WARNING Do not remove those namespaces as
     saxon:evaluate needs them for matching -->
<xsl:stylesheet version="3.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:gmd="http://www.isotc211.org/2005/gmd"
                xmlns:gts="http://www.isotc211.org/2005/gts"
                xmlns:gco="http://www.isotc211.org/2005/gco"
                xmlns:gmx="http://www.isotc211.org/2005/gmx"
                xmlns:gfc="http://www.isotc211.org/2005/gfc"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                xmlns:err="http://www.w3.org/2005/xqt-errors"
                xmlns:gn="http://www.fao.org/geonetwork"
                xmlns:gn-fn-metadata="http://geonetwork-opensource.org/xsl/functions/metadata"
                exclude-result-prefixes="#all">

  <!-- Evaluate an expression. This is schema dependant in order to properly
        set namespaces required for evaluate.

       A node returned by evaluate will lost its context (ancestors).
    -->
  <xsl:function name="gn-fn-metadata:evaluate-iso19110">
    <xsl:param name="base" as="node()"/>
    <xsl:param name="in"/>
    <!--
     <xsl:message>in xml <xsl:copy-of select="$base"/></xsl:message>
     <xsl:message>search for <xsl:copy-of select="$in"/></xsl:message>-->

    <!-- saxon:evaluate and xsl:evaluate does not have the same context mechanism.
    TODO-SAXON: Check how to better handle XPath expression
    in edit and view mode. -->
    <xsl:variable name="context" as="node()">
      <root>
        <xsl:copy-of select="$base"/>
      </root>
    </xsl:variable>

    <xsl:try>
      <xsl:evaluate xpath="if (starts-with($in, '/../')) then substring($in, 5)
                           else if (starts-with($in, '..//')) then substring($in, 5)
                           else $in" context-item="$context"/>
      <xsl:catch>
        <xsl:message>Error evaluating <xsl:value-of select="$in"/> in context item <xsl:value-of select="name($base)"/>.
          <xsl:value-of select="$err:description"/></xsl:message>
      </xsl:catch>
    </xsl:try>
  </xsl:function>

  <!-- Evaluate XPath returning a boolean value. -->
  <xsl:function name="gn-fn-metadata:evaluate-iso19110-boolean"
                as="xs:boolean">
    <xsl:param name="base" as="node()"/>
    <xsl:param name="in"/>

    <xsl:evaluate xpath="$in" context-item="$base"/>
  </xsl:function>
</xsl:stylesheet>
