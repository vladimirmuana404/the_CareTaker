import java.awt.Color;
import java.awt.Graphics2D;

// OOP - INHERITANCE: Caretaker IS-A GameObject.
// For now she only stands still. Patrol, detection, noise and hiding memory
// come in Phase 4 (simple if-conditions, no learning AI).
public class Caretaker extends GameObject {

    public Caretaker(int col, int row) {
        super(col * TileMap.TILE_SIZE + 10, row * TileMap.TILE_SIZE + 12, 28, 24);
        setCharacterSprite();                // same idea as the player
        // ASSET LINK: assets/images/caretaker.png
        setSprite(AssetLoader.load("images/caretaker.png"));
    }

    // OOP - POLYMORPHISM:
    public void update() {
        // Phase 4: patrol / investigate / search / chase goes here.
    }

    public void draw(Graphics2D g) {
        if (drawSpriteIfAvailable(g)) {
            return;
        }
        g.setColor(Color.RED);                              // PLACEHOLDER: red circle
        g.fillOval(getX(), getY(), getWidth(), getHeight());
        g.setColor(Color.BLACK);
        g.drawOval(getX(), getY(), getWidth(), getHeight());
    }
}
