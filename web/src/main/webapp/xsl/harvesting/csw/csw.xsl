<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	
	<!-- ============================================================================================= -->
	<!-- === editPanel -->
	<!-- ============================================================================================= -->

	<xsl:template name="editPanel-CSW">
		<div id="csw.editPanel">
			<xsl:call-template name="site-CSW"/>
			<div class="dots"/>
			<xsl:call-template name="search-CSW"/>
			<div class="dots"/>
			<xsl:call-template name="options-CSW"/>
			<div class="dots"/>
			<xsl:call-template name="content-CSW"/>
			<div class="dots"/>
			<xsl:call-template name="privileges-CSW"/>
			<div class="dots"/>
			<xsl:call-template name="categories-CSW"/>
		</div>
	</xsl:template>

	<!-- ============================================================================================= -->

	<xsl:template name="site-CSW">
		<h1 align="left"><xsl:value-of select="/root/gui/harvesting/site"/></h1>
	
		<table border="0">
			<tr>
				<td class="padded"><xsl:value-of select="/root/gui/harvesting/name"/></td>
				<td class="padded"><input id="csw.name" class="content" type="text" value="" size="30"/></td>
			</tr>

			<tr>
				<td class="padded"><xsl:value-of select="/root/gui/harvesting/capabUrl"/></td>
				<td class="padded"><input id="csw.capabUrl" class="content" type="text" value="" size="30"/></td>
			</tr>

            <tr>
                <script language="javascript">
                function updateTextFieldOutputSchema(string)
                {
                   $('csw.outputSchema').value =  string;
                }
                </script>
                <td class="padded"><xsl:value-of select="/root/gui/harvesting/outputSchema"/></td>
                <td class="padded">
                    <input type="text" class="content" size="70" name="outputSchema" id="csw.outputSchema" value="http://www.geocat.ch/2008/che" />
                    <p>Examples : </p>
                    <ul>
                      <li><a href="javascript:updateTextFieldOutputSchema('http://www.opengis.net/cat/csw/2.0.2')">http://www.opengis.net/cat/csw/2.0.2' for Dublin Core</a></li>
                      <li><a href="javascript:updateTextFieldOutputSchema('http://www.isotc211.org/2005/gmd')">http://www.isotc211.org/2005/gmd' for ISO19139</a></li>
                      <li><a href="javascript:updateTextFieldOutputSchema('http://www.geocat.ch/2008/che')">http://www.geocat.ch/2008/che' for ISO19139-CHE</a></li>
                      <!--  issue #133730 : deactivated for now : GC subsystem is not able to handle GM03 MD yet -->
                      <!-- <li><a href="javascript:updateTextFieldOutputSchema('http://www.geocat.ch/2008/gm03_2')">'http://www.geocat.ch/2008/gm03_2' for GM03_2</a></li>   -->
                    </ul>
                </td>
            </tr>

			<tr>
				<td class="padded" valign="bottom"><xsl:value-of select="/root/gui/harvesting/icon"/></td>
				<td class="padded">
					<select id="csw.icon" class="content" name="icon" size="1"/>
					&#xA0;
					<img id="csw.icon.image" src="" alt="" />
				</td>
			</tr>
			
			<tr>
				<td class="padded"><xsl:value-of select="/root/gui/harvesting/useAccount"/></td>
				<td class="padded"><input id="csw.useAccount" type="checkbox" checked="on"/></td>
			</tr>

			<tr>
				<td/>
				<td>
					<table id="csw.account">
						<tr>
							<td class="padded"><xsl:value-of select="/root/gui/harvesting/username"/></td>
							<td class="padded"><input id="csw.username" class="content" type="text" value="" size="20"/></td>
						</tr>
		
						<tr>
							<td class="padded"><xsl:value-of select="/root/gui/harvesting/password"/></td>
							<td class="padded"><input id="csw.password" class="content" type="password" value="" size="20"/></td>
						</tr>
					</table>
				</td>
			</tr>			
		</table>
	</xsl:template>
	
	<!-- ============================================================================================= -->
	
	<xsl:template name="search-CSW">
		<h1 align="left"><xsl:value-of select="/root/gui/harvesting/search"/></h1>
		
		<div id="csw.searches"/>
		
		<button id="csw.addSearch" class="content" onclick="harvesting.csw.addSearchRow()">
			<xsl:value-of select="/root/gui/harvesting/add"/>
		</button>
	</xsl:template>

	<!-- ============================================================================================= -->
	
	<xsl:template name="options-CSW">
		<h1 align="left"><xsl:value-of select="/root/gui/harvesting/options"/></h1>
		<xsl:call-template name="schedule-widget">
			<xsl:with-param name="type">csw</xsl:with-param>
		</xsl:call-template>
		</xsl:template>
	
	<!-- ============================================================================================= -->

	<xsl:template name="content-CSW">
	<div>
		<h1 align="left"><xsl:value-of select="/root/gui/harvesting/content"/></h1>

		<table border="0">
             <!-- UNUSED -->
			<tr style="display:none;">
				<td class="padded"><xsl:value-of select="/root/gui/harvesting/importxslt"/></td>
				<td class="padded">
					&#160;
					<select id="csw.importxslt" class="content" name="importxslt" size="1"/>
				</td>
			</tr>

			<tr>
				<td class="padded"><xsl:value-of select="/root/gui/harvesting/validate"/></td>
				<td class="padded"><input id="csw.validate" type="checkbox" value=""/></td>
			</tr>
		</table>
	</div>
	</xsl:template>

	<!-- ============================================================================================= -->

	<xsl:template name="privileges-CSW">
		<h1 align="left"><xsl:value-of select="/root/gui/harvesting/privileges"/></h1>
		
		<table>
			<tr>
				<td class="padded" valign="top"><xsl:value-of select="/root/gui/harvesting/groups"/></td>
				<td class="padded"><select id="csw.groups" class="content" size="8" multiple="on"/></td>					
				<td class="padded" valign="top">
					<div align="center">
						<button id="csw.addGroups" class="content" onclick="harvesting.csw.addGroupRow()">
							<xsl:value-of select="/root/gui/harvesting/add"/>
						</button>
					</div>
				</td>					
			</tr>
		</table>
		
		<table id="csw.privileges">
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

	<xsl:template name="categories-CSW">
		<h1 align="left"><xsl:value-of select="/root/gui/harvesting/categories"/></h1>
		
		<select id="csw.categories" class="content" size="8" multiple="on"/>
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
