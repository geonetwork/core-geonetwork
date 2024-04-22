<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:gmd="http://www.isotc211.org/2005/gmd"
                xmlns:gco="http://www.isotc211.org/2005/gco"
                xmlns:dct="http://purl.org/dc/terms/"
                xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
                xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#"
                xmlns:spdx="http://spdx.org/rdf/terms#"
                xmlns:prov="http://www.w3.org/ns/prov#"
                xmlns:owl="http://www.w3.org/2002/07/owl#"
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                xmlns:adms="http://www.w3.org/ns/adms#"
                xmlns:dcat="http://www.w3.org/ns/dcat#"
                xmlns:locn="http://www.w3.org/ns/locn#"
                xmlns:gml="http://www.opengis.net/gml/3.2"
                xmlns:foaf="http://xmlns.com/foaf/0.1/"
                xmlns:vcard="http://www.w3.org/2006/vcard/ns#"
                xmlns:skos="http://www.w3.org/2004/02/skos/core#"
                xmlns:util="java:org.fao.geonet.util.XslUtil"
                xmlns:geonet="http://www.fao.org/geonetwork"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                exclude-result-prefixes="#all">

  <xsl:template name="contact-from-iso19139-to-foaf-agent">
    <foaf:Agent>
      <foaf:name><xsl:value-of select=".//gmd:organisationName/*/text()"/></foaf:name>
      <dct:type>
        <skos:Concept>
          <skos:prefLabel xml:lang="en"><xsl:value-of select=".//gmd:role/gmd:CI_RoleCode/@codeListValue" /></skos:prefLabel>
          <skos:inScheme rdf:resource="http://purl.org/adms/publishertype/1.0"/>
        </skos:Concept>
      </dct:type>
      <xsl:if test="string(.//gmd:contactInfo/*/gmd:phone/*/gmd:voice/gco:CharacterString)">
        <foaf:phone rdf:resource="tel:{.//gmd:contactInfo/*/gmd:phone/*/gmd:voice/gco:CharacterString}"/>
      </xsl:if>
      <xsl:choose>
        <xsl:when test="string(.//gmd:contactInfo/*/gmd:address/*/gmd:electronicMailAddress/gco:CharacterString)">
          <foaf:mbox rdf:resource="mailto:{.//gmd:contactInfo/*/gmd:address/*/gmd:electronicMailAddress/gco:CharacterString}"/>
        </xsl:when>
        <xsl:otherwise>
          <foaf:mbox rdf:resource=""/>
        </xsl:otherwise>
      </xsl:choose>
    </foaf:Agent>
  </xsl:template>

  <xsl:template name="contact-from-iso19139-to-vcard-org">
    <vcard:Organization>
      <vcard:fn/>
      <vcard:organization-name>
        <xsl:value-of select=".//gmd:organisationName/*/text()"/>
      </vcard:organization-name>
      <vcard:hasAddress>
        <vcard:Address>
          <vcard:street-address/>
          <vcard:locality/>
          <vcard:postal-code/>
          <vcard:country-name/>
        </vcard:Address>
      </vcard:hasAddress>
      <vcard:hasEmail rdf:resource=""/>
      <vcard:hasURL rdf:resource=""/>
      <vcard:hasTelephone/>
    </vcard:Organization>
  </xsl:template>
</xsl:stylesheet>
