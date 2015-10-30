/**
 * 
 */
package org.fao.geonet.services.openwis.subscription;

import java.util.Locale;

import org.openwis.subscription.client.Diffusion;
import org.openwis.subscription.client.Dissemination;
import org.openwis.subscription.client.DisseminationZipMode;
import org.openwis.subscription.client.ExtractMode;
import org.openwis.subscription.client.FtpDiffusion;
import org.openwis.subscription.client.MailAttachmentMode;
import org.openwis.subscription.client.MailDiffusion;
import org.openwis.subscription.client.MailDispatchMode;
import org.openwis.subscription.client.PublicDissemination;
import org.openwis.subscription.client.ShoppingCartDissemination;
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
public class DisseminationPairFormatter
        implements Formatter<DisseminationPair> {

    /**
     * @see org.springframework.format.Printer#print(java.lang.Object,
     *      java.util.Locale)
     * @param object
     * @param locale
     * @return
     */
    @Override
    public String print(DisseminationPair object, Locale locale) {
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
    public DisseminationPair parse(String text, Locale locale)
            throws java.text.ParseException {
        DisseminationPair dis = new DisseminationPair();

        JSONObject json = JSONObject.fromObject(text);

        if (json.containsKey("id")) {
            dis.setId(Long.valueOf(json.getString("id")));
        }
        dis.setMetadataUrn(json.getString("metadataUrn"));
        dis.setUsername(json.getString("username"));
        dis.setExtractMode(ExtractMode
                .fromValue(json.getString("extractMode").toUpperCase()));

        dis.setPrimary(getDissemination(json.getJSONObject("primary")));
        dis.setSecondary(getDissemination(json.getJSONObject("secondary")));

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
