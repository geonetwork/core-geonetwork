<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:geonet="http://www.fao.org/geonetwork">

	<xsl:include href="res.xsl"/>
	
	<!--
	page content
	-->
	<xsl:template name="content">
		<xsl:choose>
			<xsl:when test="string(/root/gui/validation/message)='Metadata Validated successfully'">
				Metadata Validated against XML schema successfully
				<br/><br/>
				<xsl:choose>
					<xsl:when test="/root/response/geonet:schematronerrors/geonet:errorFound">
						<font color="FF0000"><b>BUT Schematron found errors in the Metadata</b></font>
					</xsl:when>
					<xsl:otherwise>
						AND Metadata Validated against Schematron rules successfully.
					</xsl:otherwise>
				</xsl:choose>
				<br/><br/>
				<a href="{/root/gui/url}/schematronCache/SchematronReport{/root/response/id}/schematron-frame.html">Schematron report available here</a>
				<br/>
			</xsl:when>
			<xsl:otherwise>
				<font color="FF0000">Metadata FAILED to validate against XML schema</font>
				<br/><br/>
				<xsl:value-of select="/root/gui/validation/message"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	
	<!--
	title
	-->
	<xsl:template mode="title" match="/">
		<xsl:choose>
			<xsl:when test="/root/response/geonet:schematronerrors/geonet:errorFound">
				Validation Error
			</xsl:when>
			<xsl:otherwise>
				Metadata Validation was successful
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

</xsl:stylesheet>

