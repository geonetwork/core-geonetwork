<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:geonet="http://www.fao.org/geonetwork"
                xmlns:saxon="http://saxon.sf.net/" version="2.0"
                extension-element-prefixes="saxon" exclude-result-prefixes="geonet saxon">

  <xsl:include href="../common.xsl"/>

  <xsl:variable name="mode" select="/root/request/currTab"/>

  <xsl:template match="/">
    <!--<html>
      <head>
        <link rel="stylesheet" type="text/css" href="../../style.css"/>
      </head>
      <body>-->
    <xsl:for-each select="/root/*[name(.)!='gui' and name(.)!='request']">
      <div class="metadata {$currTab}">
        <div>
          <!-- Tabs -->
          <xsl:call-template name="tab">
            <xsl:with-param name="tabLink"
                            select="concat(/root/gui/locService,'/metadata.update.new')"/>
            <xsl:with-param name="schema" select="geonet:info/schema"/>
          </xsl:call-template>

          <xsl:choose>
            <xsl:when test="starts-with($currTab, 'view')">
              <xsl:variable name="schemaTemplate" select="concat('metadata-',$schema, $currTab)"/>
              <saxon:call-template name="{$schemaTemplate}">
                <xsl:with-param name="schema" select="$schema"/>
                <xsl:fallback>
                  <xsl:message>Fall back as no saxon:call-template exists</xsl:message>
                  <table class="gn">
                    <tbody>
                      <xsl:apply-templates mode="elementEP" select=".">
                        <xsl:with-param name="edit" select="false()"/>
                      </xsl:apply-templates>
                    </tbody>
                  </table>
                </xsl:fallback>
              </saxon:call-template>
            </xsl:when>
            <xsl:when test="$currTab='xml'">
              <xsl:apply-templates mode="xmlDocument" select="."/>
            </xsl:when>
            <xsl:when test="$currTab='simple' or $currTab='inspire'">
              <xsl:variable name="schemaTemplate" select="concat('view-with-header-',$schema)"/>
              <saxon:call-template name="{$schemaTemplate}">
                <xsl:with-param name="tabs">
                  <xsl:apply-templates mode="elementEP" select=".">
                    <xsl:with-param name="edit" select="false()"/>
                  </xsl:apply-templates>
                </xsl:with-param>
              </saxon:call-template>
            </xsl:when>
            <xsl:otherwise>
              <table class="gn">
                <tbody>
                  <xsl:apply-templates mode="elementEP" select=".">
                    <xsl:with-param name="edit" select="false()"/>
                  </xsl:apply-templates>
                </tbody>
              </table>
            </xsl:otherwise>
          </xsl:choose>
        </div>
      </div>
    </xsl:for-each>
    <!--  </body>
    </html>-->
  </xsl:template>
</xsl:stylesheet>
