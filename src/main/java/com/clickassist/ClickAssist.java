package com.clickassist;

import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.lwjgl.input.Keyboard;

@Mod(modid = ClickAssist.MODID, name = "ClickAssist", version = "1.0",
        clientSideOnly = true, acceptedMinecraftVersions = "[1.12.2]", acceptableRemoteVersions = "*")
public class ClickAssist {
    public static final String MODID = "clickassist";
    public static final int MAX_CPS = 25;

    public static boolean attackEnabled = true;
    public static boolean placeEnabled = true;
    public static boolean humanize = true;
    public static boolean skipBlocks = true;
    public static int attackCps = 12;
    public static int placeCps = 12;

    public static KeyBinding openKey;
    private static Configuration config;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent e) {
        config = new Configuration(e.getSuggestedConfigurationFile());
        attackEnabled = config.getBoolean("attackEnabled", "main", true, "Авто-атака включена");
        placeEnabled = config.getBoolean("placeEnabled", "main", true, "Авто-стройка включена");
        humanize = config.getBoolean("humanize", "main", true, "Случайные интервалы между кликами");
        skipBlocks = config.getBoolean("skipBlocks", "main", true, "Не кликать ЛКМ по блокам (копание работает как обычно)");
        attackCps = config.getInt("attackCps", "main", 12, 1, MAX_CPS, "Максимум КПС при ударах");
        placeCps = config.getInt("placeCps", "main", 12, 1, MAX_CPS, "Максимум КПС при строительстве");
        if (config.hasChanged()) {
            config.save();
        }
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent e) {
        openKey = new KeyBinding("Открыть меню ClickAssist", Keyboard.KEY_RSHIFT, "ClickAssist");
        ClientRegistry.registerKeyBinding(openKey);
        MinecraftForge.EVENT_BUS.register(new ClickHandler());
    }

    public static void save() {
        if (config == null) {
            return;
        }
        config.get("main", "attackEnabled", true).set(attackEnabled);
        config.get("main", "placeEnabled", true).set(placeEnabled);
        config.get("main", "humanize", true).set(humanize);
        config.get("main", "skipBlocks", true).set(skipBlocks);
        config.get("main", "attackCps", 12).set(attackCps);
        config.get("main", "placeCps", 12).set(placeCps);
        config.save();
    }
}
