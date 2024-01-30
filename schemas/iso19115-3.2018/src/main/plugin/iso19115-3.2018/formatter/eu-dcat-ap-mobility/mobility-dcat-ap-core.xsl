<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:mdb="http://standards.iso.org/iso/19115/-3/mdb/2.0"
                xmlns:mcc="http://standards.iso.org/iso/19115/-3/mcc/1.0"
                xmlns:gco="http://standards.iso.org/iso/19115/-3/gco/1.0"
                xmlns:mri="http://standards.iso.org/iso/19115/-3/mri/1.0"
                xmlns:cit="http://standards.iso.org/iso/19115/-3/cit/2.0"
                xmlns:mco="http://standards.iso.org/iso/19115/-3/mco/1.0"
                xmlns:lan="http://standards.iso.org/iso/19115/-3/lan/1.0"
                xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
                xmlns:dct="http://purl.org/dc/terms/"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                xmlns:skos="http://www.w3.org/2004/02/skos/core#"
                xmlns:mobilitydcatap="https://w3id.org/mobilitydcat-ap"
                exclude-result-prefixes="#all">

  <xsl:import href="../eu-dcat-ap/eu-dcat-ap-core.xsl"/>

  <xsl:template name="create-namespaces-eu-dcat-mobilitydcatap">
    <xsl:call-template name="create-namespaces-eu-dcat-ap"/>
    <xsl:namespace name="mobilitydcatap" select="'https://w3id.org/mobilitydcat-ap'"/>
  </xsl:template>


  <xsl:variable name="mobilityThemeThesaurusKey"
                select="'https://w3id.org/mobilitydcat-ap/mobility-theme'"/>

  <xsl:template mode="iso19115-3-to-dcat"
                match="mri:descriptiveKeywords[*/mri:thesaurusName/*/cit:title/*/@xlink:href = $mobilityThemeThesaurusKey]">
    <xsl:for-each select="*/mri:keyword[*/text() != '']">

<!--      <xsl:variable name="hvdCategory"-->
<!--                    select="$euHvdDataCategories/rdf:RDF/rdf:Description[skos:prefLabel/text() = current()/*/text()]"/>-->
<!--      <xsl:if test="$hvdCategory">-->
        <mobilitydcatap:mobilityTheme>
<!--          <skos:Concept rdf:about="{$hvdCategory/@rdf:about}">-->
<!--            <xsl:copy-of select="$hvdCategory/skos:prefLabel[@xml:lang = $languages/@iso2code]"/>-->
<!--          </skos:Concept>-->
          <skos:Concept rdf:about="{*/@xlink:href}">
            <skos:prefLabel><xsl:value-of select="*/text()"/></skos:prefLabel>
          </skos:Concept>
        </mobilitydcatap:mobilityTheme>
<!--      </xsl:if>-->
    </xsl:for-each>
  </xsl:template>

</xsl:stylesheet>
