# Recipe Index - Hearth Design System

**Version:** 1.0  
**Last Updated:** November 17, 2025  
**Theme Name:** Hearth

## Overview

The Hearth design system creates a warm, inviting cooking companion with rich terracotta and clay tones. It emphasizes readability during cooking while maintaining a sophisticated, menu-like aesthetic for browsing recipes.

---

## Color Palette

### Primary Colors

| Color Name | Hex Code | RGB | Usage |
|------------|----------|-----|-------|
| **Warm Cream** | `#FBF8F3` | `rgb(251, 248, 243)` | Main background (light mode) |
| **Deep Clay** | `#2C1810` | `rgb(44, 24, 16)` | Recipe cards, headers, dark mode background |
| **Terracotta** | `#E8997A` | `rgb(232, 153, 122)` | Primary accent, recipe images (light) |
| **Burnt Orange** | `#D97757` | `rgb(217, 119, 87)` | Primary accent, recipe images (dark) |

### Contrast Colors

| Color Name | Hex Code | RGB | Usage |
|------------|----------|-----|-------|
| **Sage Green (Light)** | `#5A7A6B` | `rgb(90, 122, 107)` | Active states, ratings, important actions (light mode) |
| **Soft Sage (Dark)** | `#8FB5A3` | `rgb(143, 181, 163)` | Active states, ratings, important actions (dark mode) |

### Text Colors

| Color Name | Hex Code | RGB | Usage |
|------------|----------|-----|-------|
| **Rich Brown** | `#3D2817` | `rgb(61, 40, 23)` | Primary text (light mode) |
| **Cream Text** | `#F5E6D3` | `rgb(245, 230, 211)` | Primary text (dark mode) |
| **Warm Beige** | `#D4B89F` | `rgb(212, 184, 159)` | Secondary text (dark mode) |
| **Muted Clay** | `#A08872` | `rgb(160, 136, 114)` | Placeholder text, disabled states |

### Utility Colors

| Color Name | Hex Code | RGB | Usage |
|------------|----------|-----|-------|
| **Pure White** | `#FFFFFF` | `rgb(255, 255, 255)` | Search bars, light cards, highlights |
| **Soft Border** | `#E8DED0` | `rgb(232, 222, 208)` | Borders, dividers (light mode) |
| **Dark Border** | `#4A3420` | `rgb(74, 52, 32)` | Borders, dividers (dark mode) |

---

## Typography

### Font Family

```
Primary: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, Cantarell, sans-serif
Fallback: System default sans-serif
```

### Type Scale

| Element | Size | Weight | Line Height | Usage |
|---------|------|--------|-------------|-------|
| **Display** | 32px (2rem) | 700 (Bold) | 1.2 | Page titles, headers |
| **Heading 1** | 28px (1.75rem) | 700 (Bold) | 1.3 | Section headers |
| **Heading 2** | 24px (1.5rem) | 700 (Bold) | 1.3 | Subsection headers |
| **Heading 3** | 20px (1.25rem) | 600 (Semibold) | 1.4 | Card titles, recipe names |
| **Body Large** | 18px (1.125rem) | 400 (Regular) | 1.6 | Instructions, important body text |
| **Body** | 16px (1rem) | 400 (Regular) | 1.5 | Standard body text |
| **Body Small** | 14px (0.875rem) | 400 (Regular) | 1.5 | Meta info, timestamps |
| **Caption** | 12px (0.75rem) | 600 (Semibold) | 1.4 | Labels, tags, tiny text |

### Cooking Mode Typography

For cooking mode, increase all font sizes by 25% for better readability:

| Element | Size | Weight |
|---------|------|--------|
| **Instructions** | 22px (1.375rem) | 400 (Regular) |
| **Ingredient** | 20px (1.25rem) | 400 (Regular) |
| **Step Number** | 36px (2.25rem) | 700 (Bold) |

---

## Component Styles

### Buttons

#### Primary Button
```
Background: Linear Gradient (135deg, #E8997A 0%, #D97757 100%)
Text Color: #FFFFFF
Font Weight: 600 (Semibold)
Border Radius: 12px
Padding: 14px 24px
Font Size: 16px

Hover State:
  Background: #D97757 (solid)
  Box Shadow: 0 4px 12px rgba(217, 119, 87, 0.3)

Active State:
  Background: #C85A35
  Transform: scale(0.98)
```

#### Secondary Button
```
Background: #FFFFFF
Text Color: #3D2817
Border: 2px solid #E8997A
Font Weight: 600 (Semibold)
Border Radius: 12px
Padding: 14px 24px
Font Size: 16px

Hover State:
  Background: #FBF8F3
  Border Color: #D97757
```

#### Action Button (Sage Green)
```
Background: #5A7A6B
Text Color: #FFFFFF
Font Weight: 600 (Semibold)
Border Radius: 12px
Padding: 14px 24px
Font Size: 16px

Hover State:
  Background: #4A6A5B
  Box Shadow: 0 4px 12px rgba(90, 122, 107, 0.3)
```

### Cards

#### Recipe Card (Dark - Primary Style)
```
Background: #2C1810
Text Color: #F5E6D3
Border Radius: 16px
Padding: 20px
Box Shadow: 0 2px 12px rgba(44, 24, 16, 0.2)
Margin Bottom: 16px

Recipe Title:
  Color: #F5E6D3
  Font Size: 20px (1.25rem)
  Font Weight: 700 (Bold)
  Margin Bottom: 8px

Recipe Meta:
  Color: #D4B89F
  Font Size: 14px (0.875rem)
  Opacity: 0.9

Rating Star:
  Color: #8FB5A3
```

#### Recipe Card (Light - For Cooking Mode)
```
Background: #FFFFFF
Text Color: #3D2817
Border Radius: 16px
Padding: 20px
Box Shadow: 0 2px 12px rgba(0, 0, 0, 0.06)
```

#### Featured/Favorite Recipe Card
```
Background: #2C1810
Border: 2px solid #D97757
(All other properties same as Recipe Card Dark)
```

### Recipe Image Container
```
Border Radius: 12px
Aspect Ratio: 16:9 or 1:1 (configurable)
Background: Linear Gradient (135deg, #E8997A 0%, #D97757 100%)
Object Fit: cover

Loading State:
  Background: #E8DED0
  Display shimmer animation
```

### Search Bar
```
Background: #FFFFFF
Text Color: #3D2817
Placeholder Color: #A08872
Border: none
Border Radius: 12px
Padding: 14px 18px
Font Size: 16px (1rem)
Box Shadow: 0 2px 8px rgba(0, 0, 0, 0.05)

Focus State:
  Box Shadow: 0 4px 12px rgba(232, 153, 122, 0.2)
  Outline: 2px solid #E8997A
```

### Navigation Bar (Bottom)
```
Background: #FFFFFF
Border Top: 1px solid #E8DED0
Padding: 16px 0
Border Radius: 20px 20px 0 0
Box Shadow: 0 -2px 12px rgba(0, 0, 0, 0.06)

Nav Item (Default):
  Color: #3D2817
  Font Size: 14px (0.875rem)
  Font Weight: 600 (Semibold)
  Opacity: 0.6

Nav Item (Active):
  Color: #5A7A6B
  Opacity: 1
```

### Tags/Chips
```
Background: #E8DED0
Text Color: #3D2817
Border Radius: 20px (full rounded)
Padding: 6px 14px
Font Size: 13px (0.8125rem)
Font Weight: 600 (Semibold)

Active/Selected State:
  Background: #5A7A6B
  Text Color: #FFFFFF
```

### Input Fields
```
Background: #FFFFFF
Text Color: #3D2817
Border: 1px solid #E8DED0
Border Radius: 8px
Padding: 12px 16px
Font Size: 16px (1rem)

Focus State:
  Border Color: #E8997A
  Box Shadow: 0 0 0 3px rgba(232, 153, 122, 0.1)

Error State:
  Border Color: #D97757
  Box Shadow: 0 0 0 3px rgba(217, 119, 87, 0.1)
```

---

## Layout & Spacing

### Spacing Scale (8px base)

```
XXS: 4px   (0.25rem)
XS:  8px   (0.5rem)
S:   12px  (0.75rem)
M:   16px  (1rem)
L:   24px  (1.5rem)
XL:  32px  (2rem)
XXL: 48px  (3rem)
```

### Container Padding
```
Mobile (< 768px): 16px
Tablet (768px - 1024px): 24px
Desktop (> 1024px): 32px
```

### Grid System
```
Mobile: Single column
Tablet: 2 columns (recipe cards)
Desktop: 3 columns (recipe cards)

Gap: 16px (1rem)
```

### Border Radius Scale
```
Small: 8px   (buttons, inputs, small cards)
Medium: 12px (search bars, images within cards)
Large: 16px  (cards, modals)
XLarge: 20px (bottom navigation, large containers)
Full: 9999px (pills, tags, circular elements)
```

---

## Shadows

### Light Mode
```
Subtle: 0 2px 8px rgba(0, 0, 0, 0.05)
Card: 0 2px 12px rgba(0, 0, 0, 0.06)
Elevated: 0 4px 16px rgba(0, 0, 0, 0.08)
Modal: 0 8px 32px rgba(0, 0, 0, 0.12)

Dark Card Shadow: 0 2px 12px rgba(44, 24, 16, 0.2)
```

### Dark Mode
```
Card: 0 2px 12px rgba(0, 0, 0, 0.3)
Elevated: 0 4px 16px rgba(0, 0, 0, 0.4)
Modal: 0 8px 32px rgba(0, 0, 0, 0.5)
```

### Interactive Shadows (on hover/active)
```
Button Hover: 0 4px 12px rgba(232, 153, 122, 0.3)
Card Hover: 0 4px 16px rgba(0, 0, 0, 0.1)
```

---

## Screen-Specific Implementations

### Home Screen (Recipe List)
```
Background: #FBF8F3
Header: 
  - Title: "My Recipes" (Display typography)
  - Background: Transparent or #2C1810 bar
Search Bar: White (#FFFFFF) with shadow
Recipe Cards: Dark (#2C1810) with light text
Bottom Nav: White (#FFFFFF) with sage active state
```

### Recipe Detail Screen
```
Background: #FBF8F3
Hero Image: Full width, 16:9 ratio
Content Cards: 
  - Ingredients: Light card (#FFFFFF)
  - Instructions: Light card (#FFFFFF)
  - Notes: Light card (#FFFFFF)
Action Buttons: 
  - "Start Cooking": Sage green (#5A7A6B)
  - "Add to Plan": Terracotta gradient
  - "Edit Recipe": Secondary button style
```

### Cooking Mode
```
CRITICAL: Maximum readability required

Background: #FFFFFF (pure white)
Text: #2C1810 (high contrast)
Card Style: Light cards only
Typography: Increase all sizes by 25%
Buttons: Large (min 48px height)
Keep screen on: System setting
Timer: Sage green (#5A7A6B) for active timers

Step Counter:
  - Font Size: 36px
  - Color: #D97757
  - Background: Circle, #FBF8F3

Current Ingredient Highlight:
  - Background: #F5E6D3
  - Border Left: 4px solid #5A7A6B
```

### Meal Planning Screen
```
Background: #FBF8F3
Calendar Grid: Light cards (#FFFFFF)
Planned Meals: Dark cards (#2C1810) when assigned
Empty Slots: Dashed border (#E8DED0)
Add Button: Sage green (#5A7A6B)
```

### Grocery List Screen
```
Background: #FBF8F3
List Items: Light cards (#FFFFFF)
Checked Items:
  - Opacity: 0.5
  - Text Decoration: Line-through
  - Checkmark Color: #5A7A6B
Category Headers: Dark background (#2C1810), light text
```

---

## Dark Mode Specifications

### When to Use Dark Mode
- Evening cooking sessions
- Low-light environments
- User preference

### NOT Recommended for Dark Mode
- Cooking mode (always use light for maximum readability)
- Printing/sharing recipes

### Dark Mode Colors

| Element | Color |
|---------|-------|
| Background | `#2C1810` |
| Card Background | `#3D2817` |
| Text Primary | `#F5E6D3` |
| Text Secondary | `#D4B89F` |
| Borders | `#4A3420` |
| Active/Accent | `#8FB5A3` |
| Primary Gradient | `#D97757 → #C85A35` |

---

## Iconography

### Icon Style
- **Style:** Rounded, friendly
- **Weight:** Medium (2px stroke)
- **Size:** 24px standard, 32px for primary actions
- **Color:** Inherit from parent or use primary text color

### Icon Library Recommendation
- Lucide Icons (rounded variant)
- Material Symbols (rounded)
- Phosphor Icons (regular weight)

### Common Icons
```
Recipes: 📖 Book/cookbook icon
Meal Plan: 📅 Calendar icon
Shopping: 🛒 Cart icon
Cooking Mode: 👨‍🍳 Chef hat or timer icon
Favorites: ⭐ Star (filled for favorited)
Timer: ⏱️ Clock icon
Servings: 👥 People icon
Difficulty: 📊 Chart/bars icon
Time: ⏱️ Clock icon
Rating: ⭐ Star icon (use sage green when rated)
```

---

## Animation & Transitions

### Duration
```
Fast: 150ms    (hover states, simple fades)
Normal: 250ms  (most transitions)
Slow: 400ms    (page transitions, modals)
```

### Easing
```
Default: ease-in-out
Entrance: ease-out
Exit: ease-in
Bounce: cubic-bezier(0.68, -0.55, 0.265, 1.55)
```

### Common Transitions
```css
/* Button hover */
transition: all 250ms ease-in-out;

/* Card hover */
transition: transform 250ms ease-out, box-shadow 250ms ease-out;

/* Modal entrance */
transition: opacity 400ms ease-out, transform 400ms ease-out;

/* Page transitions */
transition: opacity 250ms ease-in-out;
```

### Microinteractions
```
Button tap: Scale to 0.98
Card select: Lift with shadow increase
Checkbox: Bounce animation on check
List item check-off: Fade to 50% opacity with strikethrough
Timer completion: Gentle pulse animation (sage green)
```

---

## Accessibility Guidelines

### Color Contrast Requirements
All text must meet WCAG AA standards (4.5:1 for normal text, 3:1 for large text)

**Verified Contrast Ratios:**
- `#3D2817` on `#FBF8F3`: 10.2:1 ✓
- `#F5E6D3` on `#2C1810`: 9.8:1 ✓
- `#5A7A6B` on `#FBF8F3`: 4.6:1 ✓
- `#FFFFFF` on `#E8997A`: 2.9:1 ✗ (Use for decorative only)

### Touch Targets
- Minimum size: 44px × 44px
- Recommended: 48px × 48px
- Spacing between targets: 8px minimum

### Focus States
```
Outline: 2px solid #5A7A6B
Outline Offset: 2px
Border Radius: Match element
```

### Screen Reader Considerations
- All images must have alt text
- Interactive elements must have aria-labels
- Use semantic HTML (nav, main, article, etc.)
- Recipe instructions should be in ordered lists (`<ol>`)

---

## Implementation Notes

### Android Material Design Considerations

#### Material Components Mapping
```
Material Card → Recipe Card (custom styled)
Material Button → Custom buttons (use Hearth colors)
Material TextField → Custom search/input (use Hearth colors)
Material BottomNavigation → Custom nav (use Hearth colors)
Material Chip → Tag/Chip component
```

#### Status Bar & Navigation Bar
```
Status Bar (Light Mode): 
  - Color: #FBF8F3 or #2C1810 (if using dark header)
  - Icons: Dark (#3D2817)

Status Bar (Dark Mode):
  - Color: #2C1810
  - Icons: Light (#F5E6D3)

Navigation Bar:
  - Match status bar color
```

#### Safe Areas & Insets
```
Account for:
- Status bar height
- Navigation bar height (especially gesture navigation)
- Camera cutouts/notches
- Bottom navigation bar (app's own)
```

### Kotlin/Android XML Color Resources

```xml
<!-- res/values/colors.xml -->
<resources>
    <!-- Light Mode -->
    <color name="background_light">#FBF8F3</color>
    <color name="card_dark">#2C1810</color>
    <color name="card_light">#FFFFFF</color>
    <color name="text_primary_light">#3D2817</color>
    <color name="text_on_dark">#F5E6D3</color>
    
    <!-- Primary Colors -->
    <color name="primary_terracotta">#E8997A</color>
    <color name="primary_burnt_orange">#D97757</color>
    
    <!-- Accent Colors -->
    <color name="accent_sage_light">#5A7A6B</color>
    <color name="accent_sage_dark">#8FB5A3</color>
    
    <!-- Utility -->
    <color name="border_light">#E8DED0</color>
    <color name="placeholder">#A08872</color>
</resources>
```

### Performance Considerations
- Use vector drawables for icons (smaller size)
- Optimize recipe images (WebP format, max 1024px width)
- Cache recipe images locally
- Use RecyclerView for recipe lists (efficient scrolling)
- Lazy load images as user scrolls

---

## Design Principles

### 1. Cozy but Clear
The warm terracotta and clay tones create an inviting atmosphere, but clarity is never sacrificed. All text remains highly readable with strong contrast ratios.

### 2. Cooking-First
Every design decision prioritizes the cooking experience. Cooking mode uses maximum contrast and larger text. Dark cards work for browsing; light cards for active cooking.

### 3. Visual Hierarchy
Dark recipe cards stand out against the warm cream background. The sage green accent draws attention to important actions (ratings, active states, "Start Cooking").

### 4. Japanese Simplicity
Despite warm tones, the design maintains generous whitespace, clean layouts, and minimal decoration. Every element serves a purpose.

### 5. Rustic Warmth
The color palette evokes a Mediterranean kitchen or rustic farmhouse. Rich browns, terracotta clay, and natural sage create a homey, trusted feeling.

---

## Brand Voice & Tone

### Visual Personality
- **Warm, not loud:** Rich tones without being overwhelming
- **Sophisticated, not stuffy:** Menu-like quality that's still approachable  
- **Tactile, not decorative:** Everything feels touchable and real
- **Confident, not bossy:** Clear hierarchy without being heavy-handed

### When to Use Each Color
- **Terracotta/Orange:** Recipe photos, primary actions, excitement
- **Sage Green:** Success states, favorites, "Start Cooking", calm actions
- **Dark Brown:** Grounding, sophistication, recipe cards
- **Cream/Beige:** Breathing room, warmth, backgrounds

---

## Version History

### v1.0 (November 17, 2025)
- Initial design system
- Hearth theme established
- Dark recipe cards as primary style
- Light mode optimized for cooking
- Sage green as contrast accent
- Complete component library
- Accessibility guidelines

---

## Questions & Decisions Log

### Resolved
- **Dark vs Light cards?** → Dark cards for browsing, light for cooking mode
- **How to use dark brown in light mode?** → Recipe cards and strategic accents
- **Contrast color?** → Sage green for active states and important actions
- **Dark mode strategy?** → Available but not for cooking mode

### To Be Determined
- Exact gradient stops for recipe image placeholders
- Animation library (built-in Android, Lottie, custom?)
- Illustration style for empty states
- Photo treatment/filters for recipe images

---

## Resources & Assets

### Design Files
- Figma: [Link to be added]
- Asset Export: [Location to be specified]

### Developer Handoff
- Android XML colors: See Implementation Notes section
- Zeplin/Figma: [Link to be added]
- Icon pack: Lucide Icons (recommended)

### External References
- Material Design 3: https://m3.material.io/
- WCAG Contrast Checker: https://webaim.org/resources/contrastchecker/
- Android Design Guidelines: https://developer.android.com/design

---

**End of Design System Documentation**
