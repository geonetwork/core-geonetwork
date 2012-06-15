<?xml version="1.0" encoding="UTF-8"?>

	<!--
		Extracts the information from iso19139 snippets for ReusableObjManager
		strategies when adding new reusable objects
	-->

<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fo="http://www.w3.org/1999/XSL/Format"
	xmlns:che="http://www.geocat.ch/2008/che" xmlns:gco="http://www.isotc211.org/2005/gco"
	xmlns:gml="http://www.opengis.net/gml" xmlns:gmd="http://www.isotc211.org/2005/gmd">

    <xsl:output indent="yes" method="xml"/>

	<xsl:template match="/">
		<data>
			<xsl:apply-templates />
		</data>
	</xsl:template>

	<!-- ContactInfo data -->
	<xsl:template match="//gmd:organisationName">
		<orgName>
			<xsl:copy-of select="." />
		</orgName>
	</xsl:template>
    <xsl:template match="//gmd:voice">
        <xsl:for-each select=".//gco:CharacterString">
           <xsl:variable name="elemName">voice<xsl:value-of select="count(.. | ../preceding-sibling::gmd:voice)"/></xsl:variable>
           <xsl:element name="{$elemName}">
               <xsl:value-of select="." />
           </xsl:element>
        </xsl:for-each>
    </xsl:template>
    <xsl:template match="//gmd:facsimile">
        <xsl:for-each select=".//gco:CharacterString">
           <xsl:variable name="elemName">facsimile<xsl:value-of select="count(.. | ../preceding-sibling::gmd:facsimile)"/></xsl:variable>
           <xsl:element name="{$elemName}">
               <xsl:value-of select="." />
           </xsl:element>
        </xsl:for-each>
    </xsl:template>
    <xsl:template match="//gmd:directNumber/gco:CharacterString | //che:directNumber/gco:CharacterString">
        <directNumber>
            <xsl:value-of select="." />
        </directNumber>
    </xsl:template>   
    <xsl:template match="//gmd:mobile/gco:CharacterString|//che:mobile/gco:CharacterString">
        <mobile>
            <xsl:value-of select="." />
        </mobile>
    </xsl:template>
    <xsl:template match="//gmd:CI_RoleCode">
        <role>
            <xsl:value-of select="@codeListValue" />
        </role>
    </xsl:template>

    <xsl:template match="//gmd:city/gco:CharacterString">
        <city>
            <xsl:value-of select="." />
        </city>
    </xsl:template>
    
    <xsl:template match="//che:streetName/gco:CharacterString">
        <streetName>
            <xsl:value-of select="." />
        </streetName>
    </xsl:template>
    
    <xsl:template match="//che:streetNumber/gco:CharacterString">
        <streetNumber>
            <xsl:value-of select="." />
        </streetNumber>
    </xsl:template>
    
    <xsl:template match="//che:postBox/gco:CharacterString">
        <postBox>
            <xsl:value-of select="." />
        </postBox>
    </xsl:template>

    <xsl:template match="//che:addressLine/gco:CharacterString">
        <addressLine>
            <xsl:value-of select="." />
        </addressLine>
    </xsl:template>

    
    <xsl:template match="//gmd:country/gco:CharacterString">
        <country>
            <xsl:value-of select="." />
        </country>
    </xsl:template>

    <xsl:template match="//gmd:administrativeArea/gco:CharacterString">
        <adminArea>
            <xsl:value-of select="." />
        </adminArea>
    </xsl:template>

	<xsl:template match="//gmd:postalCode/gco:CharacterString">
		<postalCode>
			<xsl:value-of select="." />
		</postalCode>
	</xsl:template>

    <xsl:template match="//gmd:electronicMailAddress">
        <xsl:for-each select=".//gco:CharacterString">
           <xsl:variable name="elemName">email<xsl:value-of select="count(.. | ../preceding-sibling::gmd:electronicMailAddress)"/></xsl:variable>
           <xsl:element name="{$elemName}">
               <xsl:value-of select="." />
           </xsl:element>
        </xsl:for-each>
    </xsl:template>
    
    <xsl:template match="//gmd:hoursOfService/gco:CharacterString">
        <hoursOfService>
            <xsl:value-of select="." />
        </hoursOfService>
    </xsl:template>    
    <xsl:template match="//che:CHE_CI_ResponsibleParty/che:parentResponsibleParty">
        <parentInfo>
            <xsl:copy-of select="che:CHE_CI_ResponsibleParty" />
        </parentInfo>
    </xsl:template>

    <xsl:template match="//gmd:contactInstructions/gco:CharacterString">
        <contactInstructions>
            <xsl:value-of select="." />
        </contactInstructions>
    </xsl:template>

    <xsl:template match="//gmd:positionName">
        <position>
            <xsl:copy-of select="." />
        </position>
    </xsl:template>

    <xsl:template
        match="//gmd:CI_OnlineResource/gmd:linkage">
        <online>
            <xsl:copy-of select="." />
        </online>
    </xsl:template>


    <xsl:template match="//che:individualFirstName/gco:CharacterString">
        <firstName>
            <xsl:value-of select="." />
        </firstName>
    </xsl:template>
    
    <xsl:template match="//che:individualLastName/gco:CharacterString">
        <lastName>
            <xsl:value-of select="." />
        </lastName>
    </xsl:template>
    <xsl:template match="//che:organisationAcronym">
        <acronym>
            <xsl:copy-of select="." />
        </acronym>
    </xsl:template>

    <xsl:template match="//gmd:name">
        <name>
            <xsl:copy-of select="." />
        </name>
    </xsl:template>

	<!-- Extent info (and online resource)-->
    <xsl:template match="//gmd:description">
        <desc>
            <xsl:copy-of select="." />
        </desc>
    </xsl:template>    
    <xsl:template match="//gmd:geographicIdentifier">
        <geoId>
            <xsl:copy-of select="." />
        </geoId>
    </xsl:template>
    <xsl:template match="//gmd:geographicIdentifier//gmd:LocalisedCharacterString">
        <geoId>
             <xsl:value-of select="." />
        </geoId>
    </xsl:template>
    <xsl:template match="//gmd:extentTypeCode//gco:Boolean">
        <extentTypeCode>
             <xsl:value-of select="." />
        </extentTypeCode>
    </xsl:template>


    <!-- Keywords info -->
    <xsl:template match="//gmd:keyword//gco:CharacterString">
        <xsl:element name="keyword">
            <xsl:value-of select="." />
        </xsl:element>
    </xsl:template>
    <xsl:template match="//gmd:keyword//gmd:LocalisedCharacterString">
        <xsl:element name="keyword">
            <xsl:attribute name="locale"><xsl:value-of select="substring(@locale,2)" /></xsl:attribute>
            <xsl:value-of select="." />
        </xsl:element>
    </xsl:template>

    <!-- Format info -->
    <xsl:template match="//gmd:MD_Format">
        <xsl:element name="format">
            <xsl:attribute name="name">
            <xsl:value-of select="gmd:name/gco:CharacterString" />
        </xsl:attribute>
            <xsl:if test="gmd:version">
                <xsl:attribute name="version">
                <xsl:value-of select="gmd:version/gco:CharacterString" />
            </xsl:attribute>
            </xsl:if>
        </xsl:element>
    </xsl:template>



	<xsl:template match="text()">
	</xsl:template>


</xsl:stylesheet>