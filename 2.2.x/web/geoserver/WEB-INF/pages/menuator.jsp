<%@ taglib uri="/tags/struts-bean" prefix="bean" %>
<%@ taglib uri="/tags/struts-html" prefix="html" %>
<%@ taglib uri="/tags/struts-logic" prefix="logic" %>
<%@ taglib uri="/tags/struts-tiles" prefix="tiles" %>
<%@ page import="org.apache.struts.action.*" %>

<logic:notEmpty name="menuForwards">
  <table class="menu">
    <tbody>
<%  // Access menuForwards - could not figure out how to with tags
    String forwards = (String) request.getAttribute("menuForwards");
    String[] array = forwards.split(":"); 
    for (int index = 0; index < array.length; index ++) {
%>
      <tr>
        <td>
<%  String forward = array[ index ];
    if( forward.length() == 0 ){
%>
          <hr/>
<%  }
    else if (forward.startsWith("/")) {
        String action = forward.substring(1);
%>
          <html:link style="item"
                     action="<%= action %>">
            <%= action %>
          </html:link>
<%  }
    else {
%>              
          <html:link style="item"
                     forward="<%= forward %>"
                     titleKey='<%= forward+".short" %>'>
            <bean:message key='<%= forward+".label" %>'/>
          </html:link>
<%  }
%>          
        </td>
      </tr>
<%  } %>
    </tbody>
  </table>
</logic:notEmpty>
