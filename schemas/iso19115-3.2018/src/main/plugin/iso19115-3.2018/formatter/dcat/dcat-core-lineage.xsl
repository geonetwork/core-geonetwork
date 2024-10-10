<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:mri="http://standards.iso.org/iso/19115/-3/mri/1.0"
                xmlns:adms="http://www.w3.org/ns/adms#"
                xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
                xmlns:skos="http://www.w3.org/2004/02/skos/core#"
                xmlns:tr="java:org.fao.geonet.api.records.formatters.SchemaLocalizations"
                exclude-result-prefixes="#all">

  <!--
  RDF Property:	adms:status
  Definition:	The status of the resource in the context of a particular workflow process [VOCAB-ADMS].
  Range:	skos:Concept
  Usage note:
  DCAT does not prescribe the use of any specific set of life-cycle statuses, but refers to existing standards and community practices fit for the relevant application scenario.
  https://www.w3.org/TR/vocab-adms/#adms-status
  -->
  <xsl:template mode="iso19115-3-to-dcat"
                match="mri:status">
    <adms:status>
      <skos:Concept rdf:about="{concat($isoCodeListBaseUri, */@codeListValue)}">
        <xsl:if test="$isExpandSkosConcept">
          <xsl:variable name="codelistKey"
                        select="*/@codeListValue"/>
          <xsl:variable name="parentName"
                        select="local-name(*)"/>

          <xsl:for-each select="$languages/@iso3code">
            <xsl:variable name="codelistTranslation"
                          select="tr:codelist-value-label(tr:create('iso19115-3.2018', current()), $parentName, $codelistKey)"/>

            <skos:prefLabel xml:lang="{current()}"><xsl:value-of select="$codelistTranslation"/></skos:prefLabel>
          </xsl:for-each>
        </xsl:if>
      </skos:Concept>
    </adms:status>
  </xsl:template>

  <!--
  RDF Property:	adms:versionNotes
  Definition:	A description of changes between this version and the previous version of the resource [VOCAB-ADMS].
  Range:	rdfs:Literal
  Usage note:
  In case of backward compatibility issues with the previous version of the resource, a textual description of them SHOULD be specified by using this property.

  See dcat-core (mdb:resourceLineage/*/mrl:statement).
  -->
</xsl:stylesheet>
