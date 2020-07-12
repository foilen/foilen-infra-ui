# Administrator

UserApi and UserHuman both have an "isAdmin" flag which give the users all the permissions on every aspects of the system.


# Roles

UserApi and UserHuman that are not administrators can be part of many roles.

A role contains permissions on different aspects of the system.


## Permissions - All and partials

- A "null" field means that you need another permission that matches that missing field (this is a partial permission)
- A "*" field means that you have the permissions on all the possible values (this is a full permission)

As an example, if we have resources:
- aaa owned by "alpha"
- bbb owned by "beta"
- machine1 owned by "shared"

Having the link permission:
- Resources owned by "*" can INSTALL on machines owned by "shared"

That means that the user can install aaa and bbb on the machine1. This is in effect providing full permissions on all the resources.

On the other hand, if the user is supposed to control only resources owned by "alpha" and be able to install them on any "shared" machine, then, we need to use a partial:
- Resources owned by "alpha" can INSTALL on machines owned by null
- Resources owned by null can INSTALL on machines owned by "shared"

With these 2 partials, the same user will have a matching permission of:
- Resources owned by "alpha" can INSTALL on machines owned by "shared"

With this concept of partial, we could have another role that:
- Resources owned by "beta" can INSTALL on machines owned by null
and a different user could have that permission and the "shared" one.


## Permissions on Resources

- Actions are: ALL / LIST/VIEW / ADD/UPDATE/DELETE
	- LIST: Means that when listing the resources, the user can see the resource type, name, description and tags (nothing else about the resource)
	- VIEW: Means that the user can view all the details of the resource (including sensitive properties)
- is explicit change: "true" means that the user can change the resource directly in any way he wants. When "false", it means that only a plugin can update the resource when the user is modifying another resource
	- Example: you do not want the users to update the "Bind (DNS) application" directly because he would just replace it with an application that does nothing or modify the DNS entries of other users, but you can allow him to update it indirectly when he creates a DnsEntry since the plugin will take care of managing the Bind application correctly.
- type: The type of Resource
- owner: The owner of the Resource

 
## Permissions on Links 

- Actions are:: ALL / ADD/DELETE
- is explicit change: "true" means that the user can change the link directly. When "false", it means that only a plugin can update the link when the user is modifying another resource
- fromType: The type of Resource
- fromOwner: The owner of the Resource
- linkType: The type of link
- toType: The type of Resource
- toOwner: The owner of the Resource
