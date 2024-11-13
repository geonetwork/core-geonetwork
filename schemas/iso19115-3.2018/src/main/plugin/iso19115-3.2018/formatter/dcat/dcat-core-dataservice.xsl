<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                xmlns:cit="http://standards.iso.org/iso/19115/-3/cit/2.0"
                xmlns:srv="http://standards.iso.org/iso/19115/-3/srv/2.1"
                xmlns:gco="http://standards.iso.org/iso/19115/-3/gco/1.0"
                xmlns:dcat="http://www.w3.org/ns/dcat#"
                xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
                exclude-result-prefixes="#all">

  <xsl:variable name="endpointDescriptionProtocols"
                as="xs:string*"
                select="('OpenAPI', 'GetCapabilities')"/>
  <!--
  RDF Property:	dcat:endpointURL
  Definition:	The root location or primary endpoint of the service (a Web-resolvable IRI).
  Domain:	dcat:DataService
  Range:	rdfs:Resource
  -->
  <xsl:template mode="iso19115-3-to-dcat"
                match="srv:containsOperations/*/srv:connectPoint/*[not(cit:protocol/*/text() = $endpointDescriptionProtocols)]/cit:linkage">
    <dcat:endpointURL rdf:resource="{normalize-space(gco:CharacterString/text())}"/>
  </xsl:template>


  <!--
  RDF Property:	dcat:endpointDescription
  Definition:	A description of the services available via the end-points, including their operations, parameters etc.
  Domain:	dcat:DataService
  Range:	rdfs:Resource
  Usage note:	The endpoint description gives specific details of the actual endpoint instances, while dcterms:conformsTo is used to indicate the general standard or specification that the endpoints implement.
  Usage note:	An endpoint description may be expressed in a machine-readable form, such as an OpenAPI (Swagger) description [OpenAPI], an OGC GetCapabilities response [WFS], [ISO-19142], [WMS], [ISO-19128], a SPARQL Service Description [SPARQL11-SERVICE-DESCRIPTION], an [OpenSearch] or [WSDL20] document, a Hydra API description [HYDRA], else in text or some other informal mode if a formal representation is not possible.
  -->
  <xsl:template mode="iso19115-3-to-dcat"
                match="srv:containsOperations/*/srv:connectPoint/*[cit:protocol/*/text() = $endpointDescriptionProtocols]/cit:linkage">
    <dcat:endpointDescription rdf:resource="{normalize-space(gco:CharacterString/text())}"/>
  </xsl:template>

  <!--
  RDF Property:	dcat:servesDataset
  Definition:	A collection of data that this data service can distribute.
  Range:	dcat:Dataset
  -->
  <xsl:template mode="iso19115-3-to-dcat"
                match="srv:operatesOn">
    <dcat:servesDataset>
      <dcat:Dataset rdf:about="{if (@xlink:href) then @xlink:href else @uriref}"/>
    </dcat:servesDataset>
  </xsl:template>
</xsl:stylesheet>
