<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0" xmlns:xsl   ="http://www.w3.org/1999/XSL/Transform"
	xmlns:geonet="http://www.fao.org/geonetwork"
	xmlns:gmd="http://www.isotc211.org/2005/gmd"
	xmlns:gco="http://www.isotc211.org/2005/gco"
	xmlns:exslt="http://exslt.org/common"
	exclude-result-prefixes="geonet exslt">
		
	<xsl:include href="main.xsl"/>
		
	<!-- ================================================================================== -->
	<!-- page content -->
	<!-- ================================================================================== -->
	<xsl:template mode="script" match="/" priority="20">
		<script type="text/javascript" src="{/root/gui/url}/scripts/tablednd.js"></script>
	</xsl:template>

	<xsl:template name="content">
		<xsl:call-template name="formLayout">
			<xsl:with-param name="title" select="/root/gui/strings/metadata-template-order"/>
			<xsl:with-param name="content">
				<xsl:choose>
					<xsl:when test="not(/root/gui/templates/record)">
						<table>
							<tr>
								<td>
									<xsl:value-of select="/root/gui/strings/noTemplatesAvailable"/>
								</td>
							</tr>
						</table>
					</xsl:when>
					<xsl:otherwise>
						<xsl:call-template name="template-list-form"/>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:with-param>
			<xsl:with-param name="buttons">
				<xsl:if test="/root/gui/templates/record">
					<button class="content" onclick="save();return false;">
						<xsl:value-of select="/root/gui/strings/save"/>
					</button>
					&#160;
				</xsl:if>
				<button class="content" onclick="goBack()"><xsl:value-of select="/root/gui/strings/back"/></button>
			</xsl:with-param>
		</xsl:call-template>
	</xsl:template>

	<xsl:template name="template-list-form">
		<div id="search-results-content">
			<form id="templateorderform" name="templateorderform" accept-charset="UTF-8" action="{/root/gui/locService}/metadata.templates.displayorder.save" method="post">
				<xsl:comment>list of templates</xsl:comment>
				<xsl:call-template name="display-templates"/>
			</form>
		</div>

		<script>
			var table = document.getElementById('templates-table');
			var tableDnD = new TableDnD();
			tableDnD.init(table);
			tableDnD.onDrop = reArrange;			
			var templateOrderList = new Array();
			
			window.onload = initTemplateOrderList;
			
			// TODO move to utilities file.
			function getInternetExplorerVersion()
				// Returns the version of Internet Explorer or a -1
				// (indicating the use of another browser).
			{
				var rv = -1; // Return value assumes failure.
				if (navigator.appName == 'Microsoft Internet Explorer')
				{
					var ua = navigator.userAgent;
					var re  = new RegExp("MSIE ([0-9]{1,}[\.0-9]{0,})");
					if (re.exec(ua) != null)
						rv = parseFloat( RegExp.$1 );
				}
				return rv;
			}


			function initTemplateOrderList() {
				var table = $('templates-table');
				var rows = table.getElementsByTagName('tr');
				for(var i = 0; i &lt; rows.length; i++) {
					var row = rows[i];
					// get metadata id and title
					var firstTD = row.getElementsByTagName("td")[0];
					var dv = firstTD.getElementsByTagName("div")[0];
					var metadataId = dv.getAttribute('value');
					var title = dv.innerHTML;
					// get displayorder
					var ip = firstTD.getElementsByTagName("input")[0];
					var displayorder = ip.getAttribute('value');
					// store in array
					var template = new Object();
					template.id = metadataId;
					template.title = title;
					template.displayorder = displayorder;
					templateOrderList.push(template);
				}
				var s = "";
				for(var i = 0;i&lt;templateOrderList.length;i++) {
					var template = templateOrderList[i];
					s += ": " + template.id + " : " + template.displayorder + "\n";
				}
				//alert(s);
			}
			
			function copyArrayToForm() {
				// sort the Array by displayOrder
				templateOrderList.sort(function(a, b) { 
						return (a.displayorder - b.displayorder)
					});			
					
				var str="";
				for(var i = 0;i&lt;templateOrderList.length;i++) {
					str+=" " + templateOrderList[i].displayorder;
				}
				//alert(str);
				
				var table = $('templates-table');
				// remove everything from table
				if (table.hasChildNodes()) {
					while(table.childNodes.length >= 1) {
						table.removeChild( table.firstChild );       
					} 
				}
				var c0 = document.createElement("col");
				c0.style.width = "300px";
				table.appendChild(c0);
				var tb = table.appendChild(document.createElement("tbody"));
				for(var i = 0;i&lt;templateOrderList.length;i++) {
					var tr = document.createElement("tr");
					var td = document.createElement("td");
					td.setAttribute("class", "bottom_border");
					var div = document.createElement("div");
					// you're not using IE
					if(getInternetExplorerVersion() == -1) {
						div.setAttribute("style","margin:3px;");
					}
					// you're using IE
					else {
						div.style.cssText = "margin:3px;";
					}
					div.setAttribute("value", templateOrderList[i].id);
					div.innerHTML = templateOrderList[i].title;
					var input = document.createElement("input");
					input.setAttribute("type", "hidden");
					input.setAttribute("value", templateOrderList[i].displayorder);
					input.setAttribute("name", "displayorder-" + templateOrderList[i].id);
					div.appendChild(input);
					td.appendChild(div);
					tr.appendChild(td);
					tb.appendChild(tr);

				}
			}
			
			/** 
			 */
			function reArrange(table, droppedRow) {
				if(targetRow != null) {
					var draggedTD = droppedRow.getElementsByTagName("td")[0];
					var droppedTD = targetRow.getElementsByTagName("td")[0];

					var draggedDv = draggedTD.getElementsByTagName("div")[0];
					var draggedMetadataId = draggedDv.getAttribute('value');

					var draggedIp = draggedTD.getElementsByTagName("input")[0];
					var draggedDisplayorder = draggedIp.getAttribute('value');
					
					var droppedDv = droppedTD.getElementsByTagName("div")[0];
					var droppedMetadataId = droppedDv.getAttribute('value');

					var droppedIp = droppedTD.getElementsByTagName("input")[0];
					var droppedDisplayorder = droppedIp.getAttribute('value');
						
					// swap them in the Array
					for(var i = 0; i&lt;templateOrderList.length; i++) {
						var template = templateOrderList[i];

						if(template.id == draggedMetadataId) {
							template.displayorder = droppedDisplayorder;
						}
						if(template.id == droppedMetadataId) {
							template.displayorder = draggedDisplayorder;
						}
					}
					copyArrayToForm();
					targetRow = null;
					tableDnD.init(table);
				}
			}

			function save() {
				$('waiting').style.display = 'block';
				copyArrayToForm();
				var myAjax = new Ajax.Request(
				getGNServiceURL('metadata.templates.displayorder.save'), 
					{
						method: 'post',
						parameters: $('templateorderform').serialize(), 
							onSuccess: success,
							onFailure: failed
					});
			}
			function success() {
				$('waiting').style.display = 'none';
				targetRow = null;
				tableDnD.init(table);				
			}
			function failed() {
				alert('save failed');
			}
		</script>	
			
	</xsl:template>
	
	<!-- ================================================================================== -->
	<!-- display templates -->
	<!-- ================================================================================== -->

	<xsl:template name="display-templates">
		<xsl:comment>templates</xsl:comment>		
		<div id="template-list" class="select-metadata-hits" style="margin:30px 0px 30px 0px;">
			<table id="templates-table" style="table-layout: fixed; width: 100%;" class="text-aligned-left">
				<col style="width:300px;"/>
				<xsl:for-each select="/root/gui/templates/record">
					<xsl:sort select="displayorder" data-type="number"/>
					<tr id="{id}-row">
						<td class="bottom_border">
							<!-- <input name="id-{id}" type="hidden" value="{id}"/> -->
							<div value="{id}" style="margin:3px;">
								<xsl:value-of select="name"/>
							</div>
							<input name="displayorder-{id}" type="hidden" value="{displayorder}"/>
						</td>						
					</tr>
				</xsl:for-each>
			</table>
			<div id="waiting" style="display:none;margin-left:20px;">
				<img src="{/root/gui/url}/images/spinner.gif" alt="" title=""/>
			</div>
		</div>
	</xsl:template>
		
</xsl:stylesheet>
