package meteordevelopment.meteorclient.timon;

import net.minecraft.util.Util;

/** Small version boundary for opening a user-selected documentation link. */
public final class PlatformLinks {
    private PlatformLinks() {}
    public static void openUri(String uri) { Util.getPlatform().openUri(uri); }
}
