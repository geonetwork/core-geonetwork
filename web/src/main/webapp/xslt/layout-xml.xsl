<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" 
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  exclude-result-prefixes="#all">
  
  <!-- 
    Render an element as XML tree in view or edit mode
  -->
  <xsl:template mode="render-xml" match="*">
    <xsl:choose>
      <xsl:when test="$isEditing">
        <textarea class="gn-textarea-xml">
          <xsl:apply-templates mode="gn-element-cleaner" select="."/>
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
