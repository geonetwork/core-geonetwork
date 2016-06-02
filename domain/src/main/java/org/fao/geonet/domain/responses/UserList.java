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
 * This class is a helper class for services that need to return user lists
 *
 * @author delawen
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
