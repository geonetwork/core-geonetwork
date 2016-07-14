<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:geonet="http://www.fao.org/geonetwork"
                xmlns:exslt="http://exslt.org/common"
                version="1.0"
                exclude-result-prefixes="#all">

  <xsl:output
    omit-xml-declaration="yes"
    method="html"
    doctype-public="-//W3C//DTD HTML 4.01 Transitional//EN"
    doctype-system="http://www.w3.org/TR/html4/loose.dtd"
    indent="yes"
    encoding="UTF-8"/>

  <xsl:include href="../../../../xsl/utils.xsl"/>
  <xsl:include href="../../../../xsl/metadata.xsl"/>

  <xsl:variable name="baseurl" select="//geonet:info/baseUrl"/>
  <xsl:variable name="locserv" select="//geonet:info/locService"/>

  <xsl:template match="/">
    <xsl:apply-templates mode="doit" select="/root/metadata/*[1]"/>
  </xsl:template>

  <xsl:template mode="doit" match="*">
    <html>
      <head>
        <xsl:call-template name="header"/>
      </head>
      <body>
        <table width="100%">
          <!-- content -->
          <tr>
            <td>
              <xsl:call-template name="content"/>
            </td>
          </tr>
        </table>
      </body>
    </html>
  </xsl:template>

  <xsl:template name="header">
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>

    <!-- use metadata title for title of page -->
    <xsl:variable name="md">
      <xsl:apply-templates mode="brief" select="."/>
    </xsl:variable>
    <xsl:variable name="metadata" select="exslt:node-set($md)/*[1]"/>
    <title>
      <xsl:value-of select="$metadata/title"/>
    </title>
    <META HTTP-EQUIV="Pragma" CONTENT="no-cache"/>
    <META HTTP-EQUIV="Expires" CONTENT="-1"/>

    <!-- include enough js stuff to do tooltips -->
    <script type="text/javascript">
      var Env = new Object();

      Env.host = "<xsl:value-of select="substring-before($baseurl,'/geonetwork')"/>";
      Env.url = "<xsl:value-of select="$baseurl"/>"
      Env.locService= "<xsl:value-of select="concat($baseurl,$locserv)"/>";
    </script>

    <!-- Actually no translations supplied here because /root/gui/strings not
             available but initialize the variable anyway -->

    <script language="JavaScript" type="text/javascript">
      var translations = {
      <xsl:apply-templates select="/root/gui/strings/*[@js='true' and not(*) and not(@id)]"
                           mode="js-translations"/>
      };
    </script>

    <link href="{$baseurl}/images/logos/favicon.png" rel="shortcut icon" type="image/x-icon"/>
    <link href="{$baseurl}/images/logos/favicon.png" rel="icon" type="image/x-icon"/>

    <!-- stylesheet -->
    <link rel="stylesheet" type="text/css" href="{$baseurl}/geonetwork.css"/>
    <link rel="stylesheet" type="text/css" href="{$baseurl}/modalbox.css"/>

    <!-- scripts -->
    <script type="text/javascript" src="{$baseurl}/scripts/prototype.js">
      // This is a comment - otherwise nothing works
    </script>
    <script type="text/javascript" src="{$baseurl}/static/kernel.js">
      // This is a comment - otherwise nothing works
    </script>
    <script type="text/javascript" src="{$baseurl}/scripts/geonetwork.js">
      // This is a comment - otherwise nothing works
    </script>
    <script type="text/javascript" src="{$baseurl}/scripts/editor/simpletooltip.js">
      // This is a comment - otherwise nothing works
    </script>
  </xsl:template>

  <!--
    page content
    -->
  <xsl:template name="content">
    <table width="100%">
      <tr>
        <td class="content" valign="top">
          <table width="100%">
            <tr>
              <td class="padded-content">
                <table class="md" width="100%">
                  <xsl:apply-templates mode="elementEP" select=".">
                    <xsl:with-param name="embedded" select="true()"/>
                  </xsl:apply-templates>
                </table>
              </td>
            </tr>
          </table>
        </td>
      </tr>
      <tr>
        <td class="blue-content"/>
      </tr>
    </table>
  </xsl:template>

</xsl:stylesheet>
