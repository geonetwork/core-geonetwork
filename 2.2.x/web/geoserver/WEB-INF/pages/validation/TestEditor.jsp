<%@ taglib uri="/tags/struts-bean" prefix="bean" %>
<%@ taglib uri="/tags/struts-html" prefix="html" %>
<%@ taglib uri="/tags/struts-logic" prefix="logic" %>

<table border=0>

	<html:form action="/config/validation/testEditor">
	<tr><td valign="top" align="right">
		<bean:message key="label.testName"/>:
	</td><td>
		<html:text property="name" size="60"/>
	</td></tr>
	<tr><td valign="top" align="right">
		<bean:message key="label.testDescription"/>:
	</td><td>
		<html:textarea property="description" cols="60" rows="4"/>
	</td></tr>

	<tr><td valign="top" align="right">
		<bean:message key="label.plugInType"/>:
	</td><td>
		<bean:write name="validationTestEditorForm" property="plugInName"/>
	</td></tr>
	
	<tr><td valign="top" align="right">
		<bean:message key="label.plugInDescription"/>:
	</td><td>
		<bean:write name="validationTestEditorForm" property="plugInDescription"/>
	</td></tr>

<logic:iterate id="attribute" indexId="ctr" name="validationTestEditorForm" property="attributeKeys">
	<tr><td align="right">
		<span class="help" title="<bean:write name="validationTestEditorForm" property='<%= "attributeHelps[" + ctr + "]" %>'/>">
			<bean:write name="validationTestEditorForm" property='<%= "attributeKeys[" + ctr + "]"%>'/>
		</span>
	</td><td colspan=2 align="left">
		<html:text property='<%= "attributeValues[" + ctr + "]"%>' size="60"/>
	</td></tr>
</logic:iterate>		
	
	<tr><td>&nbsp;</td><td valign="top" align="left">
		<html:submit>
			<bean:message key="label.submit"/>
		</html:submit>
	</td></tr>
	</html:form>

</table>
