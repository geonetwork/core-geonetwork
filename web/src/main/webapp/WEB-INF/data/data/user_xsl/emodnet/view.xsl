<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	version="1.0" xmlns:gmd="http://www.isotc211.org/2005/gmd" xmlns:gco="http://www.isotc211.org/2005/gco"
	xmlns:gmx="http://www.isotc211.org/2005/gmx" xmlns:srv="http://www.isotc211.org/2005/srv"
	xmlns:gml="http://www.opengis.net/gml" xmlns:gts="http://www.isotc211.org/2005/gts"
	xmlns:che="http://www.geocat.ch/2008/che" xmlns:xlink="http://www.w3.org/1999/xlink">


	<!-- Load labels. -->
	<xsl:variable name="label" select="/root/schemas/iso19139" />
	<xsl:template xmlns:geonet="http://www.fao.org/geonetwork"
		mode="iso19139" match="geonet:info" />
	<!-- Root element matching. -->
	<xsl:template match="/" priority="5">
		<html>
			<!-- Set some vars. -->
			<xsl:variable name="title"
				select="/root/gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:title/gco:CharacterString" />

			<head>
				<title>
					Metadata:
					<xsl:value-of select="$title" />
				</title>
			</head>
			<body>

				<link rel="stylesheet" type="text/css" href="http://localhost:8080/geonetwork/apps/sextant/css/schema/reset.css" />
				<link rel="stylesheet" type="text/css" href="http://localhost:8080/geonetwork/apps/sextant/css/schema/emodnet.css" />
				<div class="tpl-emodnet">
					<div class="ui-layout-content">
						<table class="print_table" border="0" cellpadding="0"
							cellspacing="0">
							<tbody>
								<tr valign="top">
									<td class="print_ttl">Metadata information</td>
									<td class="print_data">
									</td>
								</tr>
								<xsl:apply-templates mode="iso19139"
									select="/root/gmd:MD_Metadata/gmd:fileIdentifier|root/gmd:MD_Metadata/gmd:language" />

								<tr valign="top">
									<td class="print_ttl">Identification</td>
									<td class="print_data">
									</td>
								</tr>
								<xsl:apply-templates mode="iso19139"
									select="/root/gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:title|
											/root/gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:identifier|
											/root/gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:graphicOverview/gmd:MD_BrowseGraphic/gmd:fileName[1]" />

								<tr valign="top">
									<td class="print_ttl">What ?</td>
									<td class="print_data">
									</td>
								</tr>
								<xsl:apply-templates mode="iso19139"
									select="/root/gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:spatialRepresentationType" />
								<xsl:apply-templates mode="iso19139"
									select="/root/gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:spatialResolution" />

								<tr valign="top">
									<td class="print_ttl">Abstract</td>
									<td class="print_data">
									</td>
								</tr>
								<xsl:apply-templates mode="iso19139"
									select="/root/gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:abstract|
									/root/gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:date/gmd:CI_Date" />

								<tr valign="top">
									<td class="print_ttl_h1">Description of processed data sources</td>
									<td class="print_data"></td>
								</tr>
								<tr valign="top">
									<td class="print_ttl_h1">Description of data processing</td>
									<td class="print_data"></td>
								</tr>
								<xsl:apply-templates mode="iso19139"
									select="/root/gmd:MD_Metadata//gmd:statement" />

								<tr valign="top">
									<td class="print_ttl_h1">Quality / Accuracy / Calibration</td>
									<td class="print_data"></td>
								</tr>
								<xsl:apply-templates mode="iso19139"
									select="/root/gmd:MD_Metadata//gmd:resourceConstraints//gmd:useLimitation" />

								<tr valign="top">
									<td class="print_ttl_h1">Intellectual property</td>
									<td class="print_data"></td>
								</tr>
								<xsl:apply-templates mode="iso19139"
									select="/root/gmd:MD_Metadata//gmd:accessConstraints" />

								<tr valign="top">
									<td class="print_ttl">Where ?</td>
									<td class="print_data">
									</td>
								</tr>
								<xsl:apply-templates mode="iso19139"
									select="/root/gmd:MD_Metadata//gmd:EX_GeographicBoundingBox" />

								<tr valign="top">
									<td class="print_ttl_h1">Coordinate Reference System</td>
									<td class="print_data"></td>
								</tr>

								<xsl:apply-templates mode="iso19139"
									select="/root/gmd:MD_Metadata//gmd:referenceSystemInfo//gmd:RS_Identifier" />

								<tr valign="top">
									<td class="print_ttl">When ?</td>
									<td class="print_data">
									</td>
								</tr>

								<tr valign="top">
									<td class="print_ttl">Who ?</td>
									<td class="print_data">
									</td>
								</tr>

								<tr valign="top">
									<td class="print_ttl">Where to find ?</td>
									<td class="print_data">
									</td>
								</tr>
								<xsl:apply-templates mode="iso19139"
									select="/root/gmd:MD_Metadata//gmd:transferOptions//gmd:linkage" />

								<tr valign="top">
									<td class="print_ttl_h1">Data distributor center</td>
									<td class="print_data"></td>
								</tr>

								<xsl:apply-templates mode="iso19139"
									select="/root/gmd:MD_Metadata/gmd:distributionInfo//gmd:distributorContact//gmd:organisationName" />

								<tr valign="top">
									<td class="print_ttl_h1">Collating center</td>
									<td class="print_data"></td>
								</tr>

								<xsl:apply-templates mode="iso19139"
									select="/root/gmd:MD_Metadata/gmd:identificationInfo//gmd:pointOfContact" />
							</tbody>
						</table>
					</div>
				</div>
			</body>
		</html>
	</xsl:template>


	<xsl:template mode="iso19139" match="gmd:spatialResolution">
		<xsl:variable name="name" select="name(.)" />
		<xsl:variable name="title">
			<xsl:call-template name="getTitle">
				<xsl:with-param name="name" select="$name" />
			</xsl:call-template>
		</xsl:variable>

		<tr valign="top">
			<td class="print_desc">
				<xsl:value-of select="$title" />
			</td>
			<td class="print_data">
				<xsl:value-of
					select="concat('1 : ', gmd:MD_Resolution/gmd:equivalentScale/gmd:MD_RepresentativeFraction/gmd:denominator/gco:Integer)" />
			</td>
		</tr>
	</xsl:template>

	<xsl:template mode="iso19139" match="gmd:CI_Date">
		<xsl:variable name="name" select="name(.)" />
		<xsl:variable name="title">
			<xsl:call-template name="getTitle">
				<xsl:with-param name="name" select="$name" />
			</xsl:call-template>
		</xsl:variable>

		<tr valign="top">
			<td class="print_desc">
				<xsl:value-of select="$title" />
			</td>
			<td class="print_data">
				<xsl:value-of select="gmd:date/gco:DateTime" />
			</td>
		</tr>
	</xsl:template>

	<xsl:template mode="iso19139"
		match="gmd:spatialRepresentationType|gmd:accessConstraints">
		<xsl:variable name="name" select="name(.)" />
		<xsl:variable name="title">
			<xsl:call-template name="getTitle">
				<xsl:with-param name="name" select="$name" />
			</xsl:call-template>
		</xsl:variable>

		<tr valign="top">
			<td class="print_desc">
				<xsl:value-of select="$title" />
			</td>
			<td class="print_data">
				<xsl:value-of select="*/@codeListValue" />
			</td>
		</tr>
	</xsl:template>


	<xsl:template mode="iso19139" match="gmd:EX_GeographicBoundingBox">

		<xsl:variable name="name" select="name(.)" />
		<xsl:variable name="title">
			<xsl:call-template name="getTitle">
				<xsl:with-param name="name" select="$name" />
			</xsl:call-template>
		</xsl:variable>

		<tr valign="top">
			<td class="print_desc">
				<xsl:value-of select="$title" />
			</td>
			<td class="print_data">
				<xsl:value-of select="gmd:westBoundLongitude/gco:Decimal" />
				/
				<xsl:value-of select="gmd:southBoundLatitude/gco:Decimal" />
				/
				<xsl:value-of select="gmd:eastBoundLongitude/gco:Decimal" />
				/
				<xsl:value-of select="gmd:northBoundLatitude/gco:Decimal" />
			</td>
		</tr>
	</xsl:template>


	<!-- ACCESS PART -->

	<!-- Hide transfer option in Distribution Info tag -->
	<xsl:template mode="iso19139" match="gmd:transferOptions">
	</xsl:template>

	<!-- Display characterString -->
	<xsl:template mode="iso19139"
		match="gmd:*[gco:CharacterString or gmd:PT_FreeText]|
        srv:*[gco:CharacterString or gmd:PT_FreeText]|
        gco:aName[gco:CharacterString]|gmd:*[gmd:URL]"
		priority="2">
		<xsl:variable name="name" select="name(.)" />
		<xsl:variable name="title">
			<xsl:call-template name="getTitle">
				<xsl:with-param name="name" select="$name" />
			</xsl:call-template>
		</xsl:variable>

		<xsl:if test="gco:CharacterString!=''">
			<tr valign="top">
				<td class="print_desc">
					<xsl:value-of select="$title" />
				</td>
				<td class="print_data">
					<xsl:value-of select="gco:CharacterString" />
				</td>
			</tr>
		</xsl:if>
		<xsl:if test="gmd:URL!=''">
			<tr valign="top">
				<td class="print_desc">
					<xsl:value-of select="$title" />
				</td>
				<td class="print_data">
					<xsl:value-of select="gmd:URL" />
				</td>
			</tr>
		</xsl:if>
		<!-- Here you could display translation using PT_FreeText -->
	</xsl:template>


	<!-- Get title from che profil if exist, if not default to iso. -->
	<xsl:template name="getTitle">
		<xsl:param name="name" />
		<xsl:variable name="title"
			select="string($label/labels/element[@name=$name]/label)" />
		<xsl:choose>
			<xsl:when test="normalize-space($title)">
				<xsl:value-of select="$title" />
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="string($label/labels/element[@name=$name]/label)" />
			</xsl:otherwise>
		</xsl:choose>

	</xsl:template>


</xsl:stylesheet>