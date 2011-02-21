<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	
	<xsl:include href="main.xsl"/>

	<!-- ============================================================================= -->
	<!-- page content -->
	<!-- ============================================================================= -->

	<xsl:template name="content">
		<xsl:call-template name="formLayout">
			<xsl:with-param name="title" select="/root/gui/create/title"/>
			<xsl:with-param name="content">
				<xsl:call-template name="form"/>
			</xsl:with-param>
			<xsl:with-param name="buttons">
				<button class="content" onclick="goBack()"><xsl:value-of select="/root/gui/strings/back"/></button>
				<xsl:if test="/root/gui/templates/record">
					&#160;
					<button class="content" onclick="doCreateCheck('metadata.create','createform','{$modal}')"><xsl:value-of select="/root/gui/create/button"/></button>
				</xsl:if>
			</xsl:with-param>
		</xsl:call-template>
	</xsl:template>

	<!-- ============================================================================= -->

	<xsl:template name="form">
		<xsl:choose>
			<xsl:when test="not(/root/gui/templates/record)">
				<table>
					<tr>
						<td>
							<xsl:value-of select="/root/gui/strings/noTemplatesAvailable"/>
						</td>
					</tr>
				</table>
			</xsl:when>
			<xsl:otherwise>
				<xsl:call-template name="template-form"/>
			</xsl:otherwise>
		</xsl:choose>
		
	</xsl:template>

	<!-- ============================================================================= -->
	
	<xsl:template name="template-form">
		<form id="createform" name="createform" accept-charset="UTF-8" action="metadata.create" method="post">
			<table class="text-aligned-left">
				<tr>
					<th class="padded"><xsl:value-of select="/root/gui/strings/template"/></th>
					<td class="padded">
						<select class="content" name="id" size="1">
							<xsl:for-each select="/root/gui/templates/record">
								<xsl:sort select="displayorder" data-type="number"/>
								<option value="{id}">
									<xsl:value-of select="concat('(',schema,') ',name)"/>
								</option>
							</xsl:for-each>
						</select>
					</td>
				</tr>
				
				<!-- groups -->
				
				<xsl:variable name="lang" select="/root/gui/language"/>
				
				<tr>
					<th class="padded"><xsl:value-of select="/root/gui/strings/group"/></th>
					<td class="padded">
						<select class="content" name="group" size="1" id="group">
							<xsl:for-each select="/root/gui/groups/record">
								<xsl:sort select="label/child::*[name() = $lang]"/>
								<option value="{id}">
									<xsl:value-of select="label/child::*[name() = $lang]"/>
								</option>
							</xsl:for-each>
						</select>
					</td>
				</tr>
			</table>
		</form>
	</xsl:template>


</xsl:stylesheet>
