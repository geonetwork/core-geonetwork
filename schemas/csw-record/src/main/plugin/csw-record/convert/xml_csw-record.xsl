<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0"  
		xmlns:dc="http://purl.org/dc/elements/1.1/"    
		xmlns:ows="http://www.opengis.net/ows"
		xmlns:csw="http://www.opengis.net/cat/csw/2.0.2"
		xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
		xmlns:geonet="http://www.fao.org/geonetwork"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <!-- This stylesheet produces iso19135 metadata in XML format -->
  <xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes" />

  <!-- Metadata is passed under /root XPath -->
  <xsl:template match="/root">
    <!-- Export csw-record XML (just a copy) -->
    <xsl:apply-templates select="csw:Record|csw:SummaryRecord|csw:BriefRecord"/>
  </xsl:template>

	<!-- Delete any GeoNetwork specific elements -->
  <xsl:template match="geonet:*"/> 

  <!-- Copy everything else -->
  <xsl:template match="@*|node()">
      <xsl:copy>
        <xsl:apply-templates select="@*|node()[name(self::*)!='geonet:info']"/>
      </xsl:copy>
  </xsl:template>

</xsl:stylesheet>
