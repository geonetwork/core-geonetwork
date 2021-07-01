/*
 * Copyright (C) 2001-2021 Food and Agriculture Organization of the
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

package org.fao.geonet.api.users.validation;

import org.apache.commons.lang.StringUtils;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.kernel.setting.Settings;
import org.springframework.validation.Errors;

import java.util.regex.Pattern;

/**
 * Utility class for password validation.
 *
 */
public class PasswordValidationUtils {
    public static void rejectIfInvalid(Errors errors, String password) {

        SettingManager sm =ApplicationContextHolder.get().getBean(SettingManager.class);
        Integer minLength = sm.getValueAsInt(Settings.SYSTEM_SECURITY_PASSWORDENFORCEMENT_MINLENGH);
        Integer maxLength = sm.getValueAsInt(Settings.SYSTEM_SECURITY_PASSWORDENFORCEMENT_MAXLENGH);
        boolean usePattern = sm.getValueAsBool(Settings.SYSTEM_SECURITY_PASSWORDENFORCEMENT_USEPATTERN);
        String pattern = sm.getValue(Settings.SYSTEM_SECURITY_PASSWORDENFORCEMENT_PATTERN);

        if (StringUtils.isEmpty(password) ||
            password.trim().length() < minLength) {
            errors.rejectValue("password", "field.length",
                new Object[]{Integer.valueOf(minLength), Integer.valueOf(maxLength)},
                "Password size should be between " + minLength + " and " + maxLength + " characters");
        }

        if (StringUtils.isNotEmpty(password) &&
            password.trim().length() > maxLength) {
            errors.rejectValue("password", "field.length",
                new Object[]{Integer.valueOf(minLength), Integer.valueOf(maxLength)},
                "Password size should be between " + minLength + " and " + maxLength + " characters");
        }

        if (usePattern && !Pattern.matches(pattern, password)) {
            errors.rejectValue("password", "field.invalid",
                new Object[]{Integer.valueOf(maxLength)},
                "Password must contain at least 1 uppercase, 1 lowercase, 1 number and 1 symbol. Symbols include: `~!@#$%^&*()-_=+[]{}\\\\|;:'\",.<>/?');");
        }
    }
}
