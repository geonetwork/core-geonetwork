<%@ taglib uri="/tags/struts-bean" prefix="bean" %>
<%@ taglib uri="/tags/struts-html" prefix="html" %>
<%@ taglib uri="/tags/struts-logic" prefix="logic" %>

<html:form action="/config/wcs/coveragePlugins">
<table class="info" align="center">
  	<tr>
			<td class="datum">
				<strong>Format ID / Version</strong>
			</td>
			<td class="datum">
				<strong>Format Description</strong>
			</td>
  	</tr>
		<logic:iterate id="element" name="dataCoveragePluginsForm" property="formats">
  	<tr>
			<td class="datum">
				<bean:write name="element" property="name"/>&nbsp;/&nbsp;<bean:write name="element" property="version"/>
			</td>
			<td class="datum">
				<i><bean:write name="element" property="description"/></i>
			</td>
  	</tr>
		</logic:iterate>
</table>
</html:form>