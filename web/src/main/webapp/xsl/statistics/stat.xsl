<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
	xmlns:fo="http://www.w3.org/1999/XSL/Format"
	xmlns:date="http://exslt.org/dates-and-times"
	>
	
	<xsl:include href="../main.xsl"/>

	<xsl:template mode="css" match="/">
		<xsl:call-template name="geoCssHeader"/>
		<xsl:call-template name="ext-ux-css"/>
	</xsl:template>

	<!--
	additional scripts
	-->
	<xsl:template mode="script" match="/">
        <xsl:variable name="minimize">
            <xsl:choose>
                <xsl:when test="/root/request/debug">?minimize=false</xsl:when>
                <xsl:otherwise></xsl:otherwise>
            </xsl:choose>
        </xsl:variable>

        <script type="text/javascript" src="{/root/gui/url}/static/kernel.js{$minimize}"/>
        <xsl:call-template name="geoHeader"/>
        <xsl:call-template name="ext-ux"/>


        <script type="text/javascript" src="{/root/gui/url}/static/gn.libs.scriptaculous.js{$minimize}"></script>
		<script type="text/javascript" src="{/root/gui/url}/static/gn.js{$minimize}"></script>
		<script type="text/javascript" src="{/root/gui/url}/static/gn.editor.js{$minimize}"></script>

		<script type="text/javascript" language="JavaScript" src="{/root/gui/url}/scripts/swfobject.js"/>
		<script type="text/javascript" language="JavaScript" src="{/root/gui/url}/scripts/gn_stats.js"/>
	</xsl:template>
	
	<!-- page content -->
	<xsl:template name="content">
		<xsl:call-template name="formLayout">
			<xsl:with-param name="title" select="/root/gui/strings/searchStatisticsDes"/>
			<xsl:with-param name="content">
			<p/>
		<!--  div to display message if search log is disabled -->
		<form name="statForm">
			<table cellspacing="2" border="0" align="left">
				<!--  tagcloud -->
			 	<tr>
					<td colspan="3">
						<b><a href="javascript:updateDiv('stat.tagCloud', 'stat.tagCloudDiv')"><xsl:value-of select="/root/gui/strings/stat.searchedKeywords"/></a></b>&#160;
					</td>
				</tr>
				<tr>
					<td colspan="3">
						<div id="stat.tagCloudDiv"/>
					</td>
				</tr>
				<tr><td class="spacer"/></tr>
				<tr><td class="spacer"/></tr>
				<tr><td class="spacer"/></tr>
				<!--  tabular statistics -->
				<tr>
					<td colspan="3">
						<b><xsl:value-of select="/root/gui/strings/stat.tabularStats"/></b> 
						<small> (<xsl:value-of select="/root/gui/strings/stat.tip1begin"/> 
						<img src="{/root/gui/url}/images/arrow_down.gif"/>&#160;<xsl:value-of select="/root/gui/strings/stat.tip1end"/>)</small>
						<hr/>
					</td>
				</tr>
				<tr>
					<td colspan="3">
						<a href="javascript:injectServiceResponse('stat.lastMonthSummary','stat.lastMonthSummaryDiv')">
						<b><xsl:value-of select="/root/gui/strings/stat.lastMonthStats"/></b></a>
						&#160;<a href="#"><img src="{/root/gui/url}/images/arrow_down.gif" onclick="collapseSearch(['stat.lastMonthSummaryDiv'])"/></a>
					</td>
				</tr>
				<tr><td class="spacer"/></tr>
				<tr>
					<td><div id="stat.lastMonthSummaryDiv"/></td>
				</tr>
				<tr><td class="spacer"/></tr>
				<tr>
					<td colspan="3">
						
						<a href="javascript:injectServiceResponse('stat.uniqueIP','stat.uniqueIPDiv')">
						<b><xsl:value-of select="/root/gui/strings/stat.userIpText"/></b></a>
						&#160;<a href="#"><img src="{/root/gui/url}/images/arrow_down.gif" onclick="collapseSearch(['stat.uniqueIPDiv'])"/></a>
					</td>
				</tr>
				<tr><td class="spacer"/></tr>
				<tr>
					<td><div id="stat.uniqueIPDiv"/></td>
				</tr>
				<tr><td class="spacer"/></tr>
				<tr>
					<td colspan="3">
						<a href="javascript:injectServiceResponse('stat.mostSearchedKeyword','stat.mostSearchedKeywordDiv')">
						<b><xsl:value-of select="/root/gui/strings/stat.mostSearchedKeywords"/></b></a>
						&#160;<a href="#"><img src="{/root/gui/url}/images/arrow_down.gif" onclick="collapseSearch(['stat.mostSearchedKeywordDiv'])"/></a>
					</td>
				</tr>
				<tr><td class="spacer"/></tr>
				<tr>
					<td><div id="stat.mostSearchedKeywordDiv"/></td>
				</tr>
				<tr><td class="spacer"/></tr>
				<tr>
					<td colspan="3">
						<a href="javascript:injectServiceResponse('stat.mostSearchedCategory','stat.mostSearchedCategoryDiv')">
						<b><xsl:value-of select="/root/gui/strings/stat.mostSearchedCategories"/></b></a>
						&#160;<a href="#"><img src="{/root/gui/url}/images/arrow_down.gif" onclick="collapseSearch(['stat.mostSearchedCategoryDiv'])"/></a>
					</td>
				</tr>
				<tr><td class="spacer"/></tr>
				<tr>
					<td><div id="stat.mostSearchedCategoryDiv"/></td>
				</tr>
				<tr><td class="spacer"/></tr>
				<tr>
					<td colspan="3">
						<a href="javascript:injectServiceResponse('stat.simpleAdvancedSearch','stat.simpleAdvancedSearchDiv')">
						<b><xsl:value-of select="/root/gui/strings/stat.simpleVsAdvanced"/></b></a>
						&#160;<a href="#"><img src="{/root/gui/url}/images/arrow_down.gif" onclick="collapseSearch(['stat.simpleAdvancedSearchDiv'])"/></a>
					</td>
				</tr>
				<tr><td class="spacer"/></tr>
				<tr>
					<td><div id="stat.simpleAdvancedSearchDiv"/></td>
				</tr>
				<tr><td class="spacer"/></tr>
				<tr>
					<td colspan="3">
						<a href="javascript:injectServiceResponse('stat.mdPopularity','stat.mdPopularityDiv')">
						<b><xsl:value-of select="/root/gui/strings/stat.mdPopularity"/></b></a>
						&#160;<a href="#"><img src="{/root/gui/url}/images/arrow_down.gif" onclick="collapseSearch(['stat.mdPopularityDiv'])"/></a>
					</td>
				</tr>
				<tr><td class="spacer"/></tr>
				<tr>
					<td><div id="stat.mdPopularityDiv"/></td>
				</tr>
				<tr><td class="spacer"/></tr>
				<!--  graphical statistics -->
				<tr>
					<td colspan="3">
						<b><xsl:value-of select="/root/gui/strings/stat.graphicalStats"/></b>&#160;
						(<xsl:value-of select="/root/gui/strings/stat.tip2"/>)
						<hr/>
					</td>
				</tr>
				<tr>
					<td colspan="3"><b><xsl:value-of select="/root/gui/strings/stat.chooseDateFrom"/></b>
						&#160;<a href="#"><img src="{/root/gui/url}/images/arrow_down.gif" onclick="collapseSearch(['stat.graphicDiv'])"/></a>
					</td>
				</tr>
				<tr>
					<td colspan="3">
						<input type="radio" id="statGraphTypeYEAR" name="statGraphType" value="YEAR" checked="true">
							<label for="statGraphTypeYEAR"><xsl:value-of select="/root/gui/strings/stat.byYear"/></label>
					    </input> &#160; 
						<input type="radio" id="statGraphTypeMONTH" name="statGraphType" value="MONTH">
							<label for="statGraphTypeMONTH"><xsl:value-of select="/root/gui/strings/stat.byMonth"/></label>
						</input> &#160; 
						<input type="radio" id="statGraphTypeDAY" name="statGraphType" value="DAY">
							<label for="statGraphTypeDAY"><xsl:value-of select="/root/gui/strings/stat.byDay"/></label>
						</input>
					</td>
				</tr>
				<tr>
					<td colspan="3">
					<table width="100%">
					<tr>
						<xsl:variable name="df">[Y0001]-[M01]-[D01]</xsl:variable>
						<!-- <xsl:variable name="df">[Y0001]-[M01]-[D01]T[H01]:[m01]:[s01]</xsl:variable> -->
							
						<td>
							<xsl:value-of select="/root/gui/strings/stat.dateFrom"/><br/>
							<div class="cal" id="f_date_from"></div>
							<input type="hidden" id="f_date_from_format" value="%Y-%m-%d"/>
							<input type="hidden" id="f_date_from_cal" value=""/>
						</td>
						<td>
							<xsl:value-of select="/root/gui/strings/stat.dateTo"/><br/>
							<div class="cal" id="f_date_to"></div>
							<input type="hidden" id="f_date_to_format" value="%Y-%m-%d"/>
							<input type="hidden" id="f_date_to_cal" value="{format-dateTime(current-dateTime(),$df)}"/>
						</td>
						<td>&#160;<br/><input type="button" name="okButton" value="OK" onclick="displayGraphic()"/></td>
					</tr>
					</table>
					</td>
				</tr>
				<tr><td class="spacer"/></tr>
				<tr>
					<td colspan="3"><div id="stat.graphicDiv"/></td>
				</tr>
				<tr>
					<td colspan="3">
						<a href="javascript:injectServiceResponse('stat.groupPopularity','stat.groupPopularityDiv')">
						<b><xsl:value-of select="/root/gui/strings/stat.popuByGroup"/></b></a>
						&#160;<a href="#"><img src="{/root/gui/url}/images/arrow_down.gif" onclick="collapseSearch(['stat.groupPopularityDiv'])"/></a>
					</td>
				</tr>
				<tr><td class="spacer"/></tr>
				<tr>
					<td><div id="stat.groupPopularityDiv"/></td>
				</tr>
				<tr><td class="spacer"/></tr>
				<tr>
					<td colspan="3">
						<a href="javascript:injectServiceResponse('stat.categoryPopularity','stat.categoryPopularityDiv')">
						<b><xsl:value-of select="/root/gui/strings/stat.popularityByCategory"/></b></a>
						&#160;<a href="#"><img src="{/root/gui/url}/images/arrow_down.gif" onclick="collapseSearch(['stat.categoryPopularityDiv'])"/></a>
					</td>
				</tr>
				<tr><td class="spacer"/></tr>
				<tr>
					<td><div id="stat.categoryPopularityDiv"/></td>
				</tr>
				<tr><td class="spacer"/></tr>
				<tr>
					<td colspan="3">
						<a href="javascript:injectServiceResponse('stat.catalogPopularity','stat.catalogPopularityDiv')">
						<b><xsl:value-of select="/root/gui/strings/stat.popuByCatalog"/></b></a>
						&#160;<a href="#"><img src="{/root/gui/url}/images/arrow_down.gif" onclick="collapseSearch(['stat.catalogPopularityDiv'])"/></a>
					</td>
				</tr>
				<tr><td class="spacer"/></tr>
				<tr>
					<td><div id="stat.catalogPopularityDiv"/></td>
				</tr>
				<tr><td class="spacer"/></tr>
				<!--  export statistics table (cvs format)-->
				<tr>
					<td colspan="3">
						<b><xsl:value-of select="/root/gui/strings/stat.csvExport"/></b>&#160;
                        (<xsl:value-of select="/root/gui/strings/stat.warnCsvExport"/>)
                        &#160;<a href="#"><img src="{/root/gui/url}/images/arrow_down.gif" onclick="collapseSearch(['stat.tableExportDiv'])"/></a>
						<hr/>
					</td>
				</tr>
				<tr>
					<td colspan="3">
						<a href="javascript:injectServiceResponse('stat.tableExport?tableToExport=requests','stat.tableExportDiv')">
                            <xsl:value-of select="/root/gui/strings/stat.exportRequests"/></a>&#160;
                            <br/>
						<a href="javascript:injectServiceResponse('stat.tableExport?tableToExport=params', 'stat.tableExportDiv')">
                            <xsl:value-of select="/root/gui/strings/stat.exportParams"/></a>&#160;
					</td>
				</tr>
				<tr><td class="spacer"/></tr>
				<tr>
					<td><div id="stat.tableExportDiv"/></td>
				</tr>
				<tr><td class="spacer"/></tr>
				<!--  Image deletion -->
				<tr>
					<td colspan="3">
						<b><xsl:value-of select="/root/gui/strings/stat.maintenance"/></b>&#160;
						   (<xsl:value-of select="/root/gui/strings/stat.deleteTmpImages"/>) 
						<hr/>
					</td>
				</tr>
				<tr>
					<td colspan="3">
						<a href="javascript:injectServiceResponse('stat.deleteTmpGraphics','stat.deleteTempGraphicsDiv');collapseSearch(['stat.tableExportDiv'])">
						<b><xsl:value-of select="/root/gui/strings/stat.deleteTmpFiles"/></b></a>
						&#160;<a href="#"><img src="{/root/gui/url}/images/arrow_down.gif" onclick="collapseSearch(['stat.deleteTempGraphicsDiv'])"/></a>
					</td>
				</tr>
				<tr>
					<td><div id="stat.deleteTempGraphicsDiv"/></td>
				</tr>
		</table>
		</form>
		<script>initStat();</script>
		<!--  Services failure should be displayed here -->
		<font color="red"><div align="left" id="serviceFailureDiv"></div></font>
				<p/>
			</xsl:with-param>
		</xsl:call-template>
	</xsl:template>
</xsl:stylesheet>
