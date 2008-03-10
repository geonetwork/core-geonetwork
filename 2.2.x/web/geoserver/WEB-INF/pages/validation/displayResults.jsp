<%@ taglib uri="/tags/struts-bean" prefix="bean" %>
<%@ taglib uri="/tags/struts-html" prefix="html" %>
<%@ taglib uri="/tags/struts-logic" prefix="logic" %>

<%@ page import="java.lang.*"%>
<%@ page import="org.vfny.geoserver.action.validation.*"%>

<table border="0">
<%
try {
org.vfny.geoserver.action.validation.TestValidationResults tvr =
	(org.vfny.geoserver.action.validation.TestValidationResults) session.getAttribute(org.vfny.geoserver.action.validation.TestValidationResults.CURRENTLY_SELECTED_KEY);
if(tvr!=null && tvr.getErrors().size()>0){

Thread thread = (Thread) session.getAttribute(ValidationRunnable.KEY);

%>
<table border="0">

<%
if (thread != null && thread.isAlive()) { %>
<tr><td><html:link forward="config.validation.test.doitStop"><bean:message key="config.validation.displayResults.stop"/></html:link></td></tr>

<script>
function refresh() {
	location.replace("<%= org.apache.struts.util.RequestUtils.computeURL(pageContext, "config.validation.displayResults", null, null, null, null, true) %>");
	//location.replace("<!--%= org.apache.struts.taglib.TagUtils.computeURL(pageContext, "config.validation.displayResults", null, null, null, null, null, null, true) %-->");
}
window.setTimeout("refresh()", 6000);
</script>

<% }  %>

<tr><td><bean:message key="config.validation.displayResults.errors"/></td></tr>
<%if(tvr.isRun()){%>
<tr><td><bean:message key="config.validation.displayResults.runCompleted"/></td></tr>
<%}else{%>
<tr><td><bean:message key="config.validation.displayResults.runNotCompleted"/></td></tr>
<%
}
java.util.Iterator i = tvr.getErrors().entrySet().iterator();
while(i.hasNext()){
  java.util.Map.Entry m = (java.util.Map.Entry)i.next();  
  org.geotools.feature.Feature feature = (org.geotools.feature.Feature) m.getKey();
  String fid = feature != null ? feature.getID() : "(problem)";
  Object msg = m.getValue();
  String message = "";
  if (msg == null) {
  	message = "an error has occured";
  } else {
  	message = (String) msg;
  }
  %>
<tr><td><%=fid%></td><td><code><%=org.vfny.geoserver.action.HTMLEncoder.encode(message)%></code></td></tr>
  <%
}  // while
%>
</table>
<%
} // if
} catch( NullPointerException bad){
	bad.printStackTrace();
}
%>