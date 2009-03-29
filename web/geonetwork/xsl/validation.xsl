<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
		xmlns:geonet="http://www.fao.org/geonetwork">

<!-- templates used by xslts that need to present validation output -->

  <xsl:template name="xsd">
		<font class="error"><xsl:value-of select="/root/error/message"/></font>
		<p/>
		<xsl:for-each select="/root/error/object/xsderrors/error">
			<xsl:value-of select="message"/>
  		<br/><br/>
		</xsl:for-each>
  </xsl:template>

	<xsl:template name="schematron">
		<xsl:if test="normalize-space(/root/error/object/schematronerrors/filename)!=''">
			<font class="error">Errors in file: <xsl:value-of select="/root/error/object/schematronerrors/filename"/></font><br/><br/>
		</xsl:if>
		<xsl:value-of select="/root/gui/validation/message"/>
		<br/><br/>
		<xsl:choose>
    	<xsl:when test="/root/error/object/schematronerrors">
      	<font class="error"><b>
        	<xsl:value-of select='/root/gui/validation/schemaTronError'/>
      	</b></font>
  			<br/><br/>
  			<a href="{/root/gui/url}/htmlCache/SchematronReport{/root/error/object/schematronerrors/id}/schematron-frame.html">
    			<xsl:value-of select="/root/gui/validation/schemaTronReport"/>
  			</a>
    	</xsl:when>
    	<xsl:otherwise>
      	<xsl:value-of select='/root/gui/validation/schemaTronValid'/>
    	</xsl:otherwise>
  	</xsl:choose>
	</xsl:template>

</xsl:stylesheet>

