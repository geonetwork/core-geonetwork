<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:gmd="http://www.isotc211.org/2005/gmd" xmlns:gts="http://www.isotc211.org/2005/gts"
  xmlns:gco="http://www.isotc211.org/2005/gco" xmlns:gmx="http://www.isotc211.org/2005/gmx"
  xmlns:srv="http://www.isotc211.org/2005/srv" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xmlns:gml="http://www.opengis.net/gml" xmlns:xlink="http://www.w3.org/1999/xlink"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:gn="http://www.fao.org/geonetwork"
  xmlns:gn-fn-metadata="http://geonetwork-opensource.org/xsl/functions/metadata"
  xmlns:gn-fn-iso19139.emodnet.hydrography="http://geonetwork-opensource.org/xsl/functions/profiles/iso19139.emodnet.hydrography"
  xmlns:saxon="http://saxon.sf.net/" extension-element-prefixes="saxon"
  exclude-result-prefixes="#all">
  
  
  
  <!-- 
    Load the schema configuration for the editor.
      -->
  <xsl:template name="get-iso19139.emodnet.hydrography-configuration"/>

  <xsl:template name="dispatch-iso19139.emodnet.hydrography">
    <xsl:param name="base" as="node()"/>
    <xsl:param name="overrideLabel" as="xs:string" required="no" select="''"/>
  </xsl:template>

  <xsl:template name="evaluate-iso19139.emodnet.hydrography">
    <xsl:param name="base" as="node()"/>
    <xsl:param name="in"/>
  </xsl:template>

  <xsl:template name="evaluate-iso19139.emodnet.hydrography-boolean">
    <xsl:param name="base" as="node()"/>
    <xsl:param name="in"/>
  </xsl:template>

  <xsl:template name="get-iso19139.emodnet.hydrography-language"/>
  <xsl:template name="get-iso19139.emodnet.hydrography-other-languages-as-json"/>
  <xsl:template name="get-iso19139.emodnet.hydrography-geopublisher-config"/>
</xsl:stylesheet>
