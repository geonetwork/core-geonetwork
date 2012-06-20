<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
                xmlns:comp="http://toignore"
                xmlns:che="http://www.geocat.ch/2008/che"
                xmlns:srv="http://www.isotc211.org/2005/srv"
                xmlns:gmd="http://www.isotc211.org/2005/gmd"
                xmlns:gco="http://www.isotc211.org/2005/gco"
                xmlns:gml="http://www.opengis.net/gml"
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:int="http://www.interlis.ch/INTERLIS2.3"
                exclude-result-prefixes="int"
                xmlns:xalan="http://xml.apache.org/xalan" >

    <xsl:include href="resolve-refs.xsl"/>
    <xsl:include href="metadata.xsl"/>
    <xsl:include href="resp-party.xsl"/>
    <xsl:include href="spatial-repr.xsl"/>
    <xsl:include href="citation.xsl"/>
    <xsl:include href="content.xsl"/>
    <xsl:include href="maintenance-info.xsl"/>
    <xsl:include href="data-quality.xsl"/>
    <xsl:include href="identification.xsl"/>
    <xsl:include href="extent.xsl"/>
    <xsl:include href="distribution.xsl"/>
    <xsl:include href="ref-system.xsl"/>
    <xsl:include href="legislation.xsl"/>

    <xsl:param name="DEBUG">0</xsl:param>
    <xsl:template match="/comp:TRANSFER/comp:DATASECTION/comp:GM03_2Comprehensive.Comprehensive|/comp:TRANSFER/comp:DATASECTION/comp:GM03_2Core.Core"
                  priority="10">
        <xsl:variable name="noNamespace">
            <xsl:apply-templates mode="remove-namespace" select="/"/>
        </xsl:variable>

        <xsl:apply-templates select="$noNamespace"/>
    </xsl:template>
    <xsl:variable name="UPPER">ABCDEFGHIJKLMNOPQRSTUVWXYZ</xsl:variable>
    <xsl:variable name="LOWER">abcdefghijklmnopqrstuvwxyz</xsl:variable>
    <xsl:template mode="remove-namespace"  match="/|comment()|processing-instruction()">
        <xsl:copy>
            <xsl:apply-templates mode="remove-namespace" />
        </xsl:copy>
    </xsl:template>

     <xsl:template mode="remove-namespace" match="*">
        <xsl:element name="{local-name()}" namespace="">
            <xsl:apply-templates mode="remove-namespace" select="@*|node()" />
        </xsl:element>
     </xsl:template>

     <xsl:template mode="remove-namespace" match="@*">
        <xsl:attribute name="{local-name()}"  namespace="">
          <xsl:value-of select="." />
        </xsl:attribute>
     </xsl:template>

    <xsl:template match="/int:TRANSFER/int:DATASECTION/int:GM03_2Comprehensive.Comprehensive|/int:TRANSFER/int:DATASECTION/int:GM03_2Core.Core">
        <xsl:choose>
            <xsl:when test="$DEBUG=1">
                <xsl:apply-templates select="." mode="ResolveRefs"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:variable name="resolvedText">
                    <xsl:apply-templates select="." mode="ResolveRefs"/>
                </xsl:variable>
                <xsl:variable name="resolved" select="$resolvedText"/>

                <xsl:apply-templates select="$resolved" mode="root"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <!-- ====================================================================================== -->

    <xsl:template match="/" mode="root">
        <xsl:apply-templates mode="root"/>
    </xsl:template>

    <xsl:template match="int:GM03_2Comprehensive.Comprehensive|int:GM03_2Core.Core" mode="root">
        <che:CHE_MD_Metadata gco:isoType="gmd:MD_Metadata">
            <xsl:apply-templates mode="root"/>
        </che:CHE_MD_Metadata>
    </xsl:template>

    <xsl:template match="int:GM03_2Core.Core.MD_Metadata" mode="root">
        <xsl:apply-templates select="." mode="MetaData"/>
    </xsl:template>

    <xsl:template match="int:GM03_2Comprehensive.Comprehensive.formatDistributordistributorFormat" mode="root"/>

    <xsl:template match="*" mode="root">
        <ERROR mode="root" tag="{name(..)}/{name(.)}"/>
    </xsl:template>
    
    <xsl:template name="mainLanguage">
        <xsl:choose>
            <xsl:when test="//int:GM03_2Core.Core.MD_Metadata/int:language">
                <xsl:value-of select="//int:GM03_2Core.Core.MD_Metadata/int:language"/>
            </xsl:when>
            <xsl:otherwise>de</xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    
    <xsl:template name="locale">
        <xsl:variable name="mainLanguage">
            <xsl:call-template name="mainLanguage"/>
        </xsl:variable>
        <xsl:choose>
            <xsl:when test="int:language">
                <xsl:value-of select="translate(int:language, $LOWER, $UPPER)"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="translate($mainLanguage, $LOWER, $UPPER)"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template match="text()" mode="root">
        <xsl:call-template name="UnMatchedText">
            <xsl:with-param name="mode">root</xsl:with-param>
        </xsl:call-template>
    </xsl:template>

    <!-- ====================================================================================== -->

    <xsl:variable name="mainLanguage">
        <xsl:call-template name="mainLanguage"/>
    </xsl:variable>

    <xsl:template match="int:GM03_2Core.Core.PT_FreeURL" mode="language">
        <xsl:attribute name="xsi:type">che:PT_FreeURL_PropertyType</xsl:attribute>

        <che:PT_FreeURL>
            <xsl:apply-templates mode="language" select="int:URLGroup/int:GM03_2Core.Core.PT_URLGroup"/>
        </che:PT_FreeURL>
    </xsl:template>

    <xsl:template match="int:GM03_2Core.Core.PT_URLGroup" mode="language">

        <xsl:variable name="lang">
            <xsl:call-template name="locale"/>
        </xsl:variable>
        <che:URLGroup>
            <che:LocalisedURL locale="#{$lang}">
                <xsl:value-of select="normalize-space(int:plainURL)"/>
            </che:LocalisedURL>
        </che:URLGroup>
    </xsl:template>

    <!-- ====================================================================================== -->

    <xsl:template match="*" mode="languageGroup">
        <xsl:param name="element" select="name()" />
        <xsl:element name="gmd:{$element}"
                     namespace="http://www.isotc211.org/2005/gmd">
            <xsl:attribute name="xsi:type">gmd:PT_FreeText_PropertyType</xsl:attribute>

            <xsl:if test="*[local-name()=$element]/int:GM03_2Core.Core.PT_FreeText/int:textGroup/int:GM03_2Core.Core.PT_Group">
                <gmd:PT_FreeText>
                    <xsl:apply-templates select="*[local-name()=$element]/int:GM03_2Core.Core.PT_FreeText/int:textGroup/int:GM03_2Core.Core.PT_Group" mode="language"/>
                </gmd:PT_FreeText>
            </xsl:if>
           </xsl:element>
    </xsl:template>

    <xsl:template match="int:GM03_2Core.Core.PT_FreeText" mode="language">
        <xsl:attribute name="xsi:type">gmd:PT_FreeText_PropertyType</xsl:attribute>

        <xsl:if test="int:textGroup/int:GM03_2Core.Core.PT_Group">
            <gmd:PT_FreeText>
                <xsl:apply-templates select="int:textGroup/int:GM03_2Core.Core.PT_Group" mode="language"/>
            </gmd:PT_FreeText>
        </xsl:if>
    </xsl:template>

    <xsl:template match="int:GM03_2Core.Core.PT_Group" mode="language">
        <xsl:variable name="lang">
            <xsl:call-template name="locale"/>
        </xsl:variable>
        <gmd:textGroup>
            <gmd:LocalisedCharacterString locale="#{$lang}">
                <xsl:value-of select="int:plainText"/>
            </gmd:LocalisedCharacterString>
        </gmd:textGroup>
    </xsl:template>

    <!-- ====================================================================================== -->

    <xsl:template mode="languageToIso3" match="text()">
        <xsl:variable name="lang" select="translate(., $LOWER, $UPPER)"/>
        <xsl:choose>
            <xsl:when test="$lang='DE'">deu</xsl:when>
            <xsl:when test="$lang='FR'">fra</xsl:when>
            <xsl:when test="$lang='IT'">ita</xsl:when>
            <xsl:when test="$lang='EN'">eng</xsl:when>
            <xsl:when test="$lang='RM'">rhe</xsl:when>
            <xsl:otherwise>ERROR_<xsl:value-of select="$lang"/></xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template mode="language" match="int:CodeISO.LanguageCodeISO_">
        <gmd:language>
            <gco:CharacterString><xsl:apply-templates mode="languageToIso3" select="int:value"/></gco:CharacterString>
        </gmd:language>
    </xsl:template>

    <xsl:template mode="language" match="text()">
        <xsl:call-template name="UnMatchedText">
            <xsl:with-param name="mode">language</xsl:with-param>
        </xsl:call-template>
    </xsl:template>

    <!-- ====================================================================================== -->

    <xsl:template match="*" mode="real">
        <xsl:element name="gmd:{local-name(.)}"
                     namespace="http://www.isotc211.org/2005/gmd">
            <xsl:apply-templates mode="real"/>
        </xsl:element>
    </xsl:template>

    <xsl:template match="text()" mode="real">
        <gco:Real>
            <xsl:value-of select="."/>
        </gco:Real>
    </xsl:template>

    <!-- ====================================================================================== -->

    <xsl:template match="*" mode="integerCHE">
        <xsl:element name="che:{local-name(.)}">
            <xsl:apply-templates mode="integer"/>
        </xsl:element>
    </xsl:template>

    <xsl:template match="*" mode="integer">
        <xsl:element name="gmd:{local-name(.)}"
                     namespace="http://www.isotc211.org/2005/gmd">
            <xsl:apply-templates mode="integer"/>
        </xsl:element>
    </xsl:template>

    <xsl:template match="text()" mode="integer">
        <gco:Integer>
            <xsl:value-of select="."/>
        </gco:Integer>
    </xsl:template>

    <!-- ====================================================================================== -->

    <xsl:template match="*" mode="boolean">
        <xsl:element name="gmd:{local-name(.)}"
                     namespace="http://www.isotc211.org/2005/gmd">
            <xsl:apply-templates mode="boolean"/>
        </xsl:element>
    </xsl:template>

    <xsl:template match="text()" mode="boolean">
        <gco:Boolean>
            <xsl:choose>
                <xsl:when test="translate(string(.),$UPPER,$LOWER) = 'false'">0</xsl:when>
                <xsl:when test="translate(string(.),$UPPER,$LOWER) = 'true'">1</xsl:when>
                <xsl:otherwise>
                    <xsl:value-of select="."/>
                </xsl:otherwise>
            </xsl:choose>
        </gco:Boolean>
    </xsl:template>

    <!-- ====================================================================================== -->

    <xsl:template match="text()" mode="string">
        <gco:CharacterString>
            <xsl:value-of select="."/>
        </gco:CharacterString>
    </xsl:template>

    <!-- ====================================================================================== -->

    <xsl:template mode="text" match="*">
       <xsl:param name="prefix">gmd</xsl:param>
        <xsl:choose>
            <xsl:when test="int:GM03_2Core.Core.PT_FreeText">
                <xsl:for-each select="int:GM03_2Core.Core.PT_FreeText">
                    <xsl:element name="{$prefix}:{local-name(..)}">
                        <xsl:apply-templates mode="language" select="."/>
                    </xsl:element>
                </xsl:for-each>
            </xsl:when>
            <xsl:when test="int:GM03_2Core.Core.CharacterString_">
                <xsl:element name="{$prefix}:{local-name(.)}">
                    <xsl:for-each select="int:GM03_2Core.Core.CharacterString_">
                        <gco:CharacterString>
                            <xsl:value-of select="int:value"/>
                        </gco:CharacterString>
                    </xsl:for-each>
                </xsl:element>
            </xsl:when>
            <xsl:otherwise>
                <xsl:element name="{$prefix}:{local-name(.)}">
                    <xsl:apply-templates mode="string"/>
                </xsl:element>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template mode="textCHE" match="*">
        <xsl:choose>
            <xsl:when test="int:GM03_2Core.Core.PT_FreeText">
                <xsl:for-each select="int:GM03_2Core.Core.PT_FreeText">
                    <xsl:element name="che:{local-name(..)}">
                        <xsl:apply-templates mode="language" select="."/>
                    </xsl:element>
                </xsl:for-each>
            </xsl:when>
            <xsl:when test="int:GM03_2Core.Core.CharacterString_">
                <xsl:element name="che:{local-name(.)}">
                    <xsl:for-each select="int:GM03_2Core.Core.CharacterString_">
                        <gco:CharacterString>
                            <xsl:value-of select="int:value"/>
                        </gco:CharacterString>
                    </xsl:for-each>
                </xsl:element>
            </xsl:when>
            <xsl:otherwise>
                <xsl:element name="che:{local-name(.)}">
                    <xsl:apply-templates mode="string"/>
                </xsl:element>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <!-- ====================================================================================== -->

    <xsl:template match="text()" mode="date">
        <xsl:choose>
            <xsl:when test="string-length(.)=0">
            </xsl:when>
            <xsl:when test="string-length(.)&lt;=10">
                <gco:Date>
                    <xsl:value-of select="translate(., ':', '-')"/>
                </gco:Date>
            </xsl:when>
            <xsl:when test="string-length(.)=16">
                <gco:Date>
                    <xsl:value-of select="translate(substring(., 0, 11), ':', '-')"/>T<xsl:value-of select="substring(., 12)"/>:00
                </gco:Date>
            </xsl:when>
            <xsl:otherwise>
                <gco:DateTime>
                    <xsl:value-of select="translate(substring(., 0, 11), ':', '-')"/>T<xsl:value-of select="substring(., 12)"/>
                </gco:DateTime>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <!-- ====================================================================================== -->

    <xsl:template match="text()" mode="dateTime">
        <xsl:choose>
            <xsl:when test="string-length(.)=0">
            </xsl:when>
            <xsl:when test="string-length(.)&lt;=10">
                <gco:DateTime>
                    <xsl:value-of select="translate(., ':', '-')"/>T12:00:00
                </gco:DateTime>
            </xsl:when>
            <xsl:when test="string-length(.)=16">
                <gco:DateTime>
                    <xsl:value-of select="translate(substring(., 0, 11), ':', '-')"/>T<xsl:value-of select="substring(., 12)"/>:00
                </gco:DateTime>
            </xsl:when>
            <xsl:otherwise>
                <gco:DateTime>
                    <xsl:value-of select="translate(substring(., 0, 11), ':', '-')"/>T<xsl:value-of select="substring(., 12)"/>
                </gco:DateTime>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <!-- ====================================================================================== -->

    <xsl:template match="text()">
        <xsl:call-template name="UnMatchedText">
            <xsl:with-param name="mode"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template name="UnMatchedText">
        <xsl:param name="mode">???</xsl:param>
        <xsl:if test="normalize-space(.)!=''">
            <ERROR mode="{$mode}"
                   tag="{name(../../../../../../..)}/{name(../../../../../..)}/{name(../../../../..)}/{name(../../../..)}/{name(../../..)}/{name(../..)}/{name(..)}" TID="{@TID}">
                <xsl:value-of select="."/>
            </ERROR>
        </xsl:if>
    </xsl:template>

</xsl:stylesheet>
