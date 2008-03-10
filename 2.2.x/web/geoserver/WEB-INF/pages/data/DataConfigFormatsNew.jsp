<%@ taglib uri="/tags/struts-bean" prefix="bean" %>
<%@ taglib uri="/tags/struts-html" prefix="html" %>
<%@ taglib uri="/tags/struts-logic" prefix="logic" %>

<html:form action="/config/data/formatNewSubmit">
<table class="info">
  <tr>
    <td class="label">	
		<bean:message key="label.dataFormatDescription"/>:
	</td>
	<td class="datum">
	  <html:select property="selectedDescription">
	    <html:options property="dataFormatDescriptions"/>
	  </html:select>
	</td>
  </tr>
  <tr>
    <td class="label">	
      <span class="help" title="<bean:message key="help.dataFormat_id"/>">
		<bean:message key="label.dataFormatID"/>:
      </span>
	</td>
	<td class="datum">      
	  <html:text property="dataFormatID"/>
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