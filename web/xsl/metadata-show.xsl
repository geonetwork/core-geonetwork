<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:geonet="http://www.fao.org/geonetwork" 
	xmlns:dc = "http://purl.org/dc/elements/1.1/" 
	xmlns:gmd="http://www.isotc211.org/2005/gmd" 
	xmlns:gco="http://www.isotc211.org/2005/gco">

	<!--
	show metadata form
	-->
	
	<xsl:include href="main.xsl"/>
	<xsl:include href="metadata.xsl"/>
	
	<!--
	additional scripts
	-->
	<xsl:template mode="script" match="/">
		<script language="JavaScript1.2" type="text/javascript">
			
			function doAction(action)
			{
				// alert("In doAction(" + action + ")"); // DEBUG
				document.mainForm.action = action;
				goSubmit('mainForm');
			}
			
			function doTabAction(action, tab)
			{
				// alert("In doTabAction(" + action + ", " + tab + ")"); // DEBUG
				document.mainForm.currTab.value = tab;
				doAction(action);
			}
		</script>
	</xsl:template>
	
	<!--
	page content
	-->
	<xsl:template name="content">
		<xsl:param name="schema">
			<xsl:apply-templates mode="schema" select="."/>
		</xsl:param>

		<table  width="100%" height="100%">
			<xsl:for-each select="/root/*[name(.)!='gui' and name(.)!='request']"> <!-- just one -->
				<tr height="100%">
					<td class="blue-content" width="150" valign="top">
						<xsl:call-template name="tab">
							<xsl:with-param name="tabLink" select="concat(/root/gui/locService,'/metadata.show')"/>
						</xsl:call-template>
					</td>
					<td class="content" valign="top">
						<table width="100%">
						
							<xsl:variable name="buttons">
								<tr><td class="padded-content" height="100%" align="center" valign="top">
									<xsl:call-template name="buttons"/>
								</td></tr>
							</xsl:variable>
							<xsl:if test="$buttons!=''">
								<xsl:copy-of select="$buttons"/>
							</xsl:if>
							<tr>
								<td align="center" valign="left" class="padded-content">
									<table width="100%">
										<tr>
											<td align="left" valign="middle" class="padded-content" height="40">
												<xsl:variable name="source" select="string(geonet:info/source)"/>
												<xsl:choose>
													<!-- //FIXME does not point to baseURL yet-->
													<xsl:when test="/root/gui/sources/record[string(siteid)=$source]">
														<a href="{/root/gui/sources/record[string(siteid)=$source]/baseURL}" target="_blank">
															<img src="{/root/gui/url}/images/logos/{$source}.png" width="40"/>
														</a>
													</xsl:when>
													<xsl:otherwise>
														<img src="{/root/gui/url}/images/logos/{$source}.png" width="40"/>
													</xsl:otherwise>
												</xsl:choose>
											</td>
											<xsl:choose>
												<xsl:when test="contains(geonet:info/schema,'dublin-core')">
													<td class="padded" width="90%">
														<h1 align="left">
															<xsl:value-of select="/root/simpledc/dc:title"/>
														</h1>
													</td>
													<td align="right" class="padded-content" height="16" nowrap="nowrap">
														<a href="{/root/gui/locService}/dc.xml?id={geonet:info/id}" target="_blank" title="Download Dublin Core metadata in XML">
															<img src="{/root/gui/url}/images/xml.png" alt="Dublin Core XML" title="Save Dublin Core metadata as XML" border="0"/>
														</a>
													</td>
												</xsl:when>
												<xsl:when test="contains(geonet:info/schema,'fgdc-std')">
													<td class="padded" width="90%">
														<h1 align="left">
															<xsl:value-of select="/root/metadata/idinfo/citation/citeinfo/title"/>
														</h1>
													</td>
													<td align="right" class="padded-content" height="16" nowrap="nowrap">
														<a href="{/root/gui/locService}/fgdc.xml?id={geonet:info/id}" target="_blank" title="Download FGDC metadata in XML">
															<img src="{/root/gui/url}/images/xml.png" alt="FGDC XML" title="Save FGDC metadata as XML" border="0"/>
														</a>
													</td>
												</xsl:when>
												<xsl:when test="contains(geonet:info/schema,'iso19115')">
													<td class="padded" width="90%">
														<h1 align="left">
															<xsl:value-of select="/root/Metadata/dataIdInfo/idCitation/resTitle"/>
														</h1>
													</td>
													<td align="right" class="padded-content" height="16" nowrap="nowrap">
														<a href="{/root/gui/locService}/iso19115to19139.xml?id={geonet:info/id}" target="_blank" title="Save ISO19115/19139 metadata as XML">
															<img src="{/root/gui/url}/images/xml.png" alt="IISO19115/19139 XML" title="Save ISO19115/19139 metadata as XML" border="0"/>
														</a>
														<a href="{/root/gui/locService}/iso_arccatalog8.xml?id={geonet:info/id}" target="_blank" title="Download ISO19115 metadata in XML for ESRI ArcCatalog">
															<img src="{/root/gui/url}/images/ac.png" alt="ISO19115 XML for ArcCatalog" title="Save ISO19115 metadata in XML for ESRI ArcCatalog" border="0"/>
														</a>
													</td>
												</xsl:when>
												<xsl:when test="contains(geonet:info/schema,'iso19139')">
													<td class="padded" width="90%">
														<h1 align="left">
															<xsl:value-of select="/root/gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:title/gco:CharacterString"/>
														</h1>
													</td>
													<td align="right" class="padded-content" height="16" nowrap="nowrap">
														<a href="{/root/gui/locService}/iso19139.xml?id={geonet:info/id}" target="_blank" title="Download ISO19115/19139 metadata in XML">
															<img src="{/root/gui/url}/images/xml.png" alt="ISO19115/19139 XML" title="Save ISO19115/19139 metadata as XML" border="0"/>
														</a>
<!-- //FIXME											<a href="{/root/gui/locService}/iso_arccatalog8.xml?id={geonet:info/id}" target="_blank" title="Download ISO19115 metadata in XML for ESRI ArcCatalog">
															<img src="{/root/gui/url}/images/ac.png" alt="ISO19115 XML for ArcCatalog" title="Save ISO19115 metadata in XML for ESRI ArcCatalog" border="0"/>
	</a> -->
													</td>
												</xsl:when>
											</xsl:choose>
										</tr>
									</table>
								</td>
							</tr>
							<!-- subtemplate title button -->
							<xsl:if test="(string(geonet:info/isTemplate)='s')">
								<tr><td class="padded-content" height="100%" align="center" valign="top">
									<b><xsl:value-of select="geonet:info/title"/></b>
								</td></tr>
							</xsl:if>

							<tr><td class="padded-content">
								<table class="md" width="100%">
									<form name="mainForm" accept-charset="UTF-8" method="POST" action="{/root/gui/locService}/metadata.edit">
										<input type="hidden" name="id" value="{geonet:info/id}"/>
										<input type="hidden" name="currTab" value="{/root/gui/currTab}"/>
										
										<xsl:choose>
											<xsl:when test="$currTab='xml'">
												<xsl:apply-templates mode="xmlDocument" select="."/>
											</xsl:when>
											<xsl:otherwise>
												<xsl:apply-templates mode="elementEP" select="."/>
											</xsl:otherwise>
										</xsl:choose>
										
									</form>
								</table>
							</td></tr>
							
							<xsl:if test="$buttons!=''">
								<xsl:copy-of select="$buttons"/>
							</xsl:if>
							
						</table>
					</td>
				</tr>
			</xsl:for-each>
			<tr><td class="blue-content" colspan="3"/></tr>
		</table>
	</xsl:template>
	
</xsl:stylesheet>
