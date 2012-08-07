package jeeves.utils;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;

import javax.servlet.ServletContext;

import jeeves.exceptions.UserNotFoundEx;
import jeeves.resources.dbms.Dbms;
import jeeves.server.context.ServiceContext;

import org.jdom.Element;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * Geonetwork has had several password storing mechanisms in the past and upgrading is a tricky issue.  
 * As of version 2.9.0 the password hashes have been moved to the secure spring security hashes which are
 * SHA-256 based with a system-wide salt and a key specific salt.  However a migration path is needed.
 * A column "security" has been added to the users table which contains security related flags.  One such flag
 * is update_hash_required which indicates the password has an old password and needs to be updated.  When a user 
 * logs in the new hash will be calculated.
 * 
 * @author jeichar
 */
public class PasswordUtil {

	public static final String HASH_UPDATE_REQUIRED = "update_hash_required";
	public static final String SECURITY_FIELD = "security";
	public static final String ENCODER_ID = "geonetworkEncoder";
	public static final String PASSWORD_COLUMN = "password";

	/**
	 * Check the security column for the {@value PasswordUtil.HASH_UPDATE_REQUIRED} tag.
	 * 
	 * @param userXml a database query for the user containing the security column
	 * @return true if the user needs its hash updated
	 */
	public static boolean hasOldHash(Element userXml) {
		return userXml.getChildText(SECURITY_FIELD).contains(HASH_UPDATE_REQUIRED);
	}
	/**
	 * Compare the hash (read from database) to *all* type of hashes used by geonetwork.  This should not be used
	 * for logging in, only for upgrading passwords.
	 * 
	 * @param encoder encoder to use
	 * @param hash the hash used for comparison 
	 * @param password the unhashed password to compare to hash with the different hashing techniques
	 * 
	 * @return true is password matches hash
	 */
	public static boolean matchesAnyHash(PasswordEncoder encoder, String hash, String password) {
		return matchesOldHash(hash, password) || encoder.matches(password, hash);
	}
	/**
	 * Check if the password matches is one of the old outdated hashes
	 * 
	 * @param hash the hash used for comparison 
	 * @param password the unhashed password to compare to hash with the different hashing techniques
	 * 
	 * @return true if password matches one of the hashes
	 */
	public static boolean matchesOldHash(String hash, String password) {
		return unsaltedScramble(password).equals(hash) || oldScramble(password).equals(hash);
	}

	/**
	 * Remove the {@value PasswordUtil.HASH_UPDATE_REQUIRED} tag from the security element of the userXml
	 * and return the value of the security element (minus the tag)
	 * 
	 * @param userXml a database query for the user containing the security column
	 * @return the value of the security element (minus the {@value PasswordUtil.HASH_UPDATE_REQUIRED} tag)
	 */
	public static String removeSecurityTag(Element userXml) {
		String security = userXml.getChildTextNormalize(SECURITY_FIELD);
		StringBuilder newSec = new StringBuilder();
		for (String seg: security.split(",")) {
			if(newSec.length() > 0){
				newSec.append(',');
			}
			if(!seg.trim().equals(HASH_UPDATE_REQUIRED)){
				newSec.append(seg);
			}
		}
		userXml.getChild(SECURITY_FIELD).setText(newSec.toString());
		return newSec.toString();
	}
	/**
	 * SHA-1 Cryptographic hash algorithm
	 * See #191
	 * 
	 * @param text	password to digest
	 * @return	the hexadecimal encoded string
	 * @deprecated
	 */
	private static String unsaltedScramble(String text)
	{
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-1") ;
			md.update(text.getBytes("UTF-8"));
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
		}
		catch (UnsupportedEncodingException e) { return null; }
		catch (NoSuchAlgorithmException e)     { return null; }
	}
	/**
	 * Old Jeeves scramble method which lost leading 0
	 * during byte to hexadecimal string conversion.
	 * 
	 * @param text
	 * @return	the hexadecimal encoded string with missing leading 0
	 *  @deprecated
	 */
	private static String oldScramble(String text)
	{
		try
		{
			MessageDigest md = MessageDigest.getInstance("SHA-1") ;
	
			md.update(text.getBytes("UTF-8"));
	
			StringBuffer sb = new StringBuffer();
	
			for (byte b : md.digest())
				sb.append(Integer.toString(b & 0xFF, 16));	// #191 : here leading 0 are removed
	
			return sb.toString();
		}
		catch (UnsupportedEncodingException e) { return null; }
		catch (NoSuchAlgorithmException e)     { return null; }
	}
	/**
	 * Obtain the pasword encoder from the spring application context.
	 * @param servletContext 
	 * 
	 * @return the pasword encoder from the spring application context.
	 */
	public static PasswordEncoder encoder(ServletContext servletContext) {
		WebApplicationContext appcontext = WebApplicationContextUtils.getRequiredWebApplicationContext(servletContext);
		return (PasswordEncoder) appcontext.getBean(ENCODER_ID);
	}
	/**
	 * Updates database with new password if passwords match
	 *
	 * @param matchOldPassword if false and oldPassword is null then password 
	 * 						   will be updated without checking old password
	 * @param oldPassword the old password (obtained from user. not a hash)
	 * @param newPassword the new password
	 * @param iUserId the user id
	 * @param servletContext the servlet context, used to obtain the password encoder
	 * @return the xml from the database query containing the new password hash
	 * 
	 * @throws SQLException if an error occurred during a database access 
	 * @throws UserNotFoundEx  if the id does not reference a user
	 */
	public static Element updatePasswordWithNew(boolean matchOldPassword, String oldPassword,
			String newPassword, Integer iUserId, ServletContext servletContext, Dbms dbms) throws SQLException, UserNotFoundEx {

		PasswordEncoder encoder = encoder(servletContext);
		return updatePasswordWithNew(matchOldPassword, oldPassword, newPassword, iUserId, encoder, dbms);
	}
	/**
	 * Updates database with new password if passwords match
	 *
	 * @param matchOldPassword if false and oldPassword is null then password 
	 * 						   will be updated without checking old password
	 * @param oldPassword the old password (obtained from user. not a hash)
	 * @param newPassword the new password
	 * @param iUserId the user id
	 * @param encoder the Password encoder
	 * @return the xml from the database query containing the new password hash
	 * 
	 * @throws SQLException if an error occurred during a database access 
	 * @throws UserNotFoundEx  if the id does not reference a user
	 */
	public static Element updatePasswordWithNew(boolean matchOldPassword, String oldPassword,
			String newPassword, Integer iUserId, PasswordEncoder encoder, Dbms dbms) throws SQLException, UserNotFoundEx {
		String query = "SELECT * FROM Users WHERE id=?";
		Element elUser = dbms.select(query, iUserId);
		if (elUser.getChildren().size() == 0) {
			throw new UserNotFoundEx(""+iUserId);
		}
		elUser = elUser.getChild("record");
		String hash = elUser.getChildText(PASSWORD_COLUMN);
		if (hasOldHash(elUser)) {
			if ((matchOldPassword || oldPassword != null) && !matchesOldHash(hash , oldPassword)) {
				throw new IllegalArgumentException("Old password is not correct");
			}
		} else {
			if ((matchOldPassword || oldPassword != null) && !encoder.matches(oldPassword, hash)) {
				throw new IllegalArgumentException("Old password is not correct");
			}
		}
		
		String security = removeSecurityTag (elUser);
		
		String newPasswordHash = encoder.encode(newPassword);
		elUser.getChild(PASSWORD_COLUMN).setText(newPasswordHash);
		
		// all ok so change password
		dbms.execute("UPDATE Users SET password=?, security=? WHERE id=?", newPasswordHash, security, iUserId);
		return elUser;
	}
	public static String encode(ServiceContext context, String password) {
		return encoder(context.getServlet().getServletContext()).encode(password);
	}
}
