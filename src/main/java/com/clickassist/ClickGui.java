package com.clickassist;

import java.io.IOException;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL11;

public class ClickGui extends GuiScreen {
    private static final int W = 250;
    private static final int H = 236;
    private static final int PAD = 16;
    private static final int ROW_H = 22;
    private static final int SL_H = 34;

    private static final int R_ATK_T = 40;
    private static final int R_ATK_S = 64;
    private static final int R_PLC_T = 102;
    private static final int R_PLC_S = 126;
    private static final int R_HUM = 164;
    private static final int R_SKP = 188;

    private static final int BG = 0xF2161A24;
    private static final int SHADOW = 0x55000000;
    private static final int ACCENT = 0xFF5B8CFF;
    private static final int OFF = 0xFF353C4D;
    private static final int TEXT = 0xFFE8ECF4;
    private static final int DIM = 0xFF8089A0;
    private static final int HOVER = 0x16FFFFFF;
    private static final int WHITE = 0xFFFFFFFF;

    private int x0;
    private int y0;
    private int dragging = -1;

    @Override
    public void initGui() {
        layout();
    }

    private void layout() {
        x0 = (this.width - W) / 2;
        y0 = (this.height - H) / 2;
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    @Override
    public void drawScreen(int mx, int my, float partialTicks) {
        layout();
        drawRect(0, 0, this.width, this.height, 0x70000000);

        roundedRect(x0 - 1, y0 + 3, W + 2, H + 2, 14, SHADOW);
        roundedRect(x0, y0, W, H, 12, BG);

        String title = "ClickAssist";
        GlStateManager.pushMatrix();
        GlStateManager.translate(x0 + W / 2f, y0 + 11, 0);
        GlStateManager.scale(1.4f, 1.4f, 1f);
        fontRenderer.drawStringWithShadow(title, -fontRenderer.getStringWidth(title) / 2f, 0, WHITE);
        GlStateManager.popMatrix();
        roundedRect(x0 + W / 2f - 18, y0 + 30, 36, 2, 1, ACCENT);

        toggle(R_ATK_T, "Авто-атака (ЛКМ)", ClickAssist.attackEnabled, mx, my);
        slider(R_ATK_S, "Макс. КПС атаки", ClickAssist.attackCps, ClickAssist.attackEnabled);
        toggle(R_PLC_T, "Авто-стройка (ПКМ)", ClickAssist.placeEnabled, mx, my);
        slider(R_PLC_S, "Макс. КПС стройки", ClickAssist.placeCps, ClickAssist.placeEnabled);
        toggle(R_HUM, "Случайные интервалы", ClickAssist.humanize, mx, my);
        toggle(R_SKP, "Не кликать по блокам (ЛКМ)", ClickAssist.skipBlocks, mx, my);

        drawCenteredString(fontRenderer, "Закрыть: Esc или клавиша меню", x0 + W / 2, y0 + H - 16, DIM);
    }

    private void toggle(int ry, String label, boolean on, int mx, int my) {
        int y = y0 + ry;
        if (inRow(mx, my, ry)) {
            roundedRect(x0 + 8, y, W - 16, ROW_H, 6, HOVER);
        }
        fontRenderer.drawString(label, x0 + PAD, y + 7, on ? TEXT : DIM);
        float sx = x0 + W - PAD - 34;
        float sy = y + 4;
        roundedRect(sx, sy, 34, 14, 7, on ? ACCENT : OFF);
        roundedRect(on ? sx + 22 : sx + 2, sy + 2, 10, 10, 5, WHITE);
    }

    private void slider(int ry, String label, int val, boolean enabled) {
        int y = y0 + ry;
        fontRenderer.drawString(label, x0 + PAD, y + 2, enabled ? TEXT : DIM);
        String v = String.valueOf(val);
        fontRenderer.drawString(v, x0 + W - PAD - fontRenderer.getStringWidth(v), y + 2, enabled ? ACCENT : DIM);

        float tx = x0 + PAD;
        float tw = W - 2 * PAD;
        float ty = y + 19;
        float t = (val - 1f) / (ClickAssist.MAX_CPS - 1f);
        roundedRect(tx, ty, tw, 6, 3, OFF);
        roundedRect(tx, ty, Math.max(6f, tw * t), 6, 3, enabled ? ACCENT : DIM);
        roundedRect(tx + tw * t - 6, ty - 3, 12, 12, 6, WHITE);
    }

    private boolean inRow(int mx, int my, int ry) {
        return mx >= x0 + 8 && mx <= x0 + W - 8 && my >= y0 + ry && my < y0 + ry + ROW_H;
    }

    private boolean inSlider(int mx, int my, int ry) {
        return mx >= x0 + PAD - 8 && mx <= x0 + W - PAD + 8 && my >= y0 + ry + 14 && my <= y0 + ry + SL_H;
    }

    private void drag(int mx) {
        float t = (mx - (x0 + PAD)) / (float) (W - 2 * PAD);
        t = Math.max(0f, Math.min(1f, t));
        int v = 1 + Math.round(t * (ClickAssist.MAX_CPS - 1));
        if (dragging == 0) {
            ClickAssist.attackCps = v;
        } else if (dragging == 1) {
            ClickAssist.placeCps = v;
        }
    }

    @Override
    protected void mouseClicked(int mx, int my, int button) throws IOException {
        super.mouseClicked(mx, my, button);
        if (button != 0) {
            return;
        }
        if (inRow(mx, my, R_ATK_T)) {
            ClickAssist.attackEnabled = !ClickAssist.attackEnabled;
        } else if (inRow(mx, my, R_PLC_T)) {
            ClickAssist.placeEnabled = !ClickAssist.placeEnabled;
        } else if (inRow(mx, my, R_HUM)) {
            ClickAssist.humanize = !ClickAssist.humanize;
        } else if (inRow(mx, my, R_SKP)) {
            ClickAssist.skipBlocks = !ClickAssist.skipBlocks;
        } else if (inSlider(mx, my, R_ATK_S)) {
            dragging = 0;
            drag(mx);
        } else if (inSlider(mx, my, R_PLC_S)) {
            dragging = 1;
            drag(mx);
        }
    }

    @Override
    protected void mouseClickMove(int mx, int my, int button, long timeSinceLastClick) {
        if (dragging >= 0) {
            drag(mx);
        }
    }

    @Override
    protected void mouseReleased(int mx, int my, int state) {
        dragging = -1;
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == ClickAssist.openKey.getKeyCode()) {
            this.mc.displayGuiScreen(null);
            if (this.mc.currentScreen == null) {
                this.mc.setIngameFocus();
            }
            return;
        }
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    public void onGuiClosed() {
        ClickAssist.save();
        // сбросить накопленные нажатия клавиши меню, чтобы окно не открылось снова
        while (ClickAssist.openKey.isPressed()) {
            // drain
        }
    }

    // ---------- рисование скруглённых прямоугольников ----------

    private static void arc(float cx, float cy, float r, int from, int to) {
        for (int a = from; a <= to; a += 10) {
            double rad = Math.toRadians(a);
            GL11.glVertex2d(cx + Math.cos(rad) * r, cy + Math.sin(rad) * r);
        }
    }

    static void roundedRect(float x, float y, float w, float h, float r, int argb) {
        r = Math.min(r, Math.min(w, h) / 2f);
        float a = (argb >> 24 & 255) / 255f;
        float red = (argb >> 16 & 255) / 255f;
        float g = (argb >> 8 & 255) / 255f;
        float b = (argb & 255) / 255f;

        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
        GlStateManager.color(red, g, b, a);

        GL11.glBegin(GL11.GL_TRIANGLE_FAN);
        GL11.glVertex2d(x + w / 2f, y + h / 2f);
        arc(x + r, y + r, r, 180, 270);
        arc(x + w - r, y + r, r, 270, 360);
        arc(x + w - r, y + h - r, r, 0, 90);
        arc(x + r, y + h - r, r, 90, 180);
        GL11.glVertex2d(x, y + r);
        GL11.glEnd();

        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.color(1f, 1f, 1f, 1f);
    }
}
