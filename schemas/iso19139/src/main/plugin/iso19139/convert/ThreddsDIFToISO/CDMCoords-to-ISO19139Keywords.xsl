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

<!--  Mapping between netcdfDatasetInfo and ISO19139 keywords -->
<xsl:stylesheet xmlns:gmd="http://www.isotc211.org/2005/gmd"
                xmlns:gco="http://www.isotc211.org/2005/gco"
                xmlns:util="java:java.util.UUID"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                version="2.0"
                exclude-result-prefixes="util">

  <!-- ==================================================================== -->

  <xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes"/>

  <!-- ==================================================================== -->

  <xsl:template match="*">

    <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

    <gmd:descriptiveKeywords>
      <gmd:MD_Keywords>

        <xsl:for-each select="variable">
          <gmd:keyword>
            <gco:CharacterString>
              <xsl:value-of select="concat(@name,' ',@long_name,' ',@decl)"/>
            </gco:CharacterString>
          </gmd:keyword>
        </xsl:for-each>

        <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

        <gmd:type>
          <gmd:MD_KeywordTypeCode
            codeList="http://www.isotc211.org/2005/resources/Codelist/gmxCodelists.xml#MD_KeywordTypeCode"
            codeListValue="theme"/>
        </gmd:type>

        <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

        <gmd:thesaurusName>
          <gmd:CI_Citation>
            <gmd:title>
              <gco:CharacterString>
                <xsl:value-of
                  select="concat(convention/@name,' (see http://www.unidata.ucar.edu/software/netcdf/conventions.html for more info on some conventions and adding conventions to the Unidata netcdf-4.0 Java library)')"/>
              </gco:CharacterString>
            </gmd:title>
            <gmd:alternateTitle>
              <gco:CharacterString>Data Parameters/Variables following the
                <xsl:value-of select="convention/@name"/> conventions
              </gco:CharacterString>
            </gmd:alternateTitle>
          </gmd:CI_Citation>
        </gmd:thesaurusName>

      </gmd:MD_Keywords>
    </gmd:descriptiveKeywords>

  </xsl:template>

  <!-- ============================================================================= -->

</xsl:stylesheet>
