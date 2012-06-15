<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
	xmlns:gemet="http://www.eionet.europa.eu/gemet/2004/06/gemet-schema.rdf#"
	xmlns:foaf="http://xmlns.com/foaf/0.1/" xmlns:dcterms="http://purl.org/dc/terms/"
	xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:skos="http://www.w3.org/2004/02/skos/core#"
	xmlns:exslt="http://exslt.org/common" exclude-result-prefixes="exslt gemet">

    <xsl:template match="skos:Concept">
       <rdf:Description>
           <xsl:apply-templates select="@*" />
            <rdf:type rdf:resource="http://www.w3.org/2004/02/skos/core#Concept" />
           <xsl:apply-templates select="./*"/>
       </rdf:Description>
    </xsl:template>

    <xsl:template match="gemet:*"/>
    <xsl:template match="skos:Concept/rdf:type"/>

	<xsl:template name="copy" match="@*|node()">
		<xsl:copy>
			<xsl:apply-templates select="@*" />
			<xsl:apply-templates />
		</xsl:copy>
    </xsl:template> 
	
</xsl:stylesheet>