package org.fao.geonet.domain.responses;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.namespace.QName;

import org.fao.geonet.domain.User;

/**
 * 
 * This class is a helper class for services that need to return user lists
 * 
 * @author delawen
 * 
 */
@XmlRootElement(name = "response")
@XmlSeeAlso(User.class)
public class UserList implements Serializable {
	private static final long serialVersionUID = 7396181507081505598L;
	private List<JAXBElement<? extends User>> users;

	@XmlAnyElement(lax = true)
	public List<JAXBElement<? extends User>> getUsers() {
		if (this.users == null) {
			this.users = new LinkedList<JAXBElement<? extends User>>();
		}
		return this.users;
	}

	public void setUsers(List<JAXBElement<? extends User>> users) {
		this.users = users;
	}

	public void addUser(User user) {
		this.getUsers().add(
				new JAXBElement<User>(new QName("record"), User.class, user));
	}
}
