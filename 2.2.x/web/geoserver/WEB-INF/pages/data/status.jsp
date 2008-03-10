<%@ taglib uri="/tags/struts-bean" prefix="bean" %>
<%@ taglib uri="/tags/struts-html" prefix="html" %>
<%@ taglib uri="/tags/struts-logic" prefix="logic" %>

<table class="status">

	<tr>
		<td class="module">
			<bean:message key="config.data.label"/>:
		</td>
		<td class="health">
            <table class="progress">
			  <tbody>
		        <tr>
		        <logic:notEqual name="GeoServer.ApplicationState" property="dataGood" value="0">
		    	    <td class="good"
		    	        width="<bean:write name="GeoServer.ApplicationState" property="dataGood"/>%"
		    	        title="<bean:write name="GeoServer.ApplicationState" property="dataGood"/>%">
		    	        </td>
		    	</logic:notEqual>
		        <logic:notEqual name="GeoServer.ApplicationState" property="dataBad" value="0">		    	
		    	    <td class="bad"
		    	        width="<bean:write name="GeoServer.ApplicationState" property="dataBad"/>%"
		    	        title="<bean:write name="GeoServer.ApplicationState" property="dataBad"/>%">
		    	    </td>
		    	</logic:notEqual>		            
		        <logic:notEqual name="GeoServer.ApplicationState" property="wfsDisabled" value="0">		    	
		    	    <td class="disabled"
		    	        width="<bean:write name="GeoServer.ApplicationState" property="dataDisabled"/>%"
		    	        title="<bean:write name="GeoServer.ApplicationState" property="dataDisabled"/>%">
		    	    <td/>
		    	</logic:notEqual>		                   
		        </tr>
		      </tbody>
		    </table>
		</td>
	</tr>
</table>