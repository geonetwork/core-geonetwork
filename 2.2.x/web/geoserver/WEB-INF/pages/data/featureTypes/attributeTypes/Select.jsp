<%@ taglib uri="/tags/struts-bean" prefix="bean" %>
<%@ taglib uri="/tags/struts-html" prefix="html" %>
<%@ taglib uri="/tags/struts-logic" prefix="logic" %>

<table border=1 width=100%>
<tr><td>
<table border=0 width=100%>
	<html:form action="/config/data/attributeTypeSelect">

	<tr><td align="right" valign="top">
		<bean:message key="label.attributeTypes"/>:
	</td><td align="left" valign="top">
		<html:select property="selectedAttributeType">
			<html:options property="attributeTypes"/>
		</html:select>
	</td><td align="left" valign="top">
		<html:submit property="buttonAction">
			<bean:message key="label.moveUp"/>
		</html:submit>
		<br>
		<html:submit property="buttonAction">
			<bean:message key="label.moveDown"/>
		</html:submit>
	</td></tr>
	<tr><td>&nbsp;</td><td valign="top" align="left" colspan=2>
		<html:submit property="buttonAction">
			<bean:message key="label.edit"/>
		</html:submit>
	</td></tr>
	<tr><td>&nbsp;</td><td valign="top" align="left" colspan=2>
		<html:submit property="buttonAction">
			<bean:message key="label.delete"/>
		</html:submit>
	</td></tr>
			
	</html:form>
</table>
</td></tr>
</table>