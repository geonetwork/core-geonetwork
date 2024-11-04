<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns="http://www.w3.org/2005/Atom"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
                xmlns:gmd="http://www.isotc211.org/2005/gmd"
                xmlns:gco="http://www.isotc211.org/2005/gco"
                xmlns:srv="http://www.isotc211.org/2005/srv"
                xmlns:java="java:org.fao.geonet.util.XslUtil"
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                xmlns:georss="http://www.georss.org/georss"
                xmlns:gml="http://www.opengis.net/gml/3.2"
                xmlns:gml320="http://www.opengis.net/gml"
                xmlns:gmx="http://www.isotc211.org/2005/gmx"
                xmlns:opensearch="http://a9.com/-/spec/opensearch/1.1/"
                xmlns:inspire_dls="http://inspire.ec.europa.eu/schemas/inspire_dls/1.0"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                xmlns:skos="http://www.w3.org/2004/02/skos/core#"
                xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
                exclude-result-prefixes="#all">
    <xsl:output method="xml" indent="no" encoding="utf-8"/>

    <xsl:param name="isLocal" select="false()" />
    <xsl:param name="thesauriDir"/>
    <xsl:param name="inspire" select="false()" />
    <xsl:param name="baseUrl" />
    <xsl:param name="nodeUrl" />
    <xsl:param name="opensearchUrlSuffix" select="'opensearch'"/>
    <xsl:param name="opensearchDescriptionFileName" select="'OpenSearchDescription.xml'"/>
    <xsl:param name="atomDescribeServiceUrlSuffix" select="'describe'"/>
    <xsl:param name="atomDescribeDatasetUrlSuffix" select="'describe'"/>
    <xsl:param name="nodeName" select="string('srv')" />
    <xsl:param name="searchTerms" select="''"/>
    <xsl:param name="requestedLanguage" select="string('eng')" />
    <xsl:param name="requestedCRS" select="''"/>

    <!-- parameters used in case of dataset feed generation -->
    <xsl:variable name="serviceFeedTitle" select="'The parent service feed'" />
    <xsl:variable name="featureconceptThesaurus"
        select="if ($inspire!='false') then document(concat('file:///', replace($thesauriDir, '\\', '/'), '/external/thesauri/theme/featureconcept.en.skos.rdf')) else ''"/>
    <xsl:variable name="featureconcepts"
                select="if ($inspire!='false') then $featureconceptThesaurus//skos:Concept else ''"/>
    <xsl:variable name="featureconceptThesaurusTitle" select="'INSPIRE feature concept dictionary'" />
    <xsl:variable name="featureconceptBaseUrl" select="'http://inspire.ec.europa.eu/featureconcept/'" />

    <xsl:template match="/root">
        <feed xsi:schemaLocation="http://www.w3.org/2005/Atom http://inspire-geoportal.ec.europa.eu/schemas/inspire/atom/1.0/atom.xsd" xml:lang="en">
            <xsl:apply-templates mode="service" select="service/gmd:MD_Metadata"/>
            <xsl:apply-templates mode="dataset" select="dataset/gmd:MD_Metadata"/>
        </feed>
    </xsl:template>

    <!-- === SERVICE FEED ========================================================================================== -->

    <xsl:template mode="service" match="gmd:MD_Metadata">
        <!-- Get first element. TODO: Check if can be several -->
        <xsl:variable name="fileIdentifier" select="gmd:fileIdentifier/gco:CharacterString"/>
        <xsl:variable name="docLang" select="java:twoCharLangCode(gmd:language/gmd:LanguageCode/@codeListValue)"/>
        <xsl:variable name="titleNode" select="gmd:identificationInfo/srv:SV_ServiceIdentification/gmd:citation/gmd:CI_Citation/gmd:title"/>
        <xsl:variable name="title">
            <xsl:apply-templates mode="get-translation" select="$titleNode">
                <xsl:with-param name="lang" select="$requestedLanguage"/>
            </xsl:apply-templates>
        </xsl:variable>

        <xsl:variable name="datasetDates" select="string-join(gmd:dateStamp/gco:DateTime|datasets/*//gmd:dateStamp/gco:DateTime, ' ')" />
        <xsl:variable name="updated" select="java:getMax($datasetDates)"/>

        <!-- REQ 5: title -->
        <title>
            <xsl:value-of select="$title"/>
        </title>
        <!-- REC 1: subtitle -->
        <subtitle>
            <xsl:apply-templates mode="get-translation" select="gmd:identificationInfo/srv:SV_ServiceIdentification/gmd:abstract">
                <xsl:with-param name="lang" select="$requestedLanguage"/>
            </xsl:apply-templates>
        </subtitle>

        <!-- REQ 6: described-by -->
        <xsl:call-template name="csw-link">
            <xsl:with-param name="lang" select="$requestedLanguage"/>
            <xsl:with-param name="fileIdentifier" select="$fileIdentifier"/>
        </xsl:call-template>

        <!-- REQ 7: self -->
        <xsl:call-template name="atom-link">
            <xsl:with-param name="title" select="$title"/>
            <xsl:with-param name="lang" select="$requestedLanguage"/>
            <xsl:with-param name="fileIdentifier" select="$fileIdentifier"/>
            <xsl:with-param name="rel">self</xsl:with-param>
        </xsl:call-template>
        <!-- REQ 8: search -->
        <!-- The hreflang attribute that is referred to in the TG seems to be unnecessary. The MIWP-5 workgroup recommends the TG editors to remove the part regarding the hreflang attribute from requirement 8. -->
        <!--
        <link rel="search" type="application/opensearchdescription+xml" hreflang="{$docLang}">
        -->
        <link rel="search" type="application/opensearchdescription+xml">
            <xsl:attribute name="title">
                <xsl:call-template name="translated-description">
                    <xsl:with-param name="lang" select="$requestedLanguage"/>
                    <xsl:with-param name="type" select="1"/>
                </xsl:call-template>
                <xsl:value-of select="$title"/>
             </xsl:attribute>
             <xsl:attribute name="href">
                 <xsl:if test="$isLocal">
                     <xsl:value-of select="concat($nodeUrl,$opensearchUrlSuffix,'/', $opensearchDescriptionFileName, '?uuid=',$fileIdentifier)"/>
                 </xsl:if>
                 <xsl:if test="not($isLocal)">
                     <xsl:value-of select="concat($baseUrl,$opensearchUrlSuffix,'/',java:threeCharLangCode($requestedLanguage),'/',$fileIdentifier,'/',$opensearchDescriptionFileName)"/>
                 </xsl:if>
             </xsl:attribute>
        </link>

        <!-- REQ 36, 38: multilang -->
        <xsl:for-each select="gmd:locale/gmd:PT_Locale/gmd:languageCode/gmd:LanguageCode/@codeListValue">
            <xsl:if test="$requestedLanguage!=.">
                <xsl:call-template name="atom-link">
                    <xsl:with-param name="title">
                        <xsl:apply-templates mode="get-translation" select="$titleNode">
                            <xsl:with-param name="lang" select="."/>
                        </xsl:apply-templates>
                    </xsl:with-param>
                    <xsl:with-param name="lang" select="java:twoCharLangCode(.)"/>
                    <xsl:with-param name="fileIdentifier" select="$fileIdentifier"/>
                    <xsl:with-param name="rel">alternate</xsl:with-param>
                </xsl:call-template>
            </xsl:if>
        </xsl:for-each>
        <xsl:if test="$requestedLanguage!=$docLang">
            <xsl:call-template name="atom-link">
                <xsl:with-param name="title">
                    <xsl:apply-templates mode="get-translation" select="$titleNode">
                        <xsl:with-param name="lang" select="$docLang"/>
                    </xsl:apply-templates>
                </xsl:with-param>
                <xsl:with-param name="lang" select="$docLang"/>
                <xsl:with-param name="fileIdentifier" select="$fileIdentifier"/>
                <xsl:with-param name="rel">alternate</xsl:with-param>
            </xsl:call-template>
        </xsl:if>

        <!-- REQ 9: id -->
        <id>
            <xsl:call-template name="atom-link-href">
                <xsl:with-param name="lang" select="$requestedLanguage"/>
                <xsl:with-param name="fileIdentifier" select="$fileIdentifier"/>
            </xsl:call-template>
        </id>

        <!-- REQ 10: rights -->
        <rights>
            <xsl:apply-templates mode="translated-rights" select="gmd:identificationInfo/srv:SV_ServiceIdentification"/>
        </rights>

        <!-- REQ 11: updated -->
        <updated><xsl:value-of select="$updated"/>Z</updated>

        <!-- REQ 12: author -->
        <xsl:call-template name="add-author">
            <xsl:with-param name="pocs" select="gmd:identificationInfo/srv:SV_ServiceIdentification/gmd:pointOfContact"/>
        </xsl:call-template>

        <!-- REQ 1: list of separate entries -->
        <xsl:for-each select="datasets/gmd:MD_Metadata">
            <entry>
                <xsl:apply-templates mode="dataset_entry" select="."/>
            </entry>
        </xsl:for-each>
    </xsl:template>

    <!-- === Dataset entry within Service Feed ===================================================================== -->

    <xsl:template mode="dataset_entry" match="gmd:MD_Metadata">
        <xsl:variable name="fileIdentifier" select="./gmd:fileIdentifier/gco:CharacterString"/>
        <xsl:variable name="datasetTitleNode" select="./gmd:identificationInfo[1]//gmd:citation[1]/gmd:CI_Citation/gmd:title"/>
        <xsl:variable name="datasetTitle">
            <xsl:apply-templates mode="get-translation" select="$datasetTitleNode">
                <xsl:with-param name="lang" select="$requestedLanguage"/>
            </xsl:apply-templates>
        </xsl:variable>
        <xsl:variable name="identifierCode" select="./gmd:identificationInfo[1]//gmd:citation/gmd:CI_Citation/gmd:identifier[1]/*/gmd:code/(gco:CharacterString|gmx:Anchor)"/>
        <xsl:variable name="identifierCodeSpace" select="./gmd:identificationInfo[1]//gmd:citation/gmd:CI_Citation/gmd:identifier[1]/*/gmd:codeSpace/(gco:CharacterString|gmx:Anchor)"/>

        <!-- REQ 13: code and namespace -->
        <inspire_dls:spatial_dataset_identifier_code><xsl:value-of select="$identifierCode"/></inspire_dls:spatial_dataset_identifier_code>
        <inspire_dls:spatial_dataset_identifier_namespace><xsl:value-of select="$identifierCodeSpace"/></inspire_dls:spatial_dataset_identifier_namespace>

        <!-- Take the first one of the referenceSystemInfo and handle it as the default CRS for download resources -->
        <xsl:variable name="defaultCRS" select="normalize-space(.//gmd:referenceSystemInfo[1]/gmd:MD_ReferenceSystem/gmd:referenceSystemIdentifier/gmd:RS_Identifier/gmd:code/(gco:CharacterString|gmx:Anchor))"/>
        <xsl:call-template name="add-category">
            <xsl:with-param name="crs" select="$defaultCRS"/>
        </xsl:call-template>

        <xsl:for-each select=".//gmd:MD_DigitalTransferOptions/gmd:onLine/gmd:CI_OnlineResource[gmd:function/gmd:CI_OnLineFunctionCode/@codeListValue='download']">
            <xsl:variable name="crs">
                <xsl:call-template name="get-download-crs">
                    <xsl:with-param name="defaultCRS" select="$defaultCRS"/>
                    <xsl:with-param name="downloadCRS" select="normalize-space(gmd:description/gco:CharacterString)"/>
                </xsl:call-template>
            </xsl:variable>
            <!-- REQ 20: category (CRS) -->
            <xsl:if test="$defaultCRS!=$crs">
                <xsl:call-template name="add-category">
                    <xsl:with-param name="crs" select="$crs"/>
                </xsl:call-template>
            </xsl:if>
        </xsl:for-each>

        <!-- REC 4: author -->
        <xsl:call-template name="add-author">
            <xsl:with-param name="pocs" select="gmd:identificationInfo//gmd:pointOfContact"/>
        </xsl:call-template>

        <!-- REQ 17: id -->
        <id>
            <xsl:call-template name="atom-link-href">
                <xsl:with-param name="lang" select="$requestedLanguage"/>
                <xsl:with-param name="identifier" select="$identifierCode"/>
                <xsl:with-param name="codeSpace"  select="$identifierCodeSpace"/>
            </xsl:call-template>
        </id>

        <!-- REQ 14: describedby -->
        <xsl:call-template name="csw-link">
            <xsl:with-param name="lang" select="$requestedLanguage"/>
            <xsl:with-param name="fileIdentifier" select="$fileIdentifier"/>
        </xsl:call-template>

        <!-- REQ 15: alternate: ATOM dataset feed -->
        <xsl:call-template name="atom-link">
            <xsl:with-param name="title">
                <xsl:apply-templates mode="get-translation" select="$datasetTitleNode">
                    <xsl:with-param name="lang" select="$requestedLanguage"/>
                </xsl:apply-templates>
            </xsl:with-param>
            <xsl:with-param name="lang" select="$requestedLanguage"/>
            <xsl:with-param name="identifier" select="$identifierCode"/>
            <xsl:with-param name="codeSpace" select="$identifierCodeSpace"/>
            <xsl:with-param name="rel">alternate</xsl:with-param>
        </xsl:call-template>

        <!-- REQ 16: entry link to WFS implementation (only for hybrid implementations) -->
        <xsl:for-each select="//gmd:identificationInfo/srv:SV_ServiceIdentification/srv:containsOperations/srv:SV_OperationMetadata[srv:operationName/gco:CharacterString='GetCapabilities' and contains(gmd:protocol/gco:CharacterString,'WFS')]">
            <xsl:variable name="capabalitiesURL" select="normalize-space(srv:connectPoint/gmd:CI_OnlineResource/gmd:linkage/gmd:URL)"/>
            <xsl:if test="$capabalitiesURL!=''">
                <link href="{$capabalitiesURL}" rel="related" type="application/xml" title="Service implementing Direct Access operations"/>
            </xsl:if>
        </xsl:for-each>

        <!-- REC 3: rights -->
        <xsl:variable name="rights">
            <xsl:apply-templates mode="translated-rights" select="gmd:identificationInfo/gmd:MD_DataIdentification"/>
        </xsl:variable>

        <xsl:if test="normalize-space($rights)">
            <rights>
                <xsl:value-of select="$rights"/>
            </rights>
        </xsl:if>

        <!-- REC 5: summary -->
        <summary>
            <xsl:apply-templates mode="get-translation" select="gmd:identificationInfo/gmd:MD_DataIdentification/gmd:abstract">
                <xsl:with-param name="lang" select="$requestedLanguage"/>
            </xsl:apply-templates>
        </summary>

        <!-- REQ 18: title -->
        <title>
            <xsl:value-of select="$datasetTitle" />
            <!--<xsl:call-template name="translated-description"><xsl:with-param name="lang" select="$requestedLanguage"/><xsl:with-param name="type" select="3"/></xsl:call-template><xsl:value-of select="$datasetTitle"/>-->
        </title>

        <!-- REQ 19: updated -->
        <xsl:variable name="datasetDates" select="string-join(gmd:dateStamp/gco:DateTime, ' ')" />
        <xsl:variable name="updated" select="java:getMax($datasetDates)"/>
        <updated><xsl:value-of select="$updated"/>Z</updated>

        <!-- REC 6: georss -->
        <xsl:variable name="w" select="normalize-space(.//gmd:EX_GeographicBoundingBox/gmd:westBoundLongitude/gco:Decimal/text())"/>
        <xsl:variable name="e" select="normalize-space(.//gmd:EX_GeographicBoundingBox/gmd:eastBoundLongitude/gco:Decimal/text())"/>
        <xsl:variable name="s" select="normalize-space(.//gmd:EX_GeographicBoundingBox/gmd:southBoundLatitude/gco:Decimal/text())"/>
        <xsl:variable name="n" select="normalize-space(.//gmd:EX_GeographicBoundingBox/gmd:northBoundLatitude/gco:Decimal/text())"/>

        <xsl:if test="number($w)=number($w) and number($e)=number($e) and number($s)=number($s) and number($n)=number($n)">
            <xsl:variable name="fw" select="format-number(number($w),'#.00000')"/>
            <xsl:variable name="fe" select="format-number(number($e),'#.00000')"/>
            <xsl:variable name="fs" select="format-number(number($s),'#.00000')"/>
            <xsl:variable name="fn" select="format-number(number($n),'#.00000')"/>

            <!-- REC 7: polygon -->
            <georss:polygon>
                <xsl:value-of select="concat($fs,' ',$fw,' ',$fn,' ',$fw,' ',$fn,' ',$fe,' ',$fs,' ',$fe,' ',$fs,' ',$fw)"/>
            </georss:polygon>
        </xsl:if>

    </xsl:template>

    <!-- === DATASET FEED ========================================================================================== -->

    <xsl:template mode="dataset" match="gmd:MD_Metadata">
        <xsl:variable name="fileIdentifier" select="./gmd:fileIdentifier/gco:CharacterString"/>
        <xsl:variable name="docLang" select="java:twoCharLangCode(./gmd:language/gmd:LanguageCode/@codeListValue)"/>
        <xsl:variable name="datasetTitleNode" select="./gmd:identificationInfo[1]//gmd:citation[1]/gmd:CI_Citation/gmd:title"/>
        <xsl:variable name="datasetTitle">
            <xsl:apply-templates mode="get-translation" select="$datasetTitleNode">
                <xsl:with-param name="lang" select="$requestedLanguage"/>
            </xsl:apply-templates>
        </xsl:variable>
        <xsl:variable name="identifierCode" select="./gmd:identificationInfo[1]//gmd:citation/gmd:CI_Citation/gmd:identifier[1]/gmd:MD_Identifier/gmd:code/(gco:CharacterString|gmx:Anchor)|
                                                    ./gmd:identificationInfo[1]//gmd:citation/gmd:CI_Citation/gmd:identifier[1]/gmd:RS_Identifier/gmd:code/(gco:CharacterString|gmx:Anchor)"/>
        <xsl:variable name="identifierCodeSpace" select="./gmd:identificationInfo[1]//gmd:citation/gmd:CI_Citation/gmd:identifier[1]/gmd:MD_Identifier/gmd:codeSpace/(gco:CharacterString|gmx:Anchor)|
                                                    ./gmd:identificationInfo[1]//gmd:citation/gmd:CI_Citation/gmd:identifier[1]/gmd:RS_Identifier/gmd:codeSpace/(gco:CharacterString|gmx:Anchor)"/>

        <!-- REQ 21: title -->
        <title>
            <xsl:call-template name="translated-description">
                <xsl:with-param name="lang" select="$requestedLanguage"/>
                <xsl:with-param name="type" select="3"/>
            </xsl:call-template>
            <xsl:value-of select="$datasetTitle"/>
        </title>

        <!-- REC 8: subtitle -->
        <subtitle>
            <xsl:call-template name="translated-description">
                <xsl:with-param name="lang" select="$requestedLanguage"/>
                <xsl:with-param name="type" select="3"/>
            </xsl:call-template>
            <xsl:apply-templates mode="get-translation" select="gmd:identificationInfo/gmd:MD_DataIdentification/gmd:abstract">
                <xsl:with-param name="lang" select="$requestedLanguage"/>
            </xsl:apply-templates>
        </subtitle>

        <!-- REQ 28: TODO implement thesaurus to be used in editor to select one or more INSPIRE Spatial Object Types and based on selection show here the links -->
        <xsl:for-each select=".//gmd:keyword/gco:CharacterString[../../gmd:thesaurusName/gmd:CI_Citation/gmd:title/gco:CharacterString=$featureconceptThesaurusTitle]">
            <xsl:variable name="concept" select="."/>
            <link href="{$featureconcepts[skos:prefLabel = $concept]/@rdf:about}" rel="describedby" type="text/html" />
        </xsl:for-each>
        <xsl:for-each select=".//gmd:keyword/gmx:Anchor/@xlink:href[contains(.,'featureconcept')]">
            <xsl:call-template name="get-inspire-spatial-object-type-link">
                <xsl:with-param name="anchorhref" select="."/>
            </xsl:call-template>
        </xsl:for-each>

        <!-- describedby -->
        <xsl:call-template name="csw-link">
            <xsl:with-param name="lang" select="$requestedLanguage"/>
            <xsl:with-param name="fileIdentifier" select="$fileIdentifier"/>
        </xsl:call-template>

        <!-- ATOM dataset feed -->
        <xsl:call-template name="atom-link">
            <xsl:with-param name="title">
                <xsl:apply-templates mode="get-translation" select="$datasetTitleNode">
                    <xsl:with-param name="lang" select="$requestedLanguage"/>
                </xsl:apply-templates>
            </xsl:with-param>
            <xsl:with-param name="lang" select="$requestedLanguage"/>
            <xsl:with-param name="identifier" select="$identifierCode"/>
            <xsl:with-param name="codeSpace" select="$identifierCodeSpace"/>
            <xsl:with-param name="rel">self</xsl:with-param>
        </xsl:call-template>

        <xsl:for-each select="gmd:locale/gmd:PT_Locale/gmd:languageCode/gmd:LanguageCode/@codeListValue">
            <xsl:if test="$requestedLanguage!=.">
                <xsl:call-template name="atom-link">
                    <xsl:with-param name="title">
                        <xsl:apply-templates mode="get-translation" select="$datasetTitleNode">
                            <xsl:with-param name="lang" select="java:twoCharLangCode(.)"/>
                        </xsl:apply-templates>
                    </xsl:with-param>
                    <xsl:with-param name="lang" select="."/>
                    <xsl:with-param name="identifier" select="$identifierCode"/>
                    <xsl:with-param name="codeSpace" select="$identifierCodeSpace"/>
                    <xsl:with-param name="rel">alternate</xsl:with-param>
                </xsl:call-template>
            </xsl:if>
        </xsl:for-each>

        <xsl:if test="$requestedLanguage!=$docLang">
            <xsl:call-template name="atom-link">
                <xsl:with-param name="title">
                    <xsl:apply-templates mode="get-translation" select="$datasetTitleNode">
                        <xsl:with-param name="lang" select="$docLang"/>
                    </xsl:apply-templates>
                </xsl:with-param>
                <xsl:with-param name="lang" select="$docLang"/>
                <xsl:with-param name="identifier" select="$identifierCode"/>
                <xsl:with-param name="codeSpace" select="$identifierCodeSpace"/>
                <xsl:with-param name="rel">alternate</xsl:with-param>
            </xsl:call-template>
        </xsl:if>

        <!-- REC 9: upward link to download service feed -->
        <xsl:variable name="serviceIdentifier" select="normalize-space(/root/serviceIdentifier)"/>
        <xsl:if test="$serviceIdentifier">
            <link rel="up" title="{$serviceFeedTitle}" type="application/atom+xml" hreflang="{$requestedLanguage}">
                <xsl:attribute name="href">
                    <xsl:call-template name="atom-link-href">
                        <xsl:with-param name="lang" select="$requestedLanguage"/>
                        <xsl:with-param name="fileIdentifier" select="$serviceIdentifier"/>
                    </xsl:call-template>
                </xsl:attribute>
            </link>
        </xsl:if>

        <!-- REQ 22: id -->
        <id>
            <xsl:call-template name="atom-link-href">
                <xsl:with-param name="lang" select="$requestedLanguage"/>
                <xsl:with-param name="identifier" select="$identifierCode"/>
                <xsl:with-param name="codeSpace"  select="$identifierCodeSpace"/>
            </xsl:call-template>
        </id>

        <!-- REQ 23: rights -->
        <rights>
            <xsl:apply-templates mode="translated-rights" select="gmd:identificationInfo/gmd:MD_DataIdentification"/>
        </rights>

        <!-- REQ 24: updated -->
        <xsl:variable name="datasetDates" select="string-join(gmd:dateStamp/gco:DateTime, ' ')" />
        <xsl:variable name="updated" select="java:getMax($datasetDates)"/>
        <updated><xsl:value-of select="$updated"/>Z</updated>

        <!-- REC 6: georss -->
        <xsl:variable name="w" select="normalize-space(.//gmd:EX_GeographicBoundingBox/gmd:westBoundLongitude/gco:Decimal/text())"/>
        <xsl:variable name="e" select="normalize-space(.//gmd:EX_GeographicBoundingBox/gmd:eastBoundLongitude/gco:Decimal/text())"/>
        <xsl:variable name="s" select="normalize-space(.//gmd:EX_GeographicBoundingBox/gmd:southBoundLatitude/gco:Decimal/text())"/>
        <xsl:variable name="n" select="normalize-space(.//gmd:EX_GeographicBoundingBox/gmd:northBoundLatitude/gco:Decimal/text())"/>

        <!-- REQ 25: author -->
        <xsl:call-template name="add-author">
            <xsl:with-param name="pocs" select="gmd:identificationInfo//gmd:pointOfContact"/>
        </xsl:call-template>

        <xsl:variable name="defaultCRS" select="normalize-space(.//gmd:referenceSystemInfo[1]/gmd:MD_ReferenceSystem/gmd:referenceSystemIdentifier/gmd:RS_Identifier/gmd:code/(gco:CharacterString|gmx:Anchor))"/>

        <xsl:call-template name="add-category">
            <xsl:with-param name="crs" select="$defaultCRS"/>
        </xsl:call-template>
        <xsl:for-each select="distinct-values(.//gmd:MD_DigitalTransferOptions/gmd:onLine/gmd:CI_OnlineResource[gmd:function/gmd:CI_OnLineFunctionCode/@codeListValue='download']/normalize-space(gmd:description/gco:CharacterString))">
            <xsl:variable name="crs">
                <xsl:call-template name="get-download-crs">
                    <xsl:with-param name="defaultCRS" select="$defaultCRS"/>
                    <xsl:with-param name="downloadCRS" select="."/>
                </xsl:call-template>
            </xsl:variable>
            <xsl:if test="$defaultCRS!=$crs">
                <xsl:call-template name="add-category">
                    <xsl:with-param name="crs" select="$crs"/>
                </xsl:call-template>
            </xsl:if>
        </xsl:for-each>
        <!-- REQ 26: download entries -->
        <!-- NVM about REQ 27: an entry for each CRS or only the requested CRS in case of a search request -->
        <xsl:for-each select=".//gmd:MD_DigitalTransferOptions/gmd:onLine/gmd:CI_OnlineResource[gmd:function/gmd:CI_OnLineFunctionCode/@codeListValue='download']">
            <xsl:variable name="crs">
                <xsl:call-template name="get-download-crs">
                    <xsl:with-param name="defaultCRS" select="$defaultCRS"/>
                    <xsl:with-param name="downloadCRS" select="normalize-space(gmd:description/gco:CharacterString)"/>
                </xsl:call-template>
            </xsl:variable>
            <xsl:variable name="epsgCode">
                <xsl:call-template name="get-epsg-code">
                    <xsl:with-param name="crs" select="$crs"/>
                </xsl:call-template>
            </xsl:variable>
            <xsl:variable name="crsLabel">
                <xsl:call-template name="get-crs-label">
                    <xsl:with-param name="epsgCode" select="$epsgCode"/>
                    <xsl:with-param name="crs" select="$crs"/>
                </xsl:call-template>
            </xsl:variable>

            <xsl:variable name="mimeFileType" select="normalize-space(gmd:name/gmx:MimeFileType/@type)"/>

            <xsl:variable name="infer-mimetype">
                <xsl:call-template name="infer-mimetype">
                    <xsl:with-param name="onlineresource" select="."/>
                </xsl:call-template>
            </xsl:variable>

            <xsl:variable name="inspireMimeType">
                <xsl:choose>
                    <xsl:when test="$mimeFileType='multipart/x-zip' or $mimeFileType='application/zip'">
                        <xsl:value-of select="string('application/x-gmz')"/>
                    </xsl:when>
                    <xsl:when test="$mimeFileType='' and $infer-mimetype!=''">
                        <xsl:value-of select="$infer-mimetype"/>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:value-of select="$mimeFileType"/>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:variable>

            <xsl:variable name="mimeTypeLabel">
                <xsl:call-template name="get-mimetype-label">
                    <xsl:with-param name="mimeType" select="$inspireMimeType"/>
                </xsl:call-template>
            </xsl:variable>

            <xsl:variable name="entryTitle" select="concat($datasetTitle,' in ', $crsLabel, if ($mimeTypeLabel!='') then concat(' - ', $mimeTypeLabel) else '')"/>

            <xsl:if test="($requestedCRS='' or $requestedCRS=$crs) and ($searchTerms='' or contains(lower-case($entryTitle),$searchTerms))">
                <entry>
                    <inspire_dls:spatial_dataset_identifier_code><xsl:value-of select="$identifierCode"/></inspire_dls:spatial_dataset_identifier_code>
                    <inspire_dls:spatial_dataset_identifier_namespace><xsl:value-of select="$identifierCodeSpace"/></inspire_dls:spatial_dataset_identifier_namespace>

                    <!-- check REQ 35: category -->
                    <xsl:call-template name="add-category">
                        <xsl:with-param name="crs" select="$crs"/>
                    </xsl:call-template>
                    <!-- not required -->
                    <id>
                        <xsl:value-of select="./gmd:linkage/gmd:URL" />
                    </id>

                    <!-- REQ 29: alternate: link to data -->
                    <!-- REQ 30: mimetype -->
                    <!-- REQ 31: hreflang -->
                    <!-- NVM REQ 32: section: multiple physical files -->
                    <!-- NVM REQ 33: content: multiple physical files description -->
                    <!-- NVM REC 10: bbox: multiple physical files -->
                    <!-- NVM REC 11: time: multiple physical files -->
                    <link title="{$entryTitle}"
                          rel="alternate"
                          type="{$inspireMimeType}"
                          href="{gmd:linkage/gmd:URL}"
                          hreflang="{$docLang}">

                        <xsl:variable name="units" select="../../gmd:unitsOfDistribution/gco:CharacterString"/>
                        <xsl:variable name="length" select="../../gmd:transferSize/gco:Real"/>
                        <xsl:if test="number($length) = number($length)">
                            <xsl:attribute name="length" select="format-number(number($length) * (if ($units='MB') then 1000000 else 1),'#')" />
                        </xsl:if>
                    </link>
                    <title><xsl:value-of select="$entryTitle" /></title>
                    <updated><xsl:value-of select="$updated"/>Z</updated>

                    <!-- check REQ 34: media types -->

                    <xsl:if test="number($w)=number($w) and number($e)=number($e) and number($s)=number($s) and number($n)=number($n)">
                        <xsl:variable name="fw" select="format-number(number($w),'#.00000')"/>
                        <xsl:variable name="fe" select="format-number(number($e),'#.00000')"/>
                        <xsl:variable name="fs" select="format-number(number($s),'#.00000')"/>
                        <xsl:variable name="fn" select="format-number(number($n),'#.00000')"/>
                        <georss:polygon><xsl:value-of select="concat($fs,' ',$fw,' ',$fn,' ',$fw,' ',$fn,' ',$fe,' ',$fs,' ',$fe,' ',$fs,' ',$fw)"/></georss:polygon>
                    </xsl:if>
                </entry>
            </xsl:if>
        </xsl:for-each>
    </xsl:template>

    <xsl:template name="translated-description">
        <xsl:param name="lang"/>
        <xsl:param name="type"/>
        <xsl:choose>
            <xsl:when test="$type=1">
                <xsl:value-of select="'OpenSearch Description: '"/>
            </xsl:when>
            <xsl:when test="$type=2">
                <xsl:value-of select="'INSPIRE Download Service ATOM feed: '"/>
            </xsl:when>
            <xsl:when test="$type=3">
                <xsl:value-of select="'INSPIRE Dataset ATOM feed: '"/>
            </xsl:when>
        </xsl:choose>
    </xsl:template>

    <xsl:template mode="translated-rights" match="srv:SV_ServiceIdentification|gmd:MD_DataIdentification">
<!--        <xsl:variable name="useLimitation" select="normalize-space(gmd:resourceConstraints/gmd:MD_Constraints/gmd:useLimitation/gco:CharacterString)"/>
        <xsl:variable name="translated-useLimitation" select="normalize-space(gmd:resourceConstraints/gmd:MD_Constraints/gmd:useLimitation/gmd:PT_FreeText/gmd:textGroup/gmd:LocalisedCharacterString[@locale=concat('#',upper-case(java:threeCharLangCode($requestedLanguage)))])"/>
        <xsl:choose>
            <xsl:when test="$translated-useLimitation!=''"><xsl:value-of select="$translated-useLimitation"/></xsl:when>
            <xsl:otherwise><xsl:value-of select="$useLimitation"/></xsl:otherwise>
        </xsl:choose>
        <xsl:text> </xsl:text>
-->
        <xsl:for-each select="gmd:resourceConstraints/gmd:MD_LegalConstraints">
            <xsl:variable name="accessConstraints" select="gmd:accessConstraints/gmd:MD_RestrictionCode/@codeListValue"/>
            <xsl:variable name="otherConstraints" select="normalize-space(gmd:otherConstraints[1]/(gco:CharacterString|gmx:Anchor))"/>
            <xsl:variable name="translated-otherConstraints" select="normalize-space(gmd:otherConstraints[1]/gmd:PT_FreeText/gmd:textGroup/gmd:LocalisedCharacterString[@locale=concat('#',upper-case(java:threeCharLangCode($requestedLanguage)))])"/>
            <xsl:variable name="resultValue">
                <xsl:choose>
                    <xsl:when test="$accessConstraints='otherRestrictions' and $otherConstraints!=''">
                        <xsl:if test="$translated-otherConstraints!=''">
                            <xsl:value-of select="$translated-otherConstraints"/>
                        </xsl:if>
                        <xsl:if test="$translated-otherConstraints=''">
                            <xsl:value-of select="$otherConstraints"/>
                        </xsl:if>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:value-of select="$accessConstraints"/>
                        <!--<xsl:value-of select="/root/gui/schemas/iso19139/codelists/codelist[@name = 'gmd:MD_RestrictionCode']/entry[code = $accessConstraints]/description"/>-->
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:variable>
            <xsl:if test="normalize-space($resultValue)!=''">
                <xsl:value-of select="normalize-space($resultValue)"/>
                <xsl:if test="not(ends-with(normalize-space($resultValue),'.'))">.</xsl:if>
                <xsl:text> </xsl:text>
            </xsl:if>
        </xsl:for-each>
        <xsl:for-each select="gmd:resourceConstraints/gmd:MD_SecurityConstraints">
            <xsl:variable name="classificationConstraints" select="gmd:classification/gmd:MD_ClassificationCode/@codeListValue"/>
            <xsl:variable name="resultValue" select="$classificationConstraints"/>
            <!--<xsl:variable name="resultValue" select="/root/gui/schemas/iso19139/codelists/codelist[@name = 'gmd:MD_ClassificationCode']/entry[code = $classificationConstraints]/description"/>-->
            <xsl:if test="normalize-space($resultValue)!=''">
                <xsl:value-of select="normalize-space($resultValue)"/>
                <xsl:if test="not(ends-with(normalize-space($resultValue),'.'))">.</xsl:if>
                <xsl:text> </xsl:text>
            </xsl:if>
        </xsl:for-each>
    </xsl:template>

    <xsl:template name="csw-link">
        <xsl:param name="lang"/>
        <xsl:param name="fileIdentifier"/>
        <link rel="describedby" type="application/xml">
            <xsl:attribute name="href" select="concat($baseUrl, $nodeName, '/',java:threeCharLangCode($lang),'/csw?service=CSW&amp;version=2.0.2&amp;request=GetRecordById&amp;outputschema=http://www.isotc211.org/2005/gmd&amp;elementSetName=full&amp;id=',$fileIdentifier)"/>
           </link>
    </xsl:template>

    <xsl:template name="atom-link">
        <xsl:param name="lang"/>
        <xsl:param name="fileIdentifier"/>
        <xsl:param name="identifier"/>
        <xsl:param name="codeSpace"/>
        <xsl:param name="title"/>
        <xsl:param name="rel"/>

        <xsl:variable name="type" select="if ($fileIdentifier!='') then 2 else 3"/>

        <link type="application/atom+xml">
            <xsl:if test="$rel!=''">
                   <xsl:attribute name="rel" select="$rel"/>
            </xsl:if>
            <!-- REQ 38: the hreflang attribute indicates the language of the alternative representation -->
<!--            <xsl:if test="$rel!='self'">-->
               <xsl:attribute name="hreflang" select="$lang"/>
<!--            </xsl:if>-->
            <xsl:attribute name="title">
                <xsl:call-template name="translated-description">
                    <xsl:with-param name="lang" select="$lang"/>
                    <xsl:with-param name="type" select="$type"/>
                </xsl:call-template>
                <xsl:value-of select="$title"/>
            </xsl:attribute>
            <xsl:attribute name="href">
                <xsl:call-template name="atom-link-href">
                    <xsl:with-param name="lang" select="$lang"/>
                    <xsl:with-param name="fileIdentifier" select="$fileIdentifier"/>
                    <xsl:with-param name="identifier" select="$identifier"/>
                    <xsl:with-param name="codeSpace" select="$codeSpace"/>
                </xsl:call-template>
            </xsl:attribute>
        </link>
    </xsl:template>

    <xsl:template name="atom-link-href">
        <xsl:param name="lang"/>
        <xsl:param name="fileIdentifier"/>
        <xsl:param name="identifier"/>
        <xsl:param name="codeSpace"/>
        <xsl:if test="$fileIdentifier!=''">
            <xsl:if test="$isLocal">
                <xsl:value-of select="concat($nodeUrl,$atomDescribeServiceUrlSuffix,'?uuid=',$fileIdentifier,'&amp;language=',$lang)" />
            </xsl:if>
            <xsl:if test="not($isLocal)">
                <xsl:value-of select="concat($baseUrl,$opensearchUrlSuffix,'/',java:threeCharLangCode($lang),'/',$fileIdentifier,'/',$atomDescribeServiceUrlSuffix)" />
            </xsl:if>
        </xsl:if>
        <xsl:if test="$identifier != ''">
            <xsl:variable name="requestParams" select="concat('spatial_dataset_identifier_code=',$identifier,if($codeSpace != '') then concat('&amp;spatial_dataset_identifier_namespace=',$codeSpace) else '')" />
            <xsl:if test="$isLocal">
                <xsl:value-of select="concat($nodeUrl,$atomDescribeDatasetUrlSuffix,'?',$requestParams,'&amp;language=',$lang)" />
            </xsl:if>
            <xsl:if test="not($isLocal)">
                <xsl:value-of select="concat($baseUrl,$opensearchUrlSuffix,'/',java:threeCharLangCode($lang),'/', $atomDescribeDatasetUrlSuffix,'?',$requestParams)" />
            </xsl:if>
        </xsl:if>
    </xsl:template>

    <xsl:template mode="get-translation" match="*|@*">
        <xsl:param name="lang"/>
        <xsl:variable name="translation" select="gmd:PT_FreeText/gmd:textGroup/gmd:LocalisedCharacterString[@locale=concat('#',upper-case($lang))]"/>
        <xsl:choose>
            <xsl:when test="$translation!=''">
                <xsl:value-of select="$translation"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="normalize-space(gco:CharacterString)"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <!-- Take the poc having role=author if exists, else take the first poc -->

    <xsl:template name="add-author">
        <xsl:param name="pocs"/>
        <xsl:choose>
            <xsl:when test="count($pocs/gmd:CI_ResponsibleParty/gmd:role/gmd:CI_RoleCode[@codeListValue='author'])>0">
                <xsl:apply-templates mode="author_element" select="$pocs[gmd:CI_ResponsibleParty/gmd:role/gmd:CI_RoleCode/@codeListValue='author'][1]"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:apply-templates mode="author_element" select="$pocs[1]"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template mode="author_element" match="gmd:pointOfContact">
        <author>
            <name>
                <xsl:value-of select="./gmd:CI_ResponsibleParty/gmd:organisationName/gco:CharacterString"/>
            </name>
            <email>
                <xsl:value-of select="./gmd:CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:address/*[name(.)='gmd:CI_Address' or @gco:isoType='CI_Address_Type']/gmd:electronicMailAddress/gco:CharacterString"/>
            </email>
        </author>
    </xsl:template>

    <xsl:template name="infer-mimetype">
        <xsl:param name="onlineresource"/>

        <xsl:variable name="filename" select="lower-case(normalize-space($onlineresource/gmd:linkage/gmd:URL/text()))"/>

        <xsl:choose>
            <xsl:when test="ends-with($filename, '.bz')">application/x-bzip</xsl:when>
            <xsl:when test="ends-with($filename, '.bz2')">application/x-bzip2</xsl:when>
            <xsl:when test="ends-with($filename, '.cdf')">application/x-netcdf</xsl:when>
            <xsl:when test="ends-with($filename, '.gif')">image/gif</xsl:when>
            <xsl:when test="ends-with($filename, '.gml')">application/gml+xml</xsl:when>
            <xsl:when test="ends-with($filename, '.gz')">application/x-compressed</xsl:when>
            <xsl:when test="ends-with($filename, '.hdf')">application/x-hdf</xsl:when>
            <xsl:when test="ends-with($filename, '.jpeg')">image/jpeg</xsl:when>
            <xsl:when test="ends-with($filename, '.jpg')">image/jpeg</xsl:when>
            <xsl:when test="ends-with($filename, '.lzh')">application/x-lzh</xsl:when>
            <xsl:when test="ends-with($filename, '.tif')">image/tiff</xsl:when>
            <xsl:when test="ends-with($filename, '.zip')">application/zip</xsl:when>
            <xsl:otherwise>application/octet-stream</xsl:otherwise>
        </xsl:choose>

    </xsl:template>

    <xsl:template name="get-mimetype-label">
        <xsl:param name="mimeType"/>
        <xsl:choose>
            <xsl:when test="$mimeType='application/atom+xml'">INSPIRE Atom Service/Dataset feed</xsl:when>
            <xsl:when test="$mimeType='application/gml+xml'">GML files</xsl:when>
            <xsl:when test="$mimeType='application/pdf'">PDF files</xsl:when>
            <xsl:when test="$mimeType='application/vnd.google-earth.kml+xml'">KML files</xsl:when>
            <xsl:when test="$mimeType='application/vnd.google-earth.kmz'">KMZ files</xsl:when>
            <xsl:when test="$mimeType='application/word'">Word files</xsl:when>
            <xsl:when test="$mimeType='application/x-ascii-grid'">ASCII GRID files</xsl:when>
            <xsl:when test="$mimeType='application/x-ecw'">Enhanced Compressed Wavelet (ECW) files</xsl:when>
            <xsl:when test="$mimeType='application/x-filegdb'">Esri file geodatabases</xsl:when>
            <xsl:when test="$mimeType='application/x-gmz'">Zipped GML files</xsl:when>
            <xsl:when test="$mimeType='application/x-las'">LASer file format (LAS)</xsl:when>
            <xsl:when test="$mimeType='application/x-laz'">Zipped LAS files</xsl:when>
            <xsl:when test="$mimeType='application/xls'">Excel files</xsl:when>
            <xsl:when test="$mimeType='application/x-oracledump'">Oracle dumps</xsl:when>
            <xsl:when test="$mimeType='application/x-shapefile'">Zipped Esri shape files</xsl:when>
            <xsl:when test="$mimeType='application/x-tab'">MapInfo TAB format</xsl:when>
            <xsl:when test="$mimeType='application/x-tab-raster'">MapInfo Raster TAB format</xsl:when>
            <xsl:when test="$mimeType='application/x-worldfile'">Wereld files</xsl:when>
            <xsl:when test="$mimeType='application/zip'">Zip files</xsl:when>
            <xsl:when test="$mimeType='image/gif'">GIF files</xsl:when>
            <xsl:when test="$mimeType='image/jp2'">JPEG 2000 files</xsl:when>
            <xsl:when test="$mimeType='image/jpeg'">JPG files</xsl:when>
            <xsl:when test="$mimeType='image/png'">PNG files</xsl:when>
            <xsl:when test="$mimeType='image/tiff'">TIFF/GeoTIFF files</xsl:when>
            <xsl:when test="$mimeType='text/csv'">CSV files</xsl:when>
            <xsl:when test="$mimeType='text/html'">HTML files</xsl:when>
            <xsl:when test="$mimeType='text/plain'">Tekst files</xsl:when>
            <xsl:when test="$mimeType='text/xml'">XML files</xsl:when>
            <xsl:otherwise/>
        </xsl:choose>
    </xsl:template>

    <xsl:template name="add-category">
        <xsl:param name="crs"/>

        <xsl:choose>
            <xsl:when test="$crs">
                <xsl:variable name="epsgCode">
                    <xsl:call-template name="get-epsg-code">
                        <xsl:with-param name="crs" select="$crs"/>
                    </xsl:call-template>
                </xsl:variable>

                <xsl:choose>
                    <xsl:when test="$epsgCode">
                        <xsl:variable name="crsLabel">
                            <xsl:call-template name="get-crs-label">
                                <xsl:with-param name="epsgCode" select="$epsgCode"/>
                                <xsl:with-param name="crs" select="$crs"/>
                            </xsl:call-template>
                        </xsl:variable>

                        <category term="{concat('http://www.opengis.net/def/crs/EPSG/0/', $epsgCode)}" label="{$crsLabel}" />
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:variable name="inferredCode">
                            <xsl:choose>
                                <xsl:when test="$crs = 'WGS 84'">4326</xsl:when>
                                <xsl:when test="$crs = 'WGS84'">4326</xsl:when>
                                <xsl:otherwise>unknown</xsl:otherwise>
                            </xsl:choose>
                        </xsl:variable>

                        <category term="{concat('http://www.opengis.net/def/crs/EPSG/0/', $inferredCode)}" label="{$crs}" />
                    </xsl:otherwise>
               </xsl:choose>
            </xsl:when>
            <xsl:otherwise>
                <category term="http://www.opengis.net/def/crs/EPSG/0/unknown" label="Unknown" />
            </xsl:otherwise>
        </xsl:choose>

    </xsl:template>

    <xsl:template name="get-epsg-code">
        <xsl:param name="crs"/>

        <xsl:analyze-string select="$crs" regex="EPSG:\d+" >
            <xsl:matching-substring>
                <xsl:value-of select="substring-after(., 'EPSG:')"/>
            </xsl:matching-substring>
        </xsl:analyze-string>
        <xsl:analyze-string select="$crs" regex="EPSG/0/\d+" >
            <xsl:matching-substring>
                <xsl:value-of select="substring-after(., 'EPSG/0/')"/>
            </xsl:matching-substring>
        </xsl:analyze-string>
    </xsl:template>

    <xsl:template name="get-crs-label">
        <xsl:param name="epsgCode"/>
        <xsl:param name="crs"/>

        <xsl:choose>
            <xsl:when test="$epsgCode = '2154'">RGF93 / Lambert-93</xsl:when>
            <xsl:when test="$epsgCode = '32620'">WGS 84 / UTM zone 20N</xsl:when>
            <xsl:when test="$epsgCode = '2972'">RGFG95 / UTM zone 22N</xsl:when>
            <xsl:when test="$epsgCode = '2975'">RGR92 / UTM zone 40S</xsl:when>
            <xsl:when test="$epsgCode = '4467'">RGSPM06 / UTM zone 21N</xsl:when>
            <xsl:when test="$epsgCode = '4471'">RGM04 / UTM zone 38S</xsl:when>
            <xsl:when test="$epsgCode = '3035'">ETRS89 / LAEA Europe</xsl:when>
            <xsl:when test="$epsgCode = '31370'">Belge 1972 / Belgian Lambert 72</xsl:when>
            <xsl:when test="$epsgCode = '4936'">ETRS89-XYZ: 3D Cartesian in ETRS89</xsl:when>
            <xsl:when test="$epsgCode = '4937'">ETRS89-GRS80h: 3D geodetic in ETRS89 on GRS80</xsl:when>
            <xsl:when test="$epsgCode = '4258'">ETRS89-GRS802D: geodetic in ETRS89 on GRS80</xsl:when>
            <xsl:when test="$epsgCode = '3035'">ETRS89-LAEA2D: LAEA projection in ETRS89 on GRS80</xsl:when>
            <xsl:when test="$epsgCode = '3034'">ETRS89-LCC2D: LCC projection in ETRS89 on GRS80</xsl:when>
            <xsl:when test="$epsgCode = '3038'">ETRS89-TM26N2D: TM projection in ETRS89 on GRS80, zone 26N (30°W to 24°W)</xsl:when>
            <xsl:when test="$epsgCode = '3039'">ETRS89-TM27N2D: TM projection in ETRS89 on GRS80, zone 27N (24°W to 18°W)</xsl:when>
            <xsl:when test="$epsgCode = '3040'">ETRS89-TM28N: 2D TM projection in ETRS89 on GRS80, zone 28N (18°W to 12°W)</xsl:when>
            <xsl:when test="$epsgCode = '3041'">ETRS89-TM29N: 2D TM projection in ETRS89 on GRS80, zone 29N (12°W to 6°W)</xsl:when>
            <xsl:when test="$epsgCode = '3042'">ETRS89-TM30N: 2D TM projection in ETRS89 on GRS80, zone 30N (6°W to 0°)</xsl:when>
            <xsl:when test="$epsgCode = '3043'">ETRS89-TM31N:  2D TM projection in ETRS89 on GRS80, zone 31N (0° to 6°E)</xsl:when>
            <xsl:when test="$epsgCode = '3044'">ETRS89-TM32N: 2D TM projection in ETRS89 on GRS80, zone 32N (6°E to 12°E)</xsl:when>
            <xsl:when test="$epsgCode = '3045'">ETRS89-TM33N: 2D TM projection in ETRS89 on GRS80, zone 33N (12°E to 18°E)</xsl:when>
            <xsl:when test="$epsgCode = '3046'">ETRS89-TM34N: 2D TM projection in ETRS89 on GRS80, zone 34N (18°E to 24°E)</xsl:when>
            <xsl:when test="$epsgCode = '3047'">ETRS89-TM35N: 2D TM projection in ETRS89 on GRS80, zone 35N (24°E to 30°E)</xsl:when>
            <xsl:when test="$epsgCode = '3048'">ETRS89-TM36N: 2D TM projection in ETRS89 on GRS80, zone 36N (30°E to 36°E)</xsl:when>
            <xsl:when test="$epsgCode = '3049'">ETRS89-TM37N: 2D TM projection in ETRS89 on GRS80, zone 37N (36°E to 42°E)</xsl:when>
            <xsl:when test="$epsgCode = '3050'">ETRS89-TM38N: 2D TM projection in ETRS89 on GRS80, zone 38N (42°E to 48°E)</xsl:when>
            <xsl:when test="$epsgCode = '3051'">ETRS89-TM39N: 2D TM projection in ETRS89 on GRS80, zone 39N (48°E to 54°E)</xsl:when>
            <xsl:when test="$epsgCode = '5730'">EVRS: Height in EVRS</xsl:when>
            <xsl:when test="$epsgCode = '7409'">ETRS89-GRS80-EVRS: 3D compound: 2D geodetic in ETRS89 on GRS80, and EVRS height</xsl:when>
            <xsl:otherwise><xsl:value-of select="$crs"/></xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template name="get-download-crs">
        <xsl:param name="defaultCRS"/>
        <xsl:param name="downloadCRS"/>

        <xsl:if test="contains($downloadCRS,'EPSG') and $downloadCRS!=$defaultCRS">
            <xsl:value-of select="$downloadCRS"/>
        </xsl:if>
        <xsl:if test="not(contains($downloadCRS,'EPSG') and $downloadCRS!=$defaultCRS)">
            <xsl:value-of select="$defaultCRS"/>
        </xsl:if>
    </xsl:template>

    <xsl:template name="get-inspire-spatial-object-type-link">
        <xsl:param name="anchorhref"/>
        <xsl:variable name="inspireSpatialObjectType">
            <xsl:analyze-string select="$anchorhref" regex="featureconcept/[a-zA-Z]+" >
                <xsl:matching-substring>
                    <xsl:value-of select="substring-after(., 'featureconcept/')"/>
                </xsl:matching-substring>
            </xsl:analyze-string>
        </xsl:variable>
        <xsl:if test="$inspireSpatialObjectType!=''">
            <link href="{concat($featureconceptBaseUrl,$inspireSpatialObjectType)}" rel="describedby" type="text/html" />
        </xsl:if>
    </xsl:template>
</xsl:stylesheet>
