<%@ taglib uri="/tags/struts-bean" prefix="bean" %>
<%@ taglib uri="/tags/struts-html" prefix="html" %>
<%@ taglib uri="/tags/struts-logic" prefix="logic" %>

<%
//This file contains some ugly JSP code.
//Be wary, ye who brave the dragon's lair.
%>

<script type="text/javascript">
<!--
function addStyle(){
	for(i=0;i<document.coveragesEditorForm.panelStyleIds.length;i++){
		if(document.coveragesEditorForm.panelStyleIds.options[i].selected == true){
 			new_element = new Option(document.coveragesEditorForm.panelStyleIds.options[i].value,document.coveragesEditorForm.panelStyleIds.options[i].value,false,false);
 			document.coveragesEditorForm.otherSelectedStyles.options[document.coveragesEditorForm.otherSelectedStyles.length] = new_element;
 		}
 	}
}
function removeStyle(){
	var selected=0;
	for(i=0;i<document.coveragesEditorForm.otherSelectedStyles.length;i++){
		if(document.coveragesEditorForm.otherSelectedStyles.options[i].selected == true){
			selected++;
		}
		if(selected>0){
			for(i=0;i<document.coveragesEditorForm.otherSelectedStyles.length;i++){
				if(document.coveragesEditorForm.otherSelectedStyles.options[i].selected == true){
					document.coveragesEditorForm.otherSelectedStyles.options[i] = null;
				}
			}
		removeStyle();
		}
	}
}

function prepareFormData(){
	for(i=0;i<document.coveragesEditorForm.otherSelectedStyles.length;i++){
		document.coveragesEditorForm.otherSelectedStyles.options[i].selected = true;
	}
}

function generateColorPicker(colorFieldName, fieldValue)
{
	result = '<input name="'+colorFieldName+'" id="'+colorFieldName+'" size="10" value="'+fieldValue+'" ';
	result += 'onChange="relateColor(\''+colorFieldName+'\', fieldValue);"> ';
	result += '<a href="javascript:pickColor(\''+colorFieldName+'\');" id="pick'+colorFieldName+'" name="pick'+colorFieldName+'" style="border: 1px solid #000000; font-family:Verdana; font-size:10px; background=#FFFF33; ';
	result += 'text-decoration: none; " ><img src="colorpicker.jpg" width=12 height=12 border="none"></a>';
	result += '<script language="javascript">relateColor(\'pick'+colorFieldName+'\', getObj(\''+colorFieldName+'\').value);</script> ';

	return result;
}

// Flooble Color Picker
// Color Picker Script from Flooble.com
// For more information, visit 
//	http://www.flooble.com/scripts/colorpicker.php
// Copyright 2003 Animus Pactum Consulting inc.
// You may use and distribute this code freely, as long as
// you keep this copyright notice and the link to flooble.com
// if you chose to remove them, you must link to the page
// listed above from every web page where you use the color
// picker code.
//---------------------------------------------------------
     var perline = 15;
     var divSet = false;
     var curId;
     var colorLevels = Array('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F');
     var colorArray = Array();
     var ie = false;
     var nocolor = 'none';
	 if (document.all) { ie = true; nocolor = ''; }
	 function getObj(id) {
		if (ie) { return document.all[id]; } 
		else {	return document.getElementById(id);	}
	 }

     function addColor(r, g, b) {
     	var red = colorLevels[r];
     	var green = colorLevels[g];
     	var blue = colorLevels[b];
     	addColorValue(red, green, blue);
     }

     function addColorValue(r, g, b) {
     	colorArray[colorArray.length] = '#' + r + r + g + g + b + b;
     }
     
     function setColor(color) {
     	var link = getObj('pick'+curId);
     	var field = getObj(curId);// + 'field'
     	var picker = getObj('colorpicker');
     	field.value = color;
     	if (color == '') {
			link.innerHTML="&nbsp;&nbsp;&nbsp;";
	     	link.style.background = nocolor;
	     	link.style.color = nocolor;
	     	color = nocolor;
     	} else {
			link.innerHTML="&nbsp;&nbsp;&nbsp;";
	     	link.style.background = color;
	     	link.style.color = color;
	    }
     	picker.style.display = 'none';
	    eval(getObj(curId).title);// + 'field'
     }
        
     function setDiv() {     
     	if (!document.createElement) { return; }
        var elemDiv = document.createElement('div');
        if (typeof(elemDiv.innerHTML) != 'string') { return; }
        genColors();
        elemDiv.id = 'colorpicker';
	    elemDiv.style.position = 'absolute';
        elemDiv.style.display = 'none';
        elemDiv.style.border = '#000000 1px solid';
        elemDiv.style.background = '#FFFFFF';
        elemDiv.innerHTML = '<span style="font-family:Verdana; font-size:11px;">Pick a color: ' 
          	+ '(<a href="javascript:setColor(\'\');">No color</a>)<br>' 
        	+ getColorTable() 
        	+ '<center><a href="http://www.flooble.com/scripts/colorpicker.php"'
        	+ ' target="_blank">color picker</a> by <a href="http://www.flooble.com" target="_blank"><b>flooble</b></a></center></span>';

        document.body.appendChild(elemDiv);
        divSet = true;
     }
     
     function pickColor(id) {
     	if (!divSet) { setDiv(); }
     	var picker = getObj('colorpicker');     	
		if (id == curId && picker.style.display == 'block') {
			picker.style.display = 'none';
			return;
		}
     	curId = id;
     	var thelink = getObj(id);
     	picker.style.top = getAbsoluteOffsetTop(thelink) + 20;
     	picker.style.left = getAbsoluteOffsetLeft(thelink);     
	picker.style.display = 'block';
     }
     
     function genColors() {
        addColorValue('0','0','0');
		//addColorValue('1','1','1');
		addColorValue('2','2','2');
        addColorValue('3','3','3');
		addColorValue('4','4','4');
		addColorValue('5','5','5');
        addColorValue('6','6','6');
		addColorValue('7','7','7');
        addColorValue('8','8','8');
        addColorValue('9','9','9');
        addColorValue('A','A','A');
		addColorValue('B','B','B');
        addColorValue('C','C','C');
		addColorValue('D','D','D');
        addColorValue('E','E','E');
        addColorValue('F','F','F');
			
        for (a = 1; a < colorLevels.length; a++)
			addColor(0,0,a);
        for (a = 1; a < colorLevels.length - 1; a++)
			addColor(a,a,5);

        for (a = 1; a < colorLevels.length; a++)
			addColor(0,a,0);
        for (a = 1; a < colorLevels.length - 1; a++)
			addColor(a,5,a);
			
        for (a = 1; a < colorLevels.length; a++)
			addColor(a,0,0);
        for (a = 1; a < colorLevels.length - 1; a++)
			addColor(5,a,a);
			
			
        for (a = 1; a < colorLevels.length; a++)
			addColor(a,a,0);
        for (a = 1; a < colorLevels.length - 1; a++)
			addColor(5,5,a);
			
        for (a = 1; a < colorLevels.length; a++)
			addColor(0,a,a);
        for (a = 1; a < colorLevels.length - 1; a++)
			addColor(a,5,5);

        for (a = 1; a < colorLevels.length; a++)
			addColor(a,0,a);			
        for (a = 1; a < colorLevels.length - 1; a++)
			addColor(5,a,5);
			
       	return colorArray;
     }
     function getColorTable() {
         var colors = colorArray;
      	 var tableCode = '';
         tableCode += '<table border="0" cellspacing="1" cellpadding="1">';
         for (i = 0; i < colors.length; i++) {
              if (i % perline == 0) { tableCode += '<tr>'; }
              tableCode += '<td bgcolor="#000000"><a style="outline: 1px solid #000000; color: ' 
              	  + colors[i] + '; background: ' + colors[i] + ';font-size: 10px;" title="' 
              	  + colors[i] + '" href="javascript:setColor(\'' + colors[i] + '\');">&nbsp;&nbsp;&nbsp;</a></td>';
              if (i % perline == perline - 1) { tableCode += '</tr>'; }
         }
         if (i % perline != 0) { tableCode += '</tr>'; }
         tableCode += '</table>';
      	 return tableCode;
     }
     function relateColor(id, color) {
     	var link = getObj(id);
     	if (color == '') {
	     	link.style.background = nocolor;
	     	link.style.color = nocolor;
	     	color = nocolor;
     	} else {
	     	link.style.background = color;
	     	link.style.color = color;
	    }
	    //eval(getObj(id + 'field').title);
     }
     function getAbsoluteOffsetTop(obj) {
     	var top = obj.offsetTop;
     	var parent = obj.offsetParent;
     	while (parent != document.body) {
     		top += parent.offsetTop;
     		parent = parent.offsetParent;
     	}
     	return top;
     }
     
     function getAbsoluteOffsetLeft(obj) {
     	var left = obj.offsetLeft;
     	var parent = obj.offsetParent;
     	while (parent != document.body) {
     		left += parent.offsetLeft;
     		parent = parent.offsetParent;
     	}
     	return left;
     }

//-->
</script>

<% try { %>
<html:form action="/config/data/coverageEditorSubmit" onsubmit="prepareFormData()">
  <table class="info">
	<tr>
      <td class="label">
		<span class="help" title="<bean:message key="help.coverage.name"/>">
          <bean:message key="label.name"/>:
        </span>
      </td>
      <td class="datum">
		<bean:write name="coveragesEditorForm" property="name"/>
		<html:hidden property="name"/>
		<html:hidden property="newCoverage" value="false"/>
      </td>
    </tr>
	<tr>
      <td class="label">
		<span class="help" title="<bean:message key="help.wms.path"/>">
        <bean:message key="label.wms.path"/>:
      </span>
	  </td>
	  <td class="datum">
		<html:text property="wmsPath" size="60"/>
	  </td>
	</tr>    
	<tr>
      <td class="label">
		<span class="help" title="<bean:message key="help.type.style"/>">
          <bean:message key="label.style"/>:
        </span>
	  </td>
	  <td class="datum">
      	<html:select property="styleId">
        	<html:options property="styles"/>
		</html:select>
	  </td>
	</tr>   
	<tr>
	  <td class="label">
		<span class="help" title="<bean:message key="help.type.style"/>">
          <bean:message key="label.style"/>:
        </span>
      </td>
      <td class="datum">
        <table>
        	<tr>
        		<td>
        			<html:select property="panelStyleIds" style="width:130" multiple="multiple">
          				<html:options property="styles"/>
        			</html:select>
        		</td>
        		<td style="font-size:10">
        			<input type="button" value=">>" style="width:30" onClick="addStyle()">
        			<br>
        			<input type="button" value="<<" style="width:30" onClick="removeStyle()">
        		</td>
        		<td>
        			<html:select property="otherSelectedStyles" style="width:130" multiple="multiple">
        				<html:options property="typeStyles"/>
        			</html:select>
        		</td>
        	</tr>
        </table> 
      </td>
    </tr> 
	<tr>
      <td class="label">
		<span class="help" title="<bean:message key="help.coverage.srsName"/>">
          <bean:message key="label.SRS"/>:
        </span>
      </td>
	  <td class="datum">
	  	<table>
	  	<tr>
			<td>
			<html:text property="srsName" size="32"/>
			</td>
			<td>
	        <html:submit property="action">
	          <bean:message key="config.data.lookupSRS.label"/>
	        </html:submit>
	        </td>
			<td>
			    <a href="<bean:message key="label.SRSHelp.URL"/>">
	              <bean:message key="label.SRSHelp"/>
	            </a>
	        </td>
	        <td>
	        &nbsp;-&nbsp;
	        <a href="../../../srsHelp.do">
	              <bean:message key="label.SRSList"/>
	            </a>
	        </td>
        </tr>
        </table>
	  </td>
	</tr>
	<tr>
	  <td>
	  </td>
	  <td class="greyedOut2">
      	* <bean:message key="help.coverage.nativeCRS"/>
      </td>
	</tr>
	<!------------------------->
	<!------ This puts in the SRS WKT definition --->
	
	<tr>
	<td class="label">
		<span class="help" title="<bean:message key="help.type.srswkt"/>">
          <bean:message key="label.type.crswkt"/>:
        </span>
	  </td>
	  <td class="greyedOut2">
              <bean:write name="coveragesEditorForm" property="WKTString"/>
			  <html:hidden property="WKTString"/>
            </td>
	</tr>
	
	
	<!-------------------------->
	
    <tr>
      <td class="label">
		<span class="help" title="<bean:message key="help.coverage.label"/>">
          <bean:message key="label.coverageLabel"/>:
        </span>
	  </td>
	  <td class="datum">
		<html:text property="label" size="60"/>
	  </td>
	</tr>

    <tr>
    <td class="label">
		<span class="help" title="<bean:message key="help.coverage.description"/>">
        <bean:message key="label.copverageDescription"/>:
      </span>
	  </td>
	  <td class="datum">
		<html:text property="description" size="60"/>
	  </td>
	</tr>

    <tr>
    <td class="label">
		<span class="help" title="<bean:message key="help.coverage.metadataLink"/>">
        <bean:message key="label.metadataLink"/>:
      </span>
	  </td>
	  <td class="datum">
		<html:text property="metadataLink" size="60"/>
	  </td>
	</tr>

    <tr>
      <td class="label">
		<span class="help" title="<bean:message key="help.coverage.envelope"/>">
          <bean:message key="label.envelope"/>:          
        </span>
	  </td>
	  <td class="datum">
        <html:submit property="action">
          <bean:message key="config.data.calculateBoundingBox.label"/>
        </html:submit><br/>
        <table border=0>
          <tr>
            <td style="white-space: nowrap;">
              <span class="help" title="<bean:message key="help.coverage.minx"/>">
                <bean:message key="label.coverage.minx"/>:
              </span>
            </td>
            <td>
              <html:text property="minX" size="15"/>
            </td>
            <td style="white-space: nowrap;">
              <span class="help" title="<bean:message key="help.coverage.miny"/>">
                <bean:message key="label.coverage.miny"/>:
              </span>
            </td>
            <td>
              <html:text property="minY" size="15"/>
            </td>
          </tr>
          <tr>
            <td style="white-space: nowrap;">
              <span class="help" title="<bean:message key="help.coverage.maxx"/>">
                <bean:message key="label.coverage.maxx"/>:
              </span>
            </td>
            <td>
              <html:text property="maxX" size="15"/>
            </td>
            <td style="white-space: nowrap;">
              <span class="help" title="<bean:message key="help.coverage.maxy"/>">
                <bean:message key="label.coverage.maxy"/>:
              </span>
            </td>
            <td>
              <html:text property="maxY" size="15"/>
            </td>
          </tr>
        </table>
	  </td>
    </tr>

    <tr>
    <td class="label">
		<span class="help" title="<bean:message key="help.dataCoverageKeywords"/>">
			<bean:message key="label.keywords"/>:
		</span>
	  </td>
	  <td class="datum">
		<html:textarea property="keywords" cols="60" rows="2"/>
	  </td>
    </tr>

    <tr>
    <td class="label">
		<span class="help" title="<bean:message key="help.coverage.requestCRSs"/>">
			<bean:message key="label.requestCRSs"/>:
		</span>
	  </td>
	  <td class="datum">
		<html:textarea property="requestCRSs" cols="60" rows="2"/>
	  </td>
    </tr>
    
    <tr>
    <td class="label">
		<span class="help" title="<bean:message key="help.coverage.responseCRSs"/>">
			<bean:message key="label.responseCRSs"/>:
		</span>
	  </td>
	  <td class="datum">
		<html:textarea property="responseCRSs" cols="60" rows="2"/>
	  </td>
    </tr>

    <tr>
    <td class="label">
		<span class="help" title="<bean:message key="help.coverage.nativeFormat"/>">
        <bean:message key="label.nativeFormat"/>:
      </span>
	  </td>
	  <td class="datum">
		<bean:write name="coveragesEditorForm" property="nativeFormat"/>
		<html:hidden property="nativeFormat"/>
	  </td>
	</tr>

    <tr>
    <td class="label">
		<span class="help" title="<bean:message key="help.coverage.supportedFormats"/>">
			<bean:message key="label.supportedFormats"/>:
		</span>
	  </td>
	  <td class="datum">
		<html:textarea property="supportedFormats" cols="60" rows="2"/>
	  </td>
    </tr>

    <tr>
    <td class="label">
		<span class="help" title="<bean:message key="help.coverage.defaultInterpolationMethod"/>">
        <bean:message key="label.defaultInterpolationMethod"/>:
      </span>
	  </td>
	  <td class="datum">
		<bean:write name="coveragesEditorForm" property="defaultInterpolationMethod"/>
		<html:hidden property="defaultInterpolationMethod"/>
	  </td>
	</tr>

    <tr>
    <td class="label">
		<span class="help" title="<bean:message key="help.coverage.interpolationMethods"/>">
			<bean:message key="label.interpolationMethods"/>:
		</span>
	  </td>
	  <td class="datum">
		<html:textarea property="interpolationMethods" cols="60" rows="2" readonly="true"/>
	  </td>
    </tr>


    <logic:notEmpty name="coveragesEditorForm"
                   property="paramKeys">
    <logic:iterate id="param"
                   indexId="ctr"
                   name="coveragesEditorForm"
                   property="paramKeys">
	    <logic:notEqual name="coveragesEditorForm"
	                    property='<%= "paramKey[" + ctr + "]"%>'
	                    value="dbtype">
	        <tr>
	    	  <td class="label">
			 	<logic:notEqual name="coveragesEditorForm"
		    	        property='<%= "paramKey[" + ctr + "]"%>'
				        value="ReadGridGeometry2D">
		            <span class="help"
		    		      title="<bean:write name="coveragesEditorForm"
		    		      property='<%= "paramHelp[" + ctr + "]" %>'/>">
		              <bean:write name="coveragesEditorForm"
		                          property='<%= "paramKey[" + ctr + "]"%>'/>:
		    		</span>
				</logic:notEqual>
	          </td>
	    	  <td class="datum">
			 	<logic:notEqual name="coveragesEditorForm"
		    	        property='<%= "paramKey[" + ctr + "]"%>'
				        value="ReadGridGeometry2D">

                  <logic:equal name="coveragesEditorForm"
	                property='<%= "paramKey[" + ctr + "]"%>'
	                value="InputTransparentColor">
	                	<script>document.write(generateColorPicker('<%= "paramValue[" + ctr + "]"%>','<bean:write name="coveragesEditorForm" property='<%= "paramValue[" + ctr + "]"%>'/>'))</script>
      			  </logic:equal>
				  <logic:equal name="coveragesEditorForm"
		                property='<%= "paramKey[" + ctr + "]"%>'
		                value="OutputTransparentColor">
		                	<script>document.write(generateColorPicker('<%= "paramValue[" + ctr + "]"%>','<bean:write name="coveragesEditorForm" property='<%= "paramValue[" + ctr + "]"%>'/>'))</script>
	      		  </logic:equal>
                  
                  <logic:notEqual name="coveragesEditorForm"
	                property='<%= "paramKey[" + ctr + "]"%>'
	                value="InputTransparentColor">  
	                  <logic:notEqual name="coveragesEditorForm"
		                property='<%= "paramKey[" + ctr + "]"%>'
		                value="OutputTransparentColor">
			                <html:text property='<%= "paramValue[" + ctr + "]"%>' size="60"/>
	      			  </logic:notEqual>
      			  </logic:notEqual>
				</logic:notEqual>
	    	  </td>
	    	</tr>
	    </logic:notEqual>
    </logic:iterate>
    </logic:notEmpty>

    <tr>
      <td class="label">
        &nbsp;
      </td>
	  <td class="datum">

		<html:submit property="action">
			<bean:message key="label.submit"/>
		</html:submit>
		
		<html:reset>
			<bean:message key="label.reset"/>
		</html:reset>

	  </td>
    </tr>
  </table>
</html:form>

<% } catch (Throwable hate ){
   System.err.println( "Coverage Editor problem:"+ hate );
   hate.printStackTrace();
   throw hate;
} %>