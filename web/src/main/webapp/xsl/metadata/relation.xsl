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

  <xsl:include href="common.xsl"/>

  <xsl:template match="/">
    <relations>
      <xsl:apply-templates mode="relation" select="/root/relations/*"/>
    </relations>
  </xsl:template>

  <xsl:template mode="relation" match="related|services|datasets|children|parent|sources|fcats|hasfeaturecat|siblings|associated">
    <xsl:apply-templates mode="relation" select="response/*">
      <xsl:with-param name="type" select="name(.)"/>
    </xsl:apply-templates>
  </xsl:template>

  <xsl:template mode="relation" match="sibling">
    <xsl:apply-templates mode="relation" select="*">
      <xsl:with-param name="type" select="'sibling'"/>
      <xsl:with-param name="subType" select="@initiative"/>
    </xsl:apply-templates>
  </xsl:template>

  <!-- Bypass summary elements -->
  <xsl:template mode="relation" match="summary" priority="99"/>

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
    
    <xsl:for-each select="*/descendant::*[name(.) = 'gmd:onLine']/*">
      <relation type="onlinesrc">
        
        <!-- Compute title based on online source info-->
        <xsl:variable name="title">
          <xsl:variable name="title" select="if (../@uuidref) then util:getIndexField(string(/root/gui/app/path), string(../@uuidref), '_title', string(/root/gui/language)) else ''"/>
          <xsl:value-of select="if ($title = '' and ../@uuidref) then ../@uuidref else $title"/><xsl:text> </xsl:text>
          <xsl:value-of select="if (gmd:name/gco:CharacterString != '') 
                                  then gmd:name/gco:CharacterString 
                                  else if (gmd:name/gmx:MimeFileType != '')
                                  then gmd:name/gmx:MimeFileType
                                  else gmd:description/gco:CharacterString"/>
          <xsl:value-of select="if (gmd:protocol/*) then concat(' (', gmd:protocol/*, ')') else ''"/>
        </xsl:variable>
        
        <id><xsl:value-of select="gmd:linkage/gmd:URL"/></id>
        <title>
          <xsl:value-of select="if ($title != '') then $title else gmd:linkage/gmd:URL"/>
        </title>
        <abstract><xsl:value-of select="gmd:description/gco:CharacterString"/></abstract>
      </relation>
    </xsl:for-each>
  </xsl:template>
  

  <!-- In Lucene only mode, metadata are retrieved from 
  the index and pass as a simple XML with one level element.
  Make a simple copy here. -->
  <xsl:template mode="superBrief" match="metadata">
    <xsl:copy>
      <xsl:copy-of select="*|@*"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template mode="relation" match="*">
    <xsl:param name="type"/>
    <xsl:param name="subType" select="''"/>

    <!-- Fast output doesn't produce a full metadata record -->
    <xsl:variable name="md">
      <xsl:apply-templates mode="superBrief" select="."/>
    </xsl:variable>
    <xsl:variable name="metadata" select="exslt:node-set($md)"/>

    <relation type="{$type}">
			<xsl:if test="normalize-space($subType)!=''">
				<xsl:attribute name="subType">
					<xsl:value-of select="$subType"/>		
				</xsl:attribute>
			</xsl:if>
      <xsl:copy-of select="$metadata"/>
    </relation>
  </xsl:template>
  
  <!-- Add the default title as title. This may happen
  when title is retrieve from index and the record is
  not available in current language. eg. iso19110 records
  are only indexed with no language info. -->
  <xsl:template mode="superBrief" match="metadata[not(title)]">
    <xsl:copy>
      <xsl:copy-of select="*"/>
      <title><xsl:value-of select="defaultTitle"/></title>
    </xsl:copy>
  </xsl:template>
</xsl:stylesheet>
