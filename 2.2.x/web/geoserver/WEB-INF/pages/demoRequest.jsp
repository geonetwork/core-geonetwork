<%@ taglib uri="/tags/struts-bean" prefix="bean" %>
<%@ taglib uri="/tags/struts-html" prefix="html" %>
<%@ taglib uri="/tags/struts-logic" prefix="logic" %>

<table class="info" height="100%" width="100%" id="table">
  <tbody>
    <tr>
      <td class="label"><bean:message key="label.request"/>:</td>
      <td class="datum">
        <html:form styleId="selectForm" action="/demoRequestSubmit">
        
          <html:select property="demo" onchange="document.getElementById('selectForm').submit();">
			<html:options property="demoList"/>
		  </html:select>
		  
		  <!-- 
		  <html:submit property="action">
			<bean:message key="label.change"/>
		  </html:submit>  
		   -->
        </html:form>		  
      </td>
    <form action="../../../TestWfsPost" method="POST">    
    </tr>
      <tr>
        <td class="label"><bean:message key="label.URL"/>:</td>
        <td class="datum">
          <input id="url" type="text" size="90" name="url" value="<bean:write name="demoRequestForm" property="url"/>">
        </td>
      </tr>
      <tr>
        <td class="label"><bean:message key="label.body"/>:</td>
        <td class="datum">
          <textarea rows="6" cols="90" name="body" id="body"><bean:write name="demoRequestForm" property="body"/></textarea>
        </td>
      </tr>    
      <tr>
        <td class="label" width="1%"></td>
        <td class="datum" width="99%">
          <html:submit onclick="loadResults();return false;"><bean:message key="label.submit"/></html:submit>
          <html:submit><bean:message key="label.submitNew"/></html:submit>
        </td>
      </tr>
    </form>
    <tr>
      <td class="label" width="1%"><bean:message key="label.response"/>:</td>
      <td width="99%"><iframe id="demoResponse"  style="border:1px solid"  width="100%" height="300px" onload="resize_iframe()"/></td>
    </tr>      
  </tbody>
</table>
