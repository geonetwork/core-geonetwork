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
                xmlns:dct="http://purl.org/dc/terms/"
                xmlns:skos="http://www.w3.org/2004/02/skos/core#"
                exclude-result-prefixes="#all">

  <xsl:variable name="euLicenses"
                select="document('vocabularies/licences-skos.rdf')"/>

  <xsl:variable name="isMappingResourceConstraintsToEuVocabulary"
                as="xs:boolean"
                select="false()"/>

  <!--
  RDF Property:	dcterms:accessRights
  Definition:	Information about who can access the resource or an indication of its security status.
  Range: dcterms:RightsStatement
  = First constraints of mdb:identificationInfo/*/mri:resourceConstraints/*[mco:accessConstraints] (then dct:rights)

  RDF Property:	dcterms:license
  Definition:	A legal document under which the resource is made available.
  Range:	dcterms:LicenseDocument
  Usage note:	Information about licenses and rights MAY be provided for the Resource. See also guidance at 9. License and rights statements.
  = First useLimitation or constraints of mdb:identificationInfo/*/mri:resourceConstraints/*[mco:useConstraints] (then dct:rights)

  RDF Property:	dcterms:rights
  Definition:	A statement that concerns all rights not addressed with dcterms:license or dcterms:accessRights, such as copyright statements.
  Range:	dcterms:RightsStatement
  Usage note:	Information about licenses and rights MAY be provided for the Resource. See also guidance at 9. License and rights statements.
  -->
  <xsl:template mode="iso19115-3-to-dcat"
                match="mdb:identificationInfo/*/mri:resourceConstraints/*">
    <xsl:if test="count(../preceding-sibling::mri:resourceConstraints/*) = 0">
      <xsl:for-each select="../../mri:resourceConstraints/*[mco:accessConstraints]/mco:otherConstraints">
        <xsl:if test="position() = 1 or ($isPreservingAllResourceConstraints and position() > 1)">
          <xsl:element name="{if (position() = 1) then 'dct:accessRights' else 'dct:rights'}">
            <dct:RightsStatement>
              <xsl:choose>
                <xsl:when test="gcx:Anchor/@xlink:href">
                  <xsl:attribute name="rdf:about" select="gcx:Anchor/@xlink:href"/>
                </xsl:when>
                <xsl:otherwise>
                  <xsl:call-template name="rdf-localised">
                    <xsl:with-param name="nodeName" select="'dct:description'"/>
                  </xsl:call-template>
                </xsl:otherwise>
              </xsl:choose>
            </dct:RightsStatement>
          </xsl:element>
        </xsl:if>
      </xsl:for-each>

      <xsl:variable name="useConstraints"
                    as="node()*">
        <xsl:copy-of select="../../mri:resourceConstraints/*[mco:useConstraints]/mco:otherConstraints"/>
        <xsl:copy-of select="../../mri:resourceConstraints/*[mco:useConstraints]/mco:useLimitation"/>
      </xsl:variable>

      <xsl:variable name="licensesAndRights" as="node()*">
        <xsl:for-each select="$useConstraints">

          <xsl:variable name="httpUriInAnchorOrText"
                        select="(gcx:Anchor/@xlink:href[starts-with(., 'http')]
                                  |gco:CharacterString[starts-with(., 'http')])[1]"/>

          <xsl:choose>
            <xsl:when test="$httpUriInAnchorOrText != '' and $isMappingResourceConstraintsToEuVocabulary = true()">
              <xsl:variable name="licenseUriWithoutHttp"
                            select="replace($httpUriInAnchorOrText,'https?://','')"/>
              <xsl:variable name="euDcatLicense"
                            select="$euLicenses/rdf:RDF/skos:Concept[
                                                  matches(skos:exactMatch/@rdf:resource,
                                                          concat('https?://', $licenseUriWithoutHttp, '/?'))]"/>

              <xsl:if test="$euDcatLicense != ''">
                <dct:license>
                  <dct:LicenseDocument rdf:about="{$euDcatLicense/@rdf:about}">
                    <!--<xsl:copy-of select="$euDcatLicense/(skos:prefLabel[@xml:lang = $languages/@iso2code]
                                                      |skos:exactMatch)"
                                   copy-namespaces="no"/>-->
                  </dct:LicenseDocument>
                </dct:license>
              </xsl:if>
            </xsl:when>
            <xsl:when test="$httpUriInAnchorOrText != ''">
              <dct:license>
                <dct:LicenseDocument rdf:about="{$httpUriInAnchorOrText}"/>
              </dct:license>
            </xsl:when>
            <xsl:otherwise>
              <dct:rights>
                <dct:RightsStatement>
                  <xsl:call-template name="rdf-localised">
                    <xsl:with-param name="nodeName" select="'dct:description'"/>
                  </xsl:call-template>
                </dct:RightsStatement>
              </dct:rights>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:for-each>
      </xsl:variable>

      <xsl:copy-of select="$licensesAndRights"/>
    </xsl:if>
  </xsl:template>



  <!--
  RDF Property:	odrl:hasPolicy
  Definition:	An ODRL conformant policy expressing the rights associated with the resource.
  Range:	odrl:Policy
  Usage note:	Information about rights expressed as an ODRL policy [ODRL-MODEL] using the ODRL vocabulary [ODRL-VOCAB] MAY be provided for the resource. See also guidance at 9. License and rights statements.
  https://www.w3.org/TR/odrl-model/00Model.png

  TODO
  -->
</xsl:stylesheet>
