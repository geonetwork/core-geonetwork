<%@ taglib uri="/tags/struts-bean" prefix="bean" %>
<%@ taglib uri="/tags/struts-html" prefix="html" %>
<%@ taglib uri="/tags/struts-logic" prefix="logic" %>

<html:form action="/config/data/namespaceSubmit">
  <table class="info">
    <tr>
      <td class="label">
        <span class="help" title="<bean:message key="help.namespace.uri"/>">
          <bean:message key="label.URI"/>:
        </span>
      </td>
      <td class="datum">
		<html:text property="URI" size="60"/>
      </td>
    </tr>
    <tr>
      <td class="label">
        <span class="help" title="<bean:message key="help.namespace.prefix"/>">
		  <bean:message key="label.prefix"/>:
        </span>
      </td>
      <td class="datum">
		<html:text property="prefix" size="60"/>
      </td>
    </tr>
    <tr>
      <td class="label">&nbsp;</td>
      <td class="datum">
		<html:submit property="action">
			<bean:message key="label.submit"/>
		</html:submit>		
		<html:reset>
			<bean:message key="label.reset"/>
        </html:reset>
	  </td>
    </tr>							
  </table>
</html:form>