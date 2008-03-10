<%@ taglib uri="/tags/struts-bean" prefix="bean" %>
<%@ taglib uri="/tags/struts-html" prefix="html" %>
<%@ taglib uri="/tags/struts-logic" prefix="logic" %>

<html:form action="/config/data/styleSelectSubmit">

<table class="info">
  <tr>
    <td class="label">
	  <bean:message key="label.styles"/>:
	</td>
    <td class="datum">
	  <html:select property="selectedStyle">
	    <html:options property="styles"/>
	  </html:select>
    </td>
  </tr>
    <tr>
    <td class="label">&nbsp;</td>
    <td class="datum">
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
</table>

</html:form>