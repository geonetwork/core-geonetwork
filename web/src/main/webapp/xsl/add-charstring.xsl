<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
						    xmlns:gmd="http://www.isotc211.org/2005/gmd"
						    xmlns:gco="http://www.isotc211.org/2005/gco"
						    xmlns:gmx="http://www.isotc211.org/2005/gmx"
						    xmlns:gts="http://www.isotc211.org/2005/gts"
						    xmlns:srv="http://www.isotc211.org/2005/srv"
						    xmlns:gml="http://www.opengis.net/gml"
						    xmlns:che="http://www.geocat.ch/2008/che"
						    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"    
						    xmlns:xlink="http://www.w3.org/1999/xlink"
						    xmlns:java="java:org.fao.geonet.util.XslUtil"
						    xmlns:geonet="http://www.fao.org/geonetwork"
						    xmlns:xalan = "http://xml.apache.org/xalan"
						    exclude-result-prefixes="#all">

	<xsl:template match="@*|node()">
		<xsl:copy>
			<xsl:apply-templates select="@*|node()" />
		</xsl:copy>
	</xsl:template>
	
	<xsl:template priority="5" match="*[gmd:PT_FreeText]">
	    <xsl:variable name="mainLang">
	       <xsl:call-template name="langId19139"/>
	    </xsl:variable>
		<xsl:copy>
			<xsl:attribute name="xsi:type">gmd:PT_FreeText_PropertyType</xsl:attribute>
			<xsl:choose>
				<xsl:when
					test="gmd:PT_FreeText/gmd:textGroup/gmd:LocalisedCharacterString[@locale = concat('#',$mainLang)]">
					<gco:CharacterString>
						<xsl:value-of
							select="gmd:PT_FreeText/gmd:textGroup/gmd:LocalisedCharacterString[@locale = concat('#',$mainLang)]" />
					</gco:CharacterString>
				</xsl:when>
				<xsl:otherwise>
					<xsl:attribute name="gco:nilReason">missing</xsl:attribute>
					<gco:CharacterString />
				</xsl:otherwise>
			</xsl:choose>
			<xsl:copy-of select="gmd:PT_FreeText"/>
		</xsl:copy>
	</xsl:template>


	<xsl:template name="langId19139">
        <xsl:variable name="tmp">
            <xsl:choose>
                <xsl:when test="/*[name(.)='gmd:MD_Metadata' or @gco:isoType='gmd:MD_Metadata']/gmd:language/gco:CharacterString|
                                /*[name(.)='gmd:MD_Metadata' or @gco:isoType='gmd:MD_Metadata']/gmd:language/gmd:LanguageCode/@codeListValue">
                    <xsl:value-of select="/*[name(.)='gmd:MD_Metadata' or @gco:isoType='gmd:MD_Metadata']/gmd:language/gco:CharacterString|
                                /*[name(.)='gmd:MD_Metadata' or @gco:isoType='gmd:MD_Metadata']/gmd:language/gmd:LanguageCode/@codeListValue"/>
                </xsl:when>
                <xsl:otherwise>en</xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        
        <xsl:value-of select="upper-case(java:twoCharLangCode($tmp))"></xsl:value-of>
    </xsl:template>
	
</xsl:stylesheet>