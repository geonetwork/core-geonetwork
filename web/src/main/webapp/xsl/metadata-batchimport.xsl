<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	
	<xsl:include href="main.xsl"/>
	<xsl:include href="metadata-insert-form-utils.xsl"/>
	
	<!--
	page content
	-->
	<xsl:template name="content">
		<xsl:call-template name="formLayout">
			<xsl:with-param name="title" select="/root/gui/strings/batchImport"/>
			<xsl:with-param name="content">
				<xsl:call-template name="form"/>
			</xsl:with-param>
			<xsl:with-param name="buttons">
				<button class="content" onclick="goBack()"><xsl:value-of select="/root/gui/strings/back"/></button>
				&#160;
				<button class="content" onclick="goSubmit('xmlbatch')"><xsl:value-of select="/root/gui/strings/upload"/></button>
			</xsl:with-param>
		</xsl:call-template>
	</xsl:template>

	<xsl:template name="form">
		<form name="xmlbatch" accept-charset="UTF-8" action="{/root/gui/locService}/util.import" method="post">
			<input type="submit" style="display: none;" />
			
			<table>
				<tr>
					<th class="padded"><xsl:value-of select="/root/gui/strings/directory"/></th>
					<td class="padded"><input class="content" type="text" size="50" name="dir"/></td>
				</tr>
				
                <!-- transformation stylesheet -->
				<tr id="gn.fileType">
					<th class="padded" valign="top"><xsl:value-of select="/root/gui/strings/fileType"/></th>
					<td>
						<table>
							<tr>
								<td class="padded">
									<label for="singleFile"><xsl:value-of select="/root/gui/strings/singleFile"/></label>
									<input type="radio" id="singleFile" name="file_type" value="single" checked="true"/>
								</td>
								<td class="padded">
									<label for="mefFile"><xsl:value-of select="/root/gui/strings/mefFile"/></label>
									<input type="radio" id="mefFile" name="file_type" value="mef"/>
								</td>
							</tr>
						</table>
					</td>
				</tr>
				
				<xsl:call-template name="metadata-insert-common-form"/>
			</table>
		</form>
	</xsl:template>

</xsl:stylesheet>
