/**
 * 
 */
package org.fao.geonet.services.openwis.subscription;

import java.util.Locale;

import org.openwis.request.client.ClassOfService;
import org.openwis.request.client.Diffusion;
import org.openwis.request.client.Dissemination;
import org.openwis.request.client.DisseminationZipMode;
import org.openwis.request.client.ExtractMode;
import org.openwis.request.client.FtpDiffusion;
import org.openwis.request.client.MailAttachmentMode;
import org.openwis.request.client.MailDiffusion;
import org.openwis.request.client.MailDispatchMode;
import org.openwis.request.client.PublicDissemination;
import org.openwis.request.client.ShoppingCartDissemination;
import org.springframework.expression.ParseException;
import org.springframework.format.Formatter;
import org.springframework.stereotype.Component;

import net.sf.json.JSONObject;

/**
 * openwis4-openwis-services
 * 
 * @author delawen
 * 
 * 
 */
@Component
public class DisseminationPairRequestFormatter
        implements Formatter<DisseminationPairRequest> {

    /**
     * @see org.springframework.format.Printer#print(java.lang.Object,
     *      java.util.Locale)
     * @param object
     * @param locale
     * @return
     */
    @Override
    public String print(DisseminationPairRequest object, Locale locale) {
        return object.toString();
    }

    /**
     * @see org.springframework.format.Parser#parse(java.lang.String,
     *      java.util.Locale)
     * @param text
     * @param locale
     * @return
     * @throws ParseException
     */
    @Override
    public DisseminationPairRequest parse(String text, Locale locale)
            throws java.text.ParseException {
        DisseminationPairRequest dis = new DisseminationPairRequest();

        JSONObject json = JSONObject.fromObject(text);

        dis.setMetadataUrn(json.getString("metadataUrn"));
        dis.setUsername(json.getString("username"));
        dis.setExtractMode(ExtractMode
                .fromValue(json.getString("extractMode").toUpperCase()));

        dis.setPrimary(getDissemination(json.getJSONObject("primary")));
        dis.setSecondary(getDissemination(json.getJSONObject("secondary")));

        if (json.containsKey("email")) {
            dis.setEmail(json.getString("email"));
        }
        if (json.containsKey("requestType")) {
            dis.setRequestType(json.getString("requestType"));
        }
        if (json.containsKey("classOfService")) {
            dis.setClassOfService(ClassOfService
                    .fromValue(json.getString("classOfService").toUpperCase()));
        }

        return dis;
    }

    /**
     * @param jsonObject
     * @return
     */
    private Dissemination getDissemination(JSONObject json) {
        Dissemination d = null;

        if (!json.isEmpty()) {

            if ((json.containsKey("email") && json.getString("email") != null
                    && !json.getString("email").equals("null"))
                    || (json.containsKey("host")
                            && json.getString("host") != null)
                            && !json.getString("host").equals("null")) {
                d = new PublicDissemination();

                Diffusion dif = null;
                if (json.containsKey("email") && json.getString("email") != null
                        && !json.getString("email").equals("null")) {
                    dif = new MailDiffusion();
                    // mandatory
                    ((MailDiffusion) dif).setAddress(json.getString("email"));

                    // optional
                    if (json.containsKey("fileName")) {
                        ((MailDiffusion) dif)
                                .setFileName(json.getString("fileName"));
                    }
                    if (json.containsKey("headerLine")) {
                        ((MailDiffusion) dif)
                                .setHeaderLine(json.getString("headerLine"));
                    }
                    if (json.containsKey("subject")) {
                        ((MailDiffusion) dif)
                                .setSubject(json.getString("subject"));
                    }
                    if (json.containsKey("attachmentMode") && !json
                            .getString("attachmentMode").equals("null")) {
                        ((MailDiffusion) dif).setMailAttachmentMode(
                                MailAttachmentMode.fromValue(
                                        json.getString("attachmentMode")
                                                .toUpperCase()));
                    }
                    if (json.containsKey("dispatchMode")
                            && !json.getString("dispatchMode").equals("null")) {
                        ((MailDiffusion) dif)
                                .setMailDispatchMode(MailDispatchMode.fromValue(
                                        json.getString("dispatchMode")
                                                .toUpperCase()));
                    }
                } else {
                    dif = new FtpDiffusion();
                    // mandatory
                    ((FtpDiffusion) dif).setHost(json.getString("host"));
                    ((FtpDiffusion) dif).setPath(json.getString("path"));
                    ((FtpDiffusion) dif).setUser(json.getString("user"));
                    ((FtpDiffusion) dif)
                            .setPassword(json.getString("password"));

                    // optional
                    if (json.containsKey("fileSize")) {
                        ((FtpDiffusion) dif)
                                .setCheckFileSize(json.getBoolean("fileSize"));
                    }
                    if (json.containsKey("encrypted")) {
                        ((FtpDiffusion) dif)
                                .setEncrypted(json.getBoolean("encrypted"));
                    }
                    if (json.containsKey("fileName")) {
                        ((FtpDiffusion) dif)
                                .setFileName(json.getString("fileName"));
                    }
                    if (json.containsKey("passive")) {
                        ((FtpDiffusion) dif)
                                .setPassive(json.getBoolean("passive"));
                    }
                    if (json.containsKey("port")) {
                        ((FtpDiffusion) dif).setPort(json.getString("port"));
                    }

                }
                ((PublicDissemination) d).setDiffusion(dif);
            } else {
                d = new ShoppingCartDissemination();
            }

            d.setZipMode(DisseminationZipMode
                    .fromValue(json.getString("compression").toUpperCase()));
        }
        return d;
    }

}
