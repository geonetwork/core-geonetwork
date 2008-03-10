<%@ taglib uri="/tags/struts-bean" prefix="bean" %>
<%@ taglib uri="/tags/struts-html" prefix="html" %>
<%@ taglib uri="/tags/struts-logic" prefix="logic" %>

<%
response.setHeader("Pragma", "No-cache");
response.setDateHeader("Expires", 0);
response.setHeader("Cache-Control", "no-cache");
%>

<logic:present name="GEOSERVER.USER" property="dataFormatConfig" scope="session">

<html:form action="/config/data/formatSubmit">
  <table class="info">	
	<tr>
	  <td class="label">
		<span class="help" title="<bean:message key="help.dataFormat_id"/>">
			<bean:message key="label.dataFormatID"/>:
		</span>
	  </td>
	  <td class="datum">
		<bean:write name="dataFormatsEditorForm" property="dataFormatId"/>
	  </td>
	</tr>	
	<tr>
	  <td class="label">
		<span class="help" title="<bean:message key="help.dataFormat_enabled"/>">
          <bean:message key="label.enabled"/>:
        </span>
      </td>
	  <td class="datum">
		<html:checkbox property="enabled"/>
	  </td>
	</tr>
	<tr>
	  <td class="label">
		<span class="help" title="<bean:message key="help.dataFormat_nameSpace"/>">
			<bean:message key="label.namespace"/>:
		</span>
      </td>
	  <td class="datum">
		<html:select property="namespaceId">
			<html:options property="namespaces"/>
		</html:select>
	  </td>
	</tr>		
	<tr>
	  <td class="label">
		<span class="help" title="<bean:message key="help.dataFormat_type"/>">
			<bean:message key="label.type"/>:
		</span>
      </td>
	  <td class="datum">
          <html:text property="type" size="60" readonly="true"/>
	  </td>
	</tr>	
	<tr>
	  <td class="label">
	    <font color="red">*</font>
		<span class="help" title="<bean:message key="help.dataFormat_url"/>">
			<bean:message key="label.url"/>:
		</span>
      </td>
	  <td class="datum">
          <html:text size="60" property="url"/>
	  </td>
	</tr>	
	<tr>
	  <td class="label">
		<span class="help" title="<bean:message key="help.dataFormat_description"/>">
			<bean:message key="label.description"/>:
		</span>
      </td>
	  <td class="datum">
		<html:textarea property="description" cols="60" rows="2"/>
	  </td>
	</tr>

	<tr>
	  <td class="label">&nbsp;</td>
	  <td class="datum">
		<html:submit>
			<bean:message key="label.submit"/>
		</html:submit>
		
		<html:reset>
			<bean:message key="label.reset"/>
		</html:reset>
	  </td>
	</tr>						
  </table>
</html:form>
</logic:present>
<br>
&nbsp;&nbsp;<font color="red">*</font> = <bean:message key="config.data.format.editor.requiredField"/>