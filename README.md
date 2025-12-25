# 🗺️ LogiMap - Medieval Logistics & World Simulator

<div align="center">

![Java](https://img.shields.io/badge/Java-23+-orange?style=for-the-badge&logo=openjdk)
![JavaFX](https://img.shields.io/badge/JavaFX-23-blue?style=for-the-badge)
![Platform](https://img.shields.io/badge/Platform-Windows-lightgrey?style=for-the-badge&logo=windows)
![License](https://img.shields.io/badge/License-MIT-green?style=for-the-badge)

**A medieval-themed logistics and supply chain management simulation game built with JavaFX**

[Features](#-features) • [Installation](#-installation) • [Controls](#-controls) • [Architecture](#-architecture) • [Contributing](#-contributing)

</div>

---

## 📖 Overview

LogiMap is an immersive medieval logistics simulator where players manage supply chains, build towns, and develop trade routes across procedurally generated fantasy worlds. The game features a beautiful pixel-art style map with terrain generation, dynamic structures, and an intuitive overlay menu system.

### 🎮 Current Version: v0.1.0 (Foundation Release)

This release establishes the core game framework including:
- Procedural world generation with diverse biomes
- Town and structure placement system
- Interactive map navigation with zoom and pan
- Save/Load system for world persistence
- Medieval-themed UI with sliding control panel

---

## ✨ Features

### New (gameplay-start branch)
- Sprite-based harvest animations (generated sprite sheets in `assets/sprites/`).
- NPC inventories: NPCs can carry harvested resources and display carried items.
- UI: inspect NPC inventory and carried-item badge in NPC dialogue; hover NPCs to see inventory summary and press **I** to inspect.

To regenerate placeholder sprite sheets, run:

```
java --module-path lib/javafx-sdk-23/lib --add-modules javafx.controls,javafx.fxml,javafx.graphics,javafx.swing -cp bin SpriteAssetGenerator
```

### 🌍 World Generation
- **Procedural Terrain**: Mountains, forests, plains, deserts, swamps, and tundra
- **Water Systems**: Oceans, lakes, rivers, and coastal regions
- **Climate Zones**: Temperature and moisture-based biome distribution
- **Region Names**: Dynamically named regions across the map

### 🏰 Structures & Towns
- **Town Types**: Villages, cities, capitals, and specialized settlements
- **Production Buildings**: Lumber camps, mining quarries, mills, stoneworks
- **Smart Placement**: Terrain-aware structure positioning
- **Road Networks**: Connecting settlements with varying road qualities

### 🗺️ Map System
- **Multi-Layer View**: Local (detailed) and Region (overview) modes
- **Smooth Navigation**: Click-drag panning and mouse wheel zoom (30%-300%)
- **Map Filters**: Standard, Topographical, and Resource Heatmap views
- **Grid Overlay**: Toggleable coordinate grid with adjustable brightness

### 💾 Persistence
- **World Saves**: Complete world state serialization
- **View Memory**: Saves camera position and zoom level
- **Multiple Worlds**: Create and manage multiple save files

### 🎨 User Interface
- **Medieval Theme**: Dark parchment colors with leather-brown accents
- **Sliding Menu**: Smooth animated control panel overlay
- **Tab Navigation**: Local, Region, Production, Finance, and Analytics views
- **News Ticker**: Real-time event notifications with history log

---

## 🚀 Installation

### Prerequisites

- **Java 23** or later ([Download](https://jdk.java.net/23/))
- **Windows OS** (currently configured for Windows)

### Quick Start

1. **Clone the repository**
   ```bash
   git clone https://github.com/yourusername/LogiMap.git
   cd LogiMap
   ```

2. **Download JavaFX** (one-time setup)
   ```bash
   .\setup.bat
   ```
   This automatically downloads and extracts JavaFX SDK 23 to the `lib/` directory.

3. **Run the game**
   ```bash
   .\start.bat
   ```

### Alternative: VS Code

1. Open the project folder in VS Code
2. Install recommended extensions when prompted
3. Press `Ctrl+Shift+B` and select **"Run LogiMap"**

### Manual Compilation

```bash
# Compile
.\compile.bat

# Run
.\run.bat
```

---

## 🎮 Controls

### Map Navigation
| Action | Control |
|--------|---------|
| Pan/Scroll | Click + Drag |
| Zoom In/Out | Mouse Wheel |
| Select Structure | Left Click |
| View Coordinates | Hold Ctrl + Hover |

### Menu Controls
| Action | Control |
|--------|---------|
| Toggle Menu | Click ◀/▶ tab |
| Switch Tabs | Click tab icons (🗺️ 🏗️ 🚚 📊 ⚙️) |
| View Notifications | Click "LOG" button |

### Keyboard Shortcuts
| Action | Key |
|--------|-----|
| Build (VS Code) | Ctrl+Shift+B |
| Debug | F5 |

---

## 📁 Project Structure

```
LogiMap/
├── 📂 src/                      # Source code
│   ├── App.java                 # Main entry point
│   ├── MainMenu.java            # Title screen and main menu
│   ├── LogiMapUI.java           # Main game UI framework
│   ├── MapCanvas.java           # Map rendering engine
│   │
│   ├── 🌍 World Generation
│   │   ├── WorldGenerator.java  # Procedural world creation
│   │   ├── TerrainGenerator.java# Terrain algorithm
│   │   ├── DemoWorld.java       # World data container
│   │   ├── TerrainType.java     # Terrain definitions
│   │   └── WaterType.java       # Water body types
│   │
│   ├── 🏰 Structures
│   │   ├── MapStructure.java    # Base structure class
│   │   ├── Town.java            # Town/city implementation
│   │   ├── TownSprite.java      # Town rendering
│   │   ├── LumberCamp.java      # Wood production
│   │   ├── MiningQuarry.java    # Stone/ore extraction
│   │   ├── Millworks.java       # Grain processing
│   │   └── Stoneworks.java      # Stone processing
│   │
│   ├── 🛤️ Infrastructure
│   │   ├── Road.java            # Road segments
│   │   ├── RoadNetwork.java     # Road pathfinding
│   │   ├── RoadQuality.java     # Road types
│   │   └── PointOfInterest.java # POI markers
│   │
│   ├── 📊 Resources
│   │   ├── ResourceMap.java     # Resource distribution
│   │   ├── ResourceType.java    # Resource definitions
│   │   └── ResourceHeatmapFilter.java
│   │
│   ├── 🎨 UI Components
│   │   ├── InteractionMenu.java # Control panel
│   │   ├── TabManager.java      # Tab navigation
│   │   ├── NewsTicker.java      # Event notifications
│   │   ├── MiniMap.java         # Overview minimap
│   │   ├── LoadingScreen.java   # World generation progress
│   │   └── MedievalFont.java    # Custom font styling
│   │
│   ├── 🔧 Filters & Views
│   │   ├── MapFilter.java       # Filter interface
│   │   ├── StandardFilter.java  # Default view
│   │   └── TopographicalFilter.java
│   │
│   ├── 💾 Persistence
│   │   ├── WorldSaveManager.java# Save/load system
│   │   ├── SaveLoadMenu.java    # Save/load UI
│   │   └── GameSettings.java    # Settings management
│   │
│   ├── 🎲 Utilities
│   │   ├── NameGenerator.java   # Random name generation
│   │   └── PoiType.java         # POI type definitions
│   │
│   └── ⚙️ Menus
│       ├── WorldGenMenu.java    # World creation options
│       └── SettingsMenu.java    # Game settings
│
├── 📂 lib/                      # External libraries
│   └── javafx-sdk-23/           # JavaFX runtime (auto-downloaded)
│
├── 📂 bin/                      # Compiled classes (generated)
├── 📂 saves/                    # World save files
├── 📂 .vscode/                  # VS Code configuration
│
├── 📜 setup.bat                 # JavaFX download script
├── 📜 setup.ps1                 # PowerShell setup alternative
├── 📜 compile.bat               # Compilation script
├── 📜 run.bat                   # Run script
├── 📜 start.bat                 # Compile + run combined
│
├── 📜 settings.properties       # Game settings file
├── 📄 README.md                 # This file
├── 📄 ARCHITECTURE.md           # Technical documentation
├── 📄 CONTRIBUTING.md           # Contribution guidelines
└── 📄 LICENSE                   # MIT License
```

---

## 🏗️ Architecture

### Design Patterns

- **MVC Pattern**: Separation of game logic, rendering, and UI controls
- **Observer Pattern**: Event-driven UI updates via JavaFX properties
- **Strategy Pattern**: Swappable map filters and rendering modes
- **Factory Pattern**: Structure and terrain generation

### Key Components

| Component | Responsibility |
|-----------|----------------|
| `App` | Application bootstrap and scene management |
| `MainMenu` | Entry point UI and world selection |
| `LogiMapUI` | Main game screen composition |
| `MapCanvas` | Terrain/structure rendering and input handling |
| `WorldGenerator` | Procedural world creation algorithms |
| `InteractionMenu` | Player controls and game actions |

### Rendering Pipeline

1. **Terrain Layer**: Base terrain tiles from heightmap
2. **Road Layer**: Road network overlay
3. **Structure Layer**: Towns and buildings
4. **UI Layer**: Menus, tooltips, selection highlights

---

## 🎯 Roadmap

### v0.2.0 - Economy Update
- [ ] Resource production and consumption
- [ ] Trade routes between towns
- [ ] Market prices and supply/demand
- [ ] Treasury and finances

### v0.3.0 - Logistics Update
- [ ] Caravan and transport units
- [ ] Warehouse storage system
- [ ] Route optimization
- [ ] Delivery scheduling

### v0.4.0 - Expansion Update
- [ ] Town growth mechanics
- [ ] Building construction
- [ ] Population simulation
- [ ] Technology/upgrades

---

## 🤝 Contributing

Contributions are welcome! Please read [CONTRIBUTING.md](CONTRIBUTING.md) for guidelines.

### Development Setup

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/amazing-feature`
3. Make your changes
4. Test thoroughly
5. Commit: `git commit -m 'Add amazing feature'`
6. Push: `git push origin feature/amazing-feature`
7. Open a Pull Request

---

## 🐛 Troubleshooting

### "Package javafx does not exist"
```bash
# Run the setup script to download JavaFX
.\setup.bat
```

### Application won't start
- Verify Java 23+: `java -version`
- Check JavaFX exists: `lib/javafx-sdk-23/lib/` should contain JAR files

### Slow performance
- Reduce zoom level
- Close other applications
- Check available RAM

---

## 📜 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

## 🙏 Acknowledgments

- [OpenJFX](https://openjfx.io/) - JavaFX framework
- Inspired by classic logistics games like Transport Tycoon and Anno series

---

<div align="center">

**Made with ☕ and JavaFX**

⭐ Star this repo if you find it interesting!

</div>

