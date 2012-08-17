<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:java="java:org.fao.geonet.util.XslUtil"
	exclude-result-prefixes="#all">

	<xsl:include href="main.xsl" />

	<xsl:template name="content">
		<xsl:choose>
			<xsl:when test="string(/root/gui/session/userId)!='' and java:isCasEnabled()">
				<script type="text/javascript">
					window.close();
				</script>
			</xsl:when>
			<xsl:when test="string(/root/gui/session/userId)!=''">
				<script type="text/javascript">
					window.location = '<xsl:value-of select="/root/gui/url"/>';
				</script>
			</xsl:when>
			<xsl:otherwise>
	<form name="login" action="{/root/gui/url}/j_spring_security_check"
		method="post">
		<xsl:call-template name="formLayout">
			<xsl:with-param name="title" select="/root/gui/info/heading" />
			<xsl:with-param name="content">
				<table align="left" width="100%">
					<xsl:if test="string(/root/gui/env/shib/use)='true'">
						<tr>
							<td>
								<h1>
									<xsl:value-of select="/root/gui/strings/shibLogin" />
								</h1>
								</td><td>
								<a class="banner" href="{/root/gui/env/shib/path}">
									<xsl:value-of select="/root/gui/strings/login" />
								</a>
							</td>
						</tr>
					</xsl:if>
					<input type="submit" style="display: none;" />
					<tr>
						<td>
							<h1><xsl:value-of select="/root/gui/strings/username" /></h1>
							</td><td>
							<input class="banner" type="text" id="username" name="username"
								size="10" onkeypress="return entSub('login')" />
						</td>
					</tr>
					<tr>
						<td>
							<h1><xsl:value-of select="/root/gui/strings/password" /></h1>
							</td><td>
							<input class="banner" type="password" id="password" name="password"
								size="10" onkeypress="return entSub('login')" />
						</td>
					</tr>
				</table>
				</xsl:with-param>
				<xsl:with-param name="buttons">
					<button class="banner" onclick="goSubmit('login')">
						<xsl:value-of select="/root/gui/strings/login" />
					</button>
				</xsl:with-param>
		</xsl:call-template>

	</form>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

</xsl:stylesheet>
