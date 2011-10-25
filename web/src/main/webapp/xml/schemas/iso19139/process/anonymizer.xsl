<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:gmd="http://www.isotc211.org/2005/gmd" xmlns:gco="http://www.isotc211.org/2005/gco"
    xmlns:geonet="http://www.fao.org/geonetwork" exclude-result-prefixes="#all" version="2.0">
    
    <!-- Usage: 
        anonymizer?protocol=MYLOCALNETWORK:FILEPATH&email=gis@organisation.org&thesaurus=MYORGONLYTHEASURUS  
        * will remove MYLOCALNETWORK:FILEPATH gmd:onLine element
        * will replace all email ending with @organisation.org by gis@organisation.org
        * will remove all gmd:descriptiveKeywords having MYORGONLYTHEASURUS in their thesaurus name.
    -->
    
    <!-- Protocol name for which online resource must be removed -->
    <xsl:param name="protocol"/>
    <!-- Generic email to use for all email in same domain (ie. after @domain.org). -->
    <xsl:param name="email"/>
    <!-- Portion of thesaurus name for which keyword should be removed -->
    <xsl:param name="thesaurus"/>
    
    
    
    <xsl:variable name="emailDomain" select="substring-after($email, '@')"/>
    
    <!-- Remove individual name -->
    <xsl:template match="gmd:individualName" priority="2"/>
    
    <!-- Remove organisation email by general email -->
    <xsl:template match="gmd:electronicMailAddress[$emailDomain != '' and ends-with(gco:CharacterString, $emailDomain)]" priority="2">
        <xsl:copy>
            <gco:CharacterString><xsl:value-of select="$email"/></gco:CharacterString>
        </xsl:copy>
    </xsl:template>
    
    <!-- Remove all resources contact which are not pointOfContact -->
    <xsl:template match="gmd:identificationInfo/*/gmd:pointOfContact[gmd:CI_ResponsibleParty/gmd:role/gmd:CI_RoleCode/@codeListValue!='pointOfContact']" priority="2"/>
    
    <!-- Remove all online resource with custom protocol -->
    <xsl:template
        match="gmd:onLine[$protocol != '' and gmd:CI_OnlineResource/gmd:protocol/gco:CharacterString=$protocol]"
        priority="2"/>
    
    <!-- Remove all descriptive keyword with a thesaurus from $thesaurus -->
    <xsl:template match="gmd:descriptiveKeywords[$thesaurus != '' and contains(gmd:MD_Keywords/gmd:thesaurusName/gmd:CI_Citation/gmd:title/gco:CharacterString, $thesaurus)]" priority="2"/>

    <!-- Do a copy of every nodes and attributes -->
    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>

    <!-- Always remove geonet:* elements. -->
    <xsl:template match="geonet:*" priority="2"/>

</xsl:stylesheet>
