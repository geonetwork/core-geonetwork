<%@ taglib uri="/tags/struts-bean" prefix="bean" %>
<%@ taglib uri="/tags/struts-html" prefix="html" %>
<%@ taglib uri="/tags/struts-logic" prefix="logic" %>

<html:form action="/config/geoServerSubmit">
<table class="info">
  <tbody>	
	<tr>
	  <td class="label">
		<span class="help" title="<bean:message key="help.global.maxFeatures"/>">	  
          <bean:message key="label.maxFeatures"/>:
        </span>
      </td>
	  <td class="datum">
		<html:text property="maxFeatures" size="60"/>
	  </td>
	</tr>
	
	<tr>
	  <td class="label">
		<span class="help" title="<bean:message key="help.global.verbose"/>">
			<bean:message key="label.verbose"/>:
		</span>
      </td>
      <td class="datum">
		<html:checkbox property="verbose"/>
	  </td>
	</tr>
	
	<tr>
	  <td class="label">
		<span class="help" title="<bean:message key="help.global.verboseExceptions"/>">
			<bean:message key="label.verboseExceptions"/>:
		</span>
      </td>
      <td class="datum">
		<html:checkbox property="verboseExceptions"/>
	  </td>
	</tr>
	
	<tr>
	  <td class="label">
		<span class="help" title="<bean:message key="help.global.numDecimals"/>">
			<bean:message key="label.numDecimals"/>:
		</span>
      </td>
	  <td class="datum">
		<html:text property="numDecimals" size="60"/>
	  </td>
	</tr>
	
	<tr>
	  <td class="label">
		<span class="help" title="<bean:message key="help.global.charset"/>">
			<bean:message key="label.charset"/>:
		</span>
      </td>
	  <td class="datum">
		<html:text property="charset" size="60"/>
	  </td>
	</tr>
	
	<tr>
	  <td class="label">
		<span class="help" title="<bean:message key="help.global.proxyBaseURL"/>">
			<bean:message key="label.proxyBaseURL"/>:
		</span>
      </td>
	  <td class="datum">
		<html:text property="proxyBaseUrl" size="60"/>
	  </td>
	</tr>

	<!--tr>
	  <td class="label">
		<span class="help" title="<bean:message key="help.global.schemaBaseURL"/>">
			<bean:message key="label.schemaBaseURL"/>:
		</span>
      </td>
	  <td class="datum">
		<html:text property="schemaBaseURL" size="60"/>
	  </td>
	</tr-->

	<tr>
	  <td class="label">
		<span class="help" title="<bean:message key="help.global.loggingLevel"/>">
			<bean:message key="label.loggingLevel"/>:
		</span>
      </td>
	  <td class="datum">
		<html:select property="loggingLevel">
			<html:option value="OFF"/>
			<html:option value="SEVERE"/>
			<html:option value="WARNING"/>
			<html:option value="INFO"/>
			<html:option value="CONFIG"/>
			<html:option value="FINE"/>
			<html:option value="FINER"/>
			<html:option value="FINEST"/>
			<html:option value="ALL"/>			
		</html:select>
	  </td>
    </tr>	
    
    <tr>
      <td class="label">
		<span class="help" title="<bean:message key="help.global.loggingToFile"/>">
			<bean:message key="label.loggingToFile"/>:
		</span>
      </td>
	  <td class="datum">
	  	<html:checkbox property="loggingToFile"/>
	  </td>
	 </tr> 
	 
	 <tr>
		 <td class="label">
		  	<span class="help" title="<bean:message key="help.global.logLocation"/>">
				<bean:message key="label.logLocation"/>:
			</span>
		</td>
		<td class="datum">
		<logic:empty name="geoServerConfigurationForm" property="logLocation">
			<html:text property="logLocation" size="60" value="logs/geoserver.log"/>		
		</logic:empty>
		<logic:notEmpty name="geoServerConfigurationForm" property="logLocation">
			<html:text property="logLocation" size="60"/>		
		</logic:notEmpty>

		</td>
    </tr>	

	<tr>
	  <td class="label">
		<span class="help" title="<bean:message key="help.global.jaiMemoryCapacity"/>">
			<bean:message key="label.jaiMemoryCapacity"/>:
		</span>
      </td>
	  <td class="datum">
		<html:text property="jaiMemoryCapacity" size="60"/>
	  </td>
	</tr>

	<tr>
	  <td class="label">
		<span class="help" title="<bean:message key="help.global.jaiMemoryThreshold"/>">
			<bean:message key="label.jaiMemoryThreshold"/>:
		</span>
    </td>
	  <td class="datum">
		<html:text property="jaiMemoryThreshold" size="60"/>
	  </td>
	</tr>

	<tr>
	  <td class="label">
		<span class="help" title="<bean:message key="help.global.jaiTileThreads"/>">
			<bean:message key="label.jaiTileThreads"/>:
		</span>
    </td>
	  <td class="datum">
		<html:text property="jaiTileThreads" size="60"/>
	  </td>
	</tr>

	<tr>
	  <td class="label">
		<span class="help" title="<bean:message key="help.global.jaiTilePriority"/>">
			<bean:message key="label.jaiTilePriority"/>:
		</span>
    </td>
	  <td class="datum">
		<html:text property="jaiTilePriority" size="60"/>
	  </td>
	</tr>

	<tr>
      <td class="label">
		<span class="help" title="<bean:message key="help.global.jaiRecycling"/>">
			<bean:message key="label.jaiRecycling"/>:
		</span>
      </td>
	  <td class="datum">
	  	<html:checkbox property="jaiRecycling"/>
	  </td>
	 </tr> 

    <tr>
      <td class="label">
        <span class="help" title="<bean:message key="help.global.imageIOCache"/>">
            <bean:message key="label.imageIOCache"/>:
        </span>
      </td>
      <td class="datum">
        <html:checkbox property="imageIOCache"/>
      </td>
     </tr> 

	<tr>
      <td class="label">
		<span class="help" title="<bean:message key="help.global.jaiJPEGNative"/>">
			<bean:message key="label.jaiJPEGNative"/>:
		</span>
      </td>
	  <td class="datum">
	  	<html:checkbox property="jaiJPEGNative"/>
	  </td>
	 </tr> 
	<tr>
      <td class="label">
		<span class="help" title="<bean:message key="help.global.jaiPNGNative"/>">
			<bean:message key="label.jaiPNGNative"/>:
		</span>
      </td>
	  <td class="datum">
	  	<html:checkbox property="jaiPNGNative"/>
	  </td>
	 </tr> 

	<tr>
		<td class="label">
			<span class="help" title="<bean:message key="help.global.tileCache"/>">
				<bean:message key="label.tileCache"/>:
			</span>
		</td>
		<td class="datum">
        	<html:text property="tileCache" size="60"/>
		</td>
	</tr> 
	
  </tbody>
</table>

<h3><bean:message key="label.contactInformation"/></h3>

<table class="info">
  <tbody>
	<tr>
	  <td class="label">
	<tr><td colspan=2><center><b></b></center>
	  </td>
	</tr>

	<tr>
	  <td class="label">
		<span class="help" title="<bean:message key="help.global.contactPerson"/>">
			<bean:message key="label.contactPerson"/>:
		</span>
      </td>
	  <td class="datum">
		<html:text property="contactPerson" size="60"/>
	  </td>
	</tr>

	<tr>
	  <td class="label">
		<span class="help" title="<bean:message key="help.global.contactOrganization"/>">
			<bean:message key="label.contactOrganization"/>:
		</span>
      </td>
	  <td class="datum">
		<html:text property="contactOrganization" size="60"/>
	  </td>
	</tr>

	<tr>
	  <td class="label">
		<span class="help" title="<bean:message key="help.global.contactPosition"/>">
			<bean:message key="label.contactPosition"/>:
		</span>
      </td>
	  <td class="datum">
		<html:text property="contactPosition" size="60"/>
	  </td>
	</tr>

	<tr>
	  <td class="label">
		<span class="help" title="<bean:message key="help.global.addressType"/>">
			<bean:message key="label.addressType"/>:
		</span>
      </td>
	  <td class="datum">
		<html:text property="addressType" size="60"/>
	  </td>
	</tr>

	<tr>
	  <td class="label">
		<span class="help" title="<bean:message key="help.global.address"/>">
			<bean:message key="label.address"/>:
		</span>
      </td>
	  <td class="datum">
		<html:text property="address" size="60"/>
	  </td>
	</tr>

	<tr>
	  <td class="label">
		<span class="help" title="<bean:message key="help.global.addressCity"/>">
			<bean:message key="label.addressCity"/>:
		</span>
      </td>
	  <td class="datum">
		<html:text property="addressCity" size="60"/>
	  </td>
	</tr>

	<tr>
	  <td class="label">
		<span class="help" title="<bean:message key="help.global.addressState"/>">
			<bean:message key="label.addressState"/>:
		</span>
      </td>
	  <td class="datum">
		<html:text property="addressState" size="60"/>
	  </td>
	</tr>

	<tr>
	  <td class="label">
		<span class="help" title="<bean:message key="help.global.addressPostalCode"/>">
			<bean:message key="label.addressPostalCode"/>:
		</span>
      </td>
	  <td class="datum">
		<html:text property="addressPostalCode" size="60"/>
	  </td>
	</tr>

	<tr>
	  <td class="label">
		<span class="help" title="<bean:message key="help.global.addressCountry"/>">
			<bean:message key="label.addressCountry"/>:
		</span>
      </td>
	  <td class="datum">
		<html:text property="addressCountry" size="60"/>
	  </td>
	</tr>

	<tr>
	  <td class="label">
		<span class="help" title="<bean:message key="help.global.contactVoice"/>">
			<bean:message key="label.contactVoice"/>:
		</span>
      </td>
	  <td class="datum">
		<html:text property="contactVoice" size="60"/>
	  </td>
	</tr>

	<tr>
	  <td class="label">
		<span class="help" title="<bean:message key="help.global.contactFacsimile"/>">
			<bean:message key="label.contactFacsimile"/>:
		</span>
      </td>
	  <td class="datum">
		<html:text property="contactFacsimile" size="60"/>
	  </td>
	</tr>

	<tr>
	  <td class="label">
		<span class="help" title="<bean:message key="help.global.contactEmail"/>">
			<bean:message key="label.contactEmail"/>:
		</span>
      </td>
	  <td class="datum">
		<html:text property="contactEmail" size="60"/>
	  </td>
	</tr>

	<tr>
	  <td class="label">
	  </td>
	  <td class="datum">
		<html:submit>
			<bean:message key="label.submit"/>
		</html:submit>
		
		<html:reset>
			<bean:message key="label.reset"/>
		</html:reset>
	  </td>
	</tr>	
	
  </tbody>
</table>
</html:form>