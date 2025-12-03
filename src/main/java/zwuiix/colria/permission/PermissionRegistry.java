package zwuiix.colria.permission;

import cn.nukkit.Server;
import cn.nukkit.permission.Permission;
import lombok.Getter;
import lombok.Setter;
import zwuiix.colria.Loader;

import java.util.HashMap;

@Setter
@Getter
public class PermissionRegistry {
    @Getter
    private static final PermissionRegistry instance = new PermissionRegistry();

    private HashMap<String, Permission> permissions = new HashMap<>();

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
