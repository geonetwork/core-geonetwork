<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
		xmlns:threadUtils="java:org.fao.geonet.util.ThreadUtils">
	
	<!-- ============================================================================================= -->

	<xsl:include href="../main.xsl"/>

	<!-- ============================================================================================= -->

	<xsl:variable name="style" select="'margin-left:50px;'"/>
	<xsl:variable name="width" select="'70px'"/>
	
	<!-- ============================================================================================= -->
	
	<xsl:template mode="script" match="/">
		<script type="text/javascript" src="{/root/gui/url}/scripts/core/kernel/kernel.js"/>
		<script type="text/javascript" src="{/root/gui/url}/scripts/core/gui/gui.js"/>
		<script type="text/javascript" src="{/root/gui/url}/scripts/config/config.js"/>
	</xsl:template>

	<!-- ============================================================================================= -->
	<!-- === page content -->
	<!-- ============================================================================================= -->

	<xsl:template name="content">
		<xsl:call-template name="formLayout">
			<xsl:with-param name="title" select="/root/gui/strings/systemConfig"/>

			<xsl:with-param name="content">
				<xsl:call-template name="panel"/>
			</xsl:with-param>

			<xsl:with-param name="buttons">
				<xsl:call-template name="buttons"/>
			</xsl:with-param>
		</xsl:call-template>
	</xsl:template>
	
	<!-- ============================================================================================= -->
	<!-- === Panel -->
	<!-- ============================================================================================= -->

	<xsl:template name="panel">
		<xsl:call-template name="site"/>
		<xsl:call-template name="server"/>
		<xsl:call-template name="intranet"/>
		<xsl:call-template name="selectionmanager"/>
		<xsl:call-template name="threadedindexing"/>
		<xsl:call-template name="indexoptimizer"/>
		<xsl:call-template name="z3950"/>
		<xsl:call-template name="oai"/>
		<xsl:call-template name="xlinkResolver"/>
		<xsl:call-template name="searchStats"/>
        <xsl:call-template name="multilingual"/>
		<xsl:call-template name="downloadservice"/>
		<xsl:call-template name="hyperlinks"/>
		<xsl:call-template name="localrating"/>
        <xsl:call-template name="autofixing"/>
        <xsl:call-template name="inspire"/>
        <xsl:call-template name="metadataviews"/>
		<xsl:call-template name="metadataprivs"/>
		<xsl:call-template name="harvester"/>
		<xsl:call-template name="proxy"/>
		<xsl:call-template name="feedback"/>
		<xsl:call-template name="removedMetadata"/>
		<xsl:call-template name="authentication"/>
	</xsl:template>

	<!-- ============================================================================================= -->

	<xsl:template name="multilingual">
        <h1 align="left"><xsl:value-of select="/root/gui/config/multilingual"/></h1>
        <div align="left" style="{$style}">
            <table>
                <tr>
                    <td class="padded">
                        <input id="autodetect.enable" class="content" type="checkbox"/>
                    </td>
                    <td class="padded">
                        <label for="autodetect.enable">
                            <xsl:value-of select="/root/gui/config/autoDetectEnable"/>
                        </label>
                    </td>
                </tr>
            </table>
        </div>
        <div align="left" style="{$style}">
            <input align="left" type="radio" id="requestedLanguage.only" value="only" name="requestedlanguage"/>
        	<label for="requestedLanguage.only">
                <xsl:value-of select="/root/gui/config/requestedlanguageonly"/>
            </label>
            <div align="left" style="{$style}">
                <span id="requestedlanguage_only.subpanel">
                    <xsl:value-of select="/root/gui/config/tips/tip[id='requestedlanguage.only']"/>
                </span>
            </div>
        </div>
        <div align="left" style="{$style}">
            <input align="left" type="radio" id="requestedLanguage.sorted" value="sorted" name="requestedlanguage"/>
            <label for="requestedLanguage.sorted">
                <xsl:value-of select="/root/gui/config/requestedlanguagesorted"/>
            </label>
            <div align="left" style="{$style}">
                <span id="requestedlanguage_sorted.subpanel">
                    <xsl:value-of select="/root/gui/config/tips/tip[id='requestedlanguage.sorted']"/>
                </span>
            </div>
        </div>
        <div align="left" style="{$style}">
            <input align="left" type="radio" id="requestedLanguage.ignored" value="ignored" name="requestedlanguage"/>
            <label for="requestedLanguage.ignored">
                <xsl:value-of select="/root/gui/config/requestedlanguageignored"/>
            </label>
            <div align="left" style="{$style}">
                <span id="requestedlanguage_ignored.subpanel">
                    <xsl:value-of select="/root/gui/config/tips/tip[id='requestedlanguage.ignored']"/>
                </span>
            </div>
        </div>
    </xsl:template>

    <!-- ============================================================================================= -->

    <xsl:template name="searchStats">
        <h1 align="left"><xsl:value-of select="/root/gui/config/searchStats"/></h1>
        <div align="left" style="{$style}">
            <table>
                <tr>
                    <td class="padded" width="{$width}"><label for="searchStats.enable"><xsl:value-of select="/root/gui/config/enable"/></label></td>
                    <td class="padded"><input id="searchStats.enable" class="content" type="checkbox"/></td>
                </tr>
            </table>
        </div>
    </xsl:template>

    <!-- ============================================================================================= -->

	<xsl:template name="xlinkResolver">
		<h1 align="left"><xsl:value-of select="/root/gui/config/xlinkResolver"/></h1>
		<div align="left" style="{$style}">
			<table>
				<tr>
					<td class="padded" width="{$width}"><label for="xlinkResolver.enable"><xsl:value-of select="/root/gui/config/enable"/></label></td>
					<td class="padded"><input id="xlinkResolver.enable" class="content" type="checkbox"/></td>
				</tr>
			</table>
		</div>
	</xsl:template>

    <!-- ============================================================================================= -->

    <xsl:template name="inspire">
		<h1 align="left"><xsl:value-of select="/root/gui/config/inspire"/></h1>
		<div align="left" style="{$style}">
			<table>
				<tr>
					<td class="padded" width="{$width}"><label for="inspire.enable"><xsl:value-of select="/root/gui/config/enable"/></label></td>
					<td class="padded"><input id="inspire.enable" class="content" type="checkbox"/></td>
				</tr>

                <tr>
                	<td/>
					<td class="padded">
                        <div id="inspire.subpanel">
                            <div align="left">
                                <input id="inspire.enableSearchPanel" class="content" type="checkbox"/><label for="inpire.enableSearchPanel"><xsl:value-of select="/root/gui/config/metadataEnableInspireSearch"/></label>
                            </div>
                        </div>
					</td>
				</tr>
			</table>
		</div>
	</xsl:template>

    <!-- ============================================================================================= -->

    <xsl:template name="metadataviews">
        <h1 align="left"><xsl:value-of select="/root/gui/config/metadataViews"/></h1>
		<div align="left" style="{$style}">
			<table>

                <tr>
                	<td/>
					<td class="padded">
                        <div align="left">
                            <input id="metadata.enableSimpleView" class="content" type="checkbox"/><label for="metadata.enableSimpleView"><xsl:value-of select="/root/gui/config/metadataEnableSimpleView"/></label>
                        </div>
                        <div align="left">
                            <input id="metadata.enableIsoView" class="content" type="checkbox"/><label for="metadata.enableIsoView"><xsl:value-of select="/root/gui/config/metadataEnableIsoView"/></label>
                        </div>
                        <div align="left">
                            <input id="metadata.enableInspireView" class="content" type="checkbox"/><label for="metadata.enableInspireView"><xsl:value-of select="/root/gui/config/metadataEnableInspireView"/></label>
                        </div>
                        <div align="left">
                            <input id="metadata.enableXmlView" class="content" type="checkbox"/><label for="metadata.enableXmlView"><xsl:value-of select="/root/gui/config/metadataEnableXmlView"/></label>
                        </div>
					</td>
				</tr>

                 <tr>
                	<td class="padded" width="{$width}"><xsl:value-of select="/root/gui/config/metadataDefaultView"/></td>
					<td class="padded">
                        <select id="metadata.defaultView">
                            <option id="metadata.defaultView.Simple" value="simple"><xsl:value-of select="/root/gui/config/metadataSimpleView"/></option>
                            <option id="metadata.defaultView.Advanced" value="advanced"><xsl:value-of select="/root/gui/config/metadataAdvancedView"/></option>
                            <option id="metadata.defaultView.Iso" value="iso"><xsl:value-of select="/root/gui/config/metadataIsoView"/></option>
                            <option id="metadata.defaultView.Inspire" value="inspire"><xsl:value-of select="/root/gui/config/metadataInspireView"/></option>
                            <option id="metadata.defaultView.Xml" value="xml"><xsl:value-of select="/root/gui/config/metadataXmlView"/></option>
                        </select>
					</td>
				</tr>
			</table>
		</div>
    </xsl:template>
	
	<xsl:template name="metadataprivs">
		<h1 align="left"><xsl:value-of select="/root/gui/config/metadataPrivs"/></h1>
		<div align="left" style="{$style}">
			<table>
				<tr>
					<td class="padded" width="{$width}"><label for="metadata.usergrouponly"><xsl:value-of select="/root/gui/config/md.usergrouponly"/></label></td>
					<td class="padded"><input id="metadata.usergrouponly" class="content" type="checkbox"/></td>
				</tr>
			</table>
		</div>
	</xsl:template>
    <!-- ============================================================================================= -->

	<xsl:template name="harvester">
		<h1 align="left"><xsl:value-of select="/root/gui/config/harvester"/></h1>
		<div align="left" style="{$style}">
			<table>
				<tr>
					<td class="padded" width="{$width}"><label for="harvester.enableEditing"><xsl:value-of select="/root/gui/config/enableEditing"/></label></td>
					<td class="padded"><input id="harvester.enableEditing" class="content" type="checkbox"/></td>
				</tr>
			</table>
		</div>
	</xsl:template>
	
	<!-- ============================================================================================= -->

	<xsl:template name="authentication">
		<h1 align="left"><xsl:value-of select="/root/gui/config/authentication"/></h1>
		<div align="left" style="{$style}">
			<table>
				<tr>
					<td class="padded" width="{$width}"><label for="userSelfRegistration.enable"><xsl:value-of select="concat(/root/gui/config/enable,' ',/root/gui/config/userSelfRegistration)"/></label></td>
					<td><input id="userSelfRegistration.enable" class="content" type="checkbox"/></td>
				</tr>
			</table>
		</div>
		
		<div align="left" style="{$style}">
			<b><xsl:value-of select="concat(/root/gui/config/otherlogins,': ')"/></b>
			<div align="left" style="{$style}">
				<input align="left" type="checkbox" id="shib.use" name="authentication" value="shib"/>
				<label for="shib.use">
					<xsl:value-of select="/root/gui/config/shib"/> 
				</label>
				<xsl:call-template name="shib"/>
			</div>
		</div>
	</xsl:template>

	<!-- ============================================================================================= -->

	<xsl:template name="site">		
		<h1 align="left"><xsl:value-of select="/root/gui/config/site"/></h1>

		<div align="left" style="{$style}">
			<table>
				<tr>
					<td class="padded" width="{$width}"><xsl:value-of select="/root/gui/config/siteId"/></td>
					<td class="padded"><input id="site.siteId" class="content" type="text" value="" size="30"/></td>
				</tr>
				<tr>
					<td class="padded" width="{$width}"><xsl:value-of select="/root/gui/config/name"/></td>
					<td class="padded"><input id="site.name" class="content" type="text" value="" size="30"/></td>
				</tr>
				<tr>
					<td class="padded"><xsl:value-of select="/root/gui/config/organ"/></td>
					<td class="padded"><input id="site.organ" class="content" type="text" value="" size="30"/></td>
				</tr>
			</table>
		</div>
	</xsl:template>

	<!-- ============================================================================================= -->

	<xsl:template name="server">
		<h1 align="left"><xsl:value-of select="/root/gui/config/server"/></h1>

		<div align="left" style="{$style}">
			<table>
                <tr>
					<td class="padded" width="{$width}"><xsl:value-of select="/root/gui/config/protocol"/></td>
					<td class="padded">
                        <select id="server.protocol" class="content">
                            <option value="http">http</option>
                            <option value="https">https</option>
                        </select>

					</td>
				</tr>
				<tr>
					<td class="padded" width="{$width}"><xsl:value-of select="/root/gui/config/host"/></td>
					<td class="padded"><input id="server.host" class="content" type="text" value="" size="30"/></td>
				</tr>
				
				<tr>
					<td class="padded"><xsl:value-of select="/root/gui/config/port"/></td>
					<td class="padded"><input id="server.port" class="content" type="text" value="" size="30"/></td>
				</tr>
			</table>
		</div>
	</xsl:template>

	<!-- ============================================================================================= -->

	<xsl:template name="intranet">
		<h1 align="left"><xsl:value-of select="/root/gui/config/intranet"/></h1>

		<div align="left" style="{$style}">
			<table>
				<tr>
					<td class="padded" width="{$width}"><xsl:value-of select="/root/gui/config/network"/></td>
					<td class="padded"><input id="intranet.network" class="content" type="text" value="" size="30"/></td>
				</tr>
				
				<tr>
					<td class="padded"><xsl:value-of select="/root/gui/config/netmask"/></td>
					<td class="padded"><input id="intranet.netmask" class="content" type="text" value="" size="30"/></td>
				</tr>
			</table>
		</div>
	</xsl:template>

	<!-- ============================================================================================= -->

	<xsl:template name="downloadservice">
		<h1 align="left"><xsl:value-of select="/root/gui/config/downloadservice"/></h1>

		<div align="left" style="{$style}">
			<input align="left" type="radio" id="downloadservice.simple" value="simple" name="downloadservice"/>
			<label for="downloadservice.simple"><xsl:value-of select="/root/gui/config/simple"/></label>
			<div align="left" style="{$style}">
				<span id="downloadservice_simple.subpanel">
					<xsl:value-of select="/root/gui/config/tips/tip[id='downloadservice.simple']"/>
				</span>
			</div>
		</div>
		<div align="left" style="{$style}">
			<input align="left" type="radio" id="downloadservice.withdisclaimer" value="disclaimer" name="downloadservice"/>
			<label for="downloadservice.withdisclaimer"><xsl:value-of select="/root/gui/config/withdisclaimer"/></label>
			<div align="left" style="{$style}">
				<span id="downloadservice_withdisclaimer.subpanel">
					<xsl:value-of select="/root/gui/config/tips/tip[id='downloadservice.withdisclaimer']"/>
				</span>
			</div>
		</div>
		<div align="left" style="{$style}">
			<input align="left" type="radio" id="downloadservice.leave" value="leave" name="downloadservice"/>
			<label for="downloadservice.leave"><xsl:value-of select="/root/gui/config/leave"/></label>
			<div align="left" style="{$style}">
				<span id="downloadservice_leave.subpanel">
					<xsl:value-of select="/root/gui/config/tips/tip[id='downloadservice.leave']"/>
				</span>
			</div>
		</div>
	</xsl:template>

	<!-- ============================================================================================= -->

	<xsl:template name="z3950">
		<h1 align="left"><xsl:value-of select="/root/gui/config/z3950"/></h1>

		<div align="left" style="{$style}">
			<table>
				<tr>
					<td class="padded" width="{$width}"><label for="z3950.enable"><xsl:value-of select="/root/gui/config/enable"/></label></td>
					<td class="padded"><input id="z3950.enable" class="content" type="checkbox"/></td>
				</tr>
	
				<tr>
					<td/>
					<td>
						<table id="z3950.subpanel">
							<tr>
								<td class="padded"><xsl:value-of select="/root/gui/config/port"/></td>
								<td class="padded"><input id="z3950.port" class="content" type="text" value="" size="20"/></td>
							</tr>
						</table>
					</td>
				</tr>			
			</table>
		</div>
	</xsl:template>

	<!-- ============================================================================================= -->

	<xsl:template name="threadedindexing">
		<h1 align="left"><xsl:value-of select="/root/gui/config/threadedindexing"/></h1>

		<div align="left" style="{$style}">
			<table>
				<xsl:variable name="nrProcs" select="threadUtils:getNumberOfProcessors()"/>
				<td class="padded"><xsl:value-of select="/root/gui/config/maxthreads"/></td>
				<td class="padded">
					<input id="threadedindexing.maxthreads" class="content" type="text" size="5">
						<xsl:if test="$nrProcs='1'">
							<xsl:attribute name="value">1</xsl:attribute>
							<xsl:attribute name="style">display:none;</xsl:attribute>
						</xsl:if>
					</input>
					<xsl:if test="$nrProcs='1'">
						<xsl:value-of select="concat(':  ',$nrProcs,'  (',/root/gui/config/reasonForOne,')')"/>
					</xsl:if>
				</td>
				<xsl:if test="$nrProcs!='1'">
					<td class="padded">
						<xsl:value-of select="/root/gui/config/recommendedThreads"/>: <span style="font-weight:bold"><xsl:value-of select="$nrProcs"/></span>&#160;<xsl:value-of select="concat('(',/root/gui/config/reasonForMore,')')"/>
					</td>
				</xsl:if>
			</table>
		</div>
	</xsl:template>

	<!-- ============================================================================================= -->

	<xsl:template name="selectionmanager">
		<h1 align="left"><xsl:value-of select="/root/gui/config/selectionmanager"/></h1>

		<div align="left" style="{$style}">
			<table>
				<td class="padded"><xsl:value-of select="/root/gui/config/maxrecords"/></td>
				<td class="padded"><input id="selection.maxrecords" class="content" type="text" value="" size="10"/></td>
			</table>
		</div>
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

	<xsl:template name="indexoptimizer">
		<h1 align="left"><xsl:value-of select="/root/gui/config/indexoptimizer"/></h1>

		<div align="left" style="{$style}">
			<table>
				<tr>
					<td class="padded" width="{$width}"><label for="indexoptimizer.enable"><xsl:value-of select="/root/gui/config/enable"/></label></td>
					<td class="padded"><input id="indexoptimizer.enable" class="content" type="checkbox"/></td>
				</tr>
	
				<tr>
					<td/>
					<td>
						<table id="indexoptimizer.subpanel">
							<tr>
								<td class="padded"><xsl:value-of select="/root/gui/config/at"/></td>
								<td class="padded">
									<select id="indexoptimizer.at.hour" class="content">
										<xsl:apply-templates mode="selectoptions" select="/root/gui/config/hours/hour"/>
									</select>:
									<select id="indexoptimizer.at.min" class="content">
										<xsl:apply-templates mode="selectoptions" select="/root/gui/config/minutes/minute"/>
									</select>
									<!-- leave seconds hidden - not really necessary? -->
									<input id="indexoptimizer.at.sec"  class="content" type="hidden" value="0" size="2"/>
									&#160;
									<xsl:value-of select="/root/gui/config/atSpec"/>
								</td>
							</tr>
							<tr>
								<td class="padded"><xsl:value-of select="/root/gui/config/interval"/></td>
								<td class="padded">
									<!-- leave days hidden - not really necessary? -->
									<input id="indexoptimizer.interval.day" class="content" type="hidden" value="0" size="2"/>
									<select id="indexoptimizer.interval.hour" class="content">
										<xsl:apply-templates mode="selectoptions" select="/root/gui/config/hourintervals/hour"/>
									</select>
									<!-- leave minutes hidden - not really necessary? -->
									<input id="indexoptimizer.interval.min" class="content" type="hidden" value="0" size="2"/>
									&#160;
									<xsl:value-of select="/root/gui/config/intervalSpec"/>
								</td>
							</tr>
						</table>
					</td>
				</tr>
			</table>
		</div>
	</xsl:template>

	<!-- ============================================================================================= -->

	<xsl:template name="oai">
		<h1 align="left"><xsl:value-of select="/root/gui/config/oai"/></h1>

		<div align="left" style="{$style}">
			<table>
				<tr>
					<td class="padded" width="{$width}"><xsl:value-of select="/root/gui/config/datesearch"/></td>
					<td class="padded">
						<select id="oai.mdmode" class="content">
							<xsl:apply-templates mode="selectoptions" select="/root/gui/config/datesearchopt/dsopt"/>
						</select>
					</td>
				</tr>
	
				<tr>
					<td class="padded" width="{$width}"><xsl:value-of select="/root/gui/config/tokentimeout"/></td>
					<td class="padded"><input id="oai.tokentimeout" class="content" type="text" value="" size="20"/></td>
				</tr>
				<tr>
					<td class="padded" width="{$width}"><xsl:value-of select="/root/gui/config/cachesize"/></td>
					<td class="padded"><input id="oai.cachesize" class="content" type="text" value="" size="20"/></td>
				</tr>
							
			</table>
		</div>
	</xsl:template>

	<!-- ============================================================================================= -->
	<xsl:template name="hyperlinks">
		<h1 align="left"><xsl:value-of select="/root/gui/config/clickablehyperlinks"/></h1>

		<div align="left" style="{$style}">
			<table>
				<tr>
					<td class="padded" width="{$width}"><label for="clickablehyperlinks.enable"><xsl:value-of select="/root/gui/config/enable"/></label></td>
					<td class="padded"><input id="clickablehyperlinks.enable" class="content" type="checkbox" value=""/></td>
				</tr>
			</table>
		</div>
	</xsl:template>

	<!-- ============================================================================================= -->
	<xsl:template name="localrating">
		<h1 align="left"><xsl:value-of select="/root/gui/config/localrating"/></h1>

		<div align="left" style="{$style}">
			<table>
				<tr>
					<td class="padded" width="{$width}"><label for="localrating.enable"><xsl:value-of select="/root/gui/config/enable"/></label></td>
					<td class="padded"><input id="localrating.enable" class="content" type="checkbox" value=""/></td>
				</tr>
			</table>
		</div>
	</xsl:template>
	
	<!-- ============================================================================================= -->
	<xsl:template name="autofixing">
		<h1 align="left"><xsl:value-of select="/root/gui/config/autofixing"/></h1>

		<div align="left" style="{$style}">
			<table>
				<tr>
					<td class="padded" width="{$width}"><label for="autofixing.enable"><xsl:value-of select="/root/gui/config/enable"/></label></td>
					<td class="padded"><input id="autofixing.enable" class="content" type="checkbox" value=""/></td>
				</tr>
			</table>
		</div>
	</xsl:template>

	<!-- ============================================================================================= -->
	<xsl:template name="proxy">
		<h1 align="left"><xsl:value-of select="/root/gui/config/proxy"/></h1>

		<div align="left" style="{$style}">
			<table>
				<tr>
					<td class="padded" width="{$width}"><label for="proxy.use"><xsl:value-of select="/root/gui/config/use"/></label></td>
					<td class="padded"><input id="proxy.use" class="content" type="checkbox" value=""/></td>
				</tr>
				<tr>
					<td/>
					<td>
						<table id="proxy.subpanel">
							<tr>
								<td class="padded"><xsl:value-of select="/root/gui/config/host"/></td>
								<td class="padded"><input id="proxy.host" class="content" type="text" value="" size="20"/></td>
							</tr>
			
							<tr>
								<td class="padded"><xsl:value-of select="/root/gui/config/port"/></td>
								<td class="padded"><input id="proxy.port" class="content" type="text" value="" size="20"/></td>
							</tr>

							<tr>
								<td class="padded"><xsl:value-of select="/root/gui/config/username"/></td>
								<td class="padded"><input id="proxy.username" class="content" type="text" value="" size="20"/></td>
							</tr>

							<tr>
								<td class="padded"><xsl:value-of select="/root/gui/config/password"/></td>
								<td class="padded"><input id="proxy.password" class="content" type="password" value="" size="20"/></td>
							</tr>
						</table>
					</td>
				</tr>			
			</table>
		</div>
	</xsl:template>

	<!-- ============================================================================================= -->

	<xsl:template name="feedback">
		<h1 align="left"><xsl:value-of select="/root/gui/config/feedback"/></h1>

		<div align="left" style="{$style}">
			<table>
				<tr>
					<td class="padded" width="{$width}"><xsl:value-of select="/root/gui/config/email"/></td>
					<td class="padded"><input id="feedback.email" class="content" type="text" value=""/></td>
				</tr>
				
				<tr>
					<td class="padded"><xsl:value-of select="/root/gui/config/smtpHost"/></td>
					<td class="padded"><input id="feedback.mail.host" class="content" type="text" value=""/></td>
				</tr>
				
				<tr>
					<td class="padded"><xsl:value-of select="/root/gui/config/smtpPort"/></td>
					<td class="padded"><input id="feedback.mail.port" class="content" type="text" value=""/></td>
				</tr>
			</table>
		</div>
	</xsl:template>

	<!-- ============================================================================================= -->

	<xsl:template name="removedMetadata">
		<h1 align="left"><xsl:value-of select="/root/gui/config/removedMetadata"/></h1>

		<div align="left" style="{$style}">
			<table>
				<tr>
					<td class="padded" width="{$width}"><xsl:value-of select="/root/gui/config/dir"/></td>
					<td class="padded"><input id="removedMd.dir" class="content" type="text" value=""/></td>
				</tr>			
			</table>
		</div>
	</xsl:template>
	
	<!-- ============================================================================================= -->
	<!-- === Shibboleth panels === -->
	<!-- ============================================================================================= -->
	
	<xsl:template name="shib">

		<div align="left" style="{$style}">
			<table id="shib.subpanel">
				<tr>
					<td class="padded"><xsl:value-of select="/root/gui/config/path"/></td>
					<td class="padded"><input id="shib.path" class="content" type="text" size="256"/></td>
				</tr>

                <tr>
                    <td class="padded"><xsl:value-of select="/root/gui/config/defGroup"/></td>
                    <td class="padded"><xsl:call-template name="shibDefGroup"/></td>
                </tr>

				<!-- shibboleth attributes -->
									
				<tr>
					<td class="padded" colspan="2"><xsl:value-of select="/root/gui/config/attributes"/></td>
				</tr>
				<tr>
					<td/>
					<td class="padded"><xsl:call-template name="shibAttribs"/></td>
				</tr>
			</table>
		</div>
	</xsl:template>
	
	<!-- ============================================================================================= -->
	
	<xsl:template name="shibAttribs">
		<table>
			<tr>
				<td class="padded" width="60px"><xsl:value-of select="/root/gui/config/username"/></td>
				<td class="padded"><input id="shib.attrib.username" class="content" type="text" value="" size="150"/></td>
			</tr>
			<tr>
				<td class="padded"><xsl:value-of select="/root/gui/config/surname"/></td>
				<td class="padded"><input id="shib.attrib.surname" class="content" type="text" value="" size="150"/></td>
			</tr>
			<tr>
				<td class="padded"><xsl:value-of select="/root/gui/config/firstname"/></td>
				<td class="padded"><input id="shib.attrib.firstname" class="content" type="text" value="" size="150"/></td>
			</tr>
			<tr>
				<td class="padded"><xsl:value-of select="/root/gui/config/profile"/></td>
				<td class="padded"><input id="shib.attrib.profile" class="content" type="text" value="" size="150"/></td>
			</tr>

            <tr>
				<td class="padded"><xsl:value-of select="/root/gui/config/group"/></td>
				<td class="padded"><input id="shib.attrib.group" class="content" type="text" value="" size="150"/></td>
			</tr>

		</table>
	</xsl:template>

    <!-- ============================================================================================= -->

    <xsl:template name="shibDefGroup">
        <select class="content" size="1" name="shibgroup" id="shib.defGroup">
                <option value=""></option>
                <xsl:for-each select="/root/gui/groups/record">
                    <xsl:sort select="name"/>
                    <option>
                        <xsl:attribute name="value">
                            <xsl:value-of select="id"/>
                        </xsl:attribute>
                        <xsl:value-of select="name"/>
                    </option>
                </xsl:for-each>
            </select>
    </xsl:template>

	<!-- ============================================================================================= -->
	<!-- === Buttons -->
	<!-- ============================================================================================= -->

	<xsl:template name="buttons">
		<button class="content" onclick="load('{/root/gui/locService}/admin')">
			<xsl:value-of select="/root/gui/strings/back"/>
		</button>
		&#160;
		<button class="content" onclick="config.save()">
			<xsl:value-of select="/root/gui/config/save"/>
		</button>
		&#160;
		<button class="content" onclick="config.refresh()">
			<xsl:value-of select="/root/gui/config/refresh"/>
		</button>
	</xsl:template>

	<!-- ============================================================================================= -->

</xsl:stylesheet>
