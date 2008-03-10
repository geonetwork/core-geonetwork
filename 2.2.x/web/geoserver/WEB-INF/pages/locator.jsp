<%@ taglib uri="/tags/struts-bean" prefix="bean" %>
<%@ taglib uri="/tags/struts-html" prefix="html" %>
<%@ taglib uri="/tags/struts-logic" prefix="logic" %>
<%@ taglib uri="/tags/struts-tiles" prefix="tiles" %>
<%@ page import="org.apache.struts.action.*" %>

<span class="locator">
  <logic:notEmpty name="locationForwards">
	<%
		String forwards = (String) request.getAttribute("locationForwards");
		String[] array = forwards.split(":");
		
		for (int index = 0; index < array.length; index ++) {
			%>
			<html:link forward="<%= array[index] %>">
				<bean:message key='<%= array[ index ]+".label" %>'/>
			</html:link> |
			<%
		}
	%>
  </logic:notEmpty>
  <bean:message key='<%= request.getAttribute("key")+".label" %>'/>
</span>
