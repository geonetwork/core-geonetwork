<?xml version="1.0"?>
 
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:geonet="http://www.fao.org/geonetwork"
	xmlns:exslt = "http://exslt.org/common">
<xsl:output method="xml"/>

<xsl:include href="../../xsl/utils.xsl"/>
<xsl:include href="../../xsl/metadata.xsl"/>

<xsl:template match="/">
		
	<xsl:variable name="md">
		<xsl:apply-templates mode="brief" select="*"/>
	</xsl:variable>
	<xsl:variable name="metadata" select="exslt:node-set($md)/*[1]"/>
	
	<!--
	S: includes the following elements:
	Title (title),
	Online Linkage (onlink),
	Bounding Coordinates (bounding),
	Extent (extent),
	Publication Date (pubdate),
	Beginning Date (begtime),
	Ending Date (enddate),
	Browse Graphic (browse),
	Entity Type Label (enttypl),
	Attribute Label (attrlabl),
	and Data Set G-Polygon (dsgpoly).
	The Browse Graphic (browse) should appear as groups of
	Browse Graphic File Name (browsen),
	Browse Graphic File Description (browsed),
	and Browse Graphic File Type (browset).
	-->
	<metadata>
	  <idinfo>
		<citation>
		  <citeinfo>
			<title><xsl:value-of select="$metadata/title"/></title>
			<pubdate><xsl:value-of select="$metadata/geonet:info/createDate"/></pubdate>
			<xsl:for-each select="$metadata/link[@type='url']">
				<onlink><xsl:value-of select="$metadata/link"/></onlink>
			</xsl:for-each>	
		  </citeinfo>
		</citation>
		<descript>
		  <abstract><xsl:value-of select="$metadata/abstract"/></abstract> <!-- not mandatory for summary format -->
		</descript>
		<timeperd>
		  <timeinfo>
			<rngdates>
			  <begdate/>
			  <enddate/>
			</rngdates>
		  </timeinfo>
		  <current/>
		</timeperd>
		<spdom>
		  <bounding>
			<westbc><xsl:value-of select="$metadata/geoBox/westBL"/></westbc>
			<eastbc><xsl:value-of select="$metadata/geoBox/eastBL"/></eastbc>
			<northbc><xsl:value-of select="$metadata/geoBox/northBL"/></northbc>
			<southbc><xsl:value-of select="$metadata/geoBox/southBL"/></southbc>
		  </bounding>
		</spdom>
		<!--
		<xsl:for-each select="$metadata/image">
			<browse>
				<browsen><xsl:value-of select="$metadata/image"/></browsen>
				<browsed><xsl:value-of select="$metadata/image/@type"/></browsed>
				<browset></browset>
			</browse>
		</xsl:for-each>
		-->
	  </idinfo>
	</metadata>

</xsl:template>

</xsl:stylesheet>
