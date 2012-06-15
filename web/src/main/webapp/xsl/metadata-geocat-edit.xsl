<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
    xmlns:gmd="http://www.isotc211.org/2005/gmd"
    xmlns:gco="http://www.isotc211.org/2005/gco"
    xmlns:gmx="http://www.isotc211.org/2005/gmx"
    xmlns:gts="http://www.isotc211.org/2005/gts"
    xmlns:srv="http://www.isotc211.org/2005/srv"
    xmlns:gml="http://www.opengis.net/gml"
    xmlns:che="http://www.geocat.ch/2008/che"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:geonet="http://www.fao.org/geonetwork"
	xmlns:saxon="http://saxon.sf.net/"
    extension-element-prefixes="saxon"
	exclude-result-prefixes="geonet saxon">

	<xsl:template name="geocat-js">
		<xsl:if test="/root/request/debug">
	        <script type="text/javascript" src="{/root/gui/url}/scripts/editor/metadata-geocat-editor.js"></script>
	        <script type="text/javascript" src="{/root/gui/url}/scripts/editor/geocat.edit.js"></script>
	        <script type="text/javascript" src="{/root/gui/url}/scripts/editor/geocat.edit.Contact.js"></script>
	        <script type="text/javascript" src="{/root/gui/url}/scripts/editor/geocat.edit.Extent.js"></script>
	        <script type="text/javascript" src="{/root/gui/url}/scripts/editor/geocat.edit.Format.js"></script>
	        <script type="text/javascript" src="{/root/gui/url}/scripts/editor/geocat.edit.Keyword.js"></script>
        </xsl:if>
		<script type="text/javascript">
			geocat.edit.contactRoles = [];
			<xsl:for-each select="/root/gui/schemas/iso19139/codelists/codelist[@name='gmd:CI_RoleCode']/entry">
				geocat.edit.contactRoles.push(['<xsl:value-of select="code"/>', '<xsl:value-of select="label"/>']);
			</xsl:for-each>
		</script>
	</xsl:template>
	<xsl:template name="geocat-hidden-inputs">
	     <xsl:variable name="locales" select="//gmd:locale"/>
         <xsl:variable name="localesValue">
             <xsl:for-each select="$locales//gmd:PT_Locale">
                 <xsl:value-of select="@id"/>
                 <xsl:if test="position()!=last()">,</xsl:if>
             </xsl:for-each>
         </xsl:variable>

		<!-- Use for adding xlink -->
		
		<input type="hidden" id="xlink.schema" name="schema" value="{geonet:info/schema}" />
		<!-- <input type="hidden" id="xlink.role" name="role" value="embed" />
		    <input type="hidden" id="href" name="href" value="" />
		<input type="hidden" id="xlink.geom" name="geom" value="none" />
		<input type="hidden" id="xlink.show" name="show" value="embed" />
		<input type="hidden" id="xlink.type" name="type" value="simple" />
		<input type="hidden" id="keyword.locales" name="keyword.locales"
			value="{$localesValue}" /> -->
		<!-- Hidden div to contains extra elements like when posting multiple keywords. -->
	</xsl:template>

	<xsl:template name="geocat-xlinkSelector">
		<xsl:variable name="locales" select="//gmd:locale"/>

		<div id="popXLink" name="popXLink" style="display:none;width:460px;padding:10px">

			<!-- Almost common scripaculous autocompleter results div -->
			<div id='xll' class="keywordList" />

			<div id="popXLink.contact">
				<xsl:value-of select="/root/gui/strings/popXlink.contact.search" />
				<br />
				<input type="text" id="xlink-s-contact" value="" size="50"
					style="margin-top:2px;" />
				<span id="xlink.contact.indicator" style="display:none;">
					<img src="../../images/spinner.gif" alt="{/root/gui/strings/searching}"
						title="{/root/gui/strings/searching}" />
				</span>
				<br />
				<br />
				<!-- Codelist for contact role. CI_ResponsibleParty as to defined the 
					role of the contact element. FIXME : in iso19139.che, role could be multiple. -->
				<xsl:value-of select="/root/gui/strings/popXlink.contact.role" />
				<br />
				<select name="contact.role" id="contact.role"
					onChange="contactSetRole(this.options[this.selectedIndex].value);">
					<!-- add point of contact first -->
					                   <!-- add point of contact first -->
					<option value="pointOfContact">
						<xsl:attribute name="selected"></xsl:attribute>
						<xsl:value-of
							select="/root/gui/schemas/iso19139/codelists/codelist[@name='gmd:CI_RoleCode']/entry[code='pointOfContact']/label" />
					</option>

					<!-- add the rest of elements -->
					<xsl:for-each
						select="/root/gui/schemas/iso19139/codelists/codelist[@name='gmd:CI_RoleCode']/entry">
						<xsl:sort select="label" order="ascending" />

						<xsl:if test="code!='pointOfContact'">
							<option value="{code}">
								<xsl:value-of select="label" />
							</option>
						</xsl:if>

					</xsl:for-each>
				</select>
				<br />
				<br />
				<xsl:value-of select="/root/gui/strings/popXlink.contact.action" />
				<br />
			</div>

			<div id="popXLink.format">
				<input type="text" id="xlink-s-format" value="" size="50"
					style="margin-top:2px;" />
				<span id="xlink.format.indicator" style="display: none">
					<img src="../../images/spinner.gif" alt="{/root/gui/strings/searching}"
						title="{/root/gui/strings/searching}" />
				</span>
				<br />
				<xsl:value-of select="/root/gui/strings/popXlink.about" />
				<br />
				<!-- Autocompletion list -->
			</div>

			<div id="popXLink.keyword">
				<span id="xlink.keyword.indicator" style="display: none">
					<img src="../../images/spinner.gif" alt="{/root/gui/strings/searching}"
						title="{/root/gui/strings/searching}" />
				</span>
				<br />
				<br />
				<xsl:value-of select="/root/gui/strings/popXlink.about" />
				<br />
				<br />
				<!-- Div which contains keyword list. -->
				<div id='keywordList' style="padding:2px;margin:2px;" />
			</div>

			<div id="popXLink.extent">
				<input type="text" id="xlink-s-extent" value="" size="50"
					style="margin-top:2px;" />
				<span id="xlink.extent.indicator" style="display: none">
					<img src="../../images/spinner.gif" alt="{/root/gui/strings/searching}"
						title="{/root/gui/strings/searching}" />
				</span>
				<br />
				<br />
				<xsl:value-of select="/root/gui/strings/popXlink.about" />
				<br />
				<br />
				<!-- Autocompletion list -->
				<!--div id="extent.map" style="width:300px; height:250px;"></div -->
				<select name="extent.format" id="extent.format"
					onChange="extentSetFormat(this.options[this.selectedIndex].value);"
					style="display:none;">
					<option value="gmd_bbox">
						<xsl:value-of select="/root/gui/strings/extentBbox" />
					</option>
					<!--<option value="gmd_polygon" selected="true"><xsl:value-of select="/root/gui/strings/extentPolygon"/></option> -->
					<option value="gmd_complete" selected="true">
						<xsl:value-of select="/root/gui/strings/extentBboxAndPolygon" />
					</option>
				</select>
				<select name="extent.type.code" id="extent.type.code"
					onChange="extentTypeCode(this.options[this.selectedIndex].value);">
					<option value="true">
						<xsl:value-of
							select="/root/gui/strings/boolean[@context='gmd:extentTypeCode' and @value=true()]" />
					</option>
					<option value="false">
						<xsl:value-of
							select="/root/gui/strings/boolean[@context='gmd:extentTypeCode' and @value='false']" />
					</option>
				</select>
			</div>

			<!-- common buttons -->
			<button onClick="javascript:submitXLink();">
				<xsl:value-of select="/root/gui/strings/add" />
			</button>
			&#160;
			<button id="common.xlink.create"
				onClick="javascript:createNewXLink();">
				<xsl:value-of select="/root/gui/strings/xlink.new" />
			</button>
			<button id="extent.xlink.create" onClick="javascript:createNewExtent();">
				<xsl:value-of select="/root/gui/strings/xlink.newGeographic" />
			</button>
		</div>
	</xsl:template>

</xsl:stylesheet>