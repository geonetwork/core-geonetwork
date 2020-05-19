<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#" xmlns:skos="http://www.w3.org/2004/02/skos/core#"
    xmlns:dc="http://purl.org/dc/terms/" xmlns:dce="http://purl.org/dc/elements/1.1/"
    xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#" xmlns:grg="http://www.isotc211.org/schemas/grg/"
    xmlns:owl="http://www.w3.org/2002/07/owl#" xmlns:void="http://rdfs.org/ns/void#"
    exclude-result-prefixes="xs"
    version="2.0">
    
    <xsl:template match="skos:member[
            not(starts-with(skos:Concept/@rdf:about, 
            'http://vocab.nerc.ac.uk/collection/L05/current/POS'))]"/>
    
    <!-- Do a copy of every nodes and attributes -->
    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>
</xsl:stylesheet>