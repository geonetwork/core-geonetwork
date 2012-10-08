<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	version="1.0" xmlns:gmd="http://www.isotc211.org/2005/gmd" xmlns:gco="http://www.isotc211.org/2005/gco"
	xmlns:gmx="http://www.isotc211.org/2005/gmx" xmlns:srv="http://www.isotc211.org/2005/srv"
	xmlns:gml="http://www.opengis.net/gml" xmlns:gts="http://www.isotc211.org/2005/gts"
	xmlns:xlink="http://www.w3.org/1999/xlink">


	<!-- Load labels. -->
	<xsl:variable name="label" select="/root/schemas/iso19139.sextant" />
	<xsl:variable name="labelIso" select="/root/schemas/iso19139" />
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
			
			<link rel="stylesheet" type="text/css" href="{root/url}/apps/sextant/css/schema/reset.css"/>
			<link rel="stylesheet" type="text/css" href="{root/url}/apps/sextant/css/schema/jquery-ui-1.8.2.custom.css"/>
			<link rel="stylesheet" type="text/css" href="{root/url}/apps/sextant/css/schema/main.css"/>
			<link rel="stylesheet" type="text/css" href="{root/url}/apps/sextant/css/schema/default.css"/>
			<div class="tpl-sextant">
				<div class="ui-layout-content">
					<div>
						<div class="result-metadata-modal-tabs">
							<div id="result-metadata-modal-tab-1">
								<div class="result-metadata-modal-resume">
									<h6>
									<xsl:call-template name="getTitle">
										<xsl:with-param name="name" select="'gmd:MD_Keywords'" />
									</xsl:call-template>
									</h6>
									<p><div class="result-metadata-modal-content">
									<xsl:apply-templates mode="iso19139" 
										select="/root/gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:descriptiveKeywords/gmd:MD_Keywords/gmd:keyword" />
									</div></p>
								</div>		
								<h5>Description</h5>
								<div class="result-metadata-modal-content">
									<xsl:apply-templates mode="iso19139" 
										select="/root/gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:extent/gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicBoundingBox" />
									<xsl:apply-templates mode="iso19139"
										select="/root/gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:abstract|
										/root/gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:supplementalInformation|
										/root/gmd:MD_Metadata/gmd:dataQualityInfo/gmd:DQ_DataQuality/gmd:lineage/gmd:LI_Lineage/gmd:statement" />
								</div>
								
								<h5><xsl:value-of select="/root/schemas/iso19139.sextant/strings/constraints_access" /></h5>
								<div class="result-metadata-modal-content">
								
									<xsl:apply-templates mode="iso19139"
										select="/root/gmd:MD_Metadata/gmd:distributionInfo" />
									<hr/>
									<xsl:apply-templates mode="iso19139"
										select="/root/gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:resourceConstraints/gmd:MD_LegalConstraints" />
										<p></p>
								</div>
								
								<h5><xsl:value-of select="/root/schemas/iso19139.sextant/strings/contact" /></h5>
								<div class="result-metadata-modal-content">
									<p></p>
									<ul>
										<xsl:call-template name="contact"/>
									</ul>
								</div>
							</div>
						</div>
					</div>
				</div>
			</div>
			</body>
		</html>
	</xsl:template>

	<!--  DESCRIPTION PART -->
	
	<!--  Display the thumbnail and Geographic BBox -->
	<xsl:template mode="iso19139" match="gmd:EX_GeographicBoundingBox">
		
		<div class="result-metadata-modal-title">
			<table>
				<tr>
					<td></td>
					<td>
						<xsl:value-of select="gmd:northBoundLatitude/gco:Decimal" />
					</td>
					<td></td>
				</tr>
				<tr>
					<td><xsl:value-of select="gmd:westBoundLongitude/gco:Decimal" /></td>
					<td>
						<img class="result-photo">
							<xsl:attribute name="src">
								<xsl:value-of select="/root/gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:graphicOverview/gmd:MD_BrowseGraphic/gmd:fileName/gco:CharacterString" />
							</xsl:attribute>
						</img>
					</td>
					<td><xsl:value-of select="gmd:eastBoundLongitude/gco:Decimal" /></td>
				</tr>
				<tr>
					<td></td>
					<td><xsl:value-of select="gmd:southBoundLatitude/gco:Decimal" /></td>
					<td></td>
				</tr>
			</table>
		</div>
	</xsl:template>
	
	<!-- Key words -->
	<xsl:template mode="iso19139" match="gmd:keyword"
		priority="3">
		<xsl:choose>
			<xsl:when test="position()!=last()">
				<xsl:value-of select="concat(gco:CharacterString, ', ')" />
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="gco:CharacterString" />
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	
	<!--  Abstract & Statement : Display Title and <p> with text -->
	<xsl:template mode="iso19139" match="gmd:abstract|gmd:statement|gmd:supplementalInformation"
		priority="3">
		<xsl:if test="gco:CharacterString!=''">
			<xsl:variable name="name" select="name(.)" />
			<xsl:variable name="title">
				<xsl:call-template name="getTitle">
					<xsl:with-param name="name" select="$name" />
				</xsl:call-template>
			</xsl:variable>
			
			<hr/>
			<div class="result-metadata-modal-resume">
				<h6><xsl:value-of select="$title" /></h6>
				<div class="result-metadata-modal-content">
					<p><xsl:value-of select="gco:CharacterString" />
					</p></div>
			</div>
		</xsl:if>
	</xsl:template>

	<!-- ACCESS PART  -->
	
	<!-- Hide transfer option in Distribution Info tag -->
	<xsl:template mode="iso19139"
		match="gmd:transferOptions">
    </xsl:template>
    
    <!--  GENERIC (Access & Contact) -->
    
    <!-- Display in bold the title of a section -->
	<xsl:template mode="iso19139"
		match="gmd:locale|gmd:contact|gmd:identificationInfo|
		gmd:MD_LegalConstraints|gmd:MD_SecurityConstraints|gmd:MD_Constraints|
        gmd:resourceConstraints|
        gmd:distributionInfo">

		<xsl:variable name="name" select="name(.)" />
		<xsl:variable name="title">
			<xsl:call-template name="getTitle">
				<xsl:with-param name="name" select="$name" />
			</xsl:call-template>
		</xsl:variable>
		
		<p><b><xsl:value-of select="$title" /></b></p>
		<ul><xsl:apply-templates mode="iso19139" /></ul>
		
	</xsl:template>
	
	<xsl:template name="contact" mode="iso19139"
		match="/root/gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:pointOfContact" priority="3">
		
		<xsl:for-each select="/root/gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:pointOfContact/gmd:CI_ResponsibleParty">
			<xsl:apply-templates mode="iso19139"
					select="gmd:individualName" />
			<xsl:apply-templates mode="iso19139"
					select="gmd:organisationName" />
			<xsl:apply-templates mode="iso19139"
					select="gmd:contactInfo//gmd:electronicMailAddress" />
			
			<li>
				<b><xsl:value-of select="/root/schemas/iso19139.sextant/strings/role" /> : </b>
				<xsl:variable name="choiceValue" select="gmd:role/gmd:CI_RoleCode/@codeListValue" />
				<xsl:variable name="name" select="'gmd:CI_RoleCode'" />
				<xsl:value-of select="string($labelIso/codelists/codelist[@name = $name]/entry[code = $choiceValue]/label)" />
			</li>
			<br/>
		</xsl:for-each>
		
	</xsl:template>

	<!-- Display characterString -->
	<xsl:template mode="iso19139"
		match="gmd:*[gco:CharacterString or gmd:PT_FreeText]|
        srv:*[gco:CharacterString or gmd:PT_FreeText]|
        gco:aName[gco:CharacterString]"
		priority="2">
		<xsl:variable name="name" select="name(.)" />
		<xsl:variable name="title">
			<xsl:call-template name="getTitle">
				<xsl:with-param name="name" select="$name" />
			</xsl:call-template>
		</xsl:variable>
		
		<xsl:if test="gco:CharacterString!=''">
			<li>
				<b><xsl:value-of select="$title" /> : </b>
				<xsl:value-of select="gco:CharacterString" />
			</li>
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
				<xsl:value-of select="string($labelIso/labels/element[@name=$name]/label)" />
			</xsl:otherwise>
		</xsl:choose>

	</xsl:template>


</xsl:stylesheet>