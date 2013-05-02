<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	
	<!-- ============================================================================================= -->
	<!-- === editPanel -->
	<!-- ============================================================================================= -->

	<xsl:template name="editPanel-geoPREST">
		<div id="geoPREST.editPanel">
            <xsl:call-template name="ownerGroup-GEOPREST"/>
            <div class="dots"/>
			<xsl:call-template name="site-GEOPREST"/>
			<div class="dots"/>
			<xsl:call-template name="search-GEOPREST"/>
			<div class="dots"/>
			<xsl:call-template name="options-GEOPREST"/>
			<div class="dots"/>
			<xsl:call-template name="content-GEOPREST"/>
			<div class="dots"/>
			<xsl:call-template name="privileges-GEOPREST"/>
			<div class="dots"/>
			<xsl:call-template name="categories-GEOPREST"/>
		</div>
	</xsl:template>

    <!-- ============================================================================================= -->
    <xsl:template name="ownerGroup-GEOPREST">
        <table border="0">
            <tr>
                <td class="padded"><xsl:value-of select="/root/gui/harvesting/selectownergroup"/></td>
                <td class="padded"><select id="geoPREST.ownerGroup" class="content"/></td>
            </tr>
            <tr>
                <td colspan="2">&#xA0;</td>
            </tr>
        </table>
    </xsl:template>
	<!-- ============================================================================================= -->

	<xsl:template name="site-GEOPREST">
		<h1 align="left"><xsl:value-of select="/root/gui/harvesting/site"/></h1>
	
		<table border="0">
			<tr>
				<td class="padded"><xsl:value-of select="/root/gui/harvesting/name"/></td>
				<td class="padded"><input id="geoPREST.name" class="content" type="text" value="" size="30"/></td>
			</tr>

			<tr>
				<td class="padded"><xsl:value-of select="/root/gui/harvesting/baseUrl"/></td>
				<td class="padded"><input id="geoPREST.baseUrl" class="content" type="text" value="" size="30"/></td>
			</tr>

			<tr>
				<td class="padded" valign="bottom"><xsl:value-of select="/root/gui/harvesting/icon"/></td>
				<td class="padded">
					<select id="geoPREST.icon" class="content" name="icon" size="1"/>
					&#xA0;
					<img id="geoPREST.icon.image" src="" alt="" class="logo"/>
				</td>
			</tr>
	
			<!-- UNUSED -->
			<tr style="display:none;">
				<td class="padded"><xsl:value-of select="/root/gui/harvesting/useAccount"/></td>
				<td class="padded"><input id="geoPREST.useAccount" type="checkbox" checked="on"/></td>
			</tr>

			<!-- UNUSED -->
			<tr style="display:none;">
				<td/>
				<td>
					<table id="geoPREST.account">
						<tr>
							<td class="padded"><xsl:value-of select="/root/gui/harvesting/username"/></td>
							<td class="padded"><input id="geoPREST.username" class="content" type="text" value="" size="20"/></td>
						</tr>
		
						<tr>
							<td class="padded"><xsl:value-of select="/root/gui/harvesting/password"/></td>
							<td class="padded"><input id="geoPREST.password" class="content" type="password" value="" size="20"/></td>
						</tr>
					</table>
				</td>
			</tr>			
		</table>
	</xsl:template>
	
	<!-- ============================================================================================= -->
	
	<xsl:template name="search-GEOPREST">
		<h1 align="left"><xsl:value-of select="/root/gui/harvesting/search"/></h1>
		
		<div id="geoPREST.searches"/>
		
		<button id="geoPREST.addSearch" class="content" onclick="harvesting.geoPREST.addSearchRow()">
			<xsl:value-of select="/root/gui/harvesting/add"/>
		</button>
	</xsl:template>

	<!-- ============================================================================================= -->
	
	<xsl:template name="options-GEOPREST">
		<h1 align="left"><xsl:value-of select="/root/gui/harvesting/options"/></h1>
		<xsl:call-template name="schedule-widget">
			<xsl:with-param name="type">geoPREST</xsl:with-param>
		</xsl:call-template>
		</xsl:template>
	
	<!-- ============================================================================================= -->

	<xsl:template name="content-GEOPREST">
	<div>
		<h1 align="left"><xsl:value-of select="/root/gui/harvesting/content"/></h1>

		<table border="0">
			<tr>
				<td class="padded"><xsl:value-of select="/root/gui/harvesting/importxslt"/></td>
				<td class="padded">
					&#160;
					<select id="geoPREST.importxslt" class="content" name="importxslt" size="1"/>
				</td>
			</tr>

			<tr>
				<td class="padded"><xsl:value-of select="/root/gui/harvesting/validate"/></td>
				<td class="padded"><input id="geoPREST.validate" type="checkbox" value=""/></td>
			</tr>
		</table>
	</div>
	</xsl:template>

	<!-- ============================================================================================= -->

	<xsl:template name="privileges-GEOPREST">
		<h1 align="left"><xsl:value-of select="/root/gui/harvesting/privileges"/></h1>
		
		<table>
			<tr>
				<td class="padded" valign="top"><xsl:value-of select="/root/gui/harvesting/groups"/></td>
				<td class="padded"><select id="geoPREST.groups" class="content" size="8" multiple="on"/></td>					
				<td class="padded" valign="top">
					<div align="center">
						<button id="geoPREST.addGroups" class="content" onclick="harvesting.geoPREST.addGroupRow()">
							<xsl:value-of select="/root/gui/harvesting/add"/>
						</button>
					</div>
				</td>					
			</tr>
		</table>
		
		<table id="geoPREST.privileges">
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

	<xsl:template name="categories-GEOPREST">
		<h1 align="left"><xsl:value-of select="/root/gui/harvesting/categories"/></h1>
		
		<select id="geoPREST.categories" class="content" size="8" multiple="on"/>
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
