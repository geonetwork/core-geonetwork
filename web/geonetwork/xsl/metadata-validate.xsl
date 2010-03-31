<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:geonet="http://www.fao.org/geonetwork" xmlns:xlink="http://www.w3.org/1999/xlink"
	xmlns:sch="http://www.ascc.net/xml/schematron" xmlns:gml="http://www.opengis.net/gml"
	xmlns:gmd="http://www.isotc211.org/2005/gmd" xmlns:srv="http://www.isotc211.org/2005/srv"
	xmlns:gco="http://www.isotc211.org/2005/gco"
	exclude-result-prefixes="geonet srv gco gmd xlink gml sch">

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
							<xsl:variable name="message"> #<xsl:value-of select="geonet:errorNumber"
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
								<xsl:value-of select="geonet:message"/>
							</font>
						</li>
					</xsl:for-each>
				</ul>
			</xsl:if>
		</fieldset>
	</xsl:template>

	<xsl:template match="geonet:report" mode="validation-report">
		<xsl:variable name="rule" select="@geonet:rule"/>
		<xsl:variable name="count" select="count(geonet:schematronerrors/geonet:errorFound)"/>

		<fieldset class="validation-report">
			<legend class="block-legend">
				<xsl:value-of select="/root/gui/strings/rules[@name=$rule]"/>
				<xsl:choose>
					<xsl:when test="$count != 0"> (<xsl:value-of
							select="count(geonet:schematronerrors/geonet:errorFound)"
							/><xsl:text> </xsl:text><xsl:value-of select="/root/gui/strings/errors"
						/>) </xsl:when>
					<xsl:otherwise>
						<img src="../../images/button_ok.png" alt="pass" title="pass"/>
					</xsl:otherwise>
				</xsl:choose>
			</legend>
			<ul>
				<xsl:for-each select="geonet:schematronerrors/*">
					<!-- For each error pattern found, display error message 
					in the following sibling. -->
					<xsl:if test="name(.)='geonet:pattern'">
						<li>
							<xsl:choose>
								<xsl:when test="geonet:pattern">
									<xsl:copy-of select="."/>
								</xsl:when>
								<xsl:otherwise>
									<xsl:variable name="errorNotFound"
										select="name(following-sibling::node())!='geonet:errorFound'"/>

									<xsl:choose>
										<xsl:when test="$errorNotFound">
											<xsl:attribute name="name">pass</xsl:attribute>
											<img src="../../images/button_ok.png" alt="error"
												title="error"/>
										</xsl:when>
										<xsl:otherwise>
											<xsl:attribute name="name">error</xsl:attribute>
											<img src="../../images/schematron.gif" alt="error"
												title="error"/>
										</xsl:otherwise>
									</xsl:choose>
									<xsl:text> </xsl:text>
									<xsl:copy-of select="."/>
									<xsl:if test="not($errorNotFound)">
										<ul>
											<li>
												<xsl:copy-of
												select="following-sibling::node()[1]/geonet:diagnostics/*/."/>
												<br/>
											</li>
										</ul>
									</xsl:if>
								</xsl:otherwise>
							</xsl:choose>
						</li>
					</xsl:if>
				</xsl:for-each>
			</ul>		
		</fieldset>
	</xsl:template>

	<xsl:template match="geonet:schematronerrors" mode="validation-report">
		<fieldset class="validation-report">
			<legend class="block-legend">
				<xsl:value-of select="/root/gui/strings/schematronReport"/>
			</legend>
			<xsl:apply-templates select="*" mode="validation-report"/>
		</fieldset>
		<!--				<a href="{/root/gui/url}/htmlCache/SchematronReport{/root/error/object/schematronerrors/id}/schematron-frame.html">
			<xsl:value-of select="/root/gui/validation/schemaTronReport"/>
			<xsl:value-of select="/root/gui/validation/schemaTronValid"/>-->
	</xsl:template>

</xsl:stylesheet>
