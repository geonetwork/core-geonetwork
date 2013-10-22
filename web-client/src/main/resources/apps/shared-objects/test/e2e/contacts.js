'use strict';

/* http://docs.angularjs.org/guide/dev_guide.e2e-testing */

describe('Contacts', function () {


    describe('Default Route', function () {
        beforeEach(function () {
            browser().navigateTo(appDir + 'index-test.html');
        });
        it('should automatically redirect to /nonvalidated/contacts when location hash/fragment is empty', function () {
            expect(browser().location().url()).toBe("/nonvalidated/contacts");
        });
    });

    describe('#/nonvalidated/contacts', function () {
        beforeEach(function () {
            browser().navigateTo('#/nonvalidated/contacts');
        });

        checkBreadCrumbs('Non-validated', 'Contacts');
        hasResultsDescription('Non-Validated Contact');
//        hasResultsDescription('Non-Validated Contact 2');

        hasActionsButton(true);
        hasEditAction(true);
        hasValidateAction(true);
        hasRejectNonValidAction(true);
        hasRejectValidAction(false);
        hasDeleteAction(false);
        listsRelatedMetadata("100", "Non-validated contacts Metadata", "nvowner", "nvemail");
        menuBecomesVisible('#breadCrumb1');
        menuBecomesVisible('#breadCrumb2');
        menuBecomesVisible('#menu-bar-action');

        var numResults = function() {repeater('div.data-description').count()}
        it ('there should be two results shown before filtering', function () {
            expect(repeater('div.data-description').count()).toBe(2);
        })
        it ("filtering restricts elements", function () {
            input('search.search').enter('1');
            expect(repeater('div.data-description').count()).toBe(1);
            input('search.search').enter('xcxdfs');
            expect(repeater('div.data-description').count()).toBe(0);
            input('search.search').enter('');
            expect(repeater('div.data-description').count()).toBe(2);
        });
    });

    describe('#/validated/contacts', function () {
        beforeEach(function () {
            browser().navigateTo('#/validated/contacts');
        });

        checkBreadCrumbs('Validated', 'Contacts');
        hasResultsDescription('Validated Contact')

        hasActionsButton(true);
        hasEditAction(true);
        hasValidateAction(false);
        hasRejectNonValidAction(false);
        hasRejectValidAction(true);
        hasDeleteAction(false);
        listsRelatedMetadata("300", "Validated contacts Metadata", "owner", "email");
        menuBecomesVisible('#breadCrumb1');
        menuBecomesVisible('#breadCrumb2');
        menuBecomesVisible('#menu-bar-action');

    });
});
