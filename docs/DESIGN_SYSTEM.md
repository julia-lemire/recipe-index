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

Two-level surface system: grey base with white cards floating on top.

| Name | Hex | Token (Color.kt) | Usage |
|---|---|---|---|
| Background | `#F0F0EC` | `Background` | Screen background — soft grey |
| Surface / Card | `#FFFFFF` | `White` | Card surfaces, modals, bottom nav |
| Surface Alt | `#E8E8E2` | `SurfaceAlt` | Inner sections, no-change tag rows |
| Border | `#DDDDD6` | `Border` | Card borders, dividers |
| Border Strong | `#CECEC6` | `BorderStrong` | Phone frame, focused inputs |

### 1.2 Sage Green (Primary)

| Name | Hex | Token | Material 3 Role | Usage |
|---|---|---|---|---|
| Sage Dark | `#4A6B55` | `SageDark` | `tertiary` | Primary buttons, cooking header, grocery category headers |
| Sage | `#7A9E8A` | `Sage` | `primary` | Active nav icons, FAB, active tags, checkmarks, step progress |
| Sage Pale | `#C8D4C0` | `SagePale` | `primaryContainer` | App icon background, inactive chips, progress dots |
| Sage Background | `#EAF0EA` | `SageBackground` | `tertiaryContainer` | This Week card, timer row, changed-tag card tint |

### 1.3 Warm Gold Accent

| Name | Hex | Token | Material 3 Role | Usage |
|---|---|---|---|---|
| Accent | `#D4875A` | `Accent` | `secondary` | Import icon, step numbers, FAB, accent actions |
| Accent Pale | `#F8EEE4` | `AccentPale` | `secondaryContainer` | Edit button background in tag dialog |

### 1.4 Ink (Text)

| Name | Hex | Token | Usage |
|---|---|---|---|
| Ink | `#1E2D22` | `Ink` | Primary text, recipe names, headings |
| Ink Mid | `#4A5E50` | `InkMid` | Secondary text, ingredient amounts, section labels |
| Ink Light | `#7A8C80` | `InkLight` | Metadata, timestamps, captions |
| Ink Muted | `#AAAEB0` | `InkMuted` | Placeholder text, inactive nav labels, disabled |

### 1.5 Semantic

| Name | Hex | Token | Usage |
|---|---|---|---|
| Danger | `#C04040` | `Danger` | Remove/delete icon strokes |
| Danger Surface | `#FEE8E8` | `DangerSurface` | Remove button backgrounds |

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

| Role | Font | Fallback | Usage |
|---|---|---|---|
| Display | Lora (serif) | `FontFamily.Serif` | Page titles, recipe names, section headers, modal titles, step numbers |
| Body / UI | DM Sans (sans-serif) | `FontFamily.SansSerif` | All other text, labels, metadata, navigation |

To use exact fonts: add Lora and DM Sans to `res/font/` and update the `DisplayFont` / `BodyFont` variables in `Type.kt`.

### 2.2 Type Scale

| Token (Typography) | Size | Weight | Line Height | Font | Usage |
|---|---|---|---|---|---|
| `displayLarge` | 18sp | 600 | 1.25 | Lora | Screen titles in headers |
| `titleLarge` | 16sp | 600 | 1.3 | Lora | Recipe card names |
| `titleMedium` | 14sp | 600 | 1.3 | Lora | Meal plan dates, modal headers |
| `titleSmall` | 13sp | 600 | 1.4 | Lora | Section labels (Quick actions, This week) |
| `bodyLarge` | 16sp | 400 | 1.6 | DM Sans | Cooking mode instructions |
| `bodyMedium` | 13sp | 400 | 1.5 | DM Sans | Ingredients, grocery items |
| `bodySmall` | 11sp | 400 | 1.4 | DM Sans | Servings, cook time, dates |
| `labelLarge` | 10sp | 500 | 1.3 | DM Sans | Nav labels, tag chips, category headers |
| `labelMedium` | 9sp | 500 | 1.3 | DM Sans | Footnotes, step counter x/y |

### 2.3 Cooking Mode

Increase all sizes by 25% for cooking context readability:

| Token | Normal | Cooking Mode |
|---|---|---|
| Instructions | 13sp | 16sp (bodyLarge) |
| Step number | 30sp | 36sp |
| Ingredient | 13sp | 16sp |

---

## 3. Logo

### 3.1 Concept

Open cookbook with visible cover vs inner pages contrast:
- **Left side:** Gold book cover with spine
- **Spine (centre):** Sage green binding strip
- **Right side:** Cream inner pages with text lines
- **Leaf motif:** Sage leaf on inner pages — the only coloured element on the cream side

### 3.2 Android Assets

| File | Usage |
|---|---|
| `res/drawable/ic_launcher_foreground.xml` | Adaptive icon foreground (108dp viewport) |
| `res/drawable/ic_launcher_background.xml` | Adaptive icon background (`#EAF0EA`) |
| `res/drawable/ic_logo_header.xml` | In-app header logo (38×38dp) |

### 3.3 Colours

| Element | Hex |
|---|---|
| Cover back/spine edge | `#906030` |
| Cover front face | `#C8A060` |
| Cover inner folds | `#A07840` |
| Spine binding strip | `#4A6B55` |
| Inner pages (back) | `#EDE8DC` |
| Inner pages (front) | `#F5F0E8` |
| Page lines | `#D8D0C0` |
| Leaf | `#7A9E8A` |
| Leaf vein | `#4A6B55` |
| Icon background | `#EAF0EA` |

### 3.4 Sizes

| Context | Icon size | Border radius |
|---|---|---|
| App launcher icon | 108dp (with 18dp safe zone) | 22dp |
| App header (in-app) | 38×38dp | 10dp |

### 3.5 Rules

- Never display the app name "RecipeIndex" inside or below the icon
- Minimum display size: 36×36dp
- Do not use the icon on dark backgrounds
- The icon background (`#EAF0EA`) is part of the asset — do not crop it out

---

## 4. Components

### 4.1 Quick Action Buttons

Icon-only square buttons in a single horizontal row. Labels sit below each button.

```
Layout:     4 equal-width buttons in a flex row, gap 8dp
Size:       flex-1, height 52dp
Shape:      border-radius 14dp
Icon size:  22×22dp

Import (neutral):
  containerColor: colorScheme.surface
  border:         1dp solid colorScheme.outline
  iconColor:      colorScheme.secondary (accent/gold)

Recipes / Grocery (sage):
  containerColor: colorScheme.primary (sage)
  iconColor:      colorScheme.onPrimary (white)

Meal Plan (sage dark):
  containerColor: colorScheme.tertiary (sage dark)
  iconColor:      colorScheme.onTertiary (white)

Label below button:
  style: labelLarge, colorScheme.onSurfaceVariant
```

### 4.2 Recipe Cards

```
Card:
  Background:    colorScheme.surface (#FFFFFF)
  Border:        1dp solid colorScheme.outline (#DDDDD6)
  Border radius: 17dp
  Shadow:        shadow-subtle

Photo area:
  Height:        148dp
  Gradient ov:   linear bottom 52dp, rgba(30,45,34,0.48) → transparent

Recipe name:     titleLarge (Lora 16sp 600), colorScheme.onSurface
Meta row:        bodySmall (DM Sans 11sp), colorScheme.onSurfaceVariant
Tags:            labelLarge 10sp, colorScheme.tertiaryContainer bg, colorScheme.onTertiaryContainer text
```

### 4.3 Bottom Navigation

```
Background:    colorScheme.surface (#FFFFFF)
Border top:    1dp solid colorScheme.outline
Nav inactive:  colorScheme.onSurfaceVariant (InkMuted)
Nav active:    colorScheme.primary (Sage)
Label:         labelMedium 9sp
```

### 4.4 Search Bar

```
Background:    colorScheme.surface
Border:        1dp solid colorScheme.outline
Border radius: 13dp
Padding:       9dp 13dp
Placeholder:   bodyMedium, colorScheme.onSurfaceVariant

Focus state:
  Border:  1dp solid colorScheme.primary
  Shadow:  0 0 0 3dp rgba(122,158,138,0.15)
```

### 4.5 FAB (Floating Action Button)

```
Size:          42×42dp
Border radius: 13dp
Background:    colorScheme.secondary (#D4875A)
Icon:          18×18dp, colorScheme.onSecondary (#FFFFFF)
Position:      bottom 70dp, right 13dp
Shadow:        shadow-fab
```

### 4.6 Tags / Chips

```
Inactive:
  Background: colorScheme.tertiaryContainer (#EAF0EA)
  Text:       colorScheme.onTertiaryContainer, labelLarge 10sp
  Padding:    3dp 8dp, radius 20dp

Active:
  Background: colorScheme.primary (#7A9E8A)
  Text:       colorScheme.onPrimary (#FFFFFF)
```

### 4.7 Grocery List

```
Category header:
  Background:    colorScheme.onBackground (#1E2D22)
  Text:          colorScheme.primaryContainer (#C8D4C0), 11sp 500
  Border radius: 10dp, padding 7dp 12dp

Checkbox (unchecked):  size 19dp, border 1.5dp BorderStrong, bg White
Checkbox (checked):    bg Sage (#7A9E8A), checkmark White

Item name (checked):   line-through, InkMuted 65% opacity
Amount:                InkMid (#4A5E50), bodySmall
```

### 4.8 Cooking Mode

```
Screen background:  #FFFFFF (always — never dark mode)
Header:             colorScheme.tertiary (#4A6B55) bg, White text

Step progress:  Done #7A9E8A · Current #D4875A · Upcoming #CECEC6

Step card:
  Border left:  3dp solid colorScheme.primary (#7A9E8A)
  Border other: 1dp solid colorScheme.outline
  Step number:  Lora 30sp 600, colorScheme.secondary (#D4875A)
  Text:         DM Sans 15sp, colorScheme.onSurface

Timer row:      colorScheme.tertiaryContainer bg, Lora 19sp 600 value

Step nav:       Prev = SurfaceAlt · Next = colorScheme.primary · Icon-only
```

### 4.9 Tag Changes Dialog

```
Changed tag row:
  Background: colorScheme.tertiaryContainer (#EAF0EA)
  Before:     BEFORE label (#AAAEB0), value line-through (#4A5E50)
  After:      AFTER label (#4A6B55), value bold (#4A6B55)
  Edit:       28dp circle, AccentPale bg, Accent pencil
  Remove:     28dp circle, DangerSurface bg, Danger ×

No-change row:  SurfaceAlt bg, single line with checkmark

Footer:
  Cancel: 42dp icon-only, SurfaceAlt bg
  Accept: flex-1, colorScheme.primary bg, "Accept all changes"
```

### 4.10 Meal Plan Cards

```
Current week header:   colorScheme.primary (#7A9E8A)
Past week headers:     colorScheme.onBackground (#1E2D22)
Header title:          Lora 14sp 600, White
"This week" badge:     rgba(255,255,255,0.20) bg, White text
Action icons:          28dp circles, rgba(255,255,255,0.15) bg
Recipe bullets:        4dp circle, colorScheme.primary
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

- Library: Lucide Icons (rounded variant) — or Material Icons Extended
- Weight: 1.4–1.6dp stroke
- Size: 12–13dp overlays · 17–18dp action buttons · 19–20dp nav
- All buttons outside modals are icon-only (`contentDescription` required)

### 6.1 Common Assignments

| Action | Lucide / Material Icon |
|---|---|
| Import | `download` / `Icons.Default.Download` |
| All Recipes | `book-open` / `Icons.Default.MenuBook` |
| Grocery | `shopping-cart` / `Icons.Default.ShoppingCart` |
| Meal Plan | `calendar` / `Icons.Default.CalendarMonth` |
| Search | `search` / `Icons.Default.Search` |
| Filter | `sliders-horizontal` / `Icons.Default.FilterList` |
| Cook Now | `play` (filled) / `Icons.Default.PlayArrow` |
| Favourite | `star` / `Icons.Default.Star` |
| Share | `share-2` / `Icons.Default.Share` |
| Edit | `pencil` / `Icons.Default.Edit` |
| Delete | `x` / `Icons.Default.Close` |
| Back | `chevron-left` / `Icons.Default.ChevronLeft` |
| Timer | `clock` / `Icons.Default.AccessTime` |

---

## 7. Shadows

| Token | Value | Usage |
|---|---|---|
| `shadow-subtle` | `0 2dp 8dp rgba(0,0,0,0.05)` | Cards at rest |
| `shadow-card` | `0 2dp 12dp rgba(0,0,0,0.06)` | Elevated cards |
| `shadow-modal` | `0 10dp 40dp rgba(0,0,0,0.20)` | Modals, bottom sheets |
| `shadow-fab` | `0 4dp 12dp rgba(212,135,90,0.35)` | FAB |

---

## 8. Animation

| Token | Duration | Usage |
|---|---|---|
| `anim-fast` | 150ms | Hover, checkbox |
| `anim-normal` | 250ms | Most transitions |
| `anim-slow` | 400ms | Modal entrance, page transitions |

Microinteractions:
- Button tap: `scale(0.98)`
- Checkbox check: bounce `cubic-bezier(0.68,-0.55,0.265,1.55)`
- List item check-off: fade to 65% opacity + strikethrough
- Timer completion: gentle sage green pulse

---

## 9. Android Implementation

### 9.1 Key Files

| File | Purpose |
|---|---|
| `ui/theme/Color.kt` | All color tokens |
| `ui/theme/HearthTheme.kt` | Material 3 color scheme (`RecipeIndexTheme`) |
| `ui/theme/Type.kt` | Typography scale (`RecipeIndexTypography`) |
| `res/values/colors.xml` | XML color resources |
| `res/values/themes.xml` | XML splash/startup theme |
| `res/drawable/ic_launcher_background.xml` | Launcher icon background |
| `res/drawable/ic_launcher_foreground.xml` | Launcher icon foreground |
| `res/drawable/ic_logo_header.xml` | In-app header logo (38dp) |

### 9.2 Status Bar

```kotlin
// Light mode
window.statusBarColor = Color.parseColor("#F0F0EC")
WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true

// Cooking mode
window.statusBarColor = Color.parseColor("#4A6B55")
WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
```

### 9.3 Bundling Lora + DM Sans

Add font files to `res/font/` (e.g. `lora_regular.ttf`, `lora_semibold.ttf`, `dm_sans_regular.ttf`, `dm_sans_medium.ttf`) then update `Type.kt`:

```kotlin
private val DisplayFont = FontFamily(
    Font(R.font.lora_regular, FontWeight.Normal),
    Font(R.font.lora_semibold, FontWeight.SemiBold),
)
private val BodyFont = FontFamily(
    Font(R.font.dm_sans_regular, FontWeight.Normal),
    Font(R.font.dm_sans_medium, FontWeight.Medium),
)
```

---

## 10. Screens Reference

### 10.1 Home

- Header: `ic_logo_header` + "Recipe Index" (bold) + search button
- Quick Actions: 4 icon-only buttons (Import · Recipes · Grocery · Meal plan)
- This Week: `tertiaryContainer` card with date range + plan name

### 10.2 Recipe List

- Background: `Background` (`#F0F0EC`)
- White search bar below header
- Photo-dominant cards (148dp photo) with FAB (`secondary` `#D4875A`)

### 10.3 Recipe Detail

- Hero image 185dp with dark gradient overlay
- Action strip: 4 icon-only buttons (Grocery · Meal Plan · Cook Now · Favourite)
- Content sections on white cards

### 10.4 Cooking Mode

- Always white background
- `tertiary` (`#4A6B55`) header
- Step card with left `primary` border + step number in `secondary`
- Timer row in `tertiaryContainer`

### 10.5 Grocery List

- Category headers: `onBackground` (`#1E2D22`) + `primaryContainer` text
- Checked items: strikethrough + 65% opacity
- Amount column: `InkMid` (`#4A5E50`)

### 10.6 Meal Planning

- Current week: `primary` header · Past weeks: `onBackground` header
- FAB for new plan

### 10.7 Tag Changes Dialog

- Changed tags: `tertiaryContainer` tinted row, two-line before/after
- No-change tags: `surfaceVariant` row, single line
- Footer: icon-only cancel + "Accept all changes" confirm (`primary` bg)

---

## 11. Open Questions

| Question | Status |
|---|---|
| Bundle Lora + DM Sans fonts locally? | ⏳ TBD |
| Animation library — built-in Android, Lottie, or custom? | ⏳ TBD |
| Empty state illustration style | ⏳ TBD |
| Dark mode support (cooking mode excluded) | ⏳ TBD |

---

## 12. Version History

| Version | Date | Notes |
|---|---|---|
| v2.0 | April 2026 | Sage & Linen theme. New colour system, white+grey surfaces, icon-only buttons, updated logo, tag dialog redesign |
| v1.0 | November 2025 | Hearth theme. Terracotta/clay palette, dark recipe cards |
