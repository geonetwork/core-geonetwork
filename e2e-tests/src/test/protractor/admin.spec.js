var GeoNetworkAdminConsolePage = function() {
  this.service = 'admin.console';

  this.userInfoLink =
      element(by.css('a[href="admin.console#/organization/users/admin"]'));
  this.catalogInfoLink = element(by.css('a[href="home"]'));
  this.logoutButton =
      element(by.css('a[href="../../j_spring_security_logout"]'));

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
