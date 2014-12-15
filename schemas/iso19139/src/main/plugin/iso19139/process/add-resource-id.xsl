<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:exslt="http://exslt.org/common"
    xmlns:geonet="http://www.fao.org/geonetwork"
    xmlns:gco="http://www.isotc211.org/2005/gco"
    xmlns:gmd="http://www.isotc211.org/2005/gmd"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:gn-fn-iso19139="http://geonetwork-opensource.org/xsl/functions/profiles/iso19139"
    xmlns:util="java:org.fao.geonet.util.XslUtil"
    version="2.0" exclude-result-prefixes="#all">
    
    <xsl:import href="../../iso19139/process/process-utility.xsl"/>
    
    <!-- i18n information -->
    <xsl:variable name="add-resource-id-loc">
        <msg id="a" xml:lang="eng">Current record does not contain resource identifier. Add the following identifier: </msg>
        <msg id="a" xml:lang="fre">Cette fiche ne contient pas d'identifiant pour la ressource. Ajouter l'identifiant suivant :</msg>
    </xsl:variable>
    
    
    <xsl:variable name="resource-id-url-prefix" select="util:getSettingValue('metadata/resourceIdentifierPrefix')"/>


  <xsl:template name="list-add-resource-id">
        <suggestion process="add-resource-id"/>
    </xsl:template>
    
    
    
    <!-- Analyze the metadata record and return available suggestion
      for that process -->
    <xsl:template name="analyze-add-resource-id">
        <xsl:param name="root"/>
        <xsl:variable name="hasResourceId"
            select="count($root//gmd:identificationInfo/*/gmd:citation/
            gmd:CI_Citation/gmd:identifier/*/gmd:code[gco:CharacterString != '']) > 0"/>

      <xsl:variable name="code" select="gn-fn-iso19139:resource-id-generate($root/*/gmd:fileIdentifier/gco:CharacterString)"/>
        <xsl:if test="not($hasResourceId)">
            <suggestion process="add-resource-id" id="{generate-id()}" category="identification" target="identification">
                <name><xsl:value-of select="geonet:i18n($add-resource-id-loc, 'a', $guiLang)"/><xsl:text> </xsl:text><xsl:value-of select="$code"/>.</name>
                <operational>true</operational>
            </suggestion>
        </xsl:if>
        
    </xsl:template>
    
    
    
    
    <!-- Do a copy of every nodes and attributes -->
    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>
    
    <!-- Remove geonet:* elements. -->
    <xsl:template match="geonet:*" priority="2"/>

    <xsl:function name="gn-fn-iso19139:resource-id-generate" as="xs:string">
      <xsl:param name="fileIdentifier" as="xs:string"/>

      <!-- Create resource identifier based on metadata record identifier -->
      <xsl:variable name="urlWithoutLang" select="substring-before($catalogUrl, $nodeId)"/>
      <xsl:variable name="prefix" select="if ($resource-id-url-prefix != '') then $resource-id-url-prefix else $urlWithoutLang"/>
      <xsl:value-of select="concat($prefix, $fileIdentifier)"/>
    </xsl:function>

    <xsl:template
        match="gmd:identificationInfo/*/gmd:citation/
        gmd:CI_Citation"
        priority="2">

      <xsl:copy>
            <xsl:copy-of select="@*"/>
            <xsl:copy-of
                select="gmd:title|
                gmd:alternateTitle|
                gmd:date|
                gmd:edition|
                gmd:editionDate"/>
            
            <xsl:variable name="code" select="gn-fn-iso19139:resource-id-generate(/*/gmd:fileIdentifier/gco:CharacterString)"/>
            <xsl:copy-of
                select="gmd:identifier[gmd:MD_Identifier/gmd:code/gco:CharacterString != $code]"/>
            <gmd:identifier>
              <gmd:MD_Identifier>
                <gmd:code>
                  <gco:CharacterString><xsl:value-of select="$code"/></gco:CharacterString>
                </gmd:code>
              </gmd:MD_Identifier>
            </gmd:identifier>
            
            <xsl:copy-of
                select="gmd:citedResponsibleParty|
                gmd:presentationForm|
                gmd:series|
                gmd:otherCitationDetails|
                gmd:collectiveTitle|
                gmd:ISBN|
                gmd:ISSN"/>
            
        </xsl:copy>
    </xsl:template>
    
</xsl:stylesheet>
