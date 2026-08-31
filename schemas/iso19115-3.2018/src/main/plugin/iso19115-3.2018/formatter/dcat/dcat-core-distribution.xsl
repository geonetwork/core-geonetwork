<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:cit="http://standards.iso.org/iso/19115/-3/cit/2.0"
                xmlns:mdb="http://standards.iso.org/iso/19115/-3/mdb/2.0"
                xmlns:mrl="http://standards.iso.org/iso/19115/-3/mrl/2.0"
                xmlns:mcc="http://standards.iso.org/iso/19115/-3/mcc/1.0"
                xmlns:mrd="http://standards.iso.org/iso/19115/-3/mrd/1.0"
                xmlns:gco="http://standards.iso.org/iso/19115/-3/gco/1.0"
                xmlns:mco="http://standards.iso.org/iso/19115/-3/mco/1.0"
                xmlns:mri="http://standards.iso.org/iso/19115/-3/mri/1.0"
                xmlns:mpc="http://standards.iso.org/iso/19115/-3/mpc/1.0"
                xmlns:mdq="http://standards.iso.org/iso/19157/-2/mdq/1.0"
                xmlns:mrc="http://standards.iso.org/iso/19115/-3/mrc/2.0"
                xmlns:gcx="http://standards.iso.org/iso/19115/-3/gcx/1.0"
                xmlns:srv="http://standards.iso.org/iso/19115/-3/srv/2.0"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
                xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#"
                xmlns:dct="http://purl.org/dc/terms/"
                xmlns:dcat="http://www.w3.org/ns/dcat#"
                xmlns:foaf="http://xmlns.com/foaf/0.1/"
                exclude-result-prefixes="#all">

  <!--
  Some information are duplicated from the dataset to the distributions.
  This can be enabled or not here.

  Related discussion:
  https://github.com/SEMICeu/GeoDCAT-AP/issues/100
  -->
  <xsl:param name="copyDatasetInfoToDistribution"
             as="xs:string"
             select="'false'"/>
  <xsl:variable name="isCopyingDatasetInfoToDistribution"
                as="xs:boolean"
                select="xs:boolean($copyDatasetInfoToDistribution)"/>

  <xsl:variable name="protocolToStandardPage"
                as="node()*">
    <entry key="http://www.opengeospatial.org/standards/cat">
      <value>csw</value>
      <value>ogc:csw</value>
    </entry>
    <entry key="http://www.opengeospatial.org/standards/sos">
      <value>sos</value>
      <value>ogc:sos</value>
    </entry>
    <entry key="http://www.opengeospatial.org/standards/sps">
      <value>sps</value>
      <value>ogc:sps</value>
    </entry>
    <entry key="http://www.opengeospatial.org/standards/wcs">
      <value>wcs</value>
      <value>ogc:wcs</value>
    </entry>
    <entry key="http://www.opengeospatial.org/standards/wfs">
      <value>wfs</value>
      <value>ogc:wfs</value>
    </entry>
    <entry key="http://www.opengeospatial.org/standards/wms">
      <value>wms</value>
      <value>ogc:wms</value>
    </entry>
    <entry key="http://www.opengeospatial.org/standards/wmts">
      <value>wmts</value>
      <value>ogc:wmts</value>
    </entry>
    <entry key="http://www.opengeospatial.org/standards/wps">
      <value>wps</value>
      <value>ogc:wps</value>
    </entry>
  </xsl:variable>


  <!--
  RDF Property:	dcat:landingPage
  Definition:	A Web page that can be navigated to in a Web browser to gain access to the catalog, a dataset, its distributions and/or additional information.
  Sub-property of:	foaf:page
  Range:	foaf:Document
  Usage note:	If the distribution(s) are accessible only through a landing page (i.e., direct download URLs are not known), then the landing page link SHOULD be duplicated as dcat:accessURL on a distribution. (see 5.7 Dataset available only behind some Web page)
  -->
  <xsl:template mode="iso19115-3-to-dcat"
                match="mdb:metadataLinkage">
    <dcat:landingPage>
      <foaf:Document rdf:about="{*/cit:linkage/*/text()}">
        <xsl:for-each select="*/cit:name">
          <xsl:call-template name="rdf-localised">
            <xsl:with-param name="nodeName" select="'dct:title'"/>
          </xsl:call-template>
        </xsl:for-each>
        <xsl:for-each select="*/cit:description">
          <xsl:call-template name="rdf-localised">
            <xsl:with-param name="nodeName" select="'dct:description'"/>
          </xsl:call-template>
        </xsl:for-each>
      </foaf:Document>
    </dcat:landingPage>
  </xsl:template>


  <xsl:template mode="iso19115-3-to-dcat"
                match="mdb:identificationInfo/*/mri:graphicOverview[*/mcc:fileName/*/text() != '']">
    <foaf:page>
      <foaf:Document rdf:about="{*/mcc:fileName/*/text()}">
        <xsl:apply-templates mode="iso19115-3-to-dcat"
                             select="*/mcc:fileDescription[normalize-space(.) != '']"/>
      </foaf:Document>
    </foaf:page>
  </xsl:template>

  <xsl:template mode="iso19115-3-to-dcat"
                match="mdb:distributionInfo/mrd:MD_Distribution/mrd:distributionFormat">
    <dcat:distribution>
      <dcat:Distribution>
        <xsl:apply-templates mode="iso19115-3-to-dcat-distribution" select="*/mrd:formatSpecificationCitation"/>
      </dcat:Distribution>
    </dcat:distribution>
  </xsl:template>

  <!--
  RDF Property:	dcat:distribution
  Definition:	An available distribution of the dataset.
  Sub-property of:	dcterms:relation
  Domain:	dcat:Dataset
  Range:	dcat:Distribution
  -->
  <xsl:template mode="iso19115-3-to-dcat"
                name="iso19115-3-to-dcat-distribution"
                match="mdb:distributionInfo//mrd:onLine
                            |mpc:portrayalCatalogueCitation/*/cit:onlineResource
                            |mrl:additionalDocumentation/*/cit:onlineResource
                            |mdq:reportReference/*/cit:onlineResource
                            |mdq:reportReference/*/cit:title[gcx:Anchor/@xlink:href]
                            |mdq:specification/*/cit:onlineResource
                            |mdq:specification/*/cit:title[gcx:Anchor/@xlink:href]
                            |mrc:featureCatalogueCitation/*/cit:onlineResource">
    <xsl:param name="additionalProperties"
               as="node()*"/>

    <xsl:variable name="url"
                  select="(*/cit:linkage/gco:CharacterString/text()|gcx:Anchor/@xlink:href)[1]"/>

    <xsl:variable name="protocol"
                  select="*/cit:protocol/*/text()"/>

    <xsl:variable name="mimeType"
                  select="*/cit:protocol/gcx:MimeFileType/@type"/>

    <xsl:variable name="protocolAnchor"
                  select="*/cit:protocol/*/@xlink:href"/>

    <xsl:variable name="function"
                  select="*/cit:function/*/@codeListValue"/>

    <!-- TODO: SEMICeu check if GetCapabilities in URL. Could check for protocols ?-->
    <xsl:variable name="pointsToService" select="false()"/>

    <xsl:variable name="isServiceMetadata"
                  select="exists(//mdb:MD_Metadata/mdb:identificationInfo/srv:SV_ServiceIdentification)" />

    <xsl:choose>
      <xsl:when test="normalize-space($url) = ''"/>
      <!--
      TODO: The rule to populate the documentation element depends a lot on the context.
      There is no known common guidance how to encode this and this can be quite user specific.
      For service, it is mandatory from an HVD point of view ("quality of service information is considered part of the generic documentation of a Data Service.").
      -->
      <xsl:when test="$function = ('documentation')
                                  or count(ancestor::mrl:additionalDocumentation) = 1
                                  or starts-with($url, 'https://directory.spatineo.com')">
        <foaf:documentation>
          <foaf:Document rdf:about="{$url}">
            <xsl:apply-templates mode="iso19115-3-to-dcat"
                                 select="*/cit:name[normalize-space(.) != '']
                                        |*/cit:description[normalize-space(.) != '']"/>
          </foaf:Document>
        </foaf:documentation>
      </xsl:when>
      <xsl:when test="$function = ('information', 'information.content', 'search', 'completeMetadata', 'browseGraphic', 'upload', 'emailService')
                                 or ($function = ('browsing') and matches($protocol, 'WWW:LINK.*'))
                                 or ((not($function) or $function = '') and (matches($protocol, 'WWW:LINK.*') or not($protocol) or $protocol = ''))">
      <foaf:page>
          <foaf:Document rdf:about="{$url}">
            <xsl:apply-templates mode="iso19115-3-to-dcat"
                                 select="*/cit:name[normalize-space(.) != '']
                                        |*/cit:description[normalize-space(.) != '']"/>
          </foaf:Document>
        </foaf:page>
      </xsl:when>
      <xsl:when test="$isServiceMetadata">
        <!-- Don't add distribution for service metadata -->
      </xsl:when>
      <xsl:otherwise>
        <dcat:distribution>
          <dcat:Distribution>
            <!--
             RDF Property:	dcterms:title
             Definition:	A name given to the distribution.

             RDF Property:	dcterms:description
             Definition:	A free-text account of the distribution.
             -->
            <xsl:apply-templates mode="iso19115-3-to-dcat"
                                 select="*/cit:name[normalize-space(.) != '']
                                        |*/cit:description[normalize-space(.) != '']"/>

            <!--
             RDF Property:	dcterms:issued
             Definition:	Date of formal issuance (e.g., publication) of the distribution.
            -->
            <xsl:for-each select="ancestor::mrd:MD_Distributor/mrd:distributionOrderProcess/*/mrd:plannedAvailableDateTime|
                                               ancestor::mdb:MD_Metadata/mdb:identificationInfo/*/mri:citation/*/cit:date/*[cit:dateType/*/@codeListValue = 'publication']">
              <xsl:apply-templates mode="iso19115-3-to-dcat"
                                   select=".">
                <xsl:with-param name="dateType" select="'publication'"/>
              </xsl:apply-templates>
            </xsl:for-each>

            <!--
            RDF Property:	dcterms:modified
            Definition:	Most recent date on which the distribution was changed, updated or modified.
            Range:	rdfs:Literal encoded using the relevant ISO 8601 Date and Time compliant string [DATETIME] and typed using the appropriate XML Schema datatype [XMLSCHEMA11-2] (xsd:gYear, xsd:gYearMonth, xsd:date, or xsd:dateTime).
            -->
            <xsl:for-each select="ancestor::mdb:MD_Metadata/mdb:identificationInfo/*/mri:citation/*/cit:date/*[cit:dateType/*/@codeListValue = 'revision']">
              <xsl:apply-templates mode="iso19115-3-to-dcat"
                                   select=".">
                <xsl:with-param name="dateType" select="'revision'"/>
              </xsl:apply-templates>
            </xsl:for-each>


            <!--
            RDF Property:	dcat:accessURL
            Definition:	A URL of the resource that gives access to a distribution of the dataset. E.g., landing page, feed, SPARQL endpoint.
            Domain:	dcat:Distribution
            Range:	rdfs:Resource
            Usage note:
            dcat:accessURL SHOULD be used for the URL of a service or location that can provide access to this distribution, typically through a Web form, query or API call.

            dcat:downloadURL is preferred for direct links to downloadable resources.

            If the distribution(s) are accessible only through a landing page (i.e., direct download URLs are not known), then the landing page URL associated with the dcat:Dataset SHOULD be duplicated as access URL on a distribution (see 5.7 Dataset available only behind some Web page).
            -->
            <!-- TODO: Can be multilingual -->
            <!--
            Message: ClassConstraint[rdfs:Resource]: Expected class :rdfs:Resource for
            -->
            <dcat:accessURL rdf:resource="{$url}"/>

            <!--
            RDF Property:	dcat:downloadURL
            Definition:	The URL of the downloadable file in a given format. E.g., CSV file or RDF file. The format is indicated by the distribution's dcterms:format and/or dcat:mediaType
            Domain:	dcat:Distribution
            Range:	rdfs:Resource
            Usage note:	dcat:downloadURL SHOULD be used for the URL at which this distribution is available directly, typically through a HTTP Get request

            This protocol list is GeoNetwork specific. It is not part of the ISO 19115-3 standard.
            -->
            <xsl:if test="matches($protocol, '.*DOWNLOAD.*|DB:.*|FILE:.*')">
              <dcat:downloadURL rdf:resource="{$url}"/>
            </xsl:if>


            <!--
            RDF Property:	spdx:checksum
            Definition:	The checksum property provides a mechanism that can be used to verify that the contents of a file or package have not changed [SPDX].
            Range:	spdx:Checksum
            Usage note:
            The checksum is related to the download URL.

            TODO: Not supported https://github.com/SEMICeu/GeoDCAT-AP/issues/89
            -->


            <!--
            RDF Property:	dcat:byteSize
            Definition:	The size of a distribution in bytes.
            Domain:	dcat:Distribution
            Range:	rdfs:Literal typically typed as xsd:nonNegativeInteger.
            Usage note:	The size in bytes can be approximated (as a non-negative integer) when the precise size is not known.
            Usage note:	While it is recommended that the size be given as an integer, alternative literals such as '1.5 MB' are sometimes used.
            -->
            <xsl:for-each
              select="ancestor::mrd:MD_DigitalTransferOptions/mrd:transferSize/*/text()[. castable as xs:double]">
              <!-- Not valid for eu-dcat-ap <dcat:byteSize><xsl:value-of select="concat(., ' MB')"/></dcat:byteSize>-->
              <dcat:byteSize rdf:datatype="http://www.w3.org/2001/XMLSchema#nonNegativeInteger">
                <xsl:value-of select="format-number(. * 1048576, '#')"/>
              </dcat:byteSize>
            </xsl:for-each>


            <!--
             RDF Property:	dcat:accessService
             Definition:	A data service that gives access to the distribution of the dataset
             Range:	dcat:DataService
             Usage note:	dcat:accessService SHOULD be used to link to a description of a dcat:DataService that can provide access to this distribution.

             DataService can be better described by an associated service metadata record.
            -->
            <xsl:if test="$function = ('download', 'offlineAccess', 'order', 'browsing', 'fileAccess')
                          or matches($protocol, 'OGC:WMS|OGC:WFS|OGC:WCS|OGC:WPS|OGC API Features|OGC API Coverages|ESRI:REST')">
              <dcat:accessService>
                <rdf:Description>
                  <rdf:type rdf:resource="http://www.w3.org/ns/dcat#DataService"/>
                  <xsl:apply-templates mode="iso19115-3-to-dcat"
                                       select="*/cit:name[normalize-space(.) != '']"/>
                  <dcat:endpointURL rdf:resource="{$url}"/>
                  <!-- TODO: GetCapabilities document
                  <dcat:endpointDescription rdf:resource="{$endpoint-description}"/>
                   -->

                  <xsl:variable name="standardPage"
                                as="node()?"
                                select="$protocolToStandardPage[value = lower-case($protocol)]"/>
                  <xsl:if test="$standardPage">
                    <dct:conformsTo>
                      <dct:Standard rdf:about="{$standardPage/@key}"/>
                    </dct:conformsTo>
                  </xsl:if>
                </rdf:Description>
              </dcat:accessService>
            </xsl:if>

            <!--
            RDF Property:	dcat:mediaType
            Definition:	The media type of the distribution as defined by IANA [IANA-MEDIA-TYPES].
            Sub-property of:	dcterms:format
            Domain:	dcat:Distribution
            Range:	dcterms:MediaType
            Usage note:	This property SHOULD be used when the media type of the distribution is defined in IANA [IANA-MEDIA-TYPES], otherwise dcterms:format MAY be used with different values.

            RDF Property:	dcat:compressFormat
            Definition:	The compression format of the distribution in which the data is contained in a compressed form, e.g., to reduce the size of the downloadable file.
            Range:	dcterms:MediaType
            Usage note:	This property to be used when the files in the distribution are compressed, e.g., in a ZIP file. The format SHOULD be expressed using a media type as defined by IANA [IANA-MEDIA-TYPES], if available.

            RDF Property:	dcat:packageFormat
            Definition:	The package format of the distribution in which one or more data files are grouped together, e.g., to enable a set of related files to be downloaded together.
            Range:	dcterms:MediaType
            Usage note:	This property to be used when the files in the distribution are packaged, e.g., in a TAR file, a ZIP file, a Frictionless Data Package or a Bagit file. The format SHOULD be expressed using a media type as defined by IANA [IANA-MEDIA-TYPES], if available.
            See also:	6.8.18 Property: compression format.

            Rule:
            * Use mimetype if any
            * Use WWW:DOWNLOAD:(.*=format) if any
            * fallback to ancestor::mrd:MD_DigitalTransferOptions/mrd:distributionFormat/*/mrd:formatSpecificationCitation
            -->
            <xsl:choose>
              <xsl:when test="$mimeType">
                <xsl:call-template name="rdf-format-as-mediatype">
                  <xsl:with-param name="format" select="$mimeType"/>
                </xsl:call-template>
              </xsl:when>
              <xsl:otherwise>
                <xsl:choose>
                  <xsl:when test="starts-with($protocol, 'WWW:DOWNLOAD:')">
                      <xsl:variable name="format"
                                    select="substring-after($protocol, 'WWW:DOWNLOAD:')"/>

                      <!-- The file format of the Distribution. -->
                    <xsl:call-template name="rdf-format-as-mediatype">
                        <xsl:with-param name="format" select="$format"/>
                    </xsl:call-template>

                      <!-- The media type of the Distribution as defined in the official register of media types managed by IANA. -->
                      <xsl:if test="matches($format, '\w+/[-+.\w]+')">
                        <dcat:mediaType>
                          <dct:MediaType>
                            <rdfs:label><xsl:value-of select="$format"/></rdfs:label>
                          </dct:MediaType>
                        </dcat:mediaType>
                      </xsl:if>
                  </xsl:when>
                  <xsl:otherwise>
                    <xsl:apply-templates mode="iso19115-3-to-dcat-distribution"
                                         select="ancestor::mrd:MD_DigitalTransferOptions/mrd:distributionFormat/*/mrd:formatSpecificationCitation"/>
                  </xsl:otherwise>
                </xsl:choose>
              </xsl:otherwise>
            </xsl:choose>

            <xsl:apply-templates mode="iso19115-3-to-dcat-distribution"
                                 select="ancestor::mrd:MD_DigitalTransferOptions/mrd:distributionFormat/*/mrd:fileDecompressionTechnique"/>


            <xsl:if test="$isCopyingDatasetInfoToDistribution">
              <!--
              [mco:useConstraints]
              RDF Property:	dcterms:license
              Definition:	A legal document under which the distribution is made available.
              Range:	dcterms:LicenseDocument
              Usage note:	Information about licenses and rights SHOULD be provided on the level of Distribution.
              Information about licenses and rights MAY be provided for a Dataset in addition to
              but not instead of the information provided for the Distributions of that Dataset.
              Providing license or rights information for a Dataset that is different from information provided
              for a Distribution of that Dataset SHOULD be avoided as this can create legal conflicts.
              See also guidance at 9. License and rights statements.
              -->
              <!--
             [mco:accessConstraints]
             RDF Property:	dcterms:accessRights
             Definition:	A rights statement that concerns how the distribution is accessed.
             Range:	dcterms:RightsStatement
             Usage note:	Information about licenses and rights MAY be provided for the Distribution. See also guidance at 9. License and rights statements.
             -->
              <xsl:apply-templates mode="iso19115-3-to-dcat"
                                   select="ancestor::mdb:MD_Metadata/mdb:identificationInfo/*/mri:resourceConstraints/*"/>

              <!--
              RDF Property:	dcterms:rights
              Definition:	Information about rights held in and over the distribution.
              Range:	dcterms:RightsStatement
              Usage note:
              dcterms:license, which is a sub-property of dcterms:rights, can be used to link a distribution to a license document. However, dcterms:rights allows linking to a rights statement that can include licensing information as well as other information that supplements the license such as attribution.

              Information about licenses and rights SHOULD be provided on the level of Distribution. Information about licenses and rights MAY be provided for a Dataset in addition to but not instead of the information provided for the Distributions of that Dataset. Providing license or rights information for a Dataset that is different from information provided for a Distribution of that Dataset SHOULD be avoided as this can create legal conflicts. See also guidance at 9. License and rights statements.

              See dcterms:license and dcterms:accessRights
              -->

              <!--
              RDF Property:	dcat:spatialResolutionInMeters
              Definition:	The minimum spatial separation resolvable in a dataset distribution, measured in meters.
              Range:	xsd:decimal
              Usage note:	If the dataset is an image or grid this should correspond to the spacing of items. For other kinds of spatial datasets, this property will usually indicate the smallest distance between items in the dataset.
              Usage note:	Alternative spatial resolutions might be provided as different dataset distributions
              -->
              <xsl:apply-templates mode="iso19115-3-to-dcat"
                                   select="ancestor::mdb:MD_Metadata/mdb:identificationInfo/*/mri:spatialResolution/*/mri:distance
                                          |ancestor::mdb:MD_Metadata/mdb:identificationInfo/*/mri:temporalResolution/*"/>

              <!--
              RDF Property:	odrl:hasPolicy
              Definition:	An ODRL conformant policy expressing the rights associated with the distribution.
              Range:	odrl:Policy
              Usage note:	Information about rights expressed as an ODRL policy [ODRL-MODEL] using the ODRL vocabulary [ODRL-VOCAB] MAY be provided for the distribution. See also guidance at 9. License and rights statements.

              Not supported
              -->

              <!--
              RDF Property:	dcterms:conformsTo
              Definition:	An established standard to which the distribution conforms.
              Range:	dcterms:Standard (A basis for comparison; a reference point against which other things can be evaluated.)
              Usage note:	This property SHOULD be used to indicate the model, schema, ontology, view or profile that this representation of a dataset conforms to. This is (generally) a complementary concern to the media-type or format.
              -->
              <xsl:apply-templates mode="iso19115-3-to-dcat"
                                   select="ancestor::mdb:MD_Metadata/mdb:dataQualityInfo/*/mdq:report/*/mdq:result[mdq:DQ_ConformanceResult and mdq:DQ_ConformanceResult/mdq:pass/*/text() = 'true']"/>

              <xsl:apply-templates mode="iso19115-3-to-dcat"
                                   select="ancestor::mdb:MD_Metadata/mdb:identificationInfo/*/mri:defaultLocale"/>
            </xsl:if>

            <xsl:copy-of select="$additionalProperties"/>
          </dcat:Distribution>
        </dcat:distribution>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>


  <xsl:template mode="iso19115-3-to-dcat-distribution"
                match="mrd:distributionFormat/*/mrd:formatSpecificationCitation">
    <xsl:call-template name="rdf-format-as-mediatype">
      <xsl:with-param name="format" select="*/cit:title/*/text()"/>
    </xsl:call-template>
  </xsl:template>


  <xsl:template mode="iso19115-3-to-dcat-distribution"
                match="mrd:distributionFormat/*/mrd:fileDecompressionTechnique">
    <xsl:call-template name="rdf-format-as-mediatype">
      <xsl:with-param name="elementName" select="'dcat:compressFormat'"/>
      <xsl:with-param name="format" select="*/text()"/>
    </xsl:call-template>
  </xsl:template>


  <xsl:template name="rdf-format-as-mediatype">
    <xsl:param name="elementName" as="xs:string" select="'dct:format'"/>
    <xsl:param name="format" as="xs:string"/>

    <xsl:variable name="formatUri"
                  as="xs:string?"
                  select="($formatLabelToUri[lower-case($format) = lower-case(text())]/@key)[1]"/>

    <xsl:variable name="rangeName"
                  as="xs:string"
                  select="if ($elementName = 'dct:format') then 'dct:MediaTypeOrExtent' else 'dct:MediaType'"/>
    <xsl:element name="{$elementName}">
      <xsl:choose>
        <xsl:when test="$formatUri">
          <xsl:element name="{$rangeName}">
            <xsl:attribute name="rdf:about" select="$formatUri"/>
          </xsl:element>
        </xsl:when>
        <xsl:otherwise>
          <xsl:element name="{$rangeName}">
            <rdfs:label>
              <xsl:value-of select="$format"/>
            </rdfs:label>
          </xsl:element>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:element>
  </xsl:template>

</xsl:stylesheet>
