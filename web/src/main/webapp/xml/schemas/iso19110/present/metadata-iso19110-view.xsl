<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:geonet="http://www.fao.org/geonetwork"
  xmlns:gfc="http://www.isotc211.org/2005/gfc" xmlns:gmx="http://www.isotc211.org/2005/gmx"
  xmlns:gco="http://www.isotc211.org/2005/gco" xmlns:gmd="http://www.isotc211.org/2005/gmd"
  exclude-result-prefixes="#all">

  <!-- iso19110-simple -->
  <xsl:template name="metadata-iso19110view-simple" match="metadata-iso19110view-simple">

    <xsl:call-template name="md-content">
      <xsl:with-param name="title" select="//gfc:FC_FeatureCatalogue/gfc:name"/>
      <xsl:with-param name="exportButton"/>
      <xsl:with-param name="abstract"/>
      <xsl:with-param name="logo">
        <img src="../../images/logos/{//geonet:info/source}.gif" alt="logo"/>
      </xsl:with-param>
      <xsl:with-param name="relatedResources">
        <table class="related">
          <tbody>
            <tr style="display:none;"><!-- FIXME needed by JS-->
              <td class="main"></td><td></td>
            </tr>
          </tbody>
        </table>
      </xsl:with-param>
      <xsl:with-param name="tabs">

        <xsl:apply-templates mode="iso19110" select=".">
          <xsl:with-param name="schema" select="$schema"/>
          <xsl:with-param name="edit" select="false()"/>
        </xsl:apply-templates>

      </xsl:with-param>
    </xsl:call-template>
  </xsl:template>

</xsl:stylesheet>
