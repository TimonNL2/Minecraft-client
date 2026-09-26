package nl.timon.liteclient.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import nl.timon.liteclient.ClientConfig;
import nl.timon.liteclient.LiteClient;
import nl.timon.liteclient.ModuleId;

/** Keyboard-navigable vanilla widgets with pagination at small GUI sizes. */
public final class ClientScreen extends Screen {
    private static final int ACCENT = 0xFF75E0C3;
    private final Screen parent;
    private final List<Button> moduleButtons = new ArrayList<>();
    private final List<ModuleId> visibleModules = new ArrayList<>();
    private boolean settings;
    private int page;
    private int pages;
    private int panelX;
    private int panelY;
    private int panelWidth;
    private int panelHeight;

    public ClientScreen() {
        this(null);
    }

    public ClientScreen(Screen parent) {
        super(Component.literal("Timon Client"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        panelWidth = Math.min(520, width - 16);
        panelHeight = Math.min(338, height - 16);
        panelX = (width - panelWidth) / 2;
        panelY = (height - panelHeight) / 2;
        int innerWidth = panelWidth - 24;
        int left = panelX + 12;
        int half = (innerWidth - 8) / 2;

        Button modulesTab = addRenderableWidget(Button.builder(Component.literal("Modules"), button -> selectTab(false))
                .bounds(left, panelY + 42, half, 20).build());
        modulesTab.active = settings;
        Button settingsTab = addRenderableWidget(Button.builder(Component.literal("Instellingen"), button -> selectTab(true))
                .bounds(left + half + 8, panelY + 42, half, 20).build());
        settingsTab.active = !settings;

        moduleButtons.clear();
        visibleModules.clear();
        List<Setting> options = settings ? settings() : List.of();
        int count = settings ? options.size() : ModuleId.values().length;
        int columns = innerWidth >= 350 ? 2 : 1;
        int rows = Math.max(1, (panelHeight - 132) / 24);
        int perPage = columns * rows;
        pages = Math.max(1, (count + perPage - 1) / perPage);
        page = Math.clamp(page, 0, pages - 1);
        int cellWidth = (innerWidth - (columns - 1) * 8) / columns;

        for (int offset = 0; offset < perPage && page * perPage + offset < count; offset++) {
            int index = page * perPage + offset;
            int x = left + (offset % columns) * (cellWidth + 8);
            int y = panelY + 72 + (offset / columns) * 24;
            if (settings) {
                Setting option = options.get(index);
                addRenderableWidget(Button.builder(Component.literal(option.label().get()), button -> {
                    option.change().run();
                    LiteClient.get().config().sanitize();
                    LiteClient.get().save();
                    button.setMessage(Component.literal(option.label().get()));
                }).bounds(x, y, cellWidth, 20).tooltip(Tooltip.create(Component.literal(option.hint()))).build());
            } else {
                ModuleId module = ModuleId.values()[index];
                visibleModules.add(module);
                moduleButtons.add(addRenderableWidget(Button.builder(moduleLabel(module), button -> {
                    LiteClient client = LiteClient.get();
                    client.setEnabled(module, !client.isEnabled(module));
                    refreshModuleLabels();
                }).bounds(x, y, cellWidth, 20).tooltip(Tooltip.create(Component.literal(module.description()))).build()));
            }
        }

        int pagerY = panelY + panelHeight - 54;
        Button previous = addRenderableWidget(Button.builder(Component.literal("< Vorige"), button -> turnPage(-1))
                .bounds(left, pagerY, Math.min(90, half), 20).build());
        previous.active = page > 0;
        Button next = addRenderableWidget(Button.builder(Component.literal("Volgende >"), button -> turnPage(1))
                .bounds(left + innerWidth - Math.min(90, half), pagerY, Math.min(90, half), 20).build());
        next.active = page + 1 < pages;
        addRenderableWidget(Button.builder(Component.literal("Alles uit"), button -> {
            LiteClient.get().disableAll();
            refreshModuleLabels();
        }).bounds(left, panelY + panelHeight - 28, half, 20)
                .tooltip(Tooltip.create(Component.literal("Schakelt alle negen modules direct uit."))).build());
        addRenderableWidget(Button.builder(Component.literal("Klaar"), button -> onClose())
                .bounds(left + half + 8, panelY + panelHeight - 28, half, 20).build());
    }

    private void selectTab(boolean showSettings) {
        settings = showSettings;
        page = 0;
        rebuildWidgets();
    }

    private void turnPage(int direction) {
        page = Math.clamp(page + direction, 0, pages - 1);
        rebuildWidgets();
    }

    private Component moduleLabel(ModuleId module) {
        boolean enabled = LiteClient.get().isEnabled(module);
        return Component.literal(module.displayName() + "  " + (enabled ? "AAN" : "UIT"))
                .withColor(enabled ? 0x75E0C3 : 0xC8CED7);
    }

    private void refreshModuleLabels() {
        for (int i = 0; i < moduleButtons.size(); i++) {
            moduleButtons.get(i).setMessage(moduleLabel(visibleModules.get(i)));
        }
    }

    @Override
    public void tick() {
        refreshModuleLabels();
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        graphics.fill(0, 0, width, height, 0x9A070A10);
        graphics.fill(panelX, panelY, panelX + panelWidth, panelY + panelHeight, 0xF0161C25);
        graphics.fill(panelX, panelY, panelX + panelWidth, panelY + 2, ACCENT);
        graphics.text(font, title, panelX + 12, panelY + 12, 0xFFF0F3F7, false);
        String subtitle = settings ? "Klik om een waarde te wijzigen. Wordt direct bewaard." : "Houd je muis op een module voor uitleg en beperkingen.";
        graphics.text(font, font.plainSubstrByWidth(subtitle, panelWidth - 24), panelX + 12, panelY + 27, 0xFFA5B2C1, false);
        graphics.centeredText(font, (page + 1) + " / " + pages, width / 2, panelY + panelHeight - 48, 0xFFADB9C8);
        super.extractRenderState(graphics, mouseX, mouseY, delta);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void onClose() {
        LiteClient.get().save();
        minecraft.gui.setScreen(parent);
    }

    private List<Setting> settings() {
        ClientConfig c = LiteClient.get().config();
        return List.of(
                new Setting(() -> "Flight-snelheid: " + decimal(c.flightSpeed),
                        () -> c.flightSpeed = next(c.flightSpeed, 0.2, 0.4, 0.6, 0.8, 1.0, 1.2, 1.6, 2.0, 2.5, 3.0),
                        "Bewegingssnelheid in blokken per tick. Hogere snelheden worden vaker door de server teruggezet."),
                new Setting(() -> "Elytra-snelheid: " + decimal(c.elytraSpeed),
                        () -> c.elytraSpeed = next(c.elytraSpeed, 0.4, 0.8, 1.2, 1.6, 2.0, 2.5, 3.0, 4.0),
                        "Snelheid tijdens elytra-vlucht. De server kan de snelheid begrenzen."),
                new Setting(() -> "Flight anti-kick: " + onOff(c.flightAntiKick),
                        () -> c.flightAntiKick = !c.flightAntiKick,
                        "Voegt af en toe een kleine neerwaartse beweging toe. Voorkomt niet alle kicks of correcties."),
                new Setting(() -> "Fast Use: " + c.fastUseDelay + " ticks",
                        () -> c.fastUseDelay = (c.fastUseDelay + 1) % 5,
                        "Clientvertraging tussen gebruikspogingen: 0 tot 4 ticks. Eetduur en servercooldowns blijven gelden."),
                new Setting(() -> "Autoclicker: " + c.clicksPerSecond + " CPS",
                        () -> c.clicksPerSecond = c.clicksPerSecond >= 20 ? 1 : c.clicksPerSecond + 1,
                        "Aantal klikken per seconde: 1 tot 20. Hogere CPS verwijdert geen aanvalscooldown."),
                new Setting(() -> "Klikactie: " + (c.clickRight ? "gebruiken" : "aanvallen"),
                        () -> c.clickRight = !c.clickRight,
                        "Kies de aanvalactie (standaard links) of de gebruikactie (standaard rechts)."),
                new Setting(() -> "Klikken: " + (c.clickHold ? "vasthouden" : "automatisch"),
                        () -> c.clickHold = !c.clickHold,
                        "Vasthouden vereist de bijbehorende muisknop. Automatisch klikt voortdurend zolang Autoclicker actief is en er geen menu openstaat."),
                new Setting(() -> "Eten bij: " + c.eatAt + " / 20",
                        () -> c.eatAt = c.eatAt >= 19 ? 1 : c.eatAt + 1,
                        "Begin automatisch te eten bij deze voedselwaarde of lager. Een volledig gevulde voedselbalk is 20."),
                new Setting(() -> "Air Place-bereik: " + decimal(c.airPlaceReach),
                        () -> c.airPlaceReach = next(c.airPlaceReach, 1.0, 1.5, 2.0, 2.5, 3.0, 3.5, 4.0, 4.5),
                        "Afstand in blokken voor de plaatsingspoging. De server beslist of de plaatsing geldig is."),
                new Setting(() -> "Air Place: " + c.airPlaceDelay + " ticks",
                        () -> c.airPlaceDelay = c.airPlaceDelay >= 20 ? 1 : c.airPlaceDelay + 1,
                        "Wachttijd tussen plaatsingspogingen terwijl je gebruiken vasthoudt. 20 ticks is ongeveer een seconde."));
    }

    private static String decimal(double value) {
        return String.format(Locale.ROOT, "%.1f", value);
    }

    private static String onOff(boolean value) {
        return value ? "aan" : "uit";
    }

    private static double next(double current, double... choices) {
        for (double value : choices) {
            if (value > current + 0.001) return value;
        }
        return choices[0];
    }

    private record Setting(Supplier<String> label, Runnable change, String hint) {}
}
