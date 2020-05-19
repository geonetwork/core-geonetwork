<xsl:stylesheet version="2.0" 
  xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#" 
  xmlns:skos="http://www.w3.org/2004/02/skos/core#" 
  xmlns:dc="http://purl.org/dc/terms/" 
  xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#" 
  xmlns:grg="http://www.isotc211.org/schemas/grg/" 
  xmlns:owl="http://www.w3.org/2002/07/owl#" 
  xmlns:void="http://rdfs.org/ns/void#"
  xmlns:gml="http://www.opengis.net/gml#" 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  exclude-result-prefixes="#all">

  <xsl:output encoding="UTF-8" indent="yes" method="xml"/>
  
  <xsl:template match="/">
    <xsl:apply-templates/>
  </xsl:template>

  <xsl:template match="skos:Collection">
    <xsl:copy copy-namespaces="yes">
      <xsl:apply-templates select="*[name() != 'skos:member']|@*"/>
      
      <skos:Concept rdf:about="http://vocab.nerc.ac.uk/collection/C19/current/#SeaVoXGazetteer">
        <skos:prefLabel xml:lang="en">SeaVoX salt and fresh water body gazetteer</skos:prefLabel>
        <skos:prefLabel xml:lang="es">SeaVoX salt and fresh water body gazetteer</skos:prefLabel>
        <skos:prefLabel xml:lang="fr">SeaVoX salt and fresh water body gazetteer</skos:prefLabel>
        <skos:inScheme rdf:resource="http://geonetwork-opensource.org/regions" />
      </skos:Concept>
      
      <xsl:apply-templates select="//skos:Concept"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="*|@*|text()">
    <xsl:copy>
      <xsl:apply-templates select="*|@*|text()"/>
    </xsl:copy>
  </xsl:template>
  
  <!-- 
  <skos:prefLabel xml:lang="en">
  -->
  <xsl:template match="skos:prefLabel[@xml:lang='en']" priority="2">
    <xsl:copy-of select="."/>
    <skos:prefLabel xml:lang="fr"><xsl:value-of select="text()"/></skos:prefLabel>
  </xsl:template>
  
  <!-- Convert JSON skos definition to 
  a GML geometry usable by GN thesaurus
  manager 
  
  {"Spatial_Coverage": {
  "Southernmost_latitude": "38.9318297296407",
  "Northernmost_latitude": "46.4480840347487",
  "Westernmost_longitude": "11.417223246963",
  "Easternmost_longitude": "20.8730094658656"
  }}
  -->
  <xsl:template match="skos:definition" priority="2">
    <xsl:analyze-string select="." 
      regex='^.*"(\-?\d+\.?\d*)".*"(\-?\d+\.?\d*)".*"(\-?\d+\.?\d*)".*"(\-?\d+\.?\d*)".*$'
      flags="s">
      <xsl:matching-substring>
        <gml:BoundedBy>
          <gml:Envelope 
            gml:srsName="http://www.opengis.net/gml/srs/epsg.xml#epsg:4326">
            <gml:lowerCorner>
              <xsl:value-of select="concat(regex-group(3), ' ', regex-group(1))"/>
            </gml:lowerCorner>
            <gml:upperCorner>
              <xsl:value-of select="concat(regex-group(4), ' ', regex-group(2))"/>
            </gml:upperCorner>
          </gml:Envelope>
        </gml:BoundedBy>
      </xsl:matching-substring>
    </xsl:analyze-string>
    <skos:broader rdf:resource="http://vocab.nerc.ac.uk/collection/C19/current/#SeaVoXGazetteer" />
    
  </xsl:template>
</xsl:stylesheet>