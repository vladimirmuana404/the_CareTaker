import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.AlphaComposite;
import java.awt.RadialGradientPaint;
import java.awt.geom.Point2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

// The main menu: PLAY, SETTINGS, CREDITS, QUIT.
// It has three screens (the main list, the settings list and the credits).
public class MainMenu {

    // The three screens (just numbers with names).
    private static final int MAIN = 0;
    private static final int SETTINGS = 1;
    private static final int CREDITS = 2;

    private int screen = MAIN;
    private int selected = 0;      // which item the arrow is on

    // DATA STRUCTURE: arrays of text, one entry per menu item.
    private String[] mainItems = { "PLAY", "SETTINGS", "CREDITS", "QUIT" };

    // ---- SETTINGS (add more here later) ----
    private boolean darknessOn = true;

    // ---- CREDITS: write your own names here ----
    private String[] creditLines = {
        "THE CARETAKER: NO ROOM TO ESCAPE",
        "",
        "Made by:\n",
        "Queen Eurikha Comitan\n", 
        "Jhea Athena Domine\n",
        "Vladimir Muana\n",
        "Art and code: made for this school project",
        "",
    };

    // ASSET LINK: assets/images/menu/menu_bg.png and title.png
    private Image background;
    private Image title;
    private BufferedImage glowMask;
    private BufferedImage glowLayer;

    public MainMenu() {
        background = AssetLoader.load("images/menu/menu_bg.png");
        title = AssetLoader.load("images/menu/title.png");
        glowMask = createGlowMask(background);
    }

    public boolean isDarknessOn() { return darknessOn; }

    // Go back to the first screen (used when returning from the game).
    public void reset() {
        screen = MAIN;
        selected = 0;
    }

    // Reads the keys. Returns "play", "quit", or "" if nothing special happened.
    public String update(KeyHandler keys) {
        String action = "";
        if (screen == MAIN) {
            action = updateMain(keys);
        } else if (screen == SETTINGS) {
            updateSettings(keys);
        } else {
            updateCredits(keys);
        }

        // Use up the one-time key presses so they don't leak into the game.
        keys.menuUp = false;
        keys.menuDown = false;
        keys.confirmPressed = false;
        keys.backPressed = false;
        keys.interactPressed = false;
        return action;
    }

    // Moves the arrow up or down, wrapping around at the ends.
    private void moveSelection(KeyHandler keys, int count) {
        if (keys.menuUp) {
            selected = (selected + count - 1) % count;
        }
        if (keys.menuDown) {
            selected = (selected + 1) % count;
        }
    }

    private String updateMain(KeyHandler keys) {
        moveSelection(keys, mainItems.length);
        if (!keys.confirmPressed) {
            return "";
        }
        if (selected == 0) {
            return "play";
        } else if (selected == 1) {
            screen = SETTINGS;
            selected = 0;
        } else if (selected == 2) {
            screen = CREDITS;
            selected = 0;
        } else {
            return "quit";
        }
        return "";
    }

    private void updateSettings(KeyHandler keys) {
        moveSelection(keys, 2);
        if (keys.confirmPressed) {
            if (selected == 0) {
                darknessOn = !darknessOn;     // toggle the setting
            } else {
                goBackToMain(1);
            }
        }
        if (keys.backPressed) {
            goBackToMain(1);
        }
    }

    private void updateCredits(KeyHandler keys) {
        if (keys.confirmPressed || keys.backPressed) {
            goBackToMain(2);
        }
    }

    private void goBackToMain(int itemToSelect) {
        screen = MAIN;
        selected = itemToSelect;
    }

    private String[] getSettingsItems() {
        String state = darknessOn ? "ON" : "OFF";
        return new String[] { "DARKNESS: " + state, "BACK" };
    }

    // ---------------------------------------------------------------- drawing

    public void draw(Graphics2D g, int w, int h) {
        // 1. background picture (or a simple placeholder if the picture is missing)
        g.setColor(new Color(8, 5, 8));
        g.fillRect(0, 0, w, h);
        if (background != null) {
            g.drawImage(background, 0, 0, w, h, null);
        } else {
            drawPlaceholderBackground(g);
        }

        drawGlow(g, w, h);        // 2. pulsing red light from the door
        drawVignette(g, w, h);    // 3. dark edges
        if (screen == MAIN) {
            drawTitle(g, w, h);   // 4. THE CARETAKER
        }

        // 5. the list on the right
        if (screen == MAIN) {
            drawItems(g, mainItems);
        } else if (screen == SETTINGS) {
            drawItems(g, getSettingsItems());
        } else {
            drawCredits(g);
        }

        // 6. red corner decorations and the hint at the bottom
        drawCorner(g, 12, 12, 1, 1);
        drawCorner(g, w - 12, 12, -1, 1);
        drawCorner(g, 12, h - 12, 1, -1);
        drawCorner(g, w - 12, h - 12, -1, -1);

        g.setFont(new Font("Monospaced", Font.PLAIN, 12));
        g.setColor(new Color(150, 150, 160));
        drawCentered(g, "W/S or UP/DOWN: choose     ENTER: select     ESC: back", w / 2 + 130, h - 22);
    }

    // The red light from the doorway slowly gets brighter and darker.
    private void drawGlow(Graphics2D g, int w, int h) {
        double pulse = 0.5 + 0.5 * Math.sin(System.currentTimeMillis() / 450.0);
        int alpha = 35 + (int) (pulse * 160);
        Point2D door = new Point2D.Float(w * (150f / 768), h * (324f / 576));
        float[] fractions = { 0f, 1f };
        Color[] colors = { new Color(255, 40, 50, alpha), new Color(255, 40, 50, 0) };
        RadialGradientPaint glow = new RadialGradientPaint(door,
                (250f + (float) pulse * 90f) * Math.min(w / 768f, h / 576f), fractions, colors);

        if (glowMask == null) {
            g.setPaint(glow);
            g.fillRect(0, 0, w, h);
            return;
        }

        if (glowLayer == null || glowLayer.getWidth() != w || glowLayer.getHeight() != h) {
            glowLayer = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        }
        Graphics2D layerGraphics = glowLayer.createGraphics();
        layerGraphics.setComposite(AlphaComposite.Clear);
        layerGraphics.fillRect(0, 0, w, h);
        layerGraphics.setComposite(AlphaComposite.SrcOver);
        layerGraphics.setPaint(glow);
        layerGraphics.fillRect(0, 0, w, h);
        layerGraphics.setComposite(AlphaComposite.DstIn);
        layerGraphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        layerGraphics.drawImage(glowMask, 0, 0, w, h, null);
        layerGraphics.dispose();
        g.drawImage(glowLayer, 0, 0, null);
    }

    private BufferedImage createGlowMask(Image image) {
        if (image == null || image.getWidth(null) <= 0 || image.getHeight(null) <= 0) {
            return null;
        }

        int width = image.getWidth(null);
        int height = image.getHeight(null);
        BufferedImage pixelsImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D imageGraphics = pixelsImage.createGraphics();
        imageGraphics.drawImage(image, 0, 0, null);
        imageGraphics.dispose();

        int[] pixels = pixelsImage.getRGB(0, 0, width, height, null, 0, width);
        for (int i = 0; i < pixels.length; i++) {
            int red = (pixels[i] >>> 16) & 0xff;
            int green = (pixels[i] >>> 8) & 0xff;
            int blue = pixels[i] & 0xff;
            int brightness = (red * 299 + green * 587 + blue * 114) / 1000;
            int maskAlpha = Math.max(0, Math.min(255, (brightness - 10) * 6));
            pixels[i] = (maskAlpha << 24) | 0x00ffffff;
        }

        BufferedImage mask = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        mask.setRGB(0, 0, width, height, pixels, 0, width);
        return mask;
    }

    private void drawVignette(Graphics2D g, int w, int h) {
        Point2D center = new Point2D.Float(w * 0.4f, h * 0.5f);
        float[] fractions = { 0f, 0.55f, 1f };
        Color[] colors = { new Color(0, 0, 0, 0), new Color(0, 0, 0, 0), new Color(0, 0, 0, 200) };
        g.setPaint(new RadialGradientPaint(center, Math.max(w, h) * 0.75f, fractions, colors));
        g.fillRect(0, 0, w, h);
    }

    private void drawTitle(Graphics2D g, int w, int h) {
        int centerX = Math.round(w * (535f / 768));
        centerX = Math.max(16, Math.min(w - 16, centerX));
        int maxWidth = Math.min(340, 2 * Math.min(centerX - 16, w - 16 - centerX));
        int bottom = Math.round(h * (241f / 576));
        int maxHeight = Math.min(220, bottom - 20);
        if (maxWidth <= 0 || maxHeight <= 0) {
            return;
        }

        if (title != null && title.getWidth(null) > 0 && title.getHeight(null) > 0) {
            int picW = title.getWidth(null);
            int picH = title.getHeight(null);
            double scale = Math.min((double) maxWidth / picW, (double) maxHeight / picH);
            int titleWidth = (int) (picW * scale);
            int titleHeight = (int) (picH * scale);

            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g.drawImage(title, centerX - titleWidth / 2, bottom - titleHeight,
                    titleWidth, titleHeight, null);
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        } else {
            g.setColor(new Color(220, 44, 52));
            int titleFontSize = 44;
            Font titleFont = new Font("Serif", Font.BOLD | Font.ITALIC, titleFontSize);
            Font subtitleFont = new Font("Serif", Font.BOLD | Font.ITALIC, 26);
            FontMetrics titleMetrics = g.getFontMetrics(titleFont);
            FontMetrics subtitleMetrics = g.getFontMetrics(subtitleFont);
            while (titleFontSize > 1
                    && (titleMetrics.stringWidth("CARETAKER") > maxWidth
                            || titleMetrics.getHeight() + subtitleMetrics.getHeight() + 6 > maxHeight)) {
                titleFontSize--;
                titleFont = new Font("Serif", Font.BOLD | Font.ITALIC, titleFontSize);
                subtitleFont = new Font("Serif", Font.BOLD | Font.ITALIC, Math.max(1, titleFontSize * 26 / 44));
                titleMetrics = g.getFontMetrics(titleFont);
                subtitleMetrics = g.getFontMetrics(subtitleFont);
            }
            if (titleMetrics.stringWidth("CARETAKER") > maxWidth
                    || titleMetrics.getHeight() + subtitleMetrics.getHeight() + 6 > maxHeight) {
                return;
            }
            g.setFont(titleFont);
            g.drawString("CARETAKER", centerX - titleMetrics.stringWidth("CARETAKER") / 2, bottom - 4);
            g.setFont(subtitleFont);
            drawCentered(g, "THE", centerX, bottom - titleMetrics.getHeight() - 6);
        }
    }

    // Draws the list of choices. The selected one gets a red box and an arrow.
    private void drawItems(Graphics2D g, String[] labels) {
        g.setFont(new Font("Monospaced", Font.BOLD, 26));
        for (int i = 0; i < labels.length; i++) {
            int centerY = 280 + i * 58;
            if (i == selected) {
                g.setColor(new Color(40, 8, 12, 210));
                g.fillRect(400, centerY - 23, 270, 46);
                g.setColor(new Color(230, 50, 60));
                for (int t = 0; t < 3; t++) {                       // 3 pixel thick border
                    g.drawRect(400 + t, centerY - 23 + t, 270 - 2 * t, 46 - 2 * t);
                }
                int[] xs = { 422, 422, 438 };                       // the arrow
                int[] ys = { centerY - 9, centerY + 9, centerY };
                g.fillPolygon(xs, ys, 3);
                g.setColor(new Color(255, 96, 96));
            } else {
                g.setColor(new Color(205, 205, 215));
            }
            g.drawString(labels[i], 470, centerY + 9);
        }
    }

    private void drawCredits(Graphics2D g) {
        g.setFont(new Font("Monospaced", Font.PLAIN, 14));
        g.setColor(new Color(205, 205, 215));
        for (int i = 0; i < creditLines.length; i++) {
            g.drawString(creditLines[i], 392, 320 + i * 26);
        }
        g.setFont(new Font("Monospaced", Font.BOLD, 18));
        g.setColor(new Color(255, 96, 96));
        g.drawString("> BACK (ENTER)", 392, 320 + creditLines.length * 26 + 20);
    }

    // A red corner decoration. (x, y) is the corner point,
    // sx and sy are +1 or -1 and say which way the arms of the corner go.
    private void drawCorner(Graphics2D g, int x, int y, int sx, int sy) {
        g.setColor(new Color(200, 40, 50));
        fillBox(g, x, y, x + sx * 48, y + sy * 3);
        fillBox(g, x, y, x + sx * 3, y + sy * 48);
        int left = Math.min(x + sx * 12, x + sx * 24);
        int top = Math.min(y + sy * 12, y + sy * 24);
        g.drawRect(left, top, 12, 12);
    }

    private void fillBox(Graphics2D g, int x1, int y1, int x2, int y2) {
        g.fillRect(Math.min(x1, x2), Math.min(y1, y2), Math.abs(x2 - x1), Math.abs(y2 - y1));
    }

    private void drawCentered(Graphics2D g, String text, int centerX, int y) {
        FontMetrics fm = g.getFontMetrics();
        g.drawString(text, centerX - fm.stringWidth(text) / 2, y);
    }

    // PLACEHOLDER background: a red doorway with a black figure in it.
    private void drawPlaceholderBackground(Graphics2D g) {
        g.setColor(new Color(24, 14, 16));
        g.fillRect(40, 100, 230, 440);
        g.setColor(new Color(150, 36, 48));
        g.fillRect(66, 120, 168, 410);
        g.setColor(new Color(9, 5, 9));
        g.fillOval(112, 180, 70, 80);
        int[] xs = { 120, 174, 215, 80 };
        int[] ys = { 260, 260, 530, 530 };
        g.fillPolygon(xs, ys, 4);
    }
}

