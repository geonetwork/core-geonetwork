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

<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:gn="http://www.fao.org/geonetwork" xmlns:saxon="http://saxon.sf.net/"
  extension-element-prefixes="saxon">
  <!-- 
    Load the thesaurus transformation for each schemas
  -->

  <!-- Load schema plugin conversion -->
  <xsl:include href="blanks/metadata-schema01/convert/thesaurus-transformation.xsl"/>
  <xsl:include href="blanks/metadata-schema02/convert/thesaurus-transformation.xsl"/>
  <xsl:include href="blanks/metadata-schema03/convert/thesaurus-transformation.xsl"/>
  <xsl:include href="blanks/metadata-schema04/convert/thesaurus-transformation.xsl"/>
  <xsl:include href="blanks/metadata-schema05/convert/thesaurus-transformation.xsl"/>
  <xsl:include href="blanks/metadata-schema06/convert/thesaurus-transformation.xsl"/>
  <xsl:include href="blanks/metadata-schema07/convert/thesaurus-transformation.xsl"/>
  <xsl:include href="blanks/metadata-schema08/convert/thesaurus-transformation.xsl"/>
  <xsl:include href="blanks/metadata-schema09/convert/thesaurus-transformation.xsl"/>
  <xsl:include href="blanks/metadata-schema10/convert/thesaurus-transformation.xsl"/>
  <xsl:include href="blanks/metadata-schema11/convert/thesaurus-transformation.xsl"/>
  <xsl:include href="blanks/metadata-schema12/convert/thesaurus-transformation.xsl"/>
  <xsl:include href="blanks/metadata-schema13/convert/thesaurus-transformation.xsl"/>
  <xsl:include href="blanks/metadata-schema14/convert/thesaurus-transformation.xsl"/>
  <xsl:include href="blanks/metadata-schema15/convert/thesaurus-transformation.xsl"/>
  <xsl:include href="blanks/metadata-schema16/convert/thesaurus-transformation.xsl"/>
  <xsl:include href="blanks/metadata-schema17/convert/thesaurus-transformation.xsl"/>
  <xsl:include href="blanks/metadata-schema18/convert/thesaurus-transformation.xsl"/>
  <xsl:include href="blanks/metadata-schema19/convert/thesaurus-transformation.xsl"/>
  <xsl:include href="blanks/metadata-schema20/convert/thesaurus-transformation.xsl"/>

</xsl:stylesheet>
