<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<xsl:template match="/root/response/layersRoot">
		<div id="im_layersHeader">
			<h3>
				<xsl:value-of select="/root/gui/strings/layers"/>
			</h3>
			<!-- Layers -->
			<div id="im_layersDiv">
				<ul id="im_layerList" />
			</div>
			<table id="im_refresh">
				<tr>
					<td>
						<button id="im_refreshButton" class="disabled">
							<xsl:value-of select="/root/gui/strings/refresh" />
						</button>
					</td>
				</tr>
			</table>
		</div>
		
		<!-- layers toolbar -->
		<div id="im_layersToolbar">
			<!-- delete layer button -->
			<span id="im_deleteLayerButton">delete</span>
			<xsl:text> </xsl:text>
			<!-- add layer button -->
<!-- 
			<span id="addLayersButton">add</span>
 -->
			<a href="{/root/gui/locService}/mapServers.listServers">add</a>
			<!-- event listeners -->
			<script language="javascript">
			// <![CDATA[
				// Event.observe('addLayersButton', 'click', function(e){ addButtonPushed(); });
				Event.observe('im_deleteLayerButton', 'click', function(e){ deleteButtonListener(); });
				Event.observe('im_refreshButton', 'click', function(e){ refreshButtonListener(); });
			// ]]>
			</script>
		</div>
	</xsl:template>
</xsl:stylesheet>
