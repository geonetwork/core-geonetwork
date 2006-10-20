<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fo="http://www.w3.org/1999/XSL/Format">

	<xsl:include href="main.xsl"/>

	<!--
	additional scripts
	-->
	<xsl:template mode="script" match="/">
		<script type="text/javascript" language="JavaScript1.2">
			function processSubmit()
			{
				if (isWhitespace(document.feedbackf.name.value) || isWhitespace(document.feedbackf.org.value))
				{
					alert("Please fill in a Name or Organization");
				}
				else if (!isEmail(document.feedbackf.email.value))
				{
					 alert("Please fill correct E-mail Address");
				}
				else
				{
					goSubmit('feedbackf');
				}
			}
		</script>
	</xsl:template>
	
	<!--
	page content
	-->
	<xsl:template name="content">
		<xsl:call-template name="formLayout">
			<xsl:with-param name="title" select="/root/gui/strings/download"/>
			<xsl:with-param name="content">
				<form name="feedbackf" accept-charset="UTF-8" action="{/root/gui/locService}/file.download" method="post">
					<input type="hidden" name="id" value="{/root/response/id}"/>
					<input type="hidden" name="fname" value="{/root/response/fname}"/>
					<table>
						<tr class="padded"><td colspan="2">
							<h2><xsl:value-of select="/root/gui/strings/copyright1"/></h2>
							<xsl:copy-of select="/root/gui/strings/copyright2"/>
							<p/>
							<xsl:value-of select="/root/gui/strings/copyright3"/>
							<p/>
							<xsl:copy-of select="/root/gui/strings/feedbackTopics"/>
							<h2><xsl:value-of select="/root/gui/strings/disclaimer1"/></h2>
							<xsl:value-of select="/root/gui/strings/disclaimer2"/>
						</td></tr>
						<tr>
							<th class="padded"><xsl:value-of select="/root/gui/strings/name"/></th>
							<td class="padded"><input class="content" type="text" name="name" size="60"/></td>
						</tr>
						<tr>
							<th class="padded"><xsl:value-of select="/root/gui/strings/organisation"/></th>
							<td class="padded"><input class="content" type="text" name="org" size="60"/></td>
						</tr>
						<tr>
							<th class="padded"><xsl:value-of select="/root/gui/strings/emailAddress"/></th>
							<td class="padded"><input class="content" type="text" name="email" size="60"/></td>
						</tr>
						<tr>
							<th class="padded" valign="top"><xsl:value-of select="/root/gui/strings/feedbackComments"/></th>
							<td class="padded"><textarea class="content" name="comments" cols="60" rows="6" wrap="soft"></textarea></td>
						</tr>
					</table>
				</form>
			</xsl:with-param>
			<xsl:with-param name="buttons">
				<button class="content" onclick="goReset('feedbackf')"><xsl:value-of select="/root/gui/strings/reset"/></button>
				&#160;
				<button class="content" onclick="load('{/root/gui/locService}/metadata.show?id={/root/response/id}')"><xsl:value-of select="/root/gui/strings/back"/></button>
				&#160;
				<button class="content" onclick="processSubmit()"><xsl:value-of select="/root/gui/strings/accept"/></button>
			</xsl:with-param>
		</xsl:call-template>
	</xsl:template>
	
</xsl:stylesheet>
