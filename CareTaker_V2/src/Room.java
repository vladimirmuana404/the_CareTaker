import java.util.ArrayList;

// One room of the house. Only ONE room is on screen at a time (the full room,
// centered in the window). Doors lead to the next room.
public class Room {

    // The window always fits 16 x 12 tiles. Smaller rooms are centered inside it.
    public static final int SCREEN_COLS = 16;
    public static final int SCREEN_ROWS = 12;

    private String key;     // short id the doors use, e.g. "bedroom1"
    private String name;    // shown on screen, e.g. "Master Bedroom"
    private TileMap map;

    // DATA STRUCTURE: ArrayLists of everything inside this room.
    private ArrayList<Furniture> furniture = new ArrayList<Furniture>();
    private ArrayList<Door> doors = new ArrayList<Door>();

    private int startCol = 1;
    private int startRow = 1;
    private int caretakerCol = -1;   // -1 means "no Caretaker in this room"
    private int caretakerRow = -1;

    public Room(String key, String name, TileMap map) {
        this.key = key;
        this.name = name;
        this.map = map;
    }

    // ---- getters (encapsulation) ----
    public String getKey() { return key; }
    public String getName() { return name; }
    public TileMap getMap() { return map; }
    public ArrayList<Furniture> getFurniture() { return furniture; }
    public ArrayList<Door> getDoors() { return doors; }
    public int getStartCol() { return startCol; }
    public int getStartRow() { return startRow; }
    public int getCaretakerCol() { return caretakerCol; }
    public int getCaretakerRow() { return caretakerRow; }
    public boolean hasCaretaker() { return caretakerCol >= 0; }

    // How far the room is moved from the top-left of the window so it sits in the middle.
    public int getOffsetX() { return (SCREEN_COLS - map.getCols()) * TileMap.TILE_SIZE / 2; }
    public int getOffsetY() { return (SCREEN_ROWS - map.getRows()) * TileMap.TILE_SIZE / 2; }

    // ---- small helper methods used by House.java to build rooms ----
    public void setStart(int col, int row) { startCol = col; startRow = row; }
    public void setCaretaker(int col, int row) { caretakerCol = col; caretakerRow = row; }

    public void addSolid(String file, int col, int row, int w, int h, String text) {
        furniture.add(new Furniture(file, col, row, w, h, true, false, text));
    }
    public void addHideable(String file, int col, int row, int w, int h, String text) {
        furniture.add(new Furniture(file, col, row, w, h, true, true, text));
    }
    public void addDecor(String file, int col, int row, int w, int h) {
        furniture.add(new Furniture(file, col, row, w, h, false, false, ""));   // rugs: walk over them
    }

    // A door to another room. Put it in a wall tile (the border of the room).
    public void addDoor(int col, int row, int direction, String targetRoom) {
        map.setDoor(col, row);
        doors.add(new Door(col, row, direction, targetRoom, null));
    }

    // A door that leads OUTSIDE (escape!).
    public void addExit(int col, int row, int direction, String exitName) {
        map.setDoor(col, row);
        doors.add(new Door(col, row, direction, null, exitName));
    }
}
