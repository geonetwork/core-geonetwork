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

package jeeves.config.springutil;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;

public class AddAuthenticationProviderPostProcessor implements BeanPostProcessor {

    private final int addIndex;
    private final AuthenticationProvider providerToAdd;

    /**
     * @param addIndex the index of the location in the provider list to add the new provider.  If <
     *                 0 then add to end of list
     */
    public AddAuthenticationProviderPostProcessor(int addIndex, AuthenticationProvider providerToAdd) {
        this.addIndex = addIndex;
        this.providerToAdd = providerToAdd;
    }

    public AddAuthenticationProviderPostProcessor(AuthenticationProvider providerToAdd) {
        this(-1, providerToAdd);
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName)
        throws BeansException {
        // Nothing to do
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName)
        throws BeansException {
        if (beanName.equals("authenticationManager")) {
            ProviderManager authManager = (ProviderManager) bean;
            if (addIndex < 0) {
                authManager.getProviders().add(providerToAdd);
            } else {
                authManager.getProviders().add(addIndex, providerToAdd);
            }
        }
        return bean;
    }

}
