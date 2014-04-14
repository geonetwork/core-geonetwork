var GeoNetworkSignInPage = function() {
  this.service = 'catalog.signin';

  this.usernameInput = element(by.model('signinUsername'));
  this.passwordInput = element(by.model('signinPassword'));
  this.redirectUrlInput = element(by.css('input[name="redirectUrl"]'));
  this.signinButton = element(by.css('button'));
  this.signinForm = element(by.name('gnSigninForm'));

  this.get = function(params) {
    browser.get(this.service + (params ? params : ''));
  };
  this.setUsername = function(name) {
    this.usernameInput.sendKeys(name);
  };
  this.setPassword = function(name) {
    this.passwordInput.sendKeys(name);
  };
  this.signIn = function() {
    this.signinForm.submit();
  };
  this.signInSilent = function(redirectTo) {
    this.get('?redirect=/srv/eng/' + redirectTo);
    this.setUsername('admin');
    this.setPassword('admin');
    this.signIn();

    // Could be faster but does not work TODO
    // browser.driver.get('http://admin:admin@
    //  localhost:8080/geonetwork/j_spring_security_check');
  };
  this.signOut = function() {
    return browser.driver
        .get('http://localhost:8080/geonetwork/j_spring_security_logout');
  };
};


describe('GeoNetwork sign in page', function() {
  var signInPage;
  var adminPageUrl = 'admin.console';

  beforeEach(function() {
    signInPage = new GeoNetworkSignInPage();
    signInPage.signOut().then(function() {
      signInPage.get();
    });
  });

  it('should fail if bad user information', function() {
    signInPage.setUsername('admin');
    signInPage.setPassword('dummyPassword');
    signInPage.signIn();

    expect(browser.getCurrentUrl()).toContain(
        signInPage.service + '?failure=true');
  });

  it('should disbale login button as far as username and password are not set',
     function() {
       expect(signInPage.signinButton.getAttribute('disabled')).toEqual('true');
       signInPage.setUsername('admin');
       expect(signInPage.signinButton.getAttribute('disabled')).toEqual('true');
       signInPage.setPassword('dummyPassword');
       expect(signInPage.signinButton.getAttribute('disabled')).toEqual(null);
     });

  it('should sign in if correct user information and access admin console',
     function() {
       signInPage.setUsername('admin');
       signInPage.setPassword('admin');

       try {
         // May redirect to a page containing no Angular
         // and trigger an exception.
         // browser.driver could help?
         signInPage.signIn();
       } catch (e) {
       }
       browser.get(adminPageUrl);
       expect(browser.getCurrentUrl()).toContain(adminPageUrl);
     });

  it('should redirect to sign in page if user not identified', function() {
    signInPage.setUsername('admin');
    browser.get(adminPageUrl);
    expect(browser.driver.getCurrentUrl()).toContain(signInPage.service);
  });

  it('should redirect to the URL set in redirect parameter', function() {
    signInPage.get('?redirect=/srv/eng/' + adminPageUrl);
    signInPage.setUsername('admin');
    signInPage.setPassword('admin');
    signInPage.signIn();
    expect(browser.getCurrentUrl()).toContain(adminPageUrl);
  });


  // TODO: Test new password
  // TODO: Test new account
});
