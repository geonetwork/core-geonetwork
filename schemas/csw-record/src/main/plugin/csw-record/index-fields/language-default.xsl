<?xml version="1.0" encoding="UTF-8" ?>

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
                version="1.0">

  <!-- This file defines what parts of the metadata are indexed by Lucene
         Searches can be conducted on indexes defined here.
         The Field@name attribute defines the name of the search variable.
         If a variable has to be maintained in the user session, it needs to be
         added to the GeoNetwork constants in the Java source code.
         Please keep indexes consistent among metadata standards if they should
         work accross different metadata resources -->
  <!-- ========================================================================================= -->

  <xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes"/>

  <!-- ========================================================================================= -->

  <xsl:template match="/">
    <Documents>
      <!-- TODO dc:Records should use xml:lang attributes -->
    </Documents>
  </xsl:template>

</xsl:stylesheet>
