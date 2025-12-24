# LogiMap Developer Guide - Getting Started

## Table of Contents
1. [Project Overview](#project-overview)
2. [Project Structure](#project-structure)
3. [Setting Up Development Environment](#setting-up-development-environment)
4. [Building and Running](#building-and-running)
5. [Architecture Overview](#architecture-overview)

---

## Project Overview

LogiMap is a 2D medieval strategy/simulation game built with JavaFX. It features:
- **Procedural World Generation**: Terrain, towns, roads, resources
- **Economy System**: Trading, marketplaces, currencies
- **NPC System**: Wandering NPCs, parties, professions
- **Player Systems**: Inventory, equipment, parties, combat
- **Resource Gathering**: Mining, logging, fishing, farming

### Technology Stack
- **Language**: Java 17+
- **UI Framework**: JavaFX 23
- **Build**: Manual javac compilation (no Maven/Gradle)
- **Save Format**: Custom .logimap files

---

## Project Structure

```
LogiMap/
├── src/                    # All Java source files
│   ├── App.java           # Main entry point
│   ├── LogiMapUI.java     # Main UI container
│   ├── MapCanvas.java     # Core rendering canvas
│   └── ... (100+ files)
├── bin/                    # Compiled .class files
├── lib/
│   └── javafx-sdk-23/     # JavaFX runtime
├── saves/                  # Save game files (.logimap)
├── sprites/               # Sprite sheets and images
├── docs/                  # Documentation (you are here!)
├── UITemplates/           # UI template files
├── compile.bat            # Windows compile script
├── run.bat               # Windows run script
└── settings.properties   # Game configuration
```

### Key Packages/Categories

| Category | Files | Purpose |
|----------|-------|---------|
| **Core** | App, LogiMapUI, MapCanvas | Application entry and main rendering |
| **World** | DemoWorld, WorldGenerator, TerrainGenerator | World creation and management |
| **Terrain** | TerrainType, TerrainRenderer, TerrainDecoration | Terrain types and rendering |
| **Towns** | Town, TownSprite, TownWarehouse | Settlement systems |
| **NPCs** | NPC, NPCManager, NPCParty, TavernNPC | NPC behavior and management |
| **Player** | PlayerSprite, Party, PartyMember, CharacterStats | Player systems |
| **Items** | Item, Inventory, Equipment, ItemRegistry | Item and inventory management |
| **Resources** | ResourceNode, ResourceType, ResourceMap | Resource gathering |
| **Economy** | EconomySystem, Currency, ShopUI, MarketplaceUI | Trading and money |
| **Combat** | Combat, Enemy, CombatUI | Battle system |
| **UI** | Various *UI classes, FloatingPanel, DraggableWindow | User interface components |
| **Utilities** | NameGenerator, MedievalFont, GameTime | Helper utilities |

---

## Setting Up Development Environment

### Prerequisites
1. **Java JDK 17+** (OpenJDK or Oracle)
2. **JavaFX SDK 23** (already included in `lib/`)
3. **VS Code** (recommended) or any Java IDE

### VS Code Setup
1. Install extensions:
   - "Extension Pack for Java" by Microsoft
   - "Language Support for Java" by Red Hat

2. Create `.vscode/settings.json`:
```json
{
    "java.project.sourcePaths": ["src"],
    "java.project.outputPath": "bin",
    "java.project.referencedLibraries": [
        "lib/javafx-sdk-23/lib/*.jar"
    ]
}
```

3. Create `.vscode/tasks.json` (already exists):
```json
{
    "version": "2.0.0",
    "tasks": [
        {
            "label": "Compile Java",
            "type": "shell",
            "command": "javac",
            "args": [
                "--module-path", "lib/javafx-sdk-23/lib",
                "--add-modules", "javafx.controls,javafx.fxml,javafx.graphics",
                "-d", "bin",
                "-encoding", "UTF-8",
                "src/*.java"
            ],
            "group": { "kind": "build", "isDefault": true }
        },
        {
            "label": "Run LogiMap",
            "type": "shell",
            "command": "java",
            "args": [
                "--module-path", "lib/javafx-sdk-23/lib",
                "--add-modules", "javafx.controls,javafx.fxml,javafx.graphics",
                "-cp", "bin",
                "App"
            ],
            "dependsOn": "Compile Java"
        }
    ]
}
```

---

## Building and Running

### Using VS Code Tasks
1. Press `Ctrl+Shift+B` to compile
2. Run the "Run LogiMap" task

### Using Batch Files (Windows)
```batch
# Compile
compile.bat

# Run
run.bat

# Or both
start.bat
```

### Manual Commands
```bash
# Compile all source files
javac --module-path lib/javafx-sdk-23/lib ^
      --add-modules javafx.controls,javafx.fxml,javafx.graphics ^
      -d bin -encoding UTF-8 src/*.java

# Run the application
java --module-path lib/javafx-sdk-23/lib ^
     --add-modules javafx.controls,javafx.fxml,javafx.graphics ^
     -cp bin App
```

---

## Architecture Overview

### Application Flow
```
App.java (main)
    └── LogiMapUI (JavaFX Application)
            ├── MainMenu (Start screen)
            │       ├── New Game → WorldGenMenu → DemoWorld
            │       ├── Load Game → SaveLoadMenu
            │       └── Settings → SettingsMenu
            └── MapCanvas (Game view)
                    ├── TerrainRenderer (Background)
                    ├── TownSprite (Settlements)
                    ├── NPCManager (NPCs)
                    ├── PlayerSprite (Player)
                    └── UI Overlays (HUD, menus)
```

### Core Systems Interaction
```
┌─────────────────────────────────────────────────────────────┐
│                        MapCanvas                             │
│  ┌─────────┐  ┌──────────┐  ┌─────────┐  ┌──────────────┐  │
│  │DemoWorld│  │NPCManager│  │PlayerSpr│  │TerrainRender │  │
│  └────┬────┘  └────┬─────┘  └────┬────┘  └──────────────┘  │
│       │            │             │                          │
│  ┌────▼────┐  ┌────▼─────┐  ┌────▼────┐                    │
│  │  Towns  │  │   NPCs   │  │  Party  │                    │
│  │Resources│  │ Parties  │  │Inventory│                    │
│  │  Roads  │  │ TavernNPC│  │Equipment│                    │
│  └─────────┘  └──────────┘  └─────────┘                    │
└─────────────────────────────────────────────────────────────┘
```

### Update Loop (60 FPS)
```java
// In MapCanvas.startGameLoop()
AnimationTimer timer = new AnimationTimer() {
    public void handle(long now) {
        double deltaTime = (now - lastUpdate) / 1_000_000_000.0;
        
        update(deltaTime);  // Update game state
        render();           // Draw everything
        
        lastUpdate = now;
    }
};
```

---

## Next Steps

Continue reading the following guides:
1. **02_RENDERING_SYSTEM.md** - How graphics work
2. **03_WORLD_GENERATION.md** - Procedural world creation
3. **04_NPC_SYSTEM.md** - NPC behavior and management
4. **05_PLAYER_SYSTEMS.md** - Player, inventory, combat
5. **06_ECONOMY_SYSTEM.md** - Trading and currency
6. **07_UI_COMPONENTS.md** - Building user interfaces
7. **08_ADDING_NEW_FEATURES.md** - Extending the game
