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
  Create xml containing profile request email details from user/instance details passed
  Allows email to be customised without changing java service - info supplied is as follows

<root>
<site>localtrunk</site>
<siteURL>http://127.0.0.1:8122/geonetwork</siteURL>
<request>
  <zip>7001</zip>
  <address>University of Tasmania, Hobart</address>
  <email>craig.jones@utas.edu.au</email>
  <name>Craig</name>
  <state>Tas</state>
  <surname>Jones</surname>
  <org>emii</org>
  <kind>uni</kind>
  <profile>RegisteredUser</profile>
  <country>au</country>
</request>
</root>

  -->
  <xsl:template match="/">
    <email>
      <subject>"<xsl:value-of select="/root/request/profile"/>" access for
        <xsl:value-of select="/root/request/email"/> for
        <xsl:value-of select="/root/site"/>
      </subject>
      <content>Dear Admin,

        Newly registered user
        <xsl:value-of select="/root/request/email"/> has requested "<xsl:value-of
          select="/root/request/profile"/>" access for:

        Instance:
        <xsl:value-of select="/root/site"/>
        Url:
        <xsl:value-of select="/root/siteURL"/>

        User registration details:

        Name:
        <xsl:value-of select="/root/request/name"/>
        Surname:
        <xsl:value-of select="/root/request/surname"/>
        Email:
        <xsl:value-of select="/root/request/email"/>
        Organisation:
        <xsl:value-of select="/root/request/org"/>
        Type:
        <xsl:value-of select="/root/request/kind"/>
        Address:
        <xsl:value-of select="/root/request/address"/>
        State:
        <xsl:value-of select="/root/request/state"/>
        Post Code:
        <xsl:value-of select="/root/request/zip"/>
        Country:
        <xsl:value-of select="/root/request/country"/>

        Please action.

        <xsl:value-of select="/root/site"/>
      </content>
    </email>
  </xsl:template>

</xsl:stylesheet>
