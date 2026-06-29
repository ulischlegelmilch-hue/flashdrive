#!/usr/bin/env python3
"""Professional Karteikarten Screenshot Generator v2.2 — QC Fixed"""

from PIL import Image, ImageDraw, ImageFont, ImageFilter
import os, math

W, H = 1080, 2400
OUT = os.path.expanduser("~/flashdrive/screenshots")
os.makedirs(OUT, exist_ok=True)

APP_NAME = "FlashMind"

BG      = (0x0F, 0x0F, 0x1A)
SURFACE = (0x1C, 0x1C, 0x2E)
CARD    = (0x25, 0x25, 0x40)
PURPLE  = (0x7C, 0x5C, 0xFC)
CYAN    = (0x00, 0xE5, 0xFF)
GREEN   = (0x34, 0xD3, 0x99)
RED     = (0xF8, 0x71, 0x71)
BRIGHT_RED = (0xFF, 0x4D, 0x4D)
ORANGE  = (0xFB, 0xBF, 0x24)
WHITE   = (0xF8, 0xFA, 0xFC)
GRAY    = (0x94, 0xA3, 0xB8)
DIM     = (0x64, 0x74, 0x8B)
DARK    = (0x33, 0x41, 0x55)
NAV_BG  = (0x14, 0x14, 0x22)

FONT_REG = "/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf"
FONT_BOLD = "/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf"

_inter_path = os.path.join(os.path.dirname(__file__), "fonts", "Inter.ttf")
if os.path.exists(_inter_path):
    try:
        ImageFont.truetype(_inter_path, 20)
        FONT_REG = _inter_path
        FONT_BOLD = _inter_path
    except:
        pass

def get_font(size, bold=False):
    path = FONT_BOLD if bold else FONT_REG
    if os.path.exists(path):
        try:
            return ImageFont.truetype(path, size)
        except:
            pass
    return ImageFont.load_default()

def font_r(size): return get_font(size, bold=False)
def font_b(size): return get_font(size, bold=True)

def text_width(draw, text, font):
    bbox = draw.textbbox((0, 0), text, font=font)
    return bbox[2] - bbox[0]

def text_height(draw, text, font):
    bbox = draw.textbbox((0, 0), text, font=font)
    return bbox[3] - bbox[1]

def fit_text(draw, text, max_w, base_size, bold=False):
    for size in range(base_size, 12, -2):
        f = get_font(size, bold)
        if text_width(draw, text, f) <= max_w:
            return f
    return get_font(12, bold)

def center_text(draw, y, text, font, fill=WHITE):
    w = text_width(draw, text, font)
    draw.text(((W - w) // 2, y), text, fill=fill, font=font)

def button_with_text(draw, xy, text, bg_color, text_color=WHITE, radius=20, font_size=30, bold=True):
    x0, y0, x1, y1 = xy
    for i in range(4):
        draw.rounded_rectangle([(x0 + 4 - i, y0 + 6 - i), (x1 + 4 + i, y1 + 6 + i)], radius=radius, fill=(0, 0, 0))
    draw.rounded_rectangle(xy, radius=radius, fill=bg_color)
    max_w = (x1 - x0) - 40
    f = fit_text(draw, text, max_w, font_size, bold)
    tw = text_width(draw, text, f)
    th = text_height(draw, text, f)
    tx = x0 + (x1 - x0 - tw) // 2
    ty = y0 + (y1 - y0 - th) // 2
    draw.text((tx, ty), text, fill=text_color, font=f)

def progress_bar(draw, xy, progress, radius=8, bg=DARK, fill_colors=(PURPLE, CYAN)):
    if len(xy) == 2:
        (x0, y0), (x1, y1) = xy
    else:
        x0, y0, x1, y1 = xy
    draw.rounded_rectangle(xy, radius=radius, fill=bg)
    bar_w = int((x1 - x0) * max(0, min(1, progress)))
    if bar_w > radius * 2:
        for i in range(bar_w):
            r = i / bar_w
            c = tuple(int(c1 + (c2 - c1) * r) for c1, c2 in zip(fill_colors[0], fill_colors[1]))
            draw.line([(x0 + i, y0), (x0 + i, y1 - 1)], fill=c)

def status_bar(draw):
    f = font_r(28)
    b = font_b(30)
    draw.text((50, 38), "14:31", fill=WHITE, font=b)
    bx, by = 920, 40
    draw.rounded_rectangle([(bx, by), (bx + 72, by + 32)], radius=6, outline=WHITE, width=2)
    draw.rectangle([(bx + 72, by + 9), (bx + 78, by + 23)], fill=WHITE)
    draw.rectangle([(bx + 5, by + 5), (bx + 63, by + 27)], fill=GREEN)

def draw_flame_icon(draw, cx, cy, size, color):
    w_half = int(size * 0.35)
    h_full = int(size * 0.6)
    outer_points = [
        (cx, cy - h_full),
        (cx + w_half, cy),
        (cx + int(w_half*0.6), cy + int(h_full*0.3)),
        (cx + int(w_half*0.3), cy + int(h_full*0.15)),
        (cx, cy + int(h_full*0.1)),
        (cx - int(w_half*0.3), cy + int(h_full*0.15)),
        (cx - int(w_half*0.6), cy + int(h_full*0.3)),
        (cx - w_half, cy),
    ]
    draw.polygon(outer_points, fill=color)
    inner_points = [
        (cx, cy - int(h_full*0.6)),
        (cx + int(w_half*0.5), cy - int(h_full*0.05)),
        (cx + int(w_half*0.3), cy + int(h_full*0.15)),
        (cx, cy + int(h_full*0.1)),
        (cx - int(w_half*0.3), cy + int(h_full*0.15)),
        (cx - int(w_half*0.5), cy - int(h_full*0.05)),
    ]
    inner_color = tuple(min(255, int(c * 1.3 + 40)) for c in color)
    draw.polygon(inner_points, fill=inner_color)
    core_points = [
        (cx, cy - int(h_full*0.35)),
        (cx + int(w_half*0.2), cy),
        (cx, cy + int(h_full*0.05)),
        (cx - int(w_half*0.2), cy),
    ]
    draw.polygon(core_points, fill=WHITE)

def draw_decks_icon(draw, cx, cy, size, color, bg_color=None):
    """Detailed stacked cards icon with gradients, shadows, and card lines."""
    card_w = int(size * 0.68)
    card_h = int(size * 0.16)
    gap = int(size * 0.05)
    radius = 4
    total_h = card_h * 3 + gap * 2
    top_y = cy - total_h // 2
    for i in range(3):
        # Gradient shade per card layer
        shade = 1.0 - i * 0.12
        c = tuple(min(255, int(255 * shade)) for _ in range(3))
        x_offset = i * int(size * 0.055)
        y_off = i * (card_h + gap)
        x0 = cx - card_w // 2 + x_offset
        x1 = cx + card_w // 2 - x_offset
        y0 = top_y + y_off
        y1 = y0 + card_h
        # Shadow
        draw.rounded_rectangle([(x0+2, y0+2), (x1+2, y1+2)], radius=radius, fill=(0, 0, 0))
        # Card body with vertical gradient
        for py in range(y1 - y0):
            r = py / max(1, y1 - y0)
            row_c = tuple(min(255, int(c[j] * (0.7 + 0.3 * r))) for j in range(3))
            draw.line([(x0, y0 + py), (x1, y0 + py)], fill=row_c)
        draw.rounded_rectangle([(x0, y0), (x1, y1)], radius=radius, outline=c, width=1)
        # Card header line (accent)
        header_h = max(2, int(card_h * 0.22))
        accent_c = tuple(min(255, int(v * 1.2)) for v in c)
        draw.rounded_rectangle([(x0+2, y0+2), (x1-2, y0+header_h)], radius=2, fill=accent_c)
        # Text lines on card
        lh = max(2, int(card_h * 0.14))
        for li in range(2):
            lx0 = x0 + 5
            lx1 = x1 - 5 - int(card_w * (0.45 if li == 1 else 0.05))
            ly = y0 + header_h + 3 + li * int(card_h * 0.38)
            line_c = tuple(min(255, int(v * 0.7)) for v in c)
            draw.rectangle([(lx0, ly), (lx1, ly + lh)], fill=line_c)
        # Small dot indicator on last card
        if i == 2:
            dot_r = 3
            draw.ellipse([(x1-12, y1-10), (x1-12+dot_r*2, y1-10+dot_r*2)], fill=CYAN)

def draw_stats_icon(draw, cx, cy, size, color, bg_color=None):
    """Detailed bar chart icon with gradient bars, grid lines, and trend arrow."""
    bar_w = int(size * 0.12)
    max_h = int(size * 0.48)
    heights = [int(max_h * 0.40), int(max_h * 0.80), int(max_h * 0.60), int(max_h * 0.95)]
    spacing = int(size * 0.10)
    total_w = bar_w * 4 + spacing * 3
    left_x = cx - total_w // 2
    base_y = cy + max_h // 2
    # Grid lines (horizontal)
    for gl in range(4):
        gy = base_y - int(max_h * gl / 3)
        grid_c = (60, 60, 80)
        draw.line([(left_x - 5, gy), (left_x + total_w + 5, gy)], fill=grid_c)
    # Base line
    draw.line([(left_x - 5, base_y), (left_x + total_w + 5, base_y)], fill=(80, 80, 100), width=2)
    for i, h in enumerate(heights):
        x0 = left_x + i * (bar_w + spacing)
        x1 = x0 + bar_w
        y0 = base_y - h
        # Bar with gradient
        for py in range(h):
            r = py / max(h, 1)
            # Gradient from darker bottom to brighter top
            bar_c = tuple(min(255, int(255 * (0.5 + 0.5 * r))) for _ in range(3))
            draw.line([(x0, base_y - py), (x1, base_y - py)], fill=bar_c)
        # Bar outline
        draw.rounded_rectangle([(x0, y0), (x1, base_y)], radius=3, outline=color, width=1)
        # Highlight on top edge
        highlight_c = tuple(min(255, int(v * 1.3)) for v in (255, 255, 255))
        draw.line([(x0+1, y0), (x1-1, y0)], fill=highlight_c, width=1)
        # Value dot on top
        dot_r = 3
        draw.ellipse([(x0 + bar_w//2 - dot_r, y0 - dot_r - 2), (x0 + bar_w//2 + dot_r, y0 + dot_r - 2)], fill=CYAN)
    # Trend arrow (up-right)
    arrow_x = cx + int(size * 0.22)
    arrow_y = cy - int(size * 0.10)
    arrow_s = int(size * 0.12)
    # Arrow shaft
    draw.line([(arrow_x, arrow_y + arrow_s), (arrow_x + arrow_s, arrow_y)], fill=CYAN, width=3)
    # Arrow head
    draw.polygon([
        (arrow_x + arrow_s, arrow_y - 4),
        (arrow_x + arrow_s + 8, arrow_y + 6),
        (arrow_x + arrow_s - 6, arrow_y + 2),
    ], fill=CYAN)

def draw_settings_icon(draw, cx, cy, size, color, bg_color=None):
    """Detailed gear icon with gradient teeth, inner ring, and center bolt."""
    n = 8
    outer_r = int(size * 0.32)
    inner_r = int(size * 0.22)
    hole_r = int(size * 0.08)
    half_tooth = math.pi / n * 0.55
    step = 2 * math.pi / n
    # Outer gear teeth
    points = []
    for i in range(n):
        base = step * i
        a_v1 = base - step / 2 + half_tooth
        a_t1 = base - half_tooth
        a_t2 = base + half_tooth
        a_v2 = base + step / 2 - half_tooth
        points.append((cx + inner_r * math.cos(a_v1), cy + inner_r * math.sin(a_v1)))
        points.append((cx + outer_r * math.cos(a_t1), cy + outer_r * math.sin(a_t1)))
        points.append((cx + outer_r * math.cos(a_t2), cy + outer_r * math.sin(a_t2)))
        points.append((cx + inner_r * math.cos(a_v2), cy + inner_r * math.sin(a_v2)))
    draw.polygon(points, fill=color)
    # Inner ring (lighter shade)
    ring_c = tuple(min(255, int(v * 1.15)) for v in color)
    draw.ellipse([(cx - inner_r, cy - inner_r), (cx + inner_r, cy + inner_r)], outline=ring_c, width=2)
    # Inner circle fill
    inner_fill = tuple(min(255, int(v * 0.85)) for v in color)
    draw.ellipse([(cx - inner_r + 3, cy - inner_r + 3), (cx + inner_r - 3, cy + inner_r - 3)], fill=inner_fill)
    # Center hole
    draw.ellipse([(cx - hole_r, cy - hole_r), (cx + hole_r, cy + hole_r)], fill=BG)
    # Center bolt (small circle in hole)
    bolt_r = hole_r // 2
    draw.ellipse([(cx - bolt_r, cy - bolt_r), (cx + bolt_r, cy + bolt_r)], fill=color)
    # Accent dots between teeth
    for i in range(4):
        angle = i * math.pi / 2 + math.pi / 4
        dot_x = cx + int(inner_r * 0.65 * math.cos(angle))
        dot_y = cy + int(inner_r * 0.65 * math.sin(angle))
        draw.ellipse([(dot_x-3, dot_y-3), (dot_x+3, dot_y+3)], fill=ring_c)
    # Small highlight arc on top-left
    draw.arc([(cx - outer_r + 5, cy - outer_r + 5), (cx + outer_r - 5, cy + outer_r - 5)],
             start=200, end=260, fill=ring_c, width=2)

def bottom_nav(draw, active=0):
    bar_h = 180
    safe_bottom = 50
    margin = 40
    bar_y = H - bar_h - safe_bottom - 20
    bar_x0 = margin
    bar_x1 = W - margin
    for i in range(12):
        draw.rounded_rectangle([(bar_x0 + 6 - i, bar_y + 8 - i), (bar_x1 + 6 + i, bar_y + bar_h + 8 + i)], radius=40, fill=(0, 0, 0))
    draw.rounded_rectangle([(bar_x0, bar_y), (bar_x1, bar_y + bar_h)], radius=36, fill=(0x1E, 0x1E, 0x32))
    draw.rounded_rectangle([(bar_x0, bar_y), (bar_x1, bar_y + bar_h)], radius=36, outline=(0x28, 0x28, 0x40), width=1)
    draw.rounded_rectangle([(W//2 - 80, H - 22), (W//2 + 80, H - 14)], radius=5, fill=DIM)
    items = [('Decks', 'decks'), ('Statistik', 'stats'), ('Einstellungen', 'settings')]
    tab_w = (bar_x1 - bar_x0) // 3
    icon_cy = bar_y + 62
    for i, (label, icon_type) in enumerate(items):
        cx = bar_x0 + tab_w * i + tab_w // 2
        is_active = (i == active)
        if is_active:
            for g in range(5):
                draw.ellipse([(cx - 50 + g, bar_y + 18 + g), (cx + 50 - g, bar_y + 118 - g)], fill=PURPLE)
            draw.ellipse([(cx - 42, bar_y + 22), (cx + 42, bar_y + 106)], fill=PURPLE)
            draw.ellipse([(cx - 30, bar_y + 30), (cx + 22, bar_y + 72)], fill=(0x9B, 0x7E, 0xFF))
            icon_color = WHITE
            label_color = PURPLE
            label_font = font_b(32)
        else:
            draw.ellipse([(cx - 42, bar_y + 22), (cx + 42, bar_y + 106)], outline=(0x3A, 0x3A, 0x5C), width=2)
            icon_color = GRAY
            label_color = GRAY
            label_font = font_r(30)
        # Draw the icon with WHITE color for active, GRAY for inactive
        if icon_type == 'decks':
            draw_decks_icon(draw, cx, icon_cy, 80, icon_color)
        elif icon_type == 'stats':
            draw_stats_icon(draw, cx, icon_cy, 80, icon_color)
        elif icon_type == 'settings':
            draw_settings_icon(draw, cx, icon_cy, 80, icon_color)
        lw = text_width(draw, label, label_font)
        draw.text((cx - lw//2, bar_y + 122), label, fill=label_color, font=label_font)

def tts_button(draw, cx, cy, size=110):
    for i in range(5):
        r = size // 2 + 6 - i
        draw.ellipse([(cx - r, cy - r), (cx + r, cy + r)], fill=(0, 0, 0))
    draw.ellipse([(cx - size//2, cy - size//2), (cx + size//2, cy + size//2)], fill=(*PURPLE, 180))
    highlight_x, highlight_y = cx - 18, cy - 20
    draw.ellipse([(highlight_x - 20, highlight_y - 20), (highlight_x + 20, highlight_y + 20)], fill=(255, 255, 255, 40))
    sx, sy = cx - 26, cy - 22
    draw.rectangle([(sx, sy + 10), (sx + 14, sy + 34)], fill=WHITE)
    draw.polygon([(sx + 14, sy + 6), (sx + 30, sy), (sx + 30, sy + 44), (sx + 14, sy + 38)], fill=WHITE)
    draw.arc([(sx + 32, sy + 12), (sx + 44, sy + 32)], start=-60, end=60, fill=WHITE, width=3)
    draw.arc([(sx + 38, sy + 6), (sx + 54, sy + 38)], start=-60, end=60, fill=WHITE, width=3)

def deck_card(draw, y, title, count, due, progress, accent):
    for i in range(5):
        draw.rounded_rectangle([(45 + 6 - i, y + 8 - i), (W - 45 + 6 + i, y + 200 + 8 + i)], radius=22, fill=(0, 0, 0))
    draw.rounded_rectangle([(45, y), (W - 45, y + 200)], radius=20, fill=CARD)
    for i in range(200):
        r = i / 200
        c = tuple(int(a + (b - a) * r) for a, b in zip(accent, CYAN))
        draw.line([(55, y + 10 + i), (63, y + 10 + i)], fill=c)
    # Book icon (drawn)
    bx, by = 85, y + 22
    bw, bh = 36, 44
    draw.rounded_rectangle([(bx, by), (bx + bw, by + bh)], radius=4, fill=accent)
    draw.rectangle([(bx + 4, by + 4), (bx + bw - 4, by + bh - 4)], fill=(*accent, 180))
    draw.line([(bx + bw//2, by + 2), (bx + bw//2, by + bh - 2)], fill=WHITE, width=2)
    for li in range(3):
        ly = by + 10 + li * 10
        draw.rectangle([(bx + 6, ly), (bx + bw//2 - 4, ly + 3)], fill=WHITE)
    draw.text((145, y + 22), title, fill=WHITE, font=font_b(42))
    draw.text((145, y + 72), f"{count}  \u2022  {due}", fill=GRAY, font=font_r(32))
    progress_bar(draw, (145, y + 130, W - 85, y + 150), progress)
    pct = f"{int(progress * 100)}%"
    pw = text_width(draw, pct, font_r(28))
    draw.text((W - 85 - pw, y + 100), pct, fill=CYAN, font=font_r(28))
    ax = W - 85
    ay = y + 30
    draw.polygon([(ax, ay), (ax + 18, ay + 18), (ax, ay + 36)], fill=DIM)

def top_bar(draw, title_text, subtitle=None, show_back=False, right_widget=None):
    draw.rectangle([(0, 0), (W, 170)], fill=NAV_BG)
    x_offset = 55
    if show_back:
        draw.polygon([(42, 75), (72, 45), (72, 65), (57, 65), (57, 85), (72, 85), (72, 105)], fill=WHITE)
        x_offset = 100
    draw.text((x_offset, 42), title_text, fill=WHITE, font=font_b(52))
    if subtitle:
        draw.text((x_offset, 105), subtitle, fill=GRAY, font=font_r(32))
    if right_widget == "streak":
        draw.rounded_rectangle([(730, 32), (W - 35, 108)], radius=34, fill=CARD)
        draw_flame_icon(draw, 770, 70, 36, ORANGE)
        draw.text((800, 44), "14", fill=ORANGE, font=font_b(38))
        draw.text((800, 84), "Tage", fill=GRAY, font=font_r(22))
    elif right_widget == "profile":
        draw.ellipse([(W - 130, 35), (W - 35, 130)], fill=CARD)
        draw.ellipse([(W - 115, 50), (W - 50, 115)], fill=PURPLE)
        draw.text((W - 97, 68), "U", fill=WHITE, font=font_b(44))
    elif right_widget == "car":
        draw.rounded_rectangle([(750, 32), (W - 35, 108)], radius=34, fill=CARD)
        cx2, cy2 = 840, 70
        draw.rounded_rectangle([(cx2 - 30, cy2 - 10), (cx2 + 30, cy2 + 15)], radius=6, fill=WHITE)
        draw.polygon([(cx2 - 30, cy2), (cx2 - 40, cy2 + 5), (cx2 - 40, cy2 + 15), (cx2 - 30, cy2 + 15)], fill=WHITE)
        draw.polygon([(cx2 + 30, cy2), (cx2 + 40, cy2 + 5), (cx2 + 40, cy2 + 15), (cx2 + 30, cy2 + 15)], fill=WHITE)
        draw.ellipse([(cx2 - 22, cy2 + 12), (cx2 - 10, cy2 + 24)], fill=NAV_BG)
        draw.ellipse([(cx2 + 10, cy2 + 12), (cx2 + 22, cy2 + 24)], fill=NAV_BG)
        draw.text((880, 46), "Auto", fill=GRAY, font=font_b(30))
    # Accent line
    draw.line([(0, 170), (W, 170)], fill=(0x7C, 0x5C, 0xFC))
    draw.line([(0, 171), (W, 171)], fill=(0x3E, 0x2E, 0x7E))

def screen_home():
    img = Image.new("RGB", (W, H), BG)
    draw = ImageDraw.Draw(img)
    top_bar(draw, APP_NAME, right_widget="streak")
    draw.text((55, 195), "Hallo Uli!", fill=WHITE, font=font_b(58))
    draw.text((55, 270), "Bereit zum Lernen?", fill=GRAY, font=font_r(36))
    decks = [
        ("Medizin KB", "45 Karten", "12 f\u00E4llig", 0.67, PURPLE),
        ("Rettungsdienst", "32 Karten", "5 f\u00E4llig", 0.84, GREEN),
        ("Pharmakologie", "78 Karten", "23 f\u00E4llig", 0.31, ORANGE),
    ]
    y = 350
    for title, count, due, progress, accent in decks:
        deck_card(draw, y, title, count, due, progress, accent)
        y += 230
    widget_y = y + 20
    for i in range(5):
        draw.rounded_rectangle([(45 + 6 - i, widget_y + 8 - i), (W - 45 + 6 + i, widget_y + 240 + 8 + i)], radius=22, fill=(0, 0, 0))
    draw.rounded_rectangle([(45, widget_y), (W - 45, widget_y + 240)], radius=20, fill=CARD)
    for i in range(240):
        r = i / 240
        c = tuple(int(a + (b - a) * r) for a, b in zip(ORANGE, PURPLE))
        draw.line([(55, widget_y + i), (63, widget_y + i)], fill=c)
    draw.text((90, widget_y + 22), "Heute f\u00E4llig", fill=WHITE, font=font_b(38))
    draw.text((90, widget_y + 75), "40", fill=ORANGE, font=font_b(84))
    draw.text((90, widget_y + 175), "Karten warten auf dich", fill=GRAY, font=font_r(28))
    button_with_text(draw, (W - 345, widget_y + 55, W - 60, widget_y + 175), "Jetzt lernen", PURPLE, radius=22, font_size=30)
    # FAB
    fx, fy = W - 200, H - 420
    for i in range(6):
        draw.ellipse([(fx + 8 - i, fy + 10 - i), (fx + 150 + 8 + i, fy + 150 + 10 + i)], fill=(0, 0, 0))
    draw.ellipse([(fx, fy), (fx + 150, fy + 150)], fill=PURPLE)
    draw.rectangle([(fx + 55, fy + 35), (fx + 95, fy + 115)], fill=WHITE)
    draw.rectangle([(fx + 35, fy + 55), (fx + 115, fy + 95)], fill=WHITE)
    bottom_nav(draw, active=0)
    status_bar(draw)
    img.save(f"{OUT}/01_home.png")
    print("\u2705 01_home.png")

def screen_study():
    img = Image.new("RGB", (W, H), BG)
    draw = ImageDraw.Draw(img)
    top_bar(draw, "Medizin KB", subtitle="3 / 12", show_back=True)
    progress_bar(draw, [(50, 190), (W - 50, 212)], 0.25, radius=10)
    cx, cy, cw, ch = 55, 720, W - 110, 800
    for i in range(8):
        draw.rounded_rectangle([(cx + 8 - i, cy + 10 - i), (cx + cw + 8 + i, cy + ch + 10 + i)], radius=28, fill=(0, 0, 0))
    draw.rounded_rectangle([(cx, cy), (cx + cw, cy + ch)], radius=26, fill=CARD)
    for i in range(6):
        r = i / 6
        c = tuple(int(a + (b - a) * r) for a, b in zip(PURPLE, CYAN))
        draw.ellipse([(cx + 30 + i * 40, cy + 25), (cx + 50 + i * 40, cy + 37)], fill=c)
    draw.text((cx + 50, cy + 55), "FRAGE", fill=PURPLE, font=font_b(28))
    draw.text((cx + 50, cy + 120), "Welche 3 Symptome", fill=WHITE, font=font_b(50))
    draw.text((cx + 50, cy + 195), "geh\u00F6ren zum Akuten", fill=WHITE, font=font_b(50))
    draw.text((cx + 50, cy + 270), "Koronarsyndrom?", fill=WHITE, font=font_b(50))
    draw.text((cx + 50, cy + 380), "Tippe auf die Karte zum Aufdecken", fill=DIM, font=font_r(28))
    tts_button(draw, cx + cw - 95, cy + ch - 95, size=120)
    btn_y = H - 310
    btn_h = 200
    gap = 30
    btn_w = (W - 80 - gap * 2) // 3
    button_with_text(draw, (40, btn_y, 40 + btn_w, btn_y + btn_h), "Wusst ich", GREEN, radius=28, font_size=38)
    bx2 = 40 + btn_w + gap
    button_with_text(draw, (bx2, btn_y, bx2 + btn_w, btn_y + btn_h), "Wiederholen", ORANGE, radius=28, font_size=38)
    bx3 = bx2 + btn_w + gap
    button_with_text(draw, (bx3, btn_y, bx3 + btn_w, btn_y + btn_h), "Nicht gewusst", BRIGHT_RED, radius=28, font_size=36)
    status_bar(draw)
    img.save(f"{OUT}/02_study.png")
    print("\u2705 02_study.png")

def screen_stats():
    img = Image.new("RGB", (W, H), BG)
    draw = ImageDraw.Draw(img)
    top_bar(draw, "Statistik", right_widget="profile")
    for i in range(6):
        draw.rounded_rectangle([(45 + 6 - i, 200 + 8 - i), (W - 45 + 6 + i, 390 + 8 + i)], radius=24, fill=(0, 0, 0))
    draw.rounded_rectangle([(45, 200), (W - 45, 390)], radius=22, fill=CARD)
    draw_flame_icon(draw, 80, 295, 48, ORANGE)
    draw.text((120, 225), "14 Tage Streak", fill=WHITE, font=font_b(48))
    draw.text((120, 300), "Rekord: 21 Tage", fill=GRAY, font=font_r(30))
    draw.text((750, 240), "Heute", fill=GREEN, font=font_b(32))
    draw.polygon([(755, 270), (770, 288), (800, 252), (788, 248), (770, 270), (758, 260)], fill=GREEN)
    stats = [("247", "Karten gesamt"), ("89", "Gemeistert"), ("92%", "Genauigkeit"), ("32", "Lernseinheiten")]
    for i, (val, label) in enumerate(stats):
        col, row = i % 2, i // 2
        x = 50 + col * 515
        y = 430 + row * 220
        draw.rounded_rectangle([(x, y), (x + 460, y + 190)], radius=18, fill=CARD)
        draw.text((x + 30, y + 30), val, fill=PURPLE, font=font_b(56))
        draw.text((x + 30, y + 115), label, fill=GRAY, font=font_r(28))
    draw.text((55, 900), "Aktivit\u00E4ts-Heatmap", fill=WHITE, font=font_b(40))
    draw.text((55, 948), "Deine t\u00E4glichen Wiederholungen \u2014 je dunkler gr\u00FCn, desto mehr gelernt", fill=DIM, font=font_r(24))
    import random
    random.seed(42)
    cell, gap = 20, 4
    hm_y = 995
    days = ["Mo", "Di", "Mi", "Do", "Fr", "Sa", "So"]
    for di, day in enumerate(days):
        draw.text((12, hm_y + di * (cell + gap) + 2), day, fill=GRAY, font=font_r(20))
    hm_x = 55
    for week in range(30):
        for day in range(7):
            x = hm_x + week * (cell + gap)
            y = hm_y + day * (cell + gap)
            intensity = random.random()
            if intensity > 0.8:
                c = GREEN
            elif intensity > 0.5:
                c = (0x2E, 0x8C, 0x3A)
            elif intensity > 0.25:
                c = (0x1B, 0x5E, 0x28)
            else:
                c = DARK
            draw.rounded_rectangle([(x, y), (x + cell - 1, y + cell - 1)], radius=4, fill=c)
    legend_x = 55
    legend_y = hm_y + 7 * (cell + gap) + 25
    draw.text((legend_x, legend_y), "Weniger", fill=DIM, font=font_r(22))
    for i, c in enumerate([DARK, (0x1B, 0x5E, 0x28), (0x2E, 0x8C, 0x3A), GREEN]):
        draw.rounded_rectangle([(legend_x + 120 + i * 40, legend_y + 2), (legend_x + 152 + i * 40, legend_y + 22)], radius=4, fill=c)
    draw.text((legend_x + 295, legend_y), "Mehr", fill=DIM, font=font_r(22))
    draw.rounded_rectangle([(55, legend_y + 50), (W - 55, legend_y + 180)], radius=18, fill=CARD)
    draw.text((85, legend_y + 70), "Diese Woche", fill=WHITE, font=font_b(32))
    draw.text((85, legend_y + 115), "5 von 7 Tagen gelernt \u2022 42 Karten wiederholt", fill=GRAY, font=font_r(28))
    draw.rounded_rectangle([(W - 280, legend_y + 70), (W - 85, legend_y + 130)], radius=25, fill=(*GREEN, 100))
    draw.text((W - 255, legend_y + 78), "71%", fill=WHITE, font=font_b(32))
    draw.rounded_rectangle([(55, legend_y + 200), (W - 55, legend_y + 330)], radius=18, fill=CARD)
    draw.text((85, legend_y + 220), "Dieser Monat", fill=WHITE, font=font_b(32))
    draw.text((85, legend_y + 265), "18 von 30 Tagen gelernt \u2022 156 Karten wiederholt", fill=GRAY, font=font_r(28))
    draw.rounded_rectangle([(W - 280, legend_y + 220), (W - 85, legend_y + 280)], radius=25, fill=(*ORANGE, 100))
    draw.text((W - 260, legend_y + 228), "60%", fill=WHITE, font=font_b(32))
    chart_y = legend_y + 360
    draw.text((55, chart_y), "Wochenverlauf", fill=WHITE, font=font_b(38))
    for i in range(5):
        draw.rounded_rectangle([(45 + 6 - i, chart_y + 55 + 8 - i), (W - 45 + 6 + i, chart_y + 310 + 8 + i)], radius=20, fill=(0, 0, 0))
    draw.rounded_rectangle([(45, chart_y + 55), (W - 45, chart_y + 310)], radius=18, fill=CARD)
    days_week = ["Mo", "Di", "Mi", "Do", "Fr", "Sa", "So"]
    day_values = [12, 8, 25, 15, 30, 5, 18]
    max_val = max(day_values)
    chart_inner_x = 70
    chart_inner_w = W - 140
    chart_inner_h = 180
    chart_inner_top = chart_y + 75
    bar_base = chart_inner_top + chart_inner_h
    slot_w = chart_inner_w // len(days_week)
    bar_w = int(slot_w * 0.55)
    for i, (day, val) in enumerate(zip(days_week, day_values)):
        bar_px = int(chart_inner_h * val / max_val)
        bx = chart_inner_x + i * slot_w + (slot_w - bar_w) // 2
        by = bar_base - bar_px
        bar_c = PURPLE if i == 4 else (CYAN if val == max_val else (0x3A, 0x3A, 0x6A))
        draw.rounded_rectangle([(bx, by), (bx + bar_w, bar_base)], radius=5, fill=bar_c)
        vw = text_width(draw, str(val), font_r(22))
        draw.text((bx + (bar_w - vw) // 2, by - 28), str(val), fill=GRAY, font=font_r(22))
        dw = text_width(draw, day, font_r(24))
        draw.text((bx + (bar_w - dw) // 2, bar_base + 8), day, fill=DIM, font=font_r(24))
    bottom_nav(draw, active=1)
    status_bar(draw)
    img.save(f"{OUT}/03_stats.png")
    print("\u2705 03_stats.png")

def screen_import():
    img = Image.new("RGB", (W, H), BG)
    draw = ImageDraw.Draw(img)
    top_bar(draw, "Importieren", show_back=True)
    draw.text((55, 200), "CSV", fill=PURPLE, font=font_b(40))
    draw.text((215, 200), "Anki", fill=GRAY, font=font_b(40))
    draw.text((380, 200), "StudySmarter", fill=GRAY, font=font_b(40))
    draw.line([(55, 256), (180, 256)], fill=PURPLE, width=4)
    for i in range(6):
        draw.rounded_rectangle([(55 + 6 - i, 310 + 8 - i), (W - 55 + 6 + i, 600 + 8 + i)], radius=24, fill=(0, 0, 0))
    draw.rounded_rectangle([(55, 310), (W - 55, 600)], radius=22, fill=CARD)
    draw.rounded_rectangle([(55, 310), (W - 55, 600)], radius=22, outline=PURPLE, width=3)
    fcx, fcy = 480, 420
    draw.rounded_rectangle([(fcx - 35, fcy - 45), (fcx + 35, fcy + 45)], radius=6, fill=(*PURPLE, 120), outline=PURPLE, width=2)
    draw.polygon([(fcx - 35, fcy - 15), (fcx - 10, fcy - 45), (fcx - 10, fcy - 15), (fcx - 35, fcy - 15)], fill=PURPLE)
    center_text(draw, 470, "CSV-Datei ausw\u00E4hlen", font_b(36), fill=WHITE)
    center_text(draw, 520, "StudySmarter, Anki, oder eigene CSV", font_r(28), fill=GRAY)
    draw.text((55, 660), "Spalten zuordnen", fill=WHITE, font=font_b(36))
    mappings = [("Vorderseite", "Spalte 1: Vorderseite"), ("R\u00FCckseite", "Spalte 2: R\u00FCckseite"), ("Tags", "Spalte 3: Tags")]
    y = 720
    for label, value in mappings:
        draw.rounded_rectangle([(55, y), (W - 55, y + 95)], radius=14, fill=CARD)
        draw.text((85, y + 26), label, fill=WHITE, font=font_r(30))
        vw = text_width(draw, value, font_r(28))
        draw.text((W - 85 - vw, y + 26), value, fill=CYAN, font=font_r(28))
        arr_x = W - 100
        arr_y = y + 38
        draw.polygon([(arr_x, arr_y), (arr_x + 12, arr_y), (arr_x + 6, arr_y + 10)], fill=DIM)
        y += 115
    draw.text((55, y + 10), "Vorschau (5 Karten)", fill=WHITE, font=font_b(34))
    y += 65
    draw.rounded_rectangle([(55, y), (W - 55, y + 260)], radius=16, fill=CARD)
    draw.text((80, y + 18), "Vorderseite", fill=PURPLE, font=font_b(26))
    draw.text((500, y + 18), "R\u00FCckseite", fill=PURPLE, font=font_b(26))
    draw.line([(55, y + 55), (W - 55, y + 55)], fill=DARK, width=1)
    rows = [
        ("Welche 3 Symptome...", "Brustschmerz, Dyspnoe..."),
        ("Welches Medikament...", "Aspirin 250mg IV"),
        ("ABCDE-Schema?", "Airway, Breathing..."),
        ("Reanimation?", "30:2, 100/min"),
        ("Schock-Typen?", "Kardiogen, hypovol..."),
    ]
    for i, (front, back) in enumerate(rows):
        ry = y + 68 + i * 40
        draw.text((80, ry), front[:28] + "...", fill=WHITE, font=font_r(24))
        draw.text((500, ry), back[:22] + "...", fill=GRAY, font=font_r(24))
    button_with_text(draw, (55, H - 350, W - 55, H - 200), "47 Karten importieren", PURPLE, radius=28, font_size=36)
    bottom_nav(draw, active=0)
    status_bar(draw)
    img.save(f"{OUT}/04_import.png")
    print("\u2705 04_import.png")

def screen_auto():
    img = Image.new("RGB", (W, H), BG)
    draw = ImageDraw.Draw(img)
    top_bar(draw, APP_NAME, right_widget="car")
    draw.text((55, 195), "W\u00E4hle einen Stapel", fill=WHITE, font=font_b(48))
    draw.text((55, 265), "Sprache oder Tippen zum Ausw\u00E4hlen", fill=GRAY, font=font_r(32))
    items = [
        ("Medizin KB", "12 Karten f\u00E4llig", GREEN),
        ("Rettungsdienst", "5 Karten f\u00E4llig", PURPLE),
        ("Pharmakologie", "23 Karten f\u00E4llig", ORANGE),
    ]
    y = 350
    for title, subtitle, accent in items:
        deck_card(draw, y, title, subtitle, "", 0.0, accent)
        y += 230
    center_text(draw, H - 400, "\U0001F3A4  Wiederhole Medizin KB", font_r(32), fill=CYAN)
    center_text(draw, H - 350, "Sag den Namen des Stapels", font_r(26), fill=DIM)
    bottom_nav(draw, active=0)
    status_bar(draw)
    img.save(f"{OUT}/05_android_auto.png")
    print("\u2705 05_android_auto.png")

def screen_settings():
    img = Image.new("RGB", (W, H), BG)
    draw = ImageDraw.Draw(img)
    top_bar(draw, "Einstellungen", show_back=True)

    y = 200

    # ── Appearance ─────────────────────────────────────────────
    draw.text((55, y), "Darstellung", fill=PURPLE, font=font_b(30))
    y += 55

    # Dark Mode toggle
    draw.rounded_rectangle([(45, y), (W - 45, y + 110)], radius=18, fill=CARD)
    draw.text((100, y + 20), "Dunkles Design", fill=WHITE, font=font_b(34))
    draw.text((100, y + 62), "Dunkles Farbschema verwenden", fill=GRAY, font=font_r(26))
    # Switch (on)
    sw_x, sw_y = W - 160, y + 30
    draw.rounded_rectangle([(sw_x, sw_y), (sw_x + 100, sw_y + 52)], radius=26, fill=PURPLE)
    draw.ellipse([(sw_x + 58, sw_y + 6), (sw_x + 94, sw_y + 46)], fill=WHITE)
    y += 130

    # ── TTS ────────────────────────────────────────────────────
    draw.text((55, y), "Sprachausgabe", fill=PURPLE, font=font_b(30))
    y += 55

    # TTS toggle
    draw.rounded_rectangle([(45, y), (W - 45, y + 110)], radius=18, fill=CARD)
    draw.text((100, y + 20), "Sprachausgabe (TTS)", fill=WHITE, font=font_b(34))
    draw.text((100, y + 62), "Karten automatisch vorlesen", fill=GRAY, font=font_r(26))
    # Switch (on)
    draw.rounded_rectangle([(sw_x, sw_y + 130), (sw_x + 100, sw_y + 182)], radius=26, fill=PURPLE)
    draw.ellipse([(sw_x + 58, sw_y + 136), (sw_x + 94, sw_y + 176)], fill=WHITE)
    y += 130

    # Language dropdown
    draw.rounded_rectangle([(45, y), (W - 45, y + 110)], radius=18, fill=CARD)
    draw.text((100, y + 20), "Sprache", fill=WHITE, font=font_b(34))
    draw.text((100, y + 62), "TTS- und UI-Sprache", fill=GRAY, font=font_r(26))
    # Dropdown
    dd_x = W - 290
    draw.rounded_rectangle([(dd_x, y + 22), (W - 80, y + 88)], radius=10, outline=DARK, width=2, fill=(*DARK,))
    draw.text((dd_x + 18, y + 32), "Deutsch", fill=WHITE, font=font_r(28))
    # Arrow
    ax = W - 110
    ay = y + 50
    draw.polygon([(ax, ay), (ax + 16, ay), (ax + 8, ay + 12)], fill=GRAY)
    y += 130

    # ── Cloud Sync ─────────────────────────────────────────────
    draw.text((55, y), "Synchronisation", fill=PURPLE, font=font_b(30))
    y += 55

    # Sync toggle
    draw.rounded_rectangle([(45, y), (W - 45, y + 110)], radius=18, fill=CARD)
    draw.text((100, y + 20), "Cloud-Sync", fill=WHITE, font=font_b(34))
    draw.text((100, y + 62), "Daten mit der Cloud synchronisieren", fill=GRAY, font=font_r(26))
    # Switch (off)
    sw_y2 = y + 30
    draw.rounded_rectangle([(sw_x, sw_y2), (sw_x + 100, sw_y2 + 52)], radius=26, fill=DARK)
    draw.ellipse([(sw_x + 6, sw_y2 + 6), (sw_x + 42, sw_y2 + 46)], fill=GRAY)
    y += 130

    # ── Danger Zone ────────────────────────────────────────────
    draw.text((55, y), "Gefahrenzone", fill=PURPLE, font=font_b(30))
    y += 55

    draw.rounded_rectangle([(45, y), (W - 45, y + 130)], radius=18, fill=(*RED, 30))
    draw.rounded_rectangle([(45, y), (W - 45, y + 130)], radius=18, outline=RED, width=2)
    draw.text((100, y + 20), "Alle Daten zur\u00fccksetzen", fill=WHITE, font=font_b(34))
    draw.text((100, y + 62), "L\u00f6scht alle Decks und Karten unwiderruflich", fill=GRAY, font=font_r(26))
    # Reset button
    btn_x = W - 310
    draw.rounded_rectangle([(btn_x, y + 22), (W - 70, y + 100)], radius=14, fill=RED)
    tw = text_width(draw, "Zur\u00fccksetzen", font_b(28))
    draw.text((btn_x + (240 - tw) // 2, y + 40), "Zur\u00fccksetzen", fill=WHITE, font=font_b(28))
    y += 150

    # ── App Version ────────────────────────────────────────────
    y += 20
    ver = "Version 1.0.0"
    vw = text_width(draw, ver, font_r(26))
    draw.text(((W - vw) // 2, y), ver, fill=DIM, font=font_r(26))

    status_bar(draw)
    img.save(f"{OUT}/04_settings.png")
    print("✅ 04_settings.png")

screen_home()
screen_study()
screen_stats()
screen_import()
screen_settings()
screen_auto()
print("\n✅ All 6 professional screenshots generated!")
