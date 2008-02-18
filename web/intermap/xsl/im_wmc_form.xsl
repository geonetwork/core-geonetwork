<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:output method="html" />

	<xsl:template match="/">
		<!-- =========================================== -->
		<!--   USER MESSAGES -->
		<!-- =========================================== -->
		<div id="im_wmc_msg">
			<div id="im_wmc_msg_upload_start" style="display:none;">
				The context file is uploading... 
			</div>
			<div id="im_wmc_msg_upload_ok" style="display:none;">
				The context has been uploaded 
			</div>
			<div id="im_wmc_msg_upload_error" style="display:none;">
				An unexpected error has been encountered while uploading the context:<br/>				
			</div>
			
			<div id="im_wmc_msg_mail_start" style="display:none;">
				The mail is being build...
			</div>
			<div id="im_wmc_msg_mail_ok" style="display:none;">
				The mail has been successfully sent
			</div>
			<div id="im_wmc_msg_mail_error" style="display:none;">
				An unexpected error has been encountered while processing the mail:<br/>
			</div>						
		</div>

		<!-- =========================================== -->
		<!--   CHOOSE FORM -->
		<!-- =========================================== -->
		<div id="im_wmc_form"> 			
			<xsl:choose>
				<xsl:when test="/root/request/type='mail'">
					<xsl:call-template name="mail"/>				
				</xsl:when>
				<xsl:when test="/root/request/type='upload'">
					<xsl:call-template name="upload"/>								
				</xsl:when>
				<xsl:otherwise>
					<xsl:call-template name="main"/>												
				</xsl:otherwise>
			</xsl:choose>
		</div>

	</xsl:template>
	
	
	<xsl:template name="main">
		<ul>
			<!--   DOWNLOAD WMC -->
			<li>
				<a onClick="im_downloadWMC();">Download WMC</a>					
			</li>
			
			<!--   UPLOAD WMC -->
			<li>
				<a onClick="$('im_uploadwmc').toggle();">Upload WMC...</a>
				<div id="im_uploadwmc" style="display:none;">
					<xsl:call-template name="upload"/>				
				</div>
			</li>
			
			<!--   SEND WMC AS E-MAIL  -->
			<li>
				<a onClick="$('im_mailwmc').toggle();">Send WMC as email...</a>
				<div id="im_mailwmc" style="display:none;">
					<xsl:call-template name="mail"/>
				</div>
			</li>				
		</ul>										
	</xsl:template>
	
	<!-- =========================================== -->
	<!--   UPLOAD WMC -->
	<!-- =========================================== -->
	<xsl:template name="upload">
		<form method="post" enctype="multipart/form-data"  id="im_fuploadwmc" action="/intermap/srv/en/wmc.setContext"> <!--onsubmit="im_uploadWMC(false);">-->
			<input type="hidden" id="im_fup_clearLayers" name="clearLayers" value="false"></input>
			<table width="100%">
				<tr>
					<td colspan="2" align="center">
						<input type="file" name="fname" id="fname" size="60"/>
					</td>
				</tr>
				
				<tr>
					<td align="center">
						<button onClick="im_uploadWMC(true);" style="margin-bottom:5px;margin-top:5px;">Replace current layers</button>
					</td>
					<td align="center">
						<button onClick="im_uploadWMC(false);" style="margin-bottom:5px;margin-top:5px;">Add to current layers</button>
					</td>
				</tr>						
			</table>
		</form>
	</xsl:template>
	
	<!-- =========================================== -->
	<!--   SEND WMS AS E-MAIL  -->
	<!-- =========================================== -->
	<xsl:template name="mail">
		<table width="100%">
			<tr>
				<td>To:</td>
				<td>
					<input type="text" name="wmc_mailto" id="wmc_mailto" width="100%" size="40"></input>									
				</td>
			</tr>
			<tr>
				<td>From:</td>
				<td>
					<input type="text" name="wmc_mailfrom" id="wmc_mailfrom" width="100%" size="40"></input>									
				</td>
			</tr>
			<tr>
				<td>Title:</td>
				<td width="100%">
					<input type="text" name="wmc_title" id="wmc_title" width="100%" size="70"></input>									
				</td>
			</tr>
			<!-- This requires a captcha to be implemented first to prevent spamming -->
<!--			<tr>
				<td>Comment:</td>
				<td>
					<textarea name="wmc_comment" id="wmc_comment" rows="5" cols="40"></textarea>									
				</td>
			</tr> -->
			<tr style="padding-bottom:5px;">
				<td colspan="2" align="center">
					<button onClick="im_sendWMC();" style="margin-bottom:5px;margin-top:5px;">Send e-mail</button>											
				</td>
			</tr>
			
		</table>			
	</xsl:template>

</xsl:stylesheet>
