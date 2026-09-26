/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.utils.misc.input;

import com.mojang.blaze3d.platform.InputConstants;
import meteordevelopment.meteorclient.MeteorClient;
import net.minecraft.client.KeyMapping;

public class KeyBinds {
    private static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(MeteorClient.identifier("meteor-client"));

    public static KeyMapping OPEN_GUI = new KeyMapping("key.meteor-client.open-gui", InputConstants.Type.KEYBOARD, InputConstants.KEY_RSHIFT, CATEGORY);
    public static KeyMapping OPEN_COMMANDS = new KeyMapping("key.meteor-client.open-commands", InputConstants.Type.KEYBOARD, InputConstants.KEY_PERIOD, CATEGORY);
    public static KeyMapping DISABLE_ALL = new KeyMapping("key.timon-client.disable-all", InputConstants.KEY_F8, CATEGORY);

    private KeyBinds() {
    }

    public static KeyMapping[] apply(KeyMapping[] binds) {
        // Add key binding
        KeyMapping[] newBinds = new KeyMapping[binds.length + 3];

        System.arraycopy(binds, 0, newBinds, 0, binds.length);
        newBinds[binds.length] = OPEN_GUI;
        newBinds[binds.length + 1] = OPEN_COMMANDS;
        newBinds[binds.length + 2] = DISABLE_ALL;

        return newBinds;
    }
}
