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
  exclude-result-prefixes="xs"
  version="2.0">
  
  <xsl:output method="text"/>
  
  <xsl:param name="lang" select="'fre'"/>
  <xsl:param name="schema" select="'iso19139'"/>
  
  <xsl:variable name="i18n">
    <title>
      <eng>List of element for schema:</eng>
      <fre>Liste des éléments du standard :</fre>
    </title>
    <code>
      <eng>Tag name</eng>
      <fre>Nom de la balise</fre>
    </code>
    <desc>
      <eng>Description</eng>
      <fre>Description</fre>
    </desc>
    <moreInfo>
      <eng>More details</eng>
      <fre>Informations complémentaires</fre>
    </moreInfo>
    <helper>
      <eng>Recommended values</eng>
      <fre>Valeurs recommandées</fre>
    </helper>
  </xsl:variable>
  
  <xsl:variable name="labels" select="document(concat('../web/src/main/webapp/WEB-INF/data/config/schema_plugins/', $schema, '/loc/eng/labels.xml'))"/>
  
  <xsl:template match="/">

# <xsl:value-of select="concat($i18n/title/*[name() = $lang], ' ', $schema)"/>

<xsl:text>


</xsl:text>

    <xsl:for-each select="$labels//element">
      <xsl:sort select="name"/>
      
<xsl:variable name="rstRef" 
  select="concat('.. _', $schema, '-', 
  replace(@name, ':', '-'), ':')"/>

<xsl:variable name="rstContext" select="if (@context) 
  then concat(' (cf. `', 
    @context, ' &lt;#', 
    $schema, '-', replace(@context, ':', '-'), '&gt;', 
    @context, '`_)') 
  else ''"/>


<xsl:value-of select="$rstRef"/>

<xsl:text>

</xsl:text>

      <xsl:value-of select="concat(label, $rstContext)"/>



- *<xsl:value-of select="$i18n/desc/*[name() = $lang]"/> :* <xsl:value-of select="label"/>
      
      
- *<xsl:value-of select="$i18n/code/*[name() = $lang]"/> :* <xsl:value-of select="@name"/>

- *<xsl:value-of select="$i18n/moreInfo/*[name() = $lang]"/> :* 

<xsl:for-each select="help">
  <xsl:value-of select="."/>
</xsl:for-each>


<xsl:if test="helper">
<xsl:value-of select="$i18n/helper/*[name() = $lang]"/>
<xsl:for-each select="helper/option">
  - <xsl:value-of select="text()"/> (<xsl:value-of select="@value"/>)
</xsl:for-each>
</xsl:if>
      
      
      
 <xsl:text>


</xsl:text>
    </xsl:for-each>
  </xsl:template>
</xsl:stylesheet>