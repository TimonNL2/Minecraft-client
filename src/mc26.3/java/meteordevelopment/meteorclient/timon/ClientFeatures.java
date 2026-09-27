package meteordevelopment.meteorclient.timon;

import java.util.Set;

/** The public feature set for Minecraft 26.3, also enforced by Module.toggle. */
public final class ClientFeatures {
    public static final Set<String> MODULES = Set.of(
        "flight", "elytra-fly", "fast-use", "auto-clicker", "xray",
        "auto-eat", "anti-hunger", "no-fall", "air-place",
        "storage-esp", "auto-fish", "mace-spoof"
    );

    private ClientFeatures() {}

    public static boolean allows(String moduleName) {
        return MODULES.contains(moduleName);
    }
}
