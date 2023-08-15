<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:exslt="http://exslt.org/common"
                xmlns:geonet="http://www.fao.org/geonetwork"
                xmlns:gco="http://standards.iso.org/iso/19115/-3/gco/1.0"
                xmlns:gmx="http://www.isotc211.org/2005/gmx"
                xmlns:mcc="http://standards.iso.org/iso/19115/-3/mcc/1.0"
                xmlns:cit="http://standards.iso.org/iso/19115/-3/cit/2.0"
                xmlns:mdb="http://standards.iso.org/iso/19115/-3/mdb/2.0"
                xmlns:mri="http://standards.iso.org/iso/19115/-3/mri/1.0"
                version="2.0" exclude-result-prefixes="#all">

  <xsl:import href="../../iso19139/process/process-utility.xsl"/>

  <!-- i18n information -->
  <xsl:variable name="add-resource-id-loc">
    <msg id="a" xml:lang="eng">Current record does not contain resource identifier. Compute resource identifier from metadata record identifier.</msg>
    <msg id="a" xml:lang="fre">Cette fiche ne contient pas d'identifiant pour la ressource. Calculer l'identifiant à partir de l'identifiant de la fiche.</msg>
  </xsl:variable>


  <xsl:variable name="resource-id-url-prefix" select="''"/>


  <xsl:template name="list-add-resource-id">
    <suggestion process="add-resource-id"/>
  </xsl:template>



  <!-- Analyze the metadata record and return available suggestion
    for that process -->
  <xsl:template name="analyze-add-resource-id">
    <xsl:param name="root"/>

    <xsl:variable name="hasResourceId"
                  select="count($root//mdb:identificationInfo/*/mri:citation/
            cit:CI_Citation/cit:identifier/*/mcc:code[gco:CharacterString != '']) > 0"/>
    <xsl:if test="not($hasResourceId)">
      <suggestion process="add-resource-id" id="{generate-id()}" category="identification" target="identification">
        <name><xsl:value-of select="geonet:i18n($add-resource-id-loc, 'a', $guiLang)"/></name>
        <operational>true</operational>
      </suggestion>
    </xsl:if>

  </xsl:template>




  <!-- Do a copy of every nodes and attributes -->
  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

  <!-- Remove geonet:* elements. -->
  <xsl:template match="geonet:*" priority="2"/>

  <xsl:template
          match="mdb:identificationInfo/*/mri:citation/
        cit:CI_Citation"
          priority="2">

    <xsl:copy>
      <xsl:copy-of select="@*"/>
      <xsl:copy-of
              select="cit:title|
                cit:alternateTitle|
                cit:date|
                cit:edition|
                cit:editionDate"/>

      <!-- Create resource identifier based on metadata record identifier -->
      <xsl:variable name="urlWithoutLang" select="substring-before($catalogUrl, $nodeId)"/>
      <xsl:variable name="prefix" select="if ($resource-id-url-prefix != '') then $resource-id-url-prefix else $urlWithoutLang"/>
      <xsl:variable name="code" select="concat($prefix, /*/mdb:metadataIdentifier/mcc:MD_Identifier/mcc:code/gco:CharacterString)"/>

      <xsl:copy-of
              select="cit:identifier[mcc:MD_Identifier/mcc:code/gco:CharacterString != $code]"/>
      <cit:identifier>
        <mcc:MD_Identifier>
          <mcc:code>
            <gco:CharacterString><xsl:value-of select="$code"/></gco:CharacterString>
          </mcc:code>
        </mcc:MD_Identifier>
      </cit:identifier>

      <xsl:copy-of
              select="cit:citedResponsibleParty|
                cit:presentationForm|
                cit:series|
                cit:otherCitationDetails|
                cit:collectiveTitle|
                cit:ISBN|
                cit:ISSN|
                cit:onlineResource"/>

    </xsl:copy>
  </xsl:template>

</xsl:stylesheet>
