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

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:gn="http://www.fao.org/geonetwork"
                xmlns:saxon="http://saxon.sf.net/"
                version="2.0"
                extension-element-prefixes="saxon">
  <!--
    Load the brief template loader if the metadata has to be
    converted to its brief format. Use the mode="brief" on each
    records to be converted.


    The profile loader using the oasis-catalog to load
  <schema>/layout/tpl-brief.xsl in each activated schema plugins.
  -->
  <xsl:include href="base-variables.xsl"/>
  <xsl:include href="functions-core.xsl"/>


  <!-- Initialized variables used in <schema>/utility-tpl.xsl
  which are not used in that mode. To be improved. FIXME -->
  <xsl:variable name="editorConfig">
    <null/>
  </xsl:variable>
  <xsl:variable name="metadata">
    <null/>
  </xsl:variable>

  <xsl:include href="blanks/metadata-schema01/layout/tpl-brief.xsl"/>
  <xsl:include href="blanks/metadata-schema02/layout/tpl-brief.xsl"/>
  <xsl:include href="blanks/metadata-schema03/layout/tpl-brief.xsl"/>
  <xsl:include href="blanks/metadata-schema04/layout/tpl-brief.xsl"/>
  <xsl:include href="blanks/metadata-schema05/layout/tpl-brief.xsl"/>
  <xsl:include href="blanks/metadata-schema06/layout/tpl-brief.xsl"/>
  <xsl:include href="blanks/metadata-schema07/layout/tpl-brief.xsl"/>
  <xsl:include href="blanks/metadata-schema08/layout/tpl-brief.xsl"/>
  <xsl:include href="blanks/metadata-schema09/layout/tpl-brief.xsl"/>
  <xsl:include href="blanks/metadata-schema10/layout/tpl-brief.xsl"/>
  <xsl:include href="blanks/metadata-schema11/layout/tpl-brief.xsl"/>
  <xsl:include href="blanks/metadata-schema12/layout/tpl-brief.xsl"/>
  <xsl:include href="blanks/metadata-schema13/layout/tpl-brief.xsl"/>
  <xsl:include href="blanks/metadata-schema14/layout/tpl-brief.xsl"/>
  <xsl:include href="blanks/metadata-schema15/layout/tpl-brief.xsl"/>
  <xsl:include href="blanks/metadata-schema16/layout/tpl-brief.xsl"/>
  <xsl:include href="blanks/metadata-schema17/layout/tpl-brief.xsl"/>
  <xsl:include href="blanks/metadata-schema18/layout/tpl-brief.xsl"/>
  <xsl:include href="blanks/metadata-schema19/layout/tpl-brief.xsl"/>
  <xsl:include href="blanks/metadata-schema20/layout/tpl-brief.xsl"/>


  <xsl:template match="*[name() = 'summary']" mode="brief"/>

  <xsl:template match="*" mode="brief">
    <xsl:variable name="schema" select="gn:info/schema"/>
    <xsl:variable name="briefSchemaCallBack" select="concat($schema,'Brief')"/>
    <saxon:call-template name="{$briefSchemaCallBack}"/>
  </xsl:template>

</xsl:stylesheet>
