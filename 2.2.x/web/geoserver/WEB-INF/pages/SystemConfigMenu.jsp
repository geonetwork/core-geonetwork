<%@ taglib uri="/tags/struts-bean" prefix="bean" %>
<%@ taglib uri="/tags/struts-html" prefix="html" %>

<span class="systemConfigMenu">
	<html:link forward="wfsConfigMenu"><bean:message key="label.wfsConfig"/></html:link><BR>
	<html:link forward="wmsConfigMenu"><bean:message key="label.wmsConfig"/></html:link><BR>
	<html:link forward="dataConfigMenu"><bean:message key="label.dataConfig"/></html:link><BR>
</span>