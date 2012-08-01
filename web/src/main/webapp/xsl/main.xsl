<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
				xmlns:exslt="http://exslt.org/common">

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
						height:100%;
					}
				</style>
			</head>
			<body onload="init();">
				<!-- banner -->
				<xsl:if test="not(/root/request/modal)">
					<div id="header">
						<xsl:call-template name="banner"/>
					</div>
				</xsl:if>
			
				<div id="content_container" style="display:none">
					<xsl:if test="/root/request/modal">
						<xsl:attribute name="style">display: block"</xsl:attribute>
					</xsl:if>
					<xsl:call-template name="content"/>
				</div>

				<xsl:if test="not(/root/request/modal)">
					<xsl:apply-templates mode="loading" select="/"/>
				</xsl:if>
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
		</script><xsl:text>&#10;</xsl:text>
	</xsl:template>

	<xsl:template name="formLayout">
		<xsl:param name="title"/>
		<xsl:param name="content"/>
		<xsl:param name="buttons"/>
		<xsl:param name="indent" select="100"/>
		
		<table  width="100%" height="100%">
	
			<!-- title -->
			<xsl:if test="$title">
				<xsl:call-template name="formTitle">
					<xsl:with-param name="title" select="$title"/>
					<xsl:with-param name="indent" select="$indent"/>
				</xsl:call-template>
				<xsl:call-template name="formSeparator"/>
			</xsl:if>
			
			<!-- content -->
			<xsl:call-template name="formContent">
				<xsl:with-param name="content" select="$content"/>
				<xsl:with-param name="indent" select="$indent"/>
			</xsl:call-template>
			
			<!-- buttons -->
			<xsl:if test="$buttons">
				<xsl:call-template name="formSeparator"/>
				<xsl:choose>
					<xsl:when test="/root/request/modal">
						<!-- remove the back button -->
						<xsl:variable name="buttonsTmp" select="exslt:node-set($buttons)"/>
						<xsl:variable name="buttonsNoBack">
							<xsl:apply-templates mode="buttons" select="$buttonsTmp/*"/>
						</xsl:variable>
						<xsl:call-template name="formContent">
							<xsl:with-param name="content" select="$buttonsNoBack"/>
							<xsl:with-param name="indent" select="$indent"/>
						</xsl:call-template>
					</xsl:when>
					<xsl:otherwise>
						<xsl:call-template name="formContent">
							<xsl:with-param name="content" select="$buttons"/>
							<xsl:with-param name="indent" select="$indent"/>
						</xsl:call-template>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:if>
			
			<!-- footer -->
			<xsl:if test="not(/root/request/modal)">
				<xsl:call-template name="formFiller">
					<xsl:with-param name="indent" select="$indent"/>
				</xsl:call-template>
				<tr><td class="blue-content" colspan="3"/></tr>
			</xsl:if>
		</table>
	</xsl:template>

	<xsl:template name="formTitle">
		<xsl:param name="title"/>
		<xsl:param name="indent" select="100"/>
		
		<tr>
			<xsl:choose>
				<xsl:when test="not(/root/request/modal)">
					<td class="padded-content" width="{$indent}"/>
					<td class="dots"/>
					<td class="padded-content">
						<h1><xsl:value-of select="$title"/></h1>
					</td>
				</xsl:when>
				<xsl:otherwise>
					<td class="padded-content" colspan="3" align="center">
						<h1><xsl:value-of select="$title"/></h1>
					</td>
				</xsl:otherwise>
			</xsl:choose>
		</tr>
	</xsl:template>
	
	<xsl:template name="formContent">
		<xsl:param name="content"/>
		<xsl:param name="indent" select="100"/>
		
		<tr>
			<xsl:choose>
				<xsl:when test="not(/root/request/modal)">
					<td class="padded-content" width="{$indent}"/>
					<td class="dots"/>
					<td class="padded-content" align="center">
						<xsl:copy-of select="$content"/>
					</td>
				</xsl:when>
				<xsl:otherwise>
					<td class="padded-content" colspan="3" align="center">
						<xsl:copy-of select="$content"/>
					</td>
				</xsl:otherwise>
			</xsl:choose>
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

	<!-- when displaying a page using modalbox, it makes no sense to have 
		 a back button so copy everything in the supplied buttons except
			 the back button -->

	<xsl:variable name="backButtonName" select="/root/gui/strings/back"/>

	<xsl:template mode="buttons" match="button[normalize-space()=$backButtonName]" priority="20"/>

	<xsl:template mode="buttons" match="@*|node()">
		<xsl:copy>
			<xsl:apply-templates mode="buttons" select="@*|node()"/>
		</xsl:copy>
	</xsl:template>


</xsl:stylesheet>
