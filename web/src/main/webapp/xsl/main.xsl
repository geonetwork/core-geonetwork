<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:output
		omit-xml-declaration="yes" 
		method="html" 
		doctype-public="-//W3C//DTD HTML 4.01 Transitional//EN"
		doctype-system="http://www.w3.org/TR/html4/loose.dtd"
		indent="yes"
		encoding="UTF-8" />
	
	<xsl:include href="header.xsl"/>
	<xsl:include href="banner.xsl"/>
	<xsl:include href="utils.xsl"/>
	
	<!--
	main page
	-->
	<xsl:template match="/">
		<html>
			<head>
				<xsl:call-template name="header"/>
				<xsl:apply-templates mode="script" select="/"/>
				
				<style type="text/css">
					body {
						background-color: #E4E9F1;
						height:100%;
					}
					
					#content {
						height:100%;
					}
				</style>
			</head>
			<body onload="init()">
				<!-- banner -->
				<div id="header">
					<xsl:call-template name="banner"/>
				</div>
			
				<div id="content_container" style="display:none">
					<xsl:call-template name="content"/>
				</div>

				<xsl:apply-templates mode="loading" select="/"/>                
			</body>
		</html>
	</xsl:template>

	<xsl:template mode="script" match="/"/>
	<xsl:template mode="css" match="/"/>
    
    <xsl:template mode="loading" match="/" priority="1">
        <script>
           Event.observe(window, 'load', function() {
                if ($("content_container")) {
                    $("content_container").show();
                }
            });
        </script>
    </xsl:template>
    
	<xsl:template name="formLayout">
		<xsl:param name="title"/>
		<xsl:param name="content"/>
		<xsl:param name="buttons"/>
		<xsl:param name="indent" select="100"/>
		
		<table  width="100%" height="100%">
		
			<!-- title -->
			<xsl:call-template name="formTitle">
				<xsl:with-param name="title" select="$title"/>
				<xsl:with-param name="indent" select="$indent"/>
			</xsl:call-template>
			
			<!-- content -->
			<xsl:call-template name="formSeparator"/>
			<xsl:call-template name="formContent">
				<xsl:with-param name="content" select="$content"/>
				<xsl:with-param name="indent" select="$indent"/>
			</xsl:call-template>
			
			<!-- buttons -->
			<xsl:if test="$buttons">
				<xsl:call-template name="formSeparator"/>
				<xsl:call-template name="formContent">
					<xsl:with-param name="content" select="$buttons"/>
				<xsl:with-param name="indent" select="$indent"/>
				</xsl:call-template>
			</xsl:if>
			
			<!-- footer -->
			<xsl:call-template name="formFiller">
				<xsl:with-param name="indent" select="$indent"/>
			</xsl:call-template>
			<tr><td class="blue-content" colspan="3"/></tr>
		</table>
	</xsl:template>

	<xsl:template name="formTitle">
		<xsl:param name="title"/>
		<xsl:param name="indent" select="100"/>
		
		<tr>
			<td class="padded-content" width="{$indent}"/>
			<td class="dots"/>
			<td class="padded-content">
				<h1><xsl:value-of select="$title"/></h1>
			</td>
		</tr>
	</xsl:template>
	
	<xsl:template name="formContent">
		<xsl:param name="content"/>
		<xsl:param name="indent" select="100"/>
		
		<tr>
			<td class="padded-content" width="{$indent}"/>
			<td class="dots"/>
			<td class="padded-content" align="center">
				<xsl:copy-of select="$content"/>
			</td>
		</tr>
	</xsl:template>
	
	<xsl:template name="formSection">
		<xsl:param name="title"/>
		<xsl:param name="indent" select="100"/>
		
		<tr>
			<td class="content" width="{$indent}" valign="bottom">
				<table width="100%"><tr><td class="dots"/></tr></table>
			</td>
			<td class="content" valign="bottom" colspan="2">
				<table width="100%"><tr>
					<td class="green-content"><xsl:copy-of select="$title"/></td>
					<td class="content" width="100%" valign="bottom">
						<table width="100%"><tr><td class="dots"/></tr></table>
					</td>
				</tr></table>
			</td>
		</tr>
	</xsl:template>
	
	<xsl:template name="formSeparator">
		<tr><td class="dots" colspan="3"/></tr>
	</xsl:template>
	
	<xsl:template name="formFiller">
		<xsl:param name="indent" select="100"/>
		
		<tr height="100%">
			<td class="padded-content" width="{$indent}"/>
			<td class="dots"/>
			<td class="padded-content">
			</td>
		</tr>
	</xsl:template>
	
</xsl:stylesheet>
