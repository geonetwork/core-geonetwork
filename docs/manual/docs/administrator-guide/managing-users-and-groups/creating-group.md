# Creating group

The administrator can create new groups of users. User groups could correspond to logical units within an organisation, for example `Fisheries`, `Agriculture`, `Land`, `Water`, `Health` and so on.

To create new groups you should be logged on with an account that has administrative privileges.

1.  Select *Users and groups* from the *Admin console* drop down, then select *Manage groups*.

2.  Click *+New group*. You may want to remove the *Sample* group;

3.  Fill out the details. The email address will be used to send feedback on data downloads when they occur for resources that are part of the Group.

    !!! warning

        The Name should *NOT* contain spaces! You can use the Localization panel to provide localized names for groups.


4.  Click *Save*

## Access privileges

Access privileges can be set on a per-metadata-record basis. Privileges define which actions are available to users in the group:

- **Publish**: Controls visibility of the metadata.
- **Download**: Grants access to data downloads.
- **Interactive Map**: Provides access to map tools.
- **Featured**: Displays the record in the *Featured* section on the home page.

Additional settings:
- **Editing**: Specifies which groups can edit the metadata record.
- **Notify**: Determines which groups are notified when a file managed by GeoNetwork is downloaded.

## Group Type

Administrators can define the type of group being created. There are three group types available, each with specific characteristics and use cases:

### 1. Workspace Group

- **Description**: This is the default group type. It allows the group to own metadata records and have privileges assigned to them.
- **Use Case**: Suitable for groups that need full control over metadata records, including creation, import, and transfer.

### 2. Record Privilege Group

- **Description**: This group type can have privileges assigned to specific metadata records but cannot own metadata records.
- **Use Case**: Ideal for groups that require access to metadata records without ownership rights.

### 3. System Privilege Group

- **Description**: This group type is used to manage system-level privileges. It cannot own metadata records or have privileges assigned to them.
- **Use Case**: Designed for administrative purposes where system-level access is required without metadata ownership.
- **Profiles**: Users are not assigned profiles in System Privilege Groups, since their purpose is to grant system-level privileges to all members. Instead, users in these groups are automatically given the Registered User profile behind the scenes.

### Group Type and Operations Allowed Matrix

The table below summarizes the operations allowed for each group type:

| **Group Type**         | **Can Create Records in Group** | **Can Import Records in Group** | **Can Transfer Records to Group** | **Can Group Have Privileges on a Record** | **Group Has Profiles** |
|-------------------------|:------------------------------:|:-------------------------------:|:---------------------------------:|:-----------------------------------------:|:-----------------------:|
| **Workspace**           |               ✔               |               ✔                |                ✔                |                     ✔                     |           ✔           |
| **Record Privilege**    |                               |                                 |                                   |                     ✔                     |           ✔           |
| **System Privilege**    |                               |                                 |                                   |                                           |                       |

!!! Note

    - **Workspace Group**: Provides the most flexibility and is the default choice for most use cases.
    - **Record Privilege Group**: Focused on access control without ownership.
    - **System Privilege Group**: Reserved for system-level operations and does not interact with metadata records directly.

## Minimum user profile allowed to set privileges

This setting allows administrators to control the minimum user profile required to assign privileges for a group. It provides enhanced control over who can manage sensitive privileges for users within the group.

!!! Note

    This setting is unset and disabled when the group is a **System Privilege Group**. System privilege groups do not have profiles and cannot have privileges assigned to them.

### Default setting

By default, the **"Minimum User Profile Allowed to Set Privileges"** is set to **No Restrictions**. This means that any user with permission to manage privileges for a metadata record can assign privileges for users in this group.

### Restricted setting

When a specific profile is selected, only users with that profile or higher within the group can assign privileges. Users with lower profiles will have **read-only** access to privilege settings for this group.

### Example usage

If a group has **"Minimum User Profile Allowed to Set Privileges"** set to **Reviewer**:
- Only users with the **Reviewer** profile or higher (e.g., **Administrator**) can assign privileges for users in this group.
- Users with profiles below **Reviewer** (e.g., **Editor**) will see the group as **read-only** in the privileges interface.
