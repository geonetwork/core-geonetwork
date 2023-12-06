<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:cit="http://standards.iso.org/iso/19115/-3/cit/2.0"
                xmlns:dqm="http://standards.iso.org/iso/19157/-2/dqm/1.0"
                xmlns:gco="http://standards.iso.org/iso/19115/-3/gco/1.0"
                xmlns:cat="http://standards.iso.org/iso/19115/-3/cat/1.0"
                xmlns:lan="http://standards.iso.org/iso/19115/-3/lan/1.0"
                xmlns:mcc="http://standards.iso.org/iso/19115/-3/mcc/1.0"
                xmlns:mrc="http://standards.iso.org/iso/19115/-3/mrc/2.0"
                xmlns:mco="http://standards.iso.org/iso/19115/-3/mco/1.0"
                xmlns:mdb="http://standards.iso.org/iso/19115/-3/mdb/2.0"
                xmlns:reg="http://standards.iso.org/iso/19115/-3/reg/1.0"
                xmlns:mri="http://standards.iso.org/iso/19115/-3/mri/1.0"
                xmlns:mrs="http://standards.iso.org/iso/19115/-3/mrs/1.0"
                xmlns:mrl="http://standards.iso.org/iso/19115/-3/mrl/2.0"
                xmlns:mex="http://standards.iso.org/iso/19115/-3/mex/1.0"
                xmlns:msr="http://standards.iso.org/iso/19115/-3/msr/2.0"
                xmlns:mrd="http://standards.iso.org/iso/19115/-3/mrd/1.0"
                xmlns:mdq="http://standards.iso.org/iso/19157/-2/mdq/1.0"
                xmlns:srv="http://standards.iso.org/iso/19115/-3/srv/2.1"
                xmlns:gcx="http://standards.iso.org/iso/19115/-3/gcx/1.0"
                xmlns:gex="http://standards.iso.org/iso/19115/-3/gex/1.0"
                xmlns:gml="http://www.opengis.net/gml/3.2"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
                xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#"
                xmlns:owl="http://www.w3.org/2002/07/owl#"
                xmlns:foaf="http://xmlns.com/foaf/0.1/"
                xmlns:vcard="http://www.w3.org/2006/vcard/ns#"
                xmlns:dct="http://purl.org/dc/terms/"
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
                match="mdb:identificationInfo/*/mri:pointOfContact">
    <xsl:variable name="role"
                  as="xs:string?"
                  select="*/cit:role/*/@codeListValue"/>
    <xsl:variable name="dcatElementConfig"
                  as="node()?"
                  select="$isoContactRoleToDcatCommonNames[. = $role]"/>


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
      <!-- TODO: map other properties -->
    </rdf:Description>
  </xsl:template>


  <xsl:template name="rdf-contact-foaf">
    <rdf:Description>
      <rdf:type rdf:resource="http://xmlns.com/foaf/0.1/Organization"/>
      <rdf:type rdf:resource="http://www.w3.org/ns/prov#Agent"/>
      <xsl:for-each select="cit:name">
        <xsl:call-template name="rdf-localised">
          <xsl:with-param name="nodeName" select="'foaf:name'"/>
        </xsl:call-template>
      </xsl:for-each>
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
