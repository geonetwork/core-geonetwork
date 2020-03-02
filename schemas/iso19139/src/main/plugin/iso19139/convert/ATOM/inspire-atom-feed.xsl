<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns="http://www.w3.org/2005/Atom"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
                xmlns:gmd="http://www.isotc211.org/2005/gmd"
                xmlns:gco="http://www.isotc211.org/2005/gco"
                xmlns:srv="http://www.isotc211.org/2005/srv"
                xmlns:java="java:org.fao.geonet.util.XslUtil"
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                xmlns:georss="http://www.georss.org/georss"
                xmlns:gml="http://www.opengis.net/gml"
                xmlns:gmx="http://www.isotc211.org/2005/gmx"
                xmlns:opensearch="http://a9.com/-/spec/opensearch/1.1/"
                xmlns:opensearchextensions="http://example.com/opensearchextensions/1.0/"
                xmlns:inspire_dls="http://inspire.ec.europa.eu/schemas/inspire_dls/1.0"
                exclude-result-prefixes="gmx xsl gmd gco srv java">

    <xsl:variable name="applicationProfile">ATOM</xsl:variable>
    <!--<xsl:variable name="applicationProfile">INSPIRE-Download-Atom</xsl:variable>-->

    <xsl:param name="isLocal" select="false()" />
    <xsl:param name="guiLang" select="string('eng')" />
    <xsl:param name="baseUrl" />
    <xsl:param name="nodeName" select="string('srv')" />

    <!-- parameters used in case of dataset feed generation -->
    <xsl:param name="serviceFeedTitle" select="'The parent service feed'" />


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
        <xsl:variable name="docLang" select="gmd:language/gmd:LanguageCode/@codeListValue"/>
        <xsl:variable name="titleNode" select="gmd:identificationInfo/srv:SV_ServiceIdentification/gmd:citation/gmd:CI_Citation/gmd:title"/>
        <xsl:variable name="title">
            <xsl:apply-templates mode="get-translation" select="$titleNode">
                <xsl:with-param name="lang" select="$guiLang"/>
            </xsl:apply-templates>
        </xsl:variable>

        <xsl:variable name="datasetDates" select="string-join(gmd:dateStamp/gco:DateTime|datasets/*//gmd:dateStamp/gco:DateTime, ' ')" />
        <xsl:variable name="updated" select="java:getMax($datasetDates)"/>

        <!-- REQ 5: title -->
        <title>
            <xsl:value-of select="$titleNode/gco:CharacterString" />
            <!--<xsl:call-template name="translated-description"><xsl:with-param name="lang" select="$guiLang"/><xsl:with-param name="type" select="2"/></xsl:call-template><xsl:text> </xsl:text><xsl:value-of select="$title"/>-->
        </title>
        <!-- REC 1: subtitle -->
        <subtitle>
            <xsl:value-of select="gmd:identificationInfo/srv:SV_ServiceIdentification/gmd:abstract/gco:CharacterString" />
            <!--<xsl:call-template name="translated-description"><xsl:with-param name="lang" select="$guiLang"/><xsl:with-param name="type" select="2"/></xsl:call-template><xsl:text> </xsl:text><xsl:apply-templates mode="get-translation" select="gmd:identificationInfo/srv:SV_ServiceIdentification/gmd:abstract"><xsl:with-param name="lang" select="$guiLang"/></xsl:apply-templates>-->
        </subtitle>

        <!-- REQ 6: described-by -->
        <xsl:call-template name="csw-link">
            <xsl:with-param name="lang" select="$guiLang"/>
            <xsl:with-param name="baseUrl" select="$baseUrl"/>
            <xsl:with-param name="fileIdentifier" select="$fileIdentifier"/>
        </xsl:call-template>

        <!-- REQ 7: self -->
        <xsl:call-template name="atom-link">
            <xsl:with-param name="title" select="$title"/>
            <xsl:with-param name="lang" select="$guiLang"/>
            <xsl:with-param name="baseUrl" select="$baseUrl"/>
            <xsl:with-param name="fileIdentifier" select="$fileIdentifier"/>
            <xsl:with-param name="rel" select="'self'"/>
        </xsl:call-template>

        <!-- REQ 8: search -->
        <link rel="search" type="application/opensearchdescription+xml">
            <xsl:attribute name="title">
                <xsl:call-template name="translated-description">
                    <xsl:with-param name="lang" select="$guiLang"/>
                    <xsl:with-param name="type" select="1"/>
                </xsl:call-template>
                <xsl:value-of select="$title"/>
            </xsl:attribute>
            <xsl:attribute name="href" select="concat($baseUrl,'/opensearch/',$guiLang,'/',$fileIdentifier,'/OpenSearchDescription.xml')"/>
        </link>

        <!-- REQ 36, 38: multilang -->
        <xsl:for-each select="gmd:locale/gmd:PT_Locale/gmd:languageCode/gmd:LanguageCode/@codeListValue">
            <xsl:if test="$guiLang!=.">
                <xsl:call-template name="atom-link">
                    <xsl:with-param name="title">
                        <xsl:apply-templates mode="get-translation" select="$titleNode">
                            <xsl:with-param name="lang" select="."/>
                        </xsl:apply-templates>
                    </xsl:with-param>
                    <xsl:with-param name="lang" select="."/>
                    <xsl:with-param name="baseUrl" select="$baseUrl"/>
                    <xsl:with-param name="fileIdentifier" select="$fileIdentifier"/>
                    <xsl:with-param name="rel">
                        <xsl:if test="$guiLang=.">self</xsl:if>
                        <xsl:if test="$guiLang!=.">alternate</xsl:if>
                    </xsl:with-param>
                </xsl:call-template>
            </xsl:if>
        </xsl:for-each>

        <xsl:if test="$guiLang!=$docLang">
            <xsl:call-template name="atom-link">
                <xsl:with-param name="title">
                    <xsl:apply-templates mode="get-translation" select="$titleNode">
                        <xsl:with-param name="lang" select="$docLang"/>
                    </xsl:apply-templates>
                </xsl:with-param>
                <xsl:with-param name="lang" select="$docLang"/>
                <xsl:with-param name="baseUrl" select="$baseUrl"/>
                <xsl:with-param name="fileIdentifier" select="$fileIdentifier"/>
                <xsl:with-param name="rel">alternate</xsl:with-param>
            </xsl:call-template>
        </xsl:if>

        <!-- REQ 9: id -->
        <id>
            <xsl:call-template name="atom-link-href">
                <xsl:with-param name="lang" select="$guiLang"/>
                <xsl:with-param name="baseUrl" select="$baseUrl"/>
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
        <xsl:variable name="docLang" select="./gmd:language/gmd:LanguageCode/@codeListValue"/>
        <xsl:variable name="datasetTitleNode" select="./gmd:identificationInfo[1]//gmd:citation[1]/gmd:CI_Citation/gmd:title"/>
        <xsl:variable name="datasetTitle">
            <xsl:apply-templates mode="get-translation" select="$datasetTitleNode">
                <xsl:with-param name="lang" select="$guiLang"/>
            </xsl:apply-templates>
        </xsl:variable>
        <xsl:variable name="identifierCode" select="(./gmd:identificationInfo//gmd:citation/gmd:CI_Citation/gmd:identifier/gmd:MD_Identifier/gmd:code/gco:CharacterString)[1]|
                                                    ./gmd:identificationInfo[1]//gmd:citation/gmd:CI_Citation/gmd:identifier[1]/gmd:RS_Identifier/gmd:code/gco:CharacterString"/>
        <xsl:variable name="identifierCodeSpace" select="./gmd:identificationInfo[1]//gmd:citation/gmd:CI_Citation/gmd:identifier[1]/gmd:RS_Identifier/gmd:codeSpace/gco:CharacterString"/>

        <!-- REQ 13: code and namespace -->
        <inspire_dls:spatial_dataset_identifier_code><xsl:value-of select="$identifierCode"/></inspire_dls:spatial_dataset_identifier_code>
        <inspire_dls:spatial_dataset_identifier_namespace><xsl:value-of select="$identifierCodeSpace"/></inspire_dls:spatial_dataset_identifier_namespace>

        <!-- REQ 14: describedby -->
        <xsl:call-template name="csw-link">
            <xsl:with-param name="lang" select="$guiLang"/>
            <xsl:with-param name="baseUrl" select="$baseUrl"/>
            <xsl:with-param name="fileIdentifier" select="$fileIdentifier"/>
        </xsl:call-template>

        <!-- REQ 15: alternate: ATOM dataset feed -->
        <xsl:call-template name="atom-link">
            <xsl:with-param name="title">
                <xsl:apply-templates mode="get-translation" select="$datasetTitleNode">
                    <xsl:with-param name="lang" select="$guiLang"/>
                </xsl:apply-templates>
            </xsl:with-param>
            <xsl:with-param name="lang" select="$guiLang"/>
            <xsl:with-param name="baseUrl" select="$baseUrl"/>
            <xsl:with-param name="identifier" select="$identifierCode"/>
            <xsl:with-param name="codeSpace" select="$identifierCodeSpace"/>
            <xsl:with-param name="rel" select="'alternate'"/>
        </xsl:call-template>

        <!-- REQ 17: id -->
        <id>
            <xsl:call-template name="atom-link-href">
                <xsl:with-param name="lang" select="$guiLang"/>
                <xsl:with-param name="baseUrl" select="$baseUrl"/>
                <xsl:with-param name="identifier" select="$identifierCode"/>
                <xsl:with-param name="codeSpace"  select="$identifierCodeSpace"/>
            </xsl:call-template>
        </id>

        <!-- REQ 18: title -->
        <title>
            <xsl:value-of select="$datasetTitle" />
            <!--<xsl:call-template name="translated-description"><xsl:with-param name="lang" select="$guiLang"/><xsl:with-param name="type" select="3"/></xsl:call-template><xsl:text> </xsl:text><xsl:value-of select="$datasetTitle"/>-->
        </title>

        <!-- REQ 19: updated -->
        <!-- TODO: strangely unprecise xpath following ... -->
        <xsl:variable name="updated" select=".//gco:DateTime"/>
        <updated><xsl:value-of select="$updated"/>Z</updated>

        <!-- REC 3: rights -->
        <xsl:variable name="rights">
            <xsl:apply-templates mode="translated-rights" select="gmd:identificationInfo/gmd:MD_DataIdentification"/>
        </xsl:variable>

        <xsl:if test="normalize-space($rights)">
            <rights>
                <xsl:value-of select="$rights"/>
            </rights>
        </xsl:if>

        <!-- REC 4: author -->
        <xsl:call-template name="add-author">
            <xsl:with-param name="pocs" select="gmd:identificationInfo//gmd:pointOfContact"/>
        </xsl:call-template>

        <!-- REC 5: summary -->
        <summary>
            <xsl:apply-templates mode="get-translation" select="gmd:identificationInfo/gmd:MD_DataIdentification/gmd:abstract">
                <xsl:with-param name="lang" select="$guiLang"/>
            </xsl:apply-templates>
        </summary>

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

        <!-- REQ 20: category (CRS) -->
        <xsl:call-template name="add-category">
            <xsl:with-param name="metadata" select="."/>
        </xsl:call-template>


    </xsl:template>

    <!-- === DATASET FEED ========================================================================================== -->

    <xsl:template mode="dataset" match="gmd:MD_Metadata">

        <xsl:variable name="metadata" select="."/>
        <xsl:variable name="fileIdentifier" select="./gmd:fileIdentifier/gco:CharacterString"/>
        <xsl:variable name="docLang" select="./gmd:language/gmd:LanguageCode/@codeListValue"/>
        <xsl:variable name="datasetTitleNode" select="./gmd:identificationInfo[1]//gmd:citation[1]/gmd:CI_Citation/gmd:title"/>
        <xsl:variable name="datasetTitle">
            <xsl:apply-templates mode="get-translation" select="$datasetTitleNode">
                <xsl:with-param name="lang" select="$guiLang"/>
            </xsl:apply-templates>
        </xsl:variable>
        <xsl:variable name="identifierCode" select="./gmd:identificationInfo[1]//gmd:citation/gmd:CI_Citation/gmd:identifier[1]/gmd:MD_Identifier/gmd:code/gco:CharacterString|
                                                    ./gmd:identificationInfo[1]//gmd:citation/gmd:CI_Citation/gmd:identifier[1]/gmd:RS_Identifier/gmd:code/gco:CharacterString"/>
        <xsl:variable name="identifierCodeSpace" select="./gmd:identificationInfo[1]//gmd:citation/gmd:CI_Citation/gmd:identifier[1]/gmd:RS_Identifier/gmd:codeSpace/gco:CharacterString"/>

        <!-- REQ 21: title -->
        <title>
            <xsl:call-template name="translated-description">
                <xsl:with-param name="lang" select="$guiLang"/>
                <xsl:with-param name="type" select="3"/>
            </xsl:call-template>
            <xsl:value-of select="$datasetTitle"/>
        </title>

        <!-- REC 8: subtitle -->
        <subtitle>
            <xsl:call-template name="translated-description">
                <xsl:with-param name="lang" select="$guiLang"/>
                <xsl:with-param name="type" select="3"/>
            </xsl:call-template>
            <xsl:apply-templates mode="get-translation" select="gmd:MD_DataIdentification/gmd:abstract">
                <xsl:with-param name="lang" select="$guiLang"/>
            </xsl:apply-templates>
        </subtitle>

        <!-- REQ 22: id -->
        <id>
            <xsl:call-template name="atom-link-href">
                <xsl:with-param name="lang" select="$guiLang"/>
                <xsl:with-param name="baseUrl" select="$baseUrl"/>
                <xsl:with-param name="identifier" select="$identifierCode"/>
                <xsl:with-param name="codeSpace"  select="$identifierCodeSpace"/>
            </xsl:call-template>
        </id>

        <!-- REQ 23: rights -->
        <rights>
            <xsl:apply-templates mode="translated-rights" select="gmd:identificationInfo/gmd:MD_DataIdentification"/>
        </rights>

        <!-- REQ 24: updated -->
        <xsl:variable name="updated" select="gmd:dateStamp/gco:DateTime" />
        <updated><xsl:value-of select="$updated"/>Z</updated>

        <!-- REQ 25: author -->
        <xsl:call-template name="add-author">
            <xsl:with-param name="pocs" select="gmd:identificationInfo//gmd:pointOfContact"/>
        </xsl:call-template>

        <!-- REC 9: up: link to parent service -->
        <xsl:variable name="serviceIdentifier" select="normalize-space(/root/serviceIdentifier)"/>
        <xsl:if test="$serviceIdentifier">
            <link rel="up" title="{$serviceFeedTitle}" type="application/atom+xml" hreflang="{$guiLang}">
                <xsl:attribute name="href">
                    <xsl:call-template name="atom-link-href">
                        <xsl:with-param name="lang" select="$guiLang"/>
                        <xsl:with-param name="baseUrl" select="$baseUrl"/>
                        <xsl:with-param name="fileIdentifier" select="$serviceIdentifier"/>
                    </xsl:call-template>
                </xsl:attribute>
            </link>
        </xsl:if>

        <!-- REQ 26: download entries -->
        <!-- NVM about REQ 27: an entry for each CRS: we are only using a single CRS for dataset -->
        <xsl:for-each select="./gmd:distributionInfo/gmd:MD_Distribution/gmd:transferOptions/gmd:MD_DigitalTransferOptions/gmd:onLine/gmd:CI_OnlineResource[gmd:function/gmd:CI_OnLineFunctionCode/@codeListValue='download']">
            <entry>

                <!-- REQ 28: describedby -->
                <!-- TODO it should be text/html, not the text/xml ISO document-->
                <xsl:call-template name="csw-link">
                    <xsl:with-param name="lang" select="$guiLang"/>
                    <xsl:with-param name="baseUrl" select="$baseUrl"/>
                    <xsl:with-param name="fileIdentifier" select="$fileIdentifier"/>
                </xsl:call-template>

                <!-- not required -->
                <title>
                    <xsl:value-of select="./gmd:description/gco:CharacterString/text()" />
                </title>

                <xsl:variable name="mimetype">
                    <xsl:call-template name="infer-mimetype">
                        <xsl:with-param name="onlineresource" select="."/>
                    </xsl:call-template>
                </xsl:variable>

                <!-- REQ 29: alternate: link to data -->
                <!--   REQ 30: mimetype -->
                <!--   REQ 31: hreflang -->
                <link title="{./gmd:name/gco:CharacterString/text()}"
                      rel="alternate"
                      type="{$mimetype}"
                      href="{./gmd:linkage/gmd:URL}"
                      hreflang="{$guiLang}">

                    <xsl:variable name="length" select="../../gmd:transferSize/gco:Real" />
                    <xsl:if test="number($length) = number($length)">
                        <xsl:attribute name="length" select="number($length) * 1000000" />
                    </xsl:if>
                </link>

                <!-- not required -->
                <id>
                    <xsl:value-of select="./gmd:linkage/gmd:URL" />
                </id>

                <!-- NVM REQ 32: section: multiple physical files -->
                <!-- NVM REQ 33: content: multiple physical files description -->
                <!-- NVM REC 10: bbox: multiple physical files -->
                <!-- NVM REC 11: time: multiple physical files -->

                <!-- check REQ 34: media types -->

                <!-- REQ 35: category: crs -->
                <xsl:call-template name="add-category">
                    <xsl:with-param name="metadata" select="$metadata"/>
                </xsl:call-template>

            </entry>
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
        <!--		<xsl:variable name="useLimitation" select="normalize-space(gmd:resourceConstraints/gmd:MD_Constraints/gmd:useLimitation/gco:CharacterString)"/>
                <xsl:variable name="translated-useLimitation" select="normalize-space(gmd:resourceConstraints/gmd:MD_Constraints/gmd:useLimitation/gmd:PT_FreeText/gmd:textGroup/gmd:LocalisedCharacterString[@locale=concat('#',upper-case($guiLang))])"/>
                <xsl:choose>
                  <xsl:when test="$translated-useLimitation!=''"><xsl:value-of select="$translated-useLimitation"/></xsl:when>
                  <xsl:otherwise><xsl:value-of select="$useLimitation"/></xsl:otherwise>
                </xsl:choose>
                <xsl:text> </xsl:text>
        -->

        <xsl:for-each select="gmd:resourceConstraints/gmd:MD_LegalConstraints">
            <xsl:variable name="accessConstraints" select="gmd:accessConstraints/gmd:MD_RestrictionCode/@codeListValue"/>
            <xsl:variable name="otherConstraints" select="normalize-space(gmd:otherConstraints/gco:CharacterString)"/>
            <xsl:variable name="translated-otherConstraints" select="normalize-space(gmd:otherConstraints/gmd:PT_FreeText/gmd:textGroup/gmd:LocalisedCharacterString[@locale=concat('#',upper-case($guiLang))])"/>
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
        <xsl:param name="baseUrl"/>
        <xsl:param name="fileIdentifier"/>
        <link rel="describedby" type="application/xml">
            <xsl:attribute name="href" select="concat($baseUrl,'/', $nodeName, '/',$lang,'/csw?service=CSW&amp;version=2.0.2&amp;request=GetRecordById&amp;outputschema=http://www.isotc211.org/2005/gmd&amp;elementSetName=full&amp;id=',$fileIdentifier)"/>
        </link>
    </xsl:template>

    <xsl:template name="atom-link">
        <xsl:param name="lang"/>
        <xsl:param name="baseUrl"/>
        <xsl:param name="fileIdentifier"/>
        <xsl:param name="identifier"/>
        <xsl:param name="codeSpace"/>
        <xsl:param name="title"/>
        <xsl:param name="rel"/>

        <xsl:variable name="type" select="if ($fileIdentifier!='') then 2 else 3"/>

        <link type="application/atom+xml">
            <xsl:if test="$rel != ''">
                <xsl:attribute name="rel" select="$rel"/>
            </xsl:if>
            <xsl:if test="$lang!=$guiLang">
                <xsl:attribute name="hreflang" select="$lang"/>
            </xsl:if>
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
                    <xsl:with-param name="baseUrl" select="$baseUrl"/>
                    <xsl:with-param name="fileIdentifier" select="$fileIdentifier"/>
                    <xsl:with-param name="identifier" select="$identifier"/>
                    <xsl:with-param name="codeSpace" select="$codeSpace"/>
                </xsl:call-template>
            </xsl:attribute>
        </link>
    </xsl:template>

    <xsl:template name="atom-link-href">
        <xsl:param name="lang" />
        <xsl:param name="baseUrl" />
        <xsl:param name="fileIdentifier" />
        <xsl:param name="identifier" />
        <xsl:param name="codeSpace" />
        <xsl:choose>
            <!-- remote ATOM service -->
            <xsl:when test="not($isLocal)">
                <xsl:if test="$fileIdentifier!=''">
                    <xsl:value-of select="concat($baseUrl,'/opensearch/',$lang,'/',$fileIdentifier,'/describe')" />
                </xsl:if>
            </xsl:when>
            <!-- local ATOM service -->
            <xsl:otherwise>
                <xsl:choose>
                    <xsl:when test="$fileIdentifier != ''">
                        <xsl:value-of select="concat($baseUrl, '/', $nodeName, '/', $lang, '/atom.predefined.service?uuid=',$fileIdentifier)" />
                    </xsl:when>
                </xsl:choose>
            </xsl:otherwise>
        </xsl:choose>
        <xsl:if test="$identifier != ''">
            <xsl:variable name="tmpIdentifier" select="if(count($identifier) > 1) then $identifier[1] else $identifier" />
            <xsl:choose>
                <xsl:when test="$codeSpace != ''">
                    <xsl:value-of select="concat($baseUrl,'/', $nodeName, '/', $lang, '/atom.predefined.dataset?spatial_dataset_identifier_code=',$identifier[1],'&amp;spatial_dataset_identifier_namespace=',$codeSpace)" />
                </xsl:when>
                <xsl:otherwise>
                    <xsl:value-of select="concat($baseUrl,'/', $nodeName, '/', $lang,'/atom.predefined.dataset?spatial_dataset_identifier_code=',$identifier)" />
                </xsl:otherwise>
            </xsl:choose>
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
            <xsl:when test="$pocs/gmd:CI_ResponsibleParty/gmd:role/gmd:CI_RoleCode/@codeListValue='author'">
                <xsl:apply-templates mode="author_element" select="$pocs[gmd:CI_ResponsibleParty/gmd:role/gmd:CI_RoleCode/@codeListValue='author']"/>
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
            <xsl:when test="$onlineresource/gmd:name/gmx:MimeFileType/@type">
                <xsl:value-of select="$onlineresource/gmd:name/gmx:MimeFileType/@type"/>
            </xsl:when>
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

    <xsl:template name="add-category">
        <xsl:param name="metadata"/>

        <xsl:variable name="crs" select="$metadata//gmd:referenceSystemInfo/gmd:MD_ReferenceSystem/gmd:referenceSystemIdentifier/gmd:RS_Identifier/gmd:code/gco:CharacterString/text()" />

        <xsl:choose>
            <xsl:when test="$crs">

               <xsl:variable name="crsEpsgCode">
                    <xsl:analyze-string select="$crs" regex="EPSG:\d+" >
                        <xsl:matching-substring>
                            <xsl:value-of select="."/>
                        </xsl:matching-substring>
                    </xsl:analyze-string>
                </xsl:variable>

                <xsl:variable name="epsgCode" select="substring-after($crsEpsgCode, 'EPSG:')" />

                <xsl:choose>
                    <xsl:when test="$epsgCode">
                        <xsl:variable name="crsLabel">
                            <xsl:choose>
                                <xsl:when test="$epsgCode = '2154'">RGF93 / Lambert-93</xsl:when>
                                <xsl:when test="$epsgCode = '32620'">WGS 84 / UTM zone 20N</xsl:when>
                                <xsl:when test="$epsgCode = '2972'">RGFG95 / UTM zone 22N</xsl:when>
                                <xsl:when test="$epsgCode = '2975'">RGR92 / UTM zone 40S</xsl:when>
                                <xsl:when test="$epsgCode = '4467'">RGSPM06 / UTM zone 21N</xsl:when>
                                <xsl:when test="$epsgCode = '4467'">RGM04 / UTM zone 38S</xsl:when>
                                <xsl:otherwise><xsl:value-of select="$crs"/></xsl:otherwise>
                            </xsl:choose>
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


</xsl:stylesheet>