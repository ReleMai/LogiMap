# Character Sprites

## Player Character

### Body Structure
The player is rendered as a modular sprite with separate body parts:

```
Body Parts:
├── Head (with hair and headgear slots)
├── Chest (main body, armor slot)
├── Right Arm (main hand weapon)
├── Left Arm (off-hand/shield)
├── Right Leg (leg armor)
└── Left Leg (footwear)
```

### Base Appearance
```
Skin Color: #d4a88a (default medieval tan)
Underwear: #e8e0d0 (white/cream - visible when no clothes)
Outline: #2a1a10 (dark brown)
```

### Animation States
1. **Idle**: Slight breathing motion
2. **Walking**: 
   - Leg swing: ±10 degrees
   - Arm swing: ±8 degrees (opposite to legs)
   - Body bob: ±1.5% of height
3. **Gathering**: Arms in work position (custom per resource)

### Equipment Slots
```
Head Gear:
- None (show hair)
- Hood (various colors)
- Helmet (metal colors)
- Hat (cloth colors)

Body Armor:
- Shirt (cloth, various colors)
- Leather armor (brown tones)
- Chain mail (silver/gray)
- Plate armor (metallic)

Weapons (Main Hand):
- Empty hand
- Pickaxe (mining)
- Axe (lumber)
- Hoe (farming)
- Fishing rod
- Sword (combat)

Off-Hand:
- Empty
- Shield (wood/metal)
- Lantern
- Bag

Legs:
- Pants (various colors)
- Armored greaves

Feet:
- Bare feet
- Boots (leather/metal)
```

### Hair Styles
```
Style 0: Short crop
Style 1: Medium length
Style 2: Long
Style 3: Bald
Style 4: Ponytail

Default Hair Color: #4a3728 (dark brown)
```

### Direction
- `facingRight = true`: Normal orientation
- `facingRight = false`: Mirrored (scaleX = -1)

## NPC Templates

### Village NPCs
```
Farmer: Straw hat, simple clothes, hoe
Blacksmith: Leather apron, hammer
Merchant: Fine clothes, coin pouch
Guard: Helmet, spear/sword
```

### City NPCs
```
Noble: Fine robes, jewelry
Scholar: Robes, book
Priest: Religious garb
Knight: Full armor, sword/shield
```

## Size Reference
```
Base sprite height: ~28 pixels (at 32px cell)
Head: 8px diameter
Body: 10px height
Legs: 10px height
Arms: 8px length
```
