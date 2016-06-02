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
  Create xml containing registration password email details from user/instance details passed
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
<password>3MRaEX</password>
</root>

  -->
  <xsl:template match="/">
    <email>
      <subject>Your registration at
        <xsl:value-of select="/root/site"/>
      </subject>
      <content>
        Dear User,

        Your registration at
        <xsl:value-of select="/root/site"/> was successful.

        Your account is:
        username :
        <xsl:value-of select="/root/request/email"/>
        password :
        <xsl:value-of select="/root/password"/>
        usergroup: GUEST
        usertype : REGISTEREDUSER
        <xsl:if test="/root/request/profile != 'RegisteredUser'">
          You've told us that you want to be "<xsl:value-of select="/root/request/profile"/>", you
          will be contacted by our office soon.
        </xsl:if>
        To log in and access your account, please click on the link below.
        <xsl:value-of select="/root/siteURL"/>

        Thanks for your registration.


        Yours sincerely,
        The team at
        <xsl:value-of select="/root/site"/>
      </content>
    </email>
  </xsl:template>

</xsl:stylesheet>
