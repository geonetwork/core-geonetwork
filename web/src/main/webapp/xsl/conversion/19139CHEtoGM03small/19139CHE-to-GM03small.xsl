<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
                xmlns:che="http://www.geocat.ch/2008/che"
                xmlns:gco="http://www.isotc211.org/2005/gco"
                xmlns:gmd="http://www.isotc211.org/2005/gmd"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                xmlns:gml="http://www.opengis.net/gml"
                xmlns:GML="http://www.geocat.ch/2003/05/gateway/GML"
                xmlns:ch="http://www.geocat.ch/2003/05/gateway/GM03Small"
                exclude-result-prefixes="che gco gmd xsi gml">

    <xsl:variable name="defaultLanguage">
        <xsl:apply-templates mode="language" select="/che:CHE_MD_Metadata/gmd:language/gco:CharacterString"/>
    </xsl:variable>

    <xsl:template match="che:CHE_MD_Metadata|gmd:MD_Metadata">
        <ch:MD_Metadata objid="{gmd:fileIdentifier/gco:CharacterString}">
            <xsl:apply-templates mode="text" select="gmd:fileIdentifier"/>
            <xsl:apply-templates mode="text" select="gmd:dateStamp"/>
            <xsl:apply-templates mode="text" select="gmd:dataSetURI"/>
            <xsl:apply-templates mode="text" select="gmd:metadataSetURI"/>  <!-- TODO: eh? -->
            <xsl:apply-templates mode="ident" select="gmd:identificationInfo"/>
        </ch:MD_Metadata>
    </xsl:template>

    <xsl:template mode="ident" match="gmd:identificationInfo|gmd:citation|gmd:pointOfContact">
        <xsl:apply-templates mode="ident"/>
    </xsl:template>

    <xsl:template mode="ident" match="che:CHE_MD_DataIdentification|gmd:MD_DataIdentification">
        <ch:identificationInfo>
            <xsl:apply-templates mode="language" select="gmd:language"/>
            <xsl:apply-templates mode="text" select="gmd:purpose"/>
            <xsl:apply-templates mode="text" select="gmd:topicCategory"/>
            <xsl:apply-templates mode="text" select="gmd:abstract"/>
            <xsl:apply-templates mode="ident" select="gmd:citation"/>
            <xsl:apply-templates mode="ident" select="gmd:pointOfContact"/>
            <xsl:apply-templates mode="ident" select="gmd:descriptiveKeywords"/>
            <xsl:apply-templates mode="extent" select="gmd:extent"/>
        </ch:identificationInfo>
    </xsl:template>

    <xsl:template mode="extent" match="gmd:extent">        
        <xsl:apply-templates mode="extent"/>
    </xsl:template>
    
    <xsl:template mode="extent" match="gmd:EX_Extent">
        <ch:extent>
            <xsl:apply-templates mode="text" select="gmd:description"/>
            <xsl:for-each select="gmd:geographicElement">
                <ch:geographicElement>
                    <xsl:apply-templates mode="extent"/>
                </ch:geographicElement>
            </xsl:for-each>
        </ch:extent>
    </xsl:template>

    <xsl:template mode="extent" match="gmd:EX_GeographicDescription">
        <xsl:for-each select="gmd:geographicIdentifier/gmd:MD_Identifier">
            <ch:geographicIdentifier>
                <xsl:apply-templates mode="text" select="gmd:code"/>
            </ch:geographicIdentifier>
        </xsl:for-each>
    </xsl:template>

    <xsl:template mode="extent" match="gmd:EX_GeographicBoundingBox">
        <xsl:apply-templates mode="text" select="gmd:westBoundLongitude"/>
        <xsl:apply-templates mode="text" select="gmd:eastBoundLongitude"/>
        <xsl:apply-templates mode="text" select="gmd:southBoundLatitude"/>
        <xsl:apply-templates mode="text" select="gmd:northBoundLatitude"/>
    </xsl:template>

    <xsl:template mode="extent" match="gmd:EX_BoundingPolygon">
        <xsl:for-each select="gmd:polygon/gml:Polygon">
            <GML:Polygon srsName="EPSG:4326">
                <xsl:for-each select="gml:exterior">
                    <GML:exteriorRing>
                        <xsl:apply-templates mode="extent" select="gml:Ring/gml:curveMember/gml:LineString/gml:coordinates"/>
                    </GML:exteriorRing>
                </xsl:for-each>
                <xsl:for-each select="gml:interior">
                    <GML:interiorRing>
                        <xsl:apply-templates mode="extent" select="gml:Ring/gml:curveMember/gml:LineString/gml:coordinates"/>
                    </GML:interiorRing>
                </xsl:for-each>
            </GML:Polygon>
        </xsl:for-each>
    </xsl:template>

    <xsl:template mode="extent" match="gml:coordinates">
        <GML_COORDINATES decimal="{@decimal}" cs="{@cs}" ts="{@ts}">
            <xsl:value-of select="."/>  <!-- TODO: must be translated -->
        </GML_COORDINATES>
    </xsl:template>

    <xsl:template mode="extent" match="*">
        <ERROR>Unknown extent element <xsl:value-of select="local-name(.)"/></ERROR>
    </xsl:template>

    <xsl:template mode="ident" match="gmd:descriptiveKeywords">
        <ch:descriptiveKeywords>
            <xsl:for-each select="gmd:MD_Keywords">
                <xsl:apply-templates mode="text" select="gmd:keyword"/>
            </xsl:for-each>
        </ch:descriptiveKeywords>
    </xsl:template>

    <xsl:template mode="ident" match="gmd:pointOfContact">
        <xsl:apply-templates mode="ident"/>
    </xsl:template>

    <xsl:template mode="ident" match="che:CHE_CI_ResponsibleParty|gmd:CI_ResponsibleParty">
        <ch:pointOfContact>
            <xsl:apply-templates mode="text" select="gmd:role"/>
            <xsl:if test="che:individualFirstName|che:individualLastName">
                <ch:individualName><xsl:value-of select="che:individualFirstName/gco:CharacterString"/> <xsl:value-of select="che:individualLastName/gco:CharacterString"/></ch:individualName>
            </xsl:if>
            <xsl:if test="gmd:individualName">
                <xsl:apply-templates mode="text" select="gmd:individualName"/>
            </xsl:if>
            <xsl:apply-templates mode="text" select="gmd:positionName"/>
            <xsl:apply-templates mode="text" select="gmd:organisationName"/>
        </ch:pointOfContact>
    </xsl:template>

    <xsl:template mode="ident" match="gmd:CI_Citation">
        <ch:citation>
            <xsl:apply-templates mode="text" select="gmd:title"/>
            <xsl:apply-templates mode="ident" select="gmd:date"/>
        </ch:citation>
    </xsl:template>

    <xsl:template mode="ident" match="gmd:date">
        <xsl:apply-templates mode="ident"/>
    </xsl:template>

    <xsl:template mode="ident" match="gmd:CI_Date">
        <ch:date>
            <xsl:apply-templates mode="text" select="gmd:date"/>
            <xsl:apply-templates mode="text" select="gmd:dateType"/>
        </ch:date>
    </xsl:template>

    <xsl:template mode="ident" match="*">
        <ERROR>Unknown ident element <xsl:value-of select="local-name(.)"/></ERROR>
    </xsl:template>

    <xsl:template mode="language" match="gmd:language">
        <ch:language>
            <xsl:apply-templates mode="language"/>
        </ch:language>
    </xsl:template>

    <xsl:template mode="language" match="gco:CharacterString">
        <xsl:apply-templates mode="language"/>
    </xsl:template>

    <xsl:template mode="language" match="text()">
        <xsl:choose>
            <xsl:when test=".='deu'">de</xsl:when>
            <xsl:when test=".='ger'">de</xsl:when>
            <xsl:when test=".='fra'">fr</xsl:when>
            <xsl:when test=".='fre'">fr</xsl:when>
            <xsl:when test=".='ita'">it</xsl:when>
            <xsl:when test=".='eng'">en</xsl:when>
        </xsl:choose>
    </xsl:template>

    <xsl:template mode="language" match="*">
        <ERROR>Unknown language element <xsl:value-of select="local-name(.)"/></ERROR>
    </xsl:template>

    <xsl:template mode="text" match="*">
        <xsl:element name="ch:{local-name(.)}">
            <xsl:choose>
                <xsl:when test="@xsi:type='gmd:PT_FreeText_PropertyType'">
                    <xsl:for-each select="gco:CharacterString">
                        <ch:textGroup>
                            <ch:plainText><xsl:value-of select="."/></ch:plainText>
                            <ch:language><xsl:value-of select="$defaultLanguage"/></ch:language>
                        </ch:textGroup>
                    </xsl:for-each>
                    <xsl:for-each select="gmd:PT_FreeText/gmd:textGroup/gmd:LocalisedCharacterString">
                        <ch:textGroup>
                            <ch:plainText><xsl:value-of select="."/></ch:plainText>
                            <ch:language><xsl:value-of select="translate(substring(@locale, 2), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz')"/></ch:language>
                        </ch:textGroup>
                    </xsl:for-each>
                </xsl:when>
                <xsl:when test="gco:CharacterString"><xsl:value-of select="gco:CharacterString"/></xsl:when>
                <xsl:when test="gco:Date"><xsl:value-of select="gco:Date"/>T00:00:00+01:00</xsl:when>
                <xsl:when test="gco:DateTime"><xsl:value-of select="gco:DateTime"/>+01:00</xsl:when>
                <xsl:when test="gco:Decimal"><xsl:value-of select="gco:Decimal"/></xsl:when>
                <xsl:when test="gmd:MD_TopicCategoryCode"><xsl:value-of select="gmd:MD_TopicCategoryCode"/></xsl:when>
                <xsl:when test="*/@codeListValue"><xsl:value-of select="*/@codeListValue"/></xsl:when>
                <xsl:otherwise>
                    <ERROR>Unknow text element</ERROR>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:element>
    </xsl:template>
</xsl:stylesheet>