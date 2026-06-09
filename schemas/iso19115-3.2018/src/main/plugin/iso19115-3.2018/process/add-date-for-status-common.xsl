<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:geonet="http://www.fao.org/geonetwork"
                xmlns:cit="http://standards.iso.org/iso/19115/-3/cit/2.0"
                xmlns:mdb="http://standards.iso.org/iso/19115/-3/mdb/2.0"
                xmlns:gco="http://standards.iso.org/iso/19115/-3/gco/1.0"
                xmlns:mri="http://standards.iso.org/iso/19115/-3/mri/1.0"
                xmlns:tr="java:org.fao.geonet.api.records.formatters.SchemaLocalizations"
                version="2.0" exclude-result-prefixes="#all">

  <xsl:variable name="dateTypes" as="node()+">
   <status is="completed" dateType="publication" schema="iso19139"/>
    <status is="completed" dateType="publication" schema="iso19115-3.2018"/>
    <status is="superseded" dateType="superseded" schema="iso19115-3.2018"/>
    <status is="deprecated" dateType="deprecated" schema="iso19115-3.2018"/>
    <status is="obsolete" dateType="deprecated" schema="iso19115-3.2018"/>
    <status is="retired" dateType="deprecated" schema="iso19115-3.2018"/>
  </xsl:variable>


  <xsl:variable name="add-date-loc">
    <msg id="a" xml:lang="eng">Resource has status </msg>
    <msg id="b" xml:lang="eng">. Do you want to set the </msg>
    <msg id="c" xml:lang="eng"> date? (with default value: </msg>
    <msg id="d" xml:lang="eng">)</msg>
    <msg id="a" xml:lang="fre">Le statut de la ressource est </msg>
    <msg id="b" xml:lang="fre">. Voulez-vous ajouter la date avec le type </msg>
    <msg id="c" xml:lang="fre"> ? (avec par défaut : </msg>
    <msg id="d" xml:lang="fre">)</msg>
  </xsl:variable>

  <xsl:variable name="df"
                select="'[Y0001]-[M01]-[D01]'"/>

  <xsl:param name="dateTypeForStatus" select="''"/>
  <xsl:param name="dateValueForStatus" select="format-date(current-date(), $df)"/>

  <xsl:template name="list-add-date-for-status">
    <suggestion process="add-date-for-status"/>
  </xsl:template>

  <xsl:template name="analyze-add-date-for-status">
    <xsl:param name="root"/>

    <xsl:variable name="recordStatus"
                  select="$root//*:identificationInfo/*/*:status/*[@codeListValue = $dateTypes[starts-with(@schema, $schema)]/@is]"/>

    <xsl:for-each select="$recordStatus">
      <xsl:variable name="status"
                    select="@codeListValue"/>

      <xsl:variable name="statusLabel"
                    select="tr:codelist-value-label(
                            if ($guiLang = '') then tr:create($schema)
                            else tr:create($schema, $guiLang),
                            'MD_ProgressCode',
                            $status)"/>

      <xsl:variable name="dateTypeForStatus"
                    select="$dateTypes[@is = $status and starts-with(@schema, $schema)]/@dateType"/>

      <xsl:variable name="dateTypeLabel"
                    select="tr:codelist-value-label(
                            if ($guiLang = '') then tr:create($schema)
                            else tr:create($schema, $guiLang),
                            'CI_DateTypeCode',
                            $dateTypeForStatus)"/>

      <xsl:variable name="hasNoDateForStatus"
                    select="count($root//*:identificationInfo/*/*:citation/*/*:date/*/*:dateType/*[@codeListValue = $dateTypeForStatus]) = 0"/>

      <xsl:if test="$hasNoDateForStatus">
        <suggestion process="add-date-for-status" id="{generate-id()}"
                    category="status" target="identification">
          <name><xsl:value-of select="concat(
                geonet:i18n($add-date-loc, 'a', $guiLang),
                $statusLabel,
                geonet:i18n($add-date-loc, 'b', $guiLang),
                $dateTypeLabel,
                geonet:i18n($add-date-loc, 'c', $guiLang),
                $dateValueForStatus,
                geonet:i18n($add-date-loc, 'd', $guiLang)
                )"/>
          </name>
          <operational>true</operational>
          <params>{
            "dateTypeForStatus":{"type":"codelist", "codelist": "<xsl:value-of select="if ($schema = 'iso19115-3.2018') then 'cit:CI_DateTypeCode' else 'gmd:CI_DateTypeCode'"/>", "defaultValue":"<xsl:value-of select="$dateTypeForStatus"/>"},
            "dateValueForStatus":{"type":"string", "defaultValue":"<xsl:value-of select="$dateValueForStatus"/>"}
          }</params>
        </suggestion>
      </xsl:if>
    </xsl:for-each>
  </xsl:template>




  <!-- Do a copy of every nodes and attributes -->
  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

  <!-- Remove geonet:* elements. -->
  <xsl:template match="geonet:*" priority="2"/>

</xsl:stylesheet>
