<?xml version="1.0" encoding="UTF-8" ?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:geonet="http://www.fao.org/geonetwork"
                version="2.0" exclude-result-prefixes="xsl geonet">

  <xsl:import href="metadata.xsl"/>

  <xsl:include href="metadata-fop.xsl"/>
  <xsl:include href="utils.xsl"/>


  <!-- =============================================
    Start FOP layout
  -->
  <xsl:template match="/root">
    <fo:root xmlns:fo="http://www.w3.org/1999/XSL/Format">
      <xsl:call-template name="fop-master"/>

      <fo:page-sequence master-reference="simpleA4" initial-page-number="1">

        <xsl:call-template name="fop-footer"/>

        <fo:flow flow-name="xsl-region-body">

          <!-- Banner level -->
          <xsl:call-template name="banner"/>


          <fo:block font-size="{$font-size}">

            <fo:table width="100%" table-layout="fixed">
              <fo:table-column column-width="1.8cm"/>
              <fo:table-column column-width="18.2cm"/>
              <fo:table-body>
                <fo:table-row height="8mm">
                  <fo:table-cell display-align="center" number-columns-spanned="2">
                    <fo:block text-align="center" color="{$font-color}">
                      <xsl:value-of select="/root/response/summary/@count"/><xsl:text> </xsl:text><xsl:value-of
                      select="/root/gui/strings/ress"/>
                      <xsl:if test="/root/response/summary/@count &gt; 1">s</xsl:if>
                    </fo:block>
                  </fo:table-cell>
                </fo:table-row>

                <xsl:variable name="remote" select="/root/response/summary/@type='remote'"/>

                <xsl:call-template name="fo">
                  <xsl:with-param name="res" select="/root/response"/>
                  <xsl:with-param name="gui" select="/root/gui"/>
                  <xsl:with-param name="server" select="//server"/>
                  <xsl:with-param name="remote" select="$remote"/>

                </xsl:call-template>
              </fo:table-body>
            </fo:table>
          </fo:block>

          <fo:block id="terminator"/>
        </fo:flow>
      </fo:page-sequence>
    </fo:root>
  </xsl:template>

  <!-- ============================================================== -->

  <xsl:template match="geonet:info/title" mode="strip"/>

  <xsl:template match="@*|node()" mode="strip">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()" mode="strip"/>
    </xsl:copy>
  </xsl:template>

</xsl:stylesheet>
