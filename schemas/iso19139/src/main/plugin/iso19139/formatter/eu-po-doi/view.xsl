<?xml version="1.0" encoding="UTF-8"?>
<!-- Conversion from ISO19139 to European Union Publication Office DOI
http://ra.publications.europa.eu/schema/OP/DOIMetadata/1.0/OP_DOIMetadata_1.0.xsd

     To retrieve a record:
     http://localhost:8080/geonetwork/srv/api/records/ff8d8cd6-c753-4581-99a3-af23fe4c996b/formatters/eu-po-doi?output=xml
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:eu="http://ra.publications.europa.eu/schema/doidata/1.0"
                xmlns:grant_id="http://www.crossref.org/grant_id/0.1.1"
                xmlns:datacite="http://datacite.org/schema/kernel-4"
                version="2.0"
                exclude-result-prefixes="#all">

  <xsl:import href="../../../iso19115-3.2018/formatter/eu-po-doi/base.xsl"/>
  <xsl:import href="../datacite/view.xsl"/>

  <xsl:output method="xml"
              indent="yes"/>

  <xsl:template match="/">
    <xsl:call-template name="eu-po-doi-message">
      <xsl:with-param name="dataciteResource">
        <datacite:resource>
          <xsl:apply-templates select="$metadata"
                               mode="toDatacite"/>
        </datacite:resource>
      </xsl:with-param>
    </xsl:call-template>
  </xsl:template>
</xsl:stylesheet>
