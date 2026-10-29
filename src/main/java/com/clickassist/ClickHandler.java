package com.clickassist;

import java.lang.reflect.Field;
import java.util.Random;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import org.lwjgl.input.Mouse;

/**
 * Клики идут через обычную очередь нажатий Minecraft (KeyBinding.onTick),
 * то есть так же, как от настоящей мыши: тот же замах, те же пакеты.
 */
public class ClickHandler {
    private static final class Timer {
        boolean active;
        long next;
    }

    private final Random rnd = new Random();
    private final Timer attack = new Timer();
    private final Timer place = new Timer();
    private Field rightDelay;
    private boolean reflectionTried;

    @SubscribeEvent
    public void onKey(InputEvent.KeyInputEvent e) {
        Minecraft mc = Minecraft.getMinecraft();
        if (ClickAssist.openKey.isPressed() && mc.currentScreen == null && mc.player != null) {
            mc.displayGuiScreen(new ClickGui());
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent e) {
        if (e.phase != TickEvent.Phase.START) {
            return;
        }
        Minecraft mc = Minecraft.getMinecraft();
        boolean ready = mc.player != null && mc.world != null && mc.currentScreen == null && mc.inGameHasFocus;
        long now = System.nanoTime();

        boolean wantAttack = ready && ClickAssist.attackEnabled && Mouse.isButtonDown(0) && !blockedByBlock(mc);
        boolean wantPlace = ready && ClickAssist.placeEnabled && Mouse.isButtonDown(1) && !mc.player.isHandActive();

        int n = poll(attack, wantAttack, now, ClickAssist.attackCps);
        for (int i = 0; i < n; i++) {
            KeyBinding.onTick(mc.gameSettings.keyBindAttack.getKeyCode());
        }

        int m = poll(place, wantPlace, now, ClickAssist.placeCps);
        for (int i = 0; i < m; i++) {
            KeyBinding.onTick(mc.gameSettings.keyBindUseItem.getKeyCode());
        }
        if (wantPlace) {
            suppressVanillaRepeat(mc);
        }
    }

    private boolean blockedByBlock(Minecraft mc) {
        return ClickAssist.skipBlocks && mc.objectMouseOver != null
                && mc.objectMouseOver.typeOfHit == RayTraceResult.Type.BLOCK;
    }

    /** Сколько кликов пора сделать в этом тике. */
    private int poll(Timer t, boolean want, long now, int cps) {
        if (!want) {
            t.active = false;
            return 0;
        }
        if (!t.active) {
            // первый клик делает сама игра (настоящее нажатие), автоклик начинается после паузы
            t.active = true;
            t.next = now + delay(cps);
            return 0;
        }
        int count = 0;
        while (now >= t.next && count < 3) {
            count++;
            t.next += delay(cps);
        }
        if (now - t.next > 250_000_000L) {
            t.next = now + delay(cps);
        }
        return count;
    }

    /** Интервал в наносекундах. Никогда не быстрее, чем выбранный максимум КПС. */
    private long delay(int cps) {
        double base = 1_000_000_000.0 / Math.max(1, cps);
        if (!ClickAssist.humanize) {
            return (long) base;
        }
        double extra = Math.min(0.45, Math.abs(rnd.nextGaussian()) * 0.12);
        if (rnd.nextInt(25) == 0) {
            extra += 0.4 + rnd.nextDouble() * 0.6; // редкая «человеческая» пауза
        }
        return (long) (base * (1.0 + extra));
    }

    /** Чтобы ванильное удержание ПКМ (раз в 4 тика) не добавляло лишних кликов к выбранному КПС. */
    private void suppressVanillaRepeat(Minecraft mc) {
        try {
            if (rightDelay == null && !reflectionTried) {
                reflectionTried = true;
                rightDelay = ReflectionHelper.findField(Minecraft.class, "rightClickDelayTimer", "field_71467_ac");
            }
            if (rightDelay != null) {
                rightDelay.setInt(mc, 4);
            }
        } catch (Exception ex) {
            rightDelay = null;
        }
    }
}
