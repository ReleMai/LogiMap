# Resource Node Sprites

## Lumber Nodes (Trees)

### Tree Types

#### Oak
```
Trunk Color: #5a4030 (brown)
Canopy Color: #3a6a20 (green)
Shape: Round, full canopy
Size: 2x2 tiles

Rendering:
- Draw trunk (rectangle with slight taper)
- Draw canopy (overlapping circles)
- Add shadow underneath
```

#### Pine
```
Trunk Color: #4a3528 (dark brown)
Canopy Color: #2a5a30 (dark green)
Shape: Triangular/conical
Size: 1x2 tiles (narrow and tall)

Rendering:
- Draw trunk (thin rectangle)
- Draw triangular canopy
- Multiple layers getting smaller toward top
```

#### Birch
```
Trunk Color: #e0d8d0 (white/cream)
Trunk Detail: Horizontal dark lines
Canopy Color: #70a040 (light green)
Shape: Oval, airy canopy
Size: 2x2 tiles
```

#### Maple
```
Trunk Color: #6a4a30 (warm brown)
Canopy Color: #80a030 (yellow-green) OR #c04020 (autumn red)
Shape: Broad, spreading
Size: 2x3 tiles
```

### Tree States
```
Healthy: Full canopy, vibrant color
Harvested: Stump only (brown cylinder)
Regrowing: Small sapling (thin trunk, few leaves)
```

## Quarry Nodes (Stone)

### Stone Types

#### Granite
```
Color: #808080 (gray)
Texture: Speckled (lighter and darker spots)
Shape: Angular boulder cluster
Size: 2x2 tiles
```

#### Slate
```
Color: #505860 (blue-gray)
Texture: Layered/stacked appearance
Shape: Flat, layered rocks
Size: 2x2 tiles
```

#### Marble
```
Color: #e0e0e8 (white with gray veins)
Texture: Swirled veining pattern
Shape: Smooth, rounded boulders
Size: 2x2 tiles
```

### Quarry States
```
Full: Large rock formation
Partially mined: Chips and rubble around base
Depleted: Pit with rubble
Regenerating: Small rocks appearing
```

## Fishery Nodes (Water)

### Fish Types

#### Trout
```
Base Water Color: #4080a0 (clear blue)
Fish Color: #c0a080 (tan/brown)
Indicator: Jumping fish sprite, ripples
Size: 2x2 tiles (on water)
```

#### Salmon
```
Base Water Color: #3070a0 (river blue)  
Fish Color: #e08060 (pink/salmon)
Indicator: Fish shadow moving, splash
Size: 2x2 tiles
```

#### Catfish
```
Base Water Color: #506050 (murky green)
Fish Color: #605040 (dark brown)
Indicator: Bubbles, whisker silhouette
Size: 2x2 tiles
```

### Fishery Rendering
```
1. Draw water base (with wave animation)
2. Draw fishing spot marker (buoy or ripples)
3. Occasionally draw jumping fish (animated)
4. Add foam/splash effects
```

## Ore Nodes (Mining)

### Ore Types

#### Iron
```
Rock Color: #606060 (gray)
Ore Veins: #804020 (rusty brown-red)
Sparkle: Occasional metallic glint
Size: 2x2 tiles
```

#### Copper
```
Rock Color: #707060 (gray-green)
Ore Veins: #a06030 (copper orange)
Sparkle: Green patina spots
Size: 2x2 tiles
```

#### Gold
```
Rock Color: #808070 (tan-gray)
Ore Veins: #d0a020 (gold yellow)
Sparkle: Bright golden glints (animated)
Size: 2x2 tiles
```

## Pastoral Nodes (Livestock)

### Animal Types

#### Sheep
```
Body Color: #e8e0d0 (cream/white wool)
Face/Legs: #303030 (dark gray)
Shape: Fluffy oval body
Size: Sprite within 1x1 tile
Animation: Grazing head bob
```

#### Cow
```
Body Color: #6a4030 (brown) or #e0d8d0 (white with spots)
Spots: Random dark patches
Shape: Large rectangular body
Size: Sprite spans ~1.5 tiles
Animation: Tail swish, head turn
```

#### Pig
```
Body Color: #d0a0a0 (pink)
Nose: #c08080 (darker pink, round)
Shape: Round/oval body
Size: Sprite within 1x1 tile
Animation: Snout rooting motion
```

#### Chicken
```
Body Color: #d0a060 (tan/brown) or #e0e0e0 (white)
Comb: #c02020 (red)
Shape: Small oval with tail feathers
Size: Small sprite, ~0.5 tiles
Animation: Pecking motion
```

### Pastoral Node Layout
```
Base: Fenced area (3x3 tiles)
Fence Color: #8a6a4a (wood brown)
Interior: Grass with hay patches
Animals: 3-5 per node, scattered positions
```

## Farmland Nodes (Crops)

### Crop Growth Stages
```
1. Seeds: Brown soil with small mounds
2. Sprouts: Tiny green shoots
3. Growing: Medium green stalks
4. Mature: Full height, green-yellow
5. Ripe: Golden/harvest color
6. Harvested: Brown stubble
```

### Crop Types

#### Wheat
```
Ripe Color: #d0a020 (golden)
Shape: Thin vertical stalks with grain heads
Pattern: Rows with slight variation
```

#### Barley
```
Ripe Color: #c0a030 (pale gold)
Shape: Similar to wheat, drooping heads
Pattern: Dense rows
```

#### Oat
```
Ripe Color: #a08040 (tan)
Shape: Delicate, feathery heads
Pattern: Scattered within rows
```

#### Rye
```
Ripe Color: #908060 (gray-tan)
Shape: Tall, thin stalks
Pattern: Uniform rows
```

## General Resource Node Guidelines

### Size Indicators
- **Small**: 1-2 tiles, fewer resources
- **Medium**: 2-3 tiles, standard yield
- **Large**: 3-4 tiles, high yield

### Harvest Visual Feedback
1. Resource shrinks/disappears partially
2. Particles fly out (resource bits)
3. After cooldown, resource regrows

### Interaction Indicators
- Hover: Subtle highlight/glow
- Clickable: Slight pulse animation
- Selected: Outline or selection ring
