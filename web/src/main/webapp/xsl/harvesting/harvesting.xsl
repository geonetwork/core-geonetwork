<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	
	<!-- ============================================================================================= -->

	<xsl:include href="../main.xsl"/>
	<xsl:include href="buttons.xsl"/>
	<xsl:include href="geonet/geonetwork.xsl"/>
	<xsl:include href="geonet20/geonetwork.xsl"/>
	<xsl:include href="geoPREST/geoPREST.xsl"/>
	<xsl:include href="webdav/webdav.xsl"/>
	<xsl:include href="csw/csw.xsl"/>
	<xsl:include href="ogcwxs/ogcwxs.xsl"/>
	<xsl:include href="z3950/z3950.xsl"/>
	<xsl:include href="z3950Config/z3950Config.xsl"/>
	<xsl:include href="oaipmh/oaipmh.xsl"/>
	<xsl:include href="arcsde/arcsde.xsl"/>
	<xsl:include href="thredds/thredds.xsl"/>
	<xsl:include href="wfsfeatures/wfsfeatures.xsl"/>
	<xsl:include href="filesystem/filesystem.xsl"/>

	<!-- ============================================================================================= -->


	<xsl:variable name="widgetPath">../../apps</xsl:variable>
			
			
	<xsl:template mode="script" match="/">
        
        <link rel="stylesheet" type="text/css" href="../../scripts/ext/resources/css/ext-all.css"/>
        
        <script type="text/javascript" src="{/root/gui/url}/scripts/ext/adapter/ext/ext-base.js"/>
        <script type="text/javascript" src="{/root/gui/url}/scripts/ext/ext-all.js"></script>
		<script type="text/javascript" src="{/root/gui/url}/static/kernel.js"/>
		<script type="text/javascript" src="{/root/gui/url}/scripts/editor/metadata-editor.js"/>
		<script type="text/javascript" src="{/root/gui/url}/scripts/core/gui/gui.js"/>
		<script type="text/javascript" src="{/root/gui/url}/scripts/sarissa.js"/>
		<script type="text/javascript" src="{/root/gui/url}/scripts/harvesting/harvesting.js"/>

		<script type="text/javascript" src="{/root/gui/url}/scripts/openlayers/OpenLayers.js"/>
		<script type="text/javascript" src="{/root/gui/url}/scripts/openlayers/lib/OpenLayers/Format/CSWGetRecords/v2_0_2.js"/>
		
	</xsl:template>

	<!-- ============================================================================================= -->
	<!-- === page content -->
	<!-- ============================================================================================= -->

	<xsl:template name="content">
		<xsl:call-template name="formLayout">
			<xsl:with-param name="title" select="/root/gui/strings/harvestingManagement"/>

			<xsl:with-param name="content">
				<div id="listPanel" style="display:none;"><xsl:call-template name="listPanel"/></div>
				<div id="addPanel"  style="display:none;"><xsl:call-template name="addPanel"/> </div>
				<div id="editPanel" style="display:none;"><xsl:call-template name="editPanel"/></div>
				<div id="notifPanel" style="display:none;"><xsl:call-template name="notifPanel"/></div>
			</xsl:with-param>

			<xsl:with-param name="buttons">
				<div id="listButtons" style="display:none;"><xsl:call-template name="listButtons"/></div>
				<div id="addButtons"  style="display:none;"><xsl:call-template name="addButtons"/> </div>
				<div id="editButtons" style="display:none;"><xsl:call-template name="editButtons"/></div>
				<div id="notifButtons" style="display:none;"><xsl:call-template name="notifButtons"/></div>
				<div id="messages"></div>
			</xsl:with-param>
		</xsl:call-template>
	</xsl:template>
	
	<!-- ============================================================================================= -->
	<!-- === listPanel -->
	<!-- ============================================================================================= -->

	<xsl:template name="listPanel">
	  <div style="max-height:600px;overflow:auto;">
		<table id="table">
			<tr>
				<th class="padded" style="width:40px;"><b><xsl:value-of select="/root/gui/harvesting/select"/></b></th>
				<th class="padded" style="width:80px;"><b><xsl:value-of select="/root/gui/harvesting/name"/></b></th>
				<th class="padded" style="width:60px;"><b><xsl:value-of select="/root/gui/harvesting/type"/></b></th>
				<th class="padded" style="width:40px;" align="center"><b><xsl:value-of select="/root/gui/harvesting/status"/></b></th>
				<th class="padded" style="width:40px;" align="center"><b><xsl:value-of select="/root/gui/harvesting/errors"/></b></th>
				<th class="padded" style="width:60px;"><b><xsl:value-of select="/root/gui/harvesting/at"/></b></th>
				<th class="padded" style="width:60px;"><b><xsl:value-of select="/root/gui/harvesting/every"/></b></th>
				<th class="padded" style="width:80px;"><b><xsl:value-of select="/root/gui/harvesting/lastRun"/></b></th>
				<th class="padded" style="width:60px;"><b><xsl:value-of select="/root/gui/harvesting/operation"/></b></th>
			</tr>
		</table>
	  </div>
	</xsl:template>

	<!-- ============================================================================================= -->
	<!-- === addPanel -->
	<!-- ============================================================================================= -->

	<xsl:template name="addPanel">
		<table>
			<tr>
				<th class="padded"><xsl:value-of select="/root/gui/harvesting/type"/></th>
				<td class="padded">
					<select id="add.type" class="content" name="type" size="1"/>
				</td>
			</tr>
		</table>
	</xsl:template>

	<!-- ============================================================================================= -->
	<!-- === editPanel -->
	<!-- ============================================================================================= -->

	<xsl:template name="editPanel">
    <table class="text-aligned-left">
    <tr>
        <td>
		<xsl:call-template name="editPanel-GN"/>
		<xsl:call-template name="editPanel-WD"/>
		<xsl:call-template name="editPanel-GN20"/>
		<xsl:call-template name="editPanel-CSW"/>
		<xsl:call-template name="editPanel-geoPREST"/>
		<xsl:call-template name="editPanel-OGCWXS"/>
		<xsl:call-template name="editPanel-thredds"/>
		<xsl:call-template name="editPanel-wfsfeatures"/>
    <xsl:call-template name="editPanel-Z3950"/>
    <xsl:call-template name="editPanel-Z3950Config"/>
		<xsl:call-template name="editPanel-OAI"/>
		<xsl:call-template name="editPanel-Arcsde"/>
		<xsl:call-template name="editPanel-Filesystem"/>
        </td>
    </tr>
    </table>
    </xsl:template>	
    
	<!-- ============================================================================================= -->
	<!-- === notifPanel -->
	<!-- ============================================================================================= -->

	<xsl:template name="notifPanel">
	  <div style="max-height:600px;overflow:auto;">
		<table id="notifTable">
			<tr>
				<td class="padded" style="width:250px;" colspan="2">
					<b><xsl:value-of select="/root/gui/harvesting/enableMail"/>:&#160;&#160;&#160;</b>
					<input id="enableMail"  class="content" type="checkbox">
						<xsl:variable name="enabled" select="/root/gui/env/harvesting/mail/enabled"/>
						<xsl:if test="normalize-space($enabled) = 'true'">
							<xsl:attribute name="checked">true</xsl:attribute>
						</xsl:if>
					</input>
				</td>
			</tr>
			<tr>
				<td class="padded" style="width:80px;">
					<b><xsl:value-of select="/root/gui/harvesting/level"/></b>
					</td>
				<td class="padded" style="width:480px;">
					<label for="level1"><xsl:value-of select="/root/gui/harvesting/level1"/></label>
					<input id="level1" name="level1" class="content" type="checkbox">
						<xsl:variable name="enabled" select="/root/gui/env/harvesting/mail/level1"/>
						<xsl:if test="normalize-space($enabled) = 'true'">
							<xsl:attribute name="checked">true</xsl:attribute>
						</xsl:if>
					</input>&#160;&#160;&#160;&#160;&#160;&#160;
					<label for="level2"><xsl:value-of select="/root/gui/harvesting/level2"/></label>
					<input id="level2" name="level2" class="content" type="checkbox">
						<xsl:variable name="enabled" select="/root/gui/env/harvesting/mail/level2"/>
						<xsl:if test="normalize-space($enabled) = 'true'">
							<xsl:attribute name="checked">true</xsl:attribute>
						</xsl:if>
					</input>&#160;&#160;&#160;&#160;&#160;&#160;
					<label for="level3"><xsl:value-of select="/root/gui/harvesting/level3"/></label>
					<input id="level3" name="level3" class="content" type="checkbox">
						<xsl:variable name="enabled" select="/root/gui/env/harvesting/mail/level3"/>
						<xsl:if test="normalize-space($enabled) = 'true'">
							<xsl:attribute name="checked">true</xsl:attribute>
						</xsl:if>
					</input>
				</td>
			</tr>
			<tr>
				<td class="padded" style="width:80px;">
					<b><xsl:value-of select="/root/gui/harvesting/recipients"/>:</b>
				</td>
				<td class="padded" style="width:480px;">
					<input type="text"  class="content" id="emails_" style="width:600px;" required="true"/>
					<div style="float:right;width:70px">
						<button style="width:70px;" type="button" class="content" onclick="javascript:harvesting.addMail();">
							<xsl:value-of select="/root/gui/harvesting/add"/>
						</button>
						<button style="width:70px;" type="button" class="content" onclick="javascript:harvesting.removeMail();">
							<xsl:value-of select="/root/gui/harvesting/remove"/>
						</button>
					</div>
				</td>
				<td class="padded" style="width:250px">
					<select id="emails" class="content" multiple="true" style="width:220px;height:45px;">
						<xsl:for-each select="/root/gui/env/harvesting/mail/recipient">
							<xsl:for-each select="tokenize(.,',')">
								<xsl:variable name="valopt"><xsl:value-of select="."/></xsl:variable>
								<xsl:if test="$valopt!= ''">
									<option><xsl:attribute name="value"><xsl:value-of select="$valopt"/></xsl:attribute><xsl:value-of select="."/></option>
								</xsl:if>
							</xsl:for-each>
						</xsl:for-each>
					</select>
				</td>
			</tr><tr>
				<td class="padded" style="width:80px;">
					<b><xsl:value-of select="/root/gui/harvesting/subject"/>:</b>
				</td>
				<td class="padded" style="width:520px;">
					<input type="text"  class="content" id="subject" size="100" required="true">
						<xsl:attribute name="value">
							<xsl:variable name="tmp"><xsl:value-of select="/root/gui/env/harvesting/mail/subject"/></xsl:variable>
							<xsl:choose>
								<xsl:when test="normalize-space($tmp) != ''"><xsl:value-of select="$tmp"/></xsl:when>
								<xsl:otherwise><xsl:value-of select="/root/gui/harvesting/defaultSubject"/></xsl:otherwise>
							</xsl:choose>
						</xsl:attribute>
					</input>
				</td>
				<td class="padded" style="width:250px;" rowspan="4">
					<p><xsl:value-of select="/root/gui/harvesting/helpTemplateMail/help"/></p>
					<ul style="list-style-type:circle; padding-left: 20px">
					<li><xsl:value-of select="/root/gui/harvesting/helpTemplateMail/total"/>$$total$$</li>
					<li><xsl:value-of select="/root/gui/harvesting/helpTemplateMail/added"/>$$added$$</li>
					<li><xsl:value-of select="/root/gui/harvesting/helpTemplateMail/updated"/>$$updated$$</li>
					<li><xsl:value-of select="/root/gui/harvesting/helpTemplateMail/unchanged"/>$$unchanged$$</li>
					<li><xsl:value-of select="/root/gui/harvesting/helpTemplateMail/unretrievable"/>$$unretrievable$$</li>
					<li><xsl:value-of select="/root/gui/harvesting/helpTemplateMail/removed"/>$$removed$$</li>
					<li><xsl:value-of select="/root/gui/harvesting/helpTemplateMail/doesNotValidate"/>$$doesNotValidate$$</li>
					<li><xsl:value-of select="/root/gui/harvesting/helpTemplateMail/harvesterName"/>$$harvesterName$$</li>
					<li><xsl:value-of select="/root/gui/harvesting/helpTemplateMail/harvesterType"/>$$harvesterType$$</li>
					<li><xsl:value-of select="/root/gui/harvesting/helpTemplateMail/errorMsg"/>$$errorMsg$$</li>
					</ul>
				</td>
			</tr>
			<tr>
				<td class="padded" style="width:80px;">
					<b><xsl:value-of select="/root/gui/harvesting/template"/>:</b>
				</td>
				<td class="padded" style="width:520px;">
					<textarea id="template"  class="content" rows="5" cols="100" required="true">
						<xsl:variable name="tmp"><xsl:value-of select="/root/gui/env/harvesting/mail/template"/></xsl:variable>
						<xsl:choose>
							<xsl:when test="normalize-space($tmp) != ''"><xsl:value-of select="$tmp"/></xsl:when>
							<xsl:otherwise><xsl:value-of select="/root/gui/harvesting/defaultTemplate"/></xsl:otherwise>
						</xsl:choose>
					</textarea>
				</td>
			</tr>
			<tr>
				<td class="padded" style="width:80px;">
					<b><xsl:value-of select="/root/gui/harvesting/templateWarning"/>:</b>
				</td>
				<td class="padded" style="width:520px;">
					<textarea id="templateWarning"  class="content" rows="5" cols="100" required="true">
						<xsl:variable name="tmp"><xsl:value-of select="/root/gui/env/harvesting/mail/templateWarning"/></xsl:variable>
						<xsl:choose>
							<xsl:when test="normalize-space($tmp) != ''"><xsl:value-of select="$tmp"/></xsl:when>
							<xsl:otherwise><xsl:value-of select="/root/gui/harvesting/defaultTemplateWarning"/></xsl:otherwise>
						</xsl:choose>
					</textarea>
				</td>
			</tr>
			<tr>
				<td class="padded" style="width:80px;">
					<b><xsl:value-of select="/root/gui/harvesting/templateError"/>:</b>
				</td>
				<td class="padded" style="width:520px;">
					<textarea id="templateError"  class="content" rows="5" cols="100" required="true">
						<xsl:variable name="tmp"><xsl:value-of select="/root/gui/env/harvesting/mail/templateError"/></xsl:variable>
						<xsl:choose>
							<xsl:when test="normalize-space($tmp) != ''"><xsl:value-of select="$tmp"/></xsl:when>
							<xsl:otherwise><xsl:value-of select="/root/gui/harvesting/defaultTemplateError"/></xsl:otherwise>
						</xsl:choose>
					</textarea>
				</td>
			</tr>
		</table>
	  </div>
	</xsl:template>

	<xsl:template name="privileges">
		<xsl:param name="type"/>
		<xsl:param name="jsId" required="no" select="$type"/>
		
		<h1 align="left"><xsl:value-of select="/root/gui/harvesting/privileges"/></h1>
		
		<table>
			<tr>
				<td class="padded" valign="top"><xsl:value-of select="/root/gui/harvesting/groups"/></td>
				<td class="padded"><select id="{$type}.groups" class="content" size="8" multiple="on"/></td>
				<td class="padded" valign="top">
					<div align="center">
						<button id="{$type}.addGroups" class="content" onclick="harvesting.{$jsId}.addGroupRow()">
							<xsl:value-of select="/root/gui/harvesting/add"/>
						</button>
					</div>
				</td>
			</tr>
		</table>
		
		<table id="{$type}.privileges">
			<tr>
				<th class="padded"><b><xsl:value-of select="/root/gui/harvesting/group"/></b></th>
				<th class="padded"><b><xsl:value-of select="/root/gui/harvesting/oper/op[@id='0']"/></b></th>
				<th class="padded"><b><xsl:value-of select="/root/gui/harvesting/oper/op[@id='5']"/></b></th>
				<th class="padded"><b><xsl:value-of select="/root/gui/harvesting/oper/op[@id='6']"/></b></th>
				<th/>
			</tr>
		</table>
		
	</xsl:template>

	<!-- Create elements to define username and password field. -->
	<xsl:template name="useAccount">
		<xsl:param name="type"/>
		
		<tr>
			<td class="padded"><label for="{$type}.useAccount"><xsl:value-of select="/root/gui/harvesting/useAccount"/></label></td>
			<td class="padded"><input id="{$type}.useAccount" type="checkbox" checked="on"/></td>
		</tr>
		<tr>
			<td/>
			<td>
				<table id="{$type}.account">
					<tr>
						<td class="padded"><label for="{$type}.username"><xsl:value-of select="/root/gui/harvesting/username"/></label></td>
						<td class="padded"><input id="{$type}.username" class="content" type="text" value="" size="20"/></td>
					</tr>
					
					<tr>
						<td class="padded"><label for="{$type}.password"><xsl:value-of select="/root/gui/harvesting/password"/></label></td>
						<td class="padded"><input id="{$type}.password" class="content" type="password" value="" size="20"/></td>
					</tr>
				</table>
			</td>
		</tr>
		
	</xsl:template>
	<!-- ============================================================================================= -->
	
</xsl:stylesheet>
