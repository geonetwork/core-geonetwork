<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:gmd="http://www.isotc211.org/2005/gmd"
                xmlns:gcoold="http://www.isotc211.org/2005/gco"
                xmlns:gfcold="http://www.isotc211.org/2005/gfc"
                xmlns:gfc="http://standards.iso.org/iso/19110/gfc/1.1"
                xmlns:gmi="http://www.isotc211.org/2005/gmi"
                xmlns:gmx="http://www.isotc211.org/2005/gmx"
                xmlns:gsr="http://www.isotc211.org/2005/gsr"
                xmlns:gss="http://www.isotc211.org/2005/gss"
                xmlns:gts="http://www.isotc211.org/2005/gts"
                xmlns:srvold="http://www.isotc211.org/2005/srv"
                xmlns:gml30="http://www.opengis.net/gml"
                xmlns:cat="http://standards.iso.org/iso/19115/-3/cat/1.0"
                xmlns:cit="http://standards.iso.org/iso/19115/-3/cit/2.0"
                xmlns:gcx="http://standards.iso.org/iso/19115/-3/gcx/1.0"
                xmlns:gex="http://standards.iso.org/iso/19115/-3/gex/1.0"
                xmlns:lan="http://standards.iso.org/iso/19115/-3/lan/1.0"
                xmlns:srv="http://standards.iso.org/iso/19115/-3/srv/2.0"
                xmlns:mac="http://standards.iso.org/iso/19115/-3/mac/2.0"
                xmlns:mas="http://standards.iso.org/iso/19115/-3/mas/1.0"
                xmlns:mcc="http://standards.iso.org/iso/19115/-3/mcc/1.0"
                xmlns:mco="http://standards.iso.org/iso/19115/-3/mco/1.0"
                xmlns:mda="http://standards.iso.org/iso/19115/-3/mda/1.0"
                xmlns:mdb="http://standards.iso.org/iso/19115/-3/mdb/2.0"
                xmlns:mdt="http://standards.iso.org/iso/19115/-3/mdt/1.0"
                xmlns:mex="http://standards.iso.org/iso/19115/-3/mex/1.0"
                xmlns:mic="http://standards.iso.org/iso/19115/-3/mic/1.0"
                xmlns:mil="http://standards.iso.org/iso/19115/-3/mil/1.0"
                xmlns:mrl="http://standards.iso.org/iso/19115/-3/mrl/1.0"
                xmlns:mds="http://standards.iso.org/iso/19115/-3/mds/2.0"
                xmlns:mmi="http://standards.iso.org/iso/19115/-3/mmi/1.0"
                xmlns:mpc="http://standards.iso.org/iso/19115/-3/mpc/1.0"
                xmlns:mrc="http://standards.iso.org/iso/19115/-3/mrc/2.0"
                xmlns:mrd="http://standards.iso.org/iso/19115/-3/mrd/1.0"
                xmlns:mri="http://standards.iso.org/iso/19115/-3/mri/1.0"
                xmlns:mrs="http://standards.iso.org/iso/19115/-3/mrs/1.0"
                xmlns:msr="http://standards.iso.org/iso/19115/-3/msr/2.0"
                xmlns:mai="http://standards.iso.org/iso/19115/-3/mai/1.0"
                xmlns:mdq="http://standards.iso.org/iso/19157/-2/mdq/1.0"
                xmlns:gco="http://standards.iso.org/iso/19115/-3/gco/1.0"
                xmlns:gml="http://www.opengis.net/gml/3.2"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                xmlns:xd="http://www.oxygenxml.com/ns/doc/xsl"
                exclude-result-prefixes="#all">

    <xsl:import href="utility/create19115-3Namespaces.xsl"/>
    <xsl:import href="utility/dateTime.xsl"/>
    <xsl:import href="utility/multiLingualCharacterStrings.xsl"/>

    <xd:doc xmlns:xd="http://www.oxygenxml.com/ns/doc/xsl" scope="stylesheet">
        <xd:desc>
            <xd:p>
                <xd:b>Created on:</xd:b>March 8, 2014 </xd:p>
            <xd:p>Translates from ISO 19139 for ISO 19115 and ISO 19139-2 for 19115-2 to ISO 19139-1 for ISO 19115-1</xd:p>
            <xd:p>
                <xd:b>Version June 13, 2014</xd:b>
                <xd:ul>
                    <xd:li>Converged the 19115-2 transform into 19115-1 namespaces</xd:li>
                </xd:ul>
            </xd:p>
            <xd:p>
                <xd:b>Version August 7, 2014</xd:b>
                <xd:ul>
                    <xd:li>Changed namespace dates to 2014-07-11</xd:li>
                    <xd:li>Fixed DistributedComputingPlatform element</xd:li>
                </xd:ul>
            </xd:p>
            <xd:p>
                <xd:b>Version August 15, 2014</xd:b>
                <xd:ul>
                    <xd:li>Add multilingual metadata support by converting gmd:locale and copying gmd:PT_FreeText and element attributes (eg. gco:nilReason, xsi:type) for gmd:CharacterString elements (Author:
                        fx.prunayre@gmail.com).</xd:li>
                </xd:ul>
            </xd:p>
            <xd:p>
                <xd:b>Version September 4, 2014</xd:b>
                <xd:ul>
                    <xd:li>Added transform for MD_FeatureCatalogueDescription (problem identified by Tobias Spears</xd:li>
                </xd:ul>
            </xd:p>
            <xd:p>
                <xd:b>Version February 5, 2015</xd:b>
                <xd:ul>
                    <xd:li>Update to 2014-12-25 version</xd:li>
                </xd:ul>
            </xd:p>
            <xd:p><xd:b>Author:</xd:b>thabermann@hdfgroup.org</xd:p>
        </xd:desc>
    </xd:doc>

    <xsl:output method="xml" indent="yes"/>

    <xsl:strip-space elements="*"/>

    <xsl:variable name="stylesheetVersion" select="'0.1'"/>


    <xsl:template match="/" name="to-iso19115-3">
        <!--
        root element (MD_Metadata or MI_Metadata)
        -->
        <xsl:for-each select="//(gmd:MD_Metadata|gmi:MI_Metadata|gfcold:FC_FeatureCatalogue)">
            <xsl:variable name="nameSpacePrefix">
                <xsl:call-template name="getNamespacePrefix"/>
            </xsl:variable>

            <xsl:variable name="isFeatureCatalogue"
                          select="local-name() = 'FC_FeatureCatalogue'"
                          as="xs:boolean"/>

            <xsl:element name="mdb:MD_Metadata">
                <!-- new namespaces -->
                <xsl:call-template name="add-iso19115-3.2018-namespaces"/>

                <xsl:apply-templates select="gmd:fileIdentifier|@uuid" mode="from19139to19115-3.2018"/>

                <xsl:if test="$isFeatureCatalogue and gfcold:producer">
                  <xsl:variable name="metadataContact" as="node()?">
                    <xsl:apply-templates select="gfcold:producer"
                                         mode="from19139to19115-3.2018"/>
                  </xsl:variable>
                  <mdb:contact>
                    <xsl:copy-of select="$metadataContact/cit:CI_Responsibility"/>
                  </mdb:contact>
                </xsl:if>

                <xsl:apply-templates select="gmd:language" mode="from19139to19115-3.2018"/>
                <xsl:apply-templates select="gmd:characterSet" mode="from19139to19115-3.2018"/>
                <xsl:apply-templates select="gmd:parentIdentifier" mode="from19139to19115-3.2018"/>
                <xsl:apply-templates select="gmd:hierarchyLevel" mode="from19139to19115-3.2018"/>
                <xsl:apply-templates select="gmd:contact" mode="from19139to19115-3.2018"/>

                <xsl:if test="$isFeatureCatalogue and gfcold:functionalLanguage">
                  <mdb:defaultLocale>
                    <lan:PT_Locale id="EN">
                      <lan:language>
                        <lan:LanguageCode codeList="http://www.loc.gov/standards/iso639-2/" codeListValue="{gfcold:functionalLanguage/*/text()}"/>
                      </lan:language>
                      <lan:characterEncoding>
                        <lan:MD_CharacterSetCode codeList="http://standards.iso.org/iso/19115/resources/Codelists/cat/codelists.xml#MD_CharacterSetCode"
                                                 codeListValue="UTF-8"/>
                      </lan:characterEncoding>
                    </lan:PT_Locale>
                  </mdb:defaultLocale>
                </xsl:if>

                <xsl:apply-templates select="gmd:dateStamp" mode="from19139to19115-3.2018"/>
                <xsl:apply-templates select="gmd:metadataStandardName" mode="from19139to19115-3.2018"/>

                <xsl:if test="$isFeatureCatalogue">
                  <mdb:metadataStandard>
                    <cit:CI_Citation>
                      <cit:title>
                        <gco:CharacterString>ISO 19115-3 / ISO 19110</gco:CharacterString>
                      </cit:title>
                    </cit:CI_Citation>
                  </mdb:metadataStandard>
                </xsl:if>

                <xsl:apply-templates select="gmd:locale" mode="from19139to19115-3.2018"/>
                <xsl:apply-templates select="gmd:spatialRepresentationInfo" mode="from19139to19115-3.2018"/>
                <xsl:apply-templates select="gmd:referenceSystemInfo" mode="from19139to19115-3.2018"/>
                <xsl:apply-templates select="gmd:metadataExtensionInfo" mode="from19139to19115-3.2018"/>
                <xsl:apply-templates select="gmd:identificationInfo" mode="from19139to19115-3.2018"/>
                <xsl:apply-templates select="gmd:contentInfo" mode="from19139to19115-3.2018"/>

                <xsl:if test="$isFeatureCatalogue">
                  <mdb:contentInfo>
                    <mrc:MD_FeatureCatalogue>
                      <mrc:featureCatalogue>
                        <gfc:FC_FeatureCatalogue>
                          <xsl:apply-templates select="gfcold:name|gmx:name"
                                               mode="from19139to19115-3.2018"/>
                          <xsl:apply-templates select="gfcold:scope|gmx:scope"
                                               mode="from19139to19115-3.2018"/>
                          <xsl:apply-templates select="gfcold:fieldOfApplication|gmx:fieldOfApplication"
                                               mode="from19139to19115-3.2018"/>
                          <xsl:apply-templates select="gfcold:versionNumber|gmx:versionNumber"
                                               mode="from19139to19115-3.2018"/>
                          <xsl:apply-templates select="gfcold:versionDate|gmx:versionDate"
                                               mode="from19139to19115-3.2018"/>
                          <xsl:apply-templates select="gfcold:language|gmx:language"
                                               mode="from19139to19115-3.2018"/>
                          <xsl:apply-templates select="gfcold:characterSet|gmx:characterSet"
                                               mode="from19139to19115-3.2018"/>
                          <xsl:apply-templates select="gfcold:producer"
                                               mode="from19139to19115-3.2018"/>
                          <xsl:apply-templates select="gfcold:functionalLanguage"
                                               mode="from19139to19115-3.2018"/>
                          <xsl:apply-templates select="gfcold:featureType"
                                               mode="from19139to19115-3.2018"/>
                          <xsl:apply-templates select="gfcold:inheritanceRelation"
                                               mode="from19139to19115-3.2018"/>
                          <xsl:apply-templates select="gfcold:globalProperty"
                                               mode="from19139to19115-3.2018"/>
                          <xsl:apply-templates select="gfcold:definitionSource"
                                               mode="from19139to19115-3.2018"/>
                        </gfc:FC_FeatureCatalogue>
                      </mrc:featureCatalogue>
                    </mrc:MD_FeatureCatalogue>
                  </mdb:contentInfo>
                </xsl:if>

                <xsl:call-template name="onlineSourceDispatcher">
                    <xsl:with-param name="type" select="'featureCatalogueCitation'"/>
                </xsl:call-template>

                <xsl:apply-templates select="gmd:distributionInfo" mode="from19139to19115-3.2018"/>
                <xsl:apply-templates select="gmd:dataQualityInfo" mode="from19139to19115-3.2018"/>
                <xsl:apply-templates select="gmd:portrayalCatalogueInfo" mode="from19139to19115-3.2018"/>
                <xsl:call-template name="onlineSourceDispatcher">
                    <xsl:with-param name="type" select="'portrayalCatalogueCitation'"/>
                </xsl:call-template>

                <xsl:apply-templates select="gmd:metadataConstraints" mode="from19139to19115-3.2018"/>
                <xsl:apply-templates select="gmd:applicationSchemaInfo" mode="from19139to19115-3.2018"/>
                <xsl:apply-templates select="gmd:metadataMaintenance" mode="from19139to19115-3.2018"/>
                <xsl:apply-templates select="gmi:acquisitionInformation" mode="from19139to19115-3.2018"/>
            </xsl:element>
        </xsl:for-each>
    </xsl:template>


    <xsl:include href="mapping/core.xsl"/>
    <xsl:include href="mapping/CI_ResponsibleParty.xsl"/>
    <xsl:include href="mapping/CI_Citation.xsl"/>
    <xsl:include href="mapping/SRV.xsl"/>
    <xsl:include href="mapping/DQ.xsl"/>



    <!-- Depending on the function of online source in ISO19139,
    categorized them in more descriptive sections. -->
    <xsl:variable name="onlineFunctionMap">
        <entry key="portrayalCatalogueCitation" value="information.portrayal"/>
        <entry key="additionalDocumentation" ns="mrl" value="information.lineage" type="dq"/>
        <entry key="specification" value="information.qualitySpecification" type="dq"/>
        <entry key="reportReference" value="information.qualityReport" type="dq"/>
        <entry key="featureCatalogueCitation" value="information.content"/>
    </xsl:variable>

    <xsl:template match="gmd:onLine[*/gmd:function/*/@codeListValue = $onlineFunctionMap/entry/@value]"
                  mode="from19139to19115-3.2018"
                  priority="200"/>

    <xsl:template name="onlineSourceDispatcher">
        <xsl:param name="type" as="xs:string"/>

        <xsl:for-each select="(.|ancestor::gmd:MD_Metadata)/descendant::gmd:CI_OnlineResource[
                    gmd:function/*/@codeListValue = $onlineFunctionMap/entry[@key = $type]/@value
                    ]">
            <!-- Convert onlineSource to a citation in the corresponding element. -->
            <xsl:choose>
                <xsl:when test="$type = 'portrayalCatalogueCitation'">
                    <mdb:portrayalCatalogueInfo>
                        <mpc:MD_PortrayalCatalogueReference>
                            <mpc:portrayalCatalogueCitation>
                                <xsl:call-template name="buildCitation"/>
                            </mpc:portrayalCatalogueCitation>
                        </mpc:MD_PortrayalCatalogueReference>
                    </mdb:portrayalCatalogueInfo>
                </xsl:when>
                <xsl:when test="$type = 'featureCatalogueCitation'">
                    <mdb:contentInfo>
                        <mrc:MD_FeatureCatalogueDescription>
                            <mrc:featureCatalogueCitation>
                                <xsl:call-template name="buildCitation"/>
                            </mrc:featureCatalogueCitation>
                        </mrc:MD_FeatureCatalogueDescription>
                    </mdb:contentInfo>
                </xsl:when>
                <xsl:when test="$type = 'specification'">
                    <mdq:report>
                        <mdq:DQ_DomainConsistency>
                            <mdq:result>
                                <mdq:DQ_ConformanceResult>
                                    <mdq:specification>
                                        <xsl:call-template name="buildCitation">
                                            <xsl:with-param name="withDescription" select="false()"/>
                                        </xsl:call-template>
                                    </mdq:specification>

                                    <xsl:call-template name="writeCharacterStringElement">
                                        <xsl:with-param name="elementName" select="'mdq:explanation'"/>
                                        <xsl:with-param name="nodeWithStringToWrite" select="gmd:description"/>
                                    </xsl:call-template>

                                    <mdq:pass>
                                        <gco:Boolean>true</gco:Boolean>
                                    </mdq:pass>
                                </mdq:DQ_ConformanceResult>
                            </mdq:result>
                        </mdq:DQ_DomainConsistency>
                    </mdq:report>
                </xsl:when>
                <xsl:when test="$type = 'reportReference'">
                    <mdq:standaloneQualityReport>
                        <mdq:DQ_StandaloneQualityReportInformation>
                            <mdq:reportReference>
                                <xsl:call-template name="buildCitation">
                                    <xsl:with-param name="withDescription" select="false()"/>
                                </xsl:call-template>
                            </mdq:reportReference>

                            <xsl:call-template name="writeCharacterStringElement">
                                <xsl:with-param name="elementName" select="'mdq:abstract'"/>
                                <xsl:with-param name="nodeWithStringToWrite" select="gmd:description"/>
                            </xsl:call-template>

                        </mdq:DQ_StandaloneQualityReportInformation>
                    </mdq:standaloneQualityReport>
                </xsl:when>
                <xsl:when test="$type = 'additionalDocumentation'">
                    <xsl:element name="{concat($onlineFunctionMap/entry[@key = $type]/@ns, ':', $type)}">
                        <xsl:call-template name="buildCitation"/>
                    </xsl:element>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:message>Unsupported type: <xsl:value-of select="$type"/></xsl:message>
                </xsl:otherwise>
            </xsl:choose>

        </xsl:for-each>
    </xsl:template>

    <xsl:template name="buildCitation">
        <xsl:param name="withDescription" select="true()"/>
        <cit:CI_Citation>
            <xsl:call-template name="writeCharacterStringElement">
                <xsl:with-param name="elementName" select="'cit:title'"/>
                <xsl:with-param name="nodeWithStringToWrite" select="gmd:name"/>
            </xsl:call-template>

            <cit:onlineResource>
                <cit:CI_OnlineResource>
                    <xsl:apply-templates select="gmd:linkage"
                                         mode="from19139to19115-3.2018"/>
                    <xsl:apply-templates select="gmd:protocol"
                                         mode="from19139to19115-3.2018"/>
                    <xsl:apply-templates select="gmd:applicationProfile"
                                         mode="from19139to19115-3.2018"/>

                    <xsl:if test="$withDescription">
                        <xsl:call-template name="writeCharacterStringElement">
                            <xsl:with-param name="elementName" select="'cit:description'"/>
                            <xsl:with-param name="nodeWithStringToWrite" select="gmd:description"/>
                        </xsl:call-template>
                    </xsl:if>

                    <xsl:apply-templates select="gmd:function"
                                         mode="from19139to19115-3.2018"/>
                </cit:CI_OnlineResource>
            </cit:onlineResource>
        </cit:CI_Citation>
    </xsl:template>
</xsl:stylesheet>
