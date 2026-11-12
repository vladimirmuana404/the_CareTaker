THE CARETAKER: NO ROOM TO ESCAPE  (Version 3: the whole house, one room at a time)
==================================================================================
FOLDERS
  src/      all .java files
  assets/   your images and sounds (see assets/README.txt)

RUN (open a terminal IN THIS FOLDER, not inside src):
    javac -d bin src/*.java
    java -cp bin Main
  (delete the old "bin" folder first if you had an older version)

RUN IN AN IDE: open THIS folder as the project and run Main.java.
  The working directory must be this folder or the assets will not be found
  (the console prints MISSING: ... if that happens).

CONTROLS
  WASD / Arrows = move   SHIFT = run   E = look at / interact with furniture
  L = turn the darkness on/off (handy for looking at the art)

HOW THE MAP WORKS
  The game shows ONE FULL ROOM at a time. Walk through a door (a gap in the wall)
  and you appear in the next room, right next to the door you came through.

  THE HOUSE (matches your floor plan)

     Master Bedroom  <->  Living Room  <->  Bedroom 2          Back Exit = top of the Living Room
                              |
     Bathroom  <->  Hallway  <->  Kitchen                      Garage Exit = right side of the Kitchen
                    |  |  |
     Storage <-> Electrical Room   Dining Room <-> Garage      Front Exit = bottom of the Hallway

  You start in the Master Bedroom.
  Green arrows with a name tag are exits. Walking into one ends the game (for now).
  Keys, puzzles and locked doors come in Phase 3.

FILES
  House.java   builds all 10 rooms (furniture and doors). Edit this to change the house.
  Room.java    one room (its map, furniture and doors)
  Door.java    a door to the next room, or an exit to the outside
  GamePanel.java  game loop, drawing, HUD, switching rooms
