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

var GeoNetworkAdminConsolePage = function() {
  this.service = 'admin.console';

  this.userInfoLink =
      element(by.css('a[href="admin.console#/organization/users/admin"]'));
  this.catalogInfoLink = element(by.css('a[href="home"]'));
  this.logoutButton =
      element(by.css('a[href="../../signout"]'));

  this.get = function() {
    browser.get(this.service);
  };
};

describe('GeoNetwork admin console page', function() {
  var adminPage;
  var signInPage;

  beforeEach(function() {
    adminPage = new GeoNetworkAdminConsolePage();
  });


  it('should be accessible to identified user',
     function() {
       adminPage.get();
       expect(browser.getCurrentUrl()).toContain(
       adminPage.service);
     });

  it('should provide user and catalog info link',
     function() {
       expect(adminPage.userInfoLink.getText())
        .toContain('admin admin (Administrator)');
       expect(adminPage.catalogInfoLink.getText())
        .toContain('My GeoNetwork catalogue');

       // TODO: check language selector
       // TODO: check empty catalog info and links
     });
});
