<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:exslt="http://exslt.org/common" xmlns:geonet="http://www.fao.org/geonetwork"
  xmlns:gco="http://www.isotc211.org/2005/gco" xmlns:srv="http://www.isotc211.org/2005/srv"
  xmlns:gmd="http://www.isotc211.org/2005/gmd" xmlns:xs="http://www.w3.org/2001/XMLSchema"
  version="2.0" exclude-result-prefixes="exslt">
  
  <!-- Language of the GUI -->
  <xsl:param name="guiLang" select="'en'"/>
  <xsl:param name="baseUrl" select="''"/>
  
  <!-- Search for any of the searchStrings provided -->
  <xsl:function name="geonet:parseBoolean" as="xs:boolean">
    <xsl:param name="arg"/>
    <xsl:value-of
      select="if ($arg='on' or $arg=true() or $arg='true' or $arg='1') then true() else false()"/>
  </xsl:function>

  <!-- Return the message identified by the id in the required language
  or return the english message if not found. -->
  <xsl:function name="geonet:i18n" as="xs:string">
    <xsl:param name="loc"/>
    <xsl:param name="id"/>
    <xsl:param name="lang"/>
    <xsl:value-of
      select="if ($loc/msg[@id=$id and @xml:lang=$lang]) then $loc/msg[@id=$id and @xml:lang=$lang] else $loc/msg[@id=$id and @xml:lang='en']"/>
  </xsl:function>

  <!-- 
  Retrive a WMS capabilities document.
  -->
  <xsl:function name="geonet:get-wms-capabilities" as="node()">
    <xsl:param name="url" as="xs:string"/>
    <xsl:param name="version" as="xs:string"/>
    
    <xsl:copy-of
      select="geonet:get-wxs-capabilities($url, 'WMS', $version)"/>
    
  </xsl:function>

  <xsl:function name="geonet:get-wxs-capabilities" as="node()">
    <xsl:param name="url" as="xs:string"/>
    <xsl:param name="type" as="xs:string"/>
    <xsl:param name="version" as="xs:string"/>
    <xsl:variable name="sep" select="if (contains($url, '?')) then '&amp;' else '?'"/>
    <xsl:copy-of
      select="document(concat($url, $sep, 'SERVICE=', $type, '&amp;VERSION=', $version, '&amp;REQUEST=GetCapabilities'))"/>
    
  </xsl:function>

  <!-- Create a GetMap request for the layer which could be used to set a thumbnail.
  TODO : add projection, width, heigth
  -->
  <xsl:function name="geonet:get-wms-thumbnail-url" as="xs:string">
    <xsl:param name="url" as="xs:string"/>
    <xsl:param name="version" as="xs:string"/>
    <xsl:param name="layer" as="xs:string"/>
    <xsl:param name="bbox" as="xs:string"/>
    
    <xsl:value-of
      select="concat($url, '?SERVICE=WMS&amp;VERSION=', $version, '&amp;REQUEST=GetMap&amp;SRS=EPSG:4326&amp;WIDTH=400&amp;HEIGHT=400&amp;FORMAT=image/png&amp;STYLES=&amp;LAYERS=', $layer, '&amp;BBOX=', $bbox)"/>
    
  </xsl:function>
  

  <!-- Create an ISO 19139 extent fragment -->
  <xsl:function name="geonet:make-iso-extent" as="node()">
    <xsl:param name="w" as="xs:string"/>
    <xsl:param name="s" as="xs:string"/>
    <xsl:param name="e" as="xs:string"/>
    <xsl:param name="n" as="xs:string"/>
    <xsl:param name="description" as="xs:string?"/>

    <gmd:EX_Extent>
      <xsl:if test="normalize-space($description)!=''">
        <gmd:description>
          <gco:CharacterString>
            <xsl:value-of select="$description"/>
          </gco:CharacterString>
        </gmd:description>
      </xsl:if>
      <gmd:geographicElement>
        <gmd:EX_GeographicBoundingBox>
          <gmd:westBoundLongitude>
            <gco:Decimal>
              <xsl:value-of select="$w"/>
            </gco:Decimal>
          </gmd:westBoundLongitude>
          <gmd:eastBoundLongitude>
            <gco:Decimal>
              <xsl:value-of select="$e"/>
            </gco:Decimal>
          </gmd:eastBoundLongitude>
          <gmd:southBoundLatitude>
            <gco:Decimal>
              <xsl:value-of select="$s"/>
            </gco:Decimal>
          </gmd:southBoundLatitude>
          <gmd:northBoundLatitude>
            <gco:Decimal>
              <xsl:value-of select="$n"/>
            </gco:Decimal>
          </gmd:northBoundLatitude>
        </gmd:EX_GeographicBoundingBox>
      </gmd:geographicElement>
    </gmd:EX_Extent>
  </xsl:function>

</xsl:stylesheet>
