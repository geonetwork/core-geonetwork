<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    
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
    		<subject><xsl:value-of select="root/site"/> password change link for <xsl:value-of select="root/username"/></subject>
    		<to><xsl:value-of select="root/email"/></to>
			<content>You have requested to change your <xsl:value-of select="root/site"/> password.
			
You can change your password using the following link:

<xsl:value-of select="root/siteURL"/>/srv/eng/new.password?username=<xsl:value-of select="root/username"/>&amp;changeKey=<xsl:value-of select="root/changeKey"/>

This link is valid for today only.

<xsl:value-of select="root/site"/>
			</content>
    	</request>
    </xsl:template>
    
</xsl:stylesheet>
