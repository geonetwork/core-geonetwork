<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:geonet="http://www.fao.org/geonetwork" 
  xmlns:gco="http://www.isotc211.org/2005/gco" xmlns:gmd="http://www.isotc211.org/2005/gmd" 
  xmlns:java="java:org.fao.geonet.util.XslUtil" version="2.0" exclude-result-prefixes="#all">

  <xsl:import href="process-utility.xsl"/>
  
  <xsl:param name="uuidToRemove"/>
  
  <!-- i18n information -->
  <xsl:variable name="related-metadata-checker-loc">
    <msg id="a" xml:lang="eng"> not found (</msg>
    <msg id="b" xml:lang="eng">). Run this task to remove it.</msg>
    <msg id="a" xml:lang="fre"> non trouvé (</msg>
    <msg id="b" xml:lang="fre">). Si l'erreur persiste, corriger le lien manuellement ou exécuter cette action pour le supprimer.</msg>
  </xsl:variable>

  <xsl:template name="list-related-metadata-checker">
    <suggestion process="related-metadata-checker"/>
  </xsl:template>

  <!-- Analyze the metadata record and return available suggestion
    for that process -->
  <xsl:template name="analyze-related-metadata-checker">
    <xsl:param name="root"/>
    
    <!-- Check URL -->
    <xsl:variable name="links"
      select="$root//gmd:parentIdentifier/gco:CharacterString"/>
    <xsl:for-each select="$links">
      <xsl:call-template name="checkUuid">
        <xsl:with-param name="uuid" select="."/>
      </xsl:call-template>
    </xsl:for-each>
  </xsl:template>

  <xsl:template name="checkUuid">
    <xsl:param name="uuid"/>
    <xsl:param name="type"/>
    
    <xsl:variable name="status" select="java:getIndexField($baseUrl, '_uuid', $uuid, 'en')"/>
<!--    <xsl:message>Check:<xsl:value-of select="."/>|<xsl:value-of select="$status"/></xsl:message>
-->    
      <xsl:if test="$status=''">
      <suggestion process="related-metadata-checker" id="{generate-id()}" category="links" target="all">
        <name xml:lang="en">
          <xsl:value-of select="$type"/> <xsl:value-of select="."/>
          <xsl:value-of select="geonet:i18n($related-metadata-checker-loc, 'a', $guiLang)"/> 
          <xsl:value-of select="."/>:<xsl:value-of select="$status"/>
          <xsl:value-of select="geonet:i18n($related-metadata-checker-loc, 'b', $guiLang)"/></name>
        <operational>true</operational>
        <params>{ uuidToRemove:{type:'string', defaultValue:'<xsl:value-of select="normalize-space($uuid)"/>'}
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

  <!-- Remove the uuid. TODO : remove the parent ? -->
  <xsl:template match="*[text()=$uuidToRemove]" priority="2"/>
  

</xsl:stylesheet>
