<?xml version="1.0" encoding="UTF-8" ?>
<xsl:stylesheet 	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
						xmlns:geonet="http://www.fao.org/geonetwork" 
						xmlns:exslt= "http://exslt.org/common"
						xmlns:csw="http://www.opengis.net/cat/csw/2.0.2"
						exclude-result-prefixes="xsl exslt geonet">
	<!-- 
		CSV search results export.
		
		Default formatting will be column header + all tags.
		Sort order is schema based due to formatting which 
		could be different according to schema.
		
		In order to override default formatting, create a template
		with mode="csv" in the metadata-schema.xsl matching the root
		element in order to create a one level tree structure :

		Example to export only title from ISO19139 records.		
		<pre>
				<xsl:template match="gmd:MD_Metadata" mode="csv">
					<xsl:param name="internalSep"/>
					
					<metadata>
						<xsl:copy-of select="geonet:info"/>
						
						<xsl:copy-of select="gmd:identificationInfo/gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:title"/>
						...
					</metadata>
				</xsl:template>
					
		</pre>
		
		The internal separator is used to join multiple objects in one
		columns (eg. keywords will be keyword1###keyword2... and could be explode 
		if needed in a spreadsheet).
	 -->
	<xsl:output method="text" version="1.0" encoding="utf-8" indent="no"/>	
	
	<!-- Field separator 
		To use tab instead of semicolon, use "&#009;".
	-->
	<xsl:variable name="sep" select="'&#009;'"/>
	
	
	<!-- Intra field separator -->
	<xsl:variable name="internalSep" select="'###'"/>
	
	<xsl:include href="utils.xsl"/>
	<xsl:include href="metadata.xsl"/>

	<!-- A template to add a new line \n with no extra space. -->	
	<xsl:template name="newLine">
<xsl:text>
</xsl:text>		
	</xsl:template>


	<xsl:template name="content" match="/">

		<!-- Sort results first as csv output could be different from one schema to another. 
		Create the sorted set based on a CSW response or a classic search.
		-->
		<xsl:variable name="sortedResults">
			<xsl:for-each select="/root/csw:GetRecordsResponse/csw:SearchResults/*|
						/root/response/*[name(.)!='summary']">
				<xsl:sort select="geonet:info/schema" order="descending"/>
				<xsl:copy-of select="."/>
			</xsl:for-each>
		</xsl:variable>
		
		
		<!-- Display results
				* header first (once)
				* content then.
		-->
		<xsl:for-each select="exslt:node-set($sortedResults)/*">
			
			<!-- Try to apply csv mode template to current metadata record -->          
			<xsl:variable name="mdcsv">
				<xsl:apply-templates mode="csv" select=".">
					<xsl:with-param name="internalSep" select="$internalSep"/>
				</xsl:apply-templates>
			</xsl:variable>
			
			<!-- If not define just use the brief format -->
			<xsl:variable name="md">
				<xsl:choose>
					<xsl:when test=". != $mdcsv">
						<xsl:copy-of select="$mdcsv"/>
					</xsl:when>
					<xsl:otherwise>
						<xsl:apply-templates mode="brief" select="."/>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:variable>
			
			<xsl:variable name="metadata" select="exslt:node-set($md)/*[1]"/>
			<xsl:choose>
				<xsl:when test="position()!=1 and geonet:info/schema=preceding-sibling::node()/geonet:info/schema"/>
				<xsl:otherwise>
					<xsl:call-template name="csvHeader">
						<xsl:with-param name="metadata" select="$metadata"/>
					</xsl:call-template>
				</xsl:otherwise>
			</xsl:choose>
			
			<xsl:call-template name="csvLine">
				<xsl:with-param name="metadata" select="$metadata"/>
			</xsl:call-template>
		</xsl:for-each>
		
	</xsl:template>
	
	<!-- ============================================================== -->
	<!-- Create header using first row corresponding to current schema -->
	<xsl:template name="csvHeader">
		<xsl:param name="metadata"/>
		<xsl:value-of select="concat('schema', $sep, 'id', $sep)"/>
		
		<xsl:for-each select="$metadata/*[name(.)!='geonet:info']">
			<xsl:choose>
				<xsl:when test="name(.) = name(following-sibling::node())">
				</xsl:when>
				<xsl:otherwise>
					<xsl:value-of select="normalize-space(name(.))"/>
					<xsl:value-of select="$sep"/>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:for-each>
		
		<xsl:call-template name="newLine"/>
	</xsl:template>
	
	<!-- ============================================================== -->
	<!-- Dump line -->
	<xsl:template name="csvLine">
		<xsl:param name="metadata"/>

		<xsl:value-of select="concat($metadata/geonet:info/schema, $sep, $metadata/geonet:info/id, $sep)"/>

		<xsl:for-each select="$metadata/*[name(.)!='geonet:info']">
			<xsl:value-of select="normalize-space(.)"/>
			<xsl:choose>
				<xsl:when test="name(.) = name(following-sibling::node())">
					<xsl:value-of select="$internalSep"/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:value-of select="$sep"/>
				</xsl:otherwise>
			</xsl:choose>
			
		</xsl:for-each>

		<xsl:call-template name="newLine"/>
	</xsl:template>

</xsl:stylesheet>
