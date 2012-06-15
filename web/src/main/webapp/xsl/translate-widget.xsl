<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    
    <!-- Note for this to work you need to include the following lines in your stylesheet:
    
                <script type="text/javascript" src="{/root/gui/url}/scripts/translation_edit.js"/>
                <script src="{/root/gui/url}/scripts/mapfishIntegration/ext-small.js" type="text/javascript"/>
    
                 // call next line after the page has been rendered  (Ext.onReady for example)
                 // key is the key that identifies the widget set
                 editI18n.init('key', '<xsl:value-of select="/root/gui/language"/>');
     -->
    
    
    
    
    <xsl:variable name="UPPER">ABCDEFGHIJKLMNOPQRSTUVWXYZ</xsl:variable>
    <xsl:variable name="LOWER">abcdefghijklmnopqrstuvwxyz</xsl:variable>
    
    <xsl:template name="translationWidgetInputs">
        <xsl:param name="root" />
        <xsl:param name="key" />
        <xsl:param name="size" />
    
          <xsl:variable name="descDe" >
              <xsl:call-template name="translations">
                  <xsl:with-param name="root" select="$root"/>
                  <xsl:with-param name="langCode" select="'DE'"/>
              </xsl:call-template>
          </xsl:variable>
          <xsl:variable name="descFr" >
              <xsl:call-template name="translations">
                  <xsl:with-param name="root" select="$root"/>
                  <xsl:with-param name="langCode" select="'FR'"/>
              </xsl:call-template>
          </xsl:variable>
          <xsl:variable name="descIt" >
              <xsl:call-template name="translations">
                  <xsl:with-param name="root" select="$root"/>
                  <xsl:with-param name="langCode" select="'IT'"/>
              </xsl:call-template>
          </xsl:variable>
          <xsl:variable name="descEn" >
              <xsl:call-template name="translations">
                  <xsl:with-param name="root" select="$root"/>
                  <xsl:with-param name="langCode" select="'EN'"/>
              </xsl:call-template>
          </xsl:variable>
          <xsl:variable name="descRm" >
              <xsl:call-template name="translations">
                  <xsl:with-param name="root" select="$root"/>
                  <xsl:with-param name="langCode" select="'RM'"/>
              </xsl:call-template>
          </xsl:variable>
         <input type="text" id="{$key}DE" class="{$key} content" name="{$key}DE" value="{$descDe}" size="{$size}" style="display:none"/>
         <input type="text" id="{$key}FR" class="{$key} content" name="{$key}FR" value="{$descFr}" size="{$size}" style="display:none"/>
         <input type="text" id="{$key}IT" class="{$key} content" name="{$key}IT" value="{$descIt}" size="{$size}" style="display:none"/>
         <input type="text" id="{$key}EN" class="{$key} content" name="{$key}EN" value="{$descEn}" size="{$size}" style="display:none"/>
         <input type="text" id="{$key}RM" class="{$key} content" name="{$key}RM" value="{$descRm}" size="{$size}" style="display:none"/>
    </xsl:template>
    
    <xsl:template name="translationWidgetSelect" >
        <xsl:param name="key" />
        <xsl:param name="class" />
        
          <xsl:variable name="DE" select="/root/gui/strings/ger"/>
          <xsl:variable name="FR" select="/root/gui/strings/fre"/>
          <xsl:variable name="IT" select="/root/gui/strings/ita"/>
          <xsl:variable name="EN" select="/root/gui/strings/eng"/>
          <xsl:variable name="RM" select="/root/gui/strings/rm"/>
         
         <select id="langSelector{$key}" name="language" class="{$class} content">
             <option id="option{$key}DE" ><xsl:value-of select="$DE"/></option>
             <option id="option{$key}FR" ><xsl:value-of select="$FR"/></option>
             <option id="option{$key}IT" ><xsl:value-of select="$IT"/></option>
             <option id="option{$key}EN" ><xsl:value-of select="$EN"/></option>
             <option id="option{$key}RM" ><xsl:value-of select="$RM"/></option>
         </select>
    </xsl:template>
    
    <xsl:template name="translations" >
        <xsl:param name="root" />
        <xsl:param name="langCode" />
        
        <xsl:choose>
            <xsl:when test="normalize-space($root/node()[substring(translate(name(),$LOWER, $UPPER),1,2)=$langCode]) != ''">
                <xsl:value-of select="$root/node()[substring(translate(name(),$LOWER, $UPPER),1,2)=$langCode]/text()"/>
            </xsl:when>
            <xsl:when test="normalize-space($root/node()[substring(translate(name(),$UPPER, $LOWER),1,2)=$langCode]) != ''">
                <xsl:value-of select="$root/node()[substring(translate(name(),$UPPER, $LOWER),1,2)=$langCode]/text()"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="$root/*/text()"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    
    
</xsl:stylesheet>