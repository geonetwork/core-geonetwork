<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:geonet="http://www.fao.org/geonetwork">

	<!--
	edit metadata form
	-->
	<xsl:include href="main.xsl"/>
	<xsl:include href="metadata.xsl"/>

	<!--
	additional scripts
	-->
	<xsl:template mode="script" match="/">
		<script language="JavaScript1.2" type="text/javascript">

			function doActionInWindow(action)
			{
				// alert("In doAction(" + action + ")"); // DEBUG
				popWindow('about:blank');
				document.mainForm.action = action;
				oldTarget = document.mainForm.target;
				document.mainForm.target = 'popWindow';
				goSubmit('mainForm');
				document.mainForm.target = oldTarget;
			}

			function doAction(action)
			{
				// alert("In doAction(" + action + ")"); // DEBUG
				document.mainForm.action = action;
				goSubmit('mainForm');
			}

			function doTabAction(action, tab)
			{
				// alert("In doTabAction(" + action + ", " + tab + ")"); // DEBUG
				document.mainForm.currTab.value = tab;
				doAction(action);
			}

			function doElementAction(action, ref)
			{
				// alert("In doElementAction(" + action + ", " + ref + ")"); // DEBUG
				document.mainForm.ref.value = ref;
				doAction(action + '#_' + ref);
			}

			function doNewElementAction(action, ref, name)
			{
				// alert("In doNewElementAction(" + action + ", " + ref + ", " + name + ")"); // DEBUG
				document.mainForm.name.value = name;
				doElementAction(action, ref);
			}

			function doNewORElementAction(action, ref, name, child)
			{
				// alert("In doNewORElementAction(" + action + ", " + ref + ", " + name + ", " + child + ")"); // DEBUG
				document.mainForm.child.value = child;
				doNewElementAction(action, ref, name);
			}

			function doConfirm(action, message)
			{
				// alert("In doConfirm(" + action + ", " + message + ")"); // DEBUG
				if(confirm(message))
				{
					doAction(action);
					return true;
				}
				return false;
			}

			function doFileUploadAction(action, ref, fname, access)
			{
				// alert("In doFileUploadAction(" + action + ", " + ref + ", " + fname + ")"); // DEBUG

				if (fname.indexOf('/') > -1)
					fname = fname.substring(fname.lastIndexOf('/') + 1, fname.length);
				else
					fname = fname.substring(fname.lastIndexOf('\\') + 1, fname.length);
				
				document.mainForm.fname.value  = fname;
				document.mainForm.access.value = access;
				doElementAction(action, ref);
			}
			
			function doFileRemoveAction(action, ref, access)
			{
				// alert("In doFileRemoveAction(" + action + ", " + ref + ")"); // DEBUG
				document.mainForm.access.value = access;
				doElementAction(action, ref);
			}

			function setRegion(westField, eastField, southField, northField, choice)
			{
				// alert(westField.name + ", " + eastField.name + ", " + southField.name + ", " + northField.name + " set to " + choice); // FIXME
				
				if (choice != "")
				{
					coords = choice.split(";")
					westField.value  = coords[0];
					eastField.value  = coords[1];
					southField.value = coords[2];
					northField.value = coords[3];
				}
				else
				{
					westField.value  = "";
					eastField.value  = "";
					southField.value = "";
					northField.value = "";
				}
			}
			
		</script>
		<style type="text/css">@import url(<xsl:value-of select="/root/gui/url"/>/scripts/calendar/calendar-blue2.css);</style>
		<script type="text/javascript" src="{/root/gui/url}/scripts/calendar/calendar.js"></script>
		<script type="text/javascript" src="{/root/gui/url}/scripts/calendar/lang/calendar-en.js"></script>
		<script type="text/javascript" src="{/root/gui/url}/scripts/calendar/calendar-setup.js"></script>
	</xsl:template>
	
	<!--
	page content
	-->
	<xsl:template name="content">
		<table  width="100%" height="100%">
			<xsl:for-each select="/root/*[name(.)!='gui' and name(.)!='request']"> <!-- just one -->
				<tr height="100%">
					<td class="blue-content" width="150" valign="top">
						<xsl:call-template name="tab">
							<xsl:with-param name="tabLink" select="concat(/root/gui/locService,'/metadata.update')"/>
						</xsl:call-template>
					</td>
					<td class="content" valign="top">
						<form name="mainForm" accept-charset="UTF-8" method="POST" action="{/root/gui/locService}/metadata.update" enctype="multipart/form-data">
							<input type="hidden" name="id" value="{geonet:info/id}"/>
							<input type="hidden" name="version" value="{geonet:info/version}"/>
							<input type="hidden" name="ref"/>
							<input type="hidden" name="name"/>
							<input type="hidden" name="child"/>
							<input type="hidden" name="fname"/>
							<input type="hidden" name="access"/>
							<input type="hidden" name="currTab" value="{/root/gui/currTab}"/>

							<table width="100%">
								<tr><td class="padded-content" height="100%" align="center" valign="top">
									<xsl:call-template name="editButtons"/>
								</td></tr>
								<tr><td class="padded-content">
									<table class="md" width="100%">
										<xsl:choose>
											<xsl:when test="$currTab='xml'">
												<xsl:apply-templates mode="xmlDocument" select=".">
													<xsl:with-param name="edit" select="true()"/>
												</xsl:apply-templates>
											</xsl:when>
											<xsl:otherwise>
												<xsl:apply-templates mode="elementEP" select=".">
													<xsl:with-param name="edit" select="true()"/>
												</xsl:apply-templates>
											</xsl:otherwise>
										</xsl:choose>
									</table>
								</td></tr>
								<tr><td class="padded-content" height="100%" align="center" valign="top">
									<xsl:call-template name="editButtons"/>
								</td></tr>
								<tr><td class="padded-content" height="100%" align="center" valign="top">
									<xsl:call-template name="templateChoice"/>
								</td></tr>
							</table>
						</form>
					</td>
				</tr>
			</xsl:for-each>
			<tr><td class="blue-content" colspan="3"/></tr>
		</table>
	</xsl:template>
	
	<xsl:template name="editButtons" match="*">

		<!-- reset button -->
		<button class="content" onclick="goReset('mainForm')"><xsl:value-of select="/root/gui/strings/reset"/></button>
		
		<!-- save button -->
		&#160;
		<button class="content" onclick="doAction('{/root/gui/locService}/metadata.update')">
			<xsl:value-of select="/root/gui/strings/save"/>
		</button>
		
		<!-- save and close button -->
		&#160;
		<button class="content" onclick="doAction('{/root/gui/locService}/metadata.update.finish')">
			<xsl:value-of select="/root/gui/strings/saveAndClose"/>
		</button>
		
		<!-- validate button -->
		&#160;
		<button class="content" onclick="doActionInWindow('{/root/gui/locService}/metadata.update.validate');return false;">
			<xsl:value-of select="/root/gui/strings/saveAndValidate"/>
		</button>
		
		<!-- thumbnails -->
		<xsl:if test="string(geonet:info/schema)='iso19115' or string(geonet:info/schema)='iso19139'"> <!-- FIXME: should be more general -->
			&#160;
			<button class="content" onclick="doAction('{/root/gui/locService}/metadata.thumbnail.form')">
				<xsl:value-of select="/root/gui/strings/thumbnails"/>
			</button>
		</xsl:if>
		
		<!-- create button -->
		<xsl:if test="string(geonet:info/isTemplate)!='s' and (geonet:info/isTemplate='y' or geonet:info/source=/root/gui/env/site/siteId) and /root/gui/services/service/@name='metadata.duplicate.form'">
			&#160;
			<button class="content" onclick="load('{/root/gui/locService}/metadata.duplicate.form?id={geonet:info/id}')">
				<xsl:value-of select="/root/gui/strings/create"/>
			</button>
		</xsl:if>
		
		<!-- cancel button -->
		&#160;
		<button class="content" onclick="doAction('{/root/gui/locService}/metadata.show')">
			<xsl:value-of select="/root/gui/strings/cancel"/>
		</button>
		
	</xsl:template>
	
	<xsl:template name="templateChoice" match="*">
		
		<b><xsl:value-of select="/root/gui/strings/kind"/></b>
		<xsl:text>&#160;</xsl:text>
		<select class="content" name="template" size="1">
			<option value="n">
				<xsl:if test="string(geonet:info/isTemplate)='n'">
					<xsl:attribute name="selected">true</xsl:attribute>
				</xsl:if>
				<xsl:value-of select="/root/gui/strings/metadata"/>
			</option>
			<option value="y">
				<xsl:if test="string(geonet:info/isTemplate)='y'">
					<xsl:attribute name="selected">true</xsl:attribute>
				</xsl:if>
				<xsl:value-of select="/root/gui/strings/template"/>
			</option>
			<option value="s">
				<xsl:if test="string(geonet:info/isTemplate)='s'">
					<xsl:attribute name="selected">true</xsl:attribute>
				</xsl:if>
				<xsl:value-of select="/root/gui/strings/subtemplate"/>
			</option>
		</select>
		<xsl:text>&#160;</xsl:text>
		<xsl:value-of select="/root/gui/strings/subtemplateTitle"/>
		<xsl:text>&#160;</xsl:text>
		<input class="content" type="text" name="title" value="{geonet:info/title}"/>
		<!--
		<input class="content" type="checkbox" name="template">
			<xsl:if test="geonet:info/isTemplate='y'">
				<xsl:attribute name="checked"/>
			</xsl:if>
			<xsl:value-of select="/root/gui/strings/template"/>
		</input>
		-->
		
	</xsl:template>

</xsl:stylesheet>
