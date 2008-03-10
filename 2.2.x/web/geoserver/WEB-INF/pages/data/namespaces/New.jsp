<%@ taglib uri="/tags/struts-bean" prefix="bean" %>
<%@ taglib uri="/tags/struts-html" prefix="html" %>
<%@ taglib uri="/tags/struts-logic" prefix="logic" %>

<html:form action="/config/data/namespaceNewSubmit">
  <table class="info">
    <tr>
      <td class="label">
        <span class="help" title="<bean:message key="help.namespace.prefix"/>">
          <bean:message key="label.prefix"/>:
        </span>
      </td>
      <td class="datum">
        <html:text property="prefix" size="60"/>
	  </td>
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
