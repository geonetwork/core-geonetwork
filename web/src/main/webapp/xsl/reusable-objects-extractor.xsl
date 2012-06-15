<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:gmd="http://www.isotc211.org/2005/gmd"
	xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xmlns:gml="http://www.opengis.net/gml" xmlns:gts="http://www.isotc211.org/2005/gts"
    xmlns:geonet="http://www.fao.org/geonetwork"
    xmlns:che="http://www.geocat.ch/2008/che" 
    xmlns:srv="http://www.isotc211.org/2005/srv"
	xmlns:gco="http://www.isotc211.org/2005/gco">
	
	<xsl:output method="xml" indent="yes"></xsl:output>
	<xsl:template match="/">
		<split>
			<metadata>
				<xsl:apply-templates mode="metadata"/>
			</metadata>
			<contacts>
				<xsl:apply-templates mode="contacts" />
			</contacts>
			<formats>
				<xsl:apply-templates mode="formats" />
			</formats>
			<keywords>
				<xsl:apply-templates mode="keywords" />
			</keywords>
			<extents>
				<xsl:apply-templates mode="extents" />
			</extents>
		</split>
	</xsl:template>

	<!-- standard copy template -->
	<xsl:template match="@*|node()"
		mode="metadata">
        <xsl:param name="id"></xsl:param>
		<xsl:copy>
			<xsl:apply-templates select="@*|node()" mode="metadata" />
		</xsl:copy>
	</xsl:template>

    <xsl:template match="gmd:contact|gmd:pointOfContact|gmd:userContactInfo|gmd:distributorContact|gmd:citedResponsibleParty"
        mode="metadata">
        <contactsPlaceholder/>
    </xsl:template>

    <xsl:template match="gmd:contact|gmd:pointOfContact|gmd:userContactInfo|gmd:distributorContact|gmd:citedResponsibleParty"
        mode="contacts">
        <xsl:copy-of select="." />
    </xsl:template>

    <xsl:template match="gmd:resourceFormat|gmd:distributionFormat"
        mode="metadata">
        <formatsPlaceholder/>
    </xsl:template>

    <xsl:template match="gmd:resourceFormat|gmd:distributionFormat"
        mode="formats">
        <xsl:copy-of select="." />
    </xsl:template>

    <xsl:template match="gmd:descriptiveKeywords"
        mode="metadata">
        <keywordsPlaceholder/>
    </xsl:template>

    <xsl:template match="gmd:descriptiveKeywords"
        mode="keywords">
        <xsl:copy-of select="." />
    </xsl:template>

    <xsl:template match="srv:extent|gmd:extent|gmd:spatialExtent|gmd:sourceExtent"
        mode="metadata">
        <extentsPlaceholder/>
    </xsl:template>

    <xsl:template match="srv:extent|gmd:extent|gmd:spatialExtent|gmd:sourceExtent"
        mode="extents">
        <xsl:copy-of select="." />
    </xsl:template>

    <xsl:template match="geonet:info" mode="metadata" />
    <xsl:template match="text()" mode="contacts" />
	<xsl:template match="text()" mode="formats" />
	<xsl:template match="text()" mode="keywords" />
	<xsl:template match="text()" mode="extents" />

</xsl:stylesheet>