from PIL import Image, ImageDraw, ImageFilter
import math

SIZE = 512
RADIUS = int(SIZE * 0.30)  # 30% corner radius

# Create base image (RGBA)
img = Image.new("RGBA", (SIZE, SIZE), (0, 0, 0, 0))
draw = ImageDraw.Draw(img)

# --- Background gradient (top-left dark to bottom-right slightly lighter) ---
bg = Image.new("RGBA", (SIZE, SIZE))
bg_draw = ImageDraw.Draw(bg)
for y in range(SIZE):
    t = y / SIZE
    r = int(15 + (28 - 15) * t)
    g = int(15 + (28 - 15) * t)
    b = int(26 + (46 - 26) * t)
    bg_draw.line([(0, y), (SIZE, y)], fill=(r, g, b, 255))
img.paste(bg, (0, 0))

draw = ImageDraw.Draw(img)

# --- Purple glow blob (subtle radial, left-center area) ---
glow = Image.new("RGBA", (SIZE, SIZE), (0, 0, 0, 0))
glow_draw = ImageDraw.Draw(glow)
gx, gy = int(SIZE * 0.22), int(SIZE * 0.52)
for step in range(120, 0, -1):
    alpha = int(60 * (step / 120) ** 2)
    glow_draw.ellipse(
        [gx - step * 2, gy - step * 1.5, gx + step * 2, gy + step * 1.5],
        fill=(124, 92, 252, alpha),
    )
img = Image.alpha_composite(img, glow)
draw = ImageDraw.Draw(img)

# --- Card stack: 3 rounded rectangles, stacked with slight offsets/rotations ---
CX, CY = SIZE // 2, SIZE // 2
card_w = int(SIZE * 0.44)
card_h = int(SIZE * 0.32)
card_r = 18  # card corner radius

def draw_rounded_rect_rotated(canvas, cx, cy, w, h, r, angle_deg, fill, outline=None, outline_width=0):
    """Draw a filled rounded rectangle centered at (cx,cy) rotated by angle_deg."""
    # Draw onto a temp image then rotate and composite
    tmp = Image.new("RGBA", (SIZE * 2, SIZE * 2), (0, 0, 0, 0))
    td = ImageDraw.Draw(tmp)
    ox, oy = SIZE, SIZE  # center of tmp
    x0, y0 = ox - w // 2, oy - h // 2
    x1, y1 = ox + w // 2, oy + h // 2
    td.rounded_rectangle([x0, y0, x1, y1], radius=r, fill=fill)
    if outline and outline_width > 0:
        td.rounded_rectangle([x0, y0, x1, y1], radius=r, outline=outline, width=outline_width)
    rotated = tmp.rotate(-angle_deg, center=(SIZE, SIZE), resample=Image.BICUBIC)
    # Crop back to SIZE x SIZE shifted to (cx, cy)
    shift_x = cx - SIZE
    shift_y = cy - SIZE
    cropped = rotated.crop((-shift_x, -shift_y, SIZE - shift_x, SIZE - shift_y))
    canvas.alpha_composite(cropped)

# Back card (darkest, most rotated right)
draw_rounded_rect_rotated(img, CX + 14, CY + 10, card_w, card_h, card_r,
                           angle_deg=8,
                           fill=(200, 195, 255, 160))

# Middle card (medium, slight right rotation)
draw_rounded_rect_rotated(img, CX + 6, CY + 2, card_w, card_h, card_r,
                           angle_deg=3,
                           fill=(220, 217, 255, 200))

# Front card (brightest white, straight)
draw_rounded_rect_rotated(img, CX, CY - 4, card_w, card_h, card_r,
                           angle_deg=0,
                           fill=(255, 255, 255, 245))

# --- Accent: purple vertical stripe on left edge of front card ---
stripe_x = CX - card_w // 2
stripe_y = CY - card_h // 2 - 4
stripe_h = card_h
stripe_w = 16
stripe_r = 8

stripe = Image.new("RGBA", (SIZE, SIZE), (0, 0, 0, 0))
sd = ImageDraw.Draw(stripe)
# Gradient stripe: brighter at top
for i in range(stripe_h):
    t = i / stripe_h
    r = int(124 + (180 - 124) * (1 - t))
    g = int(92 + (80 - 92) * (1 - t))
    b = int(252)
    a = 255
    sd.rectangle([stripe_x, stripe_y + i, stripe_x + stripe_w, stripe_y + i], fill=(r, g, b, a))

# Mask stripe to rounded rect shape
mask_s = Image.new("L", (SIZE, SIZE), 0)
md = ImageDraw.Draw(mask_s)
md.rounded_rectangle(
    [stripe_x, stripe_y, stripe_x + stripe_w, stripe_y + stripe_h],
    radius=stripe_r, fill=255
)
stripe.putalpha(mask_s)
img = Image.alpha_composite(img, stripe)

# --- Three subtle horizontal lines on front card (suggest content/text lines) ---
draw = ImageDraw.Draw(img)
lx0 = CX - card_w // 2 + stripe_w + 16
ly_base = CY - 4
line_lengths = [int(card_w * 0.38), int(card_w * 0.30), int(card_w * 0.22)]
line_y_offsets = [-16, 0, 16]
for length, dy in zip(line_lengths, line_y_offsets):
    lx1 = lx0 + length
    ly = ly_base + dy
    draw.rounded_rectangle(
        [lx0, ly - 4, lx1, ly + 4],
        radius=4,
        fill=(180, 170, 230, 200),
    )

# --- Apply 30% corner radius mask to entire image ---
mask = Image.new("L", (SIZE, SIZE), 0)
mask_draw = ImageDraw.Draw(mask)
mask_draw.rounded_rectangle([0, 0, SIZE - 1, SIZE - 1], radius=RADIUS, fill=255)
img.putalpha(mask)

# --- Save ---
out_path = "/home/hermespi/flashdrive/app_icon.png"
img.save(out_path, "PNG", optimize=True)

from pathlib import Path
size_kb = Path(out_path).stat().st_size / 1024
print(f"Saved: {out_path}")
print(f"Size: {img.size}, Mode: {img.mode}")
print(f"File size: {size_kb:.1f} KB ({'OK' if size_kb < 1024 else 'TOO LARGE'})")
