<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
    xmlns:dc="http://purl.org/dc/elements/1.1/"
    xmlns:dcterms="http://purl.org/dc/terms/"
    xmlns:ns3="http://www.opengis.net/gml#"
    xmlns:skos="http://www.w3.org/2004/02/skos/core#"
    xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#"
    exclude-result-prefixes="xs"
    version="2.0">
    
    <xsl:output method="text"/>
    
    <xsl:param name="targetLanguage" select="'fr'"/>
    
    <!-- Convert a SKOS local thesaurus in GeoNetwork 
         into a simple synonym map for Elasticsearch index configuration. -->
    <xsl:template match="/">
        <xsl:variable name="thesaurus"
                      select="."/>
        <xsl:for-each-group select="//(rdf:Description|skos:Concept)"
               group-by="@rdf:about">
            <xsl:for-each select="$thesaurus//(rdf:Description|skos:Concept)[@rdf:about=current-grouping-key()]/skos:prefLabel">
                <xsl:text>"</xsl:text>
                <xsl:value-of select="lower-case(.)"/> =&gt; <xsl:value-of select="lower-case($thesaurus//(rdf:Description|skos:Concept)[@rdf:about=current-grouping-key()]/skos:prefLabel[@xml:lang = $targetLanguage])"/>
                <xsl:text>",
                </xsl:text>    
            </xsl:for-each>            
        </xsl:for-each-group>
    </xsl:template>
</xsl:stylesheet>
