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
                xmlns:util="java:org.fao.geonet.util.XslUtil"
                xmlns:gn-fn-dcat="http://geonetwork-opensource.org/xsl/functions/dcat"
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

    <xsl:if test="*/text() != ''">
      <xsl:element name="{$nodeName}">
        <xsl:attribute name="xml:lang" select="$languages[@default]/@iso2code"/>
        <xsl:value-of select="*/text()"/>
      </xsl:element>
    </xsl:if>

    <xsl:variable name="hasDefaultLanguageCharacterString"
                  select="count(gco:CharacterString|gcx:Anchor) > 0"/>

    <xsl:for-each select="lan:PT_FreeText/*/lan:LocalisedCharacterString[text() != '']">
      <xsl:variable name="translationLanguage"
                    select="@locale"/>

      <xsl:choose>
        <xsl:when test="$hasDefaultLanguageCharacterString
                        and $translationLanguage = $languages[@default]/concat('#', @id)">
          <!-- Ignored default language which may be repeated in translations. -->
        </xsl:when>
        <xsl:otherwise>
          <xsl:element name="{$nodeName}">
            <xsl:attribute name="xml:lang" select="$languages[concat('#', @id) = $translationLanguage]/@iso2code"/>
            <xsl:value-of select="text()"/>
          </xsl:element>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:for-each>
  </xsl:template>

  <xsl:template name="rdf-index-field-localised">
    <xsl:param name="nodeName"
               as="xs:string"/>
    <xsl:param name="field"
               as="node()"/>

    <xsl:for-each select="$field/*[starts-with(local-name(.), 'lang')]">
      <xsl:variable name="language" select="substring-after(local-name(.), 'lang')"/>
      <xsl:element name="{$nodeName}">
        <xsl:attribute name="xml:lang" select="util:twoCharLangCode($language)"/>
        <xsl:value-of select="."/>
      </xsl:element>
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

    <xsl:variable name="date"
                  as="xs:string?"
                  select="if (*/text()) then */text() else text()"/>

    <xsl:element name="{$nodeName}">
      <xsl:attribute name="rdf:datatype"
                     select="concat('http://www.w3.org/2001/XMLSchema#date', (if (contains($date, 'T')) then 'Time' else ''))"/>
      <xsl:value-of select="$date"/>
    </xsl:element>
  </xsl:template>


  <!--
  Get an object reference from an node.

  In ISO, object reference are usually stored using gcx:Anchor elements (eg. keywords).
  But in other cases, the reference can be stored in a more specific element.
  Define here the rules to extract the reference from the object.
  -->
  <xsl:function name="gn-fn-dcat:rdf-object-ref" as="xs:string?">
    <xsl:param name="node" as="node()"/>

    <xsl:value-of select="if (name($node) = 'cit:CI_Organisation')
                          then $node/(cit:partyIdentifier/*/mcc:code/*/text(),
                           cit:contactInfo/*/cit:onlineResource/*/cit:linkage/gco:CharacterString/text(),
                           cit:name/gcx:Anchor/@xlink:href,
                           @uuid
                          )[1]
                          else if (name($node) = 'cit:CI_Individual')
                          then $node/(cit:partyIdentifier/*/mcc:code/*/text(),
                                cit:name/gcx:Anchor/@xlink:href,
                                @uuid
                          )[1]
                          else if ($node/gcx:Anchor/@xlink:href) then $node/gcx:Anchor/@xlink:href
                          else if ($node/@xlink:href) then $node/@xlink:href
                          else if ($node/@uuidref) then $node/@uuidref
                          else if ($node/*/mri:code/*/text() != '') then $node/*/mri:code/*/text()
                          else ''"/>
  </xsl:function>

  <!--
  Template for creating a reference to an object as an XML attribute.
  -->
  <xsl:template name="rdf-object-ref-attribute"
                mode="rdf-object-ref-attribute"
                match="*">
    <xsl:param name="isAbout"
               as="xs:boolean"
               select="true()"/>
    <xsl:param name="reference"
               as="xs:string?"
               select="gn-fn-dcat:rdf-object-ref(.)"/>

    <xsl:variable name="attributeName"
                  select="if($isAbout) then 'rdf:about' else 'rdf:resource'"/>

    <xsl:if test="$reference != ''">
      <xsl:attribute name="{$attributeName}" select="$reference"/>
    </xsl:if>
  </xsl:template>

</xsl:stylesheet>
