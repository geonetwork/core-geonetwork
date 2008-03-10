<%@ taglib uri="/tags/struts-bean" prefix="bean" %>
<%@ taglib uri="/tags/struts-html" prefix="html" %>
<%@ taglib uri="/tags/struts-logic" prefix="logic" %>

<% try { %>
<html:form action="/config/data/coverageSelectSubmit">
  <table class="info">
	<tr>
      <td class="label">
		<bean:message key="label.coverages"/>:
	  </td>
      <td class="datum">
		<html:select property="selectedCoverageName">
			<html:options property="coverageNames"/>
		</html:select>
      </td>
    </tr>
	<tr>
      <td class="label">&nbsp;</td>
      <td class="datum">

		<html:submit property="buttonAction">
			<bean:message key="label.edit"/>
		</html:submit>

		<html:submit property="buttonAction">
			<bean:message key="label.delete"/>
		</html:submit>
					
	  </td>
    </tr>
  </table>
</html:form>
<% } catch (Throwable hate ){
   System.err.println( "Coverage Editor problem:"+ hate );
   hate.printStackTrace();
   throw hate;
} %>