import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;

// OOP - INHERITANCE: Door IS-A GameObject.
// A door sits in a gap of the wall. Walking into it does one of two things:
//   - normal door : takes you to the NEXT ROOM (targetRoom)
//   - exit door   : you escape the house (exitName, e.g. "Front Exit")
public class Door extends GameObject {

    // Which wall the door is in (also the direction the exit arrow points).
    public static final int NORTH = 0;
    public static final int SOUTH = 1;
    public static final int WEST = 2;
    public static final int EAST = 3;

    private int col, row;          // tile position inside the room
    private int direction;
    private String targetRoom;     // key of the room it leads to (null for an exit)
    private String exitName;       // "Front Exit", "Back Exit"... (null for a normal door)
    private boolean locked = false;

    public Door(int col, int row, int direction, String targetRoom, String exitName) {
        super(col * TileMap.TILE_SIZE, row * TileMap.TILE_SIZE, TileMap.TILE_SIZE, TileMap.TILE_SIZE);
        this.col = col;
        this.row = row;
        this.direction = direction;
        this.targetRoom = targetRoom;
        this.exitName = exitName;

        // ASSET LINK: door pictures in assets/images/doors/, exit pictures in assets/images/exits/
        boolean upDown = (direction == NORTH || direction == SOUTH);
        if (exitName == null) {
            setSprite(AssetLoader.load(upDown ? "images/doors/door_ns.png" : "images/doors/door_ew.png"));
        } else if (direction == NORTH) {
            setSprite(AssetLoader.load("images/exits/exit_north.png"));
        } else if (direction == SOUTH) {
            setSprite(AssetLoader.load("images/exits/exit_south.png"));
        } else if (direction == EAST) {
            setSprite(AssetLoader.load("images/exits/exit_east.png"));
        } else {
            setSprite(AssetLoader.load("images/exits/exit_west.png"));
        }
    }

    public boolean isExit() { return exitName != null; }
    public String getExitName() { return exitName; }
    public String getTargetRoom() { return targetRoom; }
    public boolean isLocked() { return locked; }
    public void lock() { locked = true; }       // Phase 3: doors can need a key
    public void unlock() { locked = false; }

    // The floor tile just INSIDE the room, next to this door.
    // When you arrive from another room, you appear here.
    public int getInsideCol() {
        if (direction == WEST) return col + 1;
        if (direction == EAST) return col - 1;
        return col;
    }
    public int getInsideRow() {
        if (direction == NORTH) return row + 1;
        if (direction == SOUTH) return row - 1;
        return row;
    }

    // Which way the player looks when stepping in through this door.
    public int getInsideFacing() {
        if (direction == NORTH) return Player.DOWN;
        if (direction == SOUTH) return Player.UP;
        if (direction == WEST) return Player.RIGHT;
        return Player.LEFT;
    }

    public void update() {
        // Doors don't move.
    }

    public void draw(Graphics2D g) {
        if (!drawSpriteIfAvailable(g)) {
            drawPlaceholder(g);
        }
        if (isExit()) {
            drawLabel(g);
        }
    }

    // PLACEHOLDER: brown block = door, dark green block with white arrow = exit
    private void drawPlaceholder(Graphics2D g) {
        if (isExit()) {
            g.setColor(new Color(24, 70, 46));
        } else {
            g.setColor(new Color(120, 70, 30));
        }
        g.fillRect(getX(), getY(), getWidth(), getHeight());

        if (isExit()) {
            int cx = getX() + getWidth() / 2;
            int cy = getY() + getHeight() / 2;
            int[] xs;
            int[] ys;
            if (direction == NORTH) {
                xs = new int[] { cx - 10, cx + 10, cx };
                ys = new int[] { cy + 6, cy + 6, cy - 14 };
            } else if (direction == SOUTH) {
                xs = new int[] { cx - 10, cx + 10, cx };
                ys = new int[] { cy - 6, cy - 6, cy + 14 };
            } else if (direction == EAST) {
                xs = new int[] { cx - 6, cx - 6, cx + 14 };
                ys = new int[] { cy - 10, cy + 10, cy };
            } else {
                xs = new int[] { cx + 6, cx + 6, cx - 14 };
                ys = new int[] { cy - 10, cy + 10, cy };
            }
            g.setColor(new Color(120, 255, 160));
            g.fillPolygon(xs, ys, 3);
        }
    }

    // Green name tag next to the exit, like "Front Exit" in your reference picture.
    private void drawLabel(Graphics2D g) {
        g.setFont(new Font("Monospaced", Font.BOLD, 12));
        FontMetrics fm = g.getFontMetrics();
        int w = fm.stringWidth(exitName) + 14;
        int h = 22;
        int cx = getX() + getWidth() / 2;
        int cy = getY() + getHeight() / 2;
        int x;
        int y;
        if (direction == NORTH) {
            x = cx - w / 2;
            y = getY() + getHeight() + 6;
        } else if (direction == SOUTH) {
            x = cx - w / 2;
            y = getY() - h - 6;
        } else if (direction == EAST) {
            x = getX() - w - 6;
            y = cy - h / 2;
        } else {
            x = getX() + getWidth() + 6;
            y = cy - h / 2;
        }
        g.setColor(new Color(10, 30, 20, 230));
        g.fillRect(x, y, w, h);
        g.setColor(new Color(110, 255, 160));
        g.drawRect(x, y, w, h);
        g.drawString(exitName, x + 7, y + 15);
    }
}
