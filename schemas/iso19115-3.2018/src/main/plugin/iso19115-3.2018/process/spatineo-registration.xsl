<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:geonet="http://www.fao.org/geonetwork"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                xmlns:mdb="http://standards.iso.org/iso/19115/-3/mdb/2.0"
                xmlns:cat="http://standards.iso.org/iso/19115/-3/cat/1.0"
                xmlns:cit="http://standards.iso.org/iso/19115/-3/cit/2.0"
                xmlns:gcx="http://standards.iso.org/iso/19115/-3/gcx/1.0"
                xmlns:gex="http://standards.iso.org/iso/19115/-3/gex/1.0"
                xmlns:lan="http://standards.iso.org/iso/19115/-3/lan/1.0"
                xmlns:srv="http://standards.iso.org/iso/19115/-3/srv/2.1"
                xmlns:mas="http://standards.iso.org/iso/19115/-3/mas/1.0"
                xmlns:mcc="http://standards.iso.org/iso/19115/-3/mcc/1.0"
                xmlns:mco="http://standards.iso.org/iso/19115/-3/mco/1.0"
                xmlns:mda="http://standards.iso.org/iso/19115/-3/mda/1.0"
                xmlns:mds="http://standards.iso.org/iso/19115/-3/mds/2.0"
                xmlns:mdt="http://standards.iso.org/iso/19115/-3/mdt/2.0"
                xmlns:mex="http://standards.iso.org/iso/19115/-3/mex/1.0"
                xmlns:mmi="http://standards.iso.org/iso/19115/-3/mmi/1.0"
                xmlns:mpc="http://standards.iso.org/iso/19115/-3/mpc/1.0"
                xmlns:mrc="http://standards.iso.org/iso/19115/-3/mrc/2.0"
                xmlns:mrd="http://standards.iso.org/iso/19115/-3/mrd/1.0"
                xmlns:mri="http://standards.iso.org/iso/19115/-3/mri/1.0"
                xmlns:mrl="http://standards.iso.org/iso/19115/-3/mrl/2.0"
                xmlns:mrs="http://standards.iso.org/iso/19115/-3/mrs/1.0"
                xmlns:msr="http://standards.iso.org/iso/19115/-3/msr/2.0"
                xmlns:mdq="http://standards.iso.org/iso/19157/-2/mdq/1.0"
                xmlns:mac="http://standards.iso.org/iso/19115/-3/mac/2.0"
                xmlns:gco="http://standards.iso.org/iso/19115/-3/gco/1.0"
                xmlns:gfc="http://standards.iso.org/iso/19110/gfc/1.1"
                xmlns:gml="http://www.opengis.net/gml/3.2"
                xmlns:util="java:org.fao.geonet.util.XslUtil"
                xmlns:spatineo="java:org.fao.geonet.util.SpatineoUtil"
                xmlns:saxon="http://saxon.sf.net/"
                version="2.0" exclude-result-prefixes="#all">

  <xsl:import href="process-utility.xsl"/>

  <xsl:param name="spatineoUrl" select="''"/>

  <!-- i18n information -->
  <xsl:variable name="spatineo-registration-loc">
    <msg id="a" xml:lang="eng">Register service in Spatineo monitor: </msg>
    <msg id="a" xml:lang="fre">Déclarer le service auprès de Spatineo monitor : </msg>
    <msg id="title" xml:lang="eng">Service health monitoring report for </msg>
    <msg id="title" xml:lang="fre">Rapport de disponibilité du service </msg>
    <msg id="abstract" xml:lang="eng">Service availability statistics for </msg>
    <msg id="abstract" xml:lang="fre">Statistiques de disponibilité du service </msg>
    <msg id="provided" xml:lang="eng"> provided by Spatineo.</msg>
    <msg id="provided" xml:lang="fre"> fournies par Spatineo.</msg>
    <msg id="ref" xml:lang="eng">See the related availability report.</msg>
    <msg id="ref" xml:lang="fre">Voir le rapport de disponibilité référencé.</msg>
  </xsl:variable>

  <xsl:template name="list-spatineo-registration">
    <suggestion process="spatineo-registration"/>
  </xsl:template>


  <xsl:template name="analyze-spatineo-registration">
    <xsl:param name="root"/>

    <xsl:variable name="services"
                  select="distinct-values($root//mdb:distributionInfo//mrd:onLine/*[
                            contains(cit:protocol/*, 'WFS')
                            or contains(cit:protocol/*, 'WMS')
                            or contains(cit:protocol/*, 'ESRI:REST')]
                              /cit:linkage/*/text())"/>

    <xsl:variable name="id"
                  select="generate-id(.)"/>

    <xsl:for-each select="$services">
      <xsl:variable name="url"
                    select="."/>
      <xsl:variable name="isRegistered"
                    select="count($root//mdb:dataQualityInfo/*/
                              mdq:standaloneQualityReport[
                                contains(*/mdq:abstract/*[1]/text(), $url)]) > 0"
                    as="xs:boolean"/>
      <xsl:if test="not($isRegistered)">
        <suggestion process="spatineo-registration"
                    id="{concat($id, '-', position())}"
                    category="contentinfo"
                    target="metadata">
          <name><xsl:value-of select="geonet:i18n($spatineo-registration-loc, 'a', $guiLang)"/><xsl:value-of select="."/></name>
          <operational>true</operational>
          <params>{"spatineoUrl":{"type":"text", "defaultValue":"<xsl:value-of select="."/>"}}</params>
        </suggestion>
      </xsl:if>
    </xsl:for-each>
  </xsl:template>




  <xsl:variable name="spatineoReport"
                select="if ($spatineoUrl != '')
                        then spatineo:registerServiceInSpatineoMonitor($spatineoUrl, 20)
                        else ''"/>

  <!-- Do a copy of every nodes and attributes -->
  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

  <!-- Remove geonet:* elements. -->
  <xsl:template match="geonet:*" priority="2"/>

  <xsl:template
    match="mdb:MD_Metadata"
    priority="2">

    <xsl:variable name="metadataLanguage"
                  select="//mdb:MD_Metadata/mdb:defaultLocale/
                              lan:PT_Locale/lan:language/lan:LanguageCode/@codeListValue"/>

    <xsl:copy>
      <xsl:apply-templates select="@*"/>
      <xsl:apply-templates
        select="mdb:metadataIdentifier|
                mdb:defaultLocale|
                mdb:parentMetadata|
                mdb:metadataScope|
                mdb:contact|
                mdb:dateInfo|
                mdb:metadataStandard|
                mdb:metadataProfile|
                mdb:alternativeMetadataReference|
                mdb:otherLocale|
                mdb:metadataLinkage|
                mdb:spatialRepresentationInfo|
                mdb:referenceSystemInfo|
                mdb:metadataExtensionInfo|
                mdb:identificationInfo|
                mdb:contentInfo|
                mdb:distributionInfo|
                mdb:dataQualityInfo"/>

      <!-- Insert spatineo registration as DQ report
       If something goes wrong, user needs to check the logs.
       We don't have simple way to report errors back to users.
       -->
      <xsl:for-each select="$spatineoReport/report/service">
        <mdb:dataQualityInfo>
          <mdq:DQ_DataQuality>
            <mdq:scope>
              <mcc:MD_Scope>
                <mcc:level>
                  <mcc:MD_ScopeCode codeList="http://standards.iso.org/iso/19115/resources/Codelists/cat/codelists.xml#MD_ScopeCode"
                                    codeListValue="service"/>
                </mcc:level>
              </mcc:MD_Scope>
            </mdq:scope>
            <mdq:standaloneQualityReport>
              <mdq:DQ_StandaloneQualityReportInformation>
                <mdq:reportReference>
                  <cit:CI_Citation>
                    <cit:title>
                      <gco:CharacterString>
                        <xsl:value-of select="concat(
                              geonet:i18n($spatineo-registration-loc, 'title', $metadataLanguage),
                              root/serviceType,
                              ' ', root/serviceTitle)"/>
                      </gco:CharacterString>
                    </cit:title>
                    <cit:onlineResource>
                      <cit:CI_OnlineResource>
                        <cit:linkage>
                          <gco:CharacterString>
                            <xsl:value-of select="concat('https://directory.spatineo.com/service/', root/wmsServiceID)"/>
                          </gco:CharacterString>
                        </cit:linkage>
                      </cit:CI_OnlineResource>
                    </cit:onlineResource>
                  </cit:CI_Citation>
                </mdq:reportReference>
                <mdq:abstract>
                  <gco:CharacterString>
                    <xsl:value-of select="concat(
                              geonet:i18n($spatineo-registration-loc, 'abstract', $metadataLanguage),
                              root/serviceType,
                              ' ', root/sourceURL,
                              geonet:i18n($spatineo-registration-loc, 'provided', $metadataLanguage)
                              )"/></gco:CharacterString>
                </mdq:abstract>
              </mdq:DQ_StandaloneQualityReportInformation>
            </mdq:standaloneQualityReport>
            <!-- mdq:report is mandatory so create a usability report
            referencing the standalone one. -->
            <mdq:report>
              <mdq:DQ_UsabilityElement>
                <mdq:result>
                  <mdq:DQ_DescriptiveResult>
                    <mdq:statement>
                      <gco:CharacterString>
                        <xsl:value-of select="geonet:i18n($spatineo-registration-loc, 'ref', $metadataLanguage)"/>
                      </gco:CharacterString>
                    </mdq:statement>
                  </mdq:DQ_DescriptiveResult>
                </mdq:result>
              </mdq:DQ_UsabilityElement>
            </mdq:report>
          </mdq:DQ_DataQuality>
        </mdb:dataQualityInfo>
      </xsl:for-each>

      <xsl:apply-templates
        select="mdb:resourceLineage|
                mdb:portrayalCatalogueInfo|
                mdb:metadataConstraints|
                mdb:applicationSchemaInfo|
                mdb:metadataMaintenance|
                mdb:acquisitionInformation"/>
    </xsl:copy>
  </xsl:template>
</xsl:stylesheet>
