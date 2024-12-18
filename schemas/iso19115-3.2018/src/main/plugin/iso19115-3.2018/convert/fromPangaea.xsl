<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:cit="http://standards.iso.org/iso/19115/-3/cit/2.0"
                xmlns:lan="http://standards.iso.org/iso/19115/-3/lan/1.0"
                xmlns:mcc="http://standards.iso.org/iso/19115/-3/mcc/1.0"
                xmlns:mco="http://standards.iso.org/iso/19115/-3/mco/1.0"
                xmlns:mdb="http://standards.iso.org/iso/19115/-3/mdb/2.0"
                xmlns:mrl="http://standards.iso.org/iso/19115/-3/mrl/2.0"
                xmlns:mrc="http://standards.iso.org/iso/19115/-3/mrc/2.0"
                xmlns:mrd="http://standards.iso.org/iso/19115/-3/mrd/1.0"
                xmlns:mri="http://standards.iso.org/iso/19115/-3/mri/1.0"
                xmlns:gco="http://standards.iso.org/iso/19115/-3/gco/1.0"
                xmlns:gcx="http://standards.iso.org/iso/19115/-3/gcx/1.0"
                xmlns:gex="http://standards.iso.org/iso/19115/-3/gex/1.0"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                xmlns:gml="http://www.opengis.net/gml/3.2"
                xmlns:md="http://www.pangaea.de/MetaData"
                exclude-result-prefixes="#all">

  <!--
  Conversion from
  http://ws.pangaea.de/schemas/pangaea/MetaData.xsd
  to ISO19115-3.

  Pangaea also provide an ISO 19139 conversion which is used as an example.
  https://ws.pangaea.de/oai/provider?verb=GetRecord&identifier=oai:pangaea.de:doi:10.1594/PANGAEA.820342&metadataPrefix=iso19139
  -->

  <xsl:import href="ISO19139/utility/create19115-3Namespaces.xsl"/>

  <xsl:output method="xml" indent="yes"/>

  <xsl:strip-space elements="*"/>

  <xsl:template match="/md:MetaData">
    <mdb:MD_Metadata>
      <xsl:call-template name="add-iso19115-3.2018-namespaces"/>
      <mdb:metadataIdentifier>
        <mcc:MD_Identifier>
          <mcc:code>
            <gco:CharacterString>
              <xsl:value-of select="concat('de.pangaea.', md:citation/@id)"/>
            </gco:CharacterString>
          </mcc:code>
        </mcc:MD_Identifier>
      </mdb:metadataIdentifier>
      <mdb:defaultLocale>
        <lan:PT_Locale>
          <!--
          Pangaea ISO19139 conversion provides
          <language>
          <LanguageCode codeList="http://www.loc.gov/standards/iso639-2/" codeListValue="und" >und</LanguageCode>
          </language>

          Use 'eng' as default.
          -->
          <lan:language>
            <lan:LanguageCode codeList="codeListLocation#LanguageCode"
                              codeListValue="eng"/>
          </lan:language>
          <lan:characterEncoding>
            <lan:MD_CharacterSetCode codeList="codeListLocation#MD_CharacterSetCode"
                                     codeListValue="utf8"/>
          </lan:characterEncoding>
        </lan:PT_Locale>
      </mdb:defaultLocale>
      <mdb:metadataScope>
        <mdb:MD_MetadataScope>
          <mdb:resourceScope>
            <!--
            <xs:element name="type" type="md:ReferenceTypeType" minOccurs="0"/>
            -->
            <mcc:MD_ScopeCode
              codeList="http://standards.iso.org/iso/19115/resources/Codelists/cat/codelists.xml#MD_ScopeCode"
              codeListValue="{(md:citation/md:type, 'dataset')[1]}"/>
          </mdb:resourceScope>
        </mdb:MD_MetadataScope>
      </mdb:metadataScope>
      <mdb:contact>
        <xsl:call-template name="build-pangaea-contact"/>
      </mdb:contact>

      <mdb:dateInfo>
        <cit:CI_Date>
          <cit:date>
            <gco:DateTime>
              <xsl:value-of select="md:technicalInfo/md:entry[@key = 'xmlLastModified']/@value"/>
            </gco:DateTime>
          </cit:date>
          <cit:dateType>
            <cit:CI_DateTypeCode codeList="codeListLocation#CI_DateTypeCode" codeListValue="revision"/>
          </cit:dateType>
        </cit:CI_Date>
      </mdb:dateInfo>
      <mdb:metadataStandard>
        <cit:CI_Citation>
          <cit:title>
            <gco:CharacterString>ISO 19115-3</gco:CharacterString>
          </cit:title>
        </cit:CI_Citation>
      </mdb:metadataStandard>
      <!--
      <xs:element name="URI" type="xs:anyURI" minOccurs="0"/>
      -->
      <xsl:for-each select="md:citation/md:URI">
        <mdb:metadataLinkage>
          <cit:CI_OnlineResource>
            <cit:linkage>
              <gco:CharacterString>
                <xsl:value-of select="."/>
              </gco:CharacterString>
            </cit:linkage>
            <cit:function>
              <cit:CI_OnLineFunctionCode
                codeList="http://standards.iso.org/iso/19115/resources/Codelists/cat/codelists.xml#CI_OnLineFunctionCode"
                codeListValue="completeMetadata"/>
            </cit:function>
          </cit:CI_OnlineResource>
        </mdb:metadataLinkage>
      </xsl:for-each>

      <mdb:identificationInfo>
        <mri:MD_DataIdentification>
          <mri:citation>
            <cit:CI_Citation>
              <cit:title>
                <gco:CharacterString>
                  <!--
                  <xs:element name="title" type="xs:string"/>
                  -->
                  <xsl:value-of select="md:citation/md:title"/>
                </gco:CharacterString>
              </cit:title>
              <cit:date>
                <cit:CI_Date>
                  <cit:date>
                    <gco:Date>
                      <!-- <xs:element name="year" type="xs:gYear"/> -->
                      <xsl:value-of select="md:citation/md:year"/>
                    </gco:Date>
                  </cit:date>
                  <cit:dateType>
                    <cit:CI_DateTypeCode codeList="codeListLocation#CI_DateTypeCode" codeListValue="publication"/>
                  </cit:dateType>
                </cit:CI_Date>
              </cit:date>
              <xsl:for-each select="md:citation/md:URI">
                <cit:identifier>
                  <mcc:MD_Identifier>
                    <mcc:code>
                      <gco:CharacterString>
                        <xsl:value-of select="."/>
                      </gco:CharacterString>
                    </mcc:code>
                  </mcc:MD_Identifier>
                </cit:identifier>
              </xsl:for-each>

              <!-- TODO Source to check-->
              <cit:presentationForm>
                <cit:CI_PresentationFormCode
                  codeList="http://standards.iso.org/iso/19115/resources/Codelists/cat/codelists.xml#CI_PresentationFormCode"
                  codeListValue="tableDigital"/>
              </cit:presentationForm>
            </cit:CI_Citation>
          </mri:citation>

          <!--
          <xs:element name="abstract" type="xs:string" minOccurs="0"/>
          -->
          <mri:abstract>
            <gco:CharacterString>
              <xsl:value-of select="md:abstract"/>
            </gco:CharacterString>
          </mri:abstract>

          <!-- TODO Source to check-->
          <mri:status>
            <mcc:MD_ProgressCode codeList="codeListLocation#MD_ProgressCode" codeListValue="{'completed'}"/>
          </mri:status>

          <!--
          <xs:complexType name="ResponsiblePartyType">
          <xs:sequence>
          <xs:element name="lastName" type="xs:string"/>
          <xs:element name="firstName" type="xs:string" minOccurs="0"/>
          <xs:element name="eMail" type="xs:anyURI" minOccurs="0"/>
          <xs:element name="URI" type="xs:anyURI" minOccurs="0"/>
          <xs:element name="orcid" type="xs:string" minOccurs="0"/>
          <xs:element name="affiliation" type="md:InstitutionType" minOccurs="0" maxOccurs="unbounded"/>
          </xs:sequence>
          <xs:attributeGroup ref="md:IdAttrGroup"/>
          </xs:complexType>
          -->
          <xsl:for-each select="md:citation/md:author">
            <mri:pointOfContact>
              <xsl:call-template name="build-contact">
                <xsl:with-param name="role" select="'author'"/>
              </xsl:call-template>
            </mri:pointOfContact>
          </xsl:for-each>

          <mri:pointOfContact>
            <xsl:call-template name="build-pangaea-contact">
              <xsl:with-param name="role" select="'publisher'"/>
            </xsl:call-template>
          </mri:pointOfContact>


          <!-- <xsl:for-each select="md:matrixColumn/md:PI">
             <mri:pointOfContact>
               <xsl:call-template name="build-contact">
                 <xsl:with-param name="role" select="'principalInvestigator'"/>
               </xsl:call-template>
             </mri:pointOfContact>
           </xsl:for-each>-->

          <!-- TODO Source to check-->
          <mri:spatialRepresentationType>
            <mcc:MD_SpatialRepresentationTypeCode
              codeList="http://standards.iso.org/iso/19115/resources/Codelists/cat/codelists.xml#MD_SpatialRepresentationTypeCode"
              codeListValue="{(md:extent/md:topoType, 'textTable')[1]}"/>
          </mri:spatialRepresentationType>

          <!-- TODO Source to check-->
          <mri:topicCategory>
            <mri:MD_TopicCategoryCode>geoscientificInformation</mri:MD_TopicCategoryCode>
          </mri:topicCategory>

          <!--
          <xs:complexType name="ShortExtentType">
            <xs:sequence>
              <xs:element name="geographic">
                <xs:complexType>
                  <xs:sequence>
                    <xs:element name="westBoundLongitude" type="xs:double"/>
                    <xs:element name="eastBoundLongitude" type="xs:double"/>
                    <xs:element name="southBoundLatitude" type="xs:double"/>
                    <xs:element name="northBoundLatitude" type="xs:double"/>
                    <xs:element name="meanLongitude" type="xs:double"/>
                    <xs:element name="meanLatitude" type="xs:double"/>
                    <xs:element name="location" minOccurs="0">
            -->
          <xsl:for-each select="md:extent">
            <mri:extent>
              <gex:EX_Extent>
                <xsl:for-each select="md:geographic">
                  <gex:geographicElement>
                    <gex:EX_GeographicBoundingBox>
                      <gex:westBoundLongitude>
                        <gco:Decimal>
                          <xsl:value-of select="md:westBoundLongitude"/>
                        </gco:Decimal>
                      </gex:westBoundLongitude>
                      <gex:eastBoundLongitude>
                        <gco:Decimal>
                          <xsl:value-of select="md:eastBoundLongitude"/>
                        </gco:Decimal>
                      </gex:eastBoundLongitude>
                      <gex:southBoundLatitude>
                        <gco:Decimal>
                          <xsl:value-of select="md:southBoundLatitude"/>
                        </gco:Decimal>
                      </gex:southBoundLatitude>
                      <gex:northBoundLatitude>
                        <gco:Decimal>
                          <xsl:value-of select="md:northBoundLatitude"/>
                        </gco:Decimal>
                      </gex:northBoundLatitude>
                    </gex:EX_GeographicBoundingBox>
                  </gex:geographicElement>
                </xsl:for-each>
                <xsl:for-each select="md:temporal">
                  <gex:temporalElement>
                    <gex:EX_TemporalExtent>
                      <gex:extent>
                        <gml:TimePeriod gml:id="{generate-id()}">
                          <gml:beginPosition>
                            <xsl:value-of select="md:minDateTime"/>
                          </gml:beginPosition>
                          <gml:endPosition>
                            <xsl:value-of select="md:maxDateTime"/>
                          </gml:endPosition>
                        </gml:TimePeriod>
                      </gex:extent>
                    </gex:EX_TemporalExtent>
                  </gex:temporalElement>
                </xsl:for-each>
                <xsl:for-each select="md:elevation">
                  <gex:verticalElement>
                    <gex:EX_VerticalExtent>
                      <gex:minimumValue>
                        <gco:Real>
                          <xsl:value-of select="md:min"/>
                        </gco:Real>
                      </gex:minimumValue>
                      <gex:maximumValue>
                        <gco:Real>
                          <xsl:value-of select="md:max"/>
                        </gco:Real>
                      </gex:maximumValue>
                      <!-- TODO
                      <md:elevation geocodeId="geocode1619" name="DEPTH, water" unit="m" >
                      <verticalCRS xlink:href="http://ws.pangaea.de/schemas/iso19139/pangaea-crs-dict.xml#xpointer(//*[@gml:id='vertCRS.geocode1619'])" xlink:title="Definition of geocode: DEPTH, water" ></verticalCRS>
                      -->
                    </gex:EX_VerticalExtent>
                  </gex:verticalElement>
                </xsl:for-each>
              </gex:EX_Extent>
            </mri:extent>
          </xsl:for-each>

          <!--
          <xs:element name="license" type="md:LicenseType" minOccurs="0"/>

          <xs:complexType name="LicenseType">
            <xs:complexContent>
              <xs:extension base="md:LinkedLabelNameType">
                <xs:attribute name="preliminary" type="xs:boolean"/>

          <xs:complexType name="LinkedLabelNameType">
            <xs:sequence>
              <xs:element name="label" type="xs:string" minOccurs="0"/>
              <xs:element name="name" type="xs:string" minOccurs="0"/>
              <xs:element name="URI" type="xs:anyURI" minOccurs="0"/>
          -->

          <xsl:for-each select="md:license">
            <mri:resourceConstraints>
              <mco:MD_LegalConstraints>
                <mco:accessConstraints>
                  <mco:MD_RestrictionCode
                    codeList="http://standards.iso.org/iso/19115/resources/Codelists/cat/codelists.xml#MD_RestrictionCode"
                    codeListValue="copyright"/>
                </mco:accessConstraints>
                <mco:useConstraints>
                  <mco:MD_RestrictionCode
                    codeList="http://standards.iso.org/iso/19115/resources/Codelists/cat/codelists.xml#MD_RestrictionCode"
                    codeListValue="copyright"/>
                </mco:useConstraints>
                <mco:otherConstraints>
                  <xsl:element name="{if (md:URI) then 'gcx:Anchor' else 'gco:CharacterString'}">
                    <xsl:if test="md:URI">
                      <xsl:attribute name="xlink:href">
                        <xsl:value-of select="md:URI"/>
                      </xsl:attribute>
                    </xsl:if>
                    <xsl:value-of select="md:name"/>
                  </xsl:element>
                </mco:otherConstraints>
              </mco:MD_LegalConstraints>
            </mri:resourceConstraints>
          </xsl:for-each>


          <!-- <md:parentURI>https://doi.org/10.1594/PANGAEA.820343</md:parentURI> -->

          <xsl:for-each select="md:citation/md:parentURI">
            <mri:associatedResource>
              <mri:MD_AssociatedResource>
                <mri:associationType>
                  <mri:DS_AssociationTypeCode
                    codeList="http://standards.iso.org/iso/19115/resources/Codelists/cat/codelists.xml#DS_AssociationTypeCode"
                    codeListValue="largerWorkCitation"/>
                </mri:associationType>
                <mri:metadataReference xlink:href="{.}" xlink:title="{.}"/>
              </mri:MD_AssociatedResource>
            </mri:associatedResource>
          </xsl:for-each>

          <!-- TODO Source to check-->
          <mri:defaultLocale>
            <lan:PT_Locale>
              <lan:language>
                <lan:LanguageCode codeList="codeListLocation#LanguageCode"
                                  codeListValue="{'eng'}"/>
              </lan:language>
              <lan:characterEncoding>
                <lan:MD_CharacterSetCode codeList="codeListLocation#MD_CharacterSetCode"
                                         codeListValue="utf8"/>
              </lan:characterEncoding>
            </lan:PT_Locale>
          </mri:defaultLocale>

          <!--
          <xs:element name="comment" type="xs:string" minOccurs="0"/>
          -->
          <xsl:for-each select="md:comment">
            <mri:supplementalInformation>
              <gco:CharacterString>
                <xsl:value-of select="."/>
              </gco:CharacterString>
            </mri:supplementalInformation>
          </xsl:for-each>
        </mri:MD_DataIdentification>
      </mdb:identificationInfo>


      <xsl:variable name="coverageContentType" as="node()*">
        <entry key="thematicClassification" value=""/>
        <entry key="physicalMeasurement" value=""/>
      </xsl:variable>

      <xsl:for-each select="md:matrixColumn">
        <mdb:contentInfo>
          <mrc:MD_CoverageDescription id="{@id}">
            <xsl:for-each select="md:parameter">
              <mrc:attributeDescription>
                <gco:RecordType>
                  <xsl:value-of select="md:name"/>
                </gco:RecordType>
              </mrc:attributeDescription>
              <mrc:attributeGroup>
                <mrc:MD_AttributeGroup>
                  <mrc:contentType>
                    <!-- TODO Source to check-->
                    <mrc:MD_CoverageContentTypeCode
                      codeList="http://standards.iso.org/iso/19115/resources/Codelists/cat/codelists.xml#MD_CoverageContentTypeCode"
                      codeListValue="physicalMeasurement"/>
                  </mrc:contentType>
                  <mrc:attribute>
                    <mrc:MD_Band>
                      <xsl:for-each select="../md:method">
                        <mrc:description>
                          <gco:CharacterString>
                            <xsl:value-of select="md:name"/>
                          </gco:CharacterString>
                        </mrc:description>
                      </xsl:for-each>
                      <mrc:units>
                        <gml:UnitDefinition gml:id="{generate-id()}">
                          <gml:identifier codeSpace="PANGAEA">
                            <xsl:value-of select="md:unit"/>
                          </gml:identifier>
                          <gml:name>
                            <xsl:value-of select="md:unit"/>
                          </gml:name>
                        </gml:UnitDefinition>
                      </mrc:units>
                    </mrc:MD_Band>
                  </mrc:attribute>
                </mrc:MD_AttributeGroup>
              </mrc:attributeGroup>
            </xsl:for-each>
          </mrc:MD_CoverageDescription>
        </mdb:contentInfo>
      </xsl:for-each>


      <mdb:distributionInfo>
        <mrd:MD_Distribution>
          <xsl:for-each select="md:technicalInfo/md:entry[@key = 'mimeType']/@value">
            <mrd:distributionFormat>
              <mrd:MD_Format>
                <mrd:formatSpecificationCitation>
                  <cit:CI_Citation>
                    <cit:title>
                      <gco:CharacterString>
                        <xsl:value-of select="."/>
                      </gco:CharacterString>
                    </cit:title>
                  </cit:CI_Citation>
                </mrd:formatSpecificationCitation>
              </mrd:MD_Format>
            </mrd:distributionFormat>
          </xsl:for-each>


          <mrd:transferOptions>
            <mrd:MD_DigitalTransferOptions>
              <xsl:for-each select="md:technicalInfo/md:entry[@key = 'mimeType']/@value">
                <mrd:onLine>
                  <cit:CI_OnlineResource>
                    <cit:linkage>
                      <gco:CharacterString>
                        <xsl:value-of
                          select="concat(replace(ancestor::md:MetaData/md:citation/md:URI, 'https://doi.org', 'https://doi.pangaea.de'), '?format=textfile')"/>
                      </gco:CharacterString>
                    </cit:linkage>
                    <cit:protocol>
                      <gco:CharacterString>
                        WWW:DOWNLOAD
                      </gco:CharacterString>
                    </cit:protocol>
                    <cit:name>
                      <gco:CharacterString>
                        <xsl:value-of select="ancestor::md:technicalInfo/md:entry[@key = 'filename']/@value"/>
                      </gco:CharacterString>
                    </cit:name>
                    <cit:function>
                      <cit:CI_OnLineFunctionCode
                        codeList="http://www.isotc211.org/2005/resources/Codelist/gmxCodelists.xml#CI_OnLineFunctionCode"
                        codeListValue="download"/>
                    </cit:function>
                  </cit:CI_OnlineResource>
                </mrd:onLine>
              </xsl:for-each>

              <xsl:for-each select="md:citation/md:URI">
                <mrd:onLine>
                  <cit:CI_OnlineResource>
                    <cit:linkage>
                      <gco:CharacterString>
                        <xsl:value-of select="."/>
                      </gco:CharacterString>
                    </cit:linkage>
                    <cit:protocol>
                      <gco:CharacterString>
                        DOI
                      </gco:CharacterString>
                    </cit:protocol>
                    <cit:function>
                      <cit:CI_OnLineFunctionCode
                        codeList="http://www.isotc211.org/2005/resources/Codelist/gmxCodelists.xml#CI_OnLineFunctionCode"
                        codeListValue="information"/>
                    </cit:function>
                  </cit:CI_OnlineResource>
                </mrd:onLine>
              </xsl:for-each>

            </mrd:MD_DigitalTransferOptions>
          </mrd:transferOptions>
        </mrd:MD_Distribution>
      </mdb:distributionInfo>

      <mdb:resourceLineage>
        <mrl:LI_Lineage>
          <mrl:statement>
            <gco:CharacterString>
              <!-- TODO Source to check-->
              The data set was checked for completeness, correctness, and consistency of metainformation. Validity of
              used methods was checked and - if applicable - precision and range of data.
            </gco:CharacterString>
          </mrl:statement>
          <mrl:scope>
            <mcc:MD_Scope>
              <mcc:level>
                <mcc:MD_ScopeCode codeList="codeListLocation#MD_ScopeCode" codeListValue="dataset"/>
              </mcc:level>
            </mcc:MD_Scope>
          </mrl:scope>


          <!--
          TODO: Check source
          relationTypeId ?
          <md:reference dataciteRelType="References" group="210" id="ref60752" relationType="Related to" relationTypeId="12" >
          <md:author id="ref60752.author49092" >
          -->
          <xsl:for-each select="md:reference">
            <mrl:source
              xlink:title="{md:title}"
              xlink:href="{md:URI}"/>
          </xsl:for-each>

          <!--
          <md:event id="event2705449" >
            <md:label>CI_V01_110710_120716_HW0012A</md:label>
            <md:latitude>37.28802</md:latitude>
            <md:longitude>-32.27589</md:longitude>
            <md:elevation>-1700.0</md:elevation>
            <md:dateTime>2011-07-07T16:00:00</md:dateTime>
            <md:dateTime2>2012-07-17T06:24:00</md:dateTime2>
            <md:location id="event2705449.term30853" >
              <md:name>Lucky Strike Hydrothermal Field, Cimendef</md:name>
            </md:location>
            <md:method id="event2705449.method5105" >
              <md:name>Temperature recorder</md:name>
              <md:optionalName>TEMP-R</md:optionalName>
              <md:term id="event2705449.method5105.term67658" terminologyId="3" terminologyLabel="PAN-M&D" >
              <md:name>Temperature sensors</md:name>
              </md:term>
              <md:term id="event2705449.method5105.term2299603" semanticURI="SDN:L05::134" terminologyId="21" terminologyLabel="NERC-L05" >
              <md:name>water temperature sensor</md:name>
              <md:URI>http://vocab.nerc.ac.uk/collection/L05/current/134/</md:URI>
              </md:term>
            </md:method>
            <md:comment>Record length: 37.201 days, sampling interval: 1440 s</md:comment>
          </md:event>
          -->
          <xsl:for-each select="md:event">
            <mrl:processStep>
              <mrl:LI_ProcessStep>
                <mrl:description>
                  <gco:CharacterString>
                    EVENT
                    * LABEL:
                    <xsl:value-of select="md:label"/>
                    * LATITUDE:
                    <xsl:value-of select="md:latitude"/>
                    * LONGITUDE:
                    <xsl:value-of select="md:longitude"/>
                    * DATE/TIME START:
                    <xsl:value-of select="md:elevation"/>
                    * DATE/TIME END:
                    <xsl:value-of select="md:dateTime"/>
                    * ELEVATION:
                    <xsl:value-of select="md:dateTime2"/>
                    * LOCATION:
                    <xsl:value-of select="md:location/md:name"/>
                    * METHOD/DEVICE:
                    <xsl:value-of select="md:method/md:name"/>
                  </gco:CharacterString>
                </mrl:description>
                <xsl:for-each select="md:comment">
                  <mrl:rationale>
                    <gco:CharacterString>
                      <xsl:value-of select="."/>
                    </gco:CharacterString>
                  </mrl:rationale>
                </xsl:for-each>
                <mrl:stepDateTime>
                  <gml:TimeInstant gml:id="{generate-id()}">
                    <gml:timePosition>
                      <xsl:value-of select="md:dateTime"/>
                    </gml:timePosition>
                  </gml:TimeInstant>
                </mrl:stepDateTime>
              </mrl:LI_ProcessStep>
            </mrl:processStep>
          </xsl:for-each>

        </mrl:LI_Lineage>
      </mdb:resourceLineage>
    </mdb:MD_Metadata>
  </xsl:template>


  <xsl:template name="build-contact">
    <xsl:param name="role" select="'pointOfContact'"/>

    <cit:CI_Responsibility>
      <cit:role>
        <cit:CI_RoleCode codeList="http://standards.iso.org/iso/19115/resources/Codelists/cat/codelists.xml#CI_RoleCode"
                         codeListValue="author"/>
      </cit:role>
      <cit:party>
        <cit:CI_Organisation>
          <!--
          <xs:complexType name="LinkedNameType">
          <xs:sequence>
          <xs:element name="name" type="xs:string"/>
          <xs:element name="optionalName" type="xs:string" minOccurs="0"/>
          <xs:element name="URI" type="xs:anyURI" minOccurs="0"/>
          </xs:sequence>
          <xs:attributeGroup ref="md:IdAttrGroup"/>
          </xs:complexType>
          -->
          <cit:name>
            <gco:CharacterString>
              <xsl:value-of select="md:affiliation/md:name"/>
            </gco:CharacterString>
          </cit:name>
          <xsl:for-each select="md:affiliation/md:URI|md:URI">
            <cit:contactInfo>
              <cit:CI_Contact>
                <cit:onlineResource>
                  <cit:CI_OnlineResource>
                    <cit:linkage>
                      <gco:CharacterString>
                        <xsl:value-of select="."/>
                      </gco:CharacterString>
                    </cit:linkage>
                  </cit:CI_OnlineResource>
                </cit:onlineResource>
              </cit:CI_Contact>
            </cit:contactInfo>
          </xsl:for-each>
          <cit:individual>
            <cit:CI_Individual>
              <cit:name>
                <gco:CharacterString>
                  <xsl:value-of select="concat(md:firstName, ' ', md:lastName)"/>
                </gco:CharacterString>
              </cit:name>
              <xsl:for-each select="md:eMail">
                <cit:contactInfo>
                  <cit:CI_Contact>
                    <cit:address>
                      <cit:CI_Address>
                        <cit:electronicMailAddress>
                          <gco:CharacterString>
                            <xsl:value-of select="."/>
                          </gco:CharacterString>
                        </cit:electronicMailAddress>
                      </cit:CI_Address>
                    </cit:address>
                  </cit:CI_Contact>
                </cit:contactInfo>
              </xsl:for-each>
              <xsl:for-each select="md:orcid">
                <cit:partyIdentifier>
                  <mcc:MD_Identifier>
                    <mcc:code>
                      <gco:CharacterString>
                        <xsl:value-of select="."/>
                      </gco:CharacterString>
                    </mcc:code>
                    <mcc:codeSpace>
                      <gco:CharacterString>ORCID</gco:CharacterString>
                    </mcc:codeSpace>
                  </mcc:MD_Identifier>
                </cit:partyIdentifier>
              </xsl:for-each>
            </cit:CI_Individual>
          </cit:individual>
        </cit:CI_Organisation>
      </cit:party>
    </cit:CI_Responsibility>
  </xsl:template>

  <xsl:template name="build-pangaea-contact">
    <xsl:param name="role" select="'pointOfContact'"/>

    <cit:CI_Responsibility>
      <cit:role>
        <cit:CI_RoleCode codeList="codeListLocation#CI_RoleCode" codeListValue="{$role}"/>
      </cit:role>
      <cit:party>
        <cit:CI_Organisation>
          <cit:name>
            <gco:CharacterString>
              PANGAEA
            </gco:CharacterString>
          </cit:name>
          <cit:contactInfo>
            <cit:CI_Contact>
              <cit:address>
                <cit:CI_Address>
                  <cit:electronicMailAddress>
                    <gco:CharacterString>
                      info@pangaea.de
                    </gco:CharacterString>
                  </cit:electronicMailAddress>
                </cit:CI_Address>
              </cit:address>
              <cit:onlineResource>
                <cit:CI_OnlineResource>
                  <cit:linkage>
                    <gco:CharacterString>https://www.pangaea.de</gco:CharacterString>
                  </cit:linkage>
                </cit:CI_OnlineResource>
              </cit:onlineResource>
            </cit:CI_Contact>
          </cit:contactInfo>
        </cit:CI_Organisation>
      </cit:party>
    </cit:CI_Responsibility>
  </xsl:template>

</xsl:stylesheet>
