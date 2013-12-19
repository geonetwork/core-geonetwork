<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:geonet="http://www.fao.org/geonetwork" xmlns:xlink="http://www.w3.org/1999/xlink"
	xmlns:sch="http://www.ascc.net/xml/schematron" xmlns:gml="http://www.opengis.net/gml"
	xmlns:gmd="http://www.isotc211.org/2005/gmd" xmlns:srv="http://www.isotc211.org/2005/srv"
	xmlns:gco="http://www.isotc211.org/2005/gco" xmlns:svrl="http://purl.oclc.org/dsdl/svrl" 
	xmlns:xs="http://www.w3.org/2001/XMLSchema"
	exclude-result-prefixes="geonet srv gco gmd xlink gml sch xs">

  <xsl:include href="validate-fn.xsl"/>

	<!--
	page content
	-->
	<xsl:template match="/">
		<xsl:call-template name="metadata-validation-report">
			<xsl:with-param name="report" select="/root/response/geonet:report"/>
		</xsl:call-template>
	</xsl:template>


	<!-- templates used by xslts that need to present validation output -->
	<xsl:template name="metadata-validation-report">
		<xsl:param name="report"/>

		<div style="display:block;" class="content">
			<div>
				<input type="checkbox" id="checkError"
					onclick="updateValidationReportVisibilityRules(this.checked);">
					<xsl:if test="/root/gui/config/editor-validation-errors-only-on-load">
						<xsl:attribute name="checked">checked</xsl:attribute>
					</xsl:if>
				</input>
				<label for="checkError">
					<xsl:value-of select="/root/gui/strings/errorsOnly"/>
				</label>
			</div>
		  
		  <!-- Check if an error element exists. It could happen if XSD validation failed
		  when schema not found for example. -->
		  <xsl:if test="$report/error">
		    <fieldset class="validation-report">
		      <xsl:value-of select="$report/error/message"/><br/>
		    </fieldset>
		  </xsl:if>
			
		  <xsl:choose>
				<xsl:when test="$report/geonet:xsderrors">
					<xsl:apply-templates mode="validation-report" select="$report/geonet:xsderrors"
					/>
				</xsl:when>
				<xsl:otherwise>
					<fieldset class="validation-report">
						<legend class="block-legend">
							<xsl:value-of select="/root/gui/strings/xsdReport"/>
							<xsl:text> </xsl:text>
							<img src="../../images/button_ok.png" alt="pass" title="pass"/>
						</legend>
					</fieldset>
				</xsl:otherwise>
			</xsl:choose>
			<xsl:apply-templates mode="validation-report" select="$report/geonet:schematronerrors"/>
		</div>
	</xsl:template>


	<!-- XSD validation report -->
	<xsl:template match="geonet:xsderrors" mode="validation-report">
		<xsl:variable name="count" select="count(geonet:error)"/>


		<fieldset class="validation-report">
			<legend class="block-legend">
				<xsl:choose>
					<xsl:when test="$count = 0">
						<img src="../../images/button_ok.png" alt="pass" title="pass"/>
					</xsl:when>
					<xsl:otherwise/>
				</xsl:choose>
				<xsl:value-of select="/root/gui/strings/xsdReport"/>
				<xsl:if test="$count != 0"> (<xsl:value-of select="$count"
						/><xsl:text> </xsl:text><xsl:value-of select="/root/gui/strings/errors"/>)
				</xsl:if>
			</legend>
			<xsl:if test="geonet:error">
				<ul>
					<xsl:for-each select="geonet:error">
						<li name="error">
							<xsl:variable name="message">#<xsl:value-of select="geonet:errorNumber"
									/>:<xsl:value-of select="geonet:typeOfError"/>-XPath:
									<xsl:value-of select="geonet:xpath"/>
							</xsl:variable>
							<xsl:attribute name="alt">
								<xsl:value-of select="$message"/>
							</xsl:attribute>
							<xsl:attribute name="title">
								<xsl:value-of select="$message"/>
							</xsl:attribute>
							<img src="../../images/schematron.gif" alt="error" title="error"/>
							<xsl:text> </xsl:text>
							<font class="error">
							  <xsl:value-of select="geonet:parse-xsd-error(geonet:message, //response/schema, /root/gui)"/>
							</font>
						</li>
					</xsl:for-each>
				</ul>
			</xsl:if>
		</fieldset>
		
	</xsl:template>

	<xsl:template match="svrl:active-pattern" mode="validation-report">
		<xsl:variable name="preceding-ap" select="count(following-sibling::svrl:active-pattern)"/>
		
		<span class="arrow"><xsl:value-of select="@name"/></span>
		<div>
			<ul>
				<xsl:apply-templates mode="validation-report" 
					select="following-sibling::*[(name(.)='svrl:failed-assert' or name(.)='svrl:successful-report')
					and count(following-sibling::svrl:active-pattern) = $preceding-ap]"/>
			</ul>
		</div>
	</xsl:template>


	<xsl:template match="svrl:failed-assert" mode="validation-report">
		<li>
			<xsl:attribute name="name">error</xsl:attribute>
			<a href="{@ref}" title="{@location}" alt="{@location}">
				<img src="../../images/schematron.gif" style="border:none;"/>
			</a><xsl:text> </xsl:text>
			<xsl:value-of select="svrl:text"/>								
		</li>
	</xsl:template>
	
	<xsl:template match="svrl:successful-report" mode="validation-report">
		<li>
			<xsl:attribute name="name">pass</xsl:attribute>
			<a href="{@ref}" title="{@location}" alt="{@location}">
				<img src="../../images/button_ok.png" style="border:none;"/>
			</a><xsl:text> </xsl:text>
			<xsl:value-of select="svrl:text"/>
		</li>
	</xsl:template>
	
	
	<xsl:template match="geonet:report" mode="validation-report">
		<xsl:variable name="rule" select="@geonet:rule"/>
		<xsl:variable name="count" select="count(svrl:schematron-output/svrl:failed-assert)"/>
		<xsl:variable name="schema" select="string(//response/schema)"/>
		<xsl:variable name="title">
			<xsl:choose>
				<xsl:when test="/root/gui/strings/rules[@name=$rule]">
					<xsl:value-of select="/root/gui/strings/rules[@name=$rule]"/>
				</xsl:when>
				<xsl:when test="/root/gui/schemas/*[local-name()=$schema]/strings/rules[@name=$rule]">
					<xsl:value-of select="/root/gui/schemas/*[local-name()=$schema]/strings/rules[@name=$rule]"/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:value-of select="/root/gui/strings/rulesOther"/>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>

		<fieldset class="validation-report">
			<legend class="block-legend">
				<xsl:value-of select="$title"/><xsl:text> </xsl:text>
				<xsl:choose>
					<xsl:when test="$count != 0"> (
						<img src="../../images/schematron.gif" alt="failed" title="failed"/>
						<xsl:value-of select="$count"/>
						<xsl:text> </xsl:text>
						<xsl:value-of select="/root/gui/strings/errors"/>) 
					</xsl:when>
					<xsl:otherwise>
                        <xsl:choose>
                            <xsl:when test= "geonet:schematronVerificationError">
                                <img src="../../images/schematron.gif" alt="failed" title="failed"/>
                            </xsl:when>
                            <xsl:otherwise>
                                <img src="../../images/button_ok.png" alt="pass" title="pass"/>
                            </xsl:otherwise>
                        </xsl:choose>

					</xsl:otherwise>
				</xsl:choose>
			</legend>
			
			<xsl:apply-templates mode="validation-report" select="svrl:schematron-output/svrl:active-pattern"/>

            <xsl:apply-templates mode="validation-report" select="geonet:schematronVerificationError"/>
					
		</fieldset>
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
