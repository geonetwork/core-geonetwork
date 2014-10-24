var GeoNetworkadminUserPage = function() {
  this.service = 'admin.console';
  this.route = this.service + '#/organization/users';
  this.users = {
    admin: {
      username: 'admin',
      displayName: 'admin admin (Administrator)',
      index: 0
    },
    registeredUser: {
      id: '',
      username: 'clotilde',
      password: 'aaaaaa',
      name: 'Clotilde',
      surname: 'a registered user',
      displayName: 'Clotilde a registered user (Registered user)',
      profile: 'RegisteredUser',
      address: '313 chemin du four',
      city: 'SAINT JEOIRE PRIEURÉ',
      state: 'SAVOIE',
      zip: '73190',
      country: 'FRANCE',
      email: 'clotilde@geonetwork.org',
      org: 'GeoNetwork community',
      index: 1
    }
  };

  this.userUsernameInput = element(by.model('userSelected.username'));
  this.userNameInput = element(by.model('userSelected.name'));
  this.userSurnameInput = element(by.model('userSelected.surname'));
  this.userPasswordInput = element(by.model('userSelected.password'));
  this.userPasswordCheckInput = element(by.model('passwordCheck'));
  this.userEmailInput = element(by.model('userSelected.email'));
  this.userAddressInput = element(by
      .model('userSelected.primaryaddress.address'));
  this.userCityInput = element(by.model('userSelected.primaryaddress.city'));
  this.userStateInput = element(by.model('userSelected.primaryaddress.state'));
  this.userZipInput = element(by.model('userSelected.primaryaddress.zip'));
  this.userCountryInput = element(by
      .model('userSelected.primaryaddress.country'));
  this.userOrgInput = element(by.model('userSelected.organisation'));
  this.userIdInput = element(by.model('userSelected.id'));

  this.roleRegisteredUserGroupsList = element(by.css('#groups_RegisteredUser'));
  this.roleEditorGroupsList = element(by.css('#groups_Editor'));
  this.roleReviewerGroupsList = element(by.css('#groups_Reviewer'));
  this.roleUserAdminGroupsList = element(by.css('#groups_UserAdmin'));

  this.newUserButton = element(by.css('button[id="gn-btn-user-add"]'));
  this.saveUserButton = element(by.css('button[id="gn-btn-user-save"]'));
  this.deleteUserButton = element(by.css('button[id="gn-btn-user-delete"]'));

  // TODO: this is shared among all admin pages
  this.statusTitle = element(by.css('.gn-info > h4'));
  this.statusCloseButton = element(by.css('.gn-info > button'));

  this.get = function() {
    browser.get(this.route);
  };

  this.setUser = function(user, noPassword) {
    this.userUsernameInput.sendKeys(user.username);

    if (!noPassword) {
      this.setPassword(user);
    }

    this.userNameInput.sendKeys(user.name);
    this.userSurnameInput.sendKeys(user.surname);
    this.userEmailInput.sendKeys(user.email);
    this.userAddressInput.sendKeys(user.address);
    this.userCityInput.sendKeys(user.city);
    this.userStateInput.sendKeys(user.state);
    this.userZipInput.sendKeys(user.zip);
    this.userCountryInput.sendKeys(user.country);
    this.userOrgInput.sendKeys(user.org);
  };
  this.setPassword = function(user) {
    this.userPasswordInput.sendKeys(user.password);
    this.userPasswordCheckInput.sendKeys(user.password);
  };
  this.setProfile = function(profile) {
  };
};

describe('GeoNetwork user administration page', function() {
  var adminUserPage;

  beforeEach(function() {
    adminUserPage = new GeoNetworkadminUserPage();
  });

  it('should list one user', function() {
    adminUserPage.get();
    var userRepeaters = browser.findElements(by.repeater('u in users'));
    userRepeaters.then(function(arr) {
      expect(arr.length).toEqual(1);
      expect(arr[adminUserPage.users.admin.index].getText())
      .toEqual(adminUserPage.users.admin.displayName);
    });
  });

  it('should not allow to create a user without password', function() {
    adminUserPage.newUserButton.click();
    adminUserPage.setUser(adminUserPage.users.registeredUser, true);
    expect(adminUserPage.saveUserButton.isEnabled()).toEqual(false);
  });

  it('should add a new empty user when add button is clicked', function() {
    adminUserPage.newUserButton.click();
    adminUserPage.setUser(adminUserPage.users.registeredUser);
    adminUserPage.saveUserButton.click();
    // Not sure broadcasting could be catched ? TODO
    // expect(adminUserPage.statusTitle.getText()).toEqual("User updated");

    var userRepeaters = browser.findElements(by.repeater('u in users'));
    userRepeaters
        .then(function(users) {
          expect(users.length).toEqual(2);
          expect(users[adminUserPage.users.registeredUser.index].getText())
          .toEqual(
              adminUserPage.users.registeredUser.displayName);
          expect(users[adminUserPage.users.admin.index].getText()).toEqual(
              adminUserPage.users.admin.displayName);
        });
  });

  it('should display user information when selected', function() {
    adminUserPage.get();
    var userRepeaters = browser.findElements(by.repeater('u in users'));
    userRepeaters.then(function(arr) {
      arr[adminUserPage.users.registeredUser.index].click().then(
          function() {
            expect(adminUserPage.userNameInput.getAttribute('value')).toEqual(
                adminUserPage.users.registeredUser.name);
            expect(adminUserPage.userSurnameInput.getAttribute('value'))
                .toEqual(adminUserPage.users.registeredUser.surname);
            expect(adminUserPage.userEmailInput.getAttribute('value')).toEqual(
                adminUserPage.users.registeredUser.email);
            expect(adminUserPage.userOrgInput.getAttribute('value')).toEqual(
                adminUserPage.users.registeredUser.org);
            expect(adminUserPage.userAddressInput.getAttribute('value'))
                .toEqual(adminUserPage.users.registeredUser.address);
            expect(adminUserPage.userZipInput.getAttribute('value')).toEqual(
                adminUserPage.users.registeredUser.zip);
            expect(adminUserPage.userStateInput.getAttribute('value')).toEqual(
                adminUserPage.users.registeredUser.state);
            expect(adminUserPage.userCityInput.getAttribute('value')).toEqual(
                adminUserPage.users.registeredUser.city);
            expect(adminUserPage.userCountryInput.getAttribute('value'))
                .toEqual(adminUserPage.users.registeredUser.country);
          });
    });
  });


  it('should display list of groups in all profil lists', function() {
    var userRepeaters = browser.findElements(by.repeater('u in users'));
    userRepeaters.then(function(arr) {
      arr[adminUserPage.users.registeredUser.index].click().then(
          function() {
            expect(adminUserPage.roleRegisteredUserGroupsList.getText())
            .toEqual(
                'Sample group');
            expect(adminUserPage.roleEditorGroupsList.getText()).toEqual(
                'Sample group');
            expect(adminUserPage.roleReviewerGroupsList.getText()).toEqual(
                'Sample group');
            expect(adminUserPage.roleUserAdminGroupsList.getText()).toEqual(
                'Sample group');
          });
    });
  });

  it('should not allow to create another user with same name', function() {
    adminUserPage.get();
    adminUserPage.newUserButton.click();
    adminUserPage.setUser(adminUserPage.users.registeredUser);
    adminUserPage.saveUserButton.click();
    expect(adminUserPage.statusTitle.getText())
    .toEqual("User can't be created or updated");
    adminUserPage.statusCloseButton.click();
  });


  it('should delete a user when delete button is clicked', function() {
    var userRepeaters = browser.findElements(by.repeater('u in users'));
    userRepeaters.then(function(users) {
      expect(users.length).toEqual(2);
      expect(users[adminUserPage.users.registeredUser.index].getText()).toEqual(
          adminUserPage.users.registeredUser.displayName);

      users[adminUserPage.users.registeredUser.index].click().then(
          function() {
            adminUserPage.deleteUserButton.click().then(
                function() {
                  var updateduserRepeaters = browser.findElements(by
                      .repeater('u in users'));
                  updateduserRepeaters.then(function(arr) {
                    expect(arr.length).toEqual(1);
                  });
                });
          });
    });
  });

});
