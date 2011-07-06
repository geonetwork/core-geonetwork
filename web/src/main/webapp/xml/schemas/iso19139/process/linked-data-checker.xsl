<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:geonet="http://www.fao.org/geonetwork" 
  xmlns:gco="http://www.isotc211.org/2005/gco" xmlns:gmd="http://www.isotc211.org/2005/gmd" 
  xmlns:java="java:org.fao.geonet.util.XslUtil" version="2.0" exclude-result-prefixes="#all">

  <xsl:import href="process-utility.xsl"/>
  
  <xsl:param name="linkUrl"/>
  
  <!-- i18n information -->
  <xsl:variable name="linked-data-checker-loc">
    <msg id="a" xml:lang="en"> return an error (</msg>
    <msg id="b" xml:lang="en">). Run this task to remove it.</msg>
    <msg id="a" xml:lang="fr"> a retourné une erreur (</msg>
    <msg id="b" xml:lang="fr">). Si l'erreur persiste, corriger le lien manuellement ou exécuter cette action pour le supprimer.</msg>
  </xsl:variable>

  <xsl:template name="list-linked-data-checker">
    <suggestion process="linked-data-checker"/>
  </xsl:template>

  <!-- Analyze the metadata record and return available suggestion
    for that process -->
  <xsl:template name="analyze-linked-data-checker">
    <xsl:param name="root"/>
    
    <!-- Check URL -->
    <xsl:variable name="httpLinks"
      select="$root//*[starts-with(., 'http') and name(..) != 'geonet:info']"/>
    <xsl:for-each-group select="$httpLinks" group-by=".">
      <xsl:call-template name="checkUrl">
        <xsl:with-param name="url" select="."/>
      </xsl:call-template>
    </xsl:for-each-group>
  </xsl:template>

  <xsl:template name="checkUrl">
    <xsl:param name="url"/>
    <xsl:param name="type"/>
    
    <xsl:variable name="status" select="java:getUrlStatus($url)"/>
<!--    <xsl:message>Check:<xsl:value-of select="."/>|<xsl:value-of select="$status"/></xsl:message>
-->    
      <xsl:if test="$status!=''">
      <suggestion process="linked-data-checker" id="{generate-id()}" category="links" target="all">
        <name xml:lang="en">
          <xsl:value-of select="$type"/> <xsl:value-of select="."/>
          <xsl:value-of select="geonet:i18n($linked-data-checker-loc, 'a', $guiLang)"/> 
          <xsl:value-of select="$status"/>
          <xsl:value-of select="geonet:i18n($linked-data-checker-loc, 'b', $guiLang)"/></name>
        <operational>true</operational>
        <params>{ linkUrl:{type:'string', defaultValue:'<xsl:value-of select="normalize-space($url)"/>'}
                }</params>
      </suggestion>
    </xsl:if>
  </xsl:template>

  <!-- Do a copy of every nodes and attributes -->
  <xsl:template match="@*|node()">
      <xsl:copy>
          <xsl:apply-templates select="@*|node()"/>
      </xsl:copy>
  </xsl:template>

  <!-- Always remove geonet:* elements. -->
  <xsl:template match="geonet:*" priority="2"/>

  <!-- Remove the link. TODO : remove the parent ? -->
  <xsl:template match="*[text()=$linkUrl]" priority="2"/>
  

</xsl:stylesheet>
