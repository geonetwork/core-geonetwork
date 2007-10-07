<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	
	<!-- TODO: the full stylesheet should be localized. -->
	
	<xsl:template match="/">
		
		<wmcmail>
			<html>A Geonetwork user sent you this context, i.e. a set of geographical maps.
				The map set looks like the image below.
				<br/>
				You can click on the map to get to the Geonetwork site and get more details about it. 
				<br/>
				<a href="{//url}"><img src="{//imgsrc}"/></a>
				<br/>
				This is the explicit URL <a href="{//url}"><xsl:value-of select="//url"/></a>
				<br/>
				You can also find as attachment an XML document that describes in a machine readable form the whole context.<br/> 
				Please use it if you experience any problem with the above links, or if you want to view the context on another client.
				<br/>
				<br/>
				<i>
				Kind regards,<br/>
				The Geonetwork Team
				</i>							
			</html>			
			
			<text>
A Geonetwork user sent you this context, i.e. a set of geographical maps.

This is the URL to view it on the Geonetwork portal:
<xsl:value-of select="//url"/>

You can also find as attachment an XML document that describes in a machine readable form the whole context. 
Please use it if you experience any problem with the above link, or if you want to view the context on another client.

	Kind regards,
	The Geonetwork Team												
			</text>
		</wmcmail>
		
	</xsl:template>
</xsl:stylesheet>
