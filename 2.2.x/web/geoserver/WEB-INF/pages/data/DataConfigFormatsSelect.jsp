<%@ taglib uri="/tags/struts-bean" prefix="bean" %>
<%@ taglib uri="/tags/struts-html" prefix="html" %>
<%@ taglib uri="/tags/struts-logic" prefix="logic" %>

<html:form action="/config/data/formatSelectSubmit">
<table class="info">
  <tbody>
    <tr>
      <td class="label">
        <span class="help" title="<bean:message key="help.dataFormat_id"/>">
		  <bean:message key="label.dataFormatID"/>:
        </span>
      </td>
      <td class="datum" colspan=2>
        <html:select property="selectedDataFormatId">
			<html:options name="Config.Data" property="dataFormatIds"/>
		</html:select>
      </td>
    </tr>
    <tr>
      <td class="label">&nbsp;</td>
      <td class="datum">
		<html:submit property="buttonAction">
			<bean:message key="label.edit"/>
		</html:submit>
      </td>
      <td>
        <html:submit property="buttonAction">
			<bean:message key="label.delete"/>
		</html:submit>
	  </td>
    </tr>
  </tbody>
</table>
</html:form>
