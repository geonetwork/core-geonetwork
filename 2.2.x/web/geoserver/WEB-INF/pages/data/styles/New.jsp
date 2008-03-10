<%@ taglib uri="/tags/struts-bean" prefix="bean" %>
<%@ taglib uri="/tags/struts-html" prefix="html" %>
<%@ taglib uri="/tags/struts-logic" prefix="logic" %>

<html:form action="/config/data/styleNewSubmit" focus="styleID">

<table class="info">
  <tr>
    <td class="label">
      <span class="help" title="<bean:message key="help.styleID"/>">
        <bean:message key="label.styleID"/>:
      </span>      
    </td>
    <td class="datum">
      <html:text property="styleID" size="60"/>
    </td>
  </tr>
  <tr>
    <td class="label">&nbsp;</td>
    <td class="datum">
      <html:submit property="action"><bean:message key="label.new"/></html:submit>
    </td>
  </tr>
</table>

</html:form>
