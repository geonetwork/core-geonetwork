<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    
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
    		<subject>"<xsl:value-of select="/root/request/profile"/>" access for <xsl:value-of select="/root/request/email"/> for <xsl:value-of select="/root/site"/></subject>
			<content>Dear Admin,      

Newly registered user <xsl:value-of select="/root/request/email"/> has requested "<xsl:value-of select="/root/request/profile"/>" access for:

Instance:     <xsl:value-of select="/root/site"/>
Url:          <xsl:value-of select="/root/siteURL"/>

User registration details:
  
Name:         <xsl:value-of select="/root/request/name"/>
Surname:      <xsl:value-of select="/root/request/surname"/>
Email:        <xsl:value-of select="/root/request/email"/>
Organisation: <xsl:value-of select="/root/request/org"/>
Type:         <xsl:value-of select="/root/request/kind"/>
Address:      <xsl:value-of select="/root/request/address"/>
State:        <xsl:value-of select="/root/request/state"/>
Post Code:    <xsl:value-of select="/root/request/zip"/>
Country:      <xsl:value-of select="/root/request/country"/>

Please action.

<xsl:value-of select="/root/site"/>
			</content>
    	</email>
    </xsl:template>
    
</xsl:stylesheet>
