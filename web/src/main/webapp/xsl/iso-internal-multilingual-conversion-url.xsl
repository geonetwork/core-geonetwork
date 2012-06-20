<?xml version="1.0" encoding="UTF-8"?>

<!-- This file is a modified copy of iso-internal-multilingual-conversion.xsl so fix bugs in that file too-->
<!-- ConvertLocalisedURL is a testcase for this class -->

<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xlink="http://www.w3.org/1999/xlink"
	xmlns:che="http://www.geocat.ch/2008/che" xmlns:gml="http://www.opengis.net/gml"
	xmlns:gco="http://www.isotc211.org/2005/gco" xmlns:gmd="http://www.isotc211.org/2005/gmd"
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" >

    <!-- ============================================================================= -->

	<xsl:output method="xml" />

    <xsl:param name="urlMetadataLang" select="'EN'"/>

    <!-- Converting the simple format to iso -->
	<xsl:template match="/description">
       <gmd:description xsi:type="che:PT_FreeURL_PropertyType">
            <xsl:call-template name="composeURLTranslations">
                <xsl:with-param name="elem" select="."/>
            </xsl:call-template>
       </gmd:description>
	</xsl:template>
 
    <xsl:template name="composeURLTranslations">
        <xsl:param name="elem" />
         <xsl:choose>
          <xsl:when test="count($elem/child::node()[normalize-space(text())!=''])>0 and normalize-space($elem/text())=''"/>
          <xsl:otherwise>
            <che:PT_FreeURL>
        <che:URLGroup>
         <che:LocalisedURL>
          <xsl:attribute name="locale">
            <xsl:value-of select="concat('#', $urlMetadataLang)" />
          </xsl:attribute>
                <xsl:value-of select="normalize-space($elem/text())" />
         </che:LocalisedURL>
        </che:URLGroup>
            </che:PT_FreeURL>
          </xsl:otherwise>
        </xsl:choose>
         
        <xsl:choose>
         <xsl:when test="count($elem/child::node()[normalize-space(text())!=''])>0">
          <che:PT_FreeURL>
           <xsl:apply-templates mode="convert-url-inner-to-iso" select="$elem/*"/>
          </che:PT_FreeURL>
         </xsl:when>
         <xsl:otherwise>
    
          <xsl:apply-templates mode="convert-url-inner-to-iso" select="$elem/*"/>
         </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template mode="convert-url-inner-to-iso" match="text()"/>
    
	<xsl:template mode="convert-url-inner-to-iso" match="node()" priority="1">
		<xsl:variable name="currentName" select="name(.)"/>
 		<xsl:variable name="hasPreviousSiblingWithSameName" select="count(preceding-sibling::node()[name() = $currentName]) > 0"/>
		<xsl:if test="normalize-space(string(.))!='' and not($hasPreviousSiblingWithSameName)">
        <che:URLGroup>
         <che:LocalisedURL>
          <xsl:attribute name="locale">
            <xsl:value-of select="concat('#', normalize-space(name(.)))" />
          </xsl:attribute>
          <xsl:value-of select="normalize-space(.)" />
         </che:LocalisedURL>
        </che:URLGroup>
      </xsl:if>
	</xsl:template>


    <!-- Converting the iso format to the simple format -->
    
    <xsl:template match="/root">
        <result>
            <xsl:apply-templates mode="convert-url-iso-to-inner"/>
        </result>
    </xsl:template>
    
    <xsl:template mode="convert-url-iso-to-inner" match="che:PT_FreeURL/che:URLGroup/che:LocalisedURL">
          <xsl:variable name="code" select="substring(string(@locale),2)"/>
        <xsl:if test="string(text())!='' and ($urlMetadataLang!=$code or string(../../../gmd:URL)='')">
          <xsl:element name="{$code}"><xsl:value-of select="."/></xsl:element>
        </xsl:if>
    </xsl:template>
    
    <xsl:template mode="convert-url-iso-to-inner" match="gmd:URL">
        <xsl:element name="{$urlMetadataLang}"><xsl:value-of select="."/></xsl:element>
    </xsl:template>
    
</xsl:stylesheet>