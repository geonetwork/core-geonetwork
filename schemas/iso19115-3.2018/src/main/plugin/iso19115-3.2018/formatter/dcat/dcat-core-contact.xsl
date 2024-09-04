<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:cit="http://standards.iso.org/iso/19115/-3/cit/2.0"
                xmlns:mdb="http://standards.iso.org/iso/19115/-3/mdb/2.0"
                xmlns:mri="http://standards.iso.org/iso/19115/-3/mri/1.0"
                xmlns:mcc="http://standards.iso.org/iso/19115/-3/mcc/1.0"
                xmlns:gcx="http://standards.iso.org/iso/19115/-3/gcx/1.0"
                xmlns:gco="http://standards.iso.org/iso/19115/-3/gco/1.0"
                xmlns:skos="http://www.w3.org/2004/02/skos/core#"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                xmlns:prov="http://www.w3.org/ns/prov#"
                xmlns:dcat="http://www.w3.org/ns/dcat#"
                xmlns:geodcatap="http://data.europa.eu/930/"
                xmlns:dct="http://purl.org/dc/terms/"
                xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
                xmlns:foaf="http://xmlns.com/foaf/0.1/"
                xmlns:vcard="http://www.w3.org/2006/vcard/ns#"
                xmlns:org="http://www.w3.org/ns/org#"
                xmlns:gn-fn-dcat="http://geonetwork-opensource.org/xsl/functions/dcat"
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

    <xsl:variable name="allIndividualOrOrganisationWithoutIndividual"
                  select="*/cit:party//(cit:CI_Organisation[not(cit:individual)]|cit:CI_Individual)"
                  as="node()*"/>

    <xsl:choose>
      <xsl:when test="$dcatElementConfig">
        <xsl:for-each-group select="$allIndividualOrOrganisationWithoutIndividual" group-by="cit:name">
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
        <xsl:for-each-group select="$allIndividualOrOrganisationWithoutIndividual" group-by="cit:name">
          <prov:qualifiedAttribution>
            <prov:Attribution>
              <prov:agent>
                <xsl:call-template name="rdf-contact-foaf"/>
              </prov:agent>
              <dcat:hadRole>
                <dcat:Role rdf:about="{concat($isoCodeListBaseUri, $role)}">
                  <!--
                      Property needs to have at least 1 value
                      Location:
                      [Focus node] - [http://standards.iso.org/iso/19115/resources/Codelists/cat/codelists.xml#custodian] -
                      [Result path] - [http://www.w3.org/2004/02/skos/core#prefLabel]
                  -->
                  <skos:prefLabel><xsl:value-of select="$role"/></skos:prefLabel>
                </dcat:Role>
              </dcat:hadRole>
            </prov:Attribution>
          </prov:qualifiedAttribution>
        </xsl:for-each-group>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>


  <xsl:template name="rdf-contact-vcard">
    <xsl:variable name="isindividual"
                  as="xs:boolean"
                  select="local-name() = 'CI_Individual'"/>
    <xsl:variable name="individualName"
                  as="xs:string?"
                  select="if ($isindividual) then cit:name/*/text() else ''"/>
    <xsl:variable name="organisation"
                  as="node()?"
                  select="if ($isindividual) then ancestor::cit:CI_Organisation else ."/>

    <rdf:Description>
      <xsl:call-template name="rdf-object-ref-attribute"/>

      <rdf:type rdf:resource="http://www.w3.org/2006/vcard/ns#Organization"/>
      <xsl:if test="$individualName != ''">
        <vcard:fn><xsl:value-of select="$individualName"/></vcard:fn>
      </xsl:if>
      <xsl:for-each select="$organisation/cit:name">
        <vcard:org>
          <rdf:Description>
            <xsl:call-template name="rdf-localised">
              <xsl:with-param name="nodeName" select="'vcard:organisation-name'"/>
            </xsl:call-template>
          </rdf:Description>
        </vcard:org>
      </xsl:for-each>


      <xsl:variable name="contactInfo"
                    as="node()?"
                    select="if ($isindividual)
                               then $organisation/cit:contactInfo
                               else cit:contactInfo"/>

      <!-- Priority on the individual email and fallback on org email-->
      <xsl:for-each select="(cit:contactInfo/*/cit:address/*/cit:electronicMailAddress, $organisation/cit:contactInfo/*/cit:address/*/cit:electronicMailAddress)[1]">
        <vcard:hasEmail rdf:resource="mailto:{*/text()}"/>
      </xsl:for-each>

      <xsl:for-each select="$contactInfo/*/cit:address">
        <xsl:if test="normalize-space(*/cit:city) != ''">
          <vcard:hasAddress>
            <vcard:Address>
              <xsl:for-each select="*/cit:country">
                <xsl:call-template name="rdf-localised">
                  <xsl:with-param name="nodeName" select="'vcard:country-name'"/>
                </xsl:call-template>
              </xsl:for-each>
              <xsl:for-each select="*/cit:city">
                <xsl:call-template name="rdf-localised">
                  <xsl:with-param name="nodeName" select="'vcard:locality'"/>
                </xsl:call-template>
              </xsl:for-each>
              <xsl:for-each select="*/cit:postalCode">
                <xsl:call-template name="rdf-localised">
                  <xsl:with-param name="nodeName" select="'vcard:postal-code'"/>
                </xsl:call-template>
              </xsl:for-each>
              <xsl:for-each select="*/cit:administrativeArea">
                <xsl:call-template name="rdf-localised">
                  <xsl:with-param name="nodeName" select="'vcard:region'"/>
                </xsl:call-template>
              </xsl:for-each>
              <xsl:for-each select="*/cit:deliveryPoint">
                <xsl:call-template name="rdf-localised">
                  <xsl:with-param name="nodeName" select="'vcard:street-address'"/>
                </xsl:call-template>
              </xsl:for-each>
            </vcard:Address>
          </vcard:hasAddress>
        </xsl:if>
      </xsl:for-each>
    </rdf:Description>
  </xsl:template>


  <xsl:template name="rdf-contact-foaf">
    <xsl:variable name="isindividual"
                  as="xs:boolean"
                  select="local-name() = 'CI_Individual'"/>
    <xsl:variable name="individualName"
                  as="xs:string?"
                  select="if ($isindividual) then cit:name/*/text() else ''"/>
    <xsl:variable name="organisation"
                  as="node()?"
                  select="if ($isindividual) then ancestor::cit:CI_Organisation else ."/>
    <xsl:variable name="orgReference"
                  as="xs:string?"
                  select="gn-fn-dcat:rdf-object-ref($organisation)"/>
    <xsl:variable name="reference"
                  as="xs:string?"
                  select="if ($isindividual)
                               then gn-fn-dcat:rdf-object-ref(.)
                               else $orgReference"/>

    <rdf:Description>
      <xsl:call-template name="rdf-object-ref-attribute">
        <xsl:with-param name="reference" select="$reference"/>
      </xsl:call-template>
      <rdf:type rdf:resource="http://xmlns.com/foaf/0.1/{if ($isindividual) then 'Person' else 'Organization'}"/>
      <rdf:type rdf:resource="http://www.w3.org/ns/prov#Agent"/>

      <xsl:choose>
        <xsl:when test="$isindividual">
          <foaf:name><xsl:value-of select="$individualName"/></foaf:name>
          <org:memberOf>
            <xsl:for-each select="$organisation/cit:name">
              <foaf:Organization>
                <xsl:call-template name="rdf-object-ref-attribute">
                  <xsl:with-param name="reference" select="$reference"/>
                </xsl:call-template>
                <xsl:call-template name="rdf-localised">
                  <xsl:with-param name="nodeName" select="'foaf:name'"/>
                </xsl:call-template>
              </foaf:Organization>
            </xsl:for-each>
          </org:memberOf>
        </xsl:when>
        <xsl:otherwise>
          <xsl:for-each select="$organisation/cit:name">
            <xsl:call-template name="rdf-localised">
              <xsl:with-param name="nodeName" select="'foaf:name'"/>
            </xsl:call-template>
          </xsl:for-each>
        </xsl:otherwise>
      </xsl:choose>

      <xsl:variable name="contactInfo"
                    as="node()?"
                    select="if ($isindividual)
                               then $organisation/cit:contactInfo
                               else cit:contactInfo"/>

      <!-- Priority on the individual email and fallback on org email-->
      <xsl:for-each select="(cit:contactInfo/*/cit:address/*/cit:electronicMailAddress, $organisation/*/cit:address/*/cit:electronicMailAddress)[1]">
        <foaf:mbox rdf:resource="mailto:{*/text()}"/>
      </xsl:for-each>
      <xsl:for-each select="$contactInfo/*/cit:onlineResource/*/cit:linkage">
        <xsl:call-template name="rdf-localised">
          <xsl:with-param name="nodeName" select="'foaf:workplaceHomepage'"/>
        </xsl:call-template>
      </xsl:for-each>
      <!-- TODO: map other properties -->
    </rdf:Description>
  </xsl:template>
</xsl:stylesheet>
