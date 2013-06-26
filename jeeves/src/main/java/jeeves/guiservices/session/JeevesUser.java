package jeeves.guiservices.session;

import jeeves.interfaces.Profile;

import org.springframework.security.core.userdetails.UserDetails;

public interface JeevesUser extends UserDetails {

	String getEmail();
	String getName();
	String getSurname();
	Profile getProfile();
	String getPassword();
	int getId();
}