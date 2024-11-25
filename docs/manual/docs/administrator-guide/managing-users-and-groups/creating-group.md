# Creating group

The administrator can create new groups of users. User groups could correspond to logical units within an organisation, for example `Fisheries`, `Agriculture`, `Land`, `Water`, `Health` and so on.

To create new groups you should be logged on with an account that has administrative privileges.

1.  Select the *Administration* button in the menu. On the Administration page, select *Group management*.

2.  Select *Add a new group*. You may want to remove the *Sample* group;

3.  Fill out the details. The email address will be used to send feedback on data downloads when they occur for resources that are part of the Group.

    !!! warning

        The Name should *NOT* contain spaces! You can use the Localization panel to provide localized names for groups.


4.  Click *Save*

## Access Privileges

Access privileges can be set on a per-metadata-record basis. Privileges define which actions are available to users in the group:

- **Publish**: Controls visibility of the metadata.
- **Download**: Grants access to data downloads.
- **Interactive Map**: Provides access to map tools.
- **Featured**: Displays the record in the *Featured* section on the home page.

Additional settings:
- **Editing**: Specifies which groups can edit the metadata record.
- **Notify**: Determines which groups are notified when a file managed by GeoNetwork is downloaded.

## Minimum User Profile Allowed to Set Privileges

This setting allows administrators to control the minimum user profile required to assign privileges for a group. It provides enhanced control over who can manage sensitive privileges for users within the group.

### Default Setting

By default, the **"Minimum User Profile Allowed to Set Privileges"** is set to **No Restrictions**. This means that any user with permission to manage privileges for a metadata record can assign privileges for users in this group.

### Restricted Setting

When a specific profile is selected, only users with that profile or higher within the group can assign privileges. Users with lower profiles will have **read-only** access to privilege settings for this group.

### Example Usage

If a group has **"Minimum User Profile Allowed to Set Privileges"** set to **Reviewer**:
- Only users with the **Reviewer** profile or higher (e.g., **Administrator**) can assign privileges for users in this group.
- Users with profiles below **Reviewer** (e.g., **Editor**) will see the group as **read-only** in the privileges interface.
