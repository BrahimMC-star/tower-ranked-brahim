package zwuiix.colria.permission;

import cn.nukkit.Server;
import cn.nukkit.permission.Permission;
import zwuiix.colria.Loader;

import java.util.HashMap;

public class PermissionRegistry {
    private static PermissionRegistry instance = new PermissionRegistry();

    public static PermissionRegistry getInstance() {
        return instance;
    }

    private HashMap<String, Permission> permissions = new HashMap<>();

    public HashMap<String, Permission> getPermissions() {
        return permissions;
    }

    public void setPermissions(HashMap<String, Permission> permissions) {
        this.permissions = permissions;
    }

    public Permission getPermission(String permission) {
        return permissions.get(permission);
    }

    public void invoke(Loader loader) {
        for (zwuiix.colria.permission.Permission perm : zwuiix.colria.permission.Permission.values()) {
            register(new Permission(perm.toString(), "", Permission.DEFAULT_OP));
        }
    }

    public void register(Permission permission) {
        permissions.put(permission.getName(), permission);
        Server.getInstance().getPluginManager().addPermission(permission);
    }
}
