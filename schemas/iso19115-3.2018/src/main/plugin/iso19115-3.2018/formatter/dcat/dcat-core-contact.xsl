<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:cit="http://standards.iso.org/iso/19115/-3/cit/2.0"
                xmlns:mdb="http://standards.iso.org/iso/19115/-3/mdb/2.0"
                xmlns:mri="http://standards.iso.org/iso/19115/-3/mri/1.0"
                xmlns:prov="http://www.w3.org/ns/prov#"
                xmlns:dcat="http://www.w3.org/ns/dcat#"
                xmlns:geodcatap="http://data.europa.eu/930/"
                xmlns:dct="http://purl.org/dc/terms/"
                xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
                xmlns:foaf="http://xmlns.com/foaf/0.1/"
                xmlns:vcard="http://www.w3.org/2006/vcard/ns#"
                xmlns:org="http://www.w3.org/ns/org#"
                exclude-result-prefixes="#all">

  <!--
  RDF Property:	dcat:contactPoint
  Definition:	Relevant contact information for the cataloged resource. Use of vCard is recommended [VCARD-RDF].
  Range:	vcard:Kind

  RDF Property:	dcterms:creator
  Definition:	The entity responsible for producing the resource.
  Range:	foaf:Agent
  Usage note:	Resources of type foaf:Agent are recommended as values for this property.

  RDF Property:	dcterms:publisher
  Definition:	The entity responsible for making the resource available.
  Usage note:	Resources of type foaf:Agent are recommended as values for this property.
  -->
  <xsl:template mode="iso19115-3-to-dcat"
                name="iso19115-3-to-dcat-agent"
                match="*[cit:CI_Responsibility]">
    <xsl:variable name="role"
                  as="xs:string?"
                  select="*/cit:role/*/@codeListValue"/>
    <xsl:variable name="dcatElementConfig"
                  as="node()?"
                  select="$isoContactRoleToDcatCommonNames[. = $role]"/>

    <xsl:choose>
      <xsl:when test="$dcatElementConfig">
        <xsl:for-each-group select="*/cit:party/cit:CI_Organisation" group-by="cit:name">
          <xsl:element name="{$dcatElementConfig/@key}">
            <xsl:choose>
              <xsl:when test="$dcatElementConfig/@as = 'vcard'">
                <xsl:call-template name="rdf-contact-vcard"/>
              </xsl:when>
              <xsl:otherwise>
                <xsl:call-template name="rdf-contact-foaf"/>
              </xsl:otherwise>
            </xsl:choose>
          </xsl:element>
        </xsl:for-each-group>
      </xsl:when>
      <xsl:otherwise>
        <!--
          RDF Property:	prov:qualifiedAttribution
          Definition:	Link to an Agent having some form of responsibility for the resource
          Sub-property of: prov:qualifiedInfluence
          Domain:	prov:Entity
          Range: prov:Attribution
          Usage note:	Used to link to an Agent where the nature of the relationship is known but does not match one of the standard [DCTERMS] properties (dcterms:creator, dcterms:publisher). Use dcat:hadRole on the prov:Attribution to capture the responsibility of the Agent with respect to the Resource. See 15.1 Relationships between datasets and agents for usage examples.
        -->
        <xsl:for-each-group select="*/cit:party/cit:CI_Organisation" group-by="cit:name">
          <prov:qualifiedAttribution>
            <prov:Attribution>
              <prov:agent>
                <xsl:call-template name="rdf-contact-foaf"/>
              </prov:agent>
              <dcat:hadRole>
                <dcat:Role rdf:about="{concat($isoCodeListBaseUri, $role)}"/>
              </dcat:hadRole>
            </prov:Attribution>
          </prov:qualifiedAttribution>
        </xsl:for-each-group>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>


  <xsl:template name="rdf-contact-vcard">
    <rdf:Description>
      <rdf:type rdf:resource="http://www.w3.org/2006/vcard/ns#Organization"/>
      <xsl:for-each select="cit:name">
        <xsl:call-template name="rdf-localised">
          <xsl:with-param name="nodeName" select="'vcard:fn'"/>
        </xsl:call-template>
      </xsl:for-each>
      <xsl:for-each select="cit:contactInfo/*/cit:address/*/cit:electronicMailAddress">
        <vcard:hasEmail rdf:resource="mailto:{*/text()}"/>
      </xsl:for-each>
      <!-- TODO: map other properties
      vcard:hasAddress [ a vcard:Address ;
          vcard:country-name "Denmark" ;
          vcard:locality "Copenhagen" ;
          vcard:postal-code "1050" ;
          vcard:region "K" ;
          vcard:street-address "Kongens Nytorv 6" ] ;
      -->
    </rdf:Description>
  </xsl:template>


  <xsl:template name="rdf-contact-foaf">
    <xsl:variable name="individualName"
                  as="xs:string?"
                  select="(cit:individual/*/cit:name/*/text())[1]"/>
    <xsl:variable name="isindividual"
                  as="xs:boolean"
                  select="$individualName != ''"/>
    <xsl:variable name="organisation"
                  as="node()?"
                  select="cit:name"/>

    <rdf:Description>
      <rdf:type rdf:resource="http://xmlns.com/foaf/0.1/{if ($isindividual) then 'Person' else 'Organization'}"/>
      <rdf:type rdf:resource="http://www.w3.org/ns/prov#Agent"/>

      <xsl:choose>
        <xsl:when test="$isindividual">
          <foaf:name><xsl:value-of select="$individualName"/></foaf:name>
          <org:memberOf>
            <xsl:for-each select="$organisation">
              <foaf:Organization>
                <xsl:call-template name="rdf-localised">
                  <xsl:with-param name="nodeName" select="'foaf:name'"/>
                </xsl:call-template>
              </foaf:Organization>
            </xsl:for-each>
          </org:memberOf>
        </xsl:when>
        <xsl:otherwise>
          <xsl:for-each select="$organisation">
            <xsl:call-template name="rdf-localised">
              <xsl:with-param name="nodeName" select="'foaf:name'"/>
            </xsl:call-template>
          </xsl:for-each>
        </xsl:otherwise>
      </xsl:choose>


      <xsl:for-each select="cit:contactInfo/*/cit:address/*/cit:electronicMailAddress">
        <foaf:mbox rdf:resource="mailto:{*/text()}"/>
      </xsl:for-each>
      <xsl:for-each select="cit:contactInfo/*/cit:onlineResource/*/cit:linkage">
        <xsl:call-template name="rdf-localised">
          <xsl:with-param name="nodeName" select="'foaf:workplaceHomepage'"/>
        </xsl:call-template>
      </xsl:for-each>
      <!-- TODO: map other properties -->
    </rdf:Description>
  </xsl:template>
</xsl:stylesheet>
