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

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

  <!--
  Create xml containing change password email details from user/instance details passed
  Allows email to be customised without changing java service - info supplied is as follows

<root>
  <username>JonesC</username>
  <email>Craig.Jones@utas.edu.au</email>
  <site>localtrunk</site>
  <siteURL>http://127.0.0.1:8122/geonetwork</siteURL>
  <changeKey>3MRaEX</changeKey>
</root>

  -->
  <xsl:template match="/">
    <request>
      <subject>
        <xsl:value-of select="root/site"/> password change link for
        <xsl:value-of select="root/username"/>
      </subject>
      <to>
        <xsl:value-of select="root/email"/>
      </to>
      <content>You have requested to change your
        <xsl:value-of select="root/site"/> password.

        You can change your password using the following link:

        <xsl:value-of select="root/siteURL"/>/srv/eng/new.password?username=<xsl:value-of
          select="root/username"/>&amp;changeKey=<xsl:value-of select="root/changeKey"/>

        This link is valid for today only.

        <xsl:value-of select="root/site"/>
      </content>
    </request>
  </xsl:template>

</xsl:stylesheet>
