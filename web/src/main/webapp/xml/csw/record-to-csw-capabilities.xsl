<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                xmlns:cit="http://standards.iso.org/iso/19115/-3/cit/2.0"
                xmlns:lan="http://standards.iso.org/iso/19115/-3/lan/1.0"
                xmlns:mco="http://standards.iso.org/iso/19115/-3/mco/1.0"
                xmlns:mdq="http://standards.iso.org/iso/19157/-2/mdq/1.0"
                xmlns:mdb="http://standards.iso.org/iso/19115/-3/mdb/2.0"
                xmlns:mrd="http://standards.iso.org/iso/19115/-3/mrd/1.0"
                xmlns:mri="http://standards.iso.org/iso/19115/-3/mri/1.0"
                xmlns:gco="http://standards.iso.org/iso/19115/-3/gco/1.0"
                xmlns:gex="http://standards.iso.org/iso/19115/-3/gex/1.0"
                xmlns:gcx="http://standards.iso.org/iso/19115/-3/gcx/1.0"
                xmlns:srv="http://www.isotc211.org/2005/srv"
                xmlns:srv19115="http://standards.iso.org/iso/19115/-3/srv/2.1"
                xmlns:gml="http://www.opengis.net/gml/3.2"
                xmlns:gmd="http://www.isotc211.org/2005/gmd"
                xmlns:gco139="http://www.isotc211.org/2005/gco"
                xmlns:gmx="http://www.isotc211.org/2005/gmx"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                xmlns:util="java:org.fao.geonet.util.XslUtil"
                exclude-result-prefixes="#all">

  <xsl:param name="outputLanguage" select="''"/>


  <xsl:variable name="mainLanguage"
                select="//mdb:defaultLocale/*/lan:language/*/@codeListValue|//gmd:language/*/@codeListValue"/>

  <xsl:variable name="otherLanguages"
                select="//mdb:otherLocale/*/lan:language|//gmd:locale/*/gmd:languageCode"/>

  <xsl:variable name="language"
                select="if ($outputLanguage != '' and ($outputLanguage = $mainLanguage or count($otherLanguages[*/@codeListValue = $outputLanguage]) > 0))
                                then concat('#', $otherLanguages[*/@codeListValue = $outputLanguage]/../@id)
                                else ''"/>

  <xsl:variable name="endPoint"
                select="//srv19115:containsOperations/srv19115:SV_OperationMetadata/srv19115:connectPoint/(cit:CI_OnlineResource[cit:protocol/gco:CharacterString='OGC:CSW']/cit:linkage/gco:CharacterString|gmd:CI_OnlineResource[gmd:protocol/*/text()='OGC:CSW']/gmd:linkage/gmd:URL)" />

  <xsl:variable name="endPointValue"
                select="if (string($endPoint)) then $endPoint else '$PROTOCOL://$HOST$PORT$SERVLET/$NODE_ID/$LOCALE/$END-POINT'"/>

  <xsl:variable name="endPointValuePublication"
                select="if (string($endPoint)) then $endPoint else '$PROTOCOL://$HOST$PORT$SERVLET/$NODE_ID/$LOCALE/csw-publication'"/>
  
  <xsl:template mode="localised" match="*">
    <xsl:variable name="translation"
                  select="*/*:textGroup/*[@locale = $language]"/>
    <xsl:choose>
      <xsl:when test="$translation != ''">
        <xsl:value-of select="$translation"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="gco:CharacterString|gco139:CharacterString|gcx:Anchor|gmx:Anchor"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="/">
    <xsl:variable name="inspireThemes"
                  select="//mri:descriptiveKeywords/*[mri:thesaurusName/*/cit:title/*/text() = 'GEMET - INSPIRE themes, version 1.0']/mri:keyword|
                          //gmd:identificationInfo/*/gmd:descriptiveKeywords/*[gmd:thesaurusName/*/gmd:title/*/text() = 'GEMET - INSPIRE themes, version 1.0']/gmd:keyword"/>
    <xsl:variable name="isInspire"
                  select="count($inspireThemes) > 0"/>

    <csw:Capabilities xmlns:csw="http://www.opengis.net/cat/csw/2.0.2"
                      xmlns:gml="http://www.opengis.net/gml"
                      xmlns:ows="http://www.opengis.net/ows"
                      xmlns:ogc="http://www.opengis.net/ogc"
                      xmlns:xlink="http://www.w3.org/1999/xlink"
                      xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                      xmlns:inspire_ds="http://inspire.ec.europa.eu/schemas/inspire_ds/1.0"
                      xmlns:inspire_com="http://inspire.ec.europa.eu/schemas/common/1.0"
                      version="2.0.2">
      <xsl:attribute name="xsi:schemaLocation"
                     select="if ($isInspire)
                             then 'http://www.opengis.net/cat/csw/2.0.2 http://schemas.opengis.net/csw/2.0.2/CSW-discovery.xsd http://inspire.ec.europa.eu/schemas/inspire_ds/1.0 http://inspire.ec.europa.eu/schemas/inspire_ds/1.0/inspire_ds.xsd'
                             else 'http://www.opengis.net/cat/csw/2.0.2 http://schemas.opengis.net/csw/2.0.2/CSW-discovery.xsd'"/>

      <ows:ServiceIdentification>
        <ows:Title>
          <xsl:apply-templates mode="localised"
                               select="//mdb:identificationInfo/*/mri:citation/*/cit:title|
                                       //gmd:identificationInfo/*/gmd:citation/*/gmd:title"/>
        </ows:Title>
        <ows:Abstract>
          <xsl:apply-templates mode="localised"
                               select="//mdb:identificationInfo/*/mri:abstract|
                                                   //gmd:identificationInfo/*/gmd:abstract"/>
        </ows:Abstract>
        <ows:Keywords>
          <xsl:for-each select="(//mdb:identificationInfo/*/mri:descriptiveKeywords/*/mri:keyword|
                                 //gmd:identificationInfo/*/gmd:descriptiveKeywords/*/gmd:keyword)">
            <ows:Keyword>
              <xsl:apply-templates mode="localised"
                                   select="."/>
            </ows:Keyword>
          </xsl:for-each>
        </ows:Keywords>
        <ows:ServiceType>CSW</ows:ServiceType>
        <ows:ServiceTypeVersion>2.0.2</ows:ServiceTypeVersion>
        <ows:Fees>
          <xsl:apply-templates mode="localised"
                               select="//mdb:distributionInfo/*/mrd:distributor/*/mrd:distributionOrderProcess/*/mrd:orderingInstructions|
                                       //gmd:distributionInfo/*/gmd:distributor/*/gmd:distributionOrderProcess/*/gmd:orderingInstructions"/>
        </ows:Fees>
        <ows:AccessConstraints>
          <xsl:apply-templates mode="localised"
                               select="//mdb:identificationInfo/*/mri:resourceConstraints/*[mco:accessConstraints]/mco:otherConstraints|
                                       //gmd:identificationInfo/*/gmd:resourceConstraints/*[gmd:accessConstraints]/gmd:otherConstraints"/>
        </ows:AccessConstraints>
      </ows:ServiceIdentification>
      <xsl:for-each select="//mdb:identificationInfo/*/mri:pointOfContact[1]/*|
                            //gmd:identificationInfo/*/gmd:pointOfContact[1]/*">
        <ows:ServiceProvider>
          <ows:ProviderName>
            <xsl:apply-templates mode="localised"
                                 select="cit:party/cit:CI_Organisation/cit:name|gmd:organisationName"/>
          </ows:ProviderName>
          <ows:ProviderSite xlink:href="{cit:party/*/cit:contactInfo/*/cit:onlineResource/*/cit:linkage/*/text()|
                                         gmd:contactInfo/*/gmd:onlineResource/*/gmd:linkage/*/text()}"/>
          <ows:ServiceContact>
            <xsl:for-each select="cit:party/*/cit:individual/*/cit:name|gmd:individualName">
              <ows:IndividualName>
                <xsl:apply-templates mode="localised"
                                     select="."/>
              </ows:IndividualName>
            </xsl:for-each>
            <xsl:for-each select="cit:party/*/cit:individual/*/cit:positionName|gmd:positionName">
              <ows:PositionName>
                <xsl:apply-templates mode="localised"
                                     select="."/>
              </ows:PositionName>
            </xsl:for-each>
            <ows:ContactInfo>
              <xsl:if test="cit:party/*/cit:contactInfo/*/cit:phone|gmd:contactInfo/*/gmd:phone">
                <ows:Phone>
                  <xsl:for-each
                    select="cit:party/*/cit:contactInfo/*/cit:phone/*[cit:numberType/*/@codeListValue = 'voice']/cit:number|gmd:contactInfo/*/gmd:phone/*/gmd:voice">
                    <ows:Voice>
                      <xsl:apply-templates mode="localised" select="."/>
                    </ows:Voice>
                  </xsl:for-each>
                  <xsl:for-each
                    select="cit:party/*/cit:contactInfo/*/cit:phone/*[cit:numberType/*/@codeListValue = 'facsimilie']/cit:number|gmd:contactInfo/*/gmd:phone/*/gmd:facsimile">
                    <ows:Voice>
                      <xsl:apply-templates mode="localised" select="."/>
                    </ows:Voice>
                  </xsl:for-each>
                </ows:Phone>
              </xsl:if>
              <ows:Address>
                <xsl:for-each select="cit:party/*/cit:contactInfo/*/cit:address/*/cit:deliveryPoint[normalize-space(.) != '']|
                                      gmd:contactInfo/*/gmd:address/*/gmd:deliveryPoint[normalize-space(.) != '']">
                  <ows:DeliveryPoint>
                    <xsl:apply-templates mode="localised"
                                         select="."/>
                  </ows:DeliveryPoint>
                </xsl:for-each>
                <xsl:for-each select="cit:party/*/cit:contactInfo/*/cit:address/*/cit:city[normalize-space(.) != '']|
                                      gmd:contactInfo/*/gmd:address/*/gmd:city[normalize-space(.) != '']">
                  <ows:City>
                    <xsl:apply-templates mode="localised"
                                         select="."/>
                  </ows:City>
                </xsl:for-each>
                <xsl:for-each select="cit:party/*/cit:contactInfo/*/cit:address/*/cit:administrativeArea[normalize-space(.) != '']|
                                      gmd:contactInfo/*/gmd:address/*/gmd:administrativeArea[normalize-space(.) != '']">
                  <ows:AdministrativeArea>
                    <xsl:apply-templates mode="localised"
                                         select="."/>
                  </ows:AdministrativeArea>
                </xsl:for-each>
                <xsl:for-each select="cit:party/*/cit:contactInfo/*/cit:address/*/cit:postalCode[normalize-space(.) != '']|
                                      gmd:contactInfo/*/gmd:address/*/gmd:postalCode[normalize-space(.) != '']">
                  <ows:PostalCode>
                    <xsl:apply-templates mode="localised"
                                         select="."/>
                  </ows:PostalCode>
                </xsl:for-each>
                <xsl:for-each select="cit:party/*/cit:contactInfo/*/cit:address/*/cit:country[normalize-space(.) != '']|
                                      gmd:contactInfo/*/gmd:address/*/gmd:country[normalize-space(.) != '']">
                  <ows:Country>
                    <xsl:apply-templates mode="localised"
                                         select="."/>
                  </ows:Country>
                </xsl:for-each>
                <xsl:for-each select="cit:party/*/cit:contactInfo/*/cit:address/*/cit:electronicMailAddress[normalize-space(.) != '']|
                                      gmd:contactInfo/*/gmd:address/*/gmd:electronicMailAddress[normalize-space(.) != '']">
                  <ows:ElectronicMailAddress>
                    <xsl:apply-templates mode="localised"
                                         select="."/>
                  </ows:ElectronicMailAddress>
                </xsl:for-each>
              </ows:Address>
              <xsl:for-each select="cit:party/*/cit:contactInfo/*/cit:hoursOfService[normalize-space(.) != '']|
                                      gmd:contactInfo/*/gmd:hoursOfService[normalize-space(.) != '']">
                <ows:HoursOfService>
                  <xsl:apply-templates mode="localised"
                                       select="."/>
                </ows:HoursOfService>
              </xsl:for-each>
              <xsl:for-each select="cit:party/*/cit:contactInfo/*/cit:contactInstructions[normalize-space(.) != '']|
                                      gmd:contactInfo/*/gmd:contactInstructions[normalize-space(.) != '']">
                <ows:ContactInstructions>
                  <xsl:apply-templates mode="localised"
                                       select="."/>
                </ows:ContactInstructions>
              </xsl:for-each>
            </ows:ContactInfo>
            <ows:Role>pointOfContact</ows:Role>
          </ows:ServiceContact>
        </ows:ServiceProvider>
      </xsl:for-each>
      <ows:OperationsMetadata>
        <ows:Operation name="GetCapabilities">
          <ows:DCP>
            <ows:HTTP>
              <ows:Get xlink:href="{$endPointValue}"/>
              <ows:Post xlink:href="{$endPointValue}"/>
            </ows:HTTP>
          </ows:DCP>
          <ows:Parameter name="sections">
            <ows:Value>ServiceIdentification</ows:Value>
            <ows:Value>ServiceProvider</ows:Value>
            <ows:Value>OperationsMetadata</ows:Value>
            <ows:Value>Filter_Capabilities</ows:Value>
          </ows:Parameter>
          <ows:Constraint name="PostEncoding">
            <ows:Value>XML</ows:Value>
            <ows:Value>SOAP</ows:Value>
          </ows:Constraint>
        </ows:Operation>
        <ows:Operation name="DescribeRecord">
          <ows:DCP>
            <ows:HTTP>
              <ows:Get xlink:href="{$endPointValue}"/>
              <ows:Post xlink:href="{$endPointValue}"/>
            </ows:HTTP>
          </ows:DCP>
          <ows:Parameter name="outputFormat">
            <ows:Value>application/xml</ows:Value>
          </ows:Parameter>
          <ows:Parameter name="schemaLanguage">
            <ows:Value>http://www.w3.org/TR/xmlschema-1/</ows:Value>
          </ows:Parameter>
          <ows:Parameter name="outputSchema"/>
          <ows:Constraint name="PostEncoding">
            <ows:Value>XML</ows:Value>
            <ows:Value>SOAP</ows:Value>
          </ows:Constraint>
        </ows:Operation>
        <ows:Operation name="GetDomain">
          <ows:DCP>
            <ows:HTTP>
              <ows:Get xlink:href="{$endPointValue}"/>
              <ows:Post xlink:href="{$endPointValue}"/>
            </ows:HTTP>
          </ows:DCP>
        </ows:Operation>
        <ows:Operation name="GetRecords">
          <ows:DCP>
            <ows:HTTP>
              <ows:Get xlink:href="{$endPointValue}"/>
              <ows:Post xlink:href="{$endPointValue}"/>
            </ows:HTTP>
          </ows:DCP>
          <ows:Parameter name="resultType">
            <ows:Value>hits</ows:Value>
            <ows:Value>results</ows:Value>
            <ows:Value>validate</ows:Value>
          </ows:Parameter>
          <ows:Parameter name="outputFormat">
            <ows:Value>application/xml</ows:Value>
          </ows:Parameter>
          <ows:Parameter name="outputSchema"/>
          <ows:Parameter name="typeNames"/>
          <ows:Parameter name="CONSTRAINTLANGUAGE">
            <ows:Value>FILTER</ows:Value>
            <ows:Value>CQL_TEXT</ows:Value>
          </ows:Parameter>
          <ows:Constraint name="PostEncoding">
            <ows:Value>XML</ows:Value>
            <ows:Value>SOAP</ows:Value>
          </ows:Constraint>
        </ows:Operation>
        <ows:Operation name="GetRecordById">
          <ows:DCP>
            <ows:HTTP>
              <ows:Get xlink:href="{$endPointValue}"/>
              <ows:Post xlink:href="{$endPointValue}"/>
            </ows:HTTP>
          </ows:DCP>
          <ows:Parameter name="outputSchema"/>
          <ows:Parameter name="outputFormat">
            <ows:Value>application/xml</ows:Value>
          </ows:Parameter>
          <ows:Parameter name="resultType">
            <ows:Value>hits</ows:Value>
            <ows:Value>results</ows:Value>
            <ows:Value>validate</ows:Value>
          </ows:Parameter>
          <ows:Parameter name="ElementSetName">
            <ows:Value>brief</ows:Value>
            <ows:Value>summary</ows:Value>
            <ows:Value>full</ows:Value>
          </ows:Parameter>
          <ows:Constraint name="PostEncoding">
            <ows:Value>XML</ows:Value>
            <ows:Value>SOAP</ows:Value>
          </ows:Constraint>
        </ows:Operation>
        <ows:Operation name="Transaction">
          <ows:DCP>
            <ows:HTTP>
              <ows:Get xlink:href="{$endPointValuePublication}"/>
              <ows:Post xlink:href="{$endPointValuePublication}"/>
            </ows:HTTP>
          </ows:DCP>
        </ows:Operation>
        <ows:Operation name="Harvest">
          <ows:DCP>
            <ows:HTTP>
              <ows:Get xlink:href="{$endPointValuePublication}"/>
              <ows:Post xlink:href="{$endPointValuePublication}"/>
            </ows:HTTP>
          </ows:DCP>
          <ows:Parameter name="ResourceType">
            <ows:Value>http://www.isotc211.org/schemas/2005/gmd/</ows:Value>
          </ows:Parameter>
        </ows:Operation>
        <ows:Parameter name="service">
          <ows:Value>http://www.opengis.net/cat/csw/2.0.2</ows:Value>
        </ows:Parameter>
        <ows:Parameter name="version">
          <ows:Value>2.0.2</ows:Value>
        </ows:Parameter>
        <ows:Constraint name="IsoProfiles">
          <ows:Value>http://www.isotc211.org/2005/gmd</ows:Value>
        </ows:Constraint>
        <ows:Constraint name="PostEncoding">
          <ows:Value>SOAP</ows:Value>
        </ows:Constraint>

        <xsl:if test="$isInspire">

          <inspire_ds:ExtendedCapabilities>
            <inspire_com:ResourceLocator>
              <inspire_com:URL><xsl:value-of select="$endPointValue"/>?SERVICE=CSW&amp;VERSION=2.0.2&amp;REQUEST=GetCapabilities</inspire_com:URL>
              <inspire_com:MediaType>application/xml</inspire_com:MediaType>
            </inspire_com:ResourceLocator>

            <inspire_com:ResourceLocator>
              <inspire_com:URL>$PROTOCOL://$HOST$PORT$SERVLET</inspire_com:URL>
              <inspire_com:MediaType>text/html</inspire_com:MediaType>
            </inspire_com:ResourceLocator>

            <inspire_com:ResourceType>service</inspire_com:ResourceType>

            <xsl:for-each select="//gex:temporalElement/*|//gmd:temporalElement/*">
              <inspire_com:TemporalReference>
                <inspire_com:TemporalExtent>
                  <inspire_com:IntervalOfDates>
                    <inspire_com:StartingDate><xsl:value-of select="(gex:extent|gmd:extent)/*/*:beginPosition/text()"/></inspire_com:StartingDate>
                    <inspire_com:EndDate><xsl:value-of select="(gex:extent|gmd:extent)/*/*:endPosition/text()"/></inspire_com:EndDate>
                  </inspire_com:IntervalOfDates>
                </inspire_com:TemporalExtent>
              </inspire_com:TemporalReference>
            </xsl:for-each>

            <xsl:variable name="iso2lang" select="util:twoCharLangCode($mainLanguage)" />

            <xsl:for-each select="//mdb:dataQualityInfo/*/mdq:report/*/mdq:result/*[contains(
                                    string-join(mdq:specification/*/cit:title/*/text(), ''), '1089/2010')]|
                                    //gmd:dataQualityInfo/*/gmd:report/*/gmd:result/*[contains(
                                    string-join(gmd:specification/*/gmd:title/*/text(), ''), '1089/2010')]">
              <inspire_com:Conformity>
                <inspire_com:Specification
                  xsi:type="inspire_com:citationInspireInteroperabilityRegulation_{$mainLanguage}">
                  <inspire_com:Title>
                    <xsl:apply-templates mode="localised"
                                         select="mdq:specification/*/cit:title|gmd:specification/*/gmd:title"/>
                  </inspire_com:Title>
                  <inspire_com:DateOfPublication>2010-12-08</inspire_com:DateOfPublication>
                  <inspire_com:URI>OJ:L:2010:323:0011:0102:<xsl:value-of select="upper-case($iso2lang)"/>:PDF</inspire_com:URI>
                  <inspire_com:ResourceLocator>
                    <inspire_com:URL>
                      http://eur-lex.europa.eu/LexUriServ/LexUriServ.do?uri=OJ:L:2010:323:0011:0102:<xsl:value-of select="upper-case($iso2lang)"/>:PDF
                    </inspire_com:URL>
                    <inspire_com:MediaType>application/pdf</inspire_com:MediaType>
                  </inspire_com:ResourceLocator>
                </inspire_com:Specification>

                <inspire_com:Degree>
                  <xsl:choose>
                    <xsl:when test="mdq:pass/gco:Boolean = 'true' or gmd:pass/gco139:Boolean = 'true'">conformant</xsl:when>
                    <xsl:when test="mdq:pass/gco:Boolean = 'false' or gmd:pass/gco139:Boolean = 'false'">notConformant</xsl:when>
                    <xsl:otherwise>notEvaluated</xsl:otherwise>
                  </xsl:choose>
                </inspire_com:Degree>
              </inspire_com:Conformity>
            </xsl:for-each>

            <xsl:for-each select="//mdb:dataQualityInfo/*/mdq:report/*/mdq:result/*[contains(
                                    string-join(mdq:specification/*/cit:title/*/text(), ''), '976/2009')]|
                                    //gmd:dataQualityInfo/*/gmd:report/*/gmd:result/*[contains(
                                    string-join(gmd:specification/*/gmd:title/*/text(), ''), '976/2009')]">
              <inspire_com:Conformity>
                <inspire_com:Specification
                  xsi:type="inspire_com:citationInspireInteroperabilityRegulation_{$mainLanguage}">
                  <inspire_com:Title>
                    <xsl:apply-templates mode="localised"
                                         select="mdq:specification/*/cit:title|gmd:specification/*/gmd:title"/>
                  </inspire_com:Title>
                  <inspire_com:DateOfPublication>2009-10-20</inspire_com:DateOfPublication>
                  <inspire_com:URI>CONSLEG:2009R0976:20101228:<xsl:value-of select="upper-case($iso2lang)"/>:PDF</inspire_com:URI>
                  <inspire_com:ResourceLocator>
                    <inspire_com:URL>
                      https://eur-lex.europa.eu/LexUriServ/LexUriServ.do?uri=CONSLEG:2009R0976:20101228:<xsl:value-of select="upper-case($iso2lang)"/>:PDF
                    </inspire_com:URL>
                    <inspire_com:MediaType>application/pdf</inspire_com:MediaType>
                  </inspire_com:ResourceLocator>
                </inspire_com:Specification>

                <inspire_com:Degree>
                  <xsl:choose>
                    <xsl:when test="mdq:pass/gco:Boolean = 'true' or gmd:pass/gco139:Boolean = 'true'">conformant</xsl:when>
                    <xsl:when test="mdq:pass/gco:Boolean = 'false' or gmd:pass/gco139:Boolean = 'false'">notConformant</xsl:when>
                    <xsl:otherwise>notEvaluated</xsl:otherwise>
                  </xsl:choose>
                </inspire_com:Degree>
              </inspire_com:Conformity>
            </xsl:for-each>


            <xsl:for-each select="//mdb:contact[1]/*|//gmd:contact[1]/*">
              <inspire_com:MetadataPointOfContact>
                <inspire_com:OrganisationName>
                  <xsl:apply-templates mode="localised"
                                       select="cit:party/cit:CI_Organisation/cit:name|gmd:organisationName"/>
                </inspire_com:OrganisationName>
                <inspire_com:EmailAddress>
                  <xsl:apply-templates mode="localised"
                                       select="cit:party/cit:CI_Organisation/cit:contactInfo/*/cit:address/*/cit:electronicMailAddress|
                                               gmd:contactInfo/*/gmd:address/*/gmd:electronicMailAddress"/>
                </inspire_com:EmailAddress>
              </inspire_com:MetadataPointOfContact>
            </xsl:for-each>

            <inspire_com:MetadataDate>
              <xsl:value-of
                select="tokenize(//mdb:dateInfo/*[cit:dateType/*/@codeListValue = 'revision']/cit:date/*/text()|//gmd:dateStamp/*/text(), 'T')[1]"/>
            </inspire_com:MetadataDate>
            <inspire_com:SpatialDataServiceType>discovery</inspire_com:SpatialDataServiceType>

            <inspire_com:MandatoryKeyword xsi:type="inspire_com:classificationOfSpatialDataService">
              <inspire_com:KeywordValue>infoCatalogueService</inspire_com:KeywordValue>
            </inspire_com:MandatoryKeyword>


            <xsl:for-each select="$inspireThemes">
              <inspire_com:Keyword xsi:type="inspire_com:inspireTheme_{$mainLanguage}">
                <inspire_com:OriginatingControlledVocabulary>
                  <inspire_com:Title>GEMET - INSPIRE themes</inspire_com:Title>
                  <inspire_com:DateOfPublication>2008-06-01</inspire_com:DateOfPublication>
                </inspire_com:OriginatingControlledVocabulary>

                <inspire_com:KeywordValue>
                  <xsl:apply-templates mode="localised"
                                       select="."/>
                </inspire_com:KeywordValue>
              </inspire_com:Keyword>
            </xsl:for-each>

            <inspire_com:SupportedLanguages>
              <inspire_com:DefaultLanguage>
                <inspire_com:Language>
                  <xsl:value-of select="$mainLanguage"/>
                </inspire_com:Language>
              </inspire_com:DefaultLanguage>
              <inspire_com:SupportedLanguage>
                <inspire_com:Language>
                  <xsl:value-of select="$mainLanguage"/>
                </inspire_com:Language>
              </inspire_com:SupportedLanguage>
              <xsl:for-each select="//mdb:otherLocale/*|//gmd:locale/*">
                 <inspire_com:SupportedLanguage>
                  <inspire_com:Language>
                    <xsl:value-of select="(lan:language|gmd:languageCode)/*/@codeListValue"/>
                  </inspire_com:Language>
                </inspire_com:SupportedLanguage>
              </xsl:for-each>
            </inspire_com:SupportedLanguages>
            <inspire_com:ResponseLanguage>
              <inspire_com:Language>
                <xsl:value-of select="if ($language != '') then $outputLanguage else $mainLanguage"/>
              </inspire_com:Language>
            </inspire_com:ResponseLanguage>
          </inspire_ds:ExtendedCapabilities>
        </xsl:if>
      </ows:OperationsMetadata>

      <ogc:Filter_Capabilities>
        <ogc:Spatial_Capabilities>
          <ogc:GeometryOperands>
            <ogc:GeometryOperand>gml:Envelope</ogc:GeometryOperand>
            <ogc:GeometryOperand>gml:Point</ogc:GeometryOperand>
            <ogc:GeometryOperand>gml:LineString</ogc:GeometryOperand>
            <ogc:GeometryOperand>gml:Polygon</ogc:GeometryOperand>
          </ogc:GeometryOperands>
          <ogc:SpatialOperators>
            <ogc:SpatialOperator name="BBOX"/>
            <ogc:SpatialOperator name="Equals"/>
            <ogc:SpatialOperator name="Overlaps"/>
            <ogc:SpatialOperator name="Disjoint"/>
            <ogc:SpatialOperator name="Intersects"/>
            <ogc:SpatialOperator name="Touches"/>
            <ogc:SpatialOperator name="Crosses"/>
            <ogc:SpatialOperator name="Within"/>
            <ogc:SpatialOperator name="Contains"/>
            <!--
            <ogc:SpatialOperator name="Beyond"/>
            <ogc:SpatialOperator name="DWithin"/>
             The 'SpatialOperator' element can have a GeometryOperands child -->
          </ogc:SpatialOperators>
        </ogc:Spatial_Capabilities>
        <ogc:Scalar_Capabilities>
          <ogc:LogicalOperators/>
          <ogc:ComparisonOperators>
            <ogc:ComparisonOperator>EqualTo</ogc:ComparisonOperator>
            <ogc:ComparisonOperator>Like</ogc:ComparisonOperator>
            <ogc:ComparisonOperator>LessThan</ogc:ComparisonOperator>
            <ogc:ComparisonOperator>GreaterThan</ogc:ComparisonOperator>
            <!-- LessThanOrEqualTo is in OGC Filter Spec, LessThanEqualTo is in OGC CSW schema -->
            <ogc:ComparisonOperator>LessThanEqualTo</ogc:ComparisonOperator>
            <!--<ogc:ComparisonOperator>LessThanOrEqualTo</ogc:ComparisonOperator>-->
            <!-- GreaterThanOrEqualTo is in OGC Filter Spec, GreaterThanEqualTo is in OGC CSW schema -->
            <ogc:ComparisonOperator>GreaterThanEqualTo</ogc:ComparisonOperator>
            <!--<ogc:ComparisonOperator>GreaterThanOrEqualTo</ogc:ComparisonOperator>-->
            <ogc:ComparisonOperator>NotEqualTo</ogc:ComparisonOperator>
            <ogc:ComparisonOperator>Between</ogc:ComparisonOperator>
            <ogc:ComparisonOperator>NullCheck</ogc:ComparisonOperator>
            <!-- FIXME : Check NullCheck operation is available -->
          </ogc:ComparisonOperators>
        </ogc:Scalar_Capabilities>
        <ogc:Id_Capabilities>
          <ogc:EID/>
          <ogc:FID/>
        </ogc:Id_Capabilities>
      </ogc:Filter_Capabilities>
    </csw:Capabilities>

  </xsl:template>
</xsl:stylesheet>
