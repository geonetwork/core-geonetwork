<?xml version="1.0"?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:geonet="http://www.fao.org/geonetwork"
                version="1.0">
  <xsl:output method="xml"/>

  <!--
  F: contains all elements available in the record.
  The server should include in the retrieved record all of the elements
  for which there is data available in the database record and which can
  be encoded in the requested record syntax (e.g., some types of
  locally-defined binary data may not be encodable in a USMARC or SUTRS record).
  -->
  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="geonet:info"/>

</xsl:stylesheet>
