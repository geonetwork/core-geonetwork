<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:exslt="http://exslt.org/common"
                version="1.0"
                xmlns="http://www.isotc211.org/2005/gmd" exclude-result-prefixes="exslt">
  <!-- This stylesheet converts ISO19115 and ISO19139 metadata into ISO19139 metadata in XML format -->
  <xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes"/>
  <xsl:namespace-alias stylesheet-prefix="#default" result-prefix="gmd"/>

  <xsl:include href="../19115to19139/19115-to-19139.xsl"/>

  <xsl:template match="/root">
    <!-- Export ISO19115 converting it to ISO19115/19139 XML -->
    <xsl:variable name="md">
      <xsl:apply-templates select="Metadata"/>
    </xsl:variable>
    <!--    <xsl:apply-templates select="exslt:node-set($md)/*[1]"/> -->
    <xsl:apply-templates select="exslt:node-set($md)"/>
  </xsl:template>

  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()[name(self::*)!='geonet:info']"/>
    </xsl:copy>
  </xsl:template>

  <!-- //FIXME
    <xsl:template match="node()">
        <xsl:element name="gmd:{name(current()[namespace-uri()='http://www.isotc211.org/2005/gmd'])}" namespace="http://www.isotc211.org/2005/gmd">
            <xsl:copy-of select="@*"/>
            <xsl:apply-templates />
        </xsl:element>
    </xsl:template> -->

</xsl:stylesheet>
