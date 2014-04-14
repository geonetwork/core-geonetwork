<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:gmd="http://www.isotc211.org/2005/gmd" xmlns:gts="http://www.isotc211.org/2005/gts"
  xmlns:gco="http://www.isotc211.org/2005/gco" xmlns:gmx="http://www.isotc211.org/2005/gmx"
  xmlns:gfc="http://www.isotc211.org/2005/gfc" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xmlns:gml="http://www.opengis.net/gml" xmlns:xlink="http://www.w3.org/1999/xlink"
  xmlns:gn="http://www.fao.org/geonetwork"
  xmlns:gn-fn-core="http://geonetwork-opensource.org/xsl/functions/core" 
  xmlns:gn-fn-metadata="http://geonetwork-opensource.org/xsl/functions/metadata"
  xmlns:gn-fn-iso19110="http://geonetwork-opensource.org/xsl/functions/profiles/iso19110"
  xmlns:exslt="http://exslt.org/common" exclude-result-prefixes="#all">


  <!-- Readonly elements -->
  <xsl:template mode="mode-iso19110" priority="200" match="gmx:versionDate|gfc:versionDate">
    
    <xsl:call-template name="render-element">
      <xsl:with-param name="label" select="gn-fn-metadata:getLabel($schema, name(), $labels)/label"/>
      <xsl:with-param name="value" select="*"/>
      <xsl:with-param name="cls" select="local-name()"/>
      <xsl:with-param name="xpath" select="gn-fn-metadata:getXPath(.)"/>
      <xsl:with-param name="type" select="gn-fn-iso19110:getFieldType(name(), '')"/>
      <xsl:with-param name="name" select="''"/>
      <xsl:with-param name="editInfo" select="*/gn:element"/>
      <xsl:with-param name="parentEditInfo" select="gn:element"/>
      <xsl:with-param name="isDisabled" select="true()"/>
    </xsl:call-template>
    
  </xsl:template>
  

  <!-- Do not display those elements:
     * hide nested featureType elements
     * hide definition reference elements
     * inheritance : does not support linking feature catalogue objects (eg. to indicate subtype or supertype) 
    -->
  <xsl:template mode="mode-iso19110" match="gfc:featureType[ancestor::gfc:featureType]|
    gfc:featureCatalogue|
    gfc:FC_InheritanceRelation/gfc:featureCatalogue|
    gn:child[@name='featureCatalogue']|
    gfc:FC_InheritanceRelation/gn:child[@name='subtype']|
    gfc:FC_InheritanceRelation/gn:child[@name='supertype']
    " priority="100"/>

</xsl:stylesheet>
