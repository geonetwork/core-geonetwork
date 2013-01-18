<?xml version="1.0" encoding="UTF-8"?>
<!--
  Edit metadata embedded processing - called by AddElement to add
  a piece of metadata to the editor form
  
  TODO : http://trac.osgeo.org/geonetwork/ticket/122
  -->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:geonet="http://www.fao.org/geonetwork" exclude-result-prefixes="geonet">

  <xsl:output method="html" encoding="UTF-8" indent="yes"/>

  <xsl:include href="../common.xsl"/>


  <xsl:template match="/">
    <xsl:for-each select="/root/*[name(.)!='gui' and name(.)!='request']//*[@geonet:addedObj = 'true']">
      <xsl:apply-templates mode="elementEP" select=".">
        <xsl:with-param name="edit" select="true()"/>
        <xsl:with-param name="schema">
          <xsl:apply-templates mode="schema" select="."/>
        </xsl:with-param>
        <xsl:with-param name="embedded" select="true()"/>
      </xsl:apply-templates>
    </xsl:for-each>
  </xsl:template>

</xsl:stylesheet>
