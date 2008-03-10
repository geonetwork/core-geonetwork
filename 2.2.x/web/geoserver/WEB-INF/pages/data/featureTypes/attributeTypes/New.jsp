<%@ taglib uri="/tags/struts-bean" prefix="bean" %>
<%@ taglib uri="/tags/struts-html" prefix="html" %>
<%@ taglib uri="/tags/struts-logic" prefix="logic" %>

<table border=1 width=100%>
<tr><td>
<table border=0 width=100%>
	<html:form action="/config/data/attributeTypeNewSubmit">

	<tr><td valign="top" align="right">	
		<bean:message key="label.attributeTypeName"/>:
	</td>
	<td align="left">
		<html:select property="selectedNewAttributeType">
			<html:options property="newAttributeTypes"/>
		</html:select>
	</td></tr>

	<tr><td>&nbsp;</td><td align="left">
		<html:submit>
			<bean:message key="label.new"/>
		</html:submit>
	</td></tr>
	
	</html:form>
	
</table>
</td></tr>
</table>