<%@ taglib uri="/tags/struts-bean" prefix="bean" %>
<%@ taglib uri="/tags/struts-html" prefix="html" %>

<table border=0 width=100%>

	<html:form action="/config/wfs/descriptionSubmit" focus="name">
	<tr><td align="right">
		<span class="help" title="<bean:message key="help.name_service"/>">
			<bean:message key="label.name"/>:
		</span>
	</td><td>
		<html:text property="name" size="60"/>
	</td></tr>

	<tr><td align="right">
		<span class="help" title="<bean:message key="help.title_service"/>">
			<bean:message key="label.title"/>:
		</span>
	</td><td>
			<html:text property="title" size="60"/>
	</td></tr>		
		
	<tr><td align="right">
		<span class="help" title="<bean:message key="help.accessConstraints_service"/>">
			<bean:message key="label.accessConstraints"/>:
		</span>
	</td><td>
		<html:text property="accessConstraints" size="60"/>
	</td></tr>
	
	<tr><td align="right">
		<span class="help" title="<bean:message key="help.fees_service"/>">
			<bean:message key="label.fees"/>:
		</span>
	</td><td>
		<html:text property="fees" size="60"/>
	</td></tr>
	
	<tr><td align="right">
		<span class="help" title="<bean:message key="help.maintainer_service"/>">
			<bean:message key="label.maintainer"/>:
		</span>
	</td><td>
			<html:text property="maintainer" size="60"/>
	</td></tr>
	
	<tr><td align="right">
		<span class="help" title="<bean:message key="help.keywords_service"/>">
			<bean:message key="label.keywords"/>:
		</span>
	</td><td>
		<html:textarea property="keywords" cols="60" rows="10"/>
	</td></tr>
	
	<tr><td align="right">
		<span class="help" title="<bean:message key="help.abstract_service"/>">
			<bean:message key="label.abstract"/>:
		</span>
	</td><td>
		<html:textarea property="_abstract" cols="60" rows="6"/>
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