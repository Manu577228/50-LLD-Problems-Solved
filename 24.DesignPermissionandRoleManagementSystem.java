/* -------------------------------------------------------- */
/*   ( The Authentic JS/JAVA CodeBuff )
 ___ _                      _              _ 
 | _ ) |_  __ _ _ _ __ _ __| |_ __ ____ _ (_)
 | _ \ ' \/ _` | '_/ _` / _` \ V  V / _` || |
 |___/_||_\__,_|_| \__,_\__,_|\_/\_/\__,_|/ |
                                        |__/ 
 */
/* ---------------------------------------------------------   */
/*    Youtube: https://youtube.com/@code-with-Bharadwaj        */
/*    Github :  https://github.com/Manu577228                  */
/* ----------------------------------------------------------- */


/*
------------------------------------------------------------
        Low-Level Design: Permission / Role Management
------------------------------------------------------------

1) a) Functional Requirements:
   - Add users, roles, and permissions.
   - Assign roles to users.
   - Assign permissions to roles.
   - Check if a user has a specific permission.
   - Display all roles and permissions assigned to a user.

   b) Non-Functional Requirements:
   - Must run locally (no external DB).
   - Modular and readable structure.
   - O(1) average permission lookup.
   - Lightweight and concurrency-safe (thread-safe collections if scaled).

------------------------------------------------------------
2) Algorithm Choice Discussion:
   - We use HashMap and HashSet for O(1) average access time.
   - Mappings:
        user → set(roles)
        role → set(permissions)
   - Lookup is efficient using HashSet operations.

------------------------------------------------------------
3) Concurrency and Data Model Discussion:
   - Data Model: In-memory using HashMap.
   - Thread safety: Can use ConcurrentHashMap if multi-threaded.
   - For this demo: single-threaded simulation.

------------------------------------------------------------
4) UML Diagram:
------------------------------------------------------------
            ┌─────────────┐
            │  Permission │
            └──────┬──────┘
                   │
                   ▼
            ┌─────────────┐
            │    Role     │
            │  permissions│
            └──────┬──────┘
                   │
                   ▼
            ┌─────────────┐
            │    User     │
            │    roles    │
            └─────────────┘
------------------------------------------------------------
*/

import java.util.*;
import java.io.*;

public class PermissionSystem {

    // Mapping: Role -> Set of Permissions
    private final Map<String, Set<String>> rolePermissions;

    // Mapping: User -> Set of Roles
    private final Map<String, Set<String>> userRoles;

    public PermissionSystem() {
        this.rolePermissions = new HashMap<>();
        this.userRoles = new HashMap<>();
    }

    // Add a new role
    public void addRole(String roleName) {
        rolePermissions.putIfAbsent(roleName, new HashSet<>());
    }

    // Add permission to a role
    public void addPermissionToRole(String roleName, String permission) {
        rolePermissions.putIfAbsent(roleName, new HashSet<>());
        rolePermissions.get(roleName).add(permission);
    }

    // Add a new user
    public void addUser(String username) {
        userRoles.putIfAbsent(username, new HashSet<>());
    }

    // Assign role to user
    public void assignRoleToUser(String username, String roleName) {
        userRoles.putIfAbsent(username, new HashSet<>());
        userRoles.get(username).add(roleName);
    }

    // Check if user has a specific permission
    public boolean checkUserPermission(String username, String permission) {
        Set<String> roles = userRoles.getOrDefault(username, Collections.emptySet());
        for (String role : roles) {
            Set<String> perms = rolePermissions.getOrDefault(role, Collections.emptySet());
            if (perms.contains(permission)) {
                return true;
            }
        }
        return false;
    }

    // Display all roles and permissions of a user
    public String displayUserAccess(String username) {
        if (!userRoles.containsKey(username)) {
            return "User '" + username + "' not found.";
        }
        Set<String> roles = userRoles.get(username);
        Set<String> permissions = new HashSet<>();
        for (String role : roles) {
            permissions.addAll(rolePermissions.getOrDefault(role, new HashSet<>()));
        }
        return "User: " + username + "\nRoles: " + roles + "\nPermissions: " + permissions;
    }

    // ------------------------------------------------------------
    // Demo Main Method
    // ------------------------------------------------------------
    public static void main(String[] args) throws IOException {

        PermissionSystem ps = new PermissionSystem();

        // Define roles
        ps.addRole("Admin");
        ps.addRole("Editor");

        // Add permissions
        ps.addPermissionToRole("Admin", "add_user");
        ps.addPermissionToRole("Admin", "delete_user");
        ps.addPermissionToRole("Editor", "edit_content");

        // Add users
        ps.addUser("Alice");
        ps.addUser("Bob");

        // Assign roles
        ps.assignRoleToUser("Alice", "Admin");
        ps.assignRoleToUser("Bob", "Editor");

        // Display access
        System.out.println(ps.displayUserAccess("Alice"));
        System.out.println(ps.displayUserAccess("Bob"));

        // Permission checks
        System.out.println("Can Alice delete user? " + ps.checkUserPermission("Alice", "delete_user"));
        System.out.println("Can Bob delete user? " + ps.checkUserPermission("Bob", "delete_user"));
    }
}

/*
------------------------------------------------------------
Expected Output:
------------------------------------------------------------
User: Alice
Roles: [Admin]
Permissions: [add_user, delete_user]
User: Bob
Roles: [Editor]
Permissions: [edit_content]
Can Alice delete user? true
Can Bob delete user? false
------------------------------------------------------------
*/

/*
------------------------------------------------------------
6) Limitations of Current Code:
------------------------------------------------------------
- No persistence (data lost after exit)
- No concurrency control
- No hierarchical role inheritance
- No permission removal or audit logs

------------------------------------------------------------
7) Alternative Algorithms and Trade-Offs (Future Discussions):
------------------------------------------------------------
- Graph-based Role Hierarchy:
     Enables inheritance (Admin → Editor → Viewer)
     Trade-off: More complex lookups (O(V+E))

- Persistent Storage (Database):
     Data durability, but setup overhead

- Role-Based Access Control (RBAC) with caching:
     Fast repeated lookups
     Trade-off: Cache invalidation complexity

- Attribute-Based Access Control (ABAC):
     Flexible and dynamic
     Trade-off: Higher runtime cost per access check

------------------------------------------------------------
End of File
------------------------------------------------------------
*/
