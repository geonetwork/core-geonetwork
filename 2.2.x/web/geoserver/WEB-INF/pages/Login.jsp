<%@ taglib uri="/tags/struts-bean" prefix="bean" %>
<%@ taglib uri="/tags/struts-html" prefix="html" %>

<html:form action="/admin/loginSubmit" focus="username">
  <table class="info">
    <tbody>
      <tr>
        <td class="label"><bean:message key="label.username"/>:</td>
        <td class="datum">
          <html:text size="60" property="username"/>        
        </td>      
      </tr>
      <tr>
        <td class="label"><bean:message key="label.password"/>:</td>
        <td class="datum">
          <html:password size="60" property="password"/>        
        </td>      
      </tr>    
      <tr>
        <td class="label"></td>
        <td class="datum">
          <html:submit><bean:message key="label.submit"/></html:submit><html:reset><bean:message key="label.reset"/></html:reset>
        </td>      
      </tr>    
    </tbody>
  </table>
</html:form>
