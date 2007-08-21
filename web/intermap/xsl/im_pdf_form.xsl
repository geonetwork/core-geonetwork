<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:output method="html"/>

	<xsl:template match="/">

		<div> 			
			Page size: 
			<select id="pdf_pagesize">
				<option value="A4">A4</option>
				<option value="letter">Letter</option>
				<option value="legal">Legal</option>
			</select>
			
			Orientation: 
			<select id="pdf_orientation">
				<option value="portrait">Portrait</option>
				<option value="landscape">Landscape</option>
			</select>
			<br/>
						
<!--			<input type="radio" name="orientation" value="portrait">Portrait</input>
			<input type="radio" name="orientation" value="landscape">Landscape</input>
			<br/>
-->
			<input type="checkbox" name="pdf_layerlist" id="pdf_layerlist" checked="true">Print layerlist</input>
			<br/>
			
			<input type="checkbox" name="pdf_details" id="pdf_details">Print details</input>
			<br/>

			<input type="checkbox" name="pdf_boundingbox" id="pdf_boundingbox">Print bounding box</input>
			<br/>
			
			<div id="im_requestpdf">
				<button onClick="javascript:im_requestPDF();">Generate PDF</button>
			</div>
			<div id="im_requestingpdf" class="im_extra_status" style="display:none;">
				Building PDF... please wait...
			</div>
			<div id="im_builtpdf" class="im_extra_status" style="display:none;">
				PDF successfully built
			</div>
						
		</div>

	</xsl:template>

<!--
	<div id="im_wbtitle">
		<h1>Export this map as PDF</h1>
	</div>
	<div id="im_wbcloser">
		<img title="Close" src="/intermap/images/close.png"/>
	</div>
	<div class="im_wbcontent" id="im_createPDF">
		<h1 id="im_serverList_title">TODO</h1>
	</div>
-->
</xsl:stylesheet>
