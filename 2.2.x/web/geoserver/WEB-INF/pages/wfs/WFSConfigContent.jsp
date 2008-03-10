<%@ taglib uri="/tags/struts-bean" prefix="bean" %>
<%@ taglib uri="/tags/struts-html" prefix="html" %>
<%@ taglib uri="/tags/struts-logic" prefix="logic" %>

<table border=0 width=100%>
	<html:form action="/config/wfs/contentSubmit">
	
	<tr><td align="right">
		<span class="help" title="<bean:message key="help.enabled"/>">
			<bean:message key="label.enabled"/>:
		</span>
	</td><td colspan=2>
		<html:checkbox property="enabled"/>
	</td></tr>
	<tr><td align="right">
		<span class="help" title="<bean:message key="help.srsXmlStyle"/>">
			<bean:message key="label.srsXmlStyle"/>:
		</span>
	</td><td colspan=2>
		<html:checkbox property="srsXmlStyle"/>
	</td></tr>
	

	<tr><td align="right">
		<span class="help" title="<bean:message key="help.citeConformanceHacks"/>">
			<bean:message key="label.citeConformanceHacks"/>:
		</span>
	</td><td colspan=2>
		<html:checkbox property="citeConformanceHacks"/>
	</td></tr>
	

	<tr><td align="right">
		<span class="help" title="<bean:message key="help.featureBounding"/>">
			<bean:message key="label.featureBounding"/>:
		</span>
	</td><td colspan=2>
		<html:checkbox property="featureBounding"/>
	</td></tr>

	
	<tr><td align="right">
		<span class="help" title="<bean:message key="help.serviceLevel"/>">
			<bean:message key="label.serviceLevel"/>:
		</span>
	</td><td colspan=2>
		<html:select property="serviceLevel" size="2">
			<html:option key="label.serviceLevel.basic" value="<%= java.lang.Integer.toString(org.vfny.geoserver.global.dto.WFSDTO.BASIC) %>"/>
			<html:option key="label.serviceLevel.transactional" value="<%= java.lang.Integer.toString(org.vfny.geoserver.global.dto.WFSDTO.TRANSACTIONAL) %>"/>
			<html:option key="label.serviceLevel.complete" value="<%=java.lang.Integer.toString(org.vfny.geoserver.global.dto.WFSDTO.COMPLETE) %>"/>
		</html:select>
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