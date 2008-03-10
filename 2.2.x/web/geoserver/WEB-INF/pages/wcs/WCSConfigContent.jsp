<%@ taglib uri="/tags/struts-bean" prefix="bean" %>
<%@ taglib uri="/tags/struts-html" prefix="html" %>
<%@ taglib uri="/tags/struts-logic" prefix="logic" %>

<table border=0 width=100%>
	<html:form action="/config/wcs/contentSubmit">
	
	<tr><td align="right">
		<span class="help" title="<bean:message key="help.enabled"/>">
			<bean:message key="label.enabled"/>:
		</span>
	</td><td colspan=2>
		<html:checkbox property="enabled"/>
	</td></tr>
	<tr><td align="right">
		<span class="help" title="<bean:message key="help.onlineResource_service"/>">
			<bean:message key="label.onlineResource"/>:
		</span>
	</td><td colspan=2>
		<html:text property="onlineResource" size="60"/>
	</td></tr>
	
	<tr><td align="right">&nbsp;</td><td>
		<html:submit>
			<bean:message key="label.submit"/>
		</html:submit>
		
		<html:reset>
			<bean:message key="label.reset"/>
		</html:reset>
	</td></tr>
	</html:form>
</table>