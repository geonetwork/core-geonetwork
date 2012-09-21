<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" 
  xmlns:dc="http://purl.org/dc/elements/1.1/"    
  xmlns:ows="http://www.opengis.net/ows"
  xmlns:csw="http://www.opengis.net/cat/csw/2.0.2"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xmlns:geonet="http://www.fao.org/geonetwork"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform" exclude-result-prefixes="#all">

  <xsl:template match="csw:Record|csw:SummaryRecord|csw:BriefRecord" mode="record-reference"/>
  <xsl:template match="csw:Record|csw:SummaryRecord|csw:BriefRecord" mode="to-dcat"/>
  <xsl:template match="csw:Record|csw:SummaryRecord|csw:BriefRecord" mode="references"/>
  <xsl:template mode="metadata" match="csw:Record|csw:SummaryRecord|csw:BriefRecord"/>
  
</xsl:stylesheet>
