from PIL import Image, ImageDraw, ImageFont, ImageFilter
import math

# ── Colors ──────────────────────────────────────────────────────────────────
BG      = (15, 15, 26)
SURFACE = (28, 28, 46)
CARD    = (37, 37, 64)
PURPLE  = (124, 92, 252)
CYAN    = (0, 229, 255)
WHITE   = (248, 250, 252)
GRAY    = (148, 163, 184)
GREEN   = (52, 211, 153)
RED     = (248, 113, 113)
AMBER   = (251, 191, 36)

W, H = 1080, 2400
FONT_PATH      = "/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf"
FONT_BOLD_PATH = "/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf"

def font(size, bold=False):
    return ImageFont.truetype(FONT_BOLD_PATH if bold else FONT_PATH, size)

def centered_text(draw, text, y, fnt, color=WHITE, x_center=None):
    if x_center is None:
        x_center = W // 2
    bbox = draw.textbbox((0, 0), text, font=fnt)
    tw = bbox[2] - bbox[0]
    draw.text((x_center - tw // 2, y), text, font=fnt, fill=color)

def rounded_rect(draw, xy, radius, fill, outline=None, outline_width=2):
    x0, y0, x1, y1 = xy
    draw.rounded_rectangle([x0, y0, x1, y1], radius=radius, fill=fill,
                            outline=outline, width=outline_width)

def progress_bar(draw, x, y, w, h, pct, bg=(55, 55, 85), fg=PURPLE, radius=6):
    rounded_rect(draw, [x, y, x + w, y + h], radius, bg)
    if pct > 0:
        fill_w = max(radius * 2, int(w * pct))
        rounded_rect(draw, [x, y, x + fill_w, y + h], radius, fg)

def draw_status_bar(draw, img):
    """Minimal Android status bar."""
    draw.rectangle([0, 0, W, 80], fill=SURFACE)
    # time
    draw.text((60, 22), "9:41", font=font(36, bold=True), fill=WHITE)
    # icons (simplified)
    for i, (icon_x, icon_y, r) in enumerate([(980, 40, 10), (1000, 40, 10), (1030, 40, 10)]):
        draw.ellipse([icon_x - r, icon_y - r, icon_x + r, icon_y + r], fill=GRAY)

def draw_nav_bar(draw, img, active="home"):
    """Bottom navigation with 3 items."""
    bar_y = H - 160
    draw.rectangle([0, bar_y, W, H], fill=SURFACE)
    # separator line
    draw.line([(0, bar_y), (W, bar_y)], fill=(55, 55, 85), width=2)

    items = [("Decks", "◈"), ("Study", "▶"), ("Stats", "◉")]
    icons_active = {"Decks": PURPLE, "Study": PURPLE, "Stats": PURPLE}
    x_positions = [W // 6, W // 2, 5 * W // 6]

    active_map = {"home": "Decks", "study": "Study", "stats": "Stats"}
    active_name = active_map.get(active, "Decks")

    for (name, icon), x in zip(items, x_positions):
        is_active = name == active_name
        color = PURPLE if is_active else GRAY
        icon_fnt = font(48 if is_active else 40, bold=is_active)
        label_fnt = font(28, bold=is_active)
        centered_text(draw, icon, bar_y + 20, icon_fnt, color, x)
        centered_text(draw, name, bar_y + 90, label_fnt, color, x)
        if is_active:
            pill_w = 80
            draw.rounded_rectangle(
                [x - pill_w//2, bar_y + 8, x + pill_w//2, bar_y + 14],
                radius=4, fill=PURPLE
            )

def draw_gradient_circle(img, cx, cy, r, color, alpha=40):
    """Soft glow circle via paste."""
    overlay = Image.new("RGBA", img.size, (0, 0, 0, 0))
    d = ImageDraw.Draw(overlay)
    for i in range(r, 0, -max(1, r//20)):
        a = int(alpha * (1 - i / r) ** 1.5)
        d.ellipse([cx - i, cy - i, cx + i, cy + i],
                  fill=(*color, a))
    img.paste(overlay, mask=overlay)


# ── Screenshot 1: Deck List ──────────────────────────────────────────────────
def make_deck_list():
    img = Image.new("RGB", (W, H), BG)
    draw = ImageDraw.Draw(img)

    # Subtle background glow
    draw_gradient_circle(img.convert("RGBA"), W//2, 400, 600, PURPLE, 30)
    img = img.convert("RGB")
    draw = ImageDraw.Draw(img)

    draw_status_bar(draw, img)

    # ── App bar ──────────────────────────────────────────────────────────────
    bar_h = 140
    draw.rectangle([0, 80, W, 80 + bar_h], fill=SURFACE)

    # Logo circle
    logo_cx, logo_cy = 100, 80 + bar_h // 2
    draw.ellipse([logo_cx - 34, logo_cy - 34, logo_cx + 34, logo_cy + 34],
                 fill=PURPLE)
    draw.text((logo_cx - 22, logo_cy - 28), "⚡", font=font(44, bold=True), fill=WHITE)

    draw.text((155, 80 + 28), "FlashMind", font=font(56, bold=True), fill=WHITE)
    draw.text((155, 80 + 90), "Your smart flashcard companion", font=font(30), fill=GRAY)

    # Search icon button (top right)
    sx = W - 80
    draw.rounded_rectangle([sx - 40, 80 + 30, sx + 40, 80 + 110],
                            radius=24, fill=CARD)
    draw.text((sx - 18, 80 + 42), "⌕", font=font(44), fill=GRAY)

    # ── Section header ───────────────────────────────────────────────────────
    draw.text((60, 260), "MY DECKS", font=font(30, bold=True), fill=PURPLE)
    draw.text((W - 200, 260), "4 decks", font=font(30), fill=GRAY)

    # ── Deck cards ───────────────────────────────────────────────────────────
    decks = [
        {
            "title": "Spanish Vocabulary",
            "subtitle": "Intermediate • 120 cards",
            "pct": 0.72,
            "done": 86,
            "total": 120,
            "color": PURPLE,
            "icon": "ES",
            "streak": "🔥 5",
        },
        {
            "title": "Python Programming",
            "subtitle": "Advanced • 85 cards",
            "pct": 0.45,
            "done": 38,
            "total": 85,
            "color": CYAN,
            "icon": "PY",
            "streak": "⭐ 3",
        },
        {
            "title": "World History",
            "subtitle": "Beginner • 200 cards",
            "pct": 0.18,
            "done": 36,
            "total": 200,
            "color": AMBER,
            "icon": "HI",
            "streak": "",
        },
        {
            "title": "Math Formulas",
            "subtitle": "Intermediate • 64 cards",
            "pct": 0.91,
            "done": 58,
            "total": 64,
            "color": GREEN,
            "icon": "∑",
            "streak": "🔥 12",
        },
    ]

    card_y = 330
    card_h = 210
    gap = 24
    pad = 40

    for deck in decks:
        cx0, cy0 = pad, card_y
        cx1, cy1 = W - pad, card_y + card_h

        # Card shadow
        shadow = Image.new("RGBA", img.size, (0, 0, 0, 0))
        sd = ImageDraw.Draw(shadow)
        sd.rounded_rectangle([cx0 + 4, cy0 + 6, cx1 + 4, cy1 + 6],
                              radius=24, fill=(0, 0, 0, 60))
        img.paste(shadow, mask=shadow)
        draw = ImageDraw.Draw(img)

        # Card body
        rounded_rect(draw, [cx0, cy0, cx1, cy1], 24, CARD)

        # Left accent strip
        rounded_rect(draw, [cx0, cy0, cx0 + 8, cy1], 4, deck["color"])

        # Icon circle
        icon_cx = cx0 + 80
        icon_cy = cy0 + card_h // 2
        draw.ellipse([icon_cx - 38, icon_cy - 38, icon_cx + 38, icon_cy + 38],
                     fill=(*deck["color"][:3], 40) if len(deck["color"]) == 3
                     else deck["color"])
        draw.ellipse([icon_cx - 38, icon_cy - 38, icon_cx + 38, icon_cy + 38],
                     outline=deck["color"], width=2,
                     fill=(deck["color"][0]//5, deck["color"][1]//5, deck["color"][2]//5))
        draw.text((icon_cx - 22, icon_cy - 22), deck["icon"][:2],
                  font=font(30, bold=True), fill=deck["color"])

        # Title & subtitle
        tx = cx0 + 140
        draw.text((tx, cy0 + 28), deck["title"], font=font(40, bold=True), fill=WHITE)
        draw.text((tx, cy0 + 78), deck["subtitle"], font=font(30), fill=GRAY)

        # Progress bar
        pb_y = cy0 + 128
        pb_w = cx1 - tx - 40
        progress_bar(draw, tx, pb_y, pb_w, 14, deck["pct"],
                     fg=deck["color"])

        # Progress label
        label = f"{deck['done']}/{deck['total']} cards"
        draw.text((tx, cy0 + 158), label, font=font(26), fill=GRAY)

        # Percentage pill (right side)
        pct_str = f"{int(deck['pct'] * 100)}%"
        draw.rounded_rectangle([cx1 - 130, cy0 + 24, cx1 - 20, cy0 + 72],
                                radius=16, fill=(deck["color"][0]//4,
                                                  deck["color"][1]//4,
                                                  deck["color"][2]//4))
        centered_text(draw, pct_str, cy0 + 34, font(30, bold=True),
                      deck["color"], (cx1 - 75))

        # Streak badge
        if deck["streak"]:
            draw.text((cx1 - 145, cy0 + 155), deck["streak"],
                      font=font(28), fill=GRAY)

        card_y += card_h + gap

    # ── "Study All" CTA button ───────────────────────────────────────────────
    btn_y = card_y + 30
    rounded_rect(draw, [pad, btn_y, W - pad, btn_y + 110], 32, PURPLE)
    centered_text(draw, "▶  Start Daily Review", btn_y + 30,
                  font(42, bold=True), WHITE)

    # ── Streak banner ────────────────────────────────────────────────────────
    banner_y = btn_y + 140
    rounded_rect(draw, [pad, banner_y, W - pad, banner_y + 100], 20,
                 (52, 211, 153, 20))
    rounded_rect(draw, [pad, banner_y, W - pad, banner_y + 100], 20,
                 fill=None, outline=GREEN, outline_width=2)
    draw.text((pad + 30, banner_y + 28),
              "🔥  7-day streak!  Keep it up!",
              font=font(34, bold=True), fill=GREEN)

    draw_nav_bar(draw, img, "home")

    img.save("/home/hermespi/flashdrive/screenshots/01_auto_deck_list.png", "PNG")
    print("Saved 01_auto_deck_list.png")


# ── Screenshot 2: Study / Flashcard Screen ───────────────────────────────────
def make_study():
    img = Image.new("RGB", (W, H), BG)
    draw = ImageDraw.Draw(img)

    draw_gradient_circle(img.convert("RGBA"), W//2, H//2, 700, PURPLE, 25)
    img = img.convert("RGB")
    draw = ImageDraw.Draw(img)

    draw_status_bar(draw, img)

    # ── App bar ──────────────────────────────────────────────────────────────
    draw.rectangle([0, 80, W, 220], fill=SURFACE)
    draw.text((60, 100), "←", font=font(52), fill=WHITE)
    draw.text((155, 100), "Spanish Vocabulary", font=font(46, bold=True), fill=WHITE)
    draw.text((155, 158), "Card 7 of 20", font=font(30), fill=GRAY)

    # Settings / more icon
    draw.text((W - 100, 108), "⋮", font=font(52, bold=True), fill=GRAY)

    # ── Overall session progress ─────────────────────────────────────────────
    prog_y = 236
    draw.rectangle([0, prog_y, W, prog_y + 8], fill=(55, 55, 85))
    draw.rectangle([0, prog_y, int(W * 0.35), prog_y + 8], fill=PURPLE)

    # ── Card counter row ─────────────────────────────────────────────────────
    row_y = 268
    draw.text((60, row_y), "✓ 4 correct", font=font(30, bold=True), fill=GREEN)
    draw.text((W//2 - 40, row_y), "✗ 2 wrong", font=font(30, bold=True), fill=RED)
    draw.text((W - 280, row_y), "⏱ 2:34", font=font(30), fill=GRAY)

    # ── Flashcard ────────────────────────────────────────────────────────────
    card_pad = 50
    card_top = 360
    card_bot = H - 560
    card_cx = W // 2
    card_cy = (card_top + card_bot) // 2

    # Outer glow
    for r_off, alpha in [(12, 30), (6, 50)]:
        glow = Image.new("RGBA", img.size, (0, 0, 0, 0))
        gd = ImageDraw.Draw(glow)
        gd.rounded_rectangle(
            [card_pad - r_off, card_top - r_off,
             W - card_pad + r_off, card_bot + r_off],
            radius=40 + r_off, fill=(*PURPLE, alpha))
        img.paste(glow, mask=glow)
        draw = ImageDraw.Draw(img)

    # Card face
    rounded_rect(draw, [card_pad, card_top, W - card_pad, card_bot], 36, CARD)

    # QUESTION label pill
    lbl_w = 240
    draw.rounded_rectangle([card_cx - lbl_w//2, card_top + 40,
                             card_cx + lbl_w//2, card_top + 96],
                            radius=20, fill=(PURPLE[0]//3, PURPLE[1]//3, PURPLE[2]//3))
    centered_text(draw, "QUESTION", card_top + 52, font(30, bold=True), PURPLE)

    # Divider
    draw.line([(card_pad + 60, card_top + 116),
               (W - card_pad - 60, card_top + 116)], fill=(55, 55, 90), width=2)

    # Question text (multiline)
    q_line1 = "¿Cómo se dice"
    q_line2 = '"to remember"'
    q_line3 = "en inglés?"
    centered_text(draw, q_line1, card_top + 150, font(62), GRAY)
    centered_text(draw, q_line2, card_top + 230, font(72, bold=True), WHITE)
    centered_text(draw, q_line3, card_top + 318, font(62), GRAY)

    # Decorative icon
    centered_text(draw, "🇪🇸", card_top + 430, font(96))

    # Hint text
    centered_text(draw, "Tap card to reveal answer",
                  card_bot - 90, font(32), GRAY)
    centered_text(draw, "↕", card_bot - 50, font(40), PURPLE)

    # ── Difficulty buttons ───────────────────────────────────────────────────
    btn_y = card_bot + 50
    buttons = [
        ("Again", RED,    (80, 20, 20)),
        ("Hard",  AMBER,  (50, 40, 10)),
        ("Good",  GREEN,  (10, 50, 30)),
        ("Easy",  CYAN,   (10, 40, 50)),
    ]
    bw = (W - 100) // 4
    for i, (label, color, dark) in enumerate(buttons):
        bx = 50 + i * (bw + 8)
        by = btn_y
        rounded_rect(draw, [bx, by, bx + bw, by + 120], 24, dark)
        rounded_rect(draw, [bx, by, bx + bw, by + 120], 24,
                     fill=None, outline=color, outline_width=2)
        centered_text(draw, label, by + 34, font(38, bold=True), color,
                      bx + bw // 2)

    # ── Card navigation arrows ───────────────────────────────────────────────
    nav_y = btn_y + 160
    rounded_rect(draw, [60, nav_y, 180, nav_y + 90], 20, CARD)
    centered_text(draw, "←", nav_y + 14, font(52), GRAY, 120)

    centered_text(draw, "7 / 20", nav_y + 22, font(38), WHITE)

    rounded_rect(draw, [W - 180, nav_y, W - 60, nav_y + 90], 20, CARD)
    centered_text(draw, "→", nav_y + 14, font(52), GRAY, W - 120)

    draw_nav_bar(draw, img, "study")

    img.save("/home/hermespi/flashdrive/screenshots/02_auto_study.png", "PNG")
    print("Saved 02_auto_study.png")


# ── Screenshot 3: Session Complete ──────────────────────────────────────────
def make_complete():
    img = Image.new("RGB", (W, H), BG)
    draw = ImageDraw.Draw(img)

    # Large celebration glow
    draw_gradient_circle(img.convert("RGBA"), W//2, 900, 800, GREEN, 35)
    draw_gradient_circle(img.convert("RGBA"), W//2, 900, 400, CYAN, 20)
    img = img.convert("RGB")
    draw = ImageDraw.Draw(img)

    draw_status_bar(draw, img)

    # ── App bar ──────────────────────────────────────────────────────────────
    draw.rectangle([0, 80, W, 220], fill=SURFACE)
    draw.text((60, 100), "←", font=font(52), fill=WHITE)
    centered_text(draw, "Session Complete", 104, font(48, bold=True), WHITE)

    # ── Trophy / celebration icon ────────────────────────────────────────────
    trophy_y = 290
    centered_text(draw, "🏆", trophy_y, font(160))

    # Confetti dots (static decorative)
    import random
    random.seed(42)
    confetti_colors = [PURPLE, CYAN, GREEN, AMBER, RED, WHITE]
    for _ in range(60):
        cx = random.randint(80, W - 80)
        cy = random.randint(trophy_y, trophy_y + 200)
        r = random.randint(4, 12)
        col = random.choice(confetti_colors)
        draw.ellipse([cx - r, cy - r, cx + r, cy + r], fill=col)

    # ── Headline ─────────────────────────────────────────────────────────────
    hl_y = trophy_y + 190
    centered_text(draw, "Outstanding!", hl_y, font(80, bold=True), WHITE)
    centered_text(draw, "You've mastered today's session",
                  hl_y + 94, font(38), GRAY)

    # ── Score ring ───────────────────────────────────────────────────────────
    ring_cx, ring_cy = W // 2, hl_y + 310
    ring_r = 160
    # Background circle
    draw.ellipse([ring_cx - ring_r, ring_cy - ring_r,
                  ring_cx + ring_r, ring_cy + ring_r],
                 outline=(55, 55, 85), width=18)
    # Arc for score (85% = ~306 degrees)
    from PIL import ImageDraw as ID
    score_pct = 0.85
    end_angle = -90 + 360 * score_pct
    draw.arc([ring_cx - ring_r, ring_cy - ring_r,
              ring_cx + ring_r, ring_cy + ring_r],
             start=-90, end=end_angle, fill=GREEN, width=18)

    centered_text(draw, "85%", ring_cy - 46, font(80, bold=True), GREEN)
    centered_text(draw, "Score", ring_cy + 20, font(34), GRAY)

    # ── Stats grid ───────────────────────────────────────────────────────────
    stats_top = ring_cy + ring_r + 60
    stats = [
        ("20",     "Cards Reviewed", GREEN),
        ("17",     "Correct",        GREEN),
        ("3",      "Mistakes",       RED),
        ("4:12",   "Time Spent",     CYAN),
        ("+120",   "XP Earned",      AMBER),
        ("🔥 8",   "Day Streak",     AMBER),
    ]
    cols = 3
    cell_w = (W - 80) // cols
    cell_h = 190
    for i, (value, label, color) in enumerate(stats):
        col = i % cols
        row = i // cols
        cx0 = 40 + col * cell_w
        cy0 = stats_top + row * (cell_h + 20)

        # Shadow
        sh = Image.new("RGBA", img.size, (0, 0, 0, 0))
        sd = ImageDraw.Draw(sh)
        sd.rounded_rectangle([cx0 + 3, cy0 + 5, cx0 + cell_w - 12, cy0 + cell_h + 5],
                              radius=20, fill=(0, 0, 0, 50))
        img.paste(sh, mask=sh)
        draw = ImageDraw.Draw(img)

        rounded_rect(draw, [cx0, cy0, cx0 + cell_w - 12, cy0 + cell_h], 20, CARD)

        centered_text(draw, value, cy0 + 28, font(60, bold=True), color,
                      cx0 + (cell_w - 12) // 2)
        centered_text(draw, label, cy0 + 110, font(26), GRAY,
                      cx0 + (cell_w - 12) // 2)

        # Small color dot
        dot_cx = cx0 + (cell_w - 12) // 2
        draw.ellipse([dot_cx - 5, cy0 + 152, dot_cx + 5, cy0 + 162],
                     fill=color)

    # ── Action buttons ───────────────────────────────────────────────────────
    btn_top = stats_top + 2 * (cell_h + 20) + 50
    pad = 40

    # Primary: Study Again
    rounded_rect(draw, [pad, btn_top, W - pad, btn_top + 120], 32, PURPLE)
    centered_text(draw, "▶  Study Again", btn_top + 34,
                  font(44, bold=True), WHITE)

    # Secondary: Back to Decks
    rounded_rect(draw, [pad, btn_top + 148, W - pad, btn_top + 268], 32, CARD)
    rounded_rect(draw, [pad, btn_top + 148, W - pad, btn_top + 268], 32,
                 fill=None, outline=PURPLE, outline_width=2)
    centered_text(draw, "← Back to Decks", btn_top + 182,
                  font(44, bold=True), PURPLE)

    draw_nav_bar(draw, img, "stats")

    img.save("/home/hermespi/flashdrive/screenshots/03_auto_complete.png", "PNG")
    print("Saved 03_auto_complete.png")


make_deck_list()
make_study()
make_complete()
print("All screenshots generated successfully.")
