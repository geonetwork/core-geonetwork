<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    exclude-result-prefixes="xs"
    version="2.0">
    
    <xsl:output method="text"/>
    
    <xsl:template match="/">
        <xsl:variable name="elements"
                    as="xs:string*"
                    select="'editor', 'fields', 'fieldsWithFieldset', 'multilingualFields', 
                    'views', 'view', 'tab', 'flatModeExceptions', 'thesaurusList',
                    'section', 'field', 'text', 'template', 'action'"/>
        
        <xsl:text>.. _creating-custom-editor:&#13;&#13;</xsl:text>
        <xsl:text>Customizing editor&#13;</xsl:text>
        <xsl:text>##################&#13;&#13;&#13;</xsl:text>
        
        <xsl:for-each select="//xs:element[@name = $elements]">
            
            <!-- Documentation for the current element -->
            <xsl:text>.. _creating-custom-editor-</xsl:text><xsl:value-of select="@name"/><xsl:text>:</xsl:text>
            <xsl:text>&#13;&#13;</xsl:text>
            <xsl:value-of select="xs:annotation/xs:documentation/text()"/>
            <xsl:text>&#13;&#13;</xsl:text>
            
            <xsl:if test="count(xs:complexType/xs:attribute) > 0">
                <xsl:text>Attributes:&#13;&#13;</xsl:text>
                <xsl:for-each select="xs:complexType/xs:attribute[@name]">
                    <xsl:sort select="@use" order="descending"/>
                    <xsl:text>- **</xsl:text><xsl:value-of select="@name"/><xsl:text>**</xsl:text>
                    <xsl:value-of select="if (@use = 'required') then ' (Mandatory)' else ' (Optional)'"/>
                    <xsl:value-of select="if (@fixed) then concat(' Fixed value: **', @fixed, '**') else ''"/>
                    <xsl:text>&#13;&#13;</xsl:text>
                    <xsl:value-of select="xs:annotation/xs:documentation/text()"/>
                    <xsl:text>&#13;&#13;</xsl:text>
                </xsl:for-each>
                <xsl:for-each select="xs:complexType/xs:attribute[@ref]">
                    <xsl:sort select="@use" order="descending"/>
                    <xsl:variable name="attributeName" select="@ref"/>
                    <xsl:for-each select="//xs:attribute[@name = $attributeName]">
                        <xsl:text>- **</xsl:text><xsl:value-of select="@name"/><xsl:text>**</xsl:text>
                        <xsl:value-of select="if (@use = 'required') then ' (Mandatory)' else ' (Optional)'"/>
                        <xsl:value-of select="if (@fixed) then concat(' Fixed value: **', @fixed, '**') else ''"/>
                        <xsl:text>&#13;&#13;</xsl:text>
                        <xsl:value-of select="xs:annotation/xs:documentation/text()"/>
                        <xsl:text>&#13;&#13;</xsl:text>    
                    </xsl:for-each>
                </xsl:for-each>
            </xsl:if>
            <!-- Link to children -->
            <xsl:if test="count(xs:complexType/xs:sequence/xs:element[@ref = $elements]) > 0">
                <xsl:text>Child elements:&#13;&#13;</xsl:text>
                <xsl:for-each select="xs:complexType/xs:sequence/xs:element[@ref = $elements]">
                  <xsl:text>- </xsl:text><xsl:value-of select="concat('**', @ref, '**')"/>
                  <xsl:if test="@minOccurs = '0' and @maxOccurs = '1'">
                      <xsl:text>, Optional element</xsl:text>
                  </xsl:if>
                  <xsl:if test="@minOccurs = '1' and @maxOccurs = '1'">
                      <xsl:text>, Mandatory element</xsl:text>
                  </xsl:if>
                  <xsl:if test="@minOccurs = '0' and @maxOccurs = 'unbounded'">
                      <xsl:text>, Zero or more</xsl:text>
                  </xsl:if>
                  <xsl:if test="@minOccurs = '1' and @maxOccurs = 'unbounded'">
                      <xsl:text>, One or more</xsl:text>
                  </xsl:if>
                  <xsl:value-of select="concat(' (see :ref:`creating-custom-editor-', @ref, '`)')"/>
                  <xsl:text>&#13;&#13;</xsl:text>
                </xsl:for-each>
            </xsl:if>
        </xsl:for-each>
        
    </xsl:template>
</xsl:stylesheet>