<%@ taglib uri="/tags/struts-bean" prefix="bean" %>
<%@ taglib uri="/tags/struts-html" prefix="html" %>
<%@ taglib uri="/tags/struts-logic" prefix="logic" %>

<script language="JavaScript">
<!--
	function setAction(actionType) {
		document.wmsContentForm.action.value = actionType;
		document.wmsContentForm.submit();
	}
	
	function beforeSubmit(layerID) {
		document.wmsContentForm.selectedLayer.value = layerID;
	}
-->
</script>

<table border=0 width=100%>
	
	<html:form action="/config/wms/contentSubmit">
	
	<html:hidden property="action"/>
	<html:hidden property="selectedLayer"/>
	
	<tr><td align="right">
		<span class="help" title="<bean:message key="help.enabled"/>">
			<bean:message key="label.enabled"/>:
		</span>
		</td><td colspan=2>
			<html:checkbox property="enabled"/>
	</td></tr>
		
	<tr><td align="right">
		<span class="help" title="<bean:message key="help.onlineResource_service"/>">
			<bean:message key="label.onlineResource"/>:
		</span>
	</td><td colspan=2>
		<html:text property="onlineResource" size="60"/>
	</td></tr>
	
	<tr><td align="right" valign="top">
		<span class="help" title="<bean:message key="help.baseMaps"/>">
			<bean:message key="label.baseMaps"/>:
		</span>
	</td>
	<td align="left" colspan=2>
	<!-- put this in a loop for every layer-group they specify -->
	<logic:iterate id="baseMapTitle"
                   indexId="bmi"
                   name="wmsContentForm"
                   property="baseMapTitles">
	  <table border="0">
	    <tr><td colspan=3>
			<hr>
		</td></tr>
		<tr><td align="right">
			<span class="help" title="<bean:message key="help.baseMapTitle"/>">
				&nbsp;&nbsp;&nbsp;&nbsp;<bean:message key="label.baseMapTitle"/>:
			</span>
		</td><td colspan=2>
			<html:text property='<%= "baseMapTitle[" + bmi + "]"%>' size="15"/>
			<input type="button" onclick="beforeSubmit('<%=bmi%>'); setAction('Remove');" value="Remove"/>
		</td></tr>
		<tr><td align="right">
			<span class="help" title="<bean:message key="help.baseMapLayers"/>">
				<bean:message key="label.baseMapLayers"/>:
			</span>
		</td><td colspan=2>
			<html:text property='<%= "baseMapLayers[" + bmi + "]"%>' size="60"/>
		</td></tr>
		<tr><td align="right">
			<span class="help" title="<bean:message key="help.baseMapStyles"/>">
				<bean:message key="label.baseMapStyles"/>:
			</span>
		</td><td colspan=2>
			<html:text property='<%= "baseMapStyles[" + bmi + "]"%>' size="60"/>
		</td></tr>
		
		<!-- SRS -->
		<tr>
	      <td class="label" align="right">
			<span class="help" title="<bean:message key="help.coverage.srsName"/>">
	          <bean:message key="label.SRS"/>:
	        </span>
	      </td>
		  <td class="datum">
		  	<table>
		  	<tr>
				<td>
				<html:text property='<%= "srsName[" + bmi + "]"%>' size="32"/>
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
		
		<!-- ENVELOPE -->
		<tr>
	      <td class="label" align="right">
			<span class="help" title="<bean:message key="help.coverage.envelope"/>">
	          <bean:message key="label.envelope"/>:          
	        </span>
		  </td>
		  <td class="datum">
	        <input type="button" onclick="beforeSubmit('<%=bmi%>'); setAction('<bean:message key="config.data.calculateBoundingBox.label"/>');" value="<bean:message key="config.data.calculateBoundingBox.label"/>" />
	        <br/>
	        <table border=0>
	          <tr>
	            <td style="white-space: nowrap;">
	              <span class="help" title="<bean:message key="help.coverage.minx"/>">
	                <bean:message key="label.coverage.minx"/>:
	              </span>
	            </td>
	            <td>
	              <html:text property='<%= "minX[" + bmi + "]"%>' size="15"/>
	            </td>
	            <td style="white-space: nowrap;">
	              <span class="help" title="<bean:message key="help.coverage.miny"/>">
	                <bean:message key="label.coverage.miny"/>:
	              </span>
	            </td>
	            <td>
	              <html:text property='<%= "minY[" + bmi + "]"%>' size="15"/>
	            </td>
	          </tr>
	          <tr>
	            <td style="white-space: nowrap;">
	              <span class="help" title="<bean:message key="help.coverage.maxx"/>">
	                <bean:message key="label.coverage.maxx"/>:
	              </span>
	            </td>
	            <td>
	              <html:text property='<%= "maxX[" + bmi + "]"%>' size="15"/>
	            </td>
	            <td style="white-space: nowrap;">
	              <span class="help" title="<bean:message key="help.coverage.maxy"/>">
	                <bean:message key="label.coverage.maxy"/>:
	              </span>
	            </td>
	            <td>
	              <html:text property='<%= "maxY[" + bmi + "]"%>' size="15"/>
	            </td>
	          </tr>
	        </table>
		  </td>
	    </tr>
	    
	  </table>
	</logic:iterate>
	
	</td></tr>
	
	<tr><td align="right">&nbsp;</td><td>
		<input type="button" onclick="setAction('<bean:message key="label.submit"/>');" value="<bean:message key="label.submit"/>"/>

		<input type="button" onclick="setAction('<bean:message key="label.reset"/>');" value="<bean:message key="label.reset"/>"/>

		<input type="button" onclick="setAction('Add New Layer-Group');" value="Add New Layer-Group"/>
	</td></tr>
	</html:form>
</table>