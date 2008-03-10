<%@ taglib uri="/tags/struts-bean" prefix="bean" %>
<%@ taglib uri="/tags/struts-html" prefix="html" %>
<%@ taglib uri="/tags/struts-logic" prefix="logic" %>

<table class="status">
	<tr>
		<td class="module">
			<bean:message key="config.wcs.label"/>:
		</td>
		<td style="health">
			<table class="progress">
			  <tbody>
		        <tr>
		        <logic:notEqual name="GeoServer.ApplicationState" property="wcsGood" value="0">
		    	    <td class="good"
		    	        width="<bean:write name="GeoServer.ApplicationState" property="wcsGood"/>%"
		    	        title="<bean:write name="GeoServer.ApplicationState" property="wcsGood"/>%">
		    	    </td>
		    	</logic:notEqual>
		        <logic:notEqual name="GeoServer.ApplicationState" property="wcsBad" value="0">		    	
		    	    <td class="bad"
		    	        width="<bean:write name="GeoServer.ApplicationState" property="wcsBad"/>%"
		    	        title="<bean:write name="GeoServer.ApplicationState" property="wcsBad"/>%">
		    	    </td>
		    	</logic:notEqual>		            
		        <logic:notEqual name="GeoServer.ApplicationState" property="wcsDisabled" value="0">		    	
		    	    <td class="disabled"
		    	        width="<bean:write name="GeoServer.ApplicationState" property="wcsDisabled"/>%"
		    	        title="<bean:write name="GeoServer.ApplicationState" property="wcsDisabled"/>%">
		    	    </td>
		    	</logic:notEqual>		                   
		        </tr>
		      </tbody>
		     </table>
		</td>
	</tr>
</table>