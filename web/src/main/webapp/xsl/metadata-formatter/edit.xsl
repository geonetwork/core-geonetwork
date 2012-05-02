<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	
	<xsl:include href="../main.xsl"/>

	<xsl:template mode="css" match="/">
		<xsl:call-template name="geoCssHeader"/>
		<xsl:call-template name="ext-ux-css"/>
	</xsl:template>

	<xsl:template mode="script" match="/">
		<script type="text/javascript" src="../../scripts/ext/adapter/ext/ext-base.js"></script>
		<xsl:choose>
			<xsl:when test="/root/request/debug">
				<script type="text/javascript" src="../../scripts/ext/ext-all-debug.js"></script>
			</xsl:when>
			<xsl:otherwise>
				<script type="text/javascript" src="../../scripts/ext/ext-all.js"></script>
			</xsl:otherwise>
		</xsl:choose>
	
        <script type="text/javascript">
           var lastIdParamValue = 'id=10'
           function view(type, debug) {
           	Ext.Msg.prompt('MetadataID', 'Please the id or uuid param of the metadata to view:', function(btn, idParam){
           		lastIdParamValue = idParam;
			    if (btn == 'ok'){
			        window.open('metadata.formatter.'+type+'?'+idParam+debug+'&amp;xsl=<xsl:value-of select="/root/request/id"/>','_formatter_view_tab','');
			    }
			}, this, false, lastIdParamValue);
           }
         </script>
	
	</xsl:template>

	<xsl:template name="content">
	  <xsl:call-template name="formLayout">
			<xsl:with-param name="title" select="'Edit File'"/>
			<xsl:with-param name="content">
				<form id="data-form"  accept-charset="UTF-8" method="POST"  action="{/root/gui/locService}/metadata.formatter.update">
					<input type="hidden" id="id" name="id" value="{/root/request/id}"/>
					<input type="hidden" id="fname" name="fname" value="{/root/request/fname}"/>
					<textarea style="width: 100%; height: 30em" id="data" name="data">
						<xsl:value-of select="/root/data"/>
					</textarea>
				</form>
			</xsl:with-param>
			<xsl:with-param name="buttons">
				<button class="content" onclick="document.forms['data-form'].submit();">
					<xsl:value-of select="/root/gui/strings/save"/>
				</button>
				<button class="content" onclick="view('xml', '');">
					<xsl:value-of select="/root/gui/strings/view"/> Xml
				</button>
				<button class="content" onclick="view('html', '');">
					<xsl:value-of select="/root/gui/strings/view"/> HTML
				</button>
				<button class="content" onclick="view('xml', '&amp;debug=true');">
					<xsl:value-of select="/root/gui/strings/view"/> Debug
				</button>
			</xsl:with-param>
		</xsl:call-template>
	</xsl:template>

</xsl:stylesheet>