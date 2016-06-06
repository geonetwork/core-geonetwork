<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:gmd="http://www.isotc211.org/2005/gmd"
                version="2.0">

  <!-- copy everything that isn't part of the gmd namespace -->
  <xsl:template match="@*|node()">
    <xsl:copy copy-namespaces="no">
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

  <!-- add gmd prefix for everything that is part of the gmd namespace
         - note GeoNetwork needs the gmd prefix for matching element names and
               codelists -->
  <xsl:template match="*[namespace-uri()='http://www.isotc211.org/2005/gmd']">
    <xsl:element name="gmd:{local-name()}" namespace="http://www.isotc211.org/2005/gmd">
      <xsl:apply-templates select="@*|node()"/>
    </xsl:element>
  </xsl:template>
</xsl:stylesheet>
