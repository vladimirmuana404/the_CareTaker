// OOP - ABSTRACTION: an interface says WHAT an object can do, not HOW.
// Anything the player can press E on (furniture now, mirrors and cabinets with
// puzzles later) implements this interface.
public interface Interactable {
    String interact();   // returns a message to show on screen
}
