/*
 * Copyright (C) 2001-2025 Food and Agriculture Organization of the
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
import org.fao.geonet.utils.EmailUtil;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

/**
 * Validator for UserRegisterDto.
 *
 */
public class UserRegisterDtoValidator implements Validator {

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

        if (StringUtils.hasLength(userRegisterDto.getEmail()) && !EmailUtil.isValidEmailAddress(userRegisterDto.getEmail())) {
            errors.rejectValue("email", "field.notvalid", "Email address is not valid");
        }

        UserRepository userRepository = ApplicationContextHolder.get().getBean(UserRepository.class);
        if ((userRepository.findOneByEmail(userRegisterDto.getEmail()) != null) ||
            (!userRepository.findByUsernameIgnoreCase(userRegisterDto.getEmail()).isEmpty())) {
            errors.rejectValue("", "user_with_that_email_username_found", "A user with this email or username already exists.");
        }

    }
}
