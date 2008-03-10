<%@ taglib uri="/tags/struts-bean" prefix="bean" %>
<%@ taglib uri="/tags/struts-html" prefix="html" %>
<%@ taglib uri="/tags/struts-logic" prefix="logic" %>

<html:form action="/config/data/storeNewSubmit">
<table class="info">
  <tr>
    <td class="label">	
		<bean:message key="label.dataStoreDescription"/>:
	</td>
	<td class="datum">
	  <html:select property="selectedDescription">
	    <html:options property="dataStoreDescriptions"/>
	  </html:select>
	</td>
  </tr>
  <tr>
    <td class="label">	
      <span class="help" title="<bean:message key="help.dataStore_id"/>">
		<bean:message key="label.dataStoreID"/>:
      </span>
	</td>
	<td class="datum">      
	  <html:text property="dataStoreID"/>
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