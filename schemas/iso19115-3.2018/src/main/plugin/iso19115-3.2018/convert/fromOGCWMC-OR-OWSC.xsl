<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:wmc="http://www.opengis.net/context"
                xmlns:wmc11="http://www.opengeospatial.net/context"
                xmlns:ows-context="http://www.opengis.net/ows-context"
                xmlns:ows="http://www.opengis.net/ows"
                xmlns:mri="http://standards.iso.org/iso/19115/-3/mri/1.0"
                xmlns:mrl="http://standards.iso.org/iso/19115/-3/mrl/2.0"
                xmlns:mrs="http://standards.iso.org/iso/19115/-3/mrs/1.0"
                xmlns:mds="http://standards.iso.org/iso/19115/-3/mds/2.0"
                xmlns:gco="http://standards.iso.org/iso/19115/-3/gco/1.0"
                xmlns:mrd="http://standards.iso.org/iso/19115/-3/mrd/1.0"
                xmlns:mdt="http://standards.iso.org/iso/19115/-3/mdt/2.0"
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                xmlns:lan="http://standards.iso.org/iso/19115/-3/lan/1.0"
                xmlns:gex="http://standards.iso.org/iso/19115/-3/gex/1.0"
                xmlns:mdb="http://standards.iso.org/iso/19115/-3/mdb/2.0"
                xmlns:cit="http://standards.iso.org/iso/19115/-3/cit/2.0"
                xmlns:mcc="http://standards.iso.org/iso/19115/-3/mcc/1.0"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                xmlns:java="java:org.fao.geonet.util.XslUtil"
                xmlns:saxon="http://saxon.sf.net/"
                version="2.0"
                exclude-result-prefixes="#all">

    <xsl:param name="uuid"></xsl:param>
    <xsl:param name="lang">eng</xsl:param>
    <xsl:param name="topic"></xsl:param>
    <xsl:param name="viewer_url"></xsl:param>
    <xsl:param name="title"></xsl:param>
    <xsl:param name="abstract"></xsl:param>
    <xsl:param name="map_url"></xsl:param>

    <!-- These are provided by the ImportWmc.java jeeves service -->
    <xsl:param name="currentuser_name"></xsl:param>
    <xsl:param name="currentuser_phone"></xsl:param>
    <xsl:param name="currentuser_mail"></xsl:param>
    <xsl:param name="currentuser_org"></xsl:param>


    <xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes"/>

    <xsl:variable name="df">[Y0001]-[M01]-[D01]T[H01]:[m01]:[s01]</xsl:variable>

    <xsl:variable name="isOws" select="count(//ows-context:OWSContext) > 0"/>

    <xsl:template match="/">
        <xsl:apply-templates/>
    </xsl:template>


    <xsl:template match="wmc:ViewContext|wmc11:ViewContext|ows-context:OWSContext">
        <mdb:MD_Metadata
                xmlns:mri="http://standards.iso.org/iso/19115/-3/mri/1.0"
                xmlns:mda="http://standards.iso.org/iso/19115/-3/mda/1.0"
                xmlns:mrl="http://standards.iso.org/iso/19115/-3/mrl/2.0"
                xmlns:mrs="http://standards.iso.org/iso/19115/-3/mrs/1.0"
                xmlns:mds="http://standards.iso.org/iso/19115/-3/mds/2.0"
                xmlns:mrc="http://standards.iso.org/iso/19115/-3/mrc/2.0"
                xmlns:gco="http://standards.iso.org/iso/19115/-3/gco/1.0"
                xmlns:mrd="http://standards.iso.org/iso/19115/-3/mrd/1.0"
                xmlns:mco="http://standards.iso.org/iso/19115/-3/mco/1.0"
                xmlns:mdt="http://standards.iso.org/iso/19115/-3/mdt/2.0"
                xmlns:msr="http://standards.iso.org/iso/19115/-3/msr/2.0"
                xmlns:mpc="http://standards.iso.org/iso/19115/-3/mpc/1.0"
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                xmlns:lan="http://standards.iso.org/iso/19115/-3/lan/1.0"
                xmlns:mac="http://standards.iso.org/iso/19115/-3/mac/2.0"
                xmlns:gcx="http://standards.iso.org/iso/19115/-3/gcx/1.0"
                xmlns:gfc="http://standards.iso.org/iso/19110/gfc/1.1"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                xmlns:gex="http://standards.iso.org/iso/19115/-3/gex/1.0"
                xmlns:srv="http://standards.iso.org/iso/19115/-3/srv/2.0"
                xmlns:dqm="http://standards.iso.org/iso/19157/-2/dqm/1.0"
                xmlns:gml="http://www.opengis.net/gml/3.2"
                xmlns:cat="http://standards.iso.org/iso/19115/-3/cat/1.0"
                xmlns:mdb="http://standards.iso.org/iso/19115/-3/mdb/2.0"
                xmlns:mex="http://standards.iso.org/iso/19115/-3/mex/1.0"
                xmlns:cit="http://standards.iso.org/iso/19115/-3/cit/2.0"
                xmlns:mas="http://standards.iso.org/iso/19115/-3/mas/1.0"
                xmlns:mcc="http://standards.iso.org/iso/19115/-3/mcc/1.0"
                xmlns:mmi="http://standards.iso.org/iso/19115/-3/mmi/1.0"
                xmlns:mdq="http://standards.iso.org/iso/19157/-2/mdq/1.0"
        >
            <mdb:metadataIdentifier>
                <mcc:MD_Identifier>
                    <mcc:code>
                        <gco:CharacterString>
                            <xsl:value-of select="$uuid"/>
                        </gco:CharacterString>
                    </mcc:code>
                </mcc:MD_Identifier>
            </mdb:metadataIdentifier>

            <mdb:defaultLocale>
                <lan:PT_Locale id="{upper-case(java:twoCharLangCode($lang))}">
                    <lan:language>
                        <lan:LanguageCode codeList="http://www.loc.gov/standards/iso639-2/" codeListValue="{$lang}"/>
                    </lan:language>
                    <lan:characterEncoding>
                        <lan:MD_CharacterSetCode
                                codeList="http://standards.iso.org/iso/19115/resources/Codelists/cat/codelists.xml#MD_CharacterSetCode"
                                codeListValue="utf8"/>
                    </lan:characterEncoding>
                </lan:PT_Locale>
            </mdb:defaultLocale>

            <mdb:metadataScope>
                <mdb:MD_MetadataScope>
                    <mdb:resourceScope>
                        <mcc:MD_ScopeCode
                                codeList="http://standards.iso.org/iso/19115/resources/Codelists/cat/codelists.xml#MD_ScopeCode"
                                codeListValue="document"/>
                    </mdb:resourceScope>
                    <mdb:name xsi:type="lan:PT_FreeText_PropertyType">
                        <gco:CharacterString>map</gco:CharacterString>
                    </mdb:name>
                </mdb:MD_MetadataScope>
            </mdb:metadataScope>

            <xsl:for-each select="wmc:General/wmc:ContactInformation|
                            wmc11:General/wmc11:ContactInformation|
                            ows-context:General/ows:ServiceProvider">
                <mdb:contact>
                    <cit:CI_Responsibility>
                        <xsl:apply-templates select="." mode="RespParty"/>
                    </cit:CI_Responsibility>
                </mdb:contact>
            </xsl:for-each>

            <!--  Assign a specific user with the info provided by the webservice -->
            <xsl:if test="$currentuser_name != ''">
                <mdb:contact>
                    <xsl:call-template name="build-current-user"/>
                </mdb:contact>
            </xsl:if>

            <mdb:dateInfo>
                <cit:CI_Date>
                    <cit:date>
                        <gco:DateTime>
                            <xsl:value-of select="format-dateTime(current-dateTime(), $df)"/>
                        </gco:DateTime>
                    </cit:date>
                    <cit:dateType>
                        <cit:CI_DateTypeCode
                                codeList="http://standards.iso.org/iso/19115/resources/Codelists/cat/codelists.xml#CI_DateTypeCode"
                                codeListValue="creation"/>
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

            <mdb:referenceSystemInfo>
                <mrs:MD_ReferenceSystem>
                    <mrs:referenceSystemIdentifier>
                        <mcc:MD_Identifier>
                            <mcc:code>
                                <gco:CharacterString>
                                    <xsl:value-of select="
                    wmc:General/wmc:BoundingBox/@SRS|
                    wmc11:General/wmc11:BoundingBox/@SRS|
                    ows-context:General/ows:BoundingBox/@crs"/>
                                </gco:CharacterString>
                            </mcc:code>
                        </mcc:MD_Identifier>
                    </mrs:referenceSystemIdentifier>
                </mrs:MD_ReferenceSystem>
            </mdb:referenceSystemInfo>


            <mdb:identificationInfo>
                <mri:MD_DataIdentification>
                    <xsl:apply-templates select="." mode="DataIdentification">
                        <xsl:with-param name="topic">
                            <xsl:value-of select="$topic"/>
                        </xsl:with-param>
                        <xsl:with-param name="lang">
                            <xsl:value-of select="$lang"/>
                        </xsl:with-param>
                    </xsl:apply-templates>
                </mri:MD_DataIdentification>
            </mdb:identificationInfo>

            <mdb:distributionInfo>
                <mrd:MD_Distribution>
                    <mrd:distributionFormat>
                        <mrd:MD_Format>
                            <mrd:formatSpecificationCitation>
                                <cit:CI_Citation>
                                    <cit:title xsi:type="lan:PT_FreeText_PropertyType">
                                        <gco:CharacterString>OGC:OWS-C</gco:CharacterString>
                                    </cit:title>
                                </cit:CI_Citation>
                            </mrd:formatSpecificationCitation>
                        </mrd:MD_Format>
                    </mrd:distributionFormat>
                    <mrd:transferOptions>
                        <mrd:MD_DigitalTransferOptions>

                            <!-- Add link to the map -->
                            <xsl:if test="$map_url != ''">
                                <mrd:onLine>
                                    <cit:CI_OnlineResource>
                                        <cit:linkage>
                                            <gco:CharacterString>
                                                <xsl:value-of select="$map_url"/>
                                            </gco:CharacterString>
                                        </cit:linkage>
                                        <cit:protocol>
                                            <gco:CharacterString>
                                                <xsl:value-of select="if ($isOws) then 'OGC:OWS-C' else 'OGC:WMC'"/>
                                            </gco:CharacterString>
                                        </cit:protocol>
                                        <cit:name>
                                            <gco:CharacterString>
                                                <xsl:value-of
                                                        select="wmc:General/wmc:Title|wmc11:General/wmc11:Title|ows-context:General/ows:Title"/>
                                            </gco:CharacterString>
                                        </cit:name>
                                        <cit:function>
                                            <cit:CI_OnLineFunctionCode
                                                    codeList="http://standards.iso.org/iso/19115/resources/Codelists/cat/codelists.xml#CI_OnLineFunctionCode"
                                                    codeListValue="browsing"/>
                                        </cit:function>
                                    </cit:CI_OnlineResource>
                                </mrd:onLine>
                            </xsl:if>

                            <!-- -->
                            <xsl:if test="$viewer_url != ''">
                                <mrd:onLine>
                                    <mrd:CI_OnlineResource>
                                        <cit:linkage>
                                            <gco:CharacterString>
                                                <xsl:value-of select="$viewer_url"/>
                                            </gco:CharacterString>
                                        </cit:linkage>
                                        <cit:protocol>
                                            <gco:CharacterString>WWW:LINK</gco:CharacterString>
                                        </cit:protocol>
                                        <cit:name>
                                            <gco:CharacterString>
                                                <xsl:value-of select="wmc:General/wmc:Title|
                          wmc11:General/wmc11:Title|
                          ows-context:General/ows:Title"/>
                                            </gco:CharacterString>
                                        </cit:name>
                                        <cit:function>
                                            <cit:CI_OnLineFunctionCode
                                                    codeList="http://standards.iso.org/iso/19115/resources/Codelists/cat/codelists.xml#CI_OnLineFunctionCode"
                                                    codeListValue="browsing"/>
                                        </cit:function>
                                    </mrd:CI_OnlineResource>
                                </mrd:onLine>
                            </xsl:if>

                            <xsl:for-each
                                    select="wmc:LayerList/wmc:Layer|ows-context:ResourceList/ows-context:Layer">
                                <mrd:onLine>
                                    <!-- iterates over the layers -->
                                    <!-- Only first URL is used -->
                                    <xsl:variable name="layerUrl"
                                                  select="wmc:Server/wmc:OnlineResource/@xlink:href|
                                        ows-context:Server[1]/ows-context:OnlineResource[1]/@xlink:href"/>
                                    <!--  service="urn:ogc:serviceType:WMS">-->
                                    <xsl:variable name="layerName" select="wmc:Name/text()|@name"/>
                                    <xsl:variable name="layerTitle" select="wmc:Title/text()|ows:Title/text()"/>
                                    <xsl:variable name="layerVersion" select="wmc:Server/@version"/>
                                    <xsl:variable name="layerProtocol"
                                                  select="if (ows:Server/@service) then ows:Server/@service else 'OGC:WMS'"/>
                                    <cit:CI_OnlineResource>
                                        <cit:linkage>
                                            <gco:CharacterString>
                                                <xsl:value-of select="$layerUrl"/>
                                            </gco:CharacterString>
                                        </cit:linkage>
                                        <cit:protocol>
                                            <gco:CharacterString>
                                                <xsl:value-of select="$layerProtocol"/>
                                            </gco:CharacterString>
                                        </cit:protocol>
                                        <cit:name>
                                            <gco:CharacterString>
                                                <xsl:value-of select="$layerName"/>
                                            </gco:CharacterString>
                                        </cit:name>
                                        <cit:description>
                                            <gco:CharacterString>
                                                <xsl:value-of select="$layerTitle"/>
                                            </gco:CharacterString>
                                        </cit:description>
                                    </cit:CI_OnlineResource>
                                </mrd:onLine>
                            </xsl:for-each>
                        </mrd:MD_DigitalTransferOptions>
                    </mrd:transferOptions>
                </mrd:MD_Distribution>
            </mdb:distributionInfo>

            <mdb:resourceLineage>
                <mrl:LI_Lineage>
                    <mrl:statement gco:nilReason="missing">
                        <gco:CharacterString/>
                    </mrl:statement>
                    <mrl:scope>
                        <mcc:MD_Scope>
                            <mcc:level>
                                <mcc:MD_ScopeCode codeList="http://standards.iso.org/iso/19115/resources/Codelists/cat/codelists.xml#MD_ScopeCode"
                                                  codeListValue="dataset"/>
                            </mcc:level>
                        </mcc:MD_Scope>
                    </mrl:scope>
                    <xsl:for-each
                            select="wmc:LayerList/wmc:Layer|ows-context:ResourceList/ows-context:Layer">
                        <!-- Would be good to add link to metadata using uuidref="" -->
                        <mrl:source
                                xlink:href="{wmc:MetadataURL/wmc:OnlineResource/@xlink:href|
                                 ows-context:MetadataURL/ows-context:OnlineResource/@xlink:href}"/>
                    </xsl:for-each>
                </mrl:LI_Lineage>
            </mdb:resourceLineage>
        </mdb:MD_Metadata>
    </xsl:template>


    <xsl:template match="wmc:BoundingBox|ows:BoundingBox"
                  mode="BoundingBox">
        <xsl:variable name="minx"
                      select="if (ows:LowerCorner) then tokenize(ows:LowerCorner, ' ')[1] else string(./@minx)"/>
        <xsl:variable name="miny"
                      select="if (ows:LowerCorner) then tokenize(ows:LowerCorner, ' ')[2] else string(./@miny)"/>
        <xsl:variable name="maxx"
                      select="if (ows:UpperCorner) then tokenize(ows:UpperCorner, ' ')[1] else string(./@maxx)"/>
        <xsl:variable name="maxy"
                      select="if (ows:UpperCorner) then tokenize(ows:UpperCorner, ' ')[2] else string(./@maxy)"/>
        <xsl:variable name="fromEpsg" select="if (@crs) then string(@crs) else string(./@SRS)"/>
        <xsl:variable name="reprojected"
                      select="java:reprojectCoords($minx,$miny,$maxx,$maxy,$fromEpsg)"/>
        <xsl:variable name="bbox" select="saxon:parse($reprojected)"/>
        <xsl:if test="$bbox">
            <gex:westBoundLongitude>
                <gco:Decimal>
                    <xsl:value-of select="$bbox//*:westBoundLongitude/*:Decimal/text()"/>
                </gco:Decimal>
            </gex:westBoundLongitude>
            <gex:eastBoundLongitude>
                <gco:Decimal>
                    <xsl:value-of select="$bbox//*:eastBoundLongitude/*:Decimal/text() "/>
                </gco:Decimal>
            </gex:eastBoundLongitude>
            <gex:southBoundLatitude>
                <gco:Decimal>
                    <xsl:value-of select="$bbox//*:southBoundLatitude/*:Decimal/text()"/>
                </gco:Decimal>
            </gex:southBoundLatitude>
            <gex:northBoundLatitude>
                <gco:Decimal>
                    <xsl:value-of select="$bbox//*:northBoundLatitude/*:Decimal/text()"/>
                </gco:Decimal>
            </gex:northBoundLatitude>
        </xsl:if>
    </xsl:template>


    <xsl:template match="*" mode="DataIdentification">
        <mri:citation>
            <cit:CI_Citation>
                <cit:title>
                    <gco:CharacterString>
                        <xsl:value-of select="if ($title) then $title else wmc:General/wmc:Title|
                                                     wmc11:General/wmc11:Title|
                                                     ows-context:General/ows:Title"/>
                    </gco:CharacterString>
                </cit:title>
                <!-- date is mandatory -->
                <cit:date>
                    <cit:CI_Date>
                        <cit:date>
                            <gco:DateTime>
                                <xsl:value-of select="format-dateTime(current-dateTime(),$df)"/>
                            </gco:DateTime>
                        </cit:date>
                        <cit:dateType>
                            <cit:CI_DateTypeCode codeListValue="publication"
                                                 codeList="http://standards.iso.org/iso/19115/resources/Codelists/cat/codelists.xml#CI_DateTypeCode"/>
                        </cit:dateType>
                    </cit:CI_Date>
                </cit:date>

                <cit:presentationForm>
                    <cit:CI_PresentationFormCode codeListValue="mapDigital"
                                                 codeList="http://standards.iso.org/iso/19115/resources/Codelists/cat/codelists.xml#CI_PresentationFormCode"
                    />
                </cit:presentationForm>
            </cit:CI_Citation>
        </mri:citation>

        <mri:abstract>
            <gco:CharacterString>
                <xsl:value-of select="if ($abstract)
                                      then $abstract
                                      else wmc:General/wmc:Abstract|
                                           wmc11:General/wmc11:Abstract|
                                           ows-context:General/ows:Abstract"/>
            </gco:CharacterString>
        </mri:abstract>

        <mri:status>
            <mcc:MD_ProgressCode codeList="http://standards.iso.org/iso/19115/resources/Codelists/cat/codelists.xml#MD_ProgressCode"
                                 codeListValue="completed"/>
        </mri:status>

        <xsl:for-each select="wmc:General/wmc:ContactInformation|
                          wmc11:General/wmc11:ContactInformation|
                          ows-context:General/ows:ServiceProvider">
            <mri:pointOfContact>
                <cit:CI_Responsibility>
                    <xsl:apply-templates select="." mode="RespParty"/>
                </cit:CI_Responsibility>
            </mri:pointOfContact>
        </xsl:for-each>

        <xsl:if test="$currentuser_name != ''">
            <mri:pointOfContact>
                <xsl:call-template name="build-current-user"/>
            </mri:pointOfContact>
        </xsl:if>

        <mri:topicCategory>
            <mri:MD_TopicCategoryCode>
                <xsl:value-of select="$topic"/>
            </mri:MD_TopicCategoryCode>
        </mri:topicCategory>

        <!--  extracts the extent (if not 4326, need to reproject) -->
        <gex:extent>
            <gex:EX_Extent>
                <gex:geographicElement>
                    <gex:EX_GeographicBoundingBox>
                        <xsl:apply-templates select=".//*:BoundingBox"
                                             mode="BoundingBox"/>
                    </gex:EX_GeographicBoundingBox>
                </gex:geographicElement>
            </gex:EX_Extent>
        </gex:extent>

        <xsl:for-each select="wmc:General/wmc:KeywordList|
                          wmc11:General/wmc11:KeywordList|
                          ows-context:General/ows:Keywords">
            <mri:descriptiveKeywords>
                <mri:MD_Keywords>
                    <xsl:apply-templates select="." mode="Keywords"/>
                </mri:MD_Keywords>
            </mri:descriptiveKeywords>
        </xsl:for-each>

        <mri:defaultLocale>
            <lan:PT_Locale>
                <lan:language>
                    <lan:LanguageCode codeList="http://www.loc.gov/standards/iso639-2/" codeListValue="{$lang}"/>
                </lan:language>
                <lan:characterEncoding>
                    <lan:MD_CharacterSetCode codeList="http://standards.iso.org/iso/19115/resources/Codelists/cat/codelists.xml#MD_CharacterSetCode"
                                             codeListValue="utf8"/>
                </lan:characterEncoding>
            </lan:PT_Locale>
        </mri:defaultLocale>
    </xsl:template>


    <xsl:template match="*" mode="Keywords">
        <xsl:for-each select=".//*:Keyword">
            <mri:keyword>
                <gco:CharacterString>
                    <xsl:value-of select="."/>
                </gco:CharacterString>
            </mri:keyword>
        </xsl:for-each>
        <mri:type>
            <mri:MD_KeywordTypeCode codeList="./resources/codeList.xml#MD_KeywordTypeCode"
                                    codeListValue="theme"/>
        </mri:type>
    </xsl:template>


    <xsl:template match="*" mode="RespParty">
        <cit:role>
            <cit:CI_RoleCode codeList="http://standards.iso.org/iso/19115/resources/Codelists/cat/codelists.xml#CI_RoleCode"
                             codeListValue="{if (ows:ServiceContact/ows:Role) then ows:ServiceContact/ows:Role else 'pointOfContact'}"/>
        </cit:role>
        <cit:party>
            <cit:CI_Organisation>

                <xsl:for-each select="wmc:ContactPersonPrimary/wmc:ContactOrganization|
                          wmc11:ContactPersonPrimary/wmc11:ContactOrganization|
                          ../ows:ProviderName">">
                    <cit:name>
                        <gco:CharacterString>
                            <xsl:value-of select="."/>
                        </gco:CharacterString>
                    </cit:name>
                </xsl:for-each>

                <xsl:for-each select="wmc:ContactAddress|wmc11:ContactAddress|ows:ContactInfo/ows:Address">
                    <cit:contactInfo>
                        <cit:CI_Contact>
                            <cit:address>
                                <cit:CI_Address>

                                    <xsl:for-each select="*:Address">
                                        <cit:deliveryPoint>
                                            <gco:CharacterString>
                                                <xsl:value-of select="."/>
                                            </gco:CharacterString>
                                        </cit:deliveryPoint>
                                    </xsl:for-each>

                                    <xsl:for-each select="*:City">
                                        <cit:city>
                                            <gco:CharacterString>
                                                <xsl:value-of select="."/>
                                            </gco:CharacterString>
                                        </cit:city>
                                    </xsl:for-each>

                                    <xsl:for-each select="*:StateOrProvince">
                                        <cit:administrativeArea>
                                            <gco:CharacterString>
                                                <xsl:value-of select="."/>
                                            </gco:CharacterString>
                                        </cit:administrativeArea>
                                    </xsl:for-each>

                                    <xsl:for-each select="*:PostCode">
                                        <cit:postalCode>
                                            <gco:CharacterString>
                                                <xsl:value-of select="."/>
                                            </gco:CharacterString>
                                        </cit:postalCode>
                                    </xsl:for-each>

                                    <xsl:for-each select="*:Country">
                                        <cit:country>
                                            <gco:CharacterString>
                                                <xsl:value-of select="."/>
                                            </gco:CharacterString>
                                        </cit:country>
                                    </xsl:for-each>

                                    <xsl:for-each select="*:eMailAdd|ows:electronMailAddress">
                                        <cit:electronicMailAddress>
                                            <gco:CharacterString>
                                                <xsl:value-of select="."/>
                                            </gco:CharacterString>
                                        </cit:electronicMailAddress>
                                    </xsl:for-each>
                                </cit:CI_Address>
                            </cit:address>
                        </cit:CI_Contact>
                    </cit:contactInfo>
                </xsl:for-each>


                <xsl:for-each select="wmc:ContactPersonPrimary/wmc:ContactPerson|
                          wmc11:ContactPersonPrimary/wmc11:ContactPerson|
                          ows:ServiceContact/ows:IndividualName">
                    <cit:individual>
                        <cit:CI_Individual>
                            <cit:name>
                                <gco:CharacterString>
                                    <xsl:value-of select="."/>
                                </gco:CharacterString>
                            </cit:name>
                        </cit:CI_Individual>
                    </cit:individual>
                </xsl:for-each>
            </cit:CI_Organisation>
        </cit:party>
    </xsl:template>

    <xsl:template name="build-current-user">
        <cit:CI_Responsibility>
            <cit:role>
                <cit:CI_RoleCode
                        codeList="http://standards.iso.org/iso/19115/resources/Codelists/cat/codelists.xml#CI_RoleCode"
                        codeListValue="author"/>
            </cit:role>
            <cit:party>
                <cit:CI_Organisation>
                    <cit:name>
                        <gco:CharacterString>
                            <xsl:value-of select="$currentuser_org"/>
                        </gco:CharacterString>
                    </cit:name>
                    <cit:contactInfo>
                        <cit:CI_Contact>
                            <cit:address>
                                <cit:CI_Address>
                                    <cit:electronicMailAddress>
                                        <gco:CharacterString>
                                            <xsl:value-of select="$currentuser_mail"/>
                                        </gco:CharacterString>
                                    </cit:electronicMailAddress>
                                </cit:CI_Address>
                            </cit:address>
                        </cit:CI_Contact>
                    </cit:contactInfo>
                    <cit:individual>
                        <cit:CI_Individual>
                            <cit:name>
                                <gco:CharacterString>
                                    <xsl:value-of select="$currentuser_name"/>
                                </gco:CharacterString>
                            </cit:name>
                            <cit:positionName gco:nilReason="missing"
                                              xsi:type="lan:PT_FreeText_PropertyType">
                                <gco:CharacterString/>
                            </cit:positionName>
                        </cit:CI_Individual>
                    </cit:individual>
                </cit:CI_Organisation>
            </cit:party>
        </cit:CI_Responsibility>
    </xsl:template>
</xsl:stylesheet>
