<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" 
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:saxon="http://saxon.sf.net/" extension-element-prefixes="saxon"
  exclude-result-prefixes="#all">
  
  <!-- 
    Render an element as XML tree in view or edit mode
  -->
  <xsl:template mode="render-xml" match="*">
    <xsl:choose>
      <xsl:when test="$isEditing">
        <!-- TODO: could help editor to have basic
        syntax highlighting. -->
        <textarea name="data" class="gn-textarea-xml form-control" data-gn-autogrow="">
          
          <!-- Remove gn:* element -->
          <xsl:variable name="strippedXml">
            <xsl:apply-templates mode="gn-element-cleaner" select="."/>
          </xsl:variable>
          
          <!-- Render XML in textarea -->
          <xsl:value-of select="saxon:serialize($strippedXml, 'default-indent-mode')"></xsl:value-of>
        </textarea>
      </xsl:when>
      <xsl:otherwise>
        <pre>
          <xsl:copy-of select="."/>
        </pre>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
</xsl:stylesheet>
