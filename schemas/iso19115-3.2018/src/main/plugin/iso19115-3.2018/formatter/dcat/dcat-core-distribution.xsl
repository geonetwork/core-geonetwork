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
                xmlns:dcat="http://www.w3.org/ns/dcat#"
                xmlns:foaf="http://xmlns.com/foaf/0.1/"
                xmlns:vcard="http://www.w3.org/2006/vcard/ns#"
                exclude-result-prefixes="#all">

  <!--
  RDF Property:	dcat:landingPage
  Definition:	A Web page that can be navigated to in a Web browser to gain access to the catalog, a dataset, its distributions and/or additional information.
  Sub-property of:	foaf:page
  Range:	foaf:Document
  Usage note:	If the distribution(s) are accessible only through a landing page (i.e., direct download URLs are not known), then the landing page link SHOULD be duplicated as dcat:accessURL on a distribution. (see 5.7 Dataset available only behind some Web page)
  -->
  <xsl:template mode="iso19115-3-to-dcat"
                match="mdb:metadataLinkage">
    <dcat:landingPage>
      <foaf:Document rdf:about="{*/cit:linkage/*/text()}">
        <xsl:for-each select="*/cit:name">
          <xsl:call-template name="rdf-localised">
            <xsl:with-param name="nodeName" select="'dct:title'"/>
          </xsl:call-template>
        </xsl:for-each>
        <xsl:for-each select="*/cit:description">
          <xsl:call-template name="rdf-localised">
            <xsl:with-param name="nodeName" select="'dct:description'"/>
          </xsl:call-template>
        </xsl:for-each>
      </foaf:Document>
    </dcat:landingPage>
  </xsl:template>
</xsl:stylesheet>
