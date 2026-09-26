package meteordevelopment.meteorclient.timon;

import java.util.Set;

/** The public feature set of this distribution, also enforced by Module.toggle. */
public final class ClientFeatures {
    public static final Set<String> MODULES = Set.of(
        "flight", "elytra-fly", "fast-use", "auto-clicker", "xray",
        "auto-eat", "anti-hunger", "no-fall", "air-place"
    );

    private ClientFeatures() {}

    public static boolean allows(String moduleName) {
        return MODULES.contains(moduleName);
    }
}
