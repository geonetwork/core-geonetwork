<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:mcc="http://standards.iso.org/iso/19115/-3/mcc/1.0"
                xmlns:gco="http://standards.iso.org/iso/19115/-3/gco/1.0"
                xmlns:gcx="http://standards.iso.org/iso/19115/-3/gcx/1.0"
                xmlns:lan="http://standards.iso.org/iso/19115/-3/lan/1.0"
                xmlns:cit="http://standards.iso.org/iso/19115/-3/cit/2.0"
                xmlns:mri="http://standards.iso.org/iso/19115/-3/mri/1.0"
                xmlns:dct="http://purl.org/dc/terms/"
                xmlns:dcat="http://www.w3.org/ns/dcat#"
                xmlns:foaf="http://xmlns.com/foaf/0.1/"
                xmlns:vcard="http://www.w3.org/2006/vcard/ns#"
                xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
                xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#"
                xmlns:owl="http://www.w3.org/2002/07/owl#"
                xmlns:adms="http://www.w3.org/ns/adms#"
                xmlns:skos="http://www.w3.org/2004/02/skos/core#"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                exclude-result-prefixes="#all">

  <xsl:template name="create-node-with-info">
    <xsl:param name="message" as="xs:string?"/>
    <xsl:param name="node" as="node()"/>

    <xsl:comment select="$message"/>
    <xsl:copy-of select="$node"/>
  </xsl:template>


  <xsl:template name="rdf-localised">
    <xsl:param name="nodeName"
               as="xs:string"/>

    <xsl:element name="{$nodeName}">
      <xsl:attribute name="xml:lang" select="$languages[@default]/@iso3code"/>
      <xsl:value-of select="*/text()"/>
    </xsl:element>

    <xsl:variable name="hasDefaultLanguageCharacterString"
                  select="count(gco:CharacterString|gcx:Anchor) > 0"/>

    <xsl:for-each select="lan:PT_FreeText/*/lan:LocalisedCharacterString">
      <xsl:variable name="translationLanguage"
                    select="@locale"/>

      <xsl:choose>
        <xsl:when test="$hasDefaultLanguageCharacterString
                        and $translationLanguage = $languages[@default]/concat('#', @id)">
          <!-- Ignored default language which may be repeated in translations. -->
        </xsl:when>
        <xsl:otherwise>
          <xsl:element name="{$nodeName}">
            <xsl:attribute name="xml:lang" select="$languages[concat('#', @id) = $translationLanguage]/@iso3code"/>
            <xsl:value-of select="text()"/>
          </xsl:element>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:for-each>
  </xsl:template>


  <xsl:template name="rdf-not-localised">
    <xsl:param name="nodeName"
               as="xs:string"/>
    <xsl:element name="{$nodeName}">
      <xsl:value-of select="*/text()"/>
    </xsl:element>
  </xsl:template>


  <!--
  Range:	rdfs:Literal encoded using the relevant ISO 8601 Date and Time compliant string [DATETIME] and typed using the appropriate XML Schema datatype [XMLSCHEMA11-2] (xsd:gYear, xsd:gYearMonth, xsd:date, or xsd:dateTime).
  -->
  <xsl:template name="rdf-date">
    <xsl:param name="nodeName"
               as="xs:string"/>

    <xsl:element name="{$nodeName}">
      <xsl:attribute name="rdf:datatype"
                     select="concat('http://www.w3.org/2001/XMLSchema#date', (if (contains(*/text(), 'T')) then 'Time' else ''))"/>
      <xsl:value-of select="*/text()"/>
    </xsl:element>
  </xsl:template>

  <xsl:template mode="rdf-metadata-ref" match="*">
    <xsl:choose>
      <xsl:when test="@xlink:href">
        <xsl:attribute name="rdf:resource" select="@xlink:href"/>
      </xsl:when>
      <xsl:when test="@uuidref">
        <!-- TODO: Here we need a not relative URI? -->
        <xsl:attribute name="rdf:resource" select="@uuidref"/>
      </xsl:when>
      <xsl:otherwise>
        <!-- TODO: Here we need a not relative URI? -->
        <xsl:attribute name="rdf:resource" select="*/mri:code/*/text()"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

</xsl:stylesheet>
