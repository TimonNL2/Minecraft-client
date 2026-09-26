package meteordevelopment.meteorclient.timon;

import com.mojang.blaze3d.Blaze3D;
import java.net.URI;

/** Minecraft 26.3 moved operating-system link handling into Blaze3D. */
public final class PlatformLinks {
    private PlatformLinks() {}
    public static void openUri(String uri) { Blaze3D.openUri(URI.create(uri)); }
}
