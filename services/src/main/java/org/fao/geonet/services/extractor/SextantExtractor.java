package org.fao.geonet.services.extractor;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.naming.NamingEnumeration;
import javax.naming.directory.Attribute;
import javax.naming.directory.SearchResult;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.api.ApiUtils;
import org.fao.geonet.services.extractor.mapping.ExtractRequestSpec;
import org.fao.geonet.services.extractor.mapping.LayerSpec;
import org.fao.geonet.services.extractor.mapping.UserSpec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.ldap.DefaultSpringSecurityContextSource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.common.annotations.VisibleForTesting;

import jeeves.server.UserSession;

//=============================================================================

/**
 * This controller generates the extraction requests which will be parsed by the
 * extractor (python script launched by a CronJob), see
 * https://github.com/camptocamp/sextant
 *
 * @author pmauduit
 */

@Controller
public class SextantExtractor {

    @Autowired
    private File panierXmlPathLogged;
    @Autowired
    private File panierXmlPathAnonymous;

    private final String IFREMER_PATTERN = "@ifremer.fr";

    @PostConstruct
    public void init() throws Exception {
        // ensures directories are created
        FileUtils.forceMkdir(panierXmlPathLogged);
        FileUtils.forceMkdir(panierXmlPathAnonymous);

    }

    @RequestMapping(
        value = "/{lang}/extractor.doExtract",
        method = RequestMethod.POST,
        produces = APPLICATION_JSON_VALUE
    )
    @ResponseBody
    public Map<String, Object> exec(@RequestBody ExtractRequestSpec jsonExtractionSpec, HttpServletRequest request)
        throws Exception {
        Map<String, Object> status = new HashMap<String, Object>();
        try {

            UserSpec usr = jsonExtractionSpec.getUser();
            UserSession us = ApiUtils.getUserSession(request.getSession());

            if (us.isAuthenticated()) {
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
                // if the user is not "generic", then we trust the infos coming from the LDAP
                // instead of user provided ones.
                if (us.getPrincipal().isGeneric() == false) {
                    usr.setLastname(us.getSurname());
                    usr.setFirstname(us.getName());
                    usr.setMail(us.getEmailAddr());
                }
                String xmlString = createXmlSpecification(jsonExtractionSpec, true, us.getUsername(), uidNumberStr);

                FileUtils.writeStringToFile(new File(panierXmlPathLogged, us.getEmailAddr() + "_" + UUID.randomUUID()
                + ".xml"), xmlString);

            } else {
                // using data provided by the user
                if (usr == null) {
                    throw new RuntimeException("User not logged in, and no information provided");
                }
                String xmlString = createXmlSpecification(jsonExtractionSpec, false, null, null);
                FileUtils.writeStringToFile(
                        new File(panierXmlPathAnonymous, jsonExtractionSpec.getUser().getMail() + "_" + UUID.randomUUID() + ".xml"), xmlString);
            }
            status.put("success", true);
        } catch (Exception e) {
            status.put("success", false);
            status.put("reason", e.getMessage());
        }
        return status;
    }

    /**
     * Generates a XML specification for the Sextant Extractor.
     *
     * @param jsonExtractionSpec the unserialized from JSON extraction specification
     * @param isConnected indicates whether the user is connected or not
     * @param login if connected, the login (coming from the LDAP), discarded if anonymous
     * @param uidNumber if connected, the uidNumber as a string, discarded if anonymous
     * @return the XML representation of the extraction specification as a String.
     *
     * @throws Exception, if no user is provided in the specification.
     */
    @VisibleForTesting
    public String createXmlSpecification(ExtractRequestSpec jsonExtractionSpec, boolean isConnected, String login, String uidNumber) throws Exception {
        StringBuilder out = new StringBuilder();
        // Note: the XML is generated by hand instead of using Jackson serialization mechanisms, because
        // we could not manage to generate expected XML.
        out.append("<extract xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:noNamespaceSchemaLocation=\"extracteur.xsd\">\n");
        // if connected
        UserSpec usr = jsonExtractionSpec.getUser();
        if (usr == null) {
            throw new RuntimeException("No user found in the JSON specification");
        }
        if (isConnected) {
            out.append(String.format(
                    "  <user lastname=\"%s\" firstname=\"%s\" mail=\"%s\" is_ifremer=\"%s\""
                            + " uidNumber=\"%s\" login=\"%s\" org=\"%s\" usage=\"%s\" />\n",
                    StringEscapeUtils.escapeXml(usr.getLastname()),
                    StringEscapeUtils.escapeXml(usr.getFirstname()),
                    StringEscapeUtils.escapeXml(usr.getMail()),
                    usr.getMail().contains(IFREMER_PATTERN),
                    StringEscapeUtils.escapeXml(uidNumber),
                    StringEscapeUtils.escapeXml(login),
                    StringEscapeUtils.escapeXml(usr.getOrg()),
                    StringEscapeUtils.escapeXml(usr.getUsage())));
        } else {
            // if anonymous
            out.append(String.format(
                    "  <user lastname=\"%s\" firstname=\"%s\" mail=\"%s\" is_ifremer=\"%s\""
                            + " uidNumber=\"\" login=\"\" org=\"%s\" usage=\"%s\" />\n",
                    StringEscapeUtils.escapeXml(usr.getLastname()),
                    StringEscapeUtils.escapeXml(usr.getFirstname()),
                    StringEscapeUtils.escapeXml(usr.getMail()),
                    usr.getMail().contains(IFREMER_PATTERN),
                    StringEscapeUtils.escapeXml(usr.getOrg()),
                    StringEscapeUtils.escapeXml(usr.getUsage())));
        }
        out.append("  <layers>\n");
        for (LayerSpec l : jsonExtractionSpec.getLayers()) {
            out.append(l.asXml());
        }
        out.append(  "  </layers>\n");
        out.append("</extract>");

        return out.toString();
    }
}
