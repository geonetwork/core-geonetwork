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

var GeoNetworkadminSettingsPage = function() {
  this.service = 'admin.console';
  this.route = this.service + '#/settings';

  this.siteNameInput = element(by.name('system.site.name'));
  this.organisationInput = element(by.name('system.site.organization'));

  this.saveButton = element(by.css('button[id="gn-btn-settings-save"]'));

  // TODO: this is shared among all admin pages
  this.statusTitle = element(by.css('.gn-info > h4'));
  this.statusCloseButton = element(by.css('.gn-info > button'));
  //  this.catalogTitle = element(by.css('a[href="home"]'));

  this.get = function() {
    browser.get(this.route);
  };
};


describe('GeoNetwork settings administration page', function() {
  var adminSettingsPage;
  var defaultCatalogueName = 'My GeoNetwork catalogue';
  var catalogueName = 'E2E GeoNetwork catalogue';
  var defaultOrganisationName = 'My organization';

  beforeEach(function() {
    adminSettingsPage = new GeoNetworkadminSettingsPage();
  });

  it('should have the default value set for title and organisation',
      function() {
       adminSettingsPage.get();
       expect(adminSettingsPage.siteNameInput.getAttribute('value'))
      .toEqual(defaultCatalogueName);
       expect(adminSettingsPage.organisationInput.getAttribute('value'))
      .toEqual(defaultOrganisationName);
     });

  it('should properly save title', function() {
    adminSettingsPage.siteNameInput.clear();
    adminSettingsPage.siteNameInput.sendKeys(catalogueName);
    expect(adminSettingsPage.siteNameInput.getAttribute('value'))
      .toEqual(catalogueName);
    adminSettingsPage.saveButton.click();

    // Reload and check
    adminSettingsPage.get();
    expect(adminSettingsPage.siteNameInput.getAttribute('value'))
    .toEqual(catalogueName);

    // Reset
    adminSettingsPage.siteNameInput.clear();
    adminSettingsPage.siteNameInput.sendKeys(defaultCatalogueName);
    adminSettingsPage.saveButton.click();
  });
});
