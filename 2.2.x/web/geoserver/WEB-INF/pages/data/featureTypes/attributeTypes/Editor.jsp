<%@ taglib uri="/tags/struts-bean" prefix="bean" %>
<%@ taglib uri="/tags/struts-html" prefix="html" %>
<%@ taglib uri="/tags/struts-logic" prefix="logic" %>

<table border=0 width=100%>
	<html:form action="/config/data/attributeTypeSubmit">

	<tr><td valign="top" align="right">
			<bean:message key="label.attributeTypeName"/>
		</td><td valign="top" align="left">
			<bean:write name="dataAttributeTypesEditorForm" property="name"/>
	</td></tr>

	<tr>
		<td valign="top" align="right">	
			<bean:message key="label.type"/>
		</td><td valign="top" align="left">
		<html:select property="selectedType">
			<html:options property="attributeTypes"/>
		</html:select>
	</td></tr><tr>

	<td valign="top" align="right">
		<bean:message key="label.isNillible"/>:
	</td><td valign="top" align="left">
		<html:checkbox property="nillible"/>
	</td></tr>
	<td valign="top" align="right">
		<bean:message key="label.minOccurs"/>:
	</td><td valign="top" align="left">
		<html:text property="minOccurs" size="3"/>
	</td></tr>
	<td valign="top" align="right">
		<bean:message key="label.maxOccurs"/>:
	</td><td valign="top" align="left">
		<html:text property="maxOccurs" size="3"/>
	</td></tr>
	<tr><td>&nbsp;</td><td valign="top" align="left">
		<html:textarea property="fragment" rows="6" cols="60"/>
	</td></tr>	
	<tr><td>&nbsp;</td><td valign="top" align="left">
		<html:submit>
			<bean:message key="label.submit"/>
		</html:submit>
		
		<html:reset>
			<bean:message key="label.reset"/>
		</html:reset>
	</td></tr>

	</html:form>
	
</table>