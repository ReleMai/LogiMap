# Contributing to LogiMap

Thank you for your interest in contributing to LogiMap! This document provides guidelines and information for contributors.

## Table of Contents

- [Code of Conduct](#code-of-conduct)
- [Getting Started](#getting-started)
- [Development Setup](#development-setup)
- [How to Contribute](#how-to-contribute)
- [Coding Standards](#coding-standards)
- [Commit Guidelines](#commit-guidelines)
- [Pull Request Process](#pull-request-process)

---

## Code of Conduct

By participating in this project, you agree to maintain a respectful and inclusive environment. Please:

- Be respectful of differing viewpoints and experiences
- Accept constructive criticism gracefully
- Focus on what is best for the project
- Show empathy towards other community members

---

## Getting Started

### Prerequisites

- **Java 23+** - Required for JavaFX 23 compatibility
- **Git** - For version control
- **VS Code** (recommended) or any Java IDE

### Development Setup

1. **Fork the repository**
   ```bash
   # Click "Fork" on GitHub, then clone your fork
   git clone https://github.com/YOUR_USERNAME/LogiMap.git
   cd LogiMap
   ```

2. **Set up upstream remote**
   ```bash
   git remote add upstream https://github.com/ORIGINAL_OWNER/LogiMap.git
   ```

3. **Install dependencies**
   ```bash
   .\setup.bat  # Downloads JavaFX
   ```

4. **Verify build works**
   ```bash
   .\compile.bat
   .\run.bat
   ```

---

## How to Contribute

### Reporting Bugs

Before reporting a bug:
1. Check existing issues to avoid duplicates
2. Verify you're using the latest version

When reporting:
- Use a clear, descriptive title
- Describe steps to reproduce
- Include expected vs actual behavior
- Add screenshots if applicable
- Mention your Java version and OS

### Suggesting Features

Feature requests are welcome! Please:
- Check if the feature was already suggested
- Describe the feature clearly
- Explain the use case and benefits
- Consider how it fits the game's theme

### Contributing Code

1. **Pick an issue** or create one for discussion
2. **Create a branch** from `main`
3. **Make changes** following our coding standards
4. **Test thoroughly**
5. **Submit a pull request**

---

## Coding Standards

### Java Style Guide

```java
// Class names: PascalCase
public class WorldGenerator { }

// Methods and variables: camelCase
private void generateTerrain() { }
private int mapWidth;

// Constants: UPPER_SNAKE_CASE
private static final int DEFAULT_MAP_SIZE = 256;

// Braces: same line
if (condition) {
    // code
} else {
    // code
}
```

### JavaFX Conventions

```java
// UI component naming: descriptive suffix
Button saveButton = new Button("Save");
VBox controlPanel = new VBox();
Label titleLabel = new Label("Title");

// Style constants at class level
private static final String DARK_BG = "#1a1a1a";

// Event handlers: lambda or method reference
button.setOnAction(e -> handleButtonClick());
button.setOnAction(this::handleButtonClick);
```

### Documentation

```java
/**
 * Generates procedural terrain for the world map.
 * Uses Perlin noise with multiple octaves for natural-looking results.
 * 
 * @param width  Map width in tiles
 * @param height Map height in tiles
 * @param seed   Random seed for reproducible generation
 * @return 2D array of terrain elevation values (0.0 - 1.0)
 */
public double[][] generateTerrain(int width, int height, long seed) {
    // Implementation
}
```

### File Organization

- One public class per file
- Related classes in logical groupings
- Keep files under 500 lines when possible
- Extract complex methods into helper classes

---

## Commit Guidelines

### Commit Message Format

```
<type>: <short description>

[optional body]

[optional footer]
```

### Types

| Type | Description |
|------|-------------|
| `feat` | New feature |
| `fix` | Bug fix |
| `docs` | Documentation changes |
| `style` | Code formatting (no logic change) |
| `refactor` | Code restructuring |
| `perf` | Performance improvement |
| `test` | Adding tests |
| `chore` | Maintenance tasks |

### Examples

```
feat: add resource heatmap filter

Implements a new map filter that visualizes resource distribution
using a color gradient overlay.

fix: resolve menu animation stuck state

Menu toggle button now properly prevents multiple simultaneous
animations that could leave the menu in an inconsistent state.

docs: update README with installation steps
```

---

## Pull Request Process

### Before Submitting

- [ ] Code compiles without errors
- [ ] Game runs and feature works correctly
- [ ] No obvious regressions in existing features
- [ ] Code follows style guidelines
- [ ] Commits are clean and well-described

### PR Description Template

```markdown
## Description
Brief description of changes

## Type of Change
- [ ] Bug fix
- [ ] New feature
- [ ] Documentation update
- [ ] Refactoring

## Testing Done
How did you test this?

## Screenshots (if applicable)
Add screenshots for UI changes

## Checklist
- [ ] Code compiles
- [ ] Tested locally
- [ ] Documentation updated (if needed)
```

### Review Process

1. Submit PR against `main` branch
2. Automated checks must pass
3. At least one maintainer review required
4. Address feedback promptly
5. Squash commits if requested
6. Maintainer merges when approved

---

## Development Tips

### Quick Iteration

```bash
# Compile and run in one command
.\start.bat
```

### VS Code Tasks

- `Ctrl+Shift+B` → "Compile Java" - Build only
- `Ctrl+Shift+B` → "Run LogiMap" - Build and run

### Debugging

1. Set breakpoints in VS Code
2. Press `F5` to start debugger
3. Use Debug Console for evaluation

### Common Issues

**"Module not found"**
```bash
# Ensure JavaFX is downloaded
.\setup.bat
```

**"Class not found"**
```bash
# Clean and rebuild
Remove-Item -Recurse bin/*
.\compile.bat
```

---

## Project Areas

Looking for something to work on? Consider these areas:

### Good First Issues
- UI polish and styling
- Documentation improvements
- Bug fixes with clear reproduction steps

### Medium Complexity
- New map filters
- Additional structure types
- UI component enhancements

### Advanced
- Economy system implementation
- Pathfinding algorithms
- Performance optimizations

---

## Questions?

- Open an issue for project-related questions
- Check existing issues and documentation first
- Be patient - maintainers are volunteers

---

Thank you for contributing to LogiMap! 🗺️
