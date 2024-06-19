/*
 * Copyright (C) 2001-2024 Food and Agriculture Organization of the
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

import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.api.users.model.UserRegisterDto;
import org.fao.geonet.constants.Params;
import org.fao.geonet.repository.UserRepository;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

/**
 * Validator for UserRegisterDto.
 *
 */
public class UserRegisterDtoValidator implements Validator {
    private static final String OWASP_EMAIL_REGEX = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
    private static final java.util.regex.Pattern OWASP_EMAIL_PATTERN = java.util.regex.Pattern.compile(OWASP_EMAIL_REGEX);

    @Override
    public boolean supports(Class<?> clazz) {
        return UserRegisterDto.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        UserRegisterDto userRegisterDto = (UserRegisterDto) target;

        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "name", "field.required", Params.NAME
            + " is required");

        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "email", "field.required", Params.EMAIL
            + " is required");

        if (StringUtils.hasLength(userRegisterDto.getEmail()) && !OWASP_EMAIL_PATTERN.matcher(userRegisterDto.getEmail()).matches()) {
            errors.rejectValue("email", "field.notvalid", "Email address is not valid");
        }

        UserRepository userRepository = ApplicationContextHolder.get().getBean(UserRepository.class);
        if (userRepository.findOneByEmail(userRegisterDto.getEmail()) != null) {
            errors.rejectValue("", "user_with_that_email_found", "A user with this email or username already exists.");
        }

        if (userRepository.findByUsernameIgnoreCase(userRegisterDto.getEmail()).size() != 0) {
            errors.rejectValue("", "user_with_that_username_found", "A user with this email or username already exists.");
        }
    }
}
