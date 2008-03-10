<%@ taglib uri="/tags/struts-bean" prefix="bean" %>
<%@ taglib uri="/tags/struts-html" prefix="html" %>
<%@ taglib uri="/tags/struts-logic" prefix="logic" %>

<table class="status">
	<tr>
		<td class="module">
			<bean:message key="config.wfs.label"/>:
		</td>
		<td style="health">
			<table class="progress">
			  <tbody>
		        <tr>
		        <logic:notEqual name="GeoServer.ApplicationState" property="wfsGood" value="0">
		    	    <td class="good"
		    	        width="<bean:write name="GeoServer.ApplicationState" property="wfsGood"/>%"
		    	        title="<bean:write name="GeoServer.ApplicationState" property="wfsGood"/>%">
		    	    </td>
		    	</logic:notEqual>
		        <logic:notEqual name="GeoServer.ApplicationState" property="wfsBad" value="0">		    	
		    	    <td class="bad"
		    	        width="<bean:write name="GeoServer.ApplicationState" property="wfsBad"/>%"
		    	        title="<bean:write name="GeoServer.ApplicationState" property="wfsBad"/>%">
		    	    </td>
		    	</logic:notEqual>		            
		        <logic:notEqual name="GeoServer.ApplicationState" property="wfsDisabled" value="0">		    	
		    	    <td class="disabled"
		    	        width="<bean:write name="GeoServer.ApplicationState" property="wfsDisabled"/>%"
		    	        title="<bean:write name="GeoServer.ApplicationState" property="wfsDisabled"/>%">
		    	    </td>
		    	</logic:notEqual>		                   
		        </tr>
		      </tbody>
		     </table>
		</td>
	</tr>
</table>