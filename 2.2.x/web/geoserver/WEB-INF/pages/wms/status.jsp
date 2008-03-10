<%@ taglib uri="/tags/struts-bean" prefix="bean" %>
<%@ taglib uri="/tags/struts-html" prefix="html" %>
<%@ taglib uri="/tags/struts-logic" prefix="logic" %>

<table class="status">
	<tr>
		<td class="module">
			<bean:message key="config.wms.label"/>:
		</td>
		<td class="health">
			<table class="progress">
			  <tbody>
		        <tr>
		        <logic:notEqual name="GeoServer.ApplicationState" property="wmsGood" value="0">
		    	    <td class="good"
		    	        width="<bean:write name="GeoServer.ApplicationState" property="wmsGood"/>%"
		    	        title="<bean:write name="GeoServer.ApplicationState" property="wmsGood"/>%">
		    	    </td>
		    	</logic:notEqual>
		        <logic:notEqual name="GeoServer.ApplicationState" property="wmsBad" value="0">		    	
		    	    <td class="bad"
		    	        width="<bean:write name="GeoServer.ApplicationState" property="wmsBad"/>%"
		    	        title="<bean:write name="GeoServer.ApplicationState" property="wmsBad"/>%">
		    	    </td>
		    	</logic:notEqual>		            
		        <logic:notEqual name="GeoServer.ApplicationState" property="wmsDisabled" value="0">		    	
		    	    <td class="disabled"
		    	        width="<bean:write name="GeoServer.ApplicationState" property="wmsDisabled"/>%"
		    	        title="<bean:write name="GeoServer.ApplicationState" property="wmsDisabled"/>%">
		    	    </td>
		    	</logic:notEqual>		                   
		        </tr>
		      </tbody>
		     </table>
		</td>
	</tr>

</table>