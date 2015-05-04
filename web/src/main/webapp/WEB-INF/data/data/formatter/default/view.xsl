<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0" xmlns:gmd="http://www.isotc211.org/2005/gmd" xmlns:gco="http://www.isotc211.org/2005/gco" xmlns:gmx="http://www.isotc211.org/2005/gmx" xmlns:srv="http://www.isotc211.org/2005/srv" xmlns:gml="http://www.opengis.net/gml"
    xmlns:gts="http://www.isotc211.org/2005/gts" xmlns:che="http://www.geocat.ch/2008/che" xmlns:xlink="http://www.w3.org/1999/xlink" >


    <!-- Load labels. -->
    <xsl:variable name="label" select="/root/schemas/iso19139"/>
    <xsl:template xmlns:geonet="http://www.fao.org/geonetwork" mode="iso19139" match="geonet:info" />
    <!-- Root element matching.-->
    <xsl:template match="/" priority="5">
        <html>
            <!-- Set some vars.-->
            <xsl:variable name="title" select="/root/gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:title/gco:CharacterString"/>
            
            <head>
                <title>Metadata: <xsl:value-of select="$title"/></title>
            </head>
            <body>
                <h1>
                    <xsl:value-of select="$title"/>
                </h1>
                <xsl:apply-templates mode="iso19139" select="/root/gmd:MD_Metadata/*"/>
            </body>
        </html>
    </xsl:template>

    <!-- Box that one ... -->
    <xsl:template mode="iso19139"
        match="gmd:locale|gmd:contact|gmd:identificationInfo|gmd:descriptiveKeywords|
        gmd:spatialRepresentationInfo|gmd:pointOfContact|gmd:dataQualityInfo|gmd:referenceSystemInfo|
        gmd:equivalentScale|gmd:projection|gmd:ellipsoid|gmd:extent[name(..)!='gmd:EX_TemporalExtent']|
        gmd:geographicBox|gmd:EX_TemporalExtent|gmd:MD_Distributor|srv:containsOperations|
        gmd:featureCatalogueCitation|gmd:MD_LegalConstraints|gmd:MD_SecurityConstraints|gmd:MD_Constraints|
        gmd:MD_Resolution|gmd:MD_Format|srv:SV_CoupledResource|gmd:resourceMaintenance|gmd:resourceConstraints|
        gmd:spatialResolution|gmd:distributionFormat|gmd:transferOptions|gmd:distributionInfo">
        
        <xsl:variable name="name" select="name(.)"/>
        <xsl:variable name="title">
            <xsl:call-template name="getTitle">
                <xsl:with-param name="name" select="$name"/>
            </xsl:call-template>
        </xsl:variable>
        
        <fieldset>
            <legend>
                <h3>
                    <xsl:value-of select="$title"/>
                </h3>
            </legend>
            <ul>
            <xsl:apply-templates mode="iso19139"/>
            </ul>
        </fieldset>

    </xsl:template>

    <xsl:template mode="iso19139" match="*|@*">
        <xsl:variable name="name" select="name(.)"/>
        <xsl:variable name="title">
            <xsl:call-template name="getTitle">
                <xsl:with-param name="name" select="$name"/>
            </xsl:call-template>
        </xsl:variable>
        
        <b><xsl:value-of select="$title"/></b>
        <br/>
        
        <xsl:apply-templates mode="iso19139"/>
    </xsl:template>

    <!-- Display characterString ... -->
    <xsl:template mode="iso19139" match="gmd:*[gco:CharacterString or gmd:PT_FreeText]|
        srv:*[gco:CharacterString or gmd:PT_FreeText]|
        gco:aName[gco:CharacterString]" priority="2">
        <xsl:variable name="name" select="name(.)"/>
        <xsl:variable name="title">
            <xsl:call-template name="getTitle">
                <xsl:with-param name="name" select="$name"/>
            </xsl:call-template>
        </xsl:variable>
        
        <li><xsl:value-of select="$title"/>:<b><xsl:value-of select="gco:CharacterString"/></b></li>
        <!-- Here you could display translation using PT_FreeText -->
    </xsl:template>


    <!-- Get title from che profil if exist, if not default to iso.
    -->
    <xsl:template name="getTitle">
        <xsl:param name="name"/>
        <xsl:variable name="title" select="string($label/labels/element[@name=$name]/label)"/>
        <xsl:choose>
            <xsl:when test="normalize-space($title)"><xsl:value-of select="$title"/></xsl:when>
            <xsl:otherwise><xsl:value-of select="string($label/labels/element[@name=$name]/label)"/></xsl:otherwise>
        </xsl:choose>

    </xsl:template>


</xsl:stylesheet>