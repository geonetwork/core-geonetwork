<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:geonet="http://www.fao.org/geonetwork"
                xmlns:cit="http://standards.iso.org/iso/19115/-3/cit/2.0"
                xmlns:mdb="http://standards.iso.org/iso/19115/-3/mdb/2.0"
                xmlns:gco="http://standards.iso.org/iso/19115/-3/gco/1.0"
                xmlns:mri="http://standards.iso.org/iso/19115/-3/mri/1.0"
                xmlns:tr="java:org.fao.geonet.api.records.formatters.SchemaLocalizations"
                version="2.0" exclude-result-prefixes="#all">

  <xsl:import href="process-utility.xsl"/>
  <xsl:import href="add-date-for-status-common.xsl"/>

  <xsl:template
    match="mri:citation/*"
    priority="2">
    <xsl:copy>
      <xsl:copy-of select="@*"/>

      <xsl:copy-of select="cit:title
                           |cit:alternateTitle
                           |cit:date
                          "/>
      <xsl:if test="$dateTypeForStatus != ''">
        <cit:date>
          <cit:CI_Date>
            <cit:date>
              <gco:Date>
                <xsl:value-of select="$dateValueForStatus"/>
              </gco:Date>
            </cit:date>
            <cit:dateType>
              <cit:CI_DateTypeCode codeList="http://standards.iso.org/iso/19115/resources/Codelists/cat/codelists.xml#CI_DateTypeCode"
                                   codeListValue="{$dateTypeForStatus}"/>
            </cit:dateType>
          </cit:CI_Date>
        </cit:date>
      </xsl:if>

      <xsl:copy-of select="cit:edition
                           |cit:editionDate
                           |cit:identifier
                           |cit:citedResponsibleParty
                           |cit:presentationForm
                           |cit:series
                           |cit:otherCitationDetails
                           |cit:collectiveTitle
                           |cit:ISBN
                           |cit:ISSN
                           |cit:onlineResource
                           |cit:graphic
                          "/>
    </xsl:copy>
  </xsl:template>
</xsl:stylesheet>
