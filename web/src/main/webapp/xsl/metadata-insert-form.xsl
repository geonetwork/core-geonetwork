<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<xsl:include href="main.xsl"/>
    <xsl:include href="metadata-insert-form-utils.xsl"/>
    
	<!-- ================================================================================ -->
	<!-- additional scripts -->
	<!-- ================================================================================ -->
	<xsl:template mode="script" match="/">
		<script type="text/javascript" language="JavaScript">
			function init() {
				var modeValue = getModeValue();
                
                insertMode(modeValue);
                
                if (modeValue == 1)
                    updateForm();
			}
			
			function getModeValue() {
                for (var i=0; i &lt; document.xmlinsert.insert_mode.length; i++){
                    if (document.xmlinsert.insert_mode[i].checked){
                        var modeValue = document.xmlinsert.insert_mode[i].value;
                    }
                }
                return modeValue;
            }
            
            function getFileValue() {
                for (var i=0; i &lt; document.xmlinsert.file_type.length; i++){
                    if (document.xmlinsert.file_type[i].checked){
                        var fileValue = document.xmlinsert.file_type[i].value;
                    }
                }
                return fileValue;
            }
            
            function updateForm() {
                var fileType = getFileValue();
                
                if (fileType == 'single')       displayAllForm(true);
                else if (fileType == 'mef')     displayAllForm(false);
            }
            
            function insertMode(value) {
                if (value == 0) {
                    Element.hide('gn.fileUp');
                    Element.show('gn.xmlUp');
                    Element.show('gn.type');
                    $('gn.fileType').style.display='none';
                    document.xmlinsert.enctype="application/x-www-form-urlencoded";
					document.xmlinsert.encoding="application/x-www-form-urlencoded";
                    document.xmlinsert.action=Env.locService+"/metadata.insert.paste";
                    document.xmlinsert.target='_self';
                } else {
                    Element.hide('gn.xmlUp');
                    Element.show('gn.fileUp');
                    Element.hide('gn.type');
                    $('gn.fileType').style.display='';
                    document.xmlinsert.enctype="multipart/form-data";
					document.xmlinsert.encoding="multipart/form-data";
                    document.xmlinsert.action=Env.locService+"/mef.import.ui";
                    document.xmlinsert.target='_self';
                }
            }
            
            function displayAllForm(show) {
                var display;

                if (show == true) {
                    display='';
                    mefdisplay='none';
                }
                else {
                    display='none';
                    mefdisplay='';
                }
                
                displayElement('gn.type',display);
                displayElement('gn.stylesheet',display);
                displayElement('gn.validate',display);
                displayElement('gn.groups',display);
                displayElement('gn.categories',display);
                displayElement('gn.assign',mefdisplay);
                
            }
            
            function displayElement(id, value) {
                if ($(id))
                    $(id).style.display=value;
            }
            
            function doFileUpload(){
                var response = $('upFrame').contentWindow.document;
                var elt;
                var error;
                
                if (response.XMLDocument){
                	elt = response.XMLDocument.selectSingleNode('id');
                	error = response.XMLDocument.selectSingleNode('error');
                } else {
                	elt = response.getElementsByTagName('id')[0];
                	error = response.getElementsByTagName('error')[0];  
                }
                
                if (elt != null || error != null) {
                	$('btInsert').style.display='none';
                    $('gn.insertTable').style.display = 'none';
                	if (elt != null) {
                		var id;
                		if (response.XMLDocument)
                			id= elt.text;
                		else
                			id = elt.firstChild.nodeValue;
                		createResponseForm(id);
                	} else if (error != null){
                		displayError(error.textContent);
                	}
                    $('gn.result').style.display = 'block';
                }   
            }
            
            /**
             * Create link to inserted metadata records.
             */
            function createResponseForm(value) {
                var ids = value.split(";");
                var contentElt = $('gn.resultContent');
                for (var i=0; i &lt; ids.length - 1; i++) {
                    var id = document.createTextNode(ids[i]);
                    var element = document.createElement("a");
                    var space = document.createTextNode("&#160;");
                    
                    element.setAttribute("href", "metadata.show?id=" + ids[i] + "&amp;currTab=simple");
                    element.appendChild(id);
                    contentElt.appendChild(element);
                    contentElt.appendChild(space);
                }
                
            }
            
            
            function displayError(errorMessage) {
            	var content = $('gn.resultContent');
            	$('gn.resultTitle').innerHTML = "" ;
           		if (!$('error')) {
           			var msg = document.createTextNode(errorMessage);
           			content.appendChild(msg);
           			$('back').onclick = function () { 
           				load('metadata.xmlinsert.form');
           			};
           		}
            }

            function submitInsertForm() {
                if (getModeValue() == '1') {
                    if ($('mefFileContent').value == '') {
                        alert('<xsl:value-of select="/root/gui/strings/selectMetadataFileAlert"/>');
                        return false;
                    }
                }

                goSubmit('xmlinsert');
            }
		</script>
	</xsl:template>

	<!-- ================================================================================ -->
	<!-- page content	-->
	<!-- ================================================================================ -->

	<xsl:template name="content">
		<xsl:call-template name="formLayout">
			<xsl:with-param name="title" select="/root/gui/strings/xmlInsert"/>
			<xsl:with-param name="content">
				<form name="xmlinsert" accept-charset="UTF-8" method="post" action="{/root/gui/locService}/metadata.insert" enctype="multipart/form-data">
					<input type="submit" style="display: none;" />
					<table id="gn.insertTable" class="text-aligned-left">
						<tr>
                            <th class="padded" valign="top"><xsl:value-of select="/root/gui/strings/insertMode"/></th>
                            <td>
                                <table>
                                    <tr>
                                        <td class="padded">
                                            <input type="radio" id="insertFileMode" name="insert_mode" value="1" 
                                                onclick="insertMode(this.value)" checked="true"/>
                                            <label for="insertFileMode"><xsl:value-of select="/root/gui/strings/insertFileMode"/></label>
                                        </td>
                                        <td class="padded">
                                            <input type="radio" id="insertPasteMode" name="insert_mode" value="0" 
                                                onclick="$('singleFile').checked=true;displayAllForm(true);insertMode(this.value)"/>
                                            <label for="insertPasteMode"><xsl:value-of select="/root/gui/strings/insertPasteMode"/></label>
                                            <xsl:text>&#160;</xsl:text>
                                        </td>
                                    </tr>
                                </table>
                            </td>
                        </tr>
                        <tr id="gn.fileType">
                            <th class="padded" valign="top"><xsl:value-of select="/root/gui/strings/fileType"/></th>
                            <td>
                                <table>
                                    <tr>
                                        <td class="padded">
                                            <input type="radio" id="singleFile" name="file_type" value="single" onclick="displayAllForm(this.checked)" checked="true"/>
                                            <label for="singleFile"><xsl:value-of select="/root/gui/strings/singleFile"/></label>
                                            <xsl:text>&#160;</xsl:text>
                                        </td>
                                        <td class="padded">
                                            <input type="radio" id="mefFile" name="file_type" value="mef" onclick="displayAllForm(false)"/>
                                            <label for="mefFile"><xsl:value-of select="/root/gui/strings/mefFile"/></label>
                                            <xsl:text>&#160;</xsl:text>
                                        </td>
                                    </tr>
                                </table>
                            </td>
                        </tr>
                        <tr>
                            <th class="padded" valign="top"><xsl:value-of select="/root/gui/strings/metadata"/></th>
                            <td class="padded">
                                <span id="gn.xmlUp" style="display:none">
                                    <textarea class="content" name="data" cols="80" rows="20"/>
                                </span>
                                <span id="gn.fileUp" style="display:none">
                                    <input type="file" accept="*.xml, *.zip, *.mef" class="content" size="60" name="mefFile" id="mefFileContent" value=""/>
                                </span>
                            </td>
                        </tr>
                        <xsl:call-template name="metadata-insert-common-form"/>

						
					</table>
					<iframe id="upFrame" name="upFrame" width="200p" height="100p" onload="javascript:doFileUpload();" style="display:none;"/>
                    <table id="gn.result" style="display:none;">
	                    <tr>
	                        <th id="gn.resultTitle" class="padded-content">
	                            <h2><xsl:value-of select="/root/gui/strings/newMdInsert" /></h2>
	                        </th>
	                        <td id="gn.resultContent" class="padded-content" />
	                    </tr>
                    </table>
				</form>
			</xsl:with-param>
			<xsl:with-param name="buttons">
				<button class="content" onclick="goBack()" id="back"><xsl:value-of select="/root/gui/strings/back"/></button>
				&#160;
				<button class="content" onclick="return submitInsertForm();"  id="btInsert"><xsl:value-of select="/root/gui/strings/insert"/></button>
			</xsl:with-param>
		</xsl:call-template>
	</xsl:template>

	<!-- ================================================================================ -->

</xsl:stylesheet>
