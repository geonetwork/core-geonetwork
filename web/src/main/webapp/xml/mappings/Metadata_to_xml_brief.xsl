<?xml version="1.0"?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:exslt="http://exslt.org/common"
                version="3.0">
  <xsl:output method="xml"/>

  <xsl:include href="../../xsl/utils.xsl"/>
  <xsl:include href="../../xsl/metadata.xsl"/>

  <xsl:template match="/">

    <xsl:variable name="md">
      <xsl:apply-templates mode="brief" select="*"/>
    </xsl:variable>
    <xsl:variable name="metadata" select="exslt:node-set($md)/*[1]"/>

    <!--
      B: includes the Title (title) element.
      -->
    <metadata>
      <idinfo>
        <citation>
          <citeinfo>
            <title>
              <xsl:value-of select="$metadata/title"/>
            </title>
          </citeinfo>
        </citation>
      </idinfo>
    </metadata>

  </xsl:template>

</xsl:stylesheet>
