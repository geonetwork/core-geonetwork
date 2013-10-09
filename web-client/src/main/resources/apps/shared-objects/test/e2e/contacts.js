'use strict';

/* http://docs.angularjs.org/guide/dev_guide.e2e-testing */

describe('contacts', function () {


    describe('Default Route', function () {
        beforeEach(function () {
            browser().navigateTo(appDir + 'index-test.html');
        });
        it('should automatically redirect to /nonvalidated/contacts when location hash/fragment is empty', function () {
            expect(browser().location().url()).toBe("/nonvalidated/contacts");
        });
    });

    describe('/nonvalidated/contacts', function () {
        beforeEach(function () {
            browser().navigateTo('#/nonvalidated/contacts');
        });

        checkBreadCrumbs('Non-validated', 'Contacts');
        hasResultsDescription('Non-Validated Contact')
    });


    describe('/validated/contacts', function () {

        beforeEach(function () {
            browser().navigateTo('#/validated/contacts');
        });

        checkBreadCrumbs('Validated', 'Contacts');

        hasResultsDescription('Validated Contact')
    });
});
