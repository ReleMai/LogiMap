# UI Element Sprites

## Color Palette

### Background Colors
```css
--bg-dark: #1a1208;      /* Darkest background */
--bg-medium: #2a1f10;    /* Medium background */
--bg-light: #3a2a15;     /* Light background */
```

### Accent Colors
```css
--gold: #c4a574;         /* Primary accent, borders, headers */
--gold-dark: #8a7554;    /* Darker gold for shadows */
--gold-light: #e8d0a0;   /* Lighter gold for highlights */
```

### Text Colors
```css
--text-white: #e8e0d0;   /* Primary text */
--text-gold: #c4a574;    /* Headers, labels */
--text-gray: #888888;    /* Hints, disabled */
--text-error: #ff6666;   /* Errors, warnings */
--text-success: #66ff66; /* Success, positive */
```

### Button States
```css
--button-normal: #3a2a15;
--button-hover: #4a3a25;
--button-pressed: #2a1a10;
--button-disabled: #2a2010;
```

## Common UI Components

### Panel/Container
```
Background: --bg-dark with 85-95% opacity
Border: --gold, 1-2px width
Border Radius: 8px
Padding: 10-15px
```

### Section Header
```
Font: Georgia, Bold, 14px
Color: --gold
Padding Bottom: 5px
Optional: Underline decoration
```

### Button
```
Background: --button-normal
Border: --gold, 1px
Border Radius: 4px
Padding: 8px 15px
Font: System, 12-14px
Color: --text-white
Hover: --button-hover background
```

### Slider
```
Track: --bg-dark
Fill: --gold
Thumb: --gold with darker border
Width: Full container width
Height: 8px track, 16px thumb
```

### Progress Bar
```
Background: #2a2a2a
Fill: --gold (animated shimmer)
Border: --gold-dark, 1px
Height: 25px
Border Radius: 5px
```

### Text Input
```
Background: --bg-dark
Border: --panel-border (#404040)
Text Color: --text-white
Placeholder: --text-gray
Padding: 8px
Border Radius: 4px
```

## Game HUD Elements

### Time Display
```
Position: Top-right corner
Size: 210x80px
Contents:
- Date text (formatted: "January 1st, 500 AD")
- Time text (formatted: "08:00")
- Time of day label (Morning, etc.)
- Speed indicator (Paused/Normal/2x)
- Sun position bar
- Time control buttons (Pause, Fast)
```

### Time Control Buttons
```
Size: 22x22px each
Spacing: 5px between
Icons:
- Pause: Two vertical bars (||)
- Play: Right-pointing triangle (►)
- Fast: Double arrows (>>)
Border Color: Changes based on active state
```

### Energy Meter
```
Position: Below time display
Size: 210x35px
Bar: Green to yellow to red gradient
Label: "Energy: XX/100"
```

### Minimap
```
Position: Bottom-right corner
Size: 150x150px
Border: --gold, 2px
Features:
- Terrain overview
- Player position dot (bright)
- Town markers
- Viewport rectangle
```

## Menu Screens

### Main Menu
```
Background: Dark with subtle pattern
Title: Large, --gold, centered
Buttons: Vertical stack, centered
- New World
- Load Game
- Settings
- Quit
Button Spacing: 15px
```

### World Generation Menu
```
Layout: Two-column
Left Panel: World settings
Right Panel: Preview + Region selection
Bottom: Generate/Back buttons
```

### Inventory Screen
```
Grid: 6 columns x 4 rows (24 slots)
Slot Size: 40x40px
Slot Background: --bg-medium
Slot Border: --gold-dark (hover: --gold)
Item Stack Count: Bottom-right corner
Tooltip: On hover
```

### Trading/Marketplace
```
Layout: Two panels side-by-side
Left: Available items (NPC inventory)
Right: Player inventory
Center: Transaction buttons
Bottom: Gold display, Confirm/Cancel
```

## Icons

### Resource Icons (16x16px)
```
Wheat: Golden grain bunch
Wood: Brown log section
Stone: Gray rock
Fish: Blue fish shape
Ore: Rock with colored vein
Meat: Pink/red meat cut
```

### Status Icons
```
Heart: #ff4040 (health)
Lightning: #ffff40 (energy)
Coin: #ffd700 (gold)
Clock: #80c0ff (time)
```

### Action Icons
```
Hammer: Crafting
Bag: Inventory
Map: World map
Cog: Settings
Door: Exit/Leave
```

## Tooltips

### Style
```
Background: rgba(20, 20, 30, 0.92)
Border: --gold, 1px
Border Radius: 4px
Padding: 8-10px
Max Width: 250px
Shadow: Subtle drop shadow
```

### Content Structure
```
Title: Bold, --gold
Separator: Thin --gold line
Description: --text-white
Stats: --text-gray with --gold values
Footer: Italic, --text-gray
```

## Notifications/Popups

### Toast Notification
```
Position: Top-center
Background: --bg-dark with 90% opacity
Border: --gold
Duration: 3-5 seconds
Animation: Slide down in, fade out
```

### Dialog Box
```
Background: --bg-medium
Border: --gold, 2px
Title Bar: --bg-dark with --gold text
Content Area: Padded
Buttons: Right-aligned
```

## Fonts

### Primary Font
```
Family: Georgia (serif, medieval feel)
Sizes:
- Headers: 14-16px, Bold
- Body: 11-12px
- Small: 9-10px
- Large: 18-24px (titles)
```

### Monospace (for numbers)
```
Family: Consolas or Courier New
Use for: Gold amounts, stats, coordinates
```
