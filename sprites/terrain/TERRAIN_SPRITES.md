# Terrain Tile Sprites

## Water Tiles

### Deep Ocean
- **Base Color**: #0a2a4a
- **Features**: Animated waves, dark undulating patterns
- **Decorations**: Occasional fish shadows (animated)

### Ocean  
- **Base Color**: #1e4d8b
- **Features**: Wave lines, foam near shores
- **Decorations**: Fish shadows, boat wakes near ports

### Shallow Water
- **Base Color**: #3a7ab8
- **Features**: Lighter waves, visible sandy bottom hints
- **Decorations**: Lilypads, fish, reed shadows

### Coral Reef
- **Base Color**: #40a0a0
- **Features**: Multicolored coral dots
- **Decorations**: Pink, orange, yellow, green coral patches

## Land Tiles

### Grass/Plains/Meadow
```
Base Colors:
- Grassland: #7cba4c
- Plains: #a8c868
- Meadow: #90d050

Texture Details:
- Color variation spots (darker/lighter patches)
- Small grass tuft lines
- Occasional flower dots (pink, yellow, white, purple)
```

### Forest Types
```
Base Colors:
- Forest: #2d6b1a
- Dense Forest: #1a4a0f
- Taiga: #3a6848
- Jungle: #1a5a20

Texture Details:
- Tree canopy shadows (circular dark spots)
- Lighter canopy centers
- Overlapping shadow patterns for dense forest
```

### Desert/Arid
```
Base Colors:
- Desert: #e8d090
- Dunes: #f0d878
- Savanna: #c8b060
- Scrubland: #9a9060

Texture Details:
- Sand ripple lines (wavy horizontal patterns)
- Darker sand spots
- Occasional cactus shadow (scrubland)
```

### Beach
```
Base Color: #e8d4a8

Texture Details:
- Small pebbles (gray ovals)
- Shell-like white specks
- Wet sand gradient near water
```

### Hills/Mountains
```
Base Colors:
- Hills: #8a9a70
- Rocky Hills: #888078
- Mountain: #7a7068
- Mountain Peak: #9a9090

Texture Details:
- Contour shadow arcs
- Small rock shapes for rocky hills
- Snow cap overlays for peaks
- Cracks/lines pattern for mountains
```

### Snow/Cold
```
Base Colors:
- Tundra: #a8b8a0
- Snow: #e8f0f0
- Ice: #c0e0f0
- Glacier: #b0d8e8

Texture Details:
- White sparkle dots
- Blue shadow patches for ice
- Smooth gradient surfaces
```

### Wetlands
```
Base Colors:
- Swamp: #4a6840
- Marsh: #5a7850

Texture Details:
- Murky water patches (dark ovals)
- Moss/algae streaks (green-brown)
- Occasional dead tree silhouettes
```

## Edge Blending

Terrain edges should blend smoothly:
- 25% of tile width/height for blend zone
- Semi-transparent overlay of neighbor color
- Apply to all four cardinal directions
- Check for different terrain types before blending
