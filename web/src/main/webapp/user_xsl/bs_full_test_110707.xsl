<?xml version="1.0" encoding="UTF-8"?>
<!-- XSL-Styleshett für geocat.ch ( 7.Juli 2011 )-->
<!-- Dieses XSL-Styleshett wurde für die Metadatenverwaltung des Kantons Basel-Stadt durch die Fachstelle für Geoinformationen erstellt und gepflegt. -->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0" xmlns:gmd="http://www.isotc211.org/2005/gmd" xmlns:gco="http://www.isotc211.org/2005/gco" xmlns:gmx="http://www.isotc211.org/2005/gmx" xmlns:srv="http://www.isotc211.org/2005/srv" xmlns:gml="http://www.opengis.net/gml" xmlns:gts="http://www.isotc211.org/2005/gts" xmlns:che="http://www.geocat.ch/2008/che" xmlns:geonet="http://www.fao.org/geonetwork" xmlns:exslt="http://exslt.org/common" xmlns:xlink="http://www.w3.org/1999/xlink">
	<!-- Load labels. -->
	<xsl:variable name="label" select="document('../xml/schemas/iso19139/loc/deu/labels.xml')"/>
	<xsl:variable name="label.che" select="document('../xml/schemas/iso19139.che/loc/deu/labels.xml')"/>
	<xsl:variable name="value" select="document('../xml/schemas/iso19139/loc/deu/codelists.xml')"/>
	<xsl:variable name="value.che" select="document('../xml/schemas/iso19139.che/loc/deu/codelists.xml' )"/>
	<xsl:template match="/">
		<!-- Der Slash (/) bedeutet dass das Template für alle elemente verwendet wird. -->
		<html>
			<head>
				<meta http-equiv="expires" content="0"/>
				<!-- Ist nötig, damit die attribute des Ausdehungs iframes nicht im cache gespeichert werden -->
				<title>GeoPortal Basel-Stadt - Metadatenkatalog</title>
			</head>
			<body leftmargin="0" topmargin="0" bgcolor="#ffffff" marginheight="0" marginwidth="0">
				<xsl:apply-templates/>
			</body>
		</html>
	</xsl:template>
	<!-- Hier wird das Layout Gerüst für Kanton BS, also Banner mit Logo etc. angegeben -->
	<xsl:template match="che:CHE_MD_Metadata">
		<table width="692" border="0" cellpadding="0" cellspacing="0" height="100%">
			<tbody>
				<tr>
					<td height="45" valign="top">
						<table width="662" border="0" cellpadding="0" cellspacing="0" height="45">
							<tbody>
								<tr align="left" valign="top">
									<td width="2"/>
									<td width="133" bgcolor="#ffffff">
										<a href="http://www.bs.ch" target="_blank">
											<img border="0">
												<xsl:attribute name="src">http://www.geo.bs.ch/pics_neu/logo_baselstadt.gif</xsl:attribute>
											</img>
										</a>
									</td>
									<td class="cnavunter" width="240" bgcolor="#ffffff">
										<a href="http://www.geo.bs.ch/index.cfm" target="_blank">
											<img border="0">
												<xsl:attribute name="src">http://www.geo.bs.ch/pics/logo_geoportal.gif</xsl:attribute>
											</img>
										</a>
									</td>
									<td width="287" valign="bottom" align="center">
										<a href="javascript:window.close()" style="font-family: Verdana, Arial, Helvetica, sans-serif; font-size: 11px; color: #CC9933; font-style: normal; text-decoration: none; line-height: 14px;">Fenster schliessen</a>
									</td>
								</tr>
							</tbody>
						</table>
					</td>
				</tr>
				<tr>
					<td height="47" valign="top">
						<table width="692" border="0" cellpadding="0" cellspacing="0" height="47">
							<tbody>
								<tr align="left" valign="top">
									<td width="692" bgcolor="#fdb369" height="46">
										<table width="692" border="0" cellpadding="0" cellspacing="0">
											<tbody>
												<tr>
													<td colspan="2" height="12"/>
												</tr>
												<tr>
													<td width="45" height="26"> </td>
<!-- Das Textende muss je nach Version angepasst werden: Komplett / Erweitert / Einfach -->													
													<td valign="top" height="26" align="left" width="647" style="font-family:Verdana, Arial, Helvetica, sans-serif; font-size: 20px;font-style:normal; font-weight:normal; color:#666666; text-decoration:none; line-height:26px">Metadatenkatalog - Komplett</td> 		
												</tr>
											</tbody>
										</table>
									</td>
								</tr>
								<tr>
									<td colspan="3" bgcolor="#ffffff" height="1"/>
								</tr>
							</tbody>
						</table>
					</td>
				</tr>
				<tr>
					<td valign="top">
						<table width="692" border="0" cellpadding="0" cellspacing="0" height="100%">
							<tbody>
								<tr>
									<td width="692" align="left" bgcolor="#f1f1f1" height="504" valign="top">
										<table class="fliess" width="692" border="0" cellpadding="0" cellspacing="0">
											<tbody>
												<tr>
													<td width="45" valign="top"> </td>
													<td class="text" width="*" align="left" valign="top">
														<br/>
														<table width="490" border="0" cellpadding="0" cellspacing="0" style="font-family: Verdana, Arial, Helvetica, sans-serif;font-size: 12px; font-style: normal; color: #000000; text-decoration: none; line-height: 17px;">
															<!-- Change border to 1 here -->
															<tbody>
																<tr>
																	<td colspan="2" class="textl">
																		<b>Details zum ausgewählten digitalen Datensatz</b>
																	</td>
																</tr>
																<tr>
																	<td valign="middle" class="text" height="50"><a style="font-family: Verdana, Arial, Helvetica, sans-serif; font-size: 11px; color: #CC9933; font-style: normal; text-decoration: none; line-height: 14px;" href="javascript:window.print();">Drucken</a></td>
																	<td align= "right" valign="middle" height="50" style="font-family: Verdana, Arial, Helvetica, sans-serif; font-size: 11px; color: #CC9933; font-style: normal; text-decoration: none; line-height: 14px;">
																		Darstellung:
<!-- Das Links & Texte müssen je nach Version angepasst werden: Komplett / Erweitert / Einfach -->	
																		<a target="_self">																		
																			<xsl:attribute name="href">
																				<xsl:text>http://tc-geocat0i.bgdi.admin.ch/geonetwork/srv/deu/metadata.formatter.html?xsl=bs_simple_test_110707&amp;uuid=</xsl:text>
																				<xsl:value-of select="gmd:fileIdentifier/gco:CharacterString"/>
																			</xsl:attribute>
																			Einfach</a>
																			 / 
																		<a target="_self">																		
																			<xsl:attribute name="href">
																				<xsl:text>http://tc-geocat0i.bgdi.admin.ch/geonetwork/srv/deu/metadata.formatter.html?xsl=bs_extended_test_110707&amp;uuid=</xsl:text>
																				<xsl:value-of select="gmd:fileIdentifier/gco:CharacterString"/>
																			</xsl:attribute>
																			Erweitert</a>
																	</td>
																</tr>
																<tr>
																	<td colspan="2">
																		<xsl:call-template name="content"/>
																		<!--Verweist auf das Template mit dem Namen content-->
																	</td>
																</tr>
																<tr>
																	<td colspan="2" bgcolor="#f1f1f1">
																		<br/>
																	</td>
																</tr>
															</tbody>
														</table>
													</td>
													<td width="27" align="left" valign="top"> </td>
													<td width="30" align="left" valign="top"/>
												</tr>
											</tbody>
										</table>
									</td>
								</tr>
							</tbody>
						</table>
					</td>
				</tr>
			</tbody>
		</table>
	</xsl:template>
	<!-- 	
====================================================== 
	CONTENT 
======================================================
-->
	<!-- Inhalt den wir aus der XML Datei herausholen -->
	<xsl:template name="content">
		<table border="0" cellpadding="4" cellspacing="2" style="font-family: Verdana, Arial, Helvetica, sans-serif;font-size: 12px; font-style: normal; color: #000000; text-decoration: none">
<!-- Titel - simple-->		
			<tr>
				<td colspan="2" class="text" bgcolor="silver">
					<br/>
					<span style="font-size: 18px;font-weight:bold">
						<!-- hier kann direkt gmd:ident.. angegeben werden ohne // weil im template vorhin schon che:CHE_MD_Metadata selektiert wurde und der pfad nun dort weitergeht. -->
						<!-- <xsl:value-of select="//gmd:title"/> 110209 Vollstaendiger Pfad angegeben-->
						<xsl:value-of select="gmd:identificationInfo/che:CHE_MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:title/gco:CharacterString"/>
					</span>
				</td>
			</tr>
<!--Beispielbild - simple-->	
			<tr>
				<td colspan="2" class="text" bgcolor="#ffffff" valign="TOP">
					<!-- Beispielbild das bei uns liegt. Die Adresse des Bildes ist im XML-Document abgespeichert. -->
					<img src="{gmd:identificationInfo/che:CHE_MD_DataIdentification/gmd:graphicOverview/gmd:MD_BrowseGraphic/gmd:fileName/gco:CharacterString}" border="0">
						<xsl:attribute name="alt"><xsl:value-of select="gmd:identificationInfo/che:CHE_MD_DataIdentification/gmd:graphicOverview/gmd:MD_BrowseGraphic/gmd:fileName/gco:CharacterString"/></xsl:attribute>
						<xsl:attribute name="title"><xsl:value-of select="gmd:identificationInfo/che:CHE_MD_DataIdentification/gmd:graphicOverview/gmd:MD_BrowseGraphic/gmd:fileName/gco:CharacterString"/></xsl:attribute>
					</img>
					<!-- Beispielbild das via upload bei geocat gespeichert ist. -->
					<!--<img src="http://geocat0i.bgdi.admin.ch/geonetwork/srv/deu/resources.get?access=public&amp;id={geonet:info/id}&amp;fname={gmd:identificationInfo/che:CHE_MD_DataIdentification/gmd:graphicOverview[2]/gmd:MD_BrowseGraphic/gmd:fileName/gco:CharacterString}" border="0"/>-->
					<!--	<xsl:element name="img">
								<xsl:attribute name="src">
									<xsl:value-of select="string('resources.get?access=public&amp;id=')" />
									<xsl:value-of select="geonet:info/id" />
									<xsl:value-of select="string('&amp;fname=')" />
									<xsl:value-of select="gmd:identificationInfo/che:CHE_MD_DataIdentification/gmd:graphicOverview[2]/gmd:MD_BrowseGraphic/gmd:fileName/gco:CharacterString" />
								</xsl:attribute>
								<xsl:attribute name="alt">
									<xsl:value-of select="gmd:identificationInfo/che:CHE_MD_DataIdentification/gmd:graphicOverview[2]/gmd:MD_BrowseGraphic/gmd:fileName/gco:CharacterString"/>
								</xsl:attribute>
								<xsl:attribute name="title">
									<xsl:value-of select="gmd:identificationInfo/che:CHE_MD_DataIdentification/gmd:graphicOverview[2]/gmd:MD_BrowseGraphic/gmd:fileName/gco:CharacterString"/>
								</xsl:attribute>
							</xsl:element>-->
				</td>
			</tr>
			<tr>
				<td colspan="2" class="text" bgcolor="#f1f1f1" valign="TOP">
					<xsl:text disable-output-escaping="no">&#160;</xsl:text>
				</td>
			</tr>
<!-- Kurzbeschreibung - simple-->
			<tr>
				<td colspan="2" class="text" bgcolor="silver" valign="TOP">
					<b>
						<xsl:value-of select="string($label/labels/element[@name='gmd:abstract']/label)"/>
					</b>
				</td>
			</tr>
			<tr>
				<td colspan="2" class="text" bgcolor="#ffffff" valign="TOP">
					<xsl:value-of select="gmd:identificationInfo/che:CHE_MD_DataIdentification/gmd:abstract/gco:CharacterString"/>
					<br/><br/>
				</td>
			</tr>
<!-- Zweck - full-->
			<tr>
				<td colspan="2" class="text" bgcolor="silver" valign="TOP">
					<b>
						<xsl:value-of select="string($label/labels/element[@name='gmd:purpose']/label)"/>
					</b>
				</td>
			</tr>
			<tr>
				<td colspan="2" class="text" bgcolor="#ffffff" valign="TOP">
					<xsl:value-of select="gmd:identificationInfo/che:CHE_MD_DataIdentification/gmd:purpose/gco:CharacterString"/>
					<br/><br/>
				</td>
			</tr>
<!-- Zusatzinformationen - full-->
			<tr>
				<td colspan="2" class="text" bgcolor="silver" valign="TOP">
					<b>
						<xsl:value-of select="string($label/labels/element[@name='gmd:supplementalInformation']/label)"/>
					</b>
				</td>
			</tr>
			<tr>
				<td colspan="2" class="text" bgcolor="#ffffff" valign="TOP">
					<xsl:value-of select="gmd:identificationInfo/che:CHE_MD_DataIdentification/gmd:supplementalInformation/gco:CharacterString"/>
					<br/><br/>
				</td>
			</tr>
			<tr>
				<td colspan="2" class="text" bgcolor="#f1f1f1" valign="TOP">
					<xsl:text disable-output-escaping="no">&#160;</xsl:text>
				</td>
			</tr>
<!-- Bearbeitungsstatus - extend-->
			<tr>
				<td class="text" width="30%" bgcolor="silver" valign="TOP">
					<b>
						<xsl:value-of select="string($label/labels/element[@name='gmd:status']/label)"/>
					</b>
				</td>
				<td bgcolor="#ffffff" valign="TOP">
					<xsl:call-template name="codelist">
						<xsl:with-param name="code" select="gmd:identificationInfo/che:CHE_MD_DataIdentification/gmd:status/gmd:MD_ProgressCode/@codeListValue"/>
						<xsl:with-param name="path" select="string('gmd:MD_ProgressCode')"/>
					</xsl:call-template>
				</td>
			</tr>
<!-- Datum "Erstellung" - extend-->
			<tr>
				<td width="30%" bgcolor="silver" valign="TOP">
					<b>
						<xsl:text disable-output-escaping="no">Datum&#160;</xsl:text>
						<xsl:call-template name="codelist">
						<xsl:with-param name="code" select="gmd:identificationInfo/che:CHE_MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:date/gmd:CI_Date/gmd:dateType/gmd:CI_DateTypeCode/@codeListValue"/>
						<xsl:with-param name="path" select="string('gmd:CI_DateTypeCode')"/>
					</xsl:call-template>
					</b>
				</td>
				<td bgcolor="#ffffff" valign="TOP">
					<xsl:value-of select="gmd:identificationInfo/che:CHE_MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:date/gmd:CI_Date/gmd:date/gco:Date"/>
				</td>
			</tr>
<!-- Datum "Aktualisierung" - extend-->
			<tr>
				<td width="30%" bgcolor="silver" valign="TOP">
					<b> <!-- Bis jetzt keine Label-Link bekannt -->
						<xsl:text disable-output-escaping="no">Aktualisierungsdatum</xsl:text>
					</b>
				</td>
				<td bgcolor="#ffffff" valign="TOP">
					<xsl:value-of select="gmd:identificationInfo/che:CHE_MD_DataIdentification/che:revision/che:CHE_MD_Revision/che:dateOfLastUpdate/gco:Date"/>

				</td>
			</tr>
			<tr>
				<td colspan="2" class="text" bgcolor="#f1f1f1" valign="TOP">
					<xsl:text disable-output-escaping="no">&#160;</xsl:text>
				</td>
			</tr>
<!-- Ausdehung - simple-->
			<tr>
				<td colspan="2" class="text" bgcolor="silver" valign="TOP">
					<b>
						<xsl:value-of select="string($label/labels/element[@name='gmd:extent']/label)"/>
					</b>
				</td>
			</tr>
			<tr>
				<td colspan="2" class="text" bgcolor="#ffffff">
					<!-- Darstellung des Stadtplans grau mit dem thema Nr. 16 des Radonpotenitals weil da Kantonsgrenzen ersichtlich -->
						<iframe  frameborder="no" scrolling="no" width="500" height="400" style="border-width: 0px; border-style: solid; margin:0; padding:0;">
							<xsl:attribute name="src">
								<xsl:text>http://www.stadtplan.bs.ch/geoviewer/index.php?instance=mashup_simple&amp;wgs84=</xsl:text>
								<xsl:value-of select="gmd:identificationInfo/che:CHE_MD_DataIdentification/gmd:extent/gmd:EX_Extent/gmd:geographicElement/gmd:EX_BoundingPolygon/gmd:polygon/gml:MultiSurface/gml:surfaceMember/gml:Polygon/gml:exterior/gml:LinearRing/gml:posList"/>
							</xsl:attribute>
						<p>Ihr Browser erfüllt die notwendigen Mindestanforderungen für diese Appikation nicht.</p>
					</iframe>
					<br/>
				</td>
			</tr>
			<tr>
				<td colspan="2" class="text" bgcolor="#f1f1f1" valign="TOP">
					<xsl:text disable-output-escaping="no">&#160;</xsl:text>
				</td>
			</tr>
<!-- Kontakt für die Ressource (Titel) -->
			<tr>
				<td colspan="2" class="text" bgcolor="silver" valign="TOP">
					<b>
						<xsl:value-of select="string($label/labels/element[@name='gmd:pointOfContact']/label)"/>
					</b>
				</td>
			</tr>
<!-- Adresse Eigentuemer - extend-->
			<tr>
				<td colspan="2" class="text" bgcolor="#ffffff" valign="TOP">
					<b>
						<xsl:call-template name="codelist">
							<xsl:with-param name="code" select="gmd:identificationInfo/che:CHE_MD_DataIdentification/gmd:pointOfContact/che:CHE_CI_ResponsibleParty/gmd:role/gmd:CI_RoleCode/@codeListValue"/>
							<xsl:with-param name="path" select="string('gmd:CI_RoleCode')"/>
						</xsl:call-template>
					</b>
					<br/>
					<xsl:value-of select="gmd:identificationInfo/che:CHE_MD_DataIdentification/gmd:pointOfContact/che:CHE_CI_ResponsibleParty/gmd:organisationName/gmd:PT_FreeText/gmd:textGroup/gmd:LocalisedCharacterString"/>
					<br/>
					<xsl:value-of select="gmd:identificationInfo/che:CHE_MD_DataIdentification/gmd:pointOfContact/che:CHE_CI_ResponsibleParty/gmd:positionName/gmd:PT_FreeText/gmd:textGroup/gmd:LocalisedCharacterString"/>
					<br/>
					<xsl:value-of select="gmd:identificationInfo/che:CHE_MD_DataIdentification/gmd:pointOfContact/che:CHE_CI_ResponsibleParty/che:individualFirstName/gco:CharacterString"/>
					<br/>
					<xsl:value-of select="gmd:identificationInfo/che:CHE_MD_DataIdentification/gmd:pointOfContact/che:CHE_CI_ResponsibleParty/che:individualLastName/gco:CharacterString"/>
					<br/>
					<xsl:value-of select="gmd:identificationInfo/che:CHE_MD_DataIdentification/gmd:pointOfContact/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:address/che:CHE_CI_Address/che:streetName/gco:CharacterString"/>
					<xsl:text disable-output-escaping="no">&#160;</xsl:text>
					<xsl:value-of select="gmd:identificationInfo/che:CHE_MD_DataIdentification/gmd:pointOfContact/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:address/che:CHE_CI_Address/che:streetNumber/gco:CharacterString"/>
					<br/>
					<xsl:value-of select="gmd:identificationInfo/che:CHE_MD_DataIdentification/gmd:pointOfContact/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:address/che:CHE_CI_Address/che:postBox/gco:CharacterString"/>
					<br/>
					<xsl:value-of select="gmd:identificationInfo/che:CHE_MD_DataIdentification/gmd:pointOfContact/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:address/che:CHE_CI_Address/gmd:country/gco:CharacterString"/>
					<xsl:text disable-output-escaping="no">&#160;&#45;&#160;</xsl:text>
					<xsl:value-of select="gmd:identificationInfo/che:CHE_MD_DataIdentification/gmd:pointOfContact/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:address/che:CHE_CI_Address/gmd:postalCode/gco:CharacterString"/>
					<xsl:text disable-output-escaping="no">&#160;</xsl:text>
					<xsl:value-of select="gmd:identificationInfo/che:CHE_MD_DataIdentification/gmd:pointOfContact/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:address/che:CHE_CI_Address/gmd:city/gco:CharacterString"/>
					<br/>
					<xsl:text disable-output-escaping="no">Tel&#160;</xsl:text>
					<xsl:value-of select="gmd:identificationInfo/che:CHE_MD_DataIdentification/gmd:pointOfContact/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:phone/che:CHE_CI_Telephone/gmd:voice/gco:CharacterString"/>
					<br/>
					<xsl:text disable-output-escaping="no">Fax&#160;</xsl:text>
					<xsl:value-of select="gmd:identificationInfo/che:CHE_MD_DataIdentification/gmd:pointOfContact/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:phone/che:CHE_CI_Telephone/gmd:facsimile/gco:CharacterString"/>
					<br/>
					<a href="mailto:{gmd:identificationInfo/che:CHE_MD_DataIdentification/gmd:pointOfContact/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:address/che:CHE_CI_Address/gmd:electronicMailAddress/gco:CharacterString}">
						<xsl:value-of select="gmd:identificationInfo/che:CHE_MD_DataIdentification/gmd:pointOfContact/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:address/che:CHE_CI_Address/gmd:electronicMailAddress/gco:CharacterString"/>
					</a>
					<br/>
					<a href="{gmd:identificationInfo/che:CHE_MD_DataIdentification/gmd:pointOfContact/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:onlineResource/gmd:CI_OnlineResource/gmd:linkage/che:PT_FreeURL/che:URLGroup/che:LocalisedURL/text()}" target="_new">
						<xsl:value-of select="gmd:identificationInfo/che:CHE_MD_DataIdentification/gmd:pointOfContact/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:onlineResource/gmd:CI_OnlineResource/gmd:linkage/che:PT_FreeURL/che:URLGroup/che:LocalisedURL/text()"/>
					</a>
					<br/>
					<xsl:value-of select="gmd:identificationInfo/che:CHE_MD_DataIdentification/gmd:pointOfContact/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:hoursOfService/gco:CharacterString"/>
					<br/><br/>
				</td>
			</tr>
			<tr>
				<td colspan="2" class="text" bgcolor="#f1f1f1" valign="TOP">
					<xsl:text disable-output-escaping="no">&#160;</xsl:text>
				</td>
			</tr>
<!-- Basisinformation - full-->
			<tr>
				<td colspan="2" class="text" bgcolor="silver" valign="TOP">
					<b>
						<xsl:value-of select="string($label/labels/element[@name='gmd:identificationInfo']/label)"/>
					</b>
				</td>
			</tr>
	<!-- Adresse Autor - full-->
			<tr>
				<td class="text" width="30%" bgcolor="silver" valign="TOP">
					<b>
						<xsl:call-template name="codelist">
						<xsl:with-param name="code" select="gmd:identificationInfo/che:CHE_MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:citedResponsibleParty/che:CHE_CI_ResponsibleParty/gmd:role/gmd:CI_RoleCode/@codeListValue"/>
						<xsl:with-param name="path" select="string('gmd:CI_RoleCode')"/>
						</xsl:call-template>
					</b>
				</td>
				<td bgcolor="#ffffff" valign="TOP">
					<xsl:value-of select="gmd:identificationInfo/che:CHE_MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:citedResponsibleParty/che:CHE_CI_ResponsibleParty/gmd:organisationName/gmd:PT_FreeText/gmd:textGroup/gmd:LocalisedCharacterString"/>
					<br/>
					<xsl:value-of select="gmd:identificationInfo/che:CHE_MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:citedResponsibleParty/che:CHE_CI_ResponsibleParty/gmd:positionName/gmd:PT_FreeText/gmd:textGroup/gmd:LocalisedCharacterString"/>
					<br/>
					<xsl:value-of select="gmd:identificationInfo/che:CHE_MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:citedResponsibleParty/che:CHE_CI_ResponsibleParty/che:individualFirstName/gco:CharacterString"/>
					<br/>
					<xsl:value-of select="gmd:identificationInfo/che:CHE_MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:citedResponsibleParty/che:CHE_CI_ResponsibleParty/che:individualLastName/gco:CharacterString"/>
					<br/>
					<xsl:value-of select="gmd:identificationInfo/che:CHE_MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:citedResponsibleParty/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:address/che:CHE_CI_Address/che:streetName/gco:CharacterString"/>
					<xsl:text disable-output-escaping="no">&#160;</xsl:text>
					<xsl:value-of select="gmd:identificationInfo/che:CHE_MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:citedResponsibleParty/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:address/che:CHE_CI_Address/che:streetNumber/gco:CharacterString"/>
					<br/>
					<xsl:value-of select="gmd:identificationInfo/che:CHE_MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:citedResponsibleParty/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:address/che:CHE_CI_Address/che:postBox/gco:CharacterString"/>
					<br/>
					<xsl:value-of select="gmd:identificationInfo/che:CHE_MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:citedResponsibleParty/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:address/che:CHE_CI_Address/gmd:country/gco:CharacterString"/>
					<xsl:text disable-output-escaping="no">&#160;&#45;&#160;</xsl:text>
					<xsl:value-of select="gmd:identificationInfo/che:CHE_MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:citedResponsibleParty/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:address/che:CHE_CI_Address/gmd:postalCode/gco:CharacterString"/>
					<xsl:text disable-output-escaping="no">&#160;</xsl:text>
					<xsl:value-of select="gmd:identificationInfo/che:CHE_MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:citedResponsibleParty/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:address/che:CHE_CI_Address/gmd:city/gco:CharacterString"/>
					<br/>
					<xsl:text disable-output-escaping="no">Tel&#160;</xsl:text>
					<xsl:value-of select="gmd:identificationInfo/che:CHE_MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:citedResponsibleParty/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:phone/che:CHE_CI_Telephone/gmd:voice/gco:CharacterString"/>
					<br/>
					<xsl:text disable-output-escaping="no">Fax&#160;</xsl:text>
					<xsl:value-of select="gmd:identificationInfo/che:CHE_MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:citedResponsibleParty/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:phone/che:CHE_CI_Telephone/gmd:facsimile/gco:CharacterString"/>
					<br/>
					<a href="mailto:{gmd:identificationInfo/che:CHE_MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:citedResponsibleParty/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:address/che:CHE_CI_Address/gmd:electronicMailAddress/gco:CharacterString}">
						<xsl:value-of select="gmd:identificationInfo/che:CHE_MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:citedResponsibleParty/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:address/che:CHE_CI_Address/gmd:electronicMailAddress/gco:CharacterString"/>
					</a>
					<br/>
					<a href="{gmd:identificationInfo/che:CHE_MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:citedResponsibleParty/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:onlineResource/gmd:CI_OnlineResource/gmd:linkage/che:PT_FreeURL/che:URLGroup/che:LocalisedURL/text()}" target="_new">
						<xsl:value-of select="gmd:identificationInfo/che:CHE_MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:citedResponsibleParty/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:onlineResource/gmd:CI_OnlineResource/gmd:linkage/che:PT_FreeURL/che:URLGroup/che:LocalisedURL/text()"/>
					</a>
					<br/>
					<xsl:value-of select="gmd:identificationInfo/che:CHE_MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:citedResponsibleParty/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:hoursOfService/gco:CharacterString"/>
					<br/><br/>
				</td>
			</tr>
	<!-- Überarbeitungsinterval - full-->
			<tr>
				<td class="text" width="30%" bgcolor="silver" valign="TOP">
					<b>
						<xsl:value-of select="string($label/labels/element[@name='gmd:maintenanceAndUpdateFrequency']/label)"/>
					</b>
				</td>
				<td bgcolor="#ffffff" valign="TOP">
					<xsl:call-template name="codelist">
						<xsl:with-param name="code" select="gmd:identificationInfo/che:CHE_MD_DataIdentification/gmd:resourceMaintenance/che:CHE_MD_MaintenanceInformation/gmd:maintenanceAndUpdateFrequency/gmd:MD_MaintenanceFrequencyCode/@codeListValue"/>
						<xsl:with-param name="path" select="string('gmd:MD_MaintenanceFrequencyCode')"/>
					</xsl:call-template>
				</td>
			</tr>
	<!-- Thematik - full-->
	<!-- Test mit xsl:for each-->
	<!-- Thematik kann nur in English dargestellt werden -> Anfrage bei KOGIS läuft -->
			<tr>
				<td class="text" width="30%" bgcolor="silver" valign="TOP">
					<b>
						<xsl:value-of select="string($label/labels/element[@name='gmd:MD_TopicCategoryCode']/label)"/>
					</b>
				</td>
				<td bgcolor="#ffffff" valign="TOP">
					<xsl:for-each select="gmd:identificationInfo/che:CHE_MD_DataIdentification/gmd:topicCategory">
						<xsl:value-of select="gmd:MD_TopicCategoryCode"/>
					</xsl:for-each>
					<br/><br/>
				</td>
			</tr>
	<!-- Schlüsselwörter - full-->
			<tr>
				<td class="text" width="30%" bgcolor="silver" valign="TOP">
					<b>
						<xsl:value-of select="string($label/labels/element[@name='gmd:keyword']/label)"/>
					</b>
				</td>
				<td bgcolor="#ffffff" valign="TOP">
					<xsl:for-each select="gmd:identificationInfo/che:CHE_MD_DataIdentification/gmd:descriptiveKeywords">
						<xsl:value-of select="gmd:MD_Keywords/gmd:keyword/gmd:PT_FreeText/gmd:textGroup/gmd:LocalisedCharacterString[@locale='#DE']"/>
					</xsl:for-each>
					<br/><br/>
				</td>
			</tr>
	<!-- Rechtliche Einschränkungen - full-->
	<!-- Test mit ul / il-->
			<tr>
				<td class="text" width="30%" bgcolor="silver" valign="TOP">
					<b>
						<xsl:value-of select="string($label/labels/element[@name='gmd:otherConstraints']/label)"/>
					</b>
				</td>
				<td bgcolor="#ffffff" valign="TOP">
					<xsl:for-each select="gmd:identificationInfo/che:CHE_MD_DataIdentification/gmd:resourceConstraints">
						<ul>
							<li><xsl:value-of select="che:CHE_MD_LegalConstraints/gmd:otherConstraints/gco:CharacterString"/></li>
						</ul>
					</xsl:for-each>
					<br/><br/>
				</td>
			</tr>
	<!-- Räumliche Darstellungsart - full-->
			<tr>
				<td class="text" width="30%" bgcolor="silver" valign="TOP">
					<b>
						<xsl:value-of select="string($label/labels/element[@name='gmd:spatialRepresentationType']/label)"/>
					</b>
				</td>
				<td bgcolor="#ffffff" valign="TOP">
					<xsl:call-template name="codelist">
					  <xsl:with-param name="code" select="gmd:identificationInfo/che:CHE_MD_DataIdentification/gmd:spatialRepresentationType/gmd:MD_SpatialRepresentationTypeCode/@codeListValue"/>
					  <xsl:with-param name="path" select="string('gmd:MD_SpatialRepresentationTypeCode')"/>
					</xsl:call-template>
				</td>
			</tr>  
	<!-- Masstabszahl - full-->
			<tr>
				<td class="text" width="30%" bgcolor="silver" valign="TOP">
					<b>
						<xsl:value-of select="string($label/labels/element[@name='gmd:denominator']/label)"/>
					</b>
				</td>
				<td bgcolor="#ffffff" valign="TOP">
					<xsl:value-of select="gmd:identificationInfo/che:CHE_MD_DataIdentification/gmd:spatialResolution/gmd:MD_Resolution/gmd:equivalentScale/gmd:MD_RepresentativeFraction/gmd:denominator/gco:Integer"/>
				</td>
			</tr>
	<!-- Distanz - full-->
			<tr>
				<td class="text" width="30%" bgcolor="silver" valign="TOP">
					<b>
						<xsl:value-of select="string($label/labels/element[@name='gmd:distance']/label)"/>
					</b>
				</td>
				<td bgcolor="#ffffff" valign="TOP">
					<xsl:value-of select="gmd:identificationInfo/che:CHE_MD_DataIdentification/gmd:spatialResolution/gmd:MD_Resolution/gmd:distance/gco:Distance"/>
				</td>
			</tr>
	<!-- Sprache - full-->
			<tr>
				<td class="text" width="30%" bgcolor="silver" valign="TOP">
					<b>
						<xsl:value-of select="string($label/labels/element[@name='gmd:language']/label)"/>
					</b>
				</td>
				<td bgcolor="#ffffff" valign="TOP">
					<xsl:value-of select="gmd:identificationInfo/che:CHE_MD_DataIdentification/gmd:language/gco:CharacterString"/>
				</td>
			</tr>
	<!-- Zeichensatz - full -->
			<tr>
				<td class="text" width="30%" bgcolor="silver" valign="TOP">
					<b>
						<xsl:value-of select="string($label/labels/element[@name='gmd:characterSet']/label)"/>
					</b>
				</td>
				<td bgcolor="#ffffff" valign="TOP">
					<xsl:call-template name="codelist">
					  <xsl:with-param name="code" select="gmd:identificationInfo/che:CHE_MD_DataIdentification/gmd:characterSet/gmd:MD_CharacterSetCode/@codeListValue"/>
					  <xsl:with-param name="path" select="string('gmd:MD_CharacterSetCode')"/>
					</xsl:call-template>
				</td>
			</tr>
			<tr>
				<td colspan="2" class="text" bgcolor="#f1f1f1" valign="TOP">
					<xsl:text disable-output-escaping="no">&#160;</xsl:text>
				</td>
			</tr>
<!-- Referenzsystem-->
			<tr>
				<td colspan="2" class="text" bgcolor="silver" valign="TOP">
					<b>
						<xsl:value-of select="string($label/labels/element[@name='gmd:referenceSystemInfo']/label)"/>
					</b>
				</td>
			</tr>
	<!-- Referenzsystem - full-->
			<tr>
				<td class="text" width="30%" bgcolor="silver" valign="TOP">
					<b>
						<xsl:value-of select="string($label/labels/element[@name='gmd:RS_Identifier']/label)"/>
					</b>
				</td>
				<td bgcolor="#ffffff" valign="TOP">
					<xsl:value-of select="gmd:referenceSystemInfo/gmd:MD_ReferenceSystem/gmd:referenceSystemIdentifier/gmd:RS_Identifier/gmd:code/gco:CharacterString"/>
				</td>
			</tr>
			<tr>
				<td colspan="2" class="text" bgcolor="#f1f1f1" valign="TOP">
					<xsl:text disable-output-escaping="no">&#160;</xsl:text>
				</td>
			</tr>
<!-- Vertrieb-->
			<tr>
				<td colspan="2" class="text" bgcolor="silver" valign="TOP">
					<b>
						<xsl:value-of select="string($label/labels/element[@name='gmd:distributionInfo']/label)"/>
					</b>
				</td>
			</tr>
	<!-- Online Darstellung - full-->
	<!-- Test mit xsl:for each-->
	<!-- Test URL-Darstellung -->
			<tr>
				<td class="text" width="30%" bgcolor="silver" valign="TOP">
					<b>
						<xsl:value-of select="string($label/labels/element[@name='gmd:CI_OnlineResource']/label)"/>
					</b>
				</td>
				<td bgcolor="#ffffff" valign="TOP">
					<xsl:for-each select="gmd:distributionInfo/gmd:MD_Distribution/gmd:transferOptions/gmd:MD_DigitalTransferOptions/gmd:onLine">
						<xsl:value-of select="gmd:CI_OnlineResource/gmd:name/gco:CharacterString"/>
						<xsl:text disable-output-escaping="no">&#160;&#45;&#160;</xsl:text>
						<xsl:value-of select="gmd:CI_OnlineResource/gmd:description/gco:CharacterString"/>
						<br/>
						<xsl:value-of select="gmd:CI_OnlineResource/gmd:linkage/gmd:URL"/>
						<br/>
						<a href="{gmd:CI_OnlineResource/gmd:linkage/gmd:URL}" target="_new">
							<xsl:value-of select="/gmd:CI_OnlineResource/gmd:linkage/gmd:URL"/>
						</a>
					</xsl:for-each>
					<br/><br/>
				</td>
			</tr>	
	<!-- Adresse Vertrieb - full-->
			<tr>
				<td class="text" width="30%" bgcolor="silver" valign="TOP">
					<b>
						<xsl:call-template name="codelist">
						<xsl:with-param name="code" select="gmd:distributionInfo/gmd:MD_Distribution/gmd:distributor/gmd:MD_Distributor/gmd:distributorContact/che:CHE_CI_ResponsibleParty/gmd:role/gmd:CI_RoleCode/@codeListValue"/>
						<xsl:with-param name="path" select="string('gmd:CI_RoleCode')"/>
						</xsl:call-template>
					</b>
				</td>
				<td bgcolor="#ffffff" valign="TOP">
					<xsl:value-of select="gmd:distributionInfo/gmd:MD_Distribution/gmd:distributor/gmd:MD_Distributor/gmd:distributorContact/che:CHE_CI_ResponsibleParty/gmd:organisationName/gmd:PT_FreeText/gmd:textGroup/gmd:LocalisedCharacterString"/>
					<br/>
					<xsl:value-of select="gmd:distributionInfo/gmd:MD_Distribution/gmd:distributor/gmd:MD_Distributor/gmd:distributorContact/che:CHE_CI_ResponsibleParty/gmd:positionName/gmd:PT_FreeText/gmd:textGroup/gmd:LocalisedCharacterString"/>
					<br/>
					<xsl:value-of select="gmd:distributionInfo/gmd:MD_Distribution/gmd:distributor/gmd:MD_Distributor/gmd:distributorContact/che:CHE_CI_ResponsibleParty/che:individualFirstName/gco:CharacterString"/>
					<br/>
					<xsl:value-of select="gmd:distributionInfo/gmd:MD_Distribution/gmd:distributor/gmd:MD_Distributor/gmd:distributorContact/che:CHE_CI_ResponsibleParty/che:individualLastName/gco:CharacterString"/>
					<br/>
					<xsl:value-of select="gmd:distributionInfo/gmd:MD_Distribution/gmd:distributor/gmd:MD_Distributor/gmd:distributorContact/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:address/che:CHE_CI_Address/che:streetName/gco:CharacterString"/>
					<xsl:text disable-output-escaping="no">&#160;</xsl:text>
					<xsl:value-of select="gmd:distributionInfo/gmd:MD_Distribution/gmd:distributor/gmd:MD_Distributor/gmd:distributorContact/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:address/che:CHE_CI_Address/che:streetNumber/gco:CharacterString"/>
					<br/>
					<xsl:value-of select="gmd:distributionInfo/gmd:MD_Distribution/gmd:distributor/gmd:MD_Distributor/gmd:distributorContact/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:address/che:CHE_CI_Address/che:postBox/gco:CharacterString"/>
					<br/>
					<xsl:value-of select="gmd:distributionInfo/gmd:MD_Distribution/gmd:distributor/gmd:MD_Distributor/gmd:distributorContact/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:address/che:CHE_CI_Address/gmd:country/gco:CharacterString"/>
					<xsl:text disable-output-escaping="no">&#160;&#45;&#160;</xsl:text>
					<xsl:value-of select="gmd:distributionInfo/gmd:MD_Distribution/gmd:distributor/gmd:MD_Distributor/gmd:distributorContact/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:address/che:CHE_CI_Address/gmd:postalCode/gco:CharacterString"/>
					<xsl:text disable-output-escaping="no">&#160;</xsl:text>
					<xsl:value-of select="gmd:distributionInfo/gmd:MD_Distribution/gmd:distributor/gmd:MD_Distributor/gmd:distributorContact/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:address/che:CHE_CI_Address/gmd:city/gco:CharacterString"/>
					<br/>
					<xsl:text disable-output-escaping="no">Tel&#160;</xsl:text>
					<xsl:value-of select="gmd:distributionInfo/gmd:MD_Distribution/gmd:distributor/gmd:MD_Distributor/gmd:distributorContact/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:phone/che:CHE_CI_Telephone/gmd:voice/gco:CharacterString"/>
					<br/>
					<xsl:text disable-output-escaping="no">Fax&#160;</xsl:text>
					<xsl:value-of select="gmd:distributionInfo/gmd:MD_Distribution/gmd:distributor/gmd:MD_Distributor/gmd:distributorContact/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:phone/che:CHE_CI_Telephone/gmd:facsimile/gco:CharacterString"/>
					<br/>
					<a href="mailto:{gmd:distributionInfo/gmd:MD_Distribution/gmd:distributor/gmd:MD_Distributor/gmd:distributorContact/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:address/che:CHE_CI_Address/gmd:electronicMailAddress/gco:CharacterString}">
						<xsl:value-of select="gmd:distributionInfo/gmd:MD_Distribution/gmd:distributor/gmd:MD_Distributor/gmd:distributorContact/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:address/che:CHE_CI_Address/gmd:electronicMailAddress/gco:CharacterString"/>
					</a>
					<br/>
					<a href="{gmd:distributionInfo/gmd:MD_Distribution/gmd:distributor/gmd:MD_Distributor/gmd:distributorContact/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:onlineResource/gmd:CI_OnlineResource/gmd:linkage/che:PT_FreeURL/che:URLGroup/che:LocalisedURL}" target="_new">
						<xsl:value-of select="gmd:distributionInfo/gmd:MD_Distribution/gmd:distributor/gmd:MD_Distributor/gmd:distributorContact/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:onlineResource/gmd:CI_OnlineResource/gmd:linkage/che:PT_FreeURL/che:URLGroup/che:LocalisedURL"/>
					</a>
					<br/>
					<xsl:value-of select="gmd:distributionInfo/gmd:MD_Distribution/gmd:distributor/gmd:MD_Distributor/gmd:distributorContact/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:hoursOfService/gco:CharacterString"/>
					<br/><br/>
				</td>
			</tr>
	<!-- Abgabeformat - full-->
	<!-- Test mit xsl:for each-->
			<tr>
				<td class="text" width="30%" bgcolor="silver" valign="TOP">
					<b>
						<xsl:value-of select="string($label/labels/element[@name='gmd:distributionFormat']/label)"/>
					</b>
				</td>
				<td bgcolor="#ffffff" valign="TOP">
					<xsl:for-each select="gmd:distributionInfo/gmd:MD_Distribution/gmd:distributionFormat">
						<xsl:value-of select="gmd:MD_Format/gmd:name/gco:CharacterString"/>
						<xsl:text disable-output-escaping="no">&#160;</xsl:text>
						<xsl:value-of select="gmd:MD_Format/gmd:version/gco:CharacterString"/>
					</xsl:for-each>
					<br/><br/>
				</td>
			</tr>	
			<tr>
				<td colspan="2" class="text" bgcolor="#f1f1f1" valign="TOP">
					<xsl:text disable-output-escaping="no">&#160;</xsl:text>
				</td>
			</tr>
<!-- Metadaten - full-->
			<tr>
				<td colspan="2" class="text" bgcolor="silver" valign="TOP">
					<b>
						<xsl:text disable-output-escaping="no">Metadaten</xsl:text>
					</b>
				</td>
			</tr>
	<!-- Metadatensatzidentifikator - full-->
			<tr>
				<td class="text" width="30%" bgcolor="silver" valign="TOP">
					<b>
						<xsl:value-of select="string($label/labels/element[@name='gmd:fileIdentifier']/label)"/>
					</b>
				</td>
				<td bgcolor="#ffffff" valign="TOP">
					<xsl:value-of select="gmd:fileIdentifier/gco:CharacterString"/>
				</td>
			</tr>
	<!-- Datum - full-->
			<tr>
				<td class="text" width="30%" bgcolor="silver" valign="TOP">
					<b>
						<xsl:value-of select="string($label/labels/element[@name='gmd:dateStamp']/label)"/>
					</b>
				</td>
				<td bgcolor="#ffffff" valign="TOP">
					<xsl:value-of select="gmd:dateStamp/gco:DateTime"/>

				</td>
			</tr>
	<!-- Bezeichnung des Metadatenstandards - full-->
			<tr>
				<td class="text" width="30%" bgcolor="silver" valign="TOP">
					<b>
						<xsl:value-of select="string($label/labels/element[@name='gmd:metadataStandardName']/label)"/>
					</b>
				</td>
				<td bgcolor="#ffffff" valign="TOP">
					<xsl:value-of select="gmd:metadataStandardName/gco:CharacterString"/>


				</td>
			</tr>
	<!-- Version des Metadatenstandards - full-->
			<tr>
				<td class="text" width="30%" bgcolor="silver" valign="TOP">
					<b>
						<xsl:value-of select="string($label/labels/element[@name='gmd:metadataStandardVersion']/label)"/>
					</b>
				</td>
				<td bgcolor="#ffffff" valign="TOP">
					<xsl:value-of select="gmd:metadataStandardVersion/gco:CharacterString"/>
				</td>
			</tr>
	<!-- Sprache - full-->
			<tr>
				<td class="text" width="30%" bgcolor="silver" valign="TOP">
					<b>
						<xsl:value-of select="string($label/labels/element[@name='gmd:language']/label)"/>
					</b>
				</td>
				<td bgcolor="#ffffff" valign="TOP">
					<xsl:value-of select="gmd:language/gco:CharacterString"/>	
				</td>
			</tr>
	<!-- Zeichensatz - full -->
			<tr>
				<td class="text" width="30%" bgcolor="silver" valign="TOP">
					<b>
						<xsl:value-of select="string($label/labels/element[@name='gmd:characterSet']/label)"/>
					</b>
				</td>
				<td bgcolor="#ffffff" valign="TOP">
					<xsl:call-template name="codelist">
						<xsl:with-param name="code" select="gmd:characterSet/gmd:MD_CharacterSetCode/@codeListValue"/>
						<xsl:with-param name="path" select="string('gmd:MD_CharacterSetCode')"/>
					</xsl:call-template>
				</td>
			</tr>
	<!-- Hirarchieebene - full-->
			<tr>
				<td class="text" width="30%" bgcolor="silver" valign="TOP">
					<b>
						<xsl:value-of select="string($label/labels/element[@name='gmd:hierarchyLevel']/label)"/>
					</b>
				</td>
				<td bgcolor="#ffffff" valign="TOP">
					<xsl:call-template name="codelist">
						<xsl:with-param name="code" select="gmd:hierarchyLevel/gmd:MD_ScopeCode/@codeListValue"/>
						<xsl:with-param name="path" select="string('gmd:MD_ScopeCode')"/>
					</xsl:call-template>
				</td>
			</tr>
	<!-- Adresse Metadaten - full-->
			<tr>
				<td class="text" width="30%" bgcolor="silver" valign="TOP">
					<b>
						<xsl:call-template name="codelist">
						<xsl:with-param name="code" select="gmd:contact/che:CHE_CI_ResponsibleParty/gmd:role/gmd:CI_RoleCode/@codeListValue"/>
						<xsl:with-param name="path" select="string('gmd:CI_RoleCode')"/>
						</xsl:call-template>
					</b>
				</td>
				<td bgcolor="#ffffff" valign="TOP">
					<xsl:value-of select="gmd:contact/che:CHE_CI_ResponsibleParty/gmd:organisationName/gmd:PT_FreeText/gmd:textGroup/gmd:LocalisedCharacterString"/>
					<br/>
					<xsl:value-of select="gmd:contact/che:CHE_CI_ResponsibleParty/gmd:positionName/gmd:PT_FreeText/gmd:textGroup/gmd:LocalisedCharacterString"/>
					<br/>
					<xsl:value-of select="gmd:contact/che:CHE_CI_ResponsibleParty/che:individualFirstName/gco:CharacterString"/>
					<br/>
					<xsl:value-of select="gmd:contact/che:CHE_CI_ResponsibleParty/che:individualLastName/gco:CharacterString"/>
					<br/>
					<xsl:value-of select="gmd:contact/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:address/che:CHE_CI_Address/che:streetName/gco:CharacterString"/>
					<xsl:text disable-output-escaping="no">&#160;</xsl:text>
					<xsl:value-of select="gmd:contact/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:address/che:CHE_CI_Address/che:streetNumber/gco:CharacterString"/>
					<br/>
					<xsl:value-of select="gmd:contact/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:address/che:CHE_CI_Address/che:postBox/gco:CharacterString"/>
					<br/>
					<xsl:value-of select="gmd:contact/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:address/che:CHE_CI_Address/gmd:country/gco:CharacterString"/>
					<xsl:text disable-output-escaping="no">&#160;&#45;&#160;</xsl:text>
					<xsl:value-of select="gmd:contact/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:address/che:CHE_CI_Address/gmd:postalCode/gco:CharacterString"/>
					<xsl:text disable-output-escaping="no">&#160;</xsl:text>
					<xsl:value-of select="gmd:contact/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:address/che:CHE_CI_Address/gmd:city/gco:CharacterString"/>
					<br/>
					<xsl:text disable-output-escaping="no">Tel&#160;</xsl:text>
					<xsl:value-of select="gmd:contact/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:phone/che:CHE_CI_Telephone/gmd:voice/gco:CharacterString"/>
					<br/>
					<xsl:text disable-output-escaping="no">Fax&#160;</xsl:text>
					<xsl:value-of select="gmd:contact/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:phone/che:CHE_CI_Telephone/gmd:facsimile/gco:CharacterString"/>
					<br/>
					<a href="mailto:{gmd:contact/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:address/che:CHE_CI_Address/gmd:electronicMailAddress/gco:CharacterString}">
						<xsl:value-of select="gmd:contact/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:address/che:CHE_CI_Address/gmd:electronicMailAddress/gco:CharacterString"/>
					</a>
					<br/>
					<a href="{gmd:contact/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:onlineResource/gmd:CI_OnlineResource/gmd:linkage/che:PT_FreeURL/che:URLGroup/che:LocalisedURL/text()}" target="_new">
						<xsl:value-of select="gmd:contact/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:onlineResource/gmd:CI_OnlineResource/gmd:linkage/che:PT_FreeURL/che:URLGroup/che:LocalisedURL/text()"/>
					</a>
					<br/>
					<xsl:value-of select="gmd:contact/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:hoursOfService/gco:CharacterString"/>
					<br/><br/>
				</td>
			</tr>
			<tr>
				<td colspan="2" class="text" bgcolor="#f1f1f1" valign="TOP">
					<xsl:text disable-output-escaping="no">&#160;</xsl:text>
				</td>
			</tr>
		</table>
	</xsl:template>
<!-- Der codelist Wert (label) wird ausgewählt wenn der Eintrag code gleich dem codeListValue vom xml ist -->
	<xsl:template name="codelist">
		<xsl:param name="code"/>
		<xsl:param name="path"/>
		<xsl:value-of select="string($value/codelists/codelist[@name= $path]/entry[code = $code]/label)"/>
	</xsl:template>
</xsl:stylesheet>
