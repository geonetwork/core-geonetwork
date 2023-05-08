<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:geonet="http://www.fao.org/geonetwork"
                xmlns:gco="http://www.isotc211.org/2005/gco"
                xmlns:srv="http://www.isotc211.org/2005/srv"
                xmlns:gmd="http://www.isotc211.org/2005/gmd"
                xmlns:gmx="http://www.isotc211.org/2005/gmx"
                version="2.0" exclude-result-prefixes="#all">

  <xsl:import href="process-utility.xsl"/>
  <xsl:import href="../../iso19115-3.2018/process/add-date-for-status-common.xsl"/>

  <xsl:template
    match="gmd:citation/*"
    priority="2">
    <xsl:copy>
      <xsl:copy-of select="@*"/>

      <xsl:copy-of select="gmd:title
                           |gmd:alternateTitle
                           |gmd:date
                          "/>
      <xsl:if test="$dateTypeForStatus != ''">
        <gmd:date>
          <gmd:CI_Date>
            <gmd:date>
              <gco:Date>
                <xsl:value-of select="$dateValueForStatus"/>
              </gco:Date>
            </gmd:date>
            <gmd:dateType>
              <gmd:CI_DateTypeCode codeList="http://standards.iso.org/iso/19115/resources/Codelists/cat/codelists.xml#CI_DateTypeCode"
                                   codeListValue="{$dateTypeForStatus}"/>
            </gmd:dateType>
          </gmd:CI_Date>
        </gmd:date>
      </xsl:if>

      <xsl:copy-of select="gmd:edition
                           |gmd:editionDate
                           |gmd:identifier
                           |gmd:citedResponsibleParty
                           |gmd:presentationForm
                           |gmd:series
                           |gmd:otherCitationDetails
                           |gmd:collectiveTitle
                           |gmd:ISBN
                           |gmd:ISSN
                           |gmd:onlineResource
                           |gmd:graphic
                          "/>
    </xsl:copy>
  </xsl:template>
</xsl:stylesheet>
