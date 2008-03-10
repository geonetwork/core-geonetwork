<%@ taglib uri="/tags/struts-logic" prefix="logic" %>
<%@ taglib uri="/tags/struts-bean" prefix="bean" %>
<%@ taglib uri="/tags/struts-html" prefix="html" %>

<logic:present name="selectedFeatureType" scope="session">

<span class="actions">
	<span class="actionsTitle">
		<bean:message key="label.actions"/>
	</span>

	<html:form action="/config/data/calculateBoundingBox">
		<html:submit>
			<bean:message key="label.calculateBoundingBox"/>
		</html:submit>
	</html:form>
</span>

</logic:present>