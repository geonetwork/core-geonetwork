<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	
	<!-- ============================================================================================= -->
	<!-- === editPanel -->
	<!-- ============================================================================================= -->

	<xsl:template name="editPanel-WD">
		<div id="wd.editPanel">
			<xsl:call-template name="host-WD"/>
			<div class="dots"/>
			<xsl:call-template name="options-WD"/>
			<div class="dots"/>
			<xsl:call-template name="content-WD"/>
			<div class="dots"/>
			<xsl:call-template name="privileges">
				<xsl:with-param name="type" select="'wd'"/>
				<xsl:with-param name="jsId" select="'webdav'"/>
			</xsl:call-template>
			<div class="dots"/>
			<xsl:call-template name="categories-WD"/>
		</div>
	</xsl:template>

	<!-- ============================================================================================= -->

	<xsl:template name="host-WD">
		<h1 align="left"><xsl:value-of select="/root/gui/harvesting/site"/></h1>
	
		<table border="0">

			<tr>
				<td class="padded" valign="bottom">Subtype</td>
				<td class="padded">
					<select id="wd.subtype" class="content" name="subytpe" size="1">
						<option  value="waf">Web Access Folder(WAF)</option>
						<option  value="webdav">Web DAV Server(WebDAV)</option> 				   
					</select>
				</td>
			</tr>
			
			<tr>
				<td class="padded"><xsl:value-of select="/root/gui/harvesting/name"/></td>
				<td class="padded"><input id="wd.name" class="content" type="text" value="" size="30"/></td>
			</tr>

			<tr>
				<td class="padded"><xsl:value-of select="/root/gui/harvesting/url"/></td>
				<td class="padded"><input id="wd.url" class="content" type="text" value="" size="30"/></td>
			</tr>

			<tr>
				<td class="padded" valign="bottom"><xsl:value-of select="/root/gui/harvesting/icon"/></td>
				<td class="padded">
					<select id="wd.icon" class="content" name="icon" size="1"/>
					&#xA0;
					<img id="wd.icon.image" src="" alt="" />
				</td>
			</tr>
			
			<xsl:call-template name="useAccount">
				<xsl:with-param name="type" select="'wd'"/>
			</xsl:call-template>
		</table>
	</xsl:template>
	
	<!-- ============================================================================================= -->
	
	<xsl:template name="options-WD">
		<h1 align="left"><xsl:value-of select="/root/gui/harvesting/options"/></h1>
		<xsl:call-template name="schedule-widget">
			<xsl:with-param name="type">wd</xsl:with-param>
		</xsl:call-template>

        <table>
            <tr>
                <td class="padded"><xsl:value-of select="/root/gui/harvesting/validate"/></td>
                <td class="padded"><input id="wd.validate" type="checkbox" value=""/></td>
            </tr>

            <tr>
                <td class="padded"><xsl:value-of select="/root/gui/harvesting/recurse"/></td>
                <td class="padded"><input id="wd.recurse" type="checkbox" value=""/></td>
            </tr>
        </table>
	</xsl:template>
	
	<!-- ============================================================================================= -->

	<xsl:template name="content-WD">
	<div style="display:none;"> <!-- UNUSED -->
		<h1 align="left"><xsl:value-of select="/root/gui/harvesting/content"/></h1>

		<table border="0">
			<tr>
				<td class="padded"><xsl:value-of select="/root/gui/harvesting/importxslt"/></td>
				<td class="padded">
					&#160;
					<select id="wd.importxslt" class="content" name="importxslt" size="1"/>
				</td>
			</tr>

			<tr>
				<td class="padded"><xsl:value-of select="/root/gui/harvesting/validate"/></td>
				<td class="padded"><input id="wd.validate" type="checkbox" value=""/></td>
			</tr>
		</table>
	</div>
	</xsl:template>

	<!-- ============================================================================================= -->

	<xsl:template name="categories-WD">
		<h1 align="left"><xsl:value-of select="/root/gui/harvesting/categories"/></h1>
		
		<select id="wd.categories" class="content" size="8" multiple="on"/>
	</xsl:template>
	
</xsl:stylesheet>
