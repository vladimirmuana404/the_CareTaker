import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

// OOP - INHERITANCE: Furniture IS-A GameObject.
// OOP - ABSTRACTION: Furniture also implements Interactable (press E on it).
// Beds, wardrobes, desks, sofas, crates, plants and rugs are all this one class.
public class Furniture extends GameObject implements Interactable {

    private String name;          // e.g. "bed" (also used for the placeholder label)
    private boolean solid;        // true = blocks the player. false = decoration (rug)
    private boolean hideable;     // Phase 4: the player will be able to hide here
    private String description;   // text shown when the player presses E

    // Position and size are given in TILES, then converted to pixels.
    public Furniture(String spriteFile, int col, int row, int widthInTiles, int heightInTiles,
                     boolean solid, boolean hideable, String description) {
        super(col * TileMap.TILE_SIZE, row * TileMap.TILE_SIZE,
              widthInTiles * TileMap.TILE_SIZE, heightInTiles * TileMap.TILE_SIZE);
        this.name = spriteFile.replace(".png", "");
        this.solid = solid;
        this.hideable = hideable;
        this.description = description;

        // ASSET LINK: furniture pictures live in assets/images/furniture/
        setSprite(AssetLoader.load("images/furniture/" + spriteFile));
    }

    public boolean isSolid() { return solid; }
    public boolean isHideable() { return hideable; }

    // Interface method (Interactable)
    public String interact() {
        if (hideable) {
            return description + " (A good hiding spot.)";
        }
        return description;
    }

    public void update() {
        // Furniture doesn't move.
    }

    // OOP - POLYMORPHISM: Furniture's own version of draw()
    public void draw(Graphics2D g) {
        if (drawSpriteIfAvailable(g)) {
            return;
        }
        // PLACEHOLDER: colored box with the furniture name written on it
        if (solid) {
            g.setColor(new Color(110, 78, 50));
        } else {
            g.setColor(new Color(120, 40, 44));
        }
        g.fillRect(getX(), getY(), getWidth(), getHeight());
        g.setColor(Color.BLACK);
        g.drawRect(getX(), getY(), getWidth(), getHeight());
        g.setColor(Color.WHITE);
        g.setFont(new Font("Monospaced", Font.PLAIN, 11));
        g.drawString(name, getX() + 4, getY() + 16);
    }
}
