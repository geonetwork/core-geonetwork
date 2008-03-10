<%@ taglib uri="/tags/struts-bean" prefix="bean" %>
<%@ taglib uri="/tags/struts-html" prefix="html" %>
<%@ taglib uri="/tags/struts-logic" prefix="logic" %>

<html:form action="/config/data/coverageNewSubmit">
  <table class="info">
    <tr>
      <td class="label">
		<bean:message key="label.coverageName"/>:
	  </td>
	  <td class="datum">
		<html:select property="selectedNewCoverage">
			<html:options property="newCoverages"/>
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