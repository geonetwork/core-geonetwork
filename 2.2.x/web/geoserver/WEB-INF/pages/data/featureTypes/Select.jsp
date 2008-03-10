<%@ taglib uri="/tags/struts-bean" prefix="bean" %>
<%@ taglib uri="/tags/struts-html" prefix="html" %>
<%@ taglib uri="/tags/struts-logic" prefix="logic" %>

<% try { %>
<html:form action="/config/data/typeSelectSubmit">
  <table class="info">
	<tr>
      <td class="label">
		<bean:message key="label.featureTypes"/>:
	  </td>
      <td class="datum">
		<html:select property="selectedFeatureTypeName">
			<html:options property="typeNames"/>
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
   System.err.println( "FeatureType Editor problem:"+ hate );
   hate.printStackTrace();
   throw hate;
} %>