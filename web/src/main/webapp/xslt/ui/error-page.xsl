<?xml version="1.0" encoding="UTF-8"?>
<!--
  ~ Copyright (C) 2001-2016 Food and Agriculture Organization of the
  ~ United Nations (FAO-UN), United Nations World Food Programme (WFP)
  ~ and United Nations Environment Programme (UNEP)
  ~
  ~ This program is free software; you can redistribute it and/or modify
  ~ it under the terms of the GNU General Public License as published by
  ~ the Free Software Foundation; either version 2 of the License, or (at
  ~ your option) any later version.
  ~
  ~ This program is distributed in the hope that it will be useful, but
  ~ WITHOUT ANY WARRANTY; without even the implied warranty of
  ~ MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  ~ General Public License for more details.
  ~
  ~ You should have received a copy of the GNU General Public License
  ~ along with this program; if not, write to the Free Software
  ~ Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
  ~
  ~ Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
  ~ Rome - Italy. email: geonetwork@osgeo.org
  -->

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
>


  <xsl:template match="/">
    <html>
      <head>
        <link href="../../catalog/lib/style/bootstrap-3.0.1/bootstrap.less" rel="stylesheet/less"
              type="text/css"/>
        <script src="../../catalog/lib/less-1.4.1.min.js"></script>
      </head>
      <body class="container">
        <h1 class="text-danger">
          <xsl:value-of select="/root/gui/startupError/error/Error"/>
        </h1>
        <div class="alert alert-danger">
          <table>
            <xsl:apply-templates mode="showError"
                                 select="/root/gui/startupError/error/*[name()!='Error' and name()!='Stack']"/>
            <xsl:apply-templates mode="showError" select="/root/gui/startupError/error/Stack"/>
          </table>
        </div>
      </body>
    </html>
  </xsl:template>

  <xsl:template mode="showError" match="*">
    <tr>
      <td>
        <strong>
          <xsl:value-of select="name(.)"/>
        </strong>
      </td>
      <td>
        <pre>
          <xsl:value-of select="string(.)"/>
        </pre>
      </td>
    </tr>
  </xsl:template>

</xsl:stylesheet>
