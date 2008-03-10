<%@ taglib uri="/tags/struts-bean" prefix="bean" %>
<%@ taglib uri="/tags/struts-html" prefix="html" %>
<%@ taglib uri="/tags/struts-logic" prefix="logic" %>

<table border=0>

	<html:form action="/config/validation/testSuiteSelect">
	<tr><td valign="top" align="right">
		<bean:message key="label.testSuite"/>:
	</td><td>
		<html:select property="selectedTestSuite">
			<html:options property="testSuites"/>
		</html:select>
	</td></tr>
	<tr><td>&nbsp;</td><td valign="top" align="left">
		<html:submit property="buttonAction">
			<bean:message key="label.edit"/>
		</html:submit>
	</td></tr>
	<tr><td>&nbsp;</td><td valign="top" align="left">
		<html:submit property="buttonAction">
			<bean:message key="label.delete"/>
		</html:submit>
	</td></tr>

	</html:form>

	<tr><td colspan=2><HR></td></tr>

	<html:form action="/config/validation/testSuiteNew">	
	<tr><td valign="top" align="right">
			<bean:message key="label.newName"/>:
		</td><td>
			<html:text property="newName" size="60"/>
	</td></tr>
	<tr><td>&nbsp;</td><td valign="top" align="left">
		<html:submit>
			<bean:message key="label.new"/>
		</html:submit>
	</td></tr>
	</html:form>
	

</table>
<table border="0">

