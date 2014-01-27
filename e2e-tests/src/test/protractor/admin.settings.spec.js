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
