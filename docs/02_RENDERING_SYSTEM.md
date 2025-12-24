# LogiMap Developer Guide - Rendering System

## Table of Contents
1. [Canvas-Based Rendering](#canvas-based-rendering)
2. [Coordinate Systems](#coordinate-systems)
3. [The Render Pipeline](#the-render-pipeline)
4. [Layer Order](#layer-order)
5. [Level of Detail (LOD)](#level-of-detail-lod)
6. [Code Examples](#code-examples)

---

## Canvas-Based Rendering

LogiMap uses JavaFX Canvas for all rendering. This provides:
- Direct pixel manipulation
- Hardware acceleration
- Efficient batch drawing

### Key Components

```java
// In MapCanvas.java
private final Canvas canvas;           // The drawable surface
private final GraphicsContext gc;      // Drawing context

public MapCanvas(DemoWorld world, double width, double height) {
    canvas = new Canvas(width, height);
    gc = canvas.getGraphicsContext2D();
}
```

### Why Canvas?
- **Performance**: Scene graph (nodes) would be too slow for thousands of tiles
- **Control**: Full control over draw order and optimization
- **Flexibility**: Easy to implement custom rendering effects

---

## Coordinate Systems

LogiMap uses THREE coordinate systems:

### 1. World Coordinates (Grid)
- Integer positions on the world grid
- (0,0) is top-left of the world
- Each unit is one tile

```java
int gridX = 50;  // 50 tiles from left
int gridY = 30;  // 30 tiles from top
```

### 2. Screen Coordinates (Pixels)
- Pixel position on the canvas
- (0,0) is top-left of the canvas
- Depends on zoom and camera offset

```java
double screenX = 400.0;  // 400 pixels from left
double screenY = 300.0;  // 300 pixels from top
```

### 3. Camera Offset
- How much the view is panned
- offsetX/offsetY in pixels
- Positive = world shifted right/down

### Conversion Formulas

```java
// World to Screen
double screenX = gridX * GRID_SIZE * zoom + offsetX;
double screenY = gridY * GRID_SIZE * zoom + offsetY;

// Screen to World
double gridX = (screenX - offsetX) / (GRID_SIZE * zoom);
double gridY = (screenY - offsetY) / (GRID_SIZE * zoom);

// Helper methods in MapCanvas:
public double[] gridToScreen(int gridX, int gridY) {
    double tileSize = GRID_SIZE * zoom;
    return new double[] {
        gridX * tileSize + offsetX,
        gridY * tileSize + offsetY
    };
}

public int[] screenToGrid(double screenX, double screenY) {
    double tileSize = GRID_SIZE * zoom;
    return new int[] {
        (int) Math.floor((screenX - offsetX) / tileSize),
        (int) Math.floor((screenY - offsetY) / tileSize)
    };
}
```

### Visual Example
```
World Grid:           Screen (zoomed in):
┌──┬──┬──┬──┐         ┌────────┬────────┐
│0,0│1,0│2,0│         │  0,0   │  1,0   │
├──┼──┼──┼──┤    →    ├────────┼────────┤
│0,1│1,1│2,1│         │  0,1   │  1,1   │
└──┴──┴──┴──┘         └────────┴────────┘
                      (each tile is bigger)
```

---

## The Render Pipeline

Each frame, MapCanvas.render() draws in this order:

```java
private void render() {
    // 1. Clear canvas
    gc.setFill(BG_COLOR);
    gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
    
    // 2. Calculate visible area
    int[] topLeft = screenToGrid(0, 0);
    int[] bottomRight = screenToGrid(canvas.getWidth(), canvas.getHeight());
    
    // 3. Draw terrain (tiles)
    renderTerrain(topLeft, bottomRight);
    
    // 4. Draw structures (towns, resources)
    renderStructures();
    
    // 5. Draw roads
    renderRoads();
    
    // 6. Draw NPCs
    npcManager.render(gc, offsetX, offsetY, zoom, width, height, GRID_SIZE);
    
    // 7. Draw player
    player.render(gc, screenX, screenY, playerSize);
    
    // 8. Draw UI overlays
    renderUI();
}
```

---

## Layer Order

Objects are drawn back-to-front (painter's algorithm):

```
Layer 0: Background/Sky
Layer 1: Terrain tiles
Layer 2: Terrain decorations (grass, flowers)
Layer 3: Roads
Layer 4: Structures (buildings, resources)
Layer 5: NPCs
Layer 6: Player
Layer 7: UI elements (tooltips, menus)
Layer 8: Top-level overlays (dialogs)
```

### Z-Ordering Within Layers
For NPCs/entities, sort by Y position:
```java
// Entities lower on screen render last (appear in front)
entities.sort((a, b) -> Double.compare(a.getWorldY(), b.getWorldY()));
```

---

## Level of Detail (LOD)

At low zoom levels, rendering every tile is too slow. LogiMap uses LOD:

```java
// LOD thresholds
private static final double LOD_THRESHOLD_1 = 0.15;  // 4x4 blocks
private static final double LOD_THRESHOLD_2 = 0.08;  // 8x8 blocks
private static final double LOD_THRESHOLD_3 = 0.04;  // 16x16 blocks

private void renderTerrain(int startX, int startY, int endX, int endY) {
    int blockSize;
    
    if (zoom < LOD_THRESHOLD_3) {
        blockSize = 16;  // Very zoomed out
    } else if (zoom < LOD_THRESHOLD_2) {
        blockSize = 8;
    } else if (zoom < LOD_THRESHOLD_1) {
        blockSize = 4;
    } else {
        blockSize = 1;   // Full detail
    }
    
    // Render in blocks
    for (int x = startX; x <= endX; x += blockSize) {
        for (int y = startY; y <= endY; y += blockSize) {
            // Sample terrain at this position
            TerrainType terrain = world.getTerrainAt(x, y);
            
            // Draw block-sized rectangle
            gc.setFill(terrain.getColor());
            gc.fillRect(screenX, screenY, blockPixels, blockPixels);
        }
    }
}
```

---

## Code Examples

### Drawing a Simple Sprite

```java
public void render(GraphicsContext gc, double x, double y, double size) {
    // Body
    gc.setFill(Color.BLUE);
    gc.fillRect(x, y + size * 0.3, size * 0.6, size * 0.5);
    
    // Head
    gc.setFill(Color.web("#FDBCB4"));  // Skin color
    gc.fillOval(x + size * 0.1, y, size * 0.4, size * 0.35);
    
    // Eyes
    gc.setFill(Color.BLACK);
    gc.fillOval(x + size * 0.2, y + size * 0.1, size * 0.08, size * 0.08);
    gc.fillOval(x + size * 0.35, y + size * 0.1, size * 0.08, size * 0.08);
}
```

### Animated Walking

```java
public class WalkingEntity {
    private int animFrame = 0;
    private double animTimer = 0;
    
    public void update(double deltaTime) {
        animTimer += deltaTime;
        if (animTimer >= 0.2) {  // 5 FPS animation
            animTimer = 0;
            animFrame = (animFrame + 1) % 4;  // 4 frames
        }
    }
    
    public void render(GraphicsContext gc, double x, double y, double size) {
        // Leg animation offset
        double legOffset = Math.sin(animFrame * Math.PI / 2) * size * 0.1;
        
        // Left leg
        gc.fillRect(x + size * 0.15, y + size * 0.7 + legOffset, 
                    size * 0.15, size * 0.25);
        
        // Right leg  
        gc.fillRect(x + size * 0.35, y + size * 0.7 - legOffset,
                    size * 0.15, size * 0.25);
    }
}
```

### Panning and Zooming

```java
// Mouse drag for panning
canvas.setOnMouseDragged(e -> {
    double dx = e.getX() - lastMouseX;
    double dy = e.getY() - lastMouseY;
    
    offsetX += dx;
    offsetY += dy;
    
    lastMouseX = e.getX();
    lastMouseY = e.getY();
});

// Scroll wheel for zooming
canvas.setOnScroll(e -> {
    double zoomFactor = e.getDeltaY() > 0 ? 1.1 : 0.9;
    
    // Zoom toward mouse position
    double mouseX = e.getX();
    double mouseY = e.getY();
    
    // Adjust offset to zoom toward cursor
    offsetX = mouseX - (mouseX - offsetX) * zoomFactor;
    offsetY = mouseY - (mouseY - offsetY) * zoomFactor;
    
    zoom *= zoomFactor;
    zoom = Math.max(MIN_ZOOM, Math.min(MAX_ZOOM, zoom));
});
```

### Optimized Tile Rendering

```java
private void renderTiles() {
    double tileSize = GRID_SIZE * zoom;
    
    // Calculate visible tile range
    int startX = Math.max(0, (int) (-offsetX / tileSize));
    int startY = Math.max(0, (int) (-offsetY / tileSize));
    int endX = Math.min(world.getWidth(), 
                        (int) ((canvas.getWidth() - offsetX) / tileSize) + 1);
    int endY = Math.min(world.getHeight(),
                        (int) ((canvas.getHeight() - offsetY) / tileSize) + 1);
    
    // Only render visible tiles!
    for (int x = startX; x <= endX; x++) {
        for (int y = startY; y <= endY; y++) {
            TerrainType terrain = world.getTerrainAt(x, y);
            
            double screenX = x * tileSize + offsetX;
            double screenY = y * tileSize + offsetY;
            
            gc.setFill(terrain.getColor());
            gc.fillRect(screenX, screenY, tileSize + 1, tileSize + 1);
        }
    }
}
```

---

## Tips and Best Practices

### Performance
1. **Only render visible objects** - Calculate visible area first
2. **Use LOD** - Simplify at low zoom levels
3. **Batch similar draws** - Same color fills together
4. **Avoid creating objects in render loop** - Pre-allocate colors

### Visual Quality
1. **Add +1 to tile size** - Prevents gaps between tiles
2. **Use anti-aliasing for circles**: `gc.setFill()` handles this
3. **Round pixel positions** - Prevents blurry rendering

### Debugging
```java
// Draw grid lines for debugging
private void debugRenderGrid() {
    gc.setStroke(Color.RED);
    gc.setLineWidth(1);
    
    double tileSize = GRID_SIZE * zoom;
    
    for (int x = 0; x < world.getWidth(); x++) {
        double screenX = x * tileSize + offsetX;
        gc.strokeLine(screenX, 0, screenX, canvas.getHeight());
    }
    
    for (int y = 0; y < world.getHeight(); y++) {
        double screenY = y * tileSize + offsetY;
        gc.strokeLine(0, screenY, canvas.getWidth(), screenY);
    }
}
```

---

## Next: [03_WORLD_GENERATION.md](03_WORLD_GENERATION.md)
