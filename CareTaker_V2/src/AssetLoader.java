import java.awt.Image;
import java.io.File;
import java.util.HashMap;
import javax.swing.ImageIcon;

// ASSET LINKING: every image in the game is loaded through this one class.
// Give it a path INSIDE the "assets" folder, e.g. "images/player.png".
// If the file is missing it returns null and the game draws a placeholder shape.
public class AssetLoader {

    // DATA STRUCTURE: HashMap (key = file path, value = image).
    // Each image is loaded from disk only ONCE, then reused. Much faster.
    private static HashMap<String, Image> cache = new HashMap<String, Image>();

    // Folders to check, in order, so it also works if you run from src or bin.
    private static final String[] FOLDERS = { "assets/", "../assets/", "CareTaker_V2/assets/" };

    public static Image load(String path) {
        if (cache.containsKey(path)) {
            return cache.get(path);   // already loaded before
        }

        Image img = null;
        for (int i = 0; i < FOLDERS.length; i++) {
            File file = new File(FOLDERS[i] + path);
            if (file.exists()) {
                img = new ImageIcon(file.getPath()).getImage();
                break;
            }
        }

        if (img == null) {
            System.out.println("MISSING: " + path + "   (running from: "
                               + new File(".").getAbsolutePath() + ")");
        }
        cache.put(path, img);   // remember the result (even null)
        return img;
    }
}
