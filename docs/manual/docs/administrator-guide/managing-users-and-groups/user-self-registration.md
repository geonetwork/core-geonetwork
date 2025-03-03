# User Self-Registration {#user_self_registration}

!!! note
    This function requires an email server configured. See [System configuration](../configuring-the-catalog/system-configuration.md#system-config-feedback).


To enable the self-registration functions, see [System configuration](../configuring-the-catalog/system-configuration.md). When self-registration is enabled, for users that are not logged in, an additional link is shown on the login page:

![](img/selfregistration-start.png)

Click the `Create an account` button and fill out the registration form:

![](img/selfregistration-form.png)

The fields in this form are self-explanatory except for the following:

-   **Email**: The user's email address. This is mandatory and will be used as the username.
-   **Requested profile**: By default, self-registered users are given the `Registered User` profile (see previous section). If any other profile is selected:
    -   the user will still be given the `Registered User` profile
    -   an email will be sent to the Email address nominated in the Feedback section of the 'System Administration' menu, informing them of the request for a more privileged profile
-   **Requested group**: By default, self-registered users are not assigned to any group. If a group is selected:
    -   the user will still not be assigned to any group
    -   an email will be sent to the Email address nominated in the Feedback section of the 'System Administration' menu, informing them of the requested group.

## What happens when a user self-registers?

When a user self-registration occurs, the user receives an email with the new account details that looks something like the following:

    Dear User,

    Your registration at The Greenhouse GeoNetwork Site was successful.

    Your account is:
    username :    dubya.shrub@greenhouse.gov
    password :    0110O3
    usergroup:    GUEST
    usertype :    REGISTEREDUSER

    You've told us that you want to be "Editor", you will be contacted by our office soon.

    To log in and access your account, please click on the link below.
    http://greenhouse.gov/geonetwork

    Thanks for your registration.

    Yours sincerely,
    The team at The Greenhouse GeoNetwork Site

Notice that the user has requested an 'Editor' profile. As a result an email will be sent to the Email address nominated in the Feedback (see [Feedback](../configuring-the-catalog/system-configuration.md#system-config-feedback)) section of the `System Administration` menu which looks something like the following:

Notice also that the user has been added to the built-in user group 'GUEST'. This is a security restriction. An administrator/user-administrator can add the user to other groups if that is required later.

If you want to change the content of this email, you should modify `xslt/service/account/registration-pwd-email.xsl`.

    Dear Admin,

        Newly registered user dubya.shrub@greenhouse.gov has requested "Editor" access for:

        Instance:     The Greenhouse GeoNetwork Site
        Url:          http://greenhouse.gov/geonetwork

        User registration details:

        Name:         Dubya
        Surname:      Shrub
        Email:        dubya.shrub@greenhouse.gov
        Organisation: The Greenhouse
        Type:         gov
        Address:      146 Main Avenue, Creationville
        State:        Clerical
        Post Code:    92373
        Country:      Mythical

    Please action.

    The Greenhouse GeoNetwork Site

If you want to change the content of this email, you should modify `xslt/service/account/registration-prof-email.xsl`.
