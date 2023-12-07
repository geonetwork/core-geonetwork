<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:cit="http://standards.iso.org/iso/19115/-3/cit/2.0"
                xmlns:mdb="http://standards.iso.org/iso/19115/-3/mdb/2.0"
                xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
                xmlns:dct="http://purl.org/dc/terms/"
                xmlns:dcat="http://www.w3.org/ns/dcat#"
                xmlns:foaf="http://xmlns.com/foaf/0.1/"
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
