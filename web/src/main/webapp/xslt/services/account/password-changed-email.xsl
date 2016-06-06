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
<record>
  <id>2</id>
  <username>JonesC</username>
  <password>b1cbde5bf261d841b3ed0506cb98d6df7cab4b2</password>
  <surname>Jones</surname>
  <name>Craig</name>
  <profile>RegisteredUser</profile>
  <address>6 Hillcrest Rd</address>
  <state>Tas</state>
  <zip>7054</zip>
  <country>au</country>
  <email>Craig.Jones@utas.edu.au</email>
  <organisation>emii</organisation>
  <kind>uni</kind>
</record>
<site>localtrunk</site>
<siteURL>http://127.0.0.1:8122/geonetwork</siteURL>
<adminEmail>Craig.Jones@utas.edu.au</adminEmail>
<password>3MRaEX</password>
</root>

  -->
  <xsl:template match="/">
    <request>
      <subject>
        <xsl:value-of select="root/site"/> password changed for
        <xsl:value-of select="root/record/username"/>
      </subject>
      <to>
        <xsl:value-of select="root/record/email"/>
      </to>
      <content>Your
        <xsl:value-of select="root/site"/> password has been changed.

        If you did not change this password contact the
        <xsl:value-of select="root/site"/> helpdesk

        The
        <xsl:value-of select="root/site"/> team
      </content>
    </request>
  </xsl:template>

</xsl:stylesheet>
