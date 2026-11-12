import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import javax.swing.JPanel;
import javax.swing.Timer;

// THE GAME LOOP lives here. About 60 times per second:
//   1. update()  -> move things, check collisions, check the doors
//   2. repaint() -> draw everything again
public class GamePanel extends JPanel implements ActionListener {

    private Timer timer;
    private KeyHandler keys = new KeyHandler();
    private Player player = new Player(keys);

    private House house = new House();
    private Room room;               // the room the player is in right now
    private String currentKey;       // key of that room, e.g. "bedroom1"

    // DATA STRUCTURE: ArrayList of GameObject. It holds the furniture, doors,
    // Caretaker and player of the CURRENT room, so ONE loop can update and draw all.
    private ArrayList<GameObject> gameObjects = new ArrayList<GameObject>();

    private int bannerTimer = 0;          // frames left for the room name banner
    private boolean darknessOn = true;    // press L to turn it off while testing art
    private boolean gameFinished = false;
    private String escapedName = "";      // which exit the player used

    public GamePanel() {
        setPreferredSize(new Dimension(Room.SCREEN_COLS * TileMap.TILE_SIZE,
                                       Room.SCREEN_ROWS * TileMap.TILE_SIZE));
        setBackground(Color.BLACK);
        setFocusable(true);
        addKeyListener(keys);

        loadRoom(House.START_ROOM, null);

        timer = new Timer(16, this);   // calls actionPerformed every 16 ms
        timer.start();
    }

    // Shows a new room. 'fromKey' is the room the player just left (null at the start).
    public void loadRoom(String key, String fromKey) {
        currentKey = key;
        room = house.getRoom(key);

        int col = room.getStartCol();
        int row = room.getStartRow();
        int facing = Player.DOWN;

        // ALGORITHM: LINEAR SEARCH through this room's doors to find the one that
        // leads back to the room we came from. The player appears right next to it.
        if (fromKey != null) {
            for (Door d : room.getDoors()) {
                if (!d.isExit() && d.getTargetRoom().equals(fromKey)) {
                    col = d.getInsideCol();
                    row = d.getInsideRow();
                    facing = d.getInsideFacing();
                }
            }
        }
        player.enterRoom(room, col, row, facing);

        gameObjects.clear();
        gameObjects.addAll(room.getFurniture());   // rugs come first, so they draw underneath
        gameObjects.addAll(room.getDoors());
        if (room.hasCaretaker()) {
            // Phase 4: the Caretaker will be ONE object that walks between rooms.
            gameObjects.add(new Caretaker(room.getCaretakerCol(), room.getCaretakerRow()));
        }
        gameObjects.add(player);                   // last = drawn on top

        bannerTimer = 150;
    }

    // GAME LOOP: called by the Timer.
    public void actionPerformed(ActionEvent e) {
        update();
        repaint();
    }

    private void update() {
        if (keys.lightPressed) {
            keys.lightPressed = false;
            darknessOn = !darknessOn;
        }

        if (gameFinished) {
            if (keys.interactPressed) {          // E = play again
                keys.interactPressed = false;
                gameFinished = false;
                house = new House();
                loadRoom(House.START_ROOM, null);
            }
            return;
        }

        for (GameObject obj : gameObjects) {
            obj.update();   // each object runs ITS OWN update() (polymorphism)
        }

        if (bannerTimer > 0) {
            bannerTimer--;
        }

        checkDoors();
    }

    // Is the player walking through a door?
    private void checkDoors() {
        for (Door d : room.getDoors()) {
            if (player.getBounds().intersects(d.getBounds())) {
                if (d.isLocked()) {
                    if (player.getMessage().equals("")) {
                        player.showMessage("The door is locked.");
                    }
                } else if (d.isExit()) {
                    gameFinished = true;
                    escapedName = d.getExitName();
                } else {
                    loadRoom(d.getTargetRoom(), currentKey);   // go to the next room
                }
                return;   // stop here: the room may have just changed
            }
        }
    }

    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        // 16-BIT LOOK: pictures are drawn small (16x16) and scaled up 3x.
        // NEAREST_NEIGHBOR keeps every pixel sharp instead of blurry.
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

        // Draw the room centered in the window (the rest of the window stays black).
        int offsetX = room.getOffsetX();
        int offsetY = room.getOffsetY();
        g2.translate(offsetX, offsetY);
        room.getMap().draw(g2);               // 1. floor and walls
        for (GameObject obj : gameObjects) {
            obj.draw(g2);                     // 2. rugs, furniture, doors, Caretaker, player
        }
        drawInteractPrompt(g2);                 // 3. "E" icon if the player is near furniture
        g2.translate(-offsetX, -offsetY);

        if (darknessOn) {
            drawDarkness(g2, offsetX, offsetY);   // 3. dark horror lighting
        }
        drawHud(g2);                          // 4. portrait, hearts, inventory slots
        drawMessage(g2);                      // 5. messages from the player
        drawBanner(g2);                       // 6. room name
        if (gameFinished) {
            drawFinished(g2);
        }
    }

        // A small "E" key icon that floats above the player's head
    // whenever there is furniture close enough to interact with.
    private void drawInteractPrompt(Graphics2D g) {
        if (!player.getMessage().equals("")) {
            return;   // hide it while a message is on screen
        }
        if (player.findNearestFurniture() == null) {
            return;   // nothing close enough
        }

        int size = 22;
        int bob = (int) (Math.sin(System.currentTimeMillis() / 180.0) * 3);   // floats up and down
        int headTop = player.getY() + player.getHeight() - GameObject.CHARACTER_SIZE * 15 / 16;
        int x = player.getX() + player.getWidth() / 2 - size / 2;
        int y = headTop - size - 2 + bob;

        g.setColor(new Color(10, 10, 30, 235));
        g.fillRect(x, y, size, size);
        g.setColor(new Color(250, 226, 132));                                  // gold border
        g.drawRect(x, y, size, size);
        g.setFont(new Font("Monospaced", Font.BOLD, 16));
        FontMetrics fm = g.getFontMetrics();
        g.setColor(Color.WHITE);
        g.drawString("E", x + (size - fm.stringWidth("E")) / 2, y + 16);
    }

    // Dark overlay with a soft circle of light around the player.
    private void drawDarkness(Graphics2D g, int offsetX, int offsetY) {
        Point2D center = new Point2D.Float(offsetX + player.getX() + player.getWidth() / 2,
                                           offsetY + player.getY() - 6);
        float[] fractions = { 0f, 0.5f, 1f };
        Color[] colors = { new Color(0, 0, 0, 0), new Color(0, 0, 0, 70), new Color(0, 0, 0, 185) };
        g.setPaint(new RadialGradientPaint(center, 300f, fractions, colors));
        g.fillRect(0, 0, getWidth(), getHeight());
    }

    // Top-left panel like the legend in your concept art.
    private void drawHud(Graphics2D g) {
        g.setColor(new Color(16, 18, 40, 225));
        g.fillRect(10, 10, 212, 100);
        g.setColor(new Color(90, 90, 160));
        g.drawRect(10, 10, 212, 100);

        // portrait
        g.setColor(new Color(40, 30, 40));
        g.fillRect(18, 18, 44, 44);
        g.setColor(new Color(150, 110, 90));
        g.drawRect(18, 18, 44, 44);
        if (player.getPortrait() != null) {
            g.drawImage(player.getPortrait(), 24, 24, 32, 32, null);
        } else {
            g.setColor(new Color(60, 140, 255));
            g.fillRect(24, 24, 32, 32);
        }

        // hearts
        for (int i = 0; i < 3; i++) {
            drawHeart(g, 72 + i * 28, 30, i < player.getHealth());
        }

        // inventory (empty for now - Phase 3 fills these slots)
        for (int i = 0; i < 5; i++) {
            g.setColor(new Color(30, 32, 66));
            g.fillRect(18 + i * 38, 70, 32, 32);
            g.setColor(new Color(100, 100, 170));
            g.drawRect(18 + i * 38, 70, 32, 32);
        }

        g.setColor(new Color(255, 255, 255, 170));
        g.setFont(new Font("Monospaced", Font.PLAIN, 12));
        String controls = "WASD move   SHIFT run   E interact   L light";
        int controlsWidth = g.getFontMetrics().stringWidth(controls);
        g.drawString(controls, getWidth() - controlsWidth - 10, 24);
    }

    // DATA STRUCTURE: 2D array. A heart drawn as big pixels (0 = empty, 1 = red, 2 = shine).
    private static final int[][] HEART = {
        {0, 1, 1, 0, 1, 1, 0},
        {1, 2, 1, 1, 1, 1, 1},
        {1, 1, 1, 1, 1, 1, 1},
        {0, 1, 1, 1, 1, 1, 0},
        {0, 0, 1, 1, 1, 0, 0},
        {0, 0, 0, 1, 0, 0, 0}
    };

    private void drawHeart(Graphics2D g, int x, int y, boolean filled) {
        for (int row = 0; row < HEART.length; row++) {
            for (int col = 0; col < HEART[row].length; col++) {
                int cell = HEART[row][col];
                if (cell == 0) {
                    continue;   // skip empty pixels
                }
                if (!filled) {
                    g.setColor(new Color(70, 40, 52));
                } else if (cell == 2) {
                    g.setColor(new Color(255, 170, 180));
                } else {
                    g.setColor(new Color(224, 56, 70));
                }
                g.fillRect(x + col * 3, y + row * 3, 3, 3);   // each pixel is 3x3 on screen
            }
        }
    }

    private void drawMessage(Graphics2D g) {
        String message = player.getMessage();
        if (message.equals("")) {
            return;
        }
        g.setColor(new Color(10, 10, 30, 215));
        g.fillRect(120, getHeight() - 56, getWidth() - 240, 40);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Monospaced", Font.BOLD, 16));
        drawCentered(g, message, getHeight() - 30);
    }

    private void drawBanner(Graphics2D g) {
        if (bannerTimer <= 0) {
            return;
        }
        g.setColor(new Color(16, 18, 40, 230));
        g.fillRect(getWidth() / 2 - 210, 150, 420, 50);
        g.setColor(new Color(90, 90, 160));
        g.drawRect(getWidth() / 2 - 210, 150, 420, 50);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Monospaced", Font.BOLD, 22));
        drawCentered(g, room.getName(), 183);
    }

    private void drawFinished(Graphics2D g) {
        g.setColor(new Color(0, 0, 0, 215));
        g.fillRect(0, 0, getWidth(), getHeight());
        g.setColor(Color.WHITE);
        g.setFont(new Font("Monospaced", Font.BOLD, 30));
        drawCentered(g, "You escaped through the " + escapedName + "!", getHeight() / 2 - 10);
        g.setFont(new Font("Monospaced", Font.PLAIN, 16));
        drawCentered(g, "(Keys, puzzles and the Caretaker AI come in the next phases.)", getHeight() / 2 + 24);
        drawCentered(g, "Press E to play again.", getHeight() / 2 + 54);
    }

    // Helper: writes text in the horizontal middle of the panel.
    private void drawCentered(Graphics2D g, String text, int y) {
        FontMetrics fm = g.getFontMetrics();
        g.drawString(text, (getWidth() - fm.stringWidth(text)) / 2, y);
    }
}
