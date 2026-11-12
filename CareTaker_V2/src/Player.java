import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.util.ArrayList;

// OOP - INHERITANCE: Player IS-A GameObject.
public class Player extends GameObject {

    // The four directions the player can face (just numbers with names).
    public static final int UP = 0;
    public static final int DOWN = 1;
    public static final int LEFT = 2;
    public static final int RIGHT = 3;

    // OOP - ENCAPSULATION: private data, changed only through this class's methods.
    private int walkSpeed = 2;
    private int runSpeed = 4;
    private boolean running = false;   // Phase 4: running will make noise
    private int health = 3;            // shown as hearts in the HUD
    private int facing = DOWN;         // which way the player is looking

    // One picture per direction.
    // ASSET LINK: assets/images/player_up.png, player_down.png, player_left.png, player_right.png
    private Image upImage;
    private Image downImage;
    private Image leftImage;
    private Image rightImage;

    private KeyHandler keys;
    private TileMap map;                                                  // current level's map
    private ArrayList<Furniture> furniture = new ArrayList<Furniture>();   // current level's furniture

    private String message = "";
    private int messageTimer = 0;      // frames left before the message disappears

    public Player(KeyHandler keys) {
        super(0, 0, 28, 24);                 // hitbox = the feet area only (width 28, height 24)
        setCharacterSprite();                // the big picture is drawn around the hitbox
        this.keys = keys;

        upImage = AssetLoader.load("images/player_up.png");
        downImage = AssetLoader.load("images/player_down.png");
        leftImage = AssetLoader.load("images/player_left.png");
        rightImage = AssetLoader.load("images/player_right.png");
        setFacing(DOWN);
    }

    // Changes the direction AND swaps the picture to match.
    // If a picture is missing it becomes null, so the placeholder is drawn instead.
    private void setFacing(int direction) {
        facing = direction;
        if (direction == UP) {
            setSprite(upImage);
        } else if (direction == DOWN) {
            setSprite(downImage);
        } else if (direction == LEFT) {
            setSprite(leftImage);
        } else {
            setSprite(rightImage);
        }
    }

    // Called every time the player enters a room.
    // (col, row) is the tile to stand on, facingDirection is where the player looks.
    public void enterRoom(Room room, int col, int row, int facingDirection) {
        map = room.getMap();
        furniture = room.getFurniture();
        int t = TileMap.TILE_SIZE;
        setX(col * t + (t - getWidth()) / 2);   // center on the tile
        setY(row * t + (t - getHeight()) / 2);
        setFacing(facingDirection);
        message = "";
        messageTimer = 0;
    }

    // OOP - POLYMORPHISM: the player's own version of update()
    public void update() {
        running = keys.run;
        int speed = running ? runSpeed : walkSpeed;

        int dx = 0;
        int dy = 0;
        if (keys.up)    dy -= speed;
        if (keys.down)  dy += speed;
        if (keys.left)  dx -= speed;
        if (keys.right) dx += speed;

        // Face the direction of the keys. The player turns even if a wall blocks the move.
        // Up/down is checked first, left/right second, so on a diagonal the player faces sideways.
        if (keys.up)    setFacing(UP);
        if (keys.down)  setFacing(DOWN);
        if (keys.left)  setFacing(LEFT);
        if (keys.right) setFacing(RIGHT);

        // One axis at a time, so the player can slide along walls
        tryMove(dx, 0);
        tryMove(0, dy);

        if (keys.interactPressed) {
            keys.interactPressed = false;   // use each key press only once
            interactWithNearestFurniture();
        }

        if (messageTimer > 0) {
            messageTimer--;
            if (messageTimer == 0) {
                message = "";
            }
        }
    }

    private void tryMove(int dx, int dy) {
        Rectangle next = new Rectangle(getX() + dx, getY() + dy, getWidth(), getHeight());
        if (!isBlocked(next)) {
            setX(getX() + dx);
            setY(getY() + dy);
        }
    }

    // ALGORITHM: collision detection (walls first, then every solid furniture).
    private boolean isBlocked(Rectangle next) {
        if (map.collidesWithWall(next)) {
            return true;
        }
        for (Furniture f : furniture) {
            if (f.isSolid() && next.intersects(f.getBounds())) {
                return true;
            }
        }
        return false;
    }

    // ALGORITHM: LINEAR SEARCH through the ArrayList for the closest furniture in reach.
        // ALGORITHM: LINEAR SEARCH through the ArrayList for the closest furniture in reach.
    // Returns null if nothing is close enough. (GamePanel uses this to show the "E" icon.)
    public Furniture findNearestFurniture() {
        Rectangle reach = new Rectangle(getX() - 14, getY() - 14, getWidth() + 28, getHeight() + 28);
        Furniture nearest = null;
        double nearestDistance = 99999;

        for (Furniture f : furniture) {
            if (f.isSolid() && reach.intersects(f.getBounds())) {
                double distance = Math.hypot(f.getX() + f.getWidth() / 2 - getX(),
                                             f.getY() + f.getHeight() / 2 - getY());
                if (distance < nearestDistance) {
                    nearestDistance = distance;
                    nearest = f;
                }
            }
        }
        return nearest;
    }

    // Pressing E: interact with the closest furniture.
    private void interactWithNearestFurniture() {
        Furniture nearest = findNearestFurniture();
        if (nearest != null) {
            showMessage(nearest.interact());
        }
    }

    public void showMessage(String text) {
        message = text;
        messageTimer = 150;   // about 2.5 seconds
    }

    public String getMessage() { return message; }
    public boolean isRunning() { return running; }
    public int getHealth() { return health; }
    public int getFacing() { return facing; }

    // The front-facing picture, used for the HUD portrait (it should not turn around).
    public Image getPortrait() { return downImage; }

    public void draw(Graphics2D g) {
        if (drawSpriteIfAvailable(g)) {
            return;
        }
        // PLACEHOLDER: blue square with a white mark on the side the player is facing
        g.setColor(new Color(60, 140, 255));
        g.fillRect(getX(), getY(), getWidth(), getHeight());
        g.setColor(Color.WHITE);
        g.drawRect(getX(), getY(), getWidth(), getHeight());

        int cx = getX() + getWidth() / 2;
        int cy = getY() + getHeight() / 2;
        if (facing == UP) {
            g.fillRect(cx - 4, getY() + 2, 8, 4);
        } else if (facing == DOWN) {
            g.fillRect(cx - 4, getY() + getHeight() - 6, 8, 4);
        } else if (facing == LEFT) {
            g.fillRect(getX() + 2, cy - 4, 4, 8);
        } else {
            g.fillRect(getX() + getWidth() - 6, cy - 4, 4, 8);
        }
    }
}
