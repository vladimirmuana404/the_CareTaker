ASSETS - 16-BIT STYLE (HOW THEY ARE LINKED)
===========================================
Every picture is drawn SMALL and the game scales it up 3x with sharp pixels.
So a tile is 16x16 here and shows as 48x48 on screen. Draw at these native sizes.

Every picture is loaded by AssetLoader.java using a path INSIDE this folder.
Missing file = the game draws a colored placeholder, so you can replace images
one at a time. To swap art: save a PNG with the SAME NAME and run again.
Use PNG with a transparent background. Do not anti-alias or blur: crisp pixels!

FILE (inside assets/)                 NATIVE SIZE   NOTES
images/player_down.png                16x16         facing down (front)
images/player_up.png                  16x16         facing up (back)
images/player_left.png                16x16         facing left
images/player_right.png               16x16         facing right
images/caretaker.png                  16x16

images/tiles/floor_wood.png           16x16         bedrooms, living room, hallway, dining room
images/tiles/floor_tile.png           16x16         kitchen
images/tiles/floor_bathroom.png       16x16         bathroom
images/tiles/floor_stone.png          16x16         storage, electrical room, garage
images/tiles/wall_house.png           16x16         walls of every room
images/tiles/floor_grass.png          16x16         (not used right now)
images/tiles/wall_hedge.png           16x16         (not used right now)

images/doors/door_ns.png              16x16         door in a top or bottom wall
images/doors/door_ew.png              16x16         door in a left or right wall
images/exits/exit_north.png           16x16         green arrow up    (Back Exit)
images/exits/exit_south.png           16x16         green arrow down  (Front Exit)
images/exits/exit_east.png            16x16         green arrow right (Garage Exit)
images/exits/exit_west.png            16x16         green arrow left  (not used right now)

images/furniture/bed.png              32x48         (2x3 tiles)  blue bed
images/furniture/bed_purple.png       32x48         (2x3)        master bedroom
images/furniture/wardrobe.png         32x16         (2x1)
images/furniture/desk.png             32x16
images/furniture/bookshelf.png        32x16
images/furniture/shelf_metal.png      32x16
images/furniture/sofa.png             32x16
images/furniture/counter.png          32x16
images/furniture/tv_stand.png         32x16
images/furniture/electrical_panel.png 32x16
images/furniture/table.png            32x32         (2x2)
images/furniture/car.png              32x48         (2x3)
images/furniture/bathtub.png          16x32         (1x2)
images/furniture/rug.png              48x32         (3x2)
images/furniture/rug_long.png         80x32         (5x2)
images/furniture/fridge.png           16x16         (1x1)
images/furniture/plant.png            16x16
images/furniture/crate.png            16x16
images/furniture/nightstand.png       16x16
images/furniture/toilet.png           16x16
images/furniture/sink.png             16x16
sounds/                               empty for now (Phase 6)

CHARACTERS: the player and the Caretaker are drawn 16x16 and shown 56x56 on screen
(change CHARACTER_SIZE in GameObject.java to make them bigger or smaller).
Their hitbox is only the feet area (28x24). See setCharacterSprite() in GameObject.java.

ADD NEW FURNITURE:
  1. Save the picture in images/furniture/ (example: lamp.png, 16x16 pixels)
  2. In House.java add one line inside the room you want:
       r.addSolid("lamp.png", 4, 6, 1, 1, "A flickering lamp.");
     (file, column, row, width in tiles, height in tiles, text when pressing E)
  The picture must be (width in tiles x 16) by (height in tiles x 16) pixels.
