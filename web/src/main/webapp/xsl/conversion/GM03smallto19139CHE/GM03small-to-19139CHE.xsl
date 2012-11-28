<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:che="http://www.geocat.ch/2008/che" xmlns:gco="http://www.isotc211.org/2005/gco" xmlns:gmd="http://www.isotc211.org/2005/gmd" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xmlns:gml="http://www.opengis.net/gml" xmlns:GML="http://www.geocat.ch/2003/05/gateway/GML" xmlns:ch="http://www.geocat.ch/2003/05/gateway/GM03Small" exclude-result-prefixes="ch GML">
    
    <xsl:variable name="defaultLanguage">
        <xsl:apply-templates mode="language" select="/ch:MD_Metadata/ch:identificationInfo/ch:language"/>
    </xsl:variable>
    
    <xsl:template match="ch:MD_Metadata">
        <che:CHE_MD_Metadata gco:isoType="gmd:MD_Metadata">
            <gmd:fileIdentifier>
                <gco:CharacterString>
                    <xsl:value-of select="ch:fileIdentifier"/>
                </gco:CharacterString>
            </gmd:fileIdentifier>
            <gmd:language>
                <gco:CharacterString>
                    <xsl:choose>
                        <xsl:when test="ch:identificationInfo/ch:language='de'">ger</xsl:when>
                        <xsl:when test="ch:identificationInfo/ch:language='fr'">fre</xsl:when>
                        <xsl:when test="ch:identificationInfo/ch:language='it'">ita</xsl:when>
                        <xsl:when test="ch:identificationInfo/ch:language='rm'">roh</xsl:when>
                        <xsl:otherwise>eng</xsl:otherwise>
                    </xsl:choose>
                </gco:CharacterString>
            </gmd:language>
            <gmd:characterSet>
                <gmd:MD_CharacterSetCode codeListValue="utf8" codeList="http://www.isotc211.org/2005/resources/codeList.xml#MD_CharacterSetCode"/>
            </gmd:characterSet>
            <gmd:hierarchyLevel>
                <gmd:MD_ScopeCode codeListValue="dataset" codeList="http://www.isotc211.org/2005/resources/codeList.xml#MD_ScopeCode"/>
            </gmd:hierarchyLevel>
            <gmd:contact></gmd:contact>
            <gmd:dateStamp>
                <gco:DateTime>
                    <xsl:value-of select="ch:dateStamp"/>
                </gco:DateTime>
            </gmd:dateStamp>
            <gmd:metadataStandardName>
                <gco:CharacterString xmlns:srv="http://www.isotc211.org/2005/srv">GM03 2+</gco:CharacterString>
            </gmd:metadataStandardName>
            <gmd:locale>
                <gmd:PT_Locale id="DE">
                    <gmd:languageCode>
                        <gmd:LanguageCode codeListValue="ger" codeList="#LanguageCode">German</gmd:LanguageCode>
                    </gmd:languageCode>
                    <gmd:characterEncoding>
                        <gmd:MD_CharacterSetCode codeListValue="utf8" codeList="#MD_CharacterSetCode">UTF8</gmd:MD_CharacterSetCode>
                    </gmd:characterEncoding>
                </gmd:PT_Locale>
            </gmd:locale>
            <gmd:locale>
                <gmd:PT_Locale id="FR">
                    <gmd:languageCode>
                        <gmd:LanguageCode codeListValue="fre" codeList="#LanguageCode">French</gmd:LanguageCode>
                    </gmd:languageCode>
                    <gmd:characterEncoding>
                        <gmd:MD_CharacterSetCode codeListValue="utf8" codeList="#MD_CharacterSetCode">UTF8</gmd:MD_CharacterSetCode>
                    </gmd:characterEncoding>
                </gmd:PT_Locale>
            </gmd:locale>
            <gmd:locale>
                <gmd:PT_Locale id="IT">
                    <gmd:languageCode>
                        <gmd:LanguageCode codeListValue="ita" codeList="#LanguageCode">Italian</gmd:LanguageCode>
                    </gmd:languageCode>
                    <gmd:characterEncoding>
                        <gmd:MD_CharacterSetCode codeListValue="utf8" codeList="#MD_CharacterSetCode">UTF8</gmd:MD_CharacterSetCode>
                    </gmd:characterEncoding>
                </gmd:PT_Locale>
            </gmd:locale>
            <gmd:locale>
                <gmd:PT_Locale id="EN">
                    <gmd:languageCode>
                        <gmd:LanguageCode codeListValue="eng" codeList="#LanguageCode">English</gmd:LanguageCode>
                    </gmd:languageCode>
                    <gmd:characterEncoding>
                        <gmd:MD_CharacterSetCode codeListValue="utf8" codeList="#MD_CharacterSetCode">UTF8</gmd:MD_CharacterSetCode>
                    </gmd:characterEncoding>
                </gmd:PT_Locale>
            </gmd:locale>
            <gmd:locale>
                <gmd:PT_Locale id="RM">
                    <gmd:languageCode>
                        <gmd:LanguageCode codeListValue="roh" codeList="#LanguageCode">Rumantsch</gmd:LanguageCode>
                    </gmd:languageCode>
                    <gmd:characterEncoding>
                        <gmd:MD_CharacterSetCode codeListValue="utf8" codeList="#MD_CharacterSetCode">UTF8</gmd:MD_CharacterSetCode>
                    </gmd:characterEncoding>
                </gmd:PT_Locale>
            </gmd:locale>
            
            <!-- Add identification section -->
            <xsl:call-template name="identification"/>
            
            <!-- Map metadata URL to an onlineSrc section -->
            <xsl:call-template name="distribution"/>
            
        </che:CHE_MD_Metadata>
    </xsl:template>
    
    <xsl:template name="distribution">
        <gmd:distributionInfo>
            <gmd:MD_Distribution>
                <gmd:transferOptions>
                    <gmd:MD_DigitalTransferOptions>
                        <gmd:onLine>
                            <gmd:CI_OnlineResource>
                                <gmd:linkage xsi:type="che:PT_FreeURL_PropertyType">
                                    <che:LocalisedURL>
                                        <xsl:value-of select="ch:metadataSetURI"/>                                    
                                    </che:LocalisedURL>
                                </gmd:linkage>
                                <gmd:protocol>
                                    <gco:CharacterString>WWW:LINK-1.0-http--link</gco:CharacterString>
                                </gmd:protocol>
                                <gmd:name gco:nilReason="missing">
                                    <gco:CharacterString />
                                </gmd:name>
                                <gmd:description gco:nilReason="missing">
                                    <gco:CharacterString />
                                </gmd:description>
                                <gmd:function>
                                    <gmd:CI_OnLineFunctionCode codeList="http://www.isotc211.org/2005/resources/codeList.xml#CI_OnLineFunctionCode" codeListValue="information" />
                                </gmd:function>
                            </gmd:CI_OnlineResource>
                        </gmd:onLine>                        
                    </gmd:MD_DigitalTransferOptions>
                </gmd:transferOptions>
            </gmd:MD_Distribution>
        </gmd:distributionInfo>
        
    </xsl:template>
    
    
    <xsl:template name="identification">
        
        <gmd:identificationInfo>
            <che:CHE_MD_DataIdentification gco:isoType="gmd:MD_DataIdentification">
                <gmd:citation>
                    <gmd:CI_Citation>
                        <gmd:title xsi:type="gmd:PT_FreeText_PropertyType">
                            <xsl:call-template name="localised">
                                <xsl:with-param name="node" select="ch:identificationInfo/ch:citation/ch:title"/>
                            </xsl:call-template>
                        </gmd:title>
                        <gmd:date>
                            <gmd:CI_Date>
                                <gmd:date>
                                    <gco:Date>
                                        <xsl:value-of select="ch:identificationInfo/ch:citation/ch:date/ch:date"/>
                                    </gco:Date>
                                </gmd:date>
                                <gmd:dateType>
                                    <gmd:CI_DateTypeCode codeListValue="{ch:identificationInfo/ch:citation/ch:date/ch:dateType}" codeList="http://www.isotc211.org/2005/resources/codeList.xml#CI_DateTypeCode"/>
                                </gmd:dateType>
                            </gmd:CI_Date>
                        </gmd:date>
                    </gmd:CI_Citation>
                </gmd:citation>
                <gmd:abstract xsi:type="gmd:PT_FreeText_PropertyType">
                    <xsl:call-template name="localised">
                        <xsl:with-param name="node" select="ch:identificationInfo/ch:abstract"/>
                    </xsl:call-template>
                </gmd:abstract>
                
                
                <!-- contact -->
                <xsl:for-each select="ch:identificationInfo/ch:pointOfContact">
                    <xsl:if test="ch:organisationName!='' or ch:individualName!=''">
                    <gmd:pointOfContact>
                        <che:CHE_CI_ResponsibleParty gco:isoType="gmd:CI_ResponsibleParty">
                            <xsl:if test="ch:organisationName">
                                <gmd:organisationName xsi:type="gmd:PT_FreeText_PropertyType">
                                    <xsl:call-template name="localised">
                                        <xsl:with-param name="node" select="ch:organisationName"/>
                                    </xsl:call-template>
                                </gmd:organisationName>
                            </xsl:if>
                            <xsl:if test="ch:individualName">
                                <che:individualLastName>
                                    <gco:CharacterString><xsl:value-of select="ch:individualName"/></gco:CharacterString>
                                </che:individualLastName>
                            </xsl:if>
                            <gmd:role>
                                <gmd:CI_RoleCode codeList="http://www.isotc211.org/2005/resources/codeList.xml#CI_RoleCode" codeListValue="{ch:role}"/>
                            </gmd:role>
                        </che:CHE_CI_ResponsibleParty>
                    </gmd:pointOfContact>
                    </xsl:if>
                </xsl:for-each>
                
                
                <xsl:for-each select="ch:identificationInfo/ch:descriptiveKeywords">
                    <gmd:descriptiveKeywords>
                        <gmd:MD_Keywords xmlns:skos="http://www.w3.org/2004/02/skos/core#">
                            <gmd:keyword xsi:type="gmd:PT_FreeText_PropertyType">
                                <xsl:call-template name="localised">
                                    <xsl:with-param name="node" select="ch:keyword"/>
                                </xsl:call-template>
                            </gmd:keyword>
                            <gmd:type>
                                <gmd:MD_KeywordTypeCode codeListValue="theme" codeList="http://www.isotc211.org/2005/resources/codeList.xml#MD_KeywordTypeCode"/>
                            </gmd:type>
                        </gmd:MD_Keywords>
                    </gmd:descriptiveKeywords>
                </xsl:for-each>
                
                <gmd:language>
                    <gco:CharacterString>
                        <xsl:choose>
                            <xsl:when test="ch:identificationInfo/ch:language='de'">ger</xsl:when>
                            <xsl:when test="ch:identificationInfo/ch:language='fr'">fre</xsl:when>
                            <xsl:when test="ch:identificationInfo/ch:language='it'">ita</xsl:when>
                            <xsl:when test="ch:identificationInfo/ch:language='rm'">roh</xsl:when>
                            <xsl:otherwise>eng</xsl:otherwise>
                        </xsl:choose>
                    </gco:CharacterString>
                </gmd:language>                
                
                <xsl:for-each select="ch:identificationInfo/ch:topicCategory">
                    <gmd:topicCategory>
                        <gmd:MD_TopicCategoryCode><xsl:value-of select="."/></gmd:MD_TopicCategoryCode>
                    </gmd:topicCategory>
                </xsl:for-each>                
                
                
                <xsl:for-each select="ch:identificationInfo/ch:extent">
                    <gmd:extent>
                        <gmd:EX_Extent>
                            <gmd:description xsi:type="gmd:PT_FreeText_PropertyType">
                                <xsl:call-template name="localised">
                                    <xsl:with-param name="node" select="ch:description"/>
                                </xsl:call-template>
                            </gmd:description>
                            <xsl:for-each select="ch:geographicElement">
                                <gmd:geographicElement>
                                    <gmd:EX_GeographicBoundingBox>
                                        <gmd:westBoundLongitude>
                                            <gco:Decimal xmlns:gco="http://www.isotc211.org/2005/gco">
                                                <xsl:value-of select="ch:westBoundLongitude"/>
                                            </gco:Decimal>
                                        </gmd:westBoundLongitude>
                                        <gmd:eastBoundLongitude>
                                            <gco:Decimal xmlns:gco="http://www.isotc211.org/2005/gco">
                                                <xsl:value-of select="ch:eastBoundLongitude"/>
                                            </gco:Decimal>
                                        </gmd:eastBoundLongitude>
                                        <gmd:southBoundLatitude>
                                            <gco:Decimal xmlns:gco="http://www.isotc211.org/2005/gco">
                                                <xsl:value-of select="ch:southBoundLatitude"/>
                                            </gco:Decimal>
                                        </gmd:southBoundLatitude>
                                        <gmd:northBoundLatitude>
                                            <gco:Decimal xmlns:gco="http://www.isotc211.org/2005/gco">
                                                <xsl:value-of select="ch:northBoundLatitude"/>
                                            </gco:Decimal>
                                        </gmd:northBoundLatitude>
                                    </gmd:EX_GeographicBoundingBox>
                                </gmd:geographicElement>
                            </xsl:for-each>
                        </gmd:EX_Extent>
                    </gmd:extent>
                </xsl:for-each>
            </che:CHE_MD_DataIdentification>
        </gmd:identificationInfo>
    </xsl:template>
    
    <xsl:template name="localised">
        <xsl:param name="node"/>
        
        <xsl:for-each select="$node/ch:textGroup[ch:language=$defaultLanguage]">
            <gco:CharacterString><xsl:value-of select="ch:plainText"/></gco:CharacterString>
        </xsl:for-each>
        <xsl:for-each select="$node/ch:textGroup[ch:language!=$defaultLanguage]">
            <gmd:PT_FreeText>
                <gmd:textGroup>
                    <gmd:LocalisedCharacterString>
                        <xsl:attribute name="locale">
                            <xsl:choose>
                                <xsl:when test="ch:language='de'">#DE</xsl:when>
                                <xsl:when test="ch:language='fr'">#FR</xsl:when>
                                <xsl:when test="ch:language='it'">#IT</xsl:when>
                                <xsl:otherwise>#EN</xsl:otherwise>
                            </xsl:choose>
                        </xsl:attribute>
                        <xsl:value-of select="ch:plainText"/>
                    </gmd:LocalisedCharacterString>
                </gmd:textGroup>
            </gmd:PT_FreeText>
        </xsl:for-each>
    </xsl:template>
    
</xsl:stylesheet>