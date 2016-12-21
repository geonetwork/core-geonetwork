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

package org.fao.geonet.util;

import jeeves.server.context.ServiceContext;
import org.fao.geonet.Constants;
import org.fao.geonet.domain.User;
import org.fao.geonet.domain.UserSecurityNotification;
import org.fao.geonet.exceptions.UserNotFoundEx;
import org.fao.geonet.repository.UserRepository;
import org.springframework.context.ApplicationContext;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletContext;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;

/**
 * Geonetwork has had several password storing mechanisms in the past and upgrading is a tricky
 * issue. As of version 2.9.0 the password hashes have been moved to the secure spring security
 * hashes which are SHA-256 based with a system-wide salt and a key specific salt.  However a
 * migration path is needed. A column "security" has been added to the users table which contains
 * security related flags.  One such flag is update_hash_required which indicates the password has
 * an old password and needs to be updated.  When a user logs in the new hash will be calculated.
 *
 * @author jeichar
 */
public class PasswordUtil {

    public static final String ENCODER_ID = "geonetworkEncoder";
    public static final String PASSWORD_COLUMN = "password";

    /**
     * Check the security column for the {@link UserSecurityNotification#UPDATE_HASH_REQUIRED}.
     * tag.
     *
     * @param user the user to check.
     * @return true if the user needs its hash updated.
     */
    public static boolean hasOldHash(User user) {
        return user.getSecurity().getSecurityNotifications().contains(UserSecurityNotification.UPDATE_HASH_REQUIRED);
    }

    /**
     * Compare the hash (read from database) to *all* type of hashes used by geonetwork.  This
     * should not be used for logging in, only for upgrading passwords.
     *
     * @param encoder  encoder to use
     * @param hash     the hash used for comparison
     * @param password the unhashed password to compare to hash with the different hashing
     *                 techniques
     * @return true is password matches hash
     */
    public static boolean matchesAnyHash(PasswordEncoder encoder, String hash, String password) {
        return matchesOldHash(hash, password) || encoder.matches(password, hash);
    }

    /**
     * Check if the password matches is one of the old outdated hashes
     *
     * @param hash     the hash used for comparison
     * @param password the unhashed password to compare to hash with the different hashing
     *                 techniques
     * @return true if password matches one of the hashes
     */
    public static boolean matchesOldHash(String hash, String password) {
        return unsaltedScramble(password).equals(hash) || oldScramble(password).equals(hash);
    }

    /**
     * SHA-1 Cryptographic hash algorithm See #191
     *
     * @param text password to digest
     * @return the hexadecimal encoded string
     * @deprecated
     */
    private static String unsaltedScramble(String text) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            md.update(text.getBytes(Constants.ENCODING));
            byte[] raw = md.digest();
            if (raw == null) {
                return null;
            }
            final StringBuilder hex = new StringBuilder(2 * raw.length);
            for (final byte b : raw) {
                hex.append("0123456789abcdef".charAt((b & 0xF0) >> 4)).append(
                    "0123456789abcdef".charAt((b & 0x0F)));
            }
            return hex.toString();
        } catch (UnsupportedEncodingException e) {
            return null;
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
    }

    /**
     * Old Jeeves scramble method which lost leading 0 during byte to hexadecimal string
     * conversion.
     *
     * @return the hexadecimal encoded string with missing leading 0
     * @deprecated
     */
    private static String oldScramble(String text) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");

            md.update(text.getBytes(Constants.ENCODING));

            StringBuffer sb = new StringBuffer();

            for (byte b : md.digest())
                sb.append(Integer.toString(b & 0xFF, 16));    // #191 : here leading 0 are removed

            return sb.toString();
        } catch (UnsupportedEncodingException e) {
            return null;
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
    }

    /**
     * Obtain the password encoder from the spring application context.
     *
     * @return the pasword encoder from the spring application context.
     */
    public static PasswordEncoder encoder(ServletContext servletContext) {
        WebApplicationContext appcontext = WebApplicationContextUtils.getRequiredWebApplicationContext(servletContext);
        return (PasswordEncoder) appcontext.getBean(ENCODER_ID);
    }

    /**
     * Obtain the password encoder from the spring application context.
     *
     * @return the pasword encoder from the spring application context.
     */
    public static PasswordEncoder encoder(ApplicationContext appContext) {
        return (PasswordEncoder) appContext.getBean(ENCODER_ID);
    }

    /**
     * Updates database with new password if passwords match.
     *
     * @param matchOldPassword if false and oldPassword is null then password will be updated
     *                         without checking old password.
     * @param oldPassword      the old password (obtained from user. not a hash).
     * @param newPassword      the new password.
     * @param iUserId          the user id.
     * @param appContext       the application context, used to obtain the password encoder.
     * @return the xml from the database query containing the new password hash.
     * @throws SQLException   if an error occurred during a database access.
     * @throws UserNotFoundEx if the id does not reference a user.
     */
    public static User updatePasswordWithNew(boolean matchOldPassword, String oldPassword,
                                             String newPassword, Integer iUserId, ApplicationContext appContext) throws SQLException, UserNotFoundEx {
        UserRepository repo = appContext.getBean(UserRepository.class);
        User user = repo.findOne(iUserId);
        if (user == null) {
            throw new UserNotFoundEx("" + iUserId);
        }

        PasswordEncoder encoder = encoder(appContext);
        return updatePasswordWithNew(matchOldPassword, oldPassword, newPassword, user, encoder, repo);
    }

    /**
     * Updates database with new password if passwords match
     *
     * @param matchOldPassword if false and oldPassword is null then password will be updated
     *                         without checking old password
     * @param oldPassword      the old password (obtained from user. not a hash)
     * @param newPassword      the new password
     * @param user             the user to update
     * @param encoder          the Password encoder
     * @return the xml from the database query containing the new password hash
     * @throws SQLException   if an error occurred during a database access
     * @throws UserNotFoundEx if the id does not reference a user
     */
    public static User updatePasswordWithNew(boolean matchOldPassword, String oldPassword,
                                             String newPassword, User user, PasswordEncoder encoder,
                                             UserRepository repository) throws SQLException, UserNotFoundEx {
        String hash = user.getPassword();
        if (hasOldHash(user)) {
            if ((matchOldPassword || newPassword != null) && !matchesOldHash(hash, newPassword)) {
                throw new IllegalArgumentException("Password is not correct. It does not match old hash.");
            }
        } else {
            if ((matchOldPassword || oldPassword != null) && !encoder.matches(oldPassword, hash)) {
                throw new IllegalArgumentException("Old password is not correct");
            }
        }

        user.getSecurity().getSecurityNotifications().remove(UserSecurityNotification.UPDATE_HASH_REQUIRED);

        String newPasswordHash = encoder.encode(newPassword);
        user.getSecurity().setPassword(newPasswordHash.toCharArray());

        repository.save(user);
        return user;
    }

    public static String encode(ServiceContext context, String password) {
        return encoder(context.getApplicationContext()).encode(password);
    }
}
