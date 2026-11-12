import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;

// The floor and walls of ONE level (one room).
public class TileMap {

    public static final int TILE_SIZE = 48;

    // Tile codes (the numbers stored inside the 2D array)
    public static final int FLOOR = 0;
    public static final int WALL = 1;
    public static final int DOOR = 2;   // a gap in the wall that a Door object fills

    // DATA STRUCTURE: 2D ARRAY. map[row][column], each number is one square.
    private int[][] map;

    private Image floorImage;
    private Image wallImage;
    private Color floorColor;   // placeholder colors, used if the image is missing
    private Color wallColor;

    public TileMap(int cols, int rows, String floorFile, String wallFile,
                   Color floorColor, Color wallColor) {
        this.floorColor = floorColor;
        this.wallColor = wallColor;

        // ASSET LINK: tile pictures live in assets/images/tiles/
        floorImage = AssetLoader.load("images/tiles/" + floorFile);
        wallImage = AssetLoader.load("images/tiles/" + wallFile);

        // Build an empty room: walls on the border, floor inside.
        map = new int[rows][cols];
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                boolean border = (row == 0 || col == 0 || row == rows - 1 || col == cols - 1);
                if (border) {
                    map[row][col] = WALL;
                } else {
                    map[row][col] = FLOOR;
                }
            }
        }
    }

    // Adds extra wall blocks inside the room (a half wall, a pillar...).
    public void addWall(int col, int row, int widthInTiles, int heightInTiles) {
        for (int r = row; r < row + heightInTiles; r++) {
            for (int c = col; c < col + widthInTiles; c++) {
                map[r][c] = WALL;
            }
        }
    }

    // Turns one wall tile into a door tile (the player can walk through it).
    public void setDoor(int col, int row) {
        map[row][col] = DOOR;
    }

    public int getRows() { return map.length; }
    public int getCols() { return map[0].length; }

    public int getTile(int col, int row) {
        if (col < 0 || row < 0 || row >= map.length || col >= map[0].length) {
            return WALL;   // outside the map counts as a wall
        }
        return map[row][col];
    }

    // ALGORITHM: pixel position -> grid position (divide by TILE_SIZE).
    public boolean isWallAt(int pixelX, int pixelY) {
        if (pixelX < 0 || pixelY < 0) {
            return true;
        }
        return getTile(pixelX / TILE_SIZE, pixelY / TILE_SIZE) == WALL;
    }

    // ALGORITHM: a rectangle hits a wall if any of its 4 corners is in a wall tile.
    public boolean collidesWithWall(Rectangle r) {
        int right = r.x + r.width - 1;
        int bottom = r.y + r.height - 1;
        return isWallAt(r.x, r.y) || isWallAt(right, r.y)
            || isWallAt(r.x, bottom) || isWallAt(right, bottom);
    }

    public void draw(Graphics2D g) {
        // Loop through every row and column of the 2D array
        for (int row = 0; row < getRows(); row++) {
            for (int col = 0; col < getCols(); col++) {
                int x = col * TILE_SIZE;
                int y = row * TILE_SIZE;

                if (getTile(col, row) == WALL) {
                    if (wallImage != null) {
                        g.drawImage(wallImage, x, y, TILE_SIZE, TILE_SIZE, null);
                    } else {
                        g.setColor(wallColor);                 // PLACEHOLDER wall
                        g.fillRect(x, y, TILE_SIZE, TILE_SIZE);
                        g.setColor(Color.BLACK);
                        g.drawRect(x, y, TILE_SIZE, TILE_SIZE);
                    }
                } else {
                    // Floor (door tiles also have floor underneath)
                    if (floorImage != null) {
                        g.drawImage(floorImage, x, y, TILE_SIZE, TILE_SIZE, null);
                    } else {
                        g.setColor(floorColor);                // PLACEHOLDER floor
                        g.fillRect(x, y, TILE_SIZE, TILE_SIZE);
                    }
                }
            }
        }
    }
}
