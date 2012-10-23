<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<!-- ============================================================================================= -->
	<!-- === editPanel -->
	<!-- ============================================================================================= -->

	<xsl:template name="editPanel-CGP">
		<div id="cgp.editPanel">
			<xsl:call-template name="site-CGP"/>
			<div class="dots"/>
			<xsl:call-template name="search-CGP"/>
			<div class="dots"/>
			<xsl:call-template name="options-CGP"/>
            <div class="dots"/>
            <xsl:call-template name="content-CGP"/>
			<div class="dots"/>
			<xsl:call-template name="privileges-CGP"/>
			<div class="dots"/>
			<xsl:call-template name="categories-CGP"/>
		</div>
	</xsl:template>

	<!-- ============================================================================================= -->

	<xsl:template name="site-CGP">
		<h1 align="left"><xsl:value-of select="/root/gui/harvesting/site"/></h1>

		<table border="0">
			<tr>
				<td class="padded"><xsl:value-of select="/root/gui/harvesting/name"/></td>
				<td class="padded"><input id="cgp.name" class="content" type="text" value="" size="30"/></td>
			</tr>

			<tr>
				<td class="padded"><xsl:value-of select="/root/gui/harvesting/url"/></td>
				<td class="padded">
					<input id="cgp.url" class="content" type="text" value="http://" size="30"/>
				</td>
			</tr>
			<tr>
				<td class="padded" valign="bottom"><xsl:value-of select="/root/gui/harvesting/icon"/></td>
				<td class="padded">
					<select id="cgp.icon" class="content" name="icon" size="1"/>
					&#xA0;
					<img id="cgp.icon.image" src="" alt="" />
				</td>
			</tr>

			<tr>
				<td class="padded"><xsl:value-of select="/root/gui/harvesting/useAccount"/></td>
				<td class="padded"><input id="cgp.useAccount" type="checkbox" checked="off"/></td>
			</tr>

			<tr>
				<td/>
				<td>
					<table id="cgp.account">
						<tr>
							<td class="padded"><xsl:value-of select="/root/gui/harvesting/username"/></td>
							<td class="padded"><input id="cgp.username" class="content" type="text" value="" size="20"/></td>
						</tr>

						<tr>
							<td class="padded"><xsl:value-of select="/root/gui/harvesting/password"/></td>
							<td class="padded"><input id="cgp.password" class="content" type="password" value="" size="20"/></td>
						</tr>
					</table>
				</td>
			</tr>
	               
		</table>
	</xsl:template>

	<!-- ============================================================================================= -->

	<xsl:template name="search-CGP">
		<h1 align="left"><xsl:value-of select="/root/gui/harvesting/search"/></h1>

		<div id="cgp.searches"/>

		<button id="cgp.addSearch" class="content" onclick="harvesting.cgp.addSearch()">
			<xsl:value-of select="/root/gui/harvesting/add"/>
		</button>
	</xsl:template>


	<!-- =============================================================================================  -->

	<xsl:template name="options-CGP">
		<h1 align="left"><xsl:value-of select="/root/gui/harvesting/options"/></h1>
		<xsl:call-template name="schedule-widget">
			<xsl:with-param name="type">cgp</xsl:with-param>
		</xsl:call-template>
	</xsl:template>	
    <!-- ============================================================================================= -->

    <xsl:template name="content-CGP">
    <div>
        <h1 align="left"><xsl:value-of select="/root/gui/harvesting/content"/></h1>

        <table border="0">
             <!-- UNUSED -->
            <tr style="display:none;">
                <td class="padded"><xsl:value-of select="/root/gui/harvesting/importxslt"/></td>
                <td class="padded">
                    &#160;
                    <select id="cgp.importxslt" class="content" name="importxslt" size="1"/>
                </td>
            </tr>

            <tr>
                <td class="padded"><xsl:value-of select="/root/gui/harvesting/validate"/></td>
                <td class="padded"><input id="cgp.validate" type="checkbox" value=""/></td>
            </tr>
        </table>
    </div>
    </xsl:template>

	<!-- =============================================================================================  -->

	<xsl:template name="privileges-CGP">
		<h1 align="left"><xsl:value-of select="/root/gui/harvesting/privileges"/></h1>

		<table>
			<tr>
				<td class="padded" valign="top"><xsl:value-of select="/root/gui/harvesting/groups"/></td>
				<td class="padded"><select id="cgp.groups" class="content" size="8" multiple="on"/></td>
				<td class="padded" valign="top">
					<div align="center">
						<button id="cgp.addGroups" class="content" onclick="harvesting.cgp.addGroupRow()">
							<xsl:value-of select="/root/gui/harvesting/add"/>
						</button>
					</div>
				</td>
			</tr>
		</table>

		<table id="cgp.privileges">
			<tr>
				<th class="padded"><b><xsl:value-of select="/root/gui/harvesting/group"/></b></th>
				<th class="padded"><b><xsl:value-of select="/root/gui/harvesting/oper/op[@id='0']"/></b></th>
				<th/>
			</tr>
		</table>

	</xsl:template>

	<!-- ============================================================================================= -->

	<xsl:template name="categories-CGP">
		<h1 align="left"><xsl:value-of select="/root/gui/harvesting/categories"/></h1>

		<select id="cgp.categories" class="content" size="8" multiple="on"/>
	</xsl:template>


	<!-- ============================================================================================= -->

</xsl:stylesheet>
