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
                xmlns:saxon="http://saxon.sf.net/"
                version="2.0" extension-element-prefixes="saxon"
                exclude-result-prefixes="#all">

  <!--
    Render an element as XML tree in view or edit mode
  -->
  <xsl:template mode="render-xml" match="*">
    <xsl:choose>
      <xsl:when test="$isEditing">
        <!-- Remove gn:* element -->
        <xsl:variable name="strippedXml">
          <xsl:apply-templates mode="gn-element-cleaner" select="."/>
        </xsl:variable>

        <!-- Render XML in textarea -->
        <div id="xmleditor"
             ui-ace="{{useWrapMode:true,showGutter:true,mode:'xml',onLoad:xmlEditorLoaded,onChange:xmlEditorChange}}">
          <xsl:value-of
            select="saxon:serialize($strippedXml, 'default-indent-mode')"/>
        </div>
        <textarea name="data" class="hidden">
        </textarea>
      </xsl:when>
      <xsl:otherwise>
        <pre>
          <xsl:copy-of select="."/>
        </pre>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
</xsl:stylesheet>
