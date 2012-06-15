<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:util="xalan://org.fao.geonet.util.XslUtil"
    xmlns:gmd="http://www.isotc211.org/2005/gmd"
    xmlns:gco="http://www.isotc211.org/2005/gco"
    xmlns:che="http://www.geocat.ch/2008/che"
    xmlns:srv="http://www.isotc211.org/2005/srv" >

	<xsl:template match="@*|node()|comment()">
		<xsl:copy>
            <xsl:apply-templates select="@*"/>

            <xsl:apply-templates select="comment()"/>

            <xsl:apply-templates select="gmd:citation"/>
            <xsl:apply-templates select="*[gmd:PT_FreeText or gco:CharacterString]"/>
            <xsl:apply-templates select="gmd:characterSet"/>
            <xsl:apply-templates select="gmd:topicCategory"/>
            <xsl:apply-templates select="gmd:descriptiveKeywords"/>
            <xsl:apply-templates select="che:modelType"/>
            <xsl:apply-templates select="gmd:spatialRepresentationType"/>
            <xsl:apply-templates select="*[ not(local-name(.) = 'topicCategory') and
                                            not(local-name(.) = 'citation') and
                                            not(local-name(.) = 'characterSet') and
                                            not(local-name(.) = 'modelType') and
                                            not(local-name(.) = 'descriptiveKeywords') and
                                            not(local-name(.) = 'spatialRepresentationType') and
                                            not(gmd:PT_FreeText) and
                                            not(gco:CharacterString)]"/>
            <xsl:apply-templates select="text()"/>

        </xsl:copy>
	</xsl:template>

</xsl:stylesheet>