package org.fao.geonet.services.extractor;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.naming.NamingEnumeration;
import javax.naming.directory.Attribute;
import javax.naming.directory.SearchResult;
import javax.servlet.http.HttpServletRequest;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import jeeves.server.dispatchers.ServiceManager;

import org.apache.commons.io.FileUtils;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.domain.LDAPUser;
import org.fao.geonet.domain.User;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.security.ldap.LDAPUtils;
import org.fao.geonet.services.extractor.mapping.ExtractRequestSpec;
import org.fao.geonet.services.extractor.mapping.LayerSpec;
import org.fao.geonet.services.extractor.mapping.UserSpec;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.security.ldap.DefaultSpringSecurityContextSource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.fasterxml.jackson.xml.XmlMapper;

//=============================================================================

/**
 * This controller generates the extraction requests which will be parsed by the
 * extractor (python script launched by a CronJob), see
 * https://github.com/camptocamp/sextant
 * 
 * @author pmauduit
 * 
 */

@Controller
public class SextantExtractor {

	@Autowired
	private DataManager dataManager;
	@Autowired
	private ServiceManager serviceManager;
	@Autowired
	private File panierXmlPathLogged;
	@Autowired
	private File panierXmlPathAnonymous;

	@PostConstruct
	public void init() throws Exception {
		// ensures directories are created
		FileUtils.forceMkdir(panierXmlPathLogged);
		FileUtils.forceMkdir(panierXmlPathAnonymous);

	}

	@RequestMapping(value = "/{lang}/extractor.doExtract", method = RequestMethod.POST)
	public HttpEntity<byte[]> exec(@RequestBody ExtractRequestSpec jsonExtractionSpec, HttpServletRequest request)
			throws Exception {
		JSONObject status = new JSONObject();
		try {
			XmlMapper xmlMapper = new XmlMapper();
			ByteArrayOutputStream out = new ByteArrayOutputStream();

			out.write("<extract xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:noNamespaceSchemaLocation=\"extracteur.xsd\">"
					.getBytes());
			final ServiceContext serviceContext = ServiceContext.get();

			UserSession us = serviceContext.getUserSession();
			boolean isAuthenticated = us.isAuthenticated();

			if (isAuthenticated) {
				// Some infos in the XML should come from the LDAP
				DefaultSpringSecurityContextSource contextSource = (DefaultSpringSecurityContextSource) ApplicationContextHolder
						.get().getBean("contextSource");
				String ldapUserSearchBase = (String) ApplicationContextHolder.get().getBean(
						"extractorLdapUserSearchBase");
				String ldapUserSearchAttribute = (String) ApplicationContextHolder.get().getBean(
						"extractorLdapUserSearchAttribute");

				NamingEnumeration<SearchResult> ldapRes = contextSource.getReadOnlyContext().search(ldapUserSearchBase,
						ldapUserSearchAttribute.replace("{0}", us.getUsername()), null);
				String uidNumberStr = "";
				while (ldapRes.hasMore()) {
					SearchResult r = ldapRes.next();
					Attribute uidNumber = r.getAttributes().get("uidNumber");
					uidNumberStr = uidNumber.get().toString();
				}

				out.write(String.format(
						"<user lastname=\"%s\" firstname=\"%s\" mail=\"%s\" is_ifremer=\"%s\""
								+ " uidNumber=\"%s\" login=\"%s\" org=\"%s\" usage=\"%s\" />", us.getName(),
						us.getSurname(), us.getEmailAddr(), us.getPrincipal().getOrganisation().equals("ifremer"),
						uidNumberStr, us.getUsername(), jsonExtractionSpec.getUser().getOrg(),
						jsonExtractionSpec.getUser().getUsage()).getBytes());
			} else {
				// using data provided by the user
				UserSpec usr = jsonExtractionSpec.getUser();
				if (usr == null) {
					throw new RuntimeException("User not logged in, and no information provided");
				}
				out.write(String.format(
						"<user lastname=\"%s\" firstname=\"%s\" mail=\"%s\" is_ifremer=\"%s\""
								+ " org=\"%s\" usage=\"%s\" />", usr.getLastname(), usr.getFirstname(), usr.getMail(),
						"false", usr.getOrg(), usr.getUsage()).getBytes());
			}
			out.write("<layers>".getBytes());
			for (LayerSpec l : jsonExtractionSpec.getLayers()) {
				xmlMapper.writeValue(out, l);
			}
			out.write("</layers>".getBytes());
			out.write("</extract>".getBytes());
			// Indent using the available XML lib
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
			StreamResult result = new StreamResult(new StringWriter());
			StreamSource source = new StreamSource(new StringReader(out.toString()));
			transformer.transform(source, result);
			String xmlString = result.getWriter().toString();

			if (isAuthenticated) {
				FileUtils.writeStringToFile(new File(panierXmlPathLogged, us.getEmailAddr() + "_" + UUID.randomUUID()
						+ ".xml"), xmlString);
			} else {
				FileUtils.writeStringToFile(
						new File(panierXmlPathAnonymous, "anonymous_" + UUID.randomUUID() + ".xml"), xmlString);
			}
			status.put("success", true);
		} catch (Exception e) {
			status.put("success", false);
			status.put("reason", e.getMessage());
		}
		return new HttpEntity<byte[]>(status.toString(4).getBytes());
	}

}

// =============================================================================
