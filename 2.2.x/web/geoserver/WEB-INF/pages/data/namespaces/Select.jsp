<%@ taglib uri="/tags/struts-bean" prefix="bean" %>
<%@ taglib uri="/tags/struts-html" prefix="html" %>
<%@ taglib uri="/tags/struts-logic" prefix="logic" %>

<html:form action="/config/data/namespaceSelect">
<table class="info">
  <tr>
    <td class="label">
      <bean:message key="label.namespaces"/>:
    </td>
    <td class="datum">
      <html:select property="selectedNamespace">
        <html:options property="namespaces"/>
      </html:select>
    </td>
  </tr>
  <tr>
    <td class="label">&nbsp;</td>
	<td>
       <html:submit property="action">
         <bean:message key="label.edit"/>
       </html:submit>
       <html:submit property="action">
         <bean:message key="label.delete"/>
       </html:submit>
       <html:submit property="action">
         <bean:message key="label.default"/>
       </html:submit>
    </td>
  </tr>
  <tr>
    <td class="label">&nbsp;</td>
	<td>
       <bean:message key="text.namespace"/>
    </td>
  </tr>  
</table>
</html:form>
