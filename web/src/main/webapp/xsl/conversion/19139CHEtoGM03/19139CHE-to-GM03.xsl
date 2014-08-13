<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
                xmlns="http://www.interlis.ch/INTERLIS2.3"
                xmlns:che="http://www.geocat.ch/2008/che"
                xmlns:gco="http://www.isotc211.org/2005/gco"
                xmlns:gmd="http://www.isotc211.org/2005/gmd"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                xmlns:util="xalan://org.fao.geonet.util.XslUtil"
                exclude-result-prefixes="che gco gmd util">
    <xsl:include href="header.xsl"/>
    <xsl:include href="metadata.xsl"/>
    <xsl:include href="distribution.xsl"/>
    <xsl:include href="resp-party.xsl"/>
    <xsl:include href="ref-system.xsl"/>
    <xsl:include href="identification.xsl"/>
    <xsl:include href="extent.xsl"/>
    <xsl:include href="data-quality.xsl"/>
    <xsl:include href="spatial_repr.xsl"/>
    <xsl:include href="content.xsl"/>
    <xsl:include href="maintenance-info.xsl"/>
    <xsl:include href="legislation.xsl"/>

    <xsl:template match="/">
        <TRANSFER>
            <xsl:call-template name="header"/>
            <DATASECTION>
                <GM03_2_1Comprehensive.Comprehensive BID="x{generate-id(.)}">
                    <xsl:apply-templates mode="metadata" select="*"/>
                    
                    <xsl:apply-templates mode="root" select="//gmd:distributor/gmd:MD_Distributor/gmd:distributorFormat"/>
                </GM03_2_1Comprehensive.Comprehensive>
            </DATASECTION>
        </TRANSFER>
    </xsl:template>
    <xsl:template mode="root" match="gmd:distributorFormat">
        <GM03_2_1Comprehensive.Comprehensive.formatDistributordistributorFormat TID="x{util:randomId()}">
            <xsl:apply-templates mode="root" select="gmd:MD_Format/gmd:formatDistributor[1]"/>
            <xsl:apply-templates mode="root" select="gmd:MD_Format">
                <xsl:with-param name="showDistributor" select="false()"/>
            </xsl:apply-templates>
        </GM03_2_1Comprehensive.Comprehensive.formatDistributordistributorFormat>
    </xsl:template>
    <xsl:template mode="root" match="gmd:formatDistributor">
        <formatDistributor REF="?">
            <xsl:apply-templates mode="distribution"/>    
        </formatDistributor>
    </xsl:template>
    <xsl:template mode="root" match="gmd:MD_Format">
            <distributorFormat REF="?">
                <xsl:apply-templates mode="distribution" select="."/>
            </distributorFormat>
    </xsl:template>

    <xsl:template mode="language" match="*">
        <ERROR>Unknown metadata element <xsl:value-of select="local-name(.)"/></ERROR>
    </xsl:template>

    <xsl:template name="lang3_to_lang2">
        <xsl:param name="lang3"/>
        <xsl:choose>
            <xsl:when test="string-length($lang3)=2"><xsl:value-of select="$lang3"/></xsl:when>
            <xsl:when test="$lang3='deu'">de</xsl:when>
            <xsl:when test="$lang3='ger'">de</xsl:when>
            <xsl:when test="$lang3='fra'">fr</xsl:when>
            <xsl:when test="$lang3='fre'">fr</xsl:when>
            <xsl:when test="$lang3='ita'">it</xsl:when>
            <xsl:when test="$lang3='eng'">en</xsl:when>
            <xsl:when test="$lang3='rhe'">rm</xsl:when>
            <xsl:when test="$lang3='roh'">rm</xsl:when>
            <xsl:when test="$lang3='aar'">aa</xsl:when>
            <xsl:when test="$lang3=''"></xsl:when>
            <xsl:otherwise>ERROR_<xsl:value-of select="$lang3"/></xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:variable name="defaultLanguage">
        <xsl:choose>
            <xsl:when test="/che:CHE_MD_Metadata/gmd:language/gco:CharacterString|/gmd:MD_Metadata/gmd:language/gco:CharacterString">
                <xsl:for-each select="/che:CHE_MD_Metadata/gmd:language/gco:CharacterString|/gmd:MD_Metadata/gmd:language/gco:CharacterString">
                    <xsl:call-template name="lang3_to_lang2">
                        <xsl:with-param name="lang3" select="."/>
                    </xsl:call-template>
                </xsl:for-each>
            </xsl:when>
            <xsl:otherwise>de</xsl:otherwise>
        </xsl:choose>
    </xsl:variable>

    <xsl:template mode="groupText" match="*">
        <xsl:param name="element"/>
        <xsl:if test="*[local-name()=$element]">
            <xsl:element name="{$element}">
                <xsl:for-each select="*[local-name()=$element]">
                    <GM03_2_1Core.Core.PT_FreeText>
                        <textGroup>
                        
                            <xsl:if test="normalize-space(.)=''">
                            <GM03_2_1Core.Core.PT_Group>
                                    <language><xsl:value-of select="$defaultLanguage"/></language>
                                    <plainText></plainText>
                                </GM03_2_1Core.Core.PT_Group>
                            </xsl:if>
                            <xsl:if test="normalize-space(gmd:PT_FreeText/gmd:textGroup/gmd:LocalisedCharacterString[@locale = $defaultLanguage]) != ''">
                                <xsl:for-each select="gco:CharacterString[normalize-space(.) != '']">
                                    <GM03_2_1Core.Core.PT_Group>
                                        <language><xsl:value-of select="$defaultLanguage"/></language>
                                        <plainText><xsl:value-of select="."/></plainText>
                                    </GM03_2_1Core.Core.PT_Group>
                                </xsl:for-each>
                            </xsl:if>
                            <xsl:for-each select="gmd:PT_FreeText/gmd:textGroup/gmd:LocalisedCharacterString[normalize-space(text())!='']">
                                <GM03_2_1Core.Core.PT_Group>
                                    <xsl:apply-templates mode="text" select="@locale"/>
                                    <plainText><xsl:value-of select="."/></plainText>
                                </GM03_2_1Core.Core.PT_Group>
                            </xsl:for-each>
                        </textGroup>
                    </GM03_2_1Core.Core.PT_FreeText>
                </xsl:for-each>
            </xsl:element>
        </xsl:if>
    </xsl:template>

    <xsl:template mode="textGroup" match="*" priority="2">
        <xsl:element name="{local-name(.)}">
            <GM03_2_1Core.Core.PT_FreeText>
                <textGroup>
                    <xsl:if test="normalize-space(.)=''">
                    <GM03_2_1Core.Core.PT_Group>
                            <language><xsl:value-of select="$defaultLanguage"/></language>
                            <plainText></plainText>
                        </GM03_2_1Core.Core.PT_Group>
                    </xsl:if>
                    <xsl:for-each select="gco:CharacterString[normalize-space(.) != ''
                    and not(../gmd:PT_FreeText/gmd:textGroup/gmd:LocalisedCharacterString[lower-case(@locale) = concat('#',$defaultLanguage)]) and normalize-space(.) != ''] ">
                        <GM03_2_1Core.Core.PT_Group>
                            <language><xsl:value-of select="$defaultLanguage"/></language>
                            <plainText><xsl:value-of select="."/></plainText>
                        </GM03_2_1Core.Core.PT_Group>
                    </xsl:for-each>
                    <xsl:for-each select="gmd:PT_FreeText/gmd:textGroup/gmd:LocalisedCharacterString[normalize-space(text())!='']">
                        <GM03_2_1Core.Core.PT_Group>
                            <xsl:apply-templates mode="text" select="@locale"/>
                            <plainText><xsl:value-of select="."/></plainText>
                        </GM03_2_1Core.Core.PT_Group>
                    </xsl:for-each>
                </textGroup>
            </GM03_2_1Core.Core.PT_FreeText>
        </xsl:element>
    </xsl:template>
    
    <xsl:template mode="text_" match="*">
        <xsl:element name="{local-name(.)}">
          <GM03_2_1Core.Core.CharacterString_>
            <value><xsl:value-of select="."/></value>
          </GM03_2_1Core.Core.CharacterString_>
        </xsl:element>
    </xsl:template>
    <xsl:template mode="text" match="*[@xsi:type='gmd:PT_FreeText_PropertyType']" priority="2">
    
        <xsl:if test="normalize-space(.) != ''">
            <xsl:element name="{local-name(.)}">
                <GM03_2_1Core.Core.PT_FreeText>
                    <textGroup>
                        <xsl:if test="normalize-space(.)=''">
                        <GM03_2_1Core.Core.PT_Group>
                                <language><xsl:value-of select="$defaultLanguage"/></language>
                                <plainText></plainText>
                            </GM03_2_1Core.Core.PT_Group>
                        </xsl:if>
                        <xsl:for-each select="gco:CharacterString[normalize-space(.) != ''
                    and not(../gmd:PT_FreeText/gmd:textGroup/gmd:LocalisedCharacterString[lower-case(@locale) = concat('#',$defaultLanguage) and normalize-space(.) != ''])]">
                            <GM03_2_1Core.Core.PT_Group>
                                <language><xsl:value-of select="$defaultLanguage"/></language>
                                <plainText><xsl:value-of select="."/></plainText>
                            </GM03_2_1Core.Core.PT_Group>
                        </xsl:for-each>
                        <xsl:for-each select="gmd:PT_FreeText/gmd:textGroup/gmd:LocalisedCharacterString[normalize-space(.) != '']">
                            <GM03_2_1Core.Core.PT_Group>
                                <xsl:apply-templates mode="text" select="@locale"/>
                                <plainText><xsl:value-of select="."/></plainText>
                            </GM03_2_1Core.Core.PT_Group>
                        </xsl:for-each>
                    </textGroup>
                </GM03_2_1Core.Core.PT_FreeText>
            </xsl:element>
        </xsl:if>
    </xsl:template>
    
    <xsl:template mode="text" match="*[gco:CharacterString]" priority="1">
        <xsl:if test="normalize-space(.) != ''">
        <xsl:element name="{local-name(.)}">
            <xsl:value-of select="gco:CharacterString"/>
        </xsl:element>
        </xsl:if>
    </xsl:template>
    
    <xsl:template mode="text" match="*[gco:Real]">
        <xsl:element name="{local-name(.)}">
            <xsl:value-of select="gco:Real"/>
        </xsl:element>
    </xsl:template>
    
    <xsl:template mode="text" match="*[gco:Integer]">
        <xsl:element name="{local-name(.)}">
            <xsl:value-of select="gco:Integer"/>
        </xsl:element>
    </xsl:template>

    <xsl:template mode="text" match="*[gco:Decimal]">
        <xsl:element name="{local-name(.)}">
            <xsl:value-of select="gco:Decimal"/>
        </xsl:element>
    </xsl:template>

    <xsl:template mode="text" match="*[gco:Boolean]">
        <xsl:element name="{local-name(.)}">
            <!-- In Interlis and GM03, Boolean type is String.
            List of value is true and false only.
            Transform valid xsd:boolean value (0, 1, true, false) to 
            valid ili values. -->
            <xsl:choose>
                <xsl:when test="gco:Boolean='0'">false</xsl:when>
                <xsl:when test="gco:Boolean='1'">true</xsl:when>
                <xsl:otherwise><xsl:value-of select="gco:Boolean"/></xsl:otherwise>
            </xsl:choose>
        </xsl:element>
    </xsl:template>

    <xsl:template mode="text" match="*[*/@codeListValue]">
        <xsl:for-each select="*">
            <xsl:element name="{local-name(..)}">
                <xsl:value-of select="@codeListValue"/>
            </xsl:element>
        </xsl:for-each>
    </xsl:template>

    <xsl:template mode="text" match="*[*/@uom='m']">
        <xsl:for-each select="*">
            <xsl:element name="{local-name(..)}">
                <xsl:value-of select="."/>
            </xsl:element>
        </xsl:for-each>
    </xsl:template>

    <xsl:template mode="text" match="@locale">
        <language><xsl:choose>
            <xsl:when test=".='#DE'">de</xsl:when>
            <xsl:when test=".='#FR'">fr</xsl:when>
            <xsl:when test=".='#IT'">it</xsl:when>
            <xsl:when test=".='#EN'">en</xsl:when>
            <xsl:when test=".='#RM'">rm</xsl:when>
            <xsl:when test=".='#AA'">aa</xsl:when>
            <xsl:otherwise><xsl:value-of select="substring(., 2)"/></xsl:otherwise>
        </xsl:choose></language>
    </xsl:template>

    <xsl:template mode="characterString" match="*[gco:CharacterString and normalize-space(gco:CharacterString) != '']">
        <xsl:element name="{local-name(.)}">
            <xsl:value-of select="gco:CharacterString"/>
        </xsl:element>
    </xsl:template>

    <xsl:template mode="text" match="*[@xsi:type='che:PT_FreeURL_PropertyType' or gmd:URL or che:LocalisedURL]" priority="2">
        <xsl:element name="{local-name(.)}">
            <GM03_2_1Core.Core.PT_FreeURL>
                <URLGroup>
                <xsl:if test="normalize-space(.)=''">
                    <GM03_2_1Core.Core.PT_URLGroup>
                            <language><xsl:value-of select="$defaultLanguage"/></language>
                            <plainURL></plainURL>
                        </GM03_2_1Core.Core.PT_URLGroup>
                    </xsl:if>
                    <xsl:for-each select="gmd:URL[normalize-space(.) != '']">
                        <GM03_2_1Core.Core.PT_URLGroup>
                            <language><xsl:value-of select="$defaultLanguage"/></language>
                            <plainURL><xsl:value-of select="."/></plainURL>
                        </GM03_2_1Core.Core.PT_URLGroup>
                    </xsl:for-each>
                    <xsl:for-each select="che:LocalisedURL[normalize-space(.) != '']">
                        <GM03_2_1Core.Core.PT_URLGroup>
                            <language><xsl:value-of select="$defaultLanguage"/></language>
                            <plainURL><xsl:value-of select="."/></plainURL>
                        </GM03_2_1Core.Core.PT_URLGroup>
                    </xsl:for-each>
                    <xsl:for-each select="che:PT_FreeURL/che:URLGroup/che:LocalisedURL[normalize-space(.) != '']">
                        <GM03_2_1Core.Core.PT_URLGroup>
                            <xsl:apply-templates mode="text" select="@locale"/>
                            <plainURL><xsl:value-of select="."/></plainURL>
                        </GM03_2_1Core.Core.PT_URLGroup>
                    </xsl:for-each>
              </URLGroup>
            </GM03_2_1Core.Core.PT_FreeURL>
        </xsl:element>
    </xsl:template>

    <xsl:template mode="text" match="*[gco:Date]">
        <xsl:element name="{local-name(.)}">
            <xsl:value-of select="gco:Date"/>
        </xsl:element>
    </xsl:template>

    <xsl:template mode="text" match="*[gco:DateTime]">
        <xsl:element name="{local-name(.)}">
            <xsl:value-of select="gco:DateTime"/>
        </xsl:element>
    </xsl:template>

    <xsl:template mode="text" match="*[count(*)=0]">
    </xsl:template>

    <xsl:template mode="enum" match="*">
        <xsl:element name="{local-name(.)}">
            <xsl:for-each select="*">
                <xsl:element name="GM03_2_1Core.Core.{local-name(.)}_">
                    <value><xsl:value-of select="@codeListValue"/></value>
                </xsl:element>
            </xsl:for-each>
        </xsl:element>
    </xsl:template>

    <xsl:template mode="groupEnum" match="*">
    	<xsl:param name="element"/>
    	<xsl:param name="newName"/>
    	<xsl:if test="*[local-name(.) = $element]">
        <xsl:element name="{$element}">
            <xsl:for-each select="*[local-name(.) = $element]">
                <xsl:element name="{$newName}">
                    <value><xsl:value-of select="*/@codeListValue"/></value>
                </xsl:element>
            </xsl:for-each>
        </xsl:element>
      </xsl:if>
    </xsl:template>

    <xsl:template mode="enumISO" match="*">
        <xsl:param name="name"/>
        <xsl:param name="element"/>
        <xsl:param name="lowercase">0</xsl:param>
        <xsl:if test="*[local-name()=$element]">
            <xsl:element name="{$element}">
                <xsl:for-each select="*[local-name()=$element]/*">
                    <xsl:element name="{$name}">
                        <value>
                            <xsl:choose>
                                <xsl:when test="$element='language' and $lowercase='0'">
                                    <xsl:call-template name="lang3_to_lang2">
                                        <xsl:with-param name="lang3" select="@codeListValue"/>
                                    </xsl:call-template>
                                </xsl:when>
                                <xsl:when test="$element='language'">
                                    <xsl:variable name="code">
                                      <xsl:call-template name="lang3_to_lang2">
                                          <xsl:with-param name="lang3" select="@codeListValue"/>
                                      </xsl:call-template>
                                    </xsl:variable>
                                    <xsl:value-of select="translate($code, 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz')"/>
                                </xsl:when>
                                <xsl:when test="$lowercase='0'">
                                    <xsl:value-of select="@codeListValue"/>
                                </xsl:when>
                                <xsl:otherwise>
                                    <xsl:value-of select="translate(@codeListValue, 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz')"/>
                                </xsl:otherwise>
                            </xsl:choose>
                        </value>
                    </xsl:element>
                </xsl:for-each>
            </xsl:element>
        </xsl:if>
    </xsl:template>

    <xsl:template mode="enumC" match="*">
        <xsl:element name="{local-name(.)}">
            <xsl:for-each select="*">
                <xsl:element name="GM03_2_1Comprehensive.Comprehensive.{local-name(.)}_">
                    <value><xsl:value-of select="@codeListValue"/></value>
                </xsl:element>
            </xsl:for-each>
        </xsl:element>
    </xsl:template>

    <xsl:template mode="groupEnumC" match="*">
        <xsl:param name="element"/>
        <xsl:if test="*[local-name()=$element]">
            <xsl:element name="{$element}">
                <xsl:for-each select="*[local-name()=$element]/*">
                    <xsl:element name="GM03_2_1Comprehensive.Comprehensive.{local-name(.)}_">
                        <value><xsl:value-of select="@codeListValue"/></value>
                    </xsl:element>
                </xsl:for-each>
            </xsl:element>
        </xsl:if>
    </xsl:template>

    <xsl:template mode="text" match="*" priority="-100">
        <ERROR>Unknown text element <xsl:value-of select="local-name(.)" /> with parent <xsl:value-of select="local-name(..)" /></ERROR>
    </xsl:template>
</xsl:stylesheet>
