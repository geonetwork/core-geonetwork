<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:gmd="http://www.isotc211.org/2005/gmd"
  xmlns:gn-fn-iso19139="http://geonetwork-opensource.org/xsl/functions/profiles/iso19139">
  
  
  <!-- Get lang #id in metadata PT_Locale section,  deprecated: if not return the 2 first letters
        of the lang iso3code in uper case.
        
         if not return the lang iso3code in uper case.
        -->
  <xsl:function name="gn-fn-iso19139:getLangId" as="xs:string">
    <xsl:param name="md"/>
    <xsl:param name="lang"/>
    
    <xsl:choose>
      <xsl:when
        test="$md/gmd:locale/gmd:PT_Locale[gmd:languageCode/gmd:LanguageCode/@codeListValue = $lang]/@id"
        ><xsl:value-of
          select="concat('#', $md/gmd:locale/gmd:PT_Locale[gmd:languageCode/gmd:LanguageCode/@codeListValue = $lang]/@id)"
        />
      </xsl:when>
      <xsl:otherwise><xsl:value-of select="concat('#', upper-case($lang))"/></xsl:otherwise>
    </xsl:choose>
  </xsl:function>
  
  
  
  
  
  <xsl:function name="gn-fn-iso19139:getFieldType" as="xs:string">
      <!-- The container element -->
      <xsl:param name="name" as="xs:string"/>
      <!-- The element containing the value eg. gco:Date -->
      <xsl:param name="childName" as="xs:string"/>
<!--    <xsl:message>FieldType:<xsl:value-of select="$name"/>/<xsl:value-of select="$childName"/></xsl:message>
-->    <xsl:value-of select="if ($name = 'gmd:abstract' or $name = 'gmd:statement') 
                          then 'textarea' 
                          else if ($name = 'gmd:denominator') 
                          then 'number' 
                          else if ($name = 'gmd:electronicMailAddress') 
                          then 'email' 
                          else if ($name = 'time') 
                          then 'time'
                          else if ($childName = 'gco:Date') 
                          then 'date' 
                          else if ($childName = 'gco:DateTime') 
                          then 'datetime' 
                          else 'text'"/>
  </xsl:function>
  
  
  <xsl:function name="gn-fn-iso19139:getCodeListType" as="xs:string">
    <xsl:param name="name" as="xs:string"/>
    <xsl:text>select</xsl:text>
    <!-- TODO: Could be multiple select ? -->
  </xsl:function>
  
  
  
  <xsl:function name="gn-fn-iso19139:getTextareaCSS" as="xs:string">
    <xsl:param name="name" as="xs:string"/>
    
      <xsl:choose>
        <xsl:when test="$name='gmd:title'">title</xsl:when>
        <xsl:when test="$name='gmd:abstract'">large</xsl:when>
        <xsl:when test="$name='gmd:supplementalInformation'
          or $name='gmd:purpose'
          or $name='gmd:orderingInstructions'
          or $name='gmd:statement'">medium</xsl:when>
        <xsl:when test="$name='gmd:description'
          or $name='gmd:specificUsage'
          or $name='gmd:explanation'
          or $name='gmd:credit'
          or $name='gmd:evaluationMethodDescription'
          or $name='gmd:measureDescription'
          or $name='gmd:maintenanceNote'
          or $name='gmd:useLimitation'
          or $name='gmd:otherConstraints'
          or $name='gmd:handlingDescription'
          or $name='gmd:userNote'
          or $name='gmd:checkPointDescription'
          or $name='gmd:evaluationMethodDescription'
          or $name='gmd:measureDescription'
          ">small</xsl:when>
        <xsl:otherwise>small</xsl:otherwise>
      </xsl:choose>
    
  </xsl:function>
  
  
</xsl:stylesheet>
