<?xml version="1.0" encoding="UTF-8" ?>

<xsl:stylesheet version="1.0" xmlns:gmd="http://www.isotc211.org/2005/gmd"
    xmlns:gco="http://www.isotc211.org/2005/gco"
    xmlns:gml="http://www.opengis.net/gml"
    xmlns:srv="http://www.isotc211.org/2005/srv"
    xmlns:util="java:org.fao.geonet.util.XslUtil"
    xmlns:java="java:org.fao.geonet.util.XslUtil"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    >

    <!--This file defines what parts of the metadata are indexed by Lucene
        Searches can be conducted on indexes defined here.
        The Field@name attribute defines the name of the search variable.
        If a variable has to be maintained in the user session, it needs to be
        added to the GeoNetwork constants in the Java source code.
        Please keep indexes consistent among metadata standards if they should
        work accross different metadata resources -->
    <!-- ========================================================================================= -->

    <xsl:output method="xml" version="1.0" encoding="UTF-8" indent="no" />
    <xsl:include href="../convert/functions.xsl"/>

    <!-- ========================================================================================= -->
    <xsl:variable name="isoDocLangId">
        <xsl:call-template name="langId19139"/>
    </xsl:variable>

    <xsl:template match="/">

        <Documents>
            <xsl:for-each select="/*[name(.)='gmd:MD_Metadata' or @gco:isoType='gmd:MD_Metadata']/gmd:locale/gmd:PT_Locale">
                <xsl:call-template name="document">
                    <xsl:with-param name="isoLangId" select="java:threeCharLangCode(normalize-space(string(gmd:languageCode/gmd:LanguageCode/@codeListValue)))"/>
                    <xsl:with-param name="langId" select="@id"></xsl:with-param>
                </xsl:call-template>
            </xsl:for-each>
            <!--
                    Create a language document only if PT_Locale defined (ie. is a multilingual document)
                    and gmd:locale contains the main metadata language. -->
            <xsl:if test="/*[name(.)='gmd:MD_Metadata' or @gco:isoType='gmd:MD_Metadata']/gmd:locale/gmd:PT_Locale
           					and count(/*[name(.)='gmd:MD_Metadata' or @gco:isoType='gmd:MD_Metadata']/
           						gmd:locale/gmd:PT_Locale/gmd:languageCode/gmd:LanguageCode[@codeListValue = $isoDocLangId]) = 0">
                <xsl:call-template name="document">
                    <xsl:with-param name="isoLangId" select="$isoDocLangId"></xsl:with-param>
                    <xsl:with-param name="langId" select="java:twoCharLangCode(normalize-space(string($isoDocLangId)))"></xsl:with-param>
                </xsl:call-template>
            </xsl:if>
        </Documents>
    </xsl:template>

    <!-- ========================================================================================= -->
    <xsl:template name="document">
        <xsl:param name="isoLangId"/>
        <xsl:param name="langId"/>

        <Document locale="{$isoLangId}">
            <Field name="_locale" string="{$isoLangId}" store="true" index="true"/>
            <Field name="_docLocale" string="{$isoDocLangId}" store="true" index="true"/>

            <xsl:variable name="poundLangId" select="concat('#',$langId)" />
            <xsl:variable name="_defaultTitle">
                <xsl:call-template name="defaultTitle">
                    <xsl:with-param name="isoDocLangId" select="$isoLangId"/>
                </xsl:call-template>
            </xsl:variable>
            <xsl:if test="$isoLangId!=$isoDocLangId">
                <!-- not tokenized title for sorting -->
                <Field name="_defaultTitle" string="{string($_defaultTitle)}" store="true" index="true" />
            </xsl:if>
            <xsl:variable name="title"
                select="/*[name(.)='gmd:MD_Metadata' or @gco:isoType='gmd:MD_Metadata']/gmd:identificationInfo//gmd:citation//gmd:title//gmd:LocalisedCharacterString[@locale=$poundLangId]"/>

            <!-- not tokenized title for sorting -->
            <xsl:choose>
                <xsl:when test="normalize-space($title) = ''">
                    <Field name="_title" string="{string($_defaultTitle)}" store="true" index="true" />
                </xsl:when>
                <xsl:otherwise>
                    <Field name="_title" string="{string($title)}" store="true" index="true" />
                </xsl:otherwise>
            </xsl:choose>

            <xsl:apply-templates select="/*[name(.)='gmd:MD_Metadata' or @gco:isoType='gmd:MD_Metadata']" mode="metadata">
                <xsl:with-param name="langId" select="$poundLangId"/>
                <xsl:with-param name="isoLangId" select="$isoLangId"/>
            </xsl:apply-templates>

            <xsl:apply-templates mode="index" select="*[name(.)='gmd:MD_Metadata' or @gco:isoType='gmd:MD_Metadata']">
                <xsl:with-param name="langId" select="$poundLangId"/>
            </xsl:apply-templates>
        </Document>
    </xsl:template>

    <xsl:template mode="index" match="*|@*">
        <xsl:param name="langId" />

        <xsl:apply-templates mode="index" select="*|@*">
            <xsl:with-param name="langId" select="$langId"/>
        </xsl:apply-templates>
    </xsl:template>

    <!-- ========================================================================================= -->

    <xsl:template match="*" mode="metadata">
        <xsl:param name="langId" />
        <xsl:param name="isoLangId"/>

        <!-- === Data or Service Identification === -->

        <!-- the double // here seems needed to index MD_DataIdentification when
            it is nested in a SV_ServiceIdentification class -->

        <xsl:for-each select="gmd:identificationInfo/*">
        


            <xsl:for-each select="gmd:citation/gmd:CI_Citation">

                <xsl:for-each select="gmd:identifier/gmd:MD_Identifier/gmd:code//gmd:LocalisedCharacterString[@locale=$langId]">
                    <Field name="identifier" string="{string(.)}" store="true" index="true"/>
                </xsl:for-each>

                <!-- not tokenized title for sorting -->
                <Field name="_defaultTitle" string="{string(gmd:title/gco:CharacterString)}" store="true" index="true"/>
                <!-- not tokenized title for sorting -->
                <Field name="_title" string="{string(gmd:title//gmd:LocalisedCharacterString[@locale=$langId])}" store="true" index="true"/>

                <xsl:for-each select="gmd:title//gmd:LocalisedCharacterString[@locale=$langId]">
                    <Field name="title" string="{string(.)}" store="true" index="true"/>
                </xsl:for-each>

                <xsl:for-each select="gmd:alternateTitle//gmd:LocalisedCharacterString[@locale=$langId]">
                    <Field name="altTitle" string="{string(.)}" store="true" index="true"/>
                </xsl:for-each>

                <xsl:for-each select="gmd:date/gmd:CI_Date[gmd:dateType/gmd:CI_DateTypeCode/@codeListValue='revision']/gmd:date/gco:Date">
                    <Field name="revisionDate" string="{string(.)}" store="true" index="true"/>
                </xsl:for-each>

                <xsl:for-each select="gmd:date/gmd:CI_Date[gmd:dateType/gmd:CI_DateTypeCode/@codeListValue='creation']/gmd:date/gco:Date">
                    <Field name="createDate" string="{string(.)}" store="true" index="true"/>
                </xsl:for-each>

                <xsl:for-each select="gmd:date/gmd:CI_Date[gmd:dateType/gmd:CI_DateTypeCode/@codeListValue='publication']/gmd:date/gco:Date">
                    <Field name="publicationDate" string="{string(.)}" store="true" index="true"/>
                </xsl:for-each>

                <!-- fields used to search for metadata in paper or digital format -->

                <xsl:for-each select="gmd:presentationForm">
                    <xsl:if test="contains(gmd:CI_PresentationFormCode/@codeListValue, 'Digital')">
                        <Field name="digital" string="true" store="true" index="true"/>
                    </xsl:if>

                    <xsl:if test="contains(gmd:CI_PresentationFormCode/@codeListValue, 'Hardcopy')">
                        <Field name="paper" string="true" store="true" index="true"/>
                    </xsl:if>
                </xsl:for-each>
            </xsl:for-each>

            <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

            <xsl:for-each select="gmd:abstract//gmd:LocalisedCharacterString[@locale=$langId]">
                <Field name="abstract" string="{string(.)}" store="true" index="true"/>
            </xsl:for-each>
            <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
            <xsl:for-each select="gmd:status/gmd:MD_ProgressCode/@codeListValue[. != '']">
                 <Field name="status_text" 
                    string="{util:getCodelistTranslation('gmd:MD_ProgressCode', string(.), string($isoLangId))}" 
                    store="true" index="true"/>          
            </xsl:for-each>
            <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
            
            <xsl:for-each select="*/gmd:EX_Extent">
                <xsl:apply-templates select="gmd:geographicElement/gmd:EX_GeographicBoundingBox" mode="latLon"/>

                <xsl:for-each select="gmd:geographicElement/gmd:EX_GeographicDescription/gmd:geographicIdentifier/gmd:MD_Identifier/gmd:code//gmd:LocalisedCharacterString[@locale=$langId]">
                    <Field name="geoDescCode" string="{string(.)}" store="true" index="true"/>
                </xsl:for-each>

                <xsl:for-each select="gmd:description//gmd:LocalisedCharacterString[@locale=$langId]">
                    <Field name="extentDesc" string="{string(.)}" store="true" index="true"/>
                </xsl:for-each>

                <xsl:for-each select="gmd:temporalElement/gmd:EX_TemporalExtent/gmd:extent|
					gmd:temporalElement/gmd:EX_SpatialTemporalExtent/gmd:extent">
                    <xsl:for-each select="gml:TimePeriod/gml:beginPosition">
                        <Field name="tempExtentBegin" string="{string(.)}" store="true" index="true"/>
                    </xsl:for-each>

                    <xsl:for-each select="gml:TimePeriod/gml:endPosition">
                        <Field name="tempExtentEnd" string="{string(.)}" store="true" index="true"/>
                    </xsl:for-each>

                    <xsl:for-each select="gml:TimePeriod/gml:begin/gml:TimeInstant/gml:timePosition">
                        <Field name="tempExtentBegin" string="{string(.)}" store="true" index="true"/>
                    </xsl:for-each>

                    <xsl:for-each select="gml:TimePeriod/gml:end/gml:TimeInstant/gml:timePosition">
                        <Field name="tempExtentEnd" string="{string(.)}" store="true" index="true"/>
                    </xsl:for-each>

                    <xsl:for-each select="gml:TimeInstant/gml:timePosition">
                        <Field name="tempExtentBegin" string="{string(.)}" store="true" index="true"/>
                        <Field name="tempExtentEnd" string="{string(.)}" store="true" index="true"/>
                    </xsl:for-each>

                </xsl:for-each>
            </xsl:for-each>

            <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

            <xsl:for-each select="*/gmd:MD_Keywords">
                <xsl:for-each select="gmd:keyword//gmd:LocalisedCharacterString[@locale=$langId]">
                    <Field name="keyword" string="{string(.)}" store="true" index="true"/>
                </xsl:for-each>

                <xsl:for-each select="gmd:type/gmd:MD_KeywordTypeCode/@codeListValue">
                    <Field name="keywordType" string="{string(.)}" store="true" index="true"/>
                </xsl:for-each>
            </xsl:for-each>

            <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

            <xsl:for-each select="gmd:pointOfContact/gmd:CI_ResponsibleParty/gmd:organisationName//gmd:LocalisedCharacterString[@locale=$langId]">
                <Field name="orgName" string="{string(.)}" store="true" index="true"/>
                <Field name="_orgName" string="{string(.)}" store="true" index="true"/>
            </xsl:for-each>
            <xsl:for-each select="gmd:pointOfContact/gmd:CI_ResponsibleParty/gmd:individualName/gco:CharacterString|
				gmd:pointOfContact/gmd:CI_ResponsibleParty/gmd:individualFirstName/gco:CharacterString|
				gmd:pointOfContact/gmd:CI_ResponsibleParty/gmd:individualLastName/gco:CharacterString">
                <Field name="creator" string="{string(.)}" store="true" index="true"/>
            </xsl:for-each>

            <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

            <xsl:choose>
                <xsl:when test="gmd:resourceConstraints/gmd:MD_SecurityConstraints">
                    <Field name="secConstr" string="true" store="true" index="true"/>
                </xsl:when>
                <xsl:otherwise>
                    <Field name="secConstr" string="false" store="true" index="true"/>
                </xsl:otherwise>
            </xsl:choose>

            <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

            <xsl:for-each select="gmd:topicCategory/gmd:MD_TopicCategoryCode">
                <Field name="topicCat" string="{string(.)}" store="true" index="true"/>
                <Field name="keyword"
                    string="{java:getCodelistTranslation('gmd:MD_TopicCategoryCode', string(.), string($isoLangId))}"
                    store="true"
                    index="true"/>
            </xsl:for-each>

            <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

            <xsl:for-each select="gmd:language/gco:CharacterString">
                <Field name="datasetLang" string="{string(.)}" store="true" index="true"/>
            </xsl:for-each>

            <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

            <xsl:for-each select="gmd:spatialRepresentationType/gmd:MD_SpatialRepresentationTypeCode/@codeListValue">
                <Field name="spatialRepresentationType" string="{string(.)}" store="true" index="true"/>
            </xsl:for-each>

            <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

            <xsl:for-each select="gmd:spatialResolution/gmd:MD_Resolution">
                <xsl:for-each select="gmd:equivalentScale/gmd:MD_RepresentativeFraction/gmd:denominator/gco:Integer">
                    <Field name="denominator" string="{string(.)}" store="true" index="true"/>
                </xsl:for-each>

                <xsl:for-each select="gmd:distance/gco:Distance">
                    <Field name="distanceVal" string="{string(.)}" store="true" index="true"/>
                </xsl:for-each>

                <xsl:for-each select="gmd:distance/gco:Distance/@uom">
                    <Field name="distanceUom" string="{string(.)}" store="true" index="true"/>
                </xsl:for-each>
            </xsl:for-each>

            <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

            <xsl:for-each select="gmd:graphicOverview/gmd:MD_BrowseGraphic">
                <xsl:variable name="fileName"  select="gmd:fileName/gco:CharacterString"/>
                <xsl:if test="$fileName != ''">
                    <xsl:variable name="fileDescr" select="gmd:fileDescription/gco:CharacterString"/>
                    <xsl:choose>
                        <xsl:when test="contains($fileName ,'://')">
                            <xsl:choose>
                                <xsl:when test="string($fileDescr)='thumbnail'">
                                    <Field  name="image" string="{concat('thumbnail|', $fileName)}" store="true" index="false"/>
                                </xsl:when>
                                <xsl:when test="string($fileDescr)='large_thumbnail'">
                                    <Field  name="image" string="{concat('overview|', $fileName)}" store="true" index="false"/>
                                </xsl:when>
                                <xsl:otherwise>
                                    <Field  name="image" string="{concat('unknown|', $fileName)}" store="true" index="false"/>
                                </xsl:otherwise>
                            </xsl:choose>
                        </xsl:when>
                        <xsl:when test="string($fileDescr)='thumbnail'">
                            <!-- FIXME : relative path -->
                            <Field  name="image" string="{concat($fileDescr, '|', '../../srv/eng/resources.get?uuid=', //gmd:fileIdentifier/gco:CharacterString, '&amp;fname=', $fileName, '&amp;access=public')}" store="true" index="false"/>
                        </xsl:when>
                        <xsl:when test="string($fileDescr)='large_thumbnail'">
                            <!-- FIXME : relative path -->
                            <Field  name="image" string="{concat('overview', '|', '../../srv/eng/resources.get?uuid=', //gmd:fileIdentifier/gco:CharacterString, '&amp;fname=', $fileName, '&amp;access=public')}" store="true" index="false"/>
                        </xsl:when>
                    </xsl:choose>
                </xsl:if>
            </xsl:for-each>

            <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
            <!--  Fields use to search on Service -->

            <xsl:for-each select="srv:serviceType/gco:LocalName">
                <Field  name="serviceType" string="{string(.)}" store="true" index="true"/>
                <Field  name="type" string="service-{string(.)}" store="true" index="true"/>
            </xsl:for-each>

            <xsl:for-each select="srv:serviceTypeVersion/gco:CharacterString">
                <Field  name="serviceTypeVersion" string="{string(.)}" store="true" index="true"/>
            </xsl:for-each>

            <xsl:for-each select="//srv:SV_OperationMetadata/srv:operationName/gco:CharacterString">
                <Field  name="operation" string="{string(.)}" store="true" index="true"/>
            </xsl:for-each>

            <xsl:for-each select="srv:operatesOn/@uuidref">
                <Field  name="operatesOn" string="{string(.)}" store="true" index="true"/>
            </xsl:for-each>

            <xsl:for-each select="srv:coupledResource">
                <xsl:for-each select="srv:SV_CoupledResource/srv:identifier/gco:CharacterString">
                    <Field  name="operatesOnIdentifier" string="{string(.)}" store="true" index="true"/>
                </xsl:for-each>

                <xsl:for-each select="srv:SV_CoupledResource/srv:operationName/gco:CharacterString">
                    <Field  name="operatesOnName" string="{string(.)}" store="true" index="true"/>
                </xsl:for-each>
            </xsl:for-each>

            <xsl:for-each select="//srv:SV_CouplingType/srv:code/@codeListValue">
                <Field  name="couplingType" string="{string(.)}" store="true" index="true"/>
            </xsl:for-each>

        </xsl:for-each>

        <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
        <!-- === Distribution === -->

        <xsl:for-each select="gmd:distributionInfo/gmd:MD_Distribution">
            <xsl:for-each select="gmd:distributionFormat/gmd:MD_Format/gmd:name//gmd:LocalisedCharacterString[@locale=$langId]">
                <Field name="format" string="{string(.)}" store="true" index="true"/>
            </xsl:for-each>

            <!-- index online protocol -->

            <xsl:for-each select="gmd:transferOptions/gmd:MD_DigitalTransferOptions/gmd:onLine/gmd:CI_OnlineResource/gmd:protocol//gmd:LocalisedCharacterString[@locale=$langId]">
                <Field name="protocol" string="{string(.)}" store="true" index="true"/>
            </xsl:for-each>
        </xsl:for-each>


        <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
        <!-- === Service stuff ===  -->
        <!-- Service type           -->
        <xsl:for-each select="gmd:identificationInfo/srv:SV_ServiceIdentification/srv:serviceType/gco:LocalName|
			gmd:identificationInfo/*[contains(@gco:isoType, 'SV_ServiceIdentification')]/srv:serviceType/gco:LocalName">
            <Field name="serviceType" string="{string(.)}" store="true" index="true"/>
        </xsl:for-each>

        <!-- Service version        -->
        <xsl:for-each select="gmd:identificationInfo/srv:SV_ServiceIdentification/srv:serviceTypeVersion/gco:CharacterString|
			gmd:identificationInfo/*[contains(@gco:isoType, 'SV_ServiceIdentification')]/srv:serviceTypeVersion/gco:CharacterString">
            <Field name="serviceTypeVersion" string="{string(.)}" store="true" index="true"/>
        </xsl:for-each>


        <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
        <!-- === General stuff === -->

        <xsl:choose>
            <xsl:when test="gmd:hierarchyLevel">
                <xsl:for-each select="gmd:hierarchyLevel/gmd:MD_ScopeCode/@codeListValue">
                    <Field name="type" string="{string(.)}" store="true" index="true"/>
                </xsl:for-each>
            </xsl:when>
            <xsl:otherwise>
                <Field name="type" string="dataset" store="true" index="true"/>
            </xsl:otherwise>
        </xsl:choose>

        <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

        <xsl:for-each select="gmd:hierarchyLevelName//gmd:LocalisedCharacterString[@locale=$langId]">
            <Field name="levelName" string="{string(.)}" store="true" index="true"/>
        </xsl:for-each>

        <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

        <xsl:for-each select="gmd:fileIdentifier/gco:CharacterString">
            <Field name="fileId" string="{string(.)}" store="true" index="true"/>
        </xsl:for-each>

        <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

        <xsl:for-each select="gmd:parentIdentifier/gco:CharacterString">
            <Field name="parentUuid" string="{string(.)}" store="true" index="true"/>
        </xsl:for-each>

        <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
        <!-- === Reference system info === -->

        <xsl:for-each select="gmd:referenceSystemInfo/gmd:MD_ReferenceSystem">
            <xsl:for-each select="gmd:referenceSystemIdentifier/gmd:RS_Identifier">
                <xsl:variable name="crs" select="concat(string(gmd:codeSpace/gco:CharacterString),'::',string(gmd:code/gco:CharacterString))"/>

                <xsl:if test="$crs != '::'">
                    <Field name="crs" string="{$crs}" store="true" index="true"/>
                </xsl:if>
            </xsl:for-each>
        </xsl:for-each>

        <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
        <!-- === Free text search === -->
        <Field name="any" store="false" index="true">
            <xsl:attribute name="string">
                <xsl:value-of select="normalize-space(//node()[@locale=$langId])"/>
                <xsl:text> </xsl:text>
                <xsl:for-each select="//@codeListValue">
                    <xsl:value-of select="concat(., ' ')"/>
                </xsl:for-each>
            </xsl:attribute>
        </Field>

        <xsl:apply-templates select="." mode="codeList"/>

    </xsl:template>

    <!-- ========================================================================================= -->
    <!-- codelist element, indexed, not stored nor tokenized -->

    <xsl:template match="*[./*/@codeListValue]" mode="codeList">
        <xsl:param name="name" select="name(.)"/>

        <Field name="{$name}" string="{*/@codeListValue}" store="false" index="true"/>
    </xsl:template>

    <!-- ========================================================================================= -->

    <xsl:template match="*" mode="codeList">
        <xsl:apply-templates select="*" mode="codeList"/>
    </xsl:template>

</xsl:stylesheet>
