/*
 * Copyright (C) 2001-2016 Food and Agriculture Organization of the
 * United Nations (FAO-UN), United Nations World Food Programme (WFP)
 * and United Nations Environment Programme (UNEP)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
 *
 * Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
 * Rome - Italy. email: geonetwork@osgeo.org
 */
package org.fao.geonet.api.users.recaptcha;

import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.classic.methods.HttpUriRequest;
import org.apache.hc.core5.http.io.support.ClassicRequestBuilder;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.utils.GeonetHttpRequestFactory;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;


/**
 * Recaptcha response checker.
 *
 * @author Jose GArcía
 */
public class RecaptchaChecker {

    private final static String RECAPTCHA_URL = "https://www.google.com/recaptcha/api/siteverify";
    private final static String USER_AGENT = "Mozilla/5.0";

    /**
     * Verifies the recaptcha response.
     *
     * @param recaptchaResponse Recaptcha response.
     * @param secret            Secret key for Google Recaptcha.
     * @return
     * @throws IOException
     */
    public static boolean verify(final String recaptchaResponse,
                                 final String secret) throws IOException {
        if (StringUtils.isEmpty(recaptchaResponse)) {
            return false;
        }

        final GeonetHttpRequestFactory requestFactory =
            ApplicationContextHolder.get().getBean(GeonetHttpRequestFactory.class);

        ClientHttpResponse httpResponse = null;
        try {
            //todo: verify this is still behaves correctly (http4 -> http5)
            var httpPost =  (HttpPost) ClassicRequestBuilder.post()
                .setUri(RECAPTCHA_URL)
                .setHeader("User-Agent", USER_AGENT)
                .setHeader("Accept-Language", "en-US,en;q=0.5")
                .addParameter("secret", secret)
                .addParameter("response", recaptchaResponse)
                .build();

            httpResponse = requestFactory.execute(httpPost);

            if (httpResponse.getStatusCode().value() == 200) {
                String responseText = IOUtils.toString(httpResponse.getBody());

                //parse JSON response and return 'success' value
                JSONObject jsonObject = (JSONObject) JSONSerializer.toJSON(responseText);

                return jsonObject.getBoolean("success");
            } else {
                return false;
            }
        } catch (Throwable e) {
            return false;
        } finally {
            IOUtils.closeQuietly(httpResponse);
        }
    }
}
