<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	
	<xsl:include href="main.xsl"/>
	
	<!--
	additional scripts
	-->
	<xsl:template mode="script" match="/">
		<script type="text/javascript" src="{/root/gui/url}/scripts/form_check.js"></script>
		
		<script type="text/javascript" language="JavaScript1.2">
			function processSubmit()
			{
				if (isWhitespace(document.feedbackf.name.value) || isWhitespace(document.feedbackf.org.value))
				{
					alert(translate("addName"));
				}
				else if (!isEmail(document.feedbackf.email.value))
				{
					 alert(translate("checkEmail"));
				}
				else
				{				
					if (isWhitespace(document.feedbackf.comments.value)) {
						document.feedbackf.comments.value = translate('noComment');
					}
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
			<xsl:with-param name="title" select="/root/gui/strings/feedbackCardTitle"/>
			<xsl:with-param name="content">
				<xsl:copy-of select="/root/gui/strings/feedbackTopics"/>
				<p/>
				<form name="feedbackf" accept-charset="UTF-8" action="{/root/gui/locService}/feedback.insert" method="post">
					<input type="submit" style="display: none;" />
					<input type="hidden" name="id" value="{/root/response/id}"/>
					<input type="hidden" name="fname" value="{/root/response/fname}"/>
					<table>
						<tr>
							<th class="padded"><xsl:value-of select="/root/gui/strings/name"/></th>
							<td class="padded"><input class="content" type="text" name="name" size="60"/></td>
						</tr>
						<tr>
							<th class="padded"><xsl:value-of select="/root/gui/strings/organisation"/></th>
							<td class="padded"><input class="content" type="text" name="org" size="60"/></td>
						</tr>
						<tr>
							<th class="padded"><xsl:value-of select="/root/gui/strings/email"/></th>
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
				<div align="center">
					<button class="content" onclick="goReset('feedbackf')"><xsl:value-of select="/root/gui/strings/reset"/></button>
					&#160;
					<button class="content" onclick="processSubmit()"><xsl:value-of select="/root/gui/strings/accept"/></button>
				</div>
			</xsl:with-param>
		</xsl:call-template>
	</xsl:template>
		
</xsl:stylesheet>
