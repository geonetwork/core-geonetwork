<%@ taglib uri="/tags/struts-bean" prefix="bean" %>
<%@ taglib uri="/tags/struts-html" prefix="html" %>
<%@ taglib uri="/tags/struts-tiles" prefix="tiles" %>
<%@ taglib uri="/tags/struts-logic" prefix="logic" %>


<table border=0 width=100% height=100%>
	<tr>
		<td valign="top" align="left">
			<tiles:insert attribute="ftSelect"/>
		</td>
		<td valign="top" align="left">
			<tiles:insert attribute="ftNew"/>
		</td>
	</tr>
<logic:present name="selectedFeatureType" scope="session">
	<tr><td colspan=2><HR></td></tr>
	<tr>
		<td valign="top" align="left" colspan=2>
			<tiles:insert attribute="ftEditor"/>
		</td>
	</tr>
	<tr><td colspan=2><HR></td></tr>
	<tr><td valign="top" align="left">
		<tiles:insert attribute="atSelect"/>
	</td><td>
		<tiles:insert attribute="atNew"/>
	</td></tr>
	<logic:present name="selectedAttributeType" scope="session">
	<tr><td colspan=2><HR></td></tr>
	<tr><td colspan=2>
		<tiles:insert attribute="atEditor"/>
	</td></tr>	
	</logic:present>
</logic:present>
</table>
