import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

// Reads the keyboard and remembers which keys are held down.
public class KeyHandler implements KeyListener {

    public boolean up, down, left, right;
    public boolean run;                 // SHIFT
    public boolean interactPressed;     // E (true for ONE press only)
    public boolean lightPressed;        // L (turn the darkness on/off)

    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();
        if (code == KeyEvent.VK_W || code == KeyEvent.VK_UP)    up = true;
        if (code == KeyEvent.VK_S || code == KeyEvent.VK_DOWN)  down = true;
        if (code == KeyEvent.VK_A || code == KeyEvent.VK_LEFT)  left = true;
        if (code == KeyEvent.VK_D || code == KeyEvent.VK_RIGHT) right = true;
        if (code == KeyEvent.VK_SHIFT) run = true;
        if (code == KeyEvent.VK_E) interactPressed = true;
        if (code == KeyEvent.VK_L) lightPressed = true;
    }

    public void keyReleased(KeyEvent e) {
        int code = e.getKeyCode();
        if (code == KeyEvent.VK_W || code == KeyEvent.VK_UP)    up = false;
        if (code == KeyEvent.VK_S || code == KeyEvent.VK_DOWN)  down = false;
        if (code == KeyEvent.VK_A || code == KeyEvent.VK_LEFT)  left = false;
        if (code == KeyEvent.VK_D || code == KeyEvent.VK_RIGHT) right = false;
        if (code == KeyEvent.VK_SHIFT) run = false;
    }

    public void keyTyped(KeyEvent e) { }   // required by KeyListener, not used
}
