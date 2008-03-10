<%@ taglib uri="/tags/struts-bean" prefix="bean" %>
<%@ taglib uri="/tags/struts-html" prefix="html" %>
<%@ taglib uri="/tags/struts-logic" prefix="logic" %>

<html:form action="/config/data/typeNewSubmit">
  <table class="info">
    <tr>
      <td class="label">
		<bean:message key="label.featureTypeName"/>:
	  </td>
	  <td class="datum">
		<html:select property="selectedNewFeatureType">
			<html:options property="newFeatureTypes"/>
		</html:select>
	  </td>
    </tr>

	<tr>
      <td class="label">&nbsp;</td>
      <td class="datum">
		<html:submit>
			<bean:message key="label.new"/>
		</html:submit>
      </td>
    </tr>
  </table>
</html:form>