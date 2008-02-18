<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	
	<!-- TODO: the full stylesheet should be localized. -->
	
	<xsl:template match="/">
		
		<wmcmail>
			<html><p>A GeoNetwork user, <xsl:value-of select="//mailfrom"/> sent you this interative map.</p>
				<p>To see it, click <a href="{//url}" title="Click to view the interactive map" target="_blank">here</a> or on the image below.
				</p>
				<a href="{//url}" title="Click to view the interactive map" target="_blank"><img src="{//imgsrc}"/></a>
				<p>
				If you have problems opening the map, you can open the attached Interactive_map.cml file in the interactive map viewer on the <a href="{//gnurl}" title="GeoNetwork portal">GeoNetwork portal</a></p>
				<br/>
				<i>
				Kind regards,<br/>
				The GeoNetwork Team
				</i>
				<p/>
				<hr/>
				<sub>For more information about this email and about the interactive map file (an OGC Web Map Context file), please read the documentation on <a href="http://geonetwork-opensource.org" alt="GeoNetwork opensource" title="GeoNetwork opensource">GeoNetwork opensource</a></sub>
			</html>			
			
			<text>
A GeoNetwork user, <xsl:value-of select="//mailfrom"/> sent you this email
with a link to an interactive map.

At the bottom of this message, you can find the URL to view the map on the
GeoNetwork portal.

If you have problems opening the map, you can open the attached 
Interactive_map.cml file in the interactive map viewer on the GeoNetwork portal 
<xsl:value-of select="//gnurl"/>
				
	Kind regards,
	The GeoNetwork Team		
	
ps. For more information about this email and about the interactive map 
file (an OGC Web Map Context file), please read the documentation on 
http://geonetwork-opensource.org
	
URL: <xsl:value-of select="//url"/>
	
			</text>
		</wmcmail>
		
	</xsl:template>
</xsl:stylesheet>
