# Recipe Index — Design System v2.0

**Theme:** Sage & Linen  
**Version:** 2.0  
**Updated:** April 2026  
**Replaces:** Hearth v1.0 (November 2025)  
**Platform:** Android (Kotlin / Material 3)

---

## What Changed from v1.0

| Area | v1.0 Hearth | v2.0 Sage & Linen |
|---|---|---|
| Background | Warm cream `#FBF8F3` | Soft grey `#F0F0EC` |
| Cards | Deep clay `#2C1810` (dark) | White `#FFFFFF` on grey |
| Primary accent | Terracotta `#E8997A` | Sage green `#7A9E8A` |
| Secondary accent | Burnt orange `#D97757` | Warm gold `#D4875A` |
| Text | Rich brown `#3D2817` | Forest ink `#1E2D22` |
| Buttons | Text labels | Icon-only (labels removed) |
| Logo | Book + text label | Book icon only, no app name |

---

## 1. Colour Palette

### 1.1 Backgrounds & Surfaces

Two-level surface system: grey base with white cards floating on top. This replaces the cream base which read as too brown-heavy on device screens.

| Name | Hex | Usage |
|---|---|---|
| Background | `#F0F0EC` | Screen background — soft grey |
| Surface / Card | `#FFFFFF` | Card surfaces, modals, bottom nav |
| Surface Alt | `#E8E8E2` | Inner sections, no-change tag rows |
| Border | `#DDDDD6` | Card borders, dividers |
| Border Strong | `#CECEC6` | Phone frame, focused inputs |

### 1.2 Sage Green (Primary)

Sage is the primary action colour. It appears on active nav items, primary buttons, cooking mode header, the FAB, and confirmation actions in modals.

| Name | Hex | Usage |
|---|---|---|
| Sage Dark | `#4A6B55` | Primary buttons, cooking header, grocery category headers |
| Sage | `#7A9E8A` | Active nav icons, FAB, active tags, checkmarks, step progress |
| Sage Pale | `#C8D4C0` | Inactive chips, progress dots |
| Sage Background | `#EAF0EA` | Screen backgrounds, This Week card, timer row, changed-tag card tint |

### 1.3 Warm Gold Accent

Retained from Hearth to keep warmth in the palette. Used for import actions, step numbers in cooking mode, and the FAB on list screens.

| Name | Hex | Usage |
|---|---|---|
| Accent | `#D4875A` | Import icon, step numbers, FAB, accent actions |
| Accent Pale | `#F8EEE4` | Edit button background in tag dialog |

### 1.4 Ink (Text)

A dark forest green-black replaces warm brown text. Reads as near-black on screen while harmonising with sage.

| Name | Hex | Usage |
|---|---|---|
| Ink | `#1E2D22` | Primary text, recipe names, headings |
| Ink Mid | `#4A5E50` | Secondary text, ingredient amounts, section labels |
| Ink Light | `#7A8C80` | Metadata, timestamps, captions |
| Ink Muted | `#AAAEB0` | Placeholder text, inactive nav labels, disabled |

### 1.5 Semantic

| Name | Hex | Usage |
|---|---|---|
| Danger | `#C04040` | Remove/delete icon strokes |
| Danger Surface | `#FEE8E8` | Remove button backgrounds |

### 1.6 Contrast Ratios

| Combination | Ratio | Pass |
|---|---|---|
| `#1E2D22` on `#FFFFFF` | 16.5:1 | ✅ AAA |
| `#1E2D22` on `#F0F0EC` | 14.8:1 | ✅ AAA |
| `#4A6B55` on `#FFFFFF` | 7.2:1 | ✅ AA |
| `#FFFFFF` on `#4A6B55` | 7.2:1 | ✅ AA |
| `#FFFFFF` on `#7A9E8A` | 3.2:1 | ⚠️ Large/bold UI only |

---

## 2. Typography

### 2.1 Font Families

| Role | Font | Usage |
|---|---|---|
| Display | Lora (serif) | Page titles, recipe names, section headers, modal titles, step numbers |
| Body / UI | DM Sans (sans-serif) | All other text, labels, metadata, navigation |

Both fonts are available via Google Fonts.

### 2.2 Type Scale

| Token | Size | Weight | Line Height | Font | Usage |
|---|---|---|---|---|---|
| `display` | 18sp | 600 | 1.25 | Lora | Screen titles in headers |
| `title-lg` | 16sp | 600 | 1.3 | Lora | Recipe card names |
| `title-sm` | 14sp | 600 | 1.3 | Lora | Meal plan dates, modal headers |
| `section` | 13sp | 600 | 1.4 | Lora | Section labels (Quick actions, This week) |
| `body-lg` | 16sp | 400 | 1.6 | DM Sans | Cooking mode instructions |
| `body` | 13sp | 400 | 1.5 | DM Sans | Ingredients, grocery items |
| `meta` | 11sp | 400 | 1.4 | DM Sans | Servings, cook time, dates |
| `label` | 10sp | 500 | 1.3 | DM Sans | Nav labels, tag chips, category headers |
| `caption` | 9sp | 500 | 1.3 | DM Sans | Footnotes, step counter x/y |

### 2.3 Cooking Mode

Increase all sizes by 25% for cooking context readability:

| Token | Normal | Cooking Mode |
|---|---|---|
| Instructions | 13sp | 16sp (body-lg) |
| Step number | 30sp | 36sp |
| Ingredient | 13sp | 16sp |

---

## 3. Logo

### 3.1 Concept

Open cookbook — the original shape is preserved. The key distinction the logo must communicate is **cover vs inner pages**:

- **Left side:** Dark book cover (back cover + spine thickness visible as a thick dark strip, then front cover face in a contrasting colour)
- **Spine (centre):** A narrow strip in a third colour binding the two sides
- **Right side:** Cream inner pages — layered to show depth, with subtle horizontal lines suggesting text
- **Leaf motif:** Sage green leaf sits on the inner pages — the only coloured element on the cream side

### 3.2 Recommended Option (B)

| Element | Colour | Hex |
|---|---|---|
| Cover back/spine edge | Near-black | `#101810` |
| Cover front face | Dark forest | `#1E2D22` |
| Cover inner fold lines | Darker forest | `#161E18` |
| Spine binding strip | Warm gold | `#C8A860` |
| Inner pages (back) | Warm cream | `#EDE8DC` |
| Inner pages (front) | Cream | `#F5F0E8` |
| Page lines | Soft warm grey | `#D8D0C0` |
| Leaf | Sage | `#7A9E8A` |
| Leaf vein | Sage dark | `#4A6B55` |
| Icon background | Sage | `#7A9E8A` |
| Icon border | Sage pale | `#C8D4C0` |

### 3.3 Sizes

| Context | Icon size | Border radius |
|---|---|---|
| App launcher icon | 108dp (with 18dp safe zone) | 22dp |
| App header (in-app) | 38×38dp | 10dp |
| Small/header-tight | 36×36dp | 9dp |

### 3.4 Rules

- Never display the app name "RecipeIndex" inside or below the icon — the icon stands alone
- Minimum display size: 36×36dp (below this, drop the leaf vein lines)
- The icon background (`#7A9E8A` Sage) is part of the asset — do not crop it out
- Icon is legible on both light and dark wallpapers

---

## 4. Components

### 4.1 Quick Action Buttons

Icon-only square buttons in a single horizontal row. No text labels inside the button. Small passive labels may sit below if screen space allows.

```
Layout:     4 equal-width buttons in a flex row, gap 8dp
Size:       flex-1, height 52dp
Shape:      border-radius 14dp
Icon size:  22×22dp

Import (neutral):
  Background: #FFFFFF
  Border:     1dp solid #DDDDD6
  Icon color: #D4875A (accent)

All Recipes / Grocery / Meal Plan (sage filled):
  Background: #7A9E8A  (primary actions)
           or #4A6B55  (secondary sage, e.g. Meal Plan)
  Icon color: #FFFFFF
```

### 4.2 Recipe Cards

Photo-dominant cards. Photo fills top 148–160dp, info section below is tight.

```
Card:
  Background:    #FFFFFF
  Border:        1dp solid #DDDDD6
  Border radius: 17dp
  Shadow:        0 2dp 8dp rgba(0,0,0,0.05)

Photo area:
  Height:        148dp
  Gradient ov:   linear bottom 52dp, rgba(30,45,34,0.48) → transparent

Recipe name:
  Font:          Lora 15sp 600
  Color:         #1E2D22
  Margin bottom: 4dp

Meta row (servings · time):
  Font:          DM Sans 11sp 400
  Color:         #7A8C80

Tags:
  Background:    #7A9E8A
  Text:          #4A5E50 10sp 500
  Border radius: 20dp
  Padding:       3dp 8dp

Card action buttons (top-right overlay):
  Size:     25×25dp circle
  Bg:       rgba(255,255,255,0.90)
  Icons:    11×11dp, stroke #1E2D22
```

### 4.3 Bottom Navigation

```
Background:    #FFFFFF
Border top:    1dp solid #DDDDD6
Padding:       8dp top, 15dp bottom
Border radius: 0 (flush to screen edge)

Nav icon (inactive):
  Color: #AAAEB0

Nav icon (active):
  Color: #7A9E8A

Nav label:
  Font:   DM Sans 9sp 500
  Color:  #AAAEB0 (inactive), #7A9E8A (active)
```

### 4.4 Search Bar

```
Background:    #FFFFFF
Border:        1dp solid #DDDDD6
Border radius: 13dp
Padding:       9dp 13dp
Placeholder:   DM Sans 12sp, #AAAEB0

Focus state:
  Border:     1dp solid #7A9E8A
  Shadow:     0 0 0 3dp rgba(122,158,138,0.15)
```

### 4.5 FAB (Floating Action Button)

```
Size:          42×42dp
Border radius: 13dp
Background:    #D4875A (accent)
Icon:          18×18dp, #FFFFFF
Position:      bottom 70dp, right 13dp (above bottom nav)
Shadow:        0 4dp 12dp rgba(212,135,90,0.35)
```

### 4.6 Tags / Chips

```
Inactive:
  Background: #EAF0EA
  Text:       #4A5E50, 10sp 500
  Border:     none
  Padding:    3dp 8dp
  Radius:     20dp

Active / Selected:
  Background: #7A9E8A
  Text:       #FFFFFF
```

### 4.7 Grocery List

```
Category header:
  Background:    #1E2D22 (Ink)
  Text:          #C8D4C0 (Sage Pale), Lora 11sp 500
  Border radius: 10dp
  Padding:       7dp 12dp
  Margin:        9dp top, 4dp bottom

List item:
  Layout:  checkbox · name (flex-1) · amount
  Padding: 8dp vertical, 2dp horizontal
  Divider: 1dp solid #DDDDD6

Checkbox:
  Size:          19×19dp
  Border:        1.5dp solid #CECEC6
  Border radius: 6dp
  Background:    #FFFFFF

Checked state:
  Background:    #7A9E8A
  Border:        #7A9E8A
  Checkmark:     #FFFFFF, 1.4dp stroke

Item name (checked):
  text-decoration: line-through
  Color:           #AAAEB0, opacity 0.65

Amount:
  Color: #4A6B55, 11sp 500
```

### 4.8 Cooking Mode

Cooking mode always uses pure white background regardless of system dark mode preference. Maximum readability is non-negotiable.

```
Screen background:  #FFFFFF
Status bar:         #4A6B55 (Sage Dark)

Header bar:
  Background: #4A6B55
  Title:      Lora 13sp 600, #FFFFFF
  Subtitle:   DM Sans 11sp, rgba(255,255,255,0.72)

Step progress dots:
  Done:    #7A9E8A
  Current: #D4875A
  Upcoming:#CECEC6
  Height:  3dp, border-radius 2dp

Step card:
  Background:          #FFFFFF
  Border left:         3dp solid #7A9E8A
  Border other sides:  1dp solid #DDDDD6
  Right border radius: 13dp
  Padding:             14dp 13dp

Step number:
  Font:  Lora 30sp 600
  Color: #D4875A

Step text:
  Font:  DM Sans 15sp 400
  Color: #1E2D22
  Line height: 1.6

Timer row:
  Background: #EAF0EA
  Border:     1dp solid #C8D4C0
  Radius:     13dp
  Value font: Lora 19sp 600, #4A6B55

Ingredients highlight:
  Background: #E8E8E2
  Border:     1dp solid #DDDDD6
  Radius:     12dp
  Label:      DM Sans 10sp 500 uppercase, #4A6B55

Step nav buttons:
  Height:     42dp
  Radius:     13dp
  Prev:       Background #E8E8E2, border 1dp #DDDDD6
  Next:       Background #7A9E8A
  Icon only — no text
```

### 4.9 Tag Changes Dialog

Changed tags and unchanged tags use visually distinct rows. Before/after is shown on two separate lines to accommodate unpredictable tag lengths.

```
Modal:
  Background:    #FFFFFF
  Border radius: 22dp
  Padding:       20dp 16dp 16dp
  Shadow:        0 10dp 40dp rgba(0,0,0,0.25)
  Overlay:       rgba(20,35,24,0.60)

Changed tag row:
  Background: #EAF0EA (Sage Background)
  Border:     1dp solid #C8D4C0
  Radius:     13dp
  Padding:    12dp 13dp

  Before line:
    Label "BEFORE" — DM Sans 10sp 500 uppercase, #AAAEB0
    Value — DM Sans 13sp, #4A5E50, line-through

  After line:
    Label "AFTER " — DM Sans 10sp 500 uppercase, #4A6B55
    Value — DM Sans 14sp 600, #4A6B55

  Reason text:
    DM Sans 11sp, #7A8C80

  Action icons (top-right, vertical stack):
    Edit:   28dp circle, #F8EEE4 bg, #D4875A pencil icon
    Remove: 28dp circle, #FEE8E8 bg, #C04040 × icon

No-change tag row:
  Background: #E8E8E2
  Border:     1dp solid #DDDDD6
  Radius:     13dp
  Padding:    11dp 13dp
  Layout:     checkmark icon · tag name · "no change" text · remove icon

Footer:
  Cancel: 42dp square icon-only button, #E8E8E2 bg
  Accept: flex-1 height 42dp, #7A9E8A bg, checkmark + "Accept all changes" text
```

### 4.10 Meal Plan Cards

```
Card:
  Background:    #FFFFFF
  Border:        1dp solid #DDDDD6
  Border radius: 15dp
  Overflow:      hidden

Header (current week):
  Background: #7A9E8A (Sage)

Header (past weeks):
  Background: #1E2D22 (Ink)

Header text:
  Title:    Lora 14sp 600, #FFFFFF
  Subtitle: DM Sans 10sp, rgba(255,255,255,0.62)

"This week" badge:
  Background: rgba(255,255,255,0.20)
  Text:       DM Sans 9sp 500, #FFFFFF
  Radius:     20dp

Action icons in header:
  28dp circles, rgba(255,255,255,0.15) bg
  Edit + cart icons, #FFFFFF

Body:
  Padding:    8dp 12dp 10dp
  Recipe items: DM Sans 12sp, #4A5E50
  Bullet:     4×4dp circle, #7A9E8A
```

---

## 5. Layout & Spacing

### 5.1 Spacing Scale (8dp base)

| Token | Value |
|---|---|
| `xxs` | 4dp |
| `xs` | 8dp |
| `s` | 12dp |
| `m` | 16dp |
| `l` | 24dp |
| `xl` | 32dp |
| `xxl` | 48dp |

### 5.2 Screen Padding

```
Horizontal screen padding: 12dp
Bottom clearance (above nav): 86dp
```

### 5.3 Border Radius Scale

| Token | Value | Usage |
|---|---|---|
| `r-sm` | 8dp | Small inputs |
| `r-md` | 12–13dp | Search bars, tag rows, inner elements |
| `r-lg` | 15–17dp | Cards |
| `r-xl` | 20–22dp | Modals, phone frame |
| `r-full` | 9999dp | Pills, chips, circular buttons |

---

## 6. Iconography

### 6.1 Style

- Library: Lucide Icons (rounded variant) — recommended
- Weight: 1.4–1.6dp stroke (not filled, except nav active states)
- Size: 12–13dp for card overlays, 17–18dp for action buttons, 19–20dp for nav
- Colour: inherits from context (see component specs above)

### 6.2 Icon-Only Button Rules

All interactive buttons outside of modals and the cooking step nav are icon-only. This avoids text truncation across device sizes.

- Minimum tap target: 44×44dp (the visible button can be smaller if padding extends the tap zone)
- Every icon-only button must have a `contentDescription` for accessibility
- Tooltips on long-press are recommended for quick action buttons

### 6.3 Common Icon Assignments

| Action | Icon (Lucide) |
|---|---|
| Import / Download | `download` |
| All Recipes | `book-open` or `list` |
| Grocery | `shopping-cart` |
| Meal Plan | `calendar` |
| Search | `search` |
| Filter | `sliders-horizontal` |
| Sort | `arrow-up-down` |
| Cook Now / Play | `play` (filled) |
| Favourite / Star | `star` |
| Share | `share-2` |
| Edit | `pencil` |
| Delete / Remove | `x` or `trash-2` |
| Back | `chevron-left` |
| More options | `more-vertical` |
| Timer | `clock` |
| Servings | `users` |
| Checkmark | `check` |

---

## 7. Shadows

### 7.1 Light Mode

| Token | Value | Usage |
|---|---|---|
| `shadow-subtle` | `0 2dp 8dp rgba(0,0,0,0.05)` | Cards at rest |
| `shadow-card` | `0 2dp 12dp rgba(0,0,0,0.06)` | Elevated cards |
| `shadow-modal` | `0 10dp 40dp rgba(0,0,0,0.20)` | Modals, bottom sheets |
| `shadow-fab` | `0 4dp 12dp rgba(212,135,90,0.35)` | FAB |

---

## 8. Animation

| Token | Duration | Easing | Usage |
|---|---|---|---|
| `anim-fast` | 150ms | ease-in-out | Hover, checkbox |
| `anim-normal` | 250ms | ease-in-out | Most transitions |
| `anim-slow` | 400ms | ease-out | Modal entrance, page transitions |

Microinteractions:
- Button tap: `scale(0.98)`
- Card press: shadow increase + `translateY(-1dp)`
- Checkbox check: bounce `cubic-bezier(0.68,-0.55,0.265,1.55)`
- List item check-off: fade to 65% opacity + strikethrough
- Timer completion: gentle sage green pulse

---

## 9. Android / Kotlin Implementation

### 9.1 colors.xml

```xml
<!-- res/values/colors.xml -->
<resources>
  <!-- Backgrounds -->
  <color name="background">#F0F0EC</color>
  <color name="surface">#FFFFFF</color>
  <color name="surface_alt">#E8E8E2</color>
  <color name="border">#DDDDD6</color>
  <color name="border_strong">#CECEC6</color>

  <!-- Sage Green -->
  <color name="sage_dark">#4A6B55</color>
  <color name="sage">#7A9E8A</color>
  <color name="sage_pale">#C8D4C0</color>
  <color name="sage_bg">#EAF0EA</color>

  <!-- Accent -->
  <color name="accent">#D4875A</color>
  <color name="accent_pale">#F8EEE4</color>

  <!-- Ink -->
  <color name="ink">#1E2D22</color>
  <color name="ink_mid">#4A5E50</color>
  <color name="ink_light">#7A8C80</color>
  <color name="ink_muted">#AAAEB0</color>

  <!-- Semantic -->
  <color name="danger">#C04040</color>
  <color name="danger_surface">#FEE8E8</color>
</resources>
```

### 9.2 Status Bar

```kotlin
// Light mode
window.statusBarColor = Color.parseColor("#F0F0EC")
WindowCompat.getInsetsController(window, view)
    .isAppearanceLightStatusBars = true

// Cooking mode
window.statusBarColor = Color.parseColor("#4A6B55")
WindowCompat.getInsetsController(window, view)
    .isAppearanceLightStatusBars = false
```

### 9.3 Material Theme Mapping

```xml
<!-- res/values/themes.xml -->
<style name="Theme.RecipeIndex" parent="Theme.Material3.Light">
  <item name="colorPrimary">#7A9E8A</item>
  <item name="colorPrimaryVariant">#4A6B55</item>
  <item name="colorSecondary">#D4875A</item>
  <item name="colorSurface">#FFFFFF</item>
  <item name="colorBackground">#F0F0EC</item>
  <item name="colorOnPrimary">#FFFFFF</item>
  <item name="colorOnSurface">#1E2D22</item>
  <item name="android:colorBackground">#F0F0EC</item>
</style>
```

---

## 10. Screens Reference

### 10.1 Home

- Background: `#F0F0EC`
- Header: logo icon + "Recipe Index" title + search icon
- Quick Actions: 4 icon-only buttons in a row
- This Week: `#EAF0EA` card with sage accent
- Recent Recipes: horizontal scroll of mini photo cards

### 10.2 Recipe List

- Background: `#F0F0EC`
- White search bar below header
- Photo-dominant recipe cards (photo 148dp, info below)
- FAB (accent `#D4875A`) for add

### 10.3 Recipe Detail

- Hero image full-width, 185dp, with dark gradient overlay
- Title + meta overlaid on gradient
- Action strip: 4 icon-only buttons (Grocery, Meal Plan, Cook Now, Favourite)
- Content sections on white cards

### 10.4 Cooking Mode

- Always white background — never dark mode
- Sage dark header
- Step card with left sage border
- Timer row in sage background
- Icon-only prev/next nav buttons

### 10.5 Grocery List

- Category headers: ink dark `#1E2D22` with sage pale text
- Checked items: strikethrough + 65% opacity
- Amount column: sage dark `#4A6B55`

### 10.6 Meal Planning

- Current week card: sage header
- Past week cards: ink header
- FAB for new plan

### 10.7 Tag Changes Dialog (Import Flow)

- Changed tags: sage-tinted card, two-line before/after layout
- Unchanged tags: grey card, single line with checkmark
- Actions: edit (pencil, accent) + remove (×, danger) stacked vertically on right
- Footer: icon-only cancel + text "Accept all changes" confirm

---

## 11. Open Questions

| Question | Status |
|---|---|
| Logo: final option A / B / C? | ⏳ Pending Alice decision |
| Animation library — built-in Android, Lottie, or custom? | ⏳ TBD |
| Empty state illustration style | ⏳ TBD |
| Dark mode support (cooking mode excluded) | ⏳ TBD |
| Photo placeholder gradient exact stops | ⏳ TBD |

---

## 12. Version History

| Version | Date | Notes |
|---|---|---|
| v2.0 | April 2026 | Sage & Linen theme. New colour system, white+grey surfaces, icon-only buttons, updated logo spec, tag dialog redesign |
| v1.0 | November 2025 | Hearth theme. Terracotta/clay palette, dark recipe cards |
