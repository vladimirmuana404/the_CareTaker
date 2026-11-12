import java.awt.Color;
import java.util.HashMap;

// The whole house, based on your floor plan.
//
// DATA STRUCTURE: this is a GRAPH. Every room is a node, every door is a link
// to another room (a door stores the key of the room it leads to).
//
//   TOP     Master Bedroom  <->  Living Room  <->  Bedroom 2        (Back Exit is on top of the Living Room)
//   MIDDLE  Bathroom        <->  Hallway      <->  Kitchen          (Garage Exit is on the right of the Kitchen)
//   BOTTOM  Storage <-> Electrical Room <-> Hallway <-> Dining Room <-> Garage   (Front Exit is below the Hallway)
//
// The Living Room connects down to the Hallway.
public class House {

    public static final String START_ROOM = "bedroom1";

    private static final Color WOOD = new Color(92, 62, 42);
    private static final Color TILE = new Color(160, 144, 124);
    private static final Color STONE = new Color(84, 82, 88);
    private static final Color BATH = new Color(158, 194, 214);
    private static final Color WALL = new Color(54, 50, 68);

    // DATA STRUCTURE: HashMap (room key -> Room).
    // A room is built the first time you enter it, then kept (so it stays the same).
    private HashMap<String, Room> rooms = new HashMap<String, Room>();

    public Room getRoom(String key) {
        if (!rooms.containsKey(key)) {
            rooms.put(key, createRoom(key));
        }
        return rooms.get(key);
    }

    // Makes an empty room (walls around, floor inside) of the given size in tiles.
    private Room newRoom(String key, String name, int cols, int rows, String floorFile, Color floorColor) {
        TileMap map = new TileMap(cols, rows, floorFile, "wall_house.png", floorColor, WALL);
        return new Room(key, name, map);
    }

    // Positions are in TILES: (column, row), counted from the top-left corner of the room.
    private Room createRoom(String key) {
        Room r;

        if (key.equals("bedroom1")) {
            r = newRoom(key, "Master Bedroom", 12, 9, "floor_wood.png", WOOD);
            r.setStart(6, 5);
            r.addDoor(11, 4, Door.EAST, "living");
            r.addDecor("rug.png", 3, 3, 3, 2);
            r.addSolid("bed_purple.png", 1, 1, 2, 3, "A neatly made bed with purple sheets.");
            r.addSolid("nightstand.png", 3, 1, 1, 1, "A nightstand with a warm lamp.");
            r.addHideable("wardrobe.png", 7, 1, 2, 1, "An old wardrobe.");
            r.addSolid("plant.png", 10, 7, 1, 1, "A dying plant.");

        } else if (key.equals("living")) {
            r = newRoom(key, "Living Room", 16, 11, "floor_wood.png", WOOD);
            r.setStart(7, 5);
            r.setCaretaker(3, 6);
            r.addExit(7, 0, Door.NORTH, "Back Exit");
            r.addDoor(7, 10, Door.SOUTH, "hallway");
            r.addDoor(0, 5, Door.WEST, "bedroom1");
            r.addDoor(15, 5, Door.EAST, "bedroom2");
            r.addDecor("rug.png", 5, 4, 3, 2);
            r.addHideable("sofa.png", 5, 7, 2, 1, "A worn-out sofa.");
            r.addHideable("sofa.png", 8, 7, 2, 1, "Another sofa. The cushions are torn.");
            r.addSolid("tv_stand.png", 9, 1, 2, 1, "A TV that only shows static.");
            r.addSolid("bookshelf.png", 12, 1, 2, 1, "Books about the house's history.");
            r.addSolid("plant.png", 5, 1, 1, 1, "A dried-out plant.");
            r.addSolid("plant.png", 14, 8, 1, 1, "A plant that is somehow still alive.");
            r.addSolid("plant.png", 1, 8, 1, 1, "A wilted plant.");

        } else if (key.equals("bedroom2")) {
            r = newRoom(key, "Bedroom 2", 12, 9, "floor_wood.png", WOOD);
            r.setStart(2, 4);
            r.addDoor(0, 4, Door.WEST, "living");
            r.addDecor("rug.png", 4, 5, 3, 2);
            r.addSolid("desk.png", 1, 1, 2, 1, "A desk with scratched initials.");
            r.addSolid("bookshelf.png", 4, 1, 2, 1, "Dusty old books.");
            r.addSolid("bed.png", 8, 1, 2, 3, "A bed with blue sheets.");
            r.addSolid("nightstand.png", 10, 1, 1, 1, "A nightstand with a lamp.");
            r.addSolid("plant.png", 10, 7, 1, 1, "A potted plant.");

        } else if (key.equals("bathroom")) {
            r = newRoom(key, "Bathroom", 10, 8, "floor_bathroom.png", BATH);
            r.setStart(7, 4);
            r.addDoor(9, 4, Door.EAST, "hallway");
            r.addSolid("bathtub.png", 1, 1, 1, 2, "A bathtub. The water is still.");
            r.addSolid("toilet.png", 3, 1, 1, 1, "A toilet.");
            r.addSolid("sink.png", 5, 1, 1, 1, "A sink with a cracked mirror.");
            r.addSolid("plant.png", 8, 6, 1, 1, "A small plant.");

        } else if (key.equals("hallway")) {
            r = newRoom(key, "Hallway", 16, 12, "floor_wood.png", WOOD);
            r.setStart(7, 5);
            r.addDoor(7, 0, Door.NORTH, "living");
            r.addDoor(0, 3, Door.WEST, "bathroom");
            r.addDoor(15, 3, Door.EAST, "kitchen");
            r.addDoor(0, 8, Door.WEST, "electrical");
            r.addDoor(15, 8, Door.EAST, "dining");
            r.addExit(7, 11, Door.SOUTH, "Front Exit");
            r.addDecor("rug_long.png", 5, 5, 5, 2);
            r.addSolid("bookshelf.png", 9, 1, 2, 1, "A shelf of faded photographs.");
            r.addSolid("plant.png", 13, 1, 1, 1, "A tall plant.");
            r.addSolid("plant.png", 2, 10, 1, 1, "A plant in the corner.");
            r.addHideable("wardrobe.png", 10, 10, 2, 1, "A coat closet.");

        } else if (key.equals("kitchen")) {
            r = newRoom(key, "Kitchen", 12, 9, "floor_tile.png", TILE);
            r.setStart(2, 4);
            r.addDoor(0, 4, Door.WEST, "hallway");
            r.addExit(11, 4, Door.EAST, "Garage Exit");
            r.addSolid("counter.png", 3, 1, 2, 1, "A kitchen counter.");
            r.addSolid("counter.png", 5, 1, 2, 1, "Dirty dishes are piled up.");
            r.addSolid("counter.png", 7, 1, 2, 1, "A counter with an old sink.");
            r.addSolid("fridge.png", 9, 1, 1, 1, "The fridge hums. Better not open it.");
            r.addSolid("table.png", 4, 4, 2, 2, "A heavy kitchen table.");
            r.addSolid("plant.png", 10, 7, 1, 1, "A wilted herb pot.");

        } else if (key.equals("storage")) {
            r = newRoom(key, "Storage Room", 12, 9, "floor_stone.png", STONE);
            r.setStart(9, 4);
            r.setCaretaker(6, 3);
            r.addDoor(11, 4, Door.EAST, "electrical");
            r.addSolid("shelf_metal.png", 3, 1, 2, 1, "Metal shelves full of boxes.");
            r.addSolid("shelf_metal.png", 6, 1, 2, 1, "Boxes of rusty tools.");
            r.addSolid("shelf_metal.png", 9, 1, 2, 1, "Jars and cables.");
            r.addHideable("crate.png", 2, 5, 1, 1, "A big crate.");
            r.addHideable("crate.png", 3, 5, 1, 1, "Another big crate.");
            r.addSolid("crate.png", 9, 6, 1, 1, "A sealed crate.");
            r.addSolid("desk.png", 5, 6, 2, 1, "A workbench with scattered parts.");

        } else if (key.equals("electrical")) {
            r = newRoom(key, "Electrical Room", 10, 8, "floor_stone.png", STONE);
            r.setStart(7, 4);
            r.addDoor(0, 4, Door.WEST, "storage");
            r.addDoor(9, 4, Door.EAST, "hallway");
            r.addSolid("shelf_metal.png", 1, 1, 2, 1, "Shelves of spare parts.");
            r.addSolid("electrical_panel.png", 4, 1, 2, 1, "The electrical panel. A fuse is missing.");
            r.addSolid("crate.png", 7, 1, 1, 1, "A crate of old wires.");
            r.addSolid("crate.png", 4, 6, 1, 1, "A heavy crate.");
            r.addSolid("crate.png", 5, 6, 1, 1, "Another heavy crate.");

        } else if (key.equals("dining")) {
            r = newRoom(key, "Dining Room", 12, 9, "floor_wood.png", WOOD);
            r.setStart(2, 4);
            r.addDoor(0, 4, Door.WEST, "hallway");
            r.addDoor(11, 4, Door.EAST, "garage");
            r.addSolid("table.png", 4, 3, 2, 2, "A dining table set for guests who never came.");
            r.addSolid("bookshelf.png", 8, 1, 2, 1, "A china cabinet.");
            r.addSolid("nightstand.png", 10, 1, 1, 1, "A side table with a lamp.");
            r.addSolid("plant.png", 1, 7, 1, 1, "A potted plant.");

        } else {
            // garage
            r = newRoom("garage", "Garage", 14, 9, "floor_stone.png", STONE);
            r.setStart(2, 4);
            r.addDoor(0, 4, Door.WEST, "dining");
            r.addSolid("car.png", 7, 2, 2, 3, "A car. The doors are locked and the tank is empty.");
            r.addHideable("crate.png", 3, 6, 1, 1, "A crate of old tires.");
            r.addSolid("crate.png", 4, 6, 1, 1, "A crate of oily rags.");
            r.addSolid("shelf_metal.png", 10, 1, 2, 1, "Cans of paint and oil.");
            r.addSolid("crate.png", 1, 6, 1, 1, "A dusty crate.");
        }
        return r;
    }
}
