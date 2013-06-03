.. _user_xml_services:

User services
=============

User Retrieval Services
-----------------------

List of Users (xml.info?type=users)
```````````````````````````````````

The **xml.info** service can be used to retrieve the users defined in GeoNetwork. See :ref:`xml.info`.

.. index:: xml.usergrpups.list

User groups list (xml.usergroups.list)
``````````````````````````````````````

The **xml.usergroups.list** service can be used
to retrieve the list of groups that a user belongs to.

Requires authentication: Yes

Request
^^^^^^^

Parameters:

- **id:** User identifier (multiple id elements can be specified)

User groups list request example::

  Url:
  http://localhost:8080/geonetwork/srv/eng/xml.usergroups.list

  Mime-type:
  application/xml

  Post request:
  <?xml version="1.0" encoding="UTF-8"?>
  <request>
    <id>3</id>
  <request>

Response
^^^^^^^^

If the request executes successfully then HTTP status code 200 is returned with an XML document containing the groups that the user belongs to. The elements in the response are:

- **group:** This is the container for each user group element returned
- **id**: Group identifier
- **name**: Group name
- **description**: Group description

User groups list response example::

  <groups>
    <group>
      <id>3</id>
      <name>RWS</name>
      <description />
    </group>
  </groups>

If the request fails then an HTTP 500 status code error is returned
and the response contains an XML document with the details of the exception/what
went wrong. An example error response is:::
 
 <error id="missing-parameter">
   <message>id</message>
   <class>MissingParameterEx</class>
   .....
 </error>

See :ref:`exception_handling` for more details.

Errors
^^^^^^

- **Service not allowed (error id: service-not-allowed)**, when the user is not authenticated or their profile has no rights to execute the service. Returns 500 HTTP code

- **User XXXX doesn't exist**, if a user with provided **id** value does not exist. Returns 500 HTTP code

.. index:: xml.user.get

User information (xml.user.get)
```````````````````````````````

The **xml.user.get** service returns information on a specified user.

Requires authentication: Yes

Request
^^^^^^^

Parameters:

- **id**: Identifier of user to retrieve

User get request example::

  Url:
  http://localhost:8080/geonetwork/srv/eng/xml.user.get

  Mime-type:
  application/xml

  Post request:
  <request>
      <id>2</id>
  </request>

Response
^^^^^^^^

If the request executed succesfully then an HTTP 200 status code is
returned and an XML document containing the user information (including the groups they belong to) is returned. An example response is:::
 
 <response>
   <record>
     <id>2</id>
     <username>bullshot</username>
     <password>112c535b861a904569285c941277d0c642eea4bb</password>
     <surname>Shot</surname>
     <name>Bull</name>
     <profile>RegisteredUser</profile>
     <address>41 Shot Street</address>
     <city>Kunnanurra</city>
     <state>Western Australia</state>
     <zip>8988</zip>
     <country>Australia</country>
     <email>gan@gan.com</email>
     <organisation>B7</organisation>
     <kind>gov</kind>
   </record>
   <groups>
     <id>2</id>
   </groups>
 </response>

If the request fails then an HTTP 500 status code error is returned
and the response contains an XML document with the details of the exception/what
went wrong. An example error response is:::
 
 <error id="missing-parameter">
   <message>id</message>
   <class>MissingParameterEx</class>
   .....
 </error>

See :ref:`exception_handling` for more details.

Errors
^^^^^^

- **Service not allowed (error id: service-not-allowed)**, when the
  user is not authenticated or their profile has no rights to
  execute the service. Returns 500 HTTP code

- **Missing parameter (error id: missing-parameter)**, when mandatory parameters
  are not provided. Returns 500 HTTP code

- **bad-parameter id**, when **id** parameter is
  empty/invalid. Returns 500 HTTP code

User Maintenance Services
-------------------------

.. index:: xml.user.update

Create a user (xml.user.update)
```````````````````````````````

The **xml.user.update** service can be used to
create new users, update user information and reset user password,
depending on the value of the **operation**
parameter. Only users with profiles **Administrator**
or **UserAdmin** can create new users.

Users with profile **Administrator** can create
users in any group, while users with profile
**UserAdmin** can create users only in the groups
to which they belong.

Requires authentication: Yes

Request
^^^^^^^

Parameters:

- **operation**: (mandatory) **newuser**
- **username**: (mandatory) User login name
- **password**: (mandatory) User password
- **profile**: (mandatory) User profile
- **surname**:User surname
- **name**: User name
- **address**: User physical address
- **city**: User address city
- **state**: User address state
- **zip**: User address zip
- **country**: User address country
- **email**: User email
- **org**: User organisation/departament
- **kind**: Kind of organisation
- **groups**: Group identifier to set for the user, can be multiple **groups** elements
- **groupid**: Group identifier

User create request example::

  Url:
  http://localhost:8080/geonetwork/srv/eng/xml.user.update

  Mime-type:
  application/xml

  Post request:
  <request>
    <operation>newuser</operation>
    <username>samantha</username>
    <password>editor2</password>
    <profile>Editor</profile>
    <name>Samantha</name>
    <city>Amsterdam</city>
    <country>Netherlands</country>
    <email>samantha@mail.net</email>
    <groups>2</groups>
    <groups>4</groups>
  </request>

Response
^^^^^^^^

If the request executed successfully then HTTP 200 status code is
returned with an XML document containing an empty response element.

If the request fails, then an HTTP 500 status code error is returned
with an XML document describing the exception/what went wrong. An example of such a response is:::
 
 <error id="error">
   <message>User with username samantha already exists</message>
   <class>IllegalArgumentException</class>
   <stack>...</stack>
   .....
 </error>

See :ref:`exception_handling` for more details.

Errors
^^^^^^

- **Service not allowed (error id: service-not-allowed)**, when the
  user is not authenticated or their profile has no rights to
  execute the service. Returns 500 HTTP code

- **Missing parameter (error id: missing-parameter)**, when mandatory parameters
  are not provided. Returns 500 HTTP code

- **bad-parameter**, when a mandatory fields is empty. Returns 500 HTTP code

- **User with username XXXX already exists (error id: error)**, when a user 
  with that username is already present. Returns 500 HTTP code

- **Unknown profile XXXX (error id: error)**, when the profile is
  not valid. Returns 500 HTTP code

- **ERROR: duplicate key violates unique constraint
  "users_username_key"**, when trying to create a new user using an existing
  username. Returns 500 HTTP code

- **ERROR: insert or update on table "usergroups" violates
  foreign key constraint "usergroups_groupid_fkey"**, when group
  identifier is not an existing group identifier. Returns 500 HTTP code

- **ERROR: tried to add group id XX to user XXXX - not
  allowed because you are not a member of that group**, when the
  authenticated user has profile **UserAdmin** and tries to add the
  user to a group they do not manage. Returns 500 HTTP code

- **ERROR: you don't have rights to do this**, when the
  authenticated user has a profile that is not **Administrator** or
  **UserAdmin**. Returns 500 HTTP code

Update user information (xml.user.update)
`````````````````````````````````````````

The **xml.user.update** service can be used to
create new users, update user information and reset user password,
depending on the value of the **operation**
parameter. Only users with profiles **Administrator**
or **UserAdmin** can update users information.

Users with profile **Administrator** can update
any user, while users with profile **UserAdmin** can
update users only in the groups where they belong.

Requires authentication: Yes

Request
^^^^^^^

Parameters:

- **operation**: (mandatory) **editinfo**
- **id**: (mandatory) Identifier of the user to update
- **username**: (mandatory) User login name
- **password**: (mandatory) User password
- **profile**: (mandatory) User profile
- **surname**: User surname
- **name**: User name
- **address**: User physical address
- **city**: User address city
- **state**: User address state
- **zip**: User address zip
- **country**: User address country
- **email**: User email
- **org**: User organisation/departament
- **kind**: Kind of organisation
- **groups**: Group identifier to set for the user, can be multiple **groups** elements
- **groupid**: Group identifier

.. note:: If an optional parameter is not provided, the value is updated in the database with an empty string.

Update user information request example::

  Url:
  http://localhost:8080/geonetwork/srv/eng/xml.user.update

  Mime-type:
  application/xml

  Post request:
  <?xml version="1.0" encoding="UTF-8"?>
  <request>
    <operation>editinfo</operation>
    <id>5</id>
    <username>samantha</username>
    <password>editor2</password>
    <profile>Editor</profile>
    <name>Samantha</name>
    <city>Rotterdam</city>
    <country>Netherlands</country>
    <email>samantha@mail.net</email>
  </request>

Response
^^^^^^^^

If the request executed successfully then HTTP 200 status code is
returned with an XML document containing an empty response element.

If the request fails, then an HTTP 500 status code error is returned
with an XML document describing the exception/what went wrong. An example of such a response is:::
 
 <error id="missing-parameter">
   <message>username</message>
   <class>MissingParameterEx</class>
   <stack>...</stack>
   .....
 </error>

See :ref:`exception_handling` for more details.


Errors
^^^^^^

- **Service not allowed (error id: service-not-allowed)**, when the
  user is not authenticated or their profile has no rights to
  execute the service. Returns 500 HTTP code

- **Missing parameter (error id: missing-parameter)**, when mandatory parameters
  are not provided. Returns 500 HTTP code

- **bad-parameter**, when a mandatory field is empty.
  Returns 500 HTTP code

- **Unknown profile XXXX (error id: error)**, when the profile is
  not valid. Returns 500 HTTP code

- **ERROR: duplicate key violates unique constraint
  "users_username_key"**, when trying to create a new user using an existing
  username. Returns 500 HTTP code

- **ERROR: insert or update on table "usergroups" violates
  foreign key constraint "usergroups_groupid_fkey"**, when the group
  identifier is not an existing group identifier. Returns 500
  HTTP code

- **ERROR: tried to add group id XX to user XXXX - not
  allowed because you are not a member of that group**, when the
  authenticated user has profile **UserAdmin** and tries to add the
  user to a group in which they do not manage. Returns 500 HTTP code

- **ERROR: you don't have rights to do this**, when the authenticated user has 
  a profile that is not **Administrator** or **UserAdmin**. Returns 500 HTTP code

Reset user password (xml.user.update)
`````````````````````````````````````

The **xml.user.update** service can be used to
create new users, update user information and reset user password,
depending on the value of the **operation**
parameter. Only users with profiles **Administrator**
or **UserAdmin** can reset users password.

Users with profile **Administrator** can reset
the password for any user, while users with profile
**UserAdmin** can reset the password for users only
in the groups where they belong.

Requires authentication: Yes

Request
^^^^^^^

Parameters:

- **operation**: (mandatory) **resetpw**
- **id**: (mandatory) Identifier of the user to reset the password
- **username**: (mandatory) User login name
- **password**: (mandatory) User new password
- **profile**: (mandatory) User profile

Reset user password request example::

  Url:
  http://localhost:8080/geonetwork/srv/eng/xml.user.update

  Mime-type:
  application/xml

  Post request:
  <?xml version="1.0" encoding="UTF-8"?>
  <request>
    <operation>resetpw</operation>
    <id>2</id>
    <username>editor</username>
    <password>newpassword</password>
    <profile>Editor</profile>
  </request>

Response
^^^^^^^^

If the request executed successfully then HTTP 200 status code is
returned with an XML document containing an empty response element.

If the request fails, then an HTTP 500 status code error is returned
with an XML document describing the exception/what went wrong. An example of such a response is:::
 
 <error id="missing-parameter">
   <message>username</message>
   <class>MissingParameterEx</class>
   <stack>...</stack>
   .....
 </error>

See :ref:`exception_handling` for more details.


Errors
^^^^^^

- **Service not allowed (error id: service-not-allowed)**, when the
  user is not authenticated or their profile has no rights to
  execute the service. Returns 500 HTTP code

- **Missing parameter (error id: missing-parameter)**, when mandatory parameters
  are not provided. Returns 500 HTTP code

- **bad-parameter**, when a mandatory field is empty.
  Returns 500 HTTP code

- **Unknown profile XXXX (error id: error)**, when the profile is
  not valid. Returns 500 HTTP code

- **ERROR: you don't have rights to do this**, when the authenticated user is not
  an **Administrator** or **UserAdmin**. Returns 500 HTTP code

.. index:: xml.user.infoupdate

Update current authenticated user information (xml.user.infoupdate)
```````````````````````````````````````````````````````````````````

The **xml.user.infoupdate** service can be used to update the information related to the current authenticated user.

Requires authentication: Yes

Request
^^^^^^^

Parameters:

- **surname**: (mandatory) User surname
- **name**: (mandatory) User name
- **address**: User physical address
- **city**: User address city
- **state**: User address state
- **zip**: User address zip
- **country**: User address country
- **email**: User email
- **org**: User organisation/departament
- **kind**: Kind of organisation

.. note:: If an optional parameter is not provided the value is updated in the database with an empty string.

Current user info update request example::

  Url:
  http://localhost:8080/geonetwork/srv/eng/xml.user.infoupdate

  Mime-type:
  application/xml

  Post request:
  <request>
    <name>admin</name>
    <surname>admin</surname>
    <address>address</address>
    <city>Amsterdam</city>
    <zip>55555</zip>
    <country>Netherlands</country>
    <email>user@mail.net</email>
    <org>GeoCat</org>
    <kind>gov</kind>
  </request>

Response
^^^^^^^^

If the request executed successfully then HTTP 200 status code is
returned with an XML document containing an empty response element.

If the request fails, then an HTTP 500 status code error is returned
with an XML document describing the exception/what went wrong. An example of such a response is:::
 
 <error id="missing-parameter">
   <message>surname</message>
   <class>MissingParameterEx</class>
   <stack>...</stack>
   .....
 </error>

See :ref:`exception_handling` for more details.


Errors
^^^^^^

- **Service not allowed (error id: service-not-allowed)**, when the
  user is not authenticated. Returns 500 HTTP code

- **Missing parameter (error id: missing-parameter)**, when mandatory parameters
  are not provided. Returns 500 HTTP code


.. index:: xml.user.pwupdate

Change current authenticated user password (xml.user.pwupdate)
``````````````````````````````````````````````````````````````

The **xml.user.pwupdate** service can be used to
change the password of the current authenticated user.

Requires authentication: Yes

Request
^^^^^^^

Parameters:

- **password**: Actual user password

- **newPassword**: New password to set for the user

Example::

  <request>
      <password>admin</password>
      <newPassword>admin2</newPassword>
  </request>

Response
^^^^^^^^

If the request executed successfully then HTTP 200 status code is
returned with an XML document containing an empty response element.

If the request fails, then an HTTP 500 status code error is returned
with an XML document describing the exception/what went wrong. An example of such a response is:::
 
 <error id="error">
   <message>Old password is not correct</message>
   <class>IllegalArgumentException</class>
   <stack>...</stack>
   .....
 </error>

See :ref:`exception_handling` for more details.

Errors
^^^^^^

- **Service not allowed (error id: service-not-allowed)**, when the
  user is not authenticated. Returns 500 HTTP code

- **Old password is not correct**. Returns 500 HTTP code

- **Bad parameter (newPassword)**, when an empty password is
  provided. Returns 500 HTTP code

.. index:: xml.user.remove

Remove a user (xml.user.remove)
```````````````````````````````

The **xml.user.remove** service can be used to
remove an existing user. Only users with profiles
**Administrator** or **UserAdmin**
can delete users.

Users with profile **Administrator** can delete
any user (except themselves), while users with profile
**UserAdmin** can delete users only in the groups
where they belong (except themselves).

Requires authentification: Yes

Request
^^^^^^^

Parameters:

- **id**: (mandatory) Identifier of user to delete

User remove request example::

  Url:
  http://localhost:8080/geonetwork/srv/eng/xml.user.remove

  Mime-type:
  application/xml

  Post request:
  <request>
      <id>2</id>
  </request>

Response
^^^^^^^^

If the request executed successfully then HTTP 200 status code is
returned with an XML document containing an empty response element.

If the request fails, then an HTTP 500 status code error is returned
with an XML document describing the exception/what went wrong. An example of such a response is:::
 
 <error id="error">
   <message>You cannot delete yourself from the user database</message>
   <class>IllegalArgumentException</class>
   <stack>...</stack>
   .....
 </error>

See :ref:`exception_handling` for more details.


Errors
^^^^^^

- **Service not allowed (error id: service-not-allowed)**, when the
  user is not authenticated or their profile has no rights to
  execute the service. Returns 500 HTTP code

- **Missing parameter (error id: missing-parameter)**, when the
  **id** parameter is not provided. Returns 500 HTTP code

- **You cannot delete yourself from the user database (error
  id: error)**. Returns 500 HTTP code

- **You don't have rights to delete this user (error id:
  error)**, when authenticated user is not 
  an **Administrator** or **User administrator**. Returns 500 HTTP code

- **You don't have rights to delete this user because the
  user is not part of your group (error id: error)**, when trying to
  delete a user that is not in the same group as the
  authenticated user and the authenticated user is a 
  **User administrator**. Returns 500 HTTP code


