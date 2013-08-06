<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:gn="http://www.fao.org/geonetwork"
  xmlns:xs="http://www.w3.org/2001/XMLSchema">


  <!-- Copy all elements and attributes excluding GeoNetwork elements. 
    This could be useful to get the source XML when working on a metadocument.
  -->
  <xsl:template match="@*|node()[namespace-uri()!='http://www.fao.org/geonetwork']" mode="gn-element-cleaner">
    <xsl:copy>
      <xsl:copy-of select="@*[namespace-uri()!='http://www.fao.org/geonetwork']"/>
      <xsl:apply-templates select="node()" mode="gn-element-cleaner"/>
    </xsl:copy>
  </xsl:template>
  <!-- Remove GeoNetwork info element and children -->
  <xsl:template mode="gn-element-cleaner" match="gn:info" priority="2"/>


</xsl:stylesheet>
