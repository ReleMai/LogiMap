# Visual Effects

## Particle Systems

### Gathering Particles
```
Type: Resource bits flying out
Count: 5-10 particles per harvest
Color: Based on resource type
- Wood: Brown (#6a4a2a)
- Stone: Gray (#808080)
- Grain: Golden (#d0a020)
- Fish: Blue-silver (#80a0c0)
Motion: Burst outward, then fall with gravity
Duration: 0.5-1 second
Size: 2-4 pixels
```

### Water Splash
```
Trigger: Fish jumping, player entering water
Count: 8-12 droplets
Color: White/light blue (#c0e0ff)
Motion: Arc upward, then fall
Duration: 0.3-0.5 seconds
Additional: Ring ripple expanding outward
```

### Dust/Dirt Puff
```
Trigger: Walking on dirt, mining
Count: 3-6 particles
Color: Tan/brown (#a09080)
Motion: Rise and disperse
Duration: 0.3-0.5 seconds
Opacity: Fades out
```

### Sparkle/Glint
```
Trigger: Gold ore, treasure, magic items
Count: 1-3 at a time
Color: White or yellow (#ffffff, #ffff80)
Motion: Appear, grow, shrink, disappear
Duration: 0.2-0.4 seconds
Pattern: Random positions on target
```

### Smoke
```
Trigger: Chimneys, campfires, forges
Count: Continuous stream, 1-2 per 0.5 seconds
Color: Gray gradient (#808080 to #404040)
Motion: Rise slowly, drift slightly, expand
Duration: 2-3 seconds per particle
Opacity: Starts solid, fades completely
```

## Weather Effects

### Rain
```
Density: Light (50/frame), Medium (100), Heavy (200)
Color: Light blue-gray (#a0c0d0)
Motion: Fall at angle (wind factor)
Speed: 200-400 pixels/second
Splash: Small ripple on ground
Additional: Puddle sprites on ground
```

### Snow
```
Density: Light (30/frame), Medium (60), Heavy (100)
Color: White (#ffffff)
Motion: Gentle fall with slight drift
Speed: 50-100 pixels/second
Accumulation: White overlay on ground tiles
```

### Fog
```
Type: Overlay layer
Color: White/gray (#e0e0e0)
Opacity: 10-40% based on density
Motion: Slow drift
Effect: Reduces visibility, dampens colors
```

## Ambient Effects

### Firelight Flicker
```
Source: Torches, campfires, lanterns
Color: Orange-yellow (#ff8020 to #ffff40)
Effect: Pulsing brightness
Range: 2-3 tile radius
Blend: Additive
```

### Day/Night Tint
```
Dawn: Light orange-pink overlay
Day: No tint (or subtle warm)
Dusk: Orange-purple gradient
Night: Deep blue overlay (#0a1020)
Moon: Subtle silver-blue light
```

### Water Reflection
```
Condition: Calm water, daytime
Color: Sky color reflection
Effect: Shimmering, broken by waves
Intensity: 20-30% opacity
```

## UI Effects

### Button Hover
```
Effect: Slight brightness increase
Color shift: +10% lightness
Duration: Instant
Additional: Subtle glow
```

### Menu Transition
```
In: Fade in (0.2s) + slight scale up
Out: Fade out (0.2s) + slight scale down
Backdrop: Dim game view to 50%
```

### Progress Bar Shimmer
```
Effect: Moving highlight across bar
Color: White at 30% opacity
Width: 8-10 pixels
Speed: 2-3 passes per second
Motion: Left to right, loop
```

### Text Popup
```
Trigger: Damage numbers, gold gained, XP
Motion: Rise upward (30-50 pixels)
Duration: 1-1.5 seconds
Fade: Last 0.3 seconds
Bounce: Optional slight bounce at peak
```

### Selection Highlight
```
Target: Structures, resources, NPCs
Effect: Pulsing outline or glow
Color: --gold (#c4a574)
Pulse: 1-2 Hz
Width: 2 pixels
```

## Combat Effects (Future)

### Hit Flash
```
Duration: 0.1 seconds
Effect: Target flashes white
```

### Slash Trail
```
Color: White streak
Duration: 0.2 seconds
Shape: Arc following weapon
```

### Blood Splatter (if applicable)
```
Color: Red (#c02020)
Type: Small drops
Duration: Persists briefly, then fades
```

## Performance Notes

### Particle Limits
- Maximum active particles: 200-300
- Culling: Remove particles off-screen
- LOD: Reduce particles at low zoom

### Animation Frames
- Target: 60 FPS
- Phase variables: Use deltaTime
- Batch rendering: Group same-type effects

### Memory
- Reuse particle objects (object pool)
- Pre-calculate common paths
- Dispose unused effect systems
