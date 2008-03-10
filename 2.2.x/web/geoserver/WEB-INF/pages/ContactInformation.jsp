<%@ taglib uri="/tags/struts-bean" prefix="bean" %>
<%@ taglib uri="/tags/struts-html" prefix="html" %>
<%@ taglib uri="/tags/struts-logic" prefix="logic" %>

<logic:notPresent name="org.apache.struts.action.MESSAGE" scope="application">
  <span class="error">
    <bean:message key="message.notLoaded"/>  
  </span>
</logic:notPresent>

<table class="info">
  <tbody>
<logic:notEmpty name="GeoServer" property="contactPerson">  
    <tr>
      <td class="label"><bean:message key="label.contactPerson"/>:</td>
      <td class="datum">
        <bean:write name="GeoServer" property="contactPerson"/>
      </td>      
    </tr>
</logic:notEmpty>    

<logic:notEmpty name="GeoServer" property="contactPosition">  
    <tr>
      <td class="label"><bean:message key="label.contactPosition"/>:</td>
      <td class="datum">
        <bean:write name="GeoServer" property="contactPosition"/>
      </td>
    </tr>
</logic:notEmpty>

<logic:notEmpty name="GeoServer" property="contactOrganization">
    <tr>
      <td class="label"><bean:message key="label.contactOrganization"/>:</td>
      <td class="datum">   
        <bean:write name="GeoServer" property="contactOrganization"/>
      </td>
    </tr>
</logic:notEmpty>

<logic:empty name="GeoServer" property="contactParty">
    <tr>
      <td class="label"><bean:message key="label.contact"/>:</td>
      <td class="datum">   
        <bean:message key="message.noContact"/>
      </td>
    </tr>
</logic:empty>

<logic:notEmpty name="GeoServer" property="address">    
    <tr>
      <td class="label">
        Address:
      </td>
      <td class="datum">   
        <bean:write name="GeoServer" property="addressType"/><br>
        <bean:write name="GeoServer" property="address"/><br>        
        <bean:write name="GeoServer" property="addressCity"/><br>
        <bean:write name="GeoServer" property="addressState"/>&nbsp;&nbsp;
        <bean:write name="GeoServer" property="addressPostalCode"/><br>
        <bean:write name="GeoServer" property="addressCountry"/><br>        
      </td>
    </tr>
</logic:notEmpty>

<logic:notEmpty name="GeoServer" property="contactVoice">
    <tr>
      <td class="label"><bean:message key="label.phoneNumber"/>:</td>
      <td class="datum">
        <bean:write name="GeoServer" property="contactVoice"/>
      </td>
    </tr>
</logic:notEmpty>

<logic:notEmpty name="GeoServer" property="contactFacsimile">
    <tr>
      <td class="label"><bean:message key="label.contactFacsimile"/>:</td>
      <td class="datum">
        <bean:write name="GeoServer" property="contactFacsimile"/>
      </td>
    </tr>
</logic:notEmpty>

<logic:notEmpty name="GeoServer" property="contactEmail">    
    <tr>
      <td class="label"><bean:message key="label.contactEmail"/>:</td>
      <td class="datum">
        <bean:write name="GeoServer" property="contactEmail"/>
      </td>
    </tr>
</logic:notEmpty>
    
  </tbody>
</table>