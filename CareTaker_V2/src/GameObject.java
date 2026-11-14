import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;

// OOP - ABSTRACTION + INHERITANCE:
// Parent of everything in the house (Player, Caretaker, Furniture, Exit, and
// later Key, Bat, Trap...). It is "abstract", so you cannot create it directly.
public abstract class GameObject {

    // How big the player and the Caretaker look on screen, in pixels.
    // Their pictures are 16x16, so 48 = 3x (same pixel size as the world), 56 = in
    // between, 64 = 4x.
    public static final int CHARACTER_SIZE = 48;

    // OOP - ENCAPSULATION: private fields, other classes use getters/setters.
    private int x, y, width, height;
    private Image sprite; // stays null until a picture exists

    // The picture can be BIGGER than the hitbox (like a 16-bit RPG character:
    // the hitbox is just the feet, the picture also shows the head and body).
    private int spriteWidth, spriteHeight;
    private int spriteOffsetX = 0, spriteOffsetY = 0;

    public GameObject(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.spriteWidth = width; // by default the picture fills the hitbox
        this.spriteHeight = height;
    }

    // Use this when the picture is bigger than the hitbox.
    // Offsets move the picture relative to the hitbox's top-left corner.
    protected void setSpriteBox(int width, int height, int offsetX, int offsetY) {
        spriteWidth = width;
        spriteHeight = height;
        spriteOffsetX = offsetX;
        spriteOffsetY = offsetY;
    }

    // OOP - POLYMORPHISM: every child class writes its OWN version of these.
    public abstract void update();

    public abstract void draw(Graphics2D g);

    // Draws the picture if there is one. Returns false if not, so the child
    // class knows it must draw its placeholder shape instead.
    protected boolean drawSpriteIfAvailable(Graphics2D g) {
        if (sprite == null) {
            return false;
        }
        g.drawImage(sprite, x + spriteOffsetX, y + spriteOffsetY, spriteWidth, spriteHeight, null);
        return true;
    }

    // ALGORITHM: this rectangle is used for collision checks (AABB collision).
    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }

    // Sets up a character picture (player / Caretaker) bigger than its hitbox:
    // centered sideways, with the feet lined up with the bottom of the hitbox.
    protected void setCharacterSprite() {
        setSpriteBox(CHARACTER_SIZE, CHARACTER_SIZE,
                -(CHARACTER_SIZE - width) / 2,
                height - CHARACTER_SIZE * 15 / 16);
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public Image getSprite() {
        return sprite;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public void setSprite(Image sprite) {
        this.sprite = sprite;
    }
}
