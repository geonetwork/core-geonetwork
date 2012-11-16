<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" 
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
	xmlns:gco="http://www.isotc211.org/2005/gco"
	xmlns:srv="http://www.isotc211.org/2005/srv"
	xmlns:gmd="http://www.isotc211.org/2005/gmd"
	xmlns:geonet="http://www.fao.org/geonetwork"
	xmlns:fo="http://www.w3.org/1999/XSL/Format">

	<xsl:include href="modal.xsl"/>
	<xsl:include href="metadata.xsl"/>

	<!--
	page content
	-->
	<xsl:template name="content">
	
		<xsl:call-template name="formLayout">
			<xsl:with-param name="title" select="/root/gui/strings/download"/>
			<xsl:with-param name="content">
				<form id="feedbackf" name="feedbackf" accept-charset="UTF-8" method="post">
					<input type="hidden" name="id" value="{/root/response/id}"/>
					<xsl:for-each select="/root/response/fname">
						<input type="hidden" name="fname" value="{.}"/>
					</xsl:for-each>
					<table align="center">
						<tr class="padded"><td colspan="2">
							<xsl:choose>
							<xsl:when test="(/root/response/metadata/gmd:MD_Metadata or /root/response/metadata/*[@gco:isoType='gmd:MD_Metadata']) and /root/response/metadata/*/gmd:identificationInfo/*/gmd:resourceConstraints">
								<!-- display usage, legal, security constraints -->
								<xsl:for-each select="/root/response/metadata/*/gmd:identificationInfo/*/gmd:resourceConstraints">
									<xsl:apply-templates mode="elementEP" select="*">
										<xsl:with-param name="schema" select="/root/response/metadata/*/geonet:info/schema"/>
									</xsl:apply-templates>
								</xsl:for-each>
							</xsl:when>
							<xsl:otherwise>
								<!-- generic stuff from strings.xml -->
								<h2><xsl:value-of select="/root/gui/strings/copyright1"/></h2>
								<xsl:copy-of select="/root/gui/strings/copyright2"/>
								<p/>
								<xsl:value-of select="/root/gui/strings/copyright3"/>
								<p/>
								<xsl:copy-of select="/root/gui/strings/feedbackTopics"/>
								<h2><xsl:value-of select="/root/gui/strings/disclaimer1"/></h2>
								<xsl:value-of select="/root/gui/strings/disclaimer2"/>
							</xsl:otherwise>
							</xsl:choose>
						</td></tr>
						<tr>
							<th class="padded"><xsl:value-of select="/root/gui/strings/name"/></th>
							<xsl:variable name="username" select="normalize-space(concat(/root/response/name,' ',/root/response/surname))"/>
							<td class="padded"><input class="content" type="text" name="name" size="60" value="{$username}"/></td>
						</tr>
						<tr>
							<th class="padded"><xsl:value-of select="/root/gui/strings/organisation"/></th>
							<td class="padded"><input class="content" type="text" name="org" size="60" value="{/root/response/organisation}"/></td>
						</tr>
						<tr>
							<th class="padded"><xsl:value-of select="/root/gui/strings/downloadEmail"/></th>
							<td class="padded"><input class="content" type="text" name="email" size="60" value="{/root/response/email}"/></td>
						</tr>
						<tr>
							<th class="padded" valign="top"><xsl:value-of select="/root/gui/strings/feedbackReasonForDownload"/></th>
							<td class="padded"><textarea class="content" name="comments" cols="60" rows="4" wrap="soft"></textarea></td>
						</tr>
					</table>
					<div align="center"><b><xsl:value-of select="/root/gui/strings/acceptanceMessage"/></b></div>
					<br/>
				</form>
			</xsl:with-param>
			<xsl:with-param name="buttons">
				<div align="center">
					<button class="content" onclick="goReset('feedbackf')"><xsl:value-of select="/root/gui/strings/reset"/></button>
					&#160;
					<button class="content" onclick="feedbackSubmit()"><xsl:value-of select="/root/gui/strings/accept"/></button>
				</div>
			</xsl:with-param>
		</xsl:call-template>
	</xsl:template>
	
</xsl:stylesheet>
