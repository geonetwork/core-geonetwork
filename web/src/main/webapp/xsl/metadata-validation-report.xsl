<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:geonet="http://www.fao.org/geonetwork">

	<!-- templates used by xslts that need to present validation output -->

	<xsl:template name="metadata-validation-report">
		<xsl:param name="metadata"/>

		<div id="validationReport" style="display:none;" class="content">
			<div>
				<input type="checkbox" id="checkError" onclick="updateValidationReportVisibleRules(this.checked);"/>
				<label for="checkError"><xsl:value-of select="/root/gui/strings/errorsOnly"/></label>
			</div>
			
			<xsl:choose>
				<xsl:when test="$metadata/geonet:xsderrors">
					<xsl:apply-templates mode="validation-report" select="$metadata/geonet:xsderrors"/>
				</xsl:when>
				<xsl:otherwise>
					<fieldset class="validation-report">
						<legend class="block-legend">
							<xsl:value-of select="/root/gui/strings/xsdReport"/>
						</legend>
						<ul>
							<li>
								<img src="../../images/button_ok.png" alt="pass" title="pass"/>
								<xsl:text> </xsl:text><xsl:value-of select="/root/gui/strings/valid"/>
							</li>
						</ul>
					</fieldset>
				</xsl:otherwise>
			</xsl:choose>
			<xsl:apply-templates mode="validation-report" select="$metadata/geonet:schematronerrors"/>
		</div>
	</xsl:template>


	<xsl:template match="geonet:xsderrors" mode="validation-report">
		<fieldset class="validation-report">
			<legend class="block-legend">
				<xsl:value-of select="/root/gui/strings/xsdReport"/>
			</legend>
			<ul>
				<xsl:for-each select="geonet:error">
					<li name="error">
						<xsl:attribute name="alt">
							<xsl:value-of select="geonet:xpath"/>
						</xsl:attribute>
						<xsl:attribute name="title">
							<xsl:value-of select="geonet:xpath"/>
						</xsl:attribute>
						<img src="../../images/schematron.gif" alt="error" title="error"/>
						<xsl:text> </xsl:text>
						<font class="error">
							<xsl:value-of select="geonet:message"/>
						</font>
					</li>
				</xsl:for-each>
			</ul>
		</fieldset>
	</xsl:template>

	<xsl:template match="geonet:report" mode="validation-report">
		<xsl:variable name="rule" select="@geonet:rule"/>

		<fieldset class="validation-report">
			<legend class="block-legend">
				<xsl:value-of select="/root/gui/strings/rules[@name=$rule]"/>
			</legend>
			<ul>
				<xsl:for-each select="geonet:schematronerrors/*">
					<li>
						<xsl:choose>
							<xsl:when test="geonet:pattern">
								<xsl:copy-of select="."/>
							</xsl:when>
							<xsl:otherwise>
								<xsl:choose>
									<xsl:when test="name(following-sibling::node())!='geonet:errorFound'">
										<xsl:attribute name="name">pass</xsl:attribute>
										<img src="../../images/button_ok.png" alt="error" title="error"/>										
									</xsl:when>
									<xsl:otherwise>
										<xsl:attribute name="name">error</xsl:attribute>
										<img src="../../images/schematron.gif" alt="error" title="error"/>										
									</xsl:otherwise>
								</xsl:choose>
								<xsl:text> </xsl:text>
								<xsl:value-of select="."/>
							</xsl:otherwise>
						</xsl:choose>
					</li>
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
