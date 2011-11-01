<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:geonet="http://www.fao.org/geonetwork" xmlns:xlink="http://www.w3.org/1999/xlink"
    xmlns:sch="http://www.ascc.net/xml/schematron" xmlns:gml="http://www.opengis.net/gml"
    xmlns:gmd="http://www.isotc211.org/2005/gmd" xmlns:srv="http://www.isotc211.org/2005/srv"
    xmlns:gco="http://www.isotc211.org/2005/gco" xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
    exclude-result-prefixes="geonet srv gco gmd xlink gml sch">


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
		<xsl:value-of select="/root/gui/validation/message"/>
		<br/><br/>
		<xsl:choose>
    	<xsl:when test="/root/error/object/geonet:schematronerrors">
      	<font class="error"><b>
        	<xsl:value-of select='/root/gui/validation/schemaTronError'/>
      	</b></font>
  			<br/><br/>

  			<xsl:call-template name="metadata-validation-report">
			<xsl:with-param name="report" select="/root/error/object/geonet:schematronerrors/geonet:report"/>
			</xsl:call-template>
    	</xsl:when>
    	<xsl:otherwise>
      	<xsl:value-of select='/root/gui/validation/schemaTronValid'/>
    	</xsl:otherwise>
  	</xsl:choose>
	</xsl:template>

	<!-- templates used by xslts that need to present validation output -->
	<xsl:template name="metadata-validation-report">
		<xsl:param name="report"/>

		<div style="display:block; text-align:left" class="content">
			<xsl:apply-templates mode="validation-report" select="$report"/>
		</div>
	</xsl:template>

	<xsl:template match="svrl:active-pattern" mode="validation-report">
		<xsl:variable name="preceding-ap" select="count(following-sibling::svrl:active-pattern)"/>

		<xsl:if test="following-sibling::*[(name(.)='svrl:failed-assert')
					and count(following-sibling::svrl:active-pattern) = $preceding-ap]">

            <span class="arrow"><xsl:value-of select="@name"/></span>
            <div>
                <ul>
                    <xsl:apply-templates mode="validation-report"
                        select="following-sibling::*[(name(.)='svrl:failed-assert')
                        and count(following-sibling::svrl:active-pattern) = $preceding-ap]"/>
                </ul>
            </div>
		</xsl:if>
	</xsl:template>


	<xsl:template match="svrl:failed-assert" mode="validation-report">
		<li>
			<xsl:attribute name="name">error</xsl:attribute>
			<a href="#" title="{@location}" alt="{@location}">
				<img src="../../images/schematron.gif" style="border:none;"/>
			</a><xsl:text> </xsl:text>
			<xsl:value-of select="svrl:text"/>
		</li>
	</xsl:template>

	<xsl:template match="svrl:successful-report" mode="validation-report" />

	<xsl:template match="geonet:report" mode="validation-report">
		<xsl:variable name="rule" select="@geonet:rule"/>
		<xsl:variable name="count" select="count(svrl:schematron-output/svrl:failed-assert)"/>

		<xsl:if test="$count != 0">

		<fieldset class="validation-report">
			<legend class="block-legend">
				<xsl:value-of select="/root/gui/strings/rules[@name=$rule]"/><xsl:text> </xsl:text>
				(<img src="../../images/schematron.gif" alt="failed" title="failed"/>
						<xsl:value-of select="$count"/>
						<xsl:text> </xsl:text>
						<xsl:value-of select="/root/gui/strings/errors"/>)
			</legend>

			<xsl:apply-templates mode="validation-report" select="svrl:schematron-output/svrl:active-pattern"/>

		</fieldset>
		</xsl:if>

        <xsl:if test="string(geonet:schematronVerificationError)">

		<fieldset class="validation-report">
			<legend class="block-legend">
				<xsl:value-of select="/root/gui/strings/rules[@name=$rule]"/><xsl:text> </xsl:text>
				<img src="../../images/schematron.gif" alt="failed" title="failed"/>
			</legend>

            <xsl:apply-templates mode="validation-report" select="geonet:schematronVerificationError"/>

		</fieldset>
		</xsl:if>
	</xsl:template>

	<xsl:template match="geonet:schematronerrors" mode="validation-report">
		<fieldset class="validation-report">
			<legend class="block-legend">
				<xsl:value-of select="/root/gui/strings/schematronReport"/>
			</legend>
			<xsl:apply-templates select="*" mode="validation-report"/>
		</fieldset>
	</xsl:template>

     <xsl:template match="geonet:schematronVerificationError" mode="validation-report">
       <span class="arrow">
            <xsl:value-of select="."/>
        </span>
	</xsl:template>

</xsl:stylesheet>

