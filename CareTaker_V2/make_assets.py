# 16-BIT STYLE ART GENERATOR
# Draws every sprite at its NATIVE size (tiles are 16x16, like a SNES game) with a
# limited color palette, 3-tone shading, dithering and dark outlines.
# The Java game then scales everything up 3x with sharp, non-blurry pixels.
import zlib, struct, random, math, os

OUT = 'assets/images/'

class Canvas:
    def __init__(s, w, h):
        s.w, s.h = w, h
        s.px = [[None] * w for _ in range(h)]
    def set(s, x, y, c):
        if 0 <= x < s.w and 0 <= y < s.h: s.px[y][x] = c
    def rect(s, x, y, w, h, c):
        for yy in range(y, y + h):
            for xx in range(x, x + w): s.set(xx, yy, c)
    def ellipse(s, cx, cy, rx, ry, c):
        for y in range(s.h):
            for x in range(s.w):
                if ((x + .5 - cx) / rx) ** 2 + ((y + .5 - cy) / ry) ** 2 <= 1: s.set(x, y, c)
    def shaded(s, cx, cy, rx, ry, light, mid, dark):
        for y in range(s.h):
            for x in range(s.w):
                dx, dy = (x + .5 - cx) / rx, (y + .5 - cy) / ry
                if dx * dx + dy * dy <= 1:
                    t = dx * 0.5 + dy * 0.7
                    s.set(x, y, dark if t > 0.45 else (light if t < -0.45 else mid))
    def outline(s, col):
        todo = []
        for y in range(s.h):
            for x in range(s.w):
                if s.px[y][x] is None:
                    for ax, ay in ((1, 0), (-1, 0), (0, 1), (0, -1)):
                        nx, ny = x + ax, y + ay
                        if 0 <= nx < s.w and 0 <= ny < s.h and s.px[ny][nx] is not None:
                            todo.append((x, y)); break
        for x, y in todo: s.px[y][x] = col
    def paste(s, o, ox, oy, scale=1):
        for y in range(o.h * scale):
            for x in range(o.w * scale):
                c = o.px[y // scale][x // scale]
                if c: s.set(ox + x, oy + y, c)
    def save(s, path):
        raw = b''.join(b'\x00' + b''.join(bytes(c + (255,)) if c else b'\x00\x00\x00\x00' for c in row) for row in s.px)
        def chunk(t, d):
            c = struct.pack('>I', len(d)) + t + d
            return c + struct.pack('>I', zlib.crc32(t + d) & 0xffffffff)
        open(path, 'wb').write(b'\x89PNG\r\n\x1a\n' + chunk(b'IHDR', struct.pack('>IIBBBBB', s.w, s.h, 8, 6, 0, 0, 0))
                               + chunk(b'IDAT', zlib.compress(raw, 9)) + chunk(b'IEND', b''))

def flip(c):
    n = Canvas(c.w, c.h)
    for y in range(c.h):
        for x in range(c.w): n.px[y][x] = c.px[y][c.w - 1 - x]
    return n

def dither(c, x, y, w, h, a, b):
    for yy in range(y, y + h):
        for xx in range(x, x + w): c.set(xx, yy, a if (xx + yy) % 2 == 0 else b)

def block(c, x, y, w, h, light, mid, dark):
    c.rect(x, y, w, h, K); c.rect(x + 1, y + 1, w - 2, h - 2, mid)
    c.rect(x + 1, y + 1, 1, h - 2, light); c.rect(x + 1, y + 1, w - 2, 1, light); c.rect(x + 1, y + h - 2, w - 2, 1, dark)

# ---------------- PALETTE (about 60 colors, like a 16-bit game) ----------------
K = (22, 16, 30)                                   # outline: dark purple, never pure black
W1, W2, W3, W4 = (58, 36, 30), (96, 60, 40), (136, 90, 54), (178, 126, 76)         # wood
S1, S2, S3 = (34, 32, 50), (58, 56, 82), (86, 84, 114)                              # wallpaper stone
G1, G2, G3 = (50, 48, 60), (76, 74, 88), (104, 102, 118)                            # flagstone
GR1, GR2, GR3, GR4 = (22, 54, 34), (36, 86, 48), (58, 122, 62), (96, 160, 80)       # grass
H1, H2, H3 = (18, 46, 30), (30, 76, 44), (50, 112, 58)                              # hedge
T1, T2, T3 = (92, 78, 70), (170, 154, 134), (204, 188, 164)                         # kitchen tile
B1, B2, B3, B4 = (34, 42, 84), (56, 72, 130), (92, 112, 178), (156, 176, 224)       # blue fabric
R1, R2, R3 = (84, 22, 34), (132, 38, 50), (180, 66, 66)                             # red rug
Y1, Y2, Y3 = (150, 108, 40), (214, 172, 70), (250, 226, 132)                        # gold
SK1, SK2 = (196, 134, 104), (244, 196, 156)                                         # skin
HB1, HB2, HB3 = (66, 38, 28), (112, 68, 42), (164, 108, 64)                         # brown hair
M1, M2, M3, M4 = (62, 68, 84), (102, 110, 128), (152, 160, 178), (204, 210, 222)    # metal
PL1, PL2, PL3 = (24, 66, 40), (44, 112, 58), (88, 170, 84)                          # plant
WHITE, EYE = (236, 236, 246), (30, 22, 34)
PANTS, SHOE = (46, 42, 70), (34, 26, 34)
rnd = random.Random(7)
os.makedirs(OUT + 'tiles', exist_ok=True); os.makedirs(OUT + 'furniture', exist_ok=True); os.makedirs(OUT + 'exits', exist_ok=True); os.makedirs(OUT + 'doors', exist_ok=True)
assets = {}
def save(c, path, name):
    c.save(OUT + path); assets[name] = c

# ---------------- TILES 16x16 ----------------
c = Canvas(16, 16)
for p in range(4):
    y0 = p * 4
    c.rect(0, y0, 16, 4, W2); c.rect(0, y0, 16, 1, W3); c.rect(0, y0 + 3, 16, 1, W1)
    c.rect((3 + p * 5) % 16, y0, 1, 3, W1)
    for _ in range(4): c.set(rnd.randint(0, 15), y0 + rnd.randint(1, 2), rnd.choice([W1, W3]))
save(c, 'tiles/floor_wood.png', 'floor_wood')

c = Canvas(16, 16)
c.rect(0, 0, 16, 16, T2)
for (x, y) in [(0, 0), (8, 8)]: c.rect(x, y, 8, 8, T3)
c.rect(0, 0, 16, 1, T1); c.rect(0, 8, 16, 1, T1); c.rect(0, 0, 1, 16, T1); c.rect(8, 0, 1, 16, T1)
for (x, y) in [(1, 1), (9, 1), (1, 9), (9, 9)]: c.set(x, y, WHITE)
for _ in range(8): c.set(rnd.randint(0, 15), rnd.randint(0, 15), T1)
save(c, 'tiles/floor_tile.png', 'floor_tile')

c = Canvas(16, 16)
c.rect(0, 0, 16, 16, G2)
for (ox, y0) in [(0, 0), (4, 8)]:
    c.rect(0, y0, 16, 1, G1); c.rect(ox, y0, 1, 8, G1); c.rect(ox + 8, y0, 1, 8, G1)
    for sx in (ox + 1, ox + 9):
        c.rect(sx % 16, y0 + 1, 6, 1, G3)
for _ in range(10): c.set(rnd.randint(0, 15), rnd.randint(0, 15), G1)
c.set(10, 3, G1); c.set(11, 4, G1); c.set(5, 11, G1); c.set(6, 12, G1)
save(c, 'tiles/floor_stone.png', 'floor_stone')

c = Canvas(16, 16)
c.rect(0, 0, 16, 16, GR2)
for _ in range(26): c.set(rnd.randint(0, 15), rnd.randint(0, 15), rnd.choice([GR1, GR3]))
for _ in range(5):
    x, y = rnd.randint(1, 14), rnd.randint(1, 13)
    c.set(x, y, GR4); c.set(x - 1, y - 1, GR4); c.set(x + 1, y - 1, GR4)
save(c, 'tiles/floor_grass.png', 'floor_grass')

c = Canvas(16, 16)
c.rect(0, 0, 16, 16, S2)
for row in range(4):
    y0 = row * 4
    c.rect(0, y0 + 3, 16, 1, S1)
    off = 0 if row % 2 == 0 else 4
    for bx in (off, off + 8): c.rect(bx % 16, y0, 1, 3, S1)
    c.rect(0, y0, 16, 1, S3)
for _ in range(10): c.set(rnd.randint(0, 15), rnd.randint(0, 15), rnd.choice([S1, S3]))
save(c, 'tiles/wall_house.png', 'wall_house')

c = Canvas(16, 16)
c.rect(0, 0, 16, 16, H2)
for _ in range(34):
    x, y = rnd.randint(0, 15), rnd.randint(0, 15)
    for dx, dy, col in ((0, 0, H3), (1, 0, H3), (0, 1, H1), (1, 1, H1)): c.set((x + dx) % 16, (y + dy) % 16, col)
save(c, 'tiles/wall_hedge.png', 'wall_hedge')

def arrow(col, edge):          # arrow pointing RIGHT
    a = Canvas(16, 16)
    a.rect(1, 7, 6, 2, col)
    for i in range(4): a.rect(6 + i, 4 + i, 1, 8 - 2 * i, col)
    a.outline(edge)
    return a

def rot_cw(c):
    n = Canvas(c.w, c.h)
    for y in range(c.h):
        for x in range(c.w): n.px[x][c.h - 1 - y] = c.px[y][x]
    return n

def rot_ccw(c):
    n = Canvas(c.w, c.h)
    for y in range(c.h):
        for x in range(c.w): n.px[c.w - 1 - x][y] = c.px[y][x]
    return n

rnd2 = random.Random(99)

# ---------------- bathroom floor tile ----------------
c = Canvas(16, 16)
c.rect(0, 0, 16, 16, (158, 194, 214))
for (x, y) in [(0, 0), (8, 8)]: c.rect(x, y, 8, 8, (176, 210, 228))
for (x, y, w, h) in [(0, 0, 16, 1), (0, 8, 16, 1), (0, 0, 1, 16), (8, 0, 1, 16)]: c.rect(x, y, w, h, (116, 148, 172))
for (x, y) in [(1, 1), (9, 1), (1, 9), (9, 9)]: c.set(x, y, WHITE)
for _ in range(6): c.set(rnd2.randint(0, 15), rnd2.randint(0, 15), (140, 174, 196))
save(c, 'tiles/floor_bathroom.png', 'floor_bathroom')

# ---------------- DOORS (a doorway in a wall) and EXITS (glowing green arrow) ----------------
GREEN, GREEN_EDGE = (120, 255, 160), (18, 84, 48)

def door_tile(vertical, green):
    c = Canvas(16, 16)
    if not vertical:                                   # door in a top/bottom wall: posts left + right
        if green: dither(c, 3, 0, 10, 16, (26, 70, 46), (20, 56, 38))
        else:
            for p in range(4):
                c.rect(3, p * 4, 10, 4, W2); c.rect(3, p * 4, 10, 1, W3); c.rect(3, p * 4 + 3, 10, 1, W1)
        c.rect(0, 0, 3, 16, W1); c.rect(1, 0, 1, 16, W3); c.rect(13, 0, 3, 16, W1); c.rect(14, 0, 1, 16, W3)
    else:                                              # door in a left/right wall: posts top + bottom
        if green: dither(c, 0, 3, 16, 10, (26, 70, 46), (20, 56, 38))
        else:
            c.rect(0, 3, 16, 10, W2); c.rect(0, 3, 16, 1, W3); c.rect(0, 7, 16, 1, W1); c.rect(0, 8, 16, 1, W3); c.rect(0, 12, 16, 1, W1)
        c.rect(0, 0, 16, 3, W1); c.rect(0, 1, 16, 1, W3); c.rect(0, 13, 16, 3, W1); c.rect(0, 14, 16, 1, W3)
    return c

save(door_tile(False, False), 'doors/door_ns.png', 'door_ns')
save(door_tile(True, False), 'doors/door_ew.png', 'door_ew')

def exit_sprite(direction):
    c = door_tile(direction in ('E', 'W'), True)
    a = arrow(GREEN, GREEN_EDGE)
    if direction == 'E': c.paste(a, 3, 0)
    elif direction == 'W': c.paste(flip(a), -2, 0)
    elif direction == 'S': c.paste(rot_cw(a), 0, 3)
    else: c.paste(rot_ccw(a), 0, -2)
    return c

save(exit_sprite('N'), 'exits/exit_north.png', 'exit_north')
save(exit_sprite('S'), 'exits/exit_south.png', 'exit_south')
save(exit_sprite('E'), 'exits/exit_east.png', 'exit_east')
save(exit_sprite('W'), 'exits/exit_west.png', 'exit_west')

# ---------------- FURNITURE (native size = tiles x 16) ----------------
def make_bed(b1, b2, b3):                              # bed 2x3 tiles
    c = Canvas(32, 48)
    block(c, 0, 0, 32, 48, W3, W2, W1)
    block(c, 0, 0, 32, 9, W3, W2, W1)
    block(c, 2, 9, 28, 35, (226, 230, 244), (198, 206, 228), (150, 160, 196))
    block(c, 4, 11, 11, 7, WHITE, (226, 228, 244), (170, 176, 204)); block(c, 17, 11, 11, 7, WHITE, (226, 228, 244), (170, 176, 204))
    block(c, 2, 21, 28, 23, b3, b2, b1)
    for y in (26, 31, 36): c.rect(3, y, 26, 1, b1); c.rect(3, y + 1, 26, 1, b3)
    dither(c, 3, 41, 26, 2, b2, b1)
    block(c, 0, 44, 32, 4, W3, W2, W1)
    return c
save(make_bed(B1, B2, B3), 'furniture/bed.png', 'bed')
save(make_bed((52, 30, 84), (100, 56, 148), (150, 104, 196)), 'furniture/bed_purple.png', 'bed_purple')

c = Canvas(32, 16)                                   # wardrobe
block(c, 0, 0, 32, 16, W3, W2, W1)
c.rect(1, 1, 30, 7, (150, 100, 60)); c.rect(1, 1, 30, 1, W4); c.rect(1, 7, 30, 1, W2)
block(c, 2, 9, 13, 6, W3, W2, W1); block(c, 17, 9, 13, 6, W3, W2, W1)
c.set(13, 11, Y3); c.set(13, 12, Y2); c.set(18, 11, Y3); c.set(18, 12, Y2)
save(c, 'furniture/wardrobe.png', 'wardrobe')

c = Canvas(32, 16)                                   # desk
block(c, 0, 0, 32, 16, W3, W2, W1)
c.rect(1, 1, 30, 9, (150, 100, 60)); c.rect(1, 1, 30, 1, W4)
for dx, dy in [(0, 0), (1, 0), (-1, 0), (0, 1), (0, -1)]: c.set(7 + dx, 5 + dy, Y3 if dx == 0 and dy == 0 else Y2)
c.set(7, 7, K); c.set(7, 6, W1)
c.rect(20, 3, 7, 5, WHITE); c.rect(20, 3, 7, 1, (200, 200, 220)); c.rect(21, 5, 5, 1, (150, 150, 180))
block(c, 2, 11, 13, 4, W3, W2, W1); block(c, 17, 11, 13, 4, W3, W2, W1)
c.set(8, 13, Y2); c.set(23, 13, Y2)
save(c, 'furniture/desk.png', 'desk')

c = Canvas(32, 16)                                   # bookshelf
block(c, 0, 0, 32, 16, W3, W2, W1)
c.rect(1, 1, 30, 6, (150, 100, 60)); c.rect(1, 1, 30, 1, W4)
c.rect(1, 8, 30, 7, (36, 26, 36))
x = 2
bookcols = [(150, 50, 56), (60, 92, 150), (84, 130, 76), (190, 150, 70), (116, 74, 130)]
while x < 29:
    w, h = rnd.randint(2, 3), rnd.randint(4, 6)
    col = rnd.choice(bookcols)
    c.rect(x, 14 - h, w, h, col); c.set(x, 14 - h, WHITE); c.rect(x, 14 - h + 1, 1, h - 1, tuple(min(255, v + 40) for v in col))
    x += w
save(c, 'furniture/bookshelf.png', 'bookshelf')

c = Canvas(32, 16)                                   # metal shelf
block(c, 0, 0, 32, 16, M3, M2, M1)
c.rect(1, 1, 30, 6, M3); c.rect(1, 1, 30, 1, M4)
c.rect(1, 8, 30, 7, (30, 34, 46))
x = 2
while x < 26:
    w, h = rnd.randint(5, 8), rnd.randint(4, 6)
    block(c, x, 14 - h, w, h, (176, 130, 84), (140, 100, 60), (100, 70, 42)); x += w + 1
save(c, 'furniture/shelf_metal.png', 'shelf_metal')

c = Canvas(32, 16)                                   # sofa
block(c, 0, 0, 32, 6, B2, B1, B1)
block(c, 5, 6, 22, 8, B3, B2, B1)
block(c, 0, 3, 5, 13, B3, B2, B1); block(c, 27, 3, 5, 13, B3, B2, B1)
c.rect(16, 7, 1, 6, B1); dither(c, 6, 12, 10, 1, B2, B1); dither(c, 17, 12, 9, 1, B2, B1)
c.set(2, 15, K); c.set(29, 15, K)
save(c, 'furniture/sofa.png', 'sofa')

c = Canvas(32, 32)                                   # table 2x2
block(c, 0, 0, 32, 32, W3, W2, W1)
c.rect(2, 2, 28, 28, (150, 100, 60)); c.rect(2, 2, 28, 1, W4)
for y in (9, 16, 23): c.rect(2, y, 28, 1, W2)
for dx, dy in [(0, 0), (1, 0), (-1, 0), (0, 1), (0, -1)]: c.set(16 + dx, 16 + dy, Y3 if dx == 0 and dy == 0 else Y2)
c.ellipse(8, 9, 3.2, 3.2, WHITE); c.ellipse(8, 9, 1.6, 1.6, (200, 200, 222)); c.ellipse(24, 23, 3.2, 3.2, WHITE); c.ellipse(24, 23, 1.6, 1.6, (200, 200, 222))
save(c, 'furniture/table.png', 'table')

c = Canvas(32, 16)                                   # kitchen counter
block(c, 0, 0, 32, 16, (86, 64, 54), (64, 46, 40), (42, 30, 28))
c.rect(1, 1, 30, 9, (202, 200, 194)); c.rect(1, 1, 30, 1, WHITE); c.rect(1, 9, 30, 1, (160, 158, 154))
block(c, 20, 2, 9, 6, M3, M2, M1); c.set(24, 2, M4); c.set(24, 3, M4)
block(c, 2, 11, 13, 4, W3, W2, W1); block(c, 17, 11, 13, 4, W3, W2, W1)
c.set(8, 13, Y2); c.set(23, 13, Y2)
save(c, 'furniture/counter.png', 'counter')

c = Canvas(16, 16)                                   # fridge
block(c, 0, 0, 16, 16, M4, M3, M2)
c.rect(1, 1, 14, 8, (222, 228, 238)); c.rect(1, 9, 14, 1, M2)
c.rect(12, 3, 1, 4, M1); c.rect(12, 11, 1, 4, M1)
save(c, 'furniture/fridge.png', 'fridge')

c = Canvas(16, 16)                                   # plant
c.rect(5, 11, 6, 4, (150, 80, 52)); c.rect(4, 10, 8, 2, (190, 108, 66)); c.rect(5, 14, 6, 1, (104, 52, 38)); c.rect(5, 10, 6, 1, (60, 40, 32))
for (cx, cy, rx, ry) in [(8, 5, 2.6, 5), (4.5, 7.5, 3.4, 2.4), (11.5, 7.5, 3.4, 2.4), (6, 3.5, 2.2, 3.4), (10, 3.5, 2.2, 3.4)]:
    c.shaded(cx, cy, rx, ry, PL3, PL2, PL1)
c.outline(K)
save(c, 'furniture/plant.png', 'plant')

c = Canvas(16, 16)                                   # crate
block(c, 0, 0, 16, 16, (186, 140, 84), (148, 104, 58), (102, 70, 40))
c.rect(1, 5, 14, 1, (102, 70, 40)); c.rect(1, 10, 14, 1, (102, 70, 40))
for i in range(1, 14): c.set(i, i, (110, 76, 44)); c.set(15 - i, i, (110, 76, 44))
save(c, 'furniture/crate.png', 'crate')

c = Canvas(48, 32)                                   # rug 3x2
c.rect(0, 0, 48, 32, R1); c.rect(2, 2, 44, 28, Y2); c.rect(3, 3, 42, 26, R2)
dither(c, 4, 4, 40, 1, R3, R2); dither(c, 4, 27, 40, 1, R3, R2)
for y in range(32):
    for x in range(48):
        d = abs(x + .5 - 24) / 13.0 + abs(y + .5 - 16) / 9.0
        if d <= 1.0 and d > 0.78: c.set(x, y, Y2)
        elif d <= 0.78 and d > 0.55: c.set(x, y, R3)
        elif d <= 0.55 and d > 0.3: c.set(x, y, Y1)
        elif d <= 0.3: c.set(x, y, Y3)
c.rect(0, 0, 48, 1, R1); c.rect(0, 31, 48, 1, R1)
save(c, 'furniture/rug.png', 'rug')

# ---------------- MORE FURNITURE (bathroom, living room, electrical room, garage) ----------------
c = Canvas(16, 16)                                   # nightstand with a lamp
block(c, 1, 6, 14, 10, W3, W2, W1)
c.rect(2, 7, 12, 3, (150, 100, 60)); c.rect(2, 7, 12, 1, W4); c.rect(3, 12, 10, 1, W1); c.set(8, 14, Y2)
c.rect(8, 4, 1, 3, K); c.rect(6, 1, 5, 3, Y3); c.rect(5, 3, 7, 1, Y2)
save(c, 'furniture/nightstand.png', 'nightstand')

c = Canvas(16, 16)                                   # toilet
c.rect(4, 1, 8, 4, (220, 224, 238)); c.rect(4, 1, 8, 1, WHITE); c.rect(5, 3, 6, 1, (190, 196, 216))
c.shaded(8, 10, 5, 5.4, WHITE, (222, 226, 240), (176, 184, 206)); c.ellipse(8, 10.5, 3, 3.4, (176, 200, 224))
c.outline(K); save(c, 'furniture/toilet.png', 'toilet')

c = Canvas(16, 32)                                   # bathtub (1x2 tiles)
block(c, 0, 0, 16, 32, WHITE, (214, 220, 236), (170, 178, 204))
c.rect(2, 3, 12, 26, (170, 212, 238)); c.rect(2, 3, 12, 1, (120, 168, 206)); c.rect(3, 5, 1, 10, WHITE)
c.rect(7, 1, 2, 2, M3); c.set(7, 1, M4)
save(c, 'furniture/bathtub.png', 'bathtub')

c = Canvas(16, 16)                                   # sink with mirror
block(c, 3, 0, 10, 5, M4, M3, M2); c.rect(4, 1, 8, 3, (150, 190, 220))
block(c, 0, 5, 16, 11, W3, W2, W1)
c.rect(1, 6, 14, 6, (214, 220, 232)); c.rect(1, 6, 14, 1, WHITE)
c.ellipse(8, 8.5, 4.5, 2.5, (150, 184, 212)); c.set(8, 7, M4); c.set(8, 14, Y2)
save(c, 'furniture/sink.png', 'sink')

c = Canvas(32, 16)                                   # TV on a stand
block(c, 0, 8, 32, 8, W3, W2, W1); c.rect(15, 9, 1, 6, W1); c.set(8, 12, Y2); c.set(23, 12, Y2)
block(c, 6, 0, 20, 9, M2, M1, K)
c.rect(7, 1, 18, 6, (18, 22, 36)); c.rect(8, 2, 3, 1, (60, 70, 100)); c.rect(8, 3, 2, 1, (60, 70, 100))
save(c, 'furniture/tv_stand.png', 'tv_stand')

c = Canvas(32, 16)                                   # electrical panel
block(c, 0, 0, 32, 16, M3, M2, M1)
c.rect(2, 2, 28, 12, M2); c.rect(2, 2, 28, 1, M3)
c.rect(4, 3, 8, 9, Y2); c.rect(4, 3, 8, 1, Y3)
for (x, y) in [(8, 4), (7, 5), (8, 6), (7, 7), (8, 8), (7, 9)]: c.set(x, y, K)
for i, col in enumerate([(220, 50, 50), (70, 220, 100), (240, 200, 60)]): c.rect(16 + i * 4, 4, 2, 2, col)
c.rect(16, 8, 12, 3, M1); c.rect(17, 9, 3, 1, M4)
save(c, 'furniture/electrical_panel.png', 'electrical_panel')

c = Canvas(32, 48)                                   # car (2x3 tiles)
c.rect(3, 2, 26, 44, (44, 62, 124))
for (x, y) in [(3, 2), (28, 2), (3, 45), (28, 45)]: c.px[y][x] = None
c.rect(4, 3, 24, 1, (80, 106, 176))
c.rect(6, 10, 20, 7, (130, 170, 214)); c.rect(6, 10, 20, 1, WHITE)
c.rect(6, 18, 20, 12, (60, 84, 156)); c.rect(7, 19, 10, 1, (110, 136, 196))
c.rect(6, 31, 20, 6, (104, 146, 196)); c.rect(6, 39, 20, 5, (36, 50, 100))
c.rect(5, 2, 6, 2, (255, 238, 160)); c.rect(21, 2, 6, 2, (255, 238, 160))
c.rect(5, 44, 5, 2, (220, 50, 50)); c.rect(22, 44, 5, 2, (220, 50, 50))
for (x, y) in [(0, 8), (29, 8), (0, 32), (29, 32)]: c.rect(x, y, 3, 8, (24, 22, 30))
c.rect(2, 14, 2, 2, (30, 42, 88)); c.rect(28, 14, 2, 2, (30, 42, 88))
c.outline(K); save(c, 'furniture/car.png', 'car')

c = Canvas(80, 32)                                   # long hallway runner rug (5x2 tiles)
c.rect(0, 0, 80, 32, R1); c.rect(2, 2, 76, 28, Y2); c.rect(3, 3, 74, 26, R2)
dither(c, 4, 4, 72, 1, R3, R2); dither(c, 4, 27, 72, 1, R3, R2)
for cx in (14, 40, 66):
    for y in range(32):
        for x in range(80):
            d = abs(x + .5 - cx) / 10.0 + abs(y + .5 - 16) / 9.0
            if 0.78 < d <= 1.0: c.set(x, y, Y2)
            elif 0.55 < d <= 0.78: c.set(x, y, R3)
            elif 0.3 < d <= 0.55: c.set(x, y, Y1)
            elif d <= 0.3: c.set(x, y, Y3)
c.rect(0, 0, 80, 1, R1); c.rect(0, 31, 80, 1, R1)
save(c, 'furniture/rug_long.png', 'rug_long')

# ---------------- CHARACTERS 16x16 ----------------
J1, J2, J3 = B1, B2, B3

def legs_and_shoes(c):
    c.rect(5, 11, 2, 2, PANTS); c.rect(9, 11, 2, 2, PANTS)
    c.rect(4, 13, 3, 1, SHOE); c.rect(9, 13, 3, 1, SHOE)

def jacket(c):
    c.rect(4, 7, 8, 5, J2); c.rect(4, 7, 8, 1, J3); c.rect(4, 11, 8, 1, J1)
    c.rect(3, 8, 1, 4, J1); c.rect(12, 8, 1, 4, J1)
    c.set(3, 12, SK2); c.set(12, 12, SK2)

c = Canvas(16, 16)                                   # facing DOWN
legs_and_shoes(c); jacket(c)
c.rect(8, 8, 1, 3, J1)
c.shaded(8, 5, 4.4, 4.4, HB3, HB2, HB1)
c.rect(4, 7, 1, 4, HB1); c.rect(11, 7, 1, 4, HB1)
c.rect(5, 5, 6, 4, SK2); c.rect(5, 8, 6, 1, SK1); c.rect(7, 9, 2, 1, SK1)
c.set(6, 6, EYE); c.set(9, 6, EYE); c.set(6, 7, EYE); c.set(9, 7, EYE) if False else None
c.outline(K); save(c, 'player_down.png', 'player_down')

c = Canvas(16, 16)                                   # facing UP
legs_and_shoes(c); jacket(c)
c.rect(5, 8, 6, 3, (62, 46, 76)); c.rect(5, 8, 6, 1, (92, 72, 108)); c.rect(5, 10, 6, 1, (42, 30, 54))
c.shaded(8, 5, 4.4, 4.4, HB3, HB2, HB1)
c.rect(5, 8, 6, 3, HB2); c.rect(5, 10, 6, 1, HB1); c.rect(5, 8, 1, 3, HB1); c.rect(10, 8, 1, 3, HB1)
c.outline(K); save(c, 'player_up.png', 'player_up')

c = Canvas(16, 16)                                   # facing LEFT
c.rect(6, 11, 4, 2, PANTS); c.rect(4, 13, 5, 1, SHOE)
c.rect(5, 7, 6, 5, J2); c.rect(5, 7, 6, 1, J3); c.rect(5, 11, 6, 1, J1)
c.rect(10, 8, 2, 4, (62, 46, 76))
c.rect(5, 8, 2, 4, J1); c.set(5, 12, SK2)
c.rect(10, 5, 2, 5, HB1)
c.shaded(8, 5, 4.2, 4.4, HB3, HB2, HB1)
c.rect(4, 5, 3, 4, SK2); c.rect(4, 8, 3, 1, SK1); c.set(5, 6, EYE)
c.outline(K); save(c, 'player_left.png', 'player_left')
save(flip(assets['player_left']), 'player_right.png', 'player_right')

AURA, GLOW = (168, 34, 58), (255, 190, 84)           # caretaker
cc = Canvas(16, 16)
C1, C2, C3 = (28, 22, 38), (52, 42, 68), (82, 70, 102)
cc.rect(4, 8, 8, 3, C2); cc.rect(3, 11, 10, 3, C2); cc.rect(3, 13, 10, 1, C1); cc.rect(4, 8, 8, 1, C3); cc.rect(8, 9, 1, 5, C1)
cc.rect(2, 9, 2, 2, C2); cc.rect(1, 11, 3, 3, (255, 170, 60)); cc.set(2, 12, (255, 236, 160)); cc.set(1, 11, (255, 210, 110))
cc.rect(4, 6, 1, 6, (70, 70, 90)); cc.rect(11, 6, 1, 6, (70, 70, 90))
cc.shaded(8, 5, 4.4, 4.4, (170, 170, 188), (118, 118, 138), (74, 74, 96))
cc.rect(5, 5, 6, 4, (228, 230, 244)); cc.rect(5, 8, 6, 1, (178, 182, 208))
cc.set(6, 6, (255, 64, 72)); cc.set(9, 6, (255, 64, 72)); cc.set(7, 8, (150, 90, 110)); cc.set(8, 8, (150, 90, 110))
cc.outline(K)
car = Canvas(16, 16)
for y in range(16):
    for x in range(16):
        r = math.hypot(x + .5 - 8, y + .5 - 8)
        if 6.4 <= r <= 8.2 and (x + y) % 2 == 0: car.set(x, y, AURA)
        lr = math.hypot(x + .5 - 2.5, y + .5 - 12.5)
        if 2.2 <= lr <= 3.6 and (x + y) % 2 == 0: car.set(x, y, GLOW)
car.paste(cc, 0, 0)
save(car, 'caretaker.png', 'caretaker')

# ---------------- PREVIEW (2x zoom of everything) ----------------
sheet = Canvas(1000, 1000)
sheet.rect(0, 0, 1000, 1000, (24, 24, 34))
x, y, rowh = 10, 10, 0
for name, cv in assets.items():
    w, h = cv.w * 2, cv.h * 2
    if x + w > 990: x, y, rowh = 10, y + rowh + 10, 0
    sheet.paste(cv, x, y, 2); x += w + 10; rowh = max(rowh, h)
sheet.save('preview.png')
print('ok')
