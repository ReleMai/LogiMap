# LogiMap Architecture Documentation

This document provides a technical overview of LogiMap's architecture, design decisions, and code organization.

## Table of Contents

1. [Overview](#overview)
2. [Application Lifecycle](#application-lifecycle)
3. [Core Systems](#core-systems)
4. [World Generation](#world-generation)
5. [Rendering Pipeline](#rendering-pipeline)
6. [UI Framework](#ui-framework)
7. [Data Persistence](#data-persistence)
8. [Class Reference](#class-reference)

---

## Overview

LogiMap is built using JavaFX 23 and follows a modular architecture separating concerns into distinct systems:

```
┌─────────────────────────────────────────────────────────────┐
│                         App.java                            │
│                    (Application Entry)                      │
└─────────────────────────┬───────────────────────────────────┘
                          │
          ┌───────────────┴───────────────┐
          ▼                               ▼
┌─────────────────┐             ┌─────────────────┐
│   MainMenu      │             │   LogiMapUI     │
│  (Title Screen) │────────────▶│  (Game Screen)  │
└─────────────────┘             └────────┬────────┘
                                         │
         ┌───────────────┬───────────────┼───────────────┬───────────────┐
         ▼               ▼               ▼               ▼               ▼
   ┌──────────┐   ┌──────────┐   ┌──────────────┐ ┌──────────┐   ┌──────────┐
   │TabManager│   │MapCanvas │   │InteractionMenu│ │NewsTicker│   │MiniMap   │
   └──────────┘   └──────────┘   └──────────────┘ └──────────┘   └──────────┘
                        │
         ┌──────────────┴──────────────┐
         ▼                             ▼
   ┌──────────┐                 ┌──────────────┐
   │DemoWorld │                 │  MapFilter   │
   │(World Data)                │ (Rendering)  │
   └──────────┘                 └──────────────┘
```

---

## Application Lifecycle

### Startup Sequence

1. **App.java** - JavaFX Application entry point
   - Initializes the primary Stage
   - Creates root StackPane for scene layering
   - Instantiates MainMenu

2. **MainMenu.java** - Title screen
   - Displays game title and menu options
   - Handles New Game / Load Game / Settings selection
   - Transitions to WorldGenMenu or LoadMenu

3. **WorldGenMenu.java** - World creation
   - Collects world parameters (name, seed, size)
   - Spawns LoadingScreen during generation
   - Creates DemoWorld and transitions to game

4. **LogiMapUI.java** - Main game interface
   - Composes all game UI components
   - Manages overlay menu system
   - Handles tab switching

### Scene Management

```java
// Root StackPane allows overlaying menus
StackPane root = new StackPane();
Scene scene = new Scene(root, 1400, 900);

// Menus are added/removed from root
root.getChildren().add(menuOverlay);
root.getChildren().remove(menuOverlay);
```

---

## Core Systems

### World Data Model

**DemoWorld.java** - Central world state container

```java
public class DemoWorld {
    private int[][] terrain;        // Terrain type grid
    private int[][] elevation;      // Height map
    private int[][] moisture;       // Humidity map
    private int[][] temperature;    // Temperature map
    private WaterType[][] waterMap; // Water classification
    
    private List<MapStructure> structures;  // Towns, buildings
    private RoadNetwork roadNetwork;        // Road connections
    private ResourceMap resourceMap;        // Resource distribution
    
    private WorldGenerator worldGen;        // Generator reference
}
```

### Structure Hierarchy

```
MapStructure (abstract)
    │
    ├── Town
    │   └── TownSprite (rendering)
    │
    ├── LumberCamp
    ├── MiningQuarry
    ├── Millworks
    └── Stoneworks
```

All structures share common properties:
- Grid position (x, y)
- Size (tiles)
- Name
- Structure type

---

## World Generation

### Generation Pipeline

```
WorldGenerator.generate()
    │
    ├── 1. generateElevation()     → int[][] heightmap
    │       └── Perlin-like noise with octaves
    │
    ├── 2. generateClimate()       → temperature + moisture
    │       └── Latitude-based with noise variation
    │
    ├── 3. assignTerrain()         → TerrainType[][] 
    │       └── Biome selection from elevation/climate
    │
    ├── 4. classifyWater()         → WaterType[][]
    │       └── Ocean, lake, river detection
    │
    └── 5. generatePOIs()          → List<PointOfInterest>
            └── Region centers and landmarks
```

### Terrain Types

| Type | Conditions |
|------|------------|
| DEEP_WATER | elevation < 0.2 |
| SHALLOW_WATER | elevation < 0.3 |
| BEACH | elevation < 0.35, near water |
| PLAINS | low elevation, medium moisture |
| FOREST | medium elevation, high moisture |
| DENSE_FOREST | high moisture, moderate temp |
| MOUNTAIN | elevation > 0.75 |
| SNOW | high elevation OR low temp |
| DESERT | low moisture, high temp |
| SWAMP | low elevation, very high moisture |

### Structure Placement

Structures are placed using terrain suitability scoring:

```java
private int calculatePlacementScore(int x, int y, MapStructure structure) {
    int score = 0;
    
    // Check terrain compatibility
    if (isValidTerrain(terrain[y][x], structure.getType())) {
        score += 50;
    }
    
    // Bonus for resource proximity
    score += nearbyResources(x, y) * 10;
    
    // Penalty for overlap
    if (hasOverlap(x, y, structure.getSize())) {
        score -= 1000;
    }
    
    return score;
}
```

---

## Rendering Pipeline

### MapCanvas Rendering Order

```java
private void render() {
    // 1. Clear canvas
    gc.setFill(BG_COLOR);
    gc.fillRect(0, 0, width, height);
    
    // 2. Render terrain tiles
    renderTerrain(width, height);
    
    // 3. Render road network
    renderRoads(width, height);
    
    // 4. Render structures
    renderStructures(width, height);
    
    // 5. Render UI overlays
    if (hoveredStructure != null) {
        renderStructureHover(hoveredStructure);
    }
    renderGrid(width, height);
    renderTooltip();
}
```

### Coordinate Systems

```
Screen Space              World Space (Grid)
(pixels)                  (tiles)
┌─────────────┐           ┌─────────────┐
│ (0,0)       │           │ (0,0)       │
│      •──────┼──────────▶│      •      │
│   (sx,sy)   │           │  (gx,gy)    │
└─────────────┘           └─────────────┘

Conversion:
gridX = (screenX - offsetX) / (GRID_SIZE * zoom)
gridY = (screenY - offsetY) / (GRID_SIZE * zoom)

screenX = gridX * GRID_SIZE * zoom + offsetX
screenY = gridY * GRID_SIZE * zoom + offsetY
```

### Map Filters

Filters modify terrain colors without changing data:

```java
public interface MapFilter {
    Color getTerrainColor(TerrainType terrain, int elevation, 
                          int moisture, int temperature);
}

// Implementations:
// - StandardFilter: Default biome colors
// - TopographicalFilter: Elevation-based coloring
// - ResourceHeatmapFilter: Resource density visualization
```

---

## UI Framework

### Overlay Menu System

The interaction menu uses a StackPane overlay approach:

```java
// Menu floats over the map, not beside it
centerStack = new StackPane(mapContainer);
centerStack.getChildren().add(menuPanel);
StackPane.setAlignment(menuPanel, Pos.CENTER_LEFT);

// Animation slides menu without affecting map layout
TranslateTransition transition = new TranslateTransition(
    Duration.millis(300), menuPanel);
transition.setToX(menuVisible ? -MENU_WIDTH : 0);
```

### Tab System

```java
public class TabManager {
    private ToggleGroup tabGroup;
    private Map<String, ToggleButton> tabs;
    
    // Tabs: Local, Region, Production, Finance, Analytics
    // Switching tabs calls LogiMapUI.switchTab(name)
}
```

### Component Communication

Components communicate via callbacks and JavaFX properties:

```java
// InteractionMenu to MapCanvas
interactionMenu.setFilterHandlers(
    () -> mapCanvas.setMapFilter(new StandardFilter()),
    () -> mapCanvas.setMapFilter(new TopographicalFilter()),
    () -> mapCanvas.setMapFilter(new ResourceHeatmapFilter(...))
);

// Events via NewsTicker
newsTicker.addNewsItem("Town founded: " + townName);
```

---

## Data Persistence

### Save File Format

Worlds are saved as `.logimap` files using Java serialization:

```java
public class WorldSaveData implements Serializable {
    String worldName;
    long seed;
    int mapWidth, mapHeight;
    
    int[][] terrain;
    int[][] elevation;
    // ... other map data
    
    List<StructureSaveData> structures;
    
    // View state
    double viewX, viewY, zoom;
}
```

### Save/Load Flow

```
Save:
LogiMapUI → WorldSaveManager.saveWorld() → File

Load:
SaveLoadMenu → WorldSaveManager.loadWorld() → DemoWorld → LogiMapUI
```

---

## Class Reference

### Entry Points

| Class | Purpose |
|-------|---------|
| `App` | JavaFX Application bootstrap |
| `MainMenu` | Title screen and navigation |
| `LogiMapUI` | Main game screen |

### World System

| Class | Purpose |
|-------|---------|
| `DemoWorld` | World state container |
| `WorldGenerator` | Procedural generation |
| `TerrainGenerator` | Terrain algorithms |
| `TerrainType` | Terrain enum |
| `WaterType` | Water classification |

### Structures

| Class | Purpose |
|-------|---------|
| `MapStructure` | Base structure class |
| `Town` | Settlement implementation |
| `TownSprite` | Town rendering |
| `LumberCamp` | Wood production |
| `MiningQuarry` | Mining facility |
| `Millworks` | Grain processing |
| `Stoneworks` | Stone processing |

### Infrastructure

| Class | Purpose |
|-------|---------|
| `Road` | Road segment |
| `RoadNetwork` | Road pathfinding |
| `RoadQuality` | Road type enum |
| `PointOfInterest` | POI marker |
| `PoiType` | POI type enum |

### Resources

| Class | Purpose |
|-------|---------|
| `ResourceMap` | Resource distribution |
| `ResourceType` | Resource enum |

### UI Components

| Class | Purpose |
|-------|---------|
| `MapCanvas` | Map rendering |
| `InteractionMenu` | Control panel |
| `TabManager` | Tab navigation |
| `NewsTicker` | Notifications |
| `MiniMap` | Overview map |
| `LoadingScreen` | Progress display |

### Filters

| Class | Purpose |
|-------|---------|
| `MapFilter` | Filter interface |
| `StandardFilter` | Default colors |
| `TopographicalFilter` | Height-based |
| `ResourceHeatmapFilter` | Resource view |

### Persistence

| Class | Purpose |
|-------|---------|
| `WorldSaveManager` | Save/load logic |
| `SaveLoadMenu` | Save/load UI |
| `GameSettings` | Settings management |

### Utilities

| Class | Purpose |
|-------|---------|
| `NameGenerator` | Random name creation |
| `MedievalFont` | Font utilities |

---

## Design Decisions

### Why Overlay Menu?

Previous approaches (side panel, separate window) caused layout issues during animation. The overlay approach:
- Doesn't affect BorderPane layout calculations
- Provides smooth, predictable animations
- Allows the map to use full available space

### Why Not Use FXML?

The UI is built programmatically for:
- Easier dynamic content generation
- Simpler theming and style customization
- Reduced file count and complexity
- Better IDE refactoring support

### Why Single-Package Structure?

For this project size, a single package keeps things simple:
- No import complexity
- Easy navigation
- Clear file relationships

Future versions may introduce packages as the codebase grows.

---

## Future Considerations

### Planned Refactoring

1. **Extract Rendering**: Move terrain/structure rendering to dedicated classes
2. **Event System**: Implement proper game event bus
3. **Entity-Component**: Consider ECS for structures/units
4. **Threading**: Background world generation improvements

### Performance Optimization

1. **Tile Caching**: Pre-render visible tile regions
2. **LOD System**: Reduce detail at low zoom levels
3. **Culling**: Skip off-screen structure rendering
4. **Chunk Loading**: Load world sections on demand
