<?xml version="1.0" encoding="UTF-8" ?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
	xmlns:geonet="http://www.fao.org/geonetwork"
	xmlns:xs="http://www.w3.org/2001/XMLSchema" version="2.0">

	<!-- Search for any of the searchStrings provided -->
	<xsl:function name="geonet:contains-any-of" as="xs:boolean">
		<xsl:param name="arg" as="xs:string?"/> 
		<xsl:param name="searchStrings" as="xs:string*"/> 
		
		<xsl:sequence select=" 
			some $searchString in $searchStrings
			satisfies contains($arg,$searchString)
			"/>
	</xsl:function>
</xsl:stylesheet>
