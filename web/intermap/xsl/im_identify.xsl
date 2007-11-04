<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:gml="http://www.opengis.net/gml" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:output method="html"/>
	
	<xsl:template match="/">
		<xsl:choose>
			<xsl:when test="/root/response/service/response/html">
				<xsl:apply-templates select="/root/response/service/response/html"/>
			</xsl:when>
			<xsl:when test="/root/response/service/response/url">
				<xsl:apply-templates select="/root/response/service/response/url"/>
			</xsl:when>
			<xsl:when test="/root/response/service/response/gml or /root/response/service[@type='ArcIMS']/response/ARCXML">
				<xsl:apply-templates select="/root/response/service[@type='WMS']/response/gml | /root/response/service[@type='ArcIMS']/response/ARCXML"/>
			</xsl:when>
			<xsl:when test="/root/response/service/response/text">
				<xsl:apply-templates select="/root/response/service[@type='WMS']/response/text"/>
			</xsl:when>
		</xsl:choose>
	</xsl:template>
	
	<!-- html response -->
	<xsl:template match="/root/response/service/response/html">
		<xsl:copy-of select="."/>
	</xsl:template>
	
	<!-- url response -->
	<xsl:template match="/root/response/service/response/url">
		<xsl:variable name="url"><xsl:value-of select="/root/response/service/response/url"/></xsl:variable>
		
		<html>
			<head>
				<title>Redirecting...</title>
				<meta http-equiv="refresh" content="0; url='{$url}'"></meta>			
			</head>
			<body>
				<font size="-1">
					Redirecting to <a href="{$url}"><xsl:value-of select="$url"/></a>...
				</font>
			</body>
		</html>
		
	</xsl:template>
		
	<!-- text response -->
	<xsl:template match="/root/response/service/response/text">
		<html>
			<head>
				<title>
					<xsl:value-of select="/root/gui/strings/title"/>
				</title>
				<link href="../../intermap.css" rel="stylesheet" type="text/css"/>
			</head>
			<body onLoad="javascript:window.focus();">
				<xsl:value-of select="." disable-output-escaping="yes"/>
			</body>
		</html>
	</xsl:template>
	
	<!-- gml response -->
	<xsl:template match="/root/response/service[@type='WMS']/response/gml | /root/response/service[@type='ArcIMS']/response/ARCXML">
		<html>
			<head>
				<title>
					<xsl:value-of select="/root/gui/strings/title"/>
				</title>
				<link href="../../intermap.css" rel="stylesheet" type="text/css"/>
				<script language="javascript">
//&gt;!--
					function popNew(url) {
						window.open(url, "");
					}
//&lt;--
				</script>
			</head>
			<body onLoad="javascript:window.focus();">
				<xsl:if test="/root/response/service[@type='ArcIMS']">
					<xsl:variable name="n">
						<xsl:value-of select="count(/root/response/service/response/ARCXML/RESPONSE//FEATURES/FEATURE/FIELDS)"/>
					</xsl:variable>
					<xsl:choose>
						<xsl:when test="$n='0'">
							<b>
								<xsl:value-of select="/root/gui/strings/noFeature"/>
							</b>
						</xsl:when>
						<xsl:when test="$n='1'">
							<table class="identify">
								<tr>
									<td colspan="2" class="featuresCount">
										<xsl:value-of select="/root/gui/strings/oneFeature"/>
									</td>
								</tr>
								<xsl:apply-templates select="/root/response/service/response/ARCXML/RESPONSE/FEATURES"/>
							</table>
						</xsl:when>
						<xsl:otherwise>
							<table class="identify">
								<tr>
									<td colspan="2" class="featuresCount">
										<xsl:value-of select="$n"/>
										<xsl:text> </xsl:text>
										<xsl:value-of select="/root/gui/strings/featuresfound"/>
									</td>
								</tr>
								<xsl:apply-templates select="/root/response/service[@type='ArcIMS']/response/ARCXML/RESPONSE/FEATURES/FEATURE"/>
							</table>
						</xsl:otherwise>
					</xsl:choose>
				</xsl:if>
				<xsl:if test="/root/response/service[@type='WMS']">
					<xsl:apply-templates select="/root/response/service[@type='WMS']/response"/>
				</xsl:if>
			</body>
		</html>
	</xsl:template>
	
	<!-- WMS services -->
	<xsl:template match="/root/response/service[@type='WMS']/response">
		<xsl:apply-templates mode="copy"/>
	</xsl:template>
	<xsl:template match="*" mode="copy">
		<h1>
			<xsl:value-of select="/root/gui/strings/featuresfound"/>
		</h1>
		<table align="left" border="1" cellpadding="0" cellspacing="1" width="400">
			<xsl:apply-templates select="gml:featureMember"/>
		</table>
	</xsl:template>
	<xsl:template match="gml:featureMember">
		<xsl:if test="position() mod 2 &gt; 0">
			<xsl:for-each select="./child::*">
				<font face="Helvetica, Arial, sans-serif">
					<xsl:for-each select="./child::*">
						<tr>
							<td bgcolor="#82A8F6">
								<xsl:value-of select="name( . )"/>
							</td>
							<td bgcolor="#C0D2F5">
								<xsl:value-of select="."/>
							</td>
						</tr>
					</xsl:for-each>
				</font>
			</xsl:for-each>
		</xsl:if>
		<xsl:if test="position() mod 2 = 0">
			<xsl:for-each select="./child::*">
				<font face="Helvetica, Arial, sans-serif">
					<xsl:for-each select="./child::*">
						<tr>
							<td bgcolor="#D5D5D5">
								<xsl:value-of select="name( . )"/>
							</td>
							<td bgcolor="#EAEAEA">
								<xsl:value-of select="."/>
							</td>
						</tr>
					</xsl:for-each>
				</font>
			</xsl:for-each>
		</xsl:if>
	</xsl:template>
	
	<!-- ArcIMS services -->
	<xsl:template match="/root/response/service[@type='ArcIMS']/response/ARCXML/RESPONSE/FEATURES_XXX">
		<table border="1">
			<tr>
				<xsl:for-each select="FEATURE[1]/FIELDS/FIELD">
					<td>
						<b>
							<xsl:value-of select="@name"/>
						</b>
					</td>
				</xsl:for-each>
			</tr>
			<xsl:for-each select="FEATURE/FIELDS">
				<tr>
					<xsl:for-each select="FIELD">
						<td>
							<xsl:value-of select="@value"/>
						</td>
					</xsl:for-each>
				</tr>
			</xsl:for-each>
		</table>
	</xsl:template>
	
	<!-- ArcIMS services -->
	<xsl:template match="/root/response/service[@type='ArcIMS']/response/ARCXML/RESPONSE/FEATURES/FEATURE">
		<tr>
			<td class="identify_first"><b>Name</b></td>
			<td class="identify_first"><b>Value</b></td>
		</tr>
		<xsl:for-each select="FIELDS/FIELD">
			<xsl:variable name="tdClass">
				<xsl:if test="position() mod 2 &gt; 0">identify_1</xsl:if>
				<xsl:if test="position() mod 2 = 0">identify_2</xsl:if>
			</xsl:variable>
			<xsl:if test="position() &lt; last()">
				<tr>
					<td class="{$tdClass}">
						<xsl:choose>
							<xsl:when test="starts-with(@name,'LINK')">
								<xsl:value-of select="substring(@name, 7)" />
							</xsl:when>
							<xsl:otherwise>
								<xsl:value-of select="@name" />
							</xsl:otherwise>
						</xsl:choose>
					</td>
					<td class="{$tdClass}">
						<xsl:choose>
							<xsl:when test="starts-with(@name,'LINK')">
								<a class="geoLink" href="javascript:popNew('{@value}')"><xsl:value-of select="@value" /></a>
							</xsl:when>
							<xsl:otherwise>
								<xsl:value-of select="@value" />
							</xsl:otherwise>
						</xsl:choose>
					</td>
				</tr>
			</xsl:if>
			<xsl:if test="position() = last()">
				<tr>
					<td class="{$tdClass}_last">
						<xsl:choose>
							<xsl:when test="starts-with(@name,'LINK')">
								<xsl:value-of select="substring(@name, 7)" />
							</xsl:when>
							<xsl:otherwise>
								<xsl:value-of select="@name" />
							</xsl:otherwise>
						</xsl:choose>
					</td>
					<td class="{$tdClass}_last">
						<xsl:choose>
							<xsl:when test="starts-with(@name,'LINK')">
								<a class="geoLink" href="javascript:popNew('{@value}')"><xsl:value-of select="@value" /></a>
							</xsl:when>
							<xsl:otherwise>
								<xsl:value-of select="@value" />
							</xsl:otherwise>
						</xsl:choose>
					</td>
				</tr>
			</xsl:if>
		</xsl:for-each>
		<tr>
			<td><br /><br /></td>
			<td><br /></td>
		</tr>
	</xsl:template>
</xsl:stylesheet>
