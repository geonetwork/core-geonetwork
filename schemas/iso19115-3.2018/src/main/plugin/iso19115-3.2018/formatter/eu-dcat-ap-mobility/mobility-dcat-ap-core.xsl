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
                xmlns:mrs="http://standards.iso.org/iso/19115/-3/mrs/1.0"
                xmlns:gcx="http://standards.iso.org/iso/19115/-3/gcx/1.0"
                xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
                xmlns:dct="http://purl.org/dc/terms/"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                xmlns:skos="http://www.w3.org/2004/02/skos/core#"
                xmlns:mobilitydcatap="https://w3id.org/mobilitydcat-ap"
                exclude-result-prefixes="#all">

  <!-- Import related templates -->
  <xsl:import href="../eu-dcat-ap/eu-dcat-ap-core.xsl"/>

  <xsl:template name="create-namespaces-eu-dcat-mobilitydcatap">
    <xsl:call-template name="create-namespaces-eu-dcat-ap"/>
    <xsl:namespace name="mobilitydcatap" select="'https://w3id.org/mobilitydcat-ap'"/>
  </xsl:template>

  <xsl:template mode="iso19115-3-to-dcat-resource"
                name="iso19115-3-to-eu-dcat-ap-mobility-resource"
                match="mdb:MD_Metadata"
                priority="2">
    <xsl:call-template name="iso19115-3-to-dcat-ap-resource"/>

    <xsl:apply-templates mode="iso19115-3-to-dcat"
                         select="mdb:referenceSystemInfo/*/mrs:referenceSystemIdentifier/*"/>
  </xsl:template>


  <!-- Create Mobility Theme element -->
  <xsl:variable name="mobilityThemeThesaurusKey"
                select="'https://w3id.org/mobilitydcat-ap/mobility-theme'"/>

  <xsl:template mode="iso19115-3-to-dcat"
                match="mri:descriptiveKeywords[*/mri:thesaurusName/*/cit:title/*/@xlink:href = $mobilityThemeThesaurusKey]">
    <xsl:for-each select="*/mri:keyword[*/text() != '']">

      <mobilitydcatap:mobilityTheme>
        <skos:Concept rdf:about="{*/@xlink:href}">
          <skos:prefLabel><xsl:value-of select="*/text()"/></skos:prefLabel>
        </skos:Concept>
      </mobilitydcatap:mobilityTheme>
    </xsl:for-each>
  </xsl:template>

  <!-- Create georeferencing method  element -->
  <xsl:variable name="georeferencingMethodThesaurusKey"
                select="'https://w3id.org/mobilitydcat-ap/georeferencing-method'"/>
  <xsl:template mode="iso19115-3-to-dcat"
                match="mri:descriptiveKeywords[*/mri:thesaurusName/*/cit:title/*/@xlink:href = $georeferencingMethodThesaurusKey]">
    <xsl:for-each select="*/mri:keyword[*/text() != '']">

      <mobilitydcatap:georeferencingMethod>
        <skos:Concept rdf:about="{*/@xlink:href}">
          <skos:prefLabel><xsl:value-of select="*/text()"/></skos:prefLabel>
        </skos:Concept>
      </mobilitydcatap:georeferencingMethod>
    </xsl:for-each>
  </xsl:template>

  <!-- Create network coverage  element -->
  <xsl:variable name="networkCoverageThesaurusKey"
                select="'https://w3id.org/mobilitydcat-ap/network-coverage'"/>
  <xsl:template mode="iso19115-3-to-dcat"
                match="mri:descriptiveKeywords[*/mri:thesaurusName/*/cit:title/*/@xlink:href = $networkCoverageThesaurusKey]">
    <xsl:for-each select="*/mri:keyword[*/text() != '']">

      <mobilitydcatap:networkCoverage>
        <skos:Concept rdf:about="{*/@xlink:href}">
          <skos:prefLabel><xsl:value-of select="*/text()"/></skos:prefLabel>
        </skos:Concept>
      </mobilitydcatap:networkCoverage>
    </xsl:for-each>
  </xsl:template>

  <!-- Create Transportation Mode element -->
  <xsl:variable name="transportationModeThesaurusKey"
                select="'https://w3id.org/mobilitydcat-ap/transport-mode'"/>
  <xsl:template mode="iso19115-3-to-dcat"
                match="mri:descriptiveKeywords[*/mri:thesaurusName/*/cit:title/*/@xlink:href = $transportationModeThesaurusKey]">
    <xsl:for-each select="*/mri:keyword[*/text() != '']">

      <mobilitydcatap:transportMode>
        <skos:Concept rdf:about="{*/@xlink:href}">
          <skos:prefLabel><xsl:value-of select="*/text()"/></skos:prefLabel>
        </skos:Concept>
      </mobilitydcatap:transportMode>
    </xsl:for-each>
  </xsl:template>

  <!--
  This is an optional added by mobilityDCAT-AP, analogue to an addition by [GEODCAT-AP-v2.0.0].

  Values from this vocabulary MUST be used as instances of class dct:Standard, being the range of the property dct:conformsTo.
  -->
  <xsl:template mode="iso19115-3-to-dcat"
                        match="mdb:referenceSystemInfo/*/mrs:referenceSystemIdentifier/*">
    <xsl:variable name="code" select="mcc:code/(gco:CharacterString|gcx:Anchor)/text()"/>
    <xsl:variable name="link" select="mcc:code/gcx:Anchor/@xlink:href"/>

    <xsl:variable name="uri"
                  select="($link, $code[matches(., '^https?://')])[1]"/>
    <xsl:if test="$uri != ''">
      <dct:conformsTo>
        <dct:Standard rdf:about="{$uri}" />
      </dct:conformsTo>
    </xsl:if>
  </xsl:template>
</xsl:stylesheet>
