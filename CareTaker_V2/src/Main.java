import javax.swing.JFrame;

// ENTRY POINT: run this file. It only creates the window.
public class Main {
    public static void main(String[] args) {
        JFrame window = new JFrame("The CareTaker: No Room to Escape");
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setResizable(false);

        GamePanel panel = new GamePanel();   // the panel holds the whole game
        window.add(panel);
        window.pack();                        // window size = panel size
        window.setLocationRelativeTo(null);   // center on screen
        window.setVisible(true);

        panel.requestFocusInWindow();         // so the keyboard works right away
    }
}
