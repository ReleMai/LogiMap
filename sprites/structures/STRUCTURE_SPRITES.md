# Structure Sprites

## Villages

### Village Types

#### Agricultural Village
```
Primary Color: #a08040 (wheat/grain brown)
Accent: #60a030 (field green)
Icon: 🌾 Wheat sheaf

Buildings:
- Farmhouses (thatched roofs, wooden walls)
- Barn (large, red/brown)
- Windmill (optional, for larger villages)
- Grain silos (cylindrical)
```

#### Pastoral Village
```
Primary Color: #80a060 (pasture green)
Accent: #c0a080 (wool/animal brown)
Icon: 🐄 Livestock

Buildings:
- Cottages (stone base, thatched roof)
- Sheep pens (wooden fences)
- Cattle barn
- Dairy/cheese house
```

#### Lumber Village
```
Primary Color: #6a4a2a (wood brown)
Accent: #408030 (forest green)
Icon: 🪓 Axe or log

Buildings:
- Log cabins
- Sawmill (large, by water if possible)
- Wood storage (log piles)
- Carpenter workshop
```

#### Mining Village
```
Primary Color: #606060 (stone gray)
Accent: #4a3020 (mine entrance brown)
Icon: ⛏️ Pickaxe

Buildings:
- Stone houses
- Mine entrance (dark opening)
- Ore processing building
- Smith forge
```

#### Fishing Village
```
Primary Color: #4080a0 (water blue)
Accent: #c0a080 (boat/net brown)
Icon: 🐟 Fish

Buildings:
- Dockside houses (stilts near water)
- Fish market
- Boat storage
- Net drying racks
```

### Village Size Variants
```
Small (4-6 buildings): 3x3 tiles
Medium (7-10 buildings): 4x4 tiles
Large (11+ buildings): 5x5 tiles
```

## Cities

### City Structure
```
Size: 6x6 to 8x8 tiles
Primary Color: #707080 (stone gray-blue)
Accent: #c4a574 (gold trim)

Components:
- Castle/Keep (center or highest point)
- City walls (stone with battlements)
- Main gate (wooden doors, stone arch)
- Central square/marketplace
- Guild buildings
- Temple/Church
- Residential district
```

### City Walls
```
Wall Height: Visual 4-6 pixels above buildings
Wall Color: #808090 (gray stone)
Battlements: Rectangular crenellations
Towers: Corner towers, taller than walls
```

## Special Structures

### Tavern/Inn
```
Color: Warm brown (#8a6a4a)
Features:
- Hanging sign
- Chimney smoke
- Windows with warm light glow
```

### Temple
```
Color: White/cream (#e8e0d0)
Features:
- Spire or dome
- Religious symbol
- Stained glass hints
```

### Castle
```
Color: Gray stone (#606570)
Features:
- Multiple towers
- Flags/banners
- Drawbridge
- Moat (optional)
```

## Rendering Notes

### Shadow Direction
- All structures cast shadow to bottom-right
- Shadow opacity: 20-30%
- Shadow offset: ~2 pixels

### Highlight Direction
- Top-left edges get subtle highlight
- Highlight opacity: 10-15%

### Animation
- Chimney smoke (rising particles)
- Flags waving
- Window light flicker (tavern/night)
