<?xml version="1.0" encoding="UTF-8"?>
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
