<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:geonet="http://www.fao.org/geonetwork">

	<xsl:include href="main.xsl"/>
	
	<!--
	page content
	-->
	<xsl:template name="content">
		<xsl:call-template name="formLayout">
			<xsl:with-param name="title" select='/root/gui/strings/metadataInsertResults'/>
			<xsl:with-param name="content">
				<xsl:choose>
					<xsl:when test='/root/response/id'>
						<table width="100%">
							<tr>
								<td align="center">
									<xsl:value-of select="concat(/root/gui/strings/metadataAdded,' ',/root/response/id)"/>
									<br/><br/>
								</td>
							</tr>
						</table>
					</xsl:when>
					<xsl:otherwise>
						<xsl:value-of select="concat(/root/gui/strings/metadataRecordsAdded,' ',/root/response/records)"/>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:with-param>
			<xsl:with-param name="buttons">
				<xsl:if test='/root/response/id'>
					<button class="content" onclick="goBack()" id="back"><xsl:value-of select="/root/gui/strings/back"/></button>
					&#160;
					<button class="content" onclick="load('{/root/gui/locService}/metadata.show?id={/root/response/id}')"><xsl:value-of select="/root/gui/strings/show"/></button>
					&#160;
					<button class="content" onclick="load('{/root/gui/locService}/metadata.edit?id={/root/response/id}')"><xsl:value-of select="/root/gui/strings/edit"/></button>
				</xsl:if>
			</xsl:with-param>
		</xsl:call-template>
	</xsl:template>
	
</xsl:stylesheet>

