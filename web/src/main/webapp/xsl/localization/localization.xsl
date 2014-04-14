<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	
	<!-- ============================================================================================= -->

	<xsl:include href="../main.xsl"/>

	<!-- ============================================================================================= -->

	<xsl:template mode="script" match="/">
		<script type="text/javascript" src="{/root/gui/url}/static/kernel.js"/>
		<script type="text/javascript" src="{/root/gui/url}/scripts/core/gui/gui.js"/>
		<script type="text/javascript" src="{/root/gui/url}/scripts/localization/localization.js"/>
	</xsl:template>

	<!-- ============================================================================================= -->
	<!-- === page content -->
	<!-- ============================================================================================= -->

	<xsl:template name="content">
		<xsl:call-template name="formLayout">
			<xsl:with-param name="title" select="/root/gui/strings/localiz"/>

			<xsl:with-param name="content">
				<xsl:call-template name="panel"/>
			</xsl:with-param>

			<xsl:with-param name="buttons">
				<xsl:call-template name="buttons"/>
			</xsl:with-param>
		</xsl:call-template>
	</xsl:template>
	
	<!-- ============================================================================================= -->
	<!-- === Panels -->
	<!-- ============================================================================================= -->

	<xsl:template name="panel">
		<table width="100%" height="100%" class="text-aligned-left">
			<tr>
				<td><xsl:call-template name="entityView"/></td>
				<td><xsl:call-template name="entityData"/></td>
			</tr>
		</table>		
	</xsl:template>

	<!-- ============================================================================================= -->

	<xsl:template name="entityView">
		<div style="margin-right: 4px">
			<div style="margin-bottom: 4px">
				<span><xsl:value-of select="/root/gui/localiz/entity"/> </span>
				<select class="content" size="1" id="entity.type" style="margin-top: 8px;">
					<xsl:for-each select="/root/gui/localiz/entityType/type">
						<option value="{@id}">
							<xsl:value-of select="."/>
						</option>
					</xsl:for-each>
				</select>
				<img id="ajax.wait" src="{/root/gui/url}/images/loading.gif" alt="Loading" valign="top"/>
			</div>
			<div>
				<select class="content" size="20" style="width: 200px" id="entity.list"/>
			</div>
		</div>
	</xsl:template>

	<!-- ============================================================================================= -->

	<xsl:template name="entityData">
		<div style="margin: 4px">
			<div>
				<div style="margin-bottom: 4px">
					<span class="padded"><xsl:value-of select="/root/gui/localiz/srcLang"/></span>
					<select class="content" size="1" id="lang.source">
						<xsl:for-each select="/root/gui/languages/record">
							<option value="{id}">
								<xsl:value-of select="name"/>
							</option>
						</xsl:for-each>
					</select>
				</div>
				<div>
					<textarea style="width: 100%" readonly="true" class="content" id="editor.source" cols="60" rows="8" wrap="soft"/>
				</div>
			</div>
			<p/>
			<div>
				<div style="margin-bottom: 4px">
					<span class="padded"><xsl:value-of select="/root/gui/localiz/desLang"/></span>
					<select class="content" size="1" id="lang.destin">
						<xsl:for-each select="/root/gui/languages/record">
							<option value="{id}">
								<xsl:value-of select="name"/>
							</option>
						</xsl:for-each>
					</select>
				</div>
				<div>
					<textarea class="content" id="editor.destin" cols="60" rows="8" wrap="soft"/>
				</div>
			</div>		
		</div>
	</xsl:template>

	<!-- ============================================================================================= -->
	<!-- === Buttons -->
	<!-- ============================================================================================= -->

	<xsl:template name="buttons">
		<button class="content" onclick="load('{/root/gui/locService}/admin')">
			<xsl:value-of select="/root/gui/strings/back"/>
		</button>
		&#160;
		<button id="btn.save" class="content">
			<xsl:value-of select="/root/gui/localiz/save"/>
		</button>
		&#160;
		<button id="btn.refresh" class="content">
			<xsl:value-of select="/root/gui/localiz/refresh"/>
		</button>
	</xsl:template>

	<!-- ============================================================================================= -->

</xsl:stylesheet>
