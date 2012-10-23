<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	
	<!-- ============================================================================================= -->
	<!-- === editPanel -->
	<!-- ============================================================================================= -->

	<xsl:template name="editPanel-wfsfeatures">
		<div id="wfsfeatures.editPanel">
			<xsl:call-template name="site-wfsfeatures"/>
			<div class="dots"/>
			<xsl:call-template name="options-wfsfeatures"/>
			<div class="dots"/>
			<xsl:call-template name="content-wfsfeatures"/>
			<div class="dots"/>
			<xsl:call-template name="privileges-wfsfeatures"/>
			<div class="dots"/>
			<xsl:call-template name="categories-wfsfeatures"/>
		</div>
	</xsl:template>

	<!-- ============================================================================================= -->

	<xsl:template name="site-wfsfeatures">
		<h1 align="left"><xsl:value-of select="/root/gui/harvesting/site"/></h1>
	
		<table border="0">
			<tr>
				<td class="padded"><xsl:value-of select="/root/gui/harvesting/name"/></td>
				<td class="padded"><input id="wfsfeatures.name" class="content" type="text" value="" size="30"/></td>
			</tr>

			<tr>
				<td class="padded"><xsl:value-of select="/root/gui/harvesting/capabUrl"/></td>
				<td class="padded">
					<input id="wfsfeatures.url" class="content" type="text" value="http://" size="30"/>
				</td>
			</tr>

			<tr>
				<td class="padded" valign="bottom"><xsl:value-of select="/root/gui/harvesting/icon"/></td>
				<td class="padded">
					<select id="wfsfeatures.icon" class="content" name="icon" size="1"/>
					&#xA0;
					<img id="wfsfeatures.icon.image" src="" alt="" />
				</td>
			</tr>

			<!-- TODO : Do we have to set up an account for WxS secured ? -->
			<tr style="display:none;">
				<td class="padded"><xsl:value-of select="/root/gui/harvesting/useAccount"/></td>
				<td class="padded"><input id="wfsfeatures.useAccount" type="checkbox"/></td>
			</tr>

			<tr style="display:none;">
				<td/>
				<td>
					<table id="wfsfeatures.account">
						<tr>
							<td class="padded"><xsl:value-of select="/root/gui/harvesting/username"/></td>
							<td class="padded"><input id="wfsfeatures.username" class="content" type="text" value="" size="20"/></td>
						</tr>
		
						<tr>
							<td class="padded"><xsl:value-of select="/root/gui/harvesting/password"/></td>
							<td class="padded"><input id="wfsfeatures.password" class="content" type="password" value="" size="20"/></td>
						</tr>
					</table>
				</td>
			</tr>			

			<!-- language -->
			<tr>
				<td class="padded"><xsl:value-of select="/root/gui/harvesting/wfsFeaturesLang"/></td>
				<td class="padded">
					<select id="wfsfeatures.lang">
					<!--  TODO loop on languages -->
						<option value="eng">eng</option>
						<option value="fra">fra</option>
					</select>
				</td>
			</tr>

			<!-- query -->
			<tr>
				<td class="padded"><xsl:value-of select="/root/gui/harvesting/wfsFeaturesQuery"/></td>
				<td class="padded">
					<textarea id="wfsfeatures.query" class="content" rows="20" cols="80"/>
				</td>
			</tr>
	
			<!-- output schema - select from those that offer WfsToFragments
			     stylesheets -->
			<tr>
				<td class="padded" colspan="2">
					<xsl:value-of select="/root/gui/harvesting/wfsFeaturesOutputSchema"/>
					&#160;
					<select id="wfsfeatures.outputSchema"/>
						
					<div id="wfsFeaturesSchemaOptions" style="margin-left:40px;display:none;border-color:#f00;border-style:solid;border-width:1px">
						<table>
							<!-- optional stylesheet to apply to wfs output -->
							<tr>
								<td class="padded">
									<xsl:value-of select="/root/gui/harvesting/wfsFeaturesStylesheet"/>
								</td><td class="padded">
									<select class="content" id="wfsfeatures.stylesheet" size="1"/>
								</td>
							</tr>

							<!-- stream features from large responses -->
							<tr>
								<td colspan="2" class="padded">
									<input type="checkbox" id="wfsfeatures.streamFeatures" value=""/>
									<label for="wfsfeatures.streamFeatures"><xsl:value-of select="/root/gui/harvesting/wfsFeaturesLargeResponse"/></label>
								</td>
							</tr>
			
							<!-- create subtemplates -->
							<tr>
								<td colspan="2" class="padded">
									<input type="checkbox" name="wfsfeatures.createSubtemplates" id="wfsfeatures.createSubtemplates" value=""/>
									<label for="wfsfeatures.createSubtemplates"><xsl:value-of select="/root/gui/harvesting/wfsFeaturesCreateSubtemplates"/></label>
								</td>
							</tr>
			
							<!-- template to match fragments into -->
							<tr>
								<td class="padded">
									<xsl:value-of select="/root/gui/harvesting/wfsFeaturesTemplate"/>
								</td>
								<td class="padded">
									<select class="content" id="wfsfeatures.templateId" size="1"/>
								</td>
							</tr>
						</table>
					</div>
				</td>
			</tr>

			<!-- categories of records build from template -->
			<tr>
				<td class="padded"><xsl:value-of select="/root/gui/harvesting/wfsFeaturesRecordCategory"/></td>
				<td class="padded">
					<select id="wfsfeatures.recordsCategory" class="content"/>
				</td>
			</tr>
		</table>
	</xsl:template>
		

	<!-- ============================================================================================= -->
	
	<xsl:template name="options-wfsfeatures">
		<h1 align="left"><xsl:value-of select="/root/gui/harvesting/options"/></h1>
		<xsl:call-template name="schedule-widget">
			<xsl:with-param name="type">wfsfeatures</xsl:with-param>
		</xsl:call-template>
	</xsl:template>
	
	<!-- ============================================================================================= -->

	<xsl:template name="content-wfsfeatures">
	<div style="display:none;"> <!-- UNUSED -->
		<h1 align="left"><xsl:value-of select="/root/gui/harvesting/content"/></h1>

		<table border="0">
			<tr>
				<td class="padded"><xsl:value-of select="/root/gui/harvesting/importxslt"/></td>
				<td class="padded">
					&#160;
					<select id="wfsfeatures.importxslt" class="content" name="importxslt" size="1"/>
				</td>
			</tr>

			<tr>
				<td class="padded"><xsl:value-of select="/root/gui/harvesting/validate"/></td>
				<td class="padded"><input id="wfsfeatures.validate" type="checkbox" value=""/></td>
			</tr>
		</table>
	</div>
	</xsl:template>

	<!-- ============================================================================================= -->

	<xsl:template name="privileges-wfsfeatures">
		<h1 align="left"><xsl:value-of select="/root/gui/harvesting/privileges"/></h1>
		
		<table>
			<tr>
				<td class="padded" valign="top"><xsl:value-of select="/root/gui/harvesting/groups"/></td>
				<td class="padded"><select id="wfsfeatures.groups" class="content" size="8" multiple="on"/></td>					
				<td class="padded" valign="top">
					<div align="center">
						<button id="wfsfeatures.addGroups" class="content" onclick="harvesting.wfsfeatures.addGroupRow()">
							<xsl:value-of select="/root/gui/harvesting/add"/>
						</button>
					</div>
				</td>					
			</tr>
		</table>
		
		<table id="wfsfeatures.privileges">
			<tr>
				<th class="padded"><b><xsl:value-of select="/root/gui/harvesting/group"/></b></th>
				<th class="padded"><b><xsl:value-of select="/root/gui/harvesting/oper/op[@id='0']"/></b></th>
				<th class="padded"><b><xsl:value-of select="/root/gui/harvesting/oper/op[@id='5']"/></b></th>
				<th class="padded"><b><xsl:value-of select="/root/gui/harvesting/oper/op[@id='6']"/></b></th>
				<th/>
			</tr>
		</table>
		
	</xsl:template>
	
	<!-- ============================================================================================= -->

	<xsl:template name="categories-wfsfeatures">
		<h1 align="left"><xsl:value-of select="/root/gui/harvesting/wfsFeaturesSubtemplateCategory"/></h1>
		
		<select id="wfsfeatures.categories" class="content"/>
	</xsl:template>
	
	<!-- ============================================================================================= -->

    <xsl:template mode="selectoptions" match="day|hour|minute|dsopt">
        <option>
            <xsl:attribute name="value">
                <xsl:value-of select="."/>
            </xsl:attribute>
            <xsl:value-of select="@label"/>
        </option>
    </xsl:template>

    <!-- ============================================================================================= -->

</xsl:stylesheet>
