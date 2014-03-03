<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    
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
    		<subject><xsl:value-of select="root/site"/> password changed for <xsl:value-of select="root/record/username"/></subject>
    		<to><xsl:value-of select="root/record/email"/></to>
			<content>Your <xsl:value-of select="root/site"/> password has been changed.
			
If you did not change this password contact the <xsl:value-of select="root/site"/> helpdesk

The <xsl:value-of select="root/site"/> team
			</content>
    	</request>
    </xsl:template>
    
</xsl:stylesheet>
