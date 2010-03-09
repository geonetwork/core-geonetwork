<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:fo="http://www.w3.org/1999/XSL/Format" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<xsl:include href="modal.xsl"/>

	<!--
	page content
	-->
	<xsl:template name="content">
		<xsl:call-template name="formLayout">
			<xsl:with-param name="content">
			<center>
				<form id="fileUploadForm" name="fileUploadForm" accept-charset="UTF-8" method="POST" enctype="multipart/form-data" encoding="multipart/form-data" onsubmit="return doFileUploadSubmit(this);" action="{/root/gui/locService}/resources.upload">
					<input type="hidden" name="id" value="{/root/request/id}"/>
					<input type="hidden" name="access" value="private"/>
					<input type="hidden" name="ref" value="{/root/request/ref}"/>
					<input type="hidden" name="proto" value="{/root/request/protocol}"/>
					<xsl:value-of select="concat(/root/gui/strings/file,': ')"/><input type="file"   name="f_{/root/request/ref}"/>
					&#160;
					<input name="submit" type="submit" value="{/root/gui/strings/upload}"/>
					&#160;
					<label for="overwrite"><xsl:value-of select="/root/gui/strings/overwriteFile"/></label>
					<input id="overwrite" name="overwrite" type="checkbox" />
				</form>
			<div id='uploadresponse'></div>
			</center>
			</xsl:with-param>
		</xsl:call-template>
	</xsl:template>
	
</xsl:stylesheet>
