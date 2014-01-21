<?xml version="1.0" encoding="UTF-8"?>
<!-- 
  Create a simple XML tree for relation description.
  <relations>
    <relation type="related|services|children">
      + super-brief representation.
-->
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:gmd="http://www.isotc211.org/2005/gmd" xmlns:gco="http://www.isotc211.org/2005/gco"
  xmlns:gmx="http://www.isotc211.org/2005/gmx" 
  xmlns:geonet="http://www.fao.org/geonetwork" 
  xmlns:util="xalan://org.fao.geonet.util.XslUtil"
  xmlns:exslt="http://exslt.org/common"
  exclude-result-prefixes="geonet exslt">

  <!-- Relation contained in the metadata record has to be returned 
  It could be document or thumbnails
  -->
  <xsl:template mode="relation" match="metadata[gmd:MD_Metadata or *[contains(@gco:isoType, 'MD_Metadata')]]" priority="99">
    
    <xsl:for-each select="*/descendant::*[name(.) = 'gmd:graphicOverview']/*">
      <relation type="thumbnail">
        <id><xsl:value-of select="gmd:fileName/gco:CharacterString"/></id>
        <title><xsl:value-of select="gmd:fileDescription/gco:CharacterString"/></title>
      </relation>
    </xsl:for-each>
    
    <xsl:for-each select="*/descendant::*[name(.) = 'gmd:onLine']/*[gmd:linkage/gmd:URL!='']">
      <relation type="onlinesrc">
        
        <!-- Compute title based on online source info-->
        <xsl:variable name="title">
          <xsl:variable name="title" select="''"/>
          <xsl:value-of select="if ($title = '' and ../@uuidref) then ../@uuidref else $title"/><xsl:text> </xsl:text>
          <xsl:value-of select="if (gmd:name/gco:CharacterString != '') 
            then gmd:name/gco:CharacterString 
            else if (gmd:name/gmx:MimeFileType != '')
            then gmd:name/gmx:MimeFileType
            else if (gmd:description/gco:CharacterString != '')
            then gmd:description/gco:CharacterString
            else gmd:linkage/gmd:URL"/>
        </xsl:variable>
        
        <id><xsl:value-of select="gmd:linkage/gmd:URL"/></id>
        <title>
          <xsl:value-of select="if ($title != '') then $title else gmd:linkage/gmd:URL"/>
        </title>
        <url>
          <xsl:value-of select="gmd:linkage/gmd:URL"/>
        </url>
        <name>
          <xsl:value-of select="gmd:name/gco:CharacterString"/>
        </name>        
        <abstract><xsl:value-of select="gmd:description/gco:CharacterString"/></abstract>
        <description><xsl:value-of select="gmd:description/gco:CharacterString"/></description>
        <protocol><xsl:value-of select="gmd:protocol/gco:CharacterString"/></protocol>
      </relation>
    </xsl:for-each>
  </xsl:template>
</xsl:stylesheet>
