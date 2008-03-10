<%@ taglib uri="/tags/struts-bean" prefix="bean" %>
<%@ taglib uri="/tags/struts-html" prefix="html" %>
<%@ taglib uri="/tags/struts-logic" prefix="logic" %>

<table border=0 width=100%>

	<html:form action="/config/data/styleSubmit">
	
	<tr><td>
		<bean:message key="label.styles"/>:
		</td><td>
		<html:select property="selectedStyle">
			<html:options property="styles"/>
		</html:select>
		
		</td><td>
		
		<html:submit property="action" value="new">
			<bean:message key="label.new"/>
		</html:submit>
		<BR>
		<html:submit property="action" value="edit">
			<bean:message key="label.edit"/>
		</html:submit>
		<BR>
		<html:submit property="action" value="delete">
			<bean:message key="label.delete"/>
		</html:submit>
		<BR>
			
	</td></tr>
	
	<tr><td align="right">
		<bean:message key="label.styleID"/>:
	</td><td colspan=2 align="left">
		<html:text property="styleID" size="60"/>
	</td></tr>
	
	<tr><td align="right">
		<bean:message key="label.default"/>:
	</td><td colspan=2 align="left">
		<html:checkbox property="_default"/>
	</td></tr>

	<tr><td align="right">
		<bean:message key="label.filename"/>
	</td><td colspan=2 align="left">
		<html:text property="filename" size="60"/>
	</td></tr>
	
	<tr><td align="right">&nbsp;</td><td colspan=2>
		<html:submit property="action">
			<bean:message key="label.submit"/>
		</html:submit>
		
		<html:reset>
			<bean:message key="label.reset"/>
		</html:reset>
	</td></tr>						
	
	</html:form>
</table>