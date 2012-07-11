<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
    xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#" xmlns:exslt="http://exslt.org/common"
    xmlns:skos="http://www.w3.org/2004/02/skos/core#" xmlns:dc="http://purl.org/dc/elements/1.1/"
    xmlns:dcterms="http://purl.org/dc/terms/" xmlns:foaf="http://xmlns.com/foaf/0.1/"
    xmlns:gemet="http://www.eionet.europa.eu/gemet/2004/06/gemet-schema.rdf#">
    <!-- Convert GEMET Skos files into a simple skos structure
managed by GeoNetwork opensource.
-->

    <xsl:output method="xml" indent="yes"/>
        
    <xsl:variable name="debug" select="false()"/>
    <xsl:variable name="maxStringLength" select="150"/>
    <xsl:variable select="document('locales.xml')" name="locales"/>    
    <xsl:variable select="document('gemet-skoscore.rdf')" name="skoscore"/>

    <xsl:template name="header">
        <!-- Scheme -->
        <skos:ConceptScheme rdf:about="http://geonetwork-opensource.org/regions">
            <dc:title>GEMET</dc:title>
            <dc:description>GEMET thesaurus for GeoNetwork opensource.</dc:description>
            <dc:creator>
                <foaf:Organization>
                    <foaf:name>EEA</foaf:name>
                </foaf:Organization>
            </dc:creator>
            <dc:uri>http://www.eionet.europa.eu/gemet/about?langcode=en</dc:uri>
            <dc:rights>Free to all for non commercial use.</dc:rights>
            <dcterms:issued>Fry Sep 22 07:57:15 CEST 2006</dcterms:issued>
            <dcterms:modified>2006-09-22 07:57:15</dcterms:modified>
        </skos:ConceptScheme>
    </xsl:template>

    <xsl:template name="debug">
        <xsl:param name="msg"/>
        <xsl:param name="cd"/>

        <xsl:if test="$debug=true()">
            <xsl:message><xsl:value-of select="$cd"/>|<xsl:value-of select="$msg"/></xsl:message>
        </xsl:if>

    </xsl:template>

    <!-- Based on gemet-backbone.rdf -->
    <xsl:template match="/">
        <rdf:RDF>
            <xsl:call-template name="header"/>
            
            <!-- defined languages which needs to be imported from gemet-description-{lang}.rdf -->
            <xsl:variable name="lang">
                <lang>
                    <xsl:for-each select="$locales//locale">
                        <xsl:copy-of select="document(concat('gemet-definitions-', ., '.rdf'))"/>
                    </xsl:for-each>
                </lang>
            </xsl:variable>
            
            <xsl:for-each select="//rdf:Description">
                <xsl:variable name="id" select="@rdf:about"/>

                <xsl:call-template name="debug">
                    <xsl:with-param name="cd">000</xsl:with-param>
                    <xsl:with-param name="msg">Processing id:<xsl:value-of select="$id"
                        /></xsl:with-param>
                </xsl:call-template>


                <skos:Concept rdf:about="{$id}">
                    <!-- Load label and definition from language specific files.
                        Added a [1] to get first occurence of prefLabel and definition
                        as at least in english version, some concepts have an empty
                        prefLabel and duplicate definition !
                    -->
                    <xsl:for-each select="exslt:node-set($lang)/lang/rdf:RDF/rdf:Description[@rdf:about=$id]">
                        <xsl:variable name="l" select="../@xml:lang"/>
                        
                        <xsl:if test="normalize-space(skos:prefLabel)!='' and string-length(skos:prefLabel) &lt; $maxStringLength">
                            <xsl:element name="skos:prefLabel">
                                <xsl:attribute name="xml:lang"><xsl:value-of select="$l"></xsl:value-of></xsl:attribute>
                                <xsl:value-of select="skos:prefLabel"/>
                            </xsl:element> 
                        </xsl:if>
                        <xsl:if test="normalize-space(skos:definition)!=''">
                            <xsl:element name="skos:definition">
                                <xsl:attribute name="xml:lang"><xsl:value-of select="$l"></xsl:value-of></xsl:attribute>
                                <xsl:value-of select="skos:definition"/>
                            </xsl:element>                             
                        </xsl:if>
                    </xsl:for-each>

                    <!-- Loaded from gemet-skoscore.rdf -->
                    <xsl:for-each select="$skoscore//skos:Concept[@rdf:about=$id]">
                        <xsl:copy-of select="skos:broader|skos:narrower|skos:related"/>
                    </xsl:for-each>

                    <!-- Loaded from gemet-backbone.rdf -->
                    <xsl:copy-of select="gemet:theme|gemet:group"/>
                </skos:Concept>
            </xsl:for-each>
        </rdf:RDF>
    </xsl:template>
</xsl:stylesheet>
