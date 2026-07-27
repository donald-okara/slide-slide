# Slide Slide Architecture

## Overview

Slide Slide is a single-module Android application built with **Jetpack Compose**, **Room**, and **Clean Architecture** principles. Although the project lives in a single Gradle module for simplicity, it maintains clear boundaries between persistence, puzzle logic, presentation logic, and UI.

The architecture is intentionally layered so that each layer has a single responsibility and communicates with adjacent layers through stable contracts.

```
┌──────────────────────────────┐
│            UI                │
│ Compose Screens              │
└──────────────┬───────────────┘
               │
               ▼
┌──────────────────────────────┐
│         ViewModel            │
│ UI State                     │
│ Image Processing             │
│ Timer                        │
└──────────────┬───────────────┘
               │
               ▼
┌──────────────────────────────┐
│       Puzzle Manager         │
│ Business Rules               │
│ Move Validation              │
│ Shuffle                      │
│ Undo                         │
│ Win Detection                │
└──────────────┬───────────────┘
               │
               ▼
┌──────────────────────────────┐
│         Room Database        │
│ DAO                          │
│ Entities                     │
└──────────────────────────────┘
```

---

# Package Structure

```
app
├── data
│   ├── dao
│   ├── database
│   ├── entity
│   └── mapper
│
├── domain
│   ├── algorithm
│   ├── manager
│   └── model
│
├── ui
│   ├── component
│   ├── navigation
│   ├── screen
│   ├── state
│   └── viewmodel
│
├── image
│   ├── BitmapSlicer
│   └── ImageLoader
│
├── di
└── util
```

---

# Layer Responsibilities

## 1. Domain

The domain layer owns every puzzle rule.

It is the only layer allowed to mutate the game state.

Responsibilities include:

* Creating a new puzzle
* Generating solved boards
* Producing solvable shuffles
* Validating legal moves
* Moving tiles
* Recording moves
* Undo
* Reset
* Win detection
* Hint generation
* Auto solving

The entry point into the domain is the `PuzzleManager`.

```
PuzzleManager
    ├── createGame()
    ├── moveTile()
    ├── shuffle()
    ├── undo()
    ├── reset()
    ├── observeGame()
    ├── bestNextMove()
    └── autoSolve()
```

The PuzzleManager is the application's orchestrator.

---

## 2. Data Layer

The data layer is responsible solely for persistence.

It has no understanding of puzzle rules.

Its responsibilities are:

* Persisting games
* Persisting tile positions
* Persisting move history
* Exposing reactive game streams through Room

The database consists of three primary entities.

```
GameEntity

TileEntity

MoveEntity
```

and a single DAO responsible for transactional updates.

---

## 3. ViewModel

The ViewModel bridges the puzzle engine and Compose.

Responsibilities include:

* Holding the current UI state
* Image selection
* Splitting uploaded images into tile bitmaps
* Maintaining a bitmap cache
* Game timer
* User interaction events
* Collecting puzzle state Flow

The ViewModel intentionally contains **no puzzle logic**.

Instead, every user interaction delegates to the PuzzleManager.

```
Shuffle
        │
        ▼
ViewModel
        │
        ▼
PuzzleManager
```

---

## 4. UI Layer

The UI is responsible only for presentation.

Screens include:

```
Image Picker

Difficulty Selection

Puzzle Screen

Victory Dialog
```

The puzzle screen displays:

* Puzzle board
* Move counter
* Timer
* Shuffle
* Undo
* Reset

All state originates from the ViewModel.

---

# Image Pipeline

Unlike a traditional puzzle game with bundled artwork, Slide Slide allows users to supply their own images.

The processing pipeline is:

```
Upload Image
        │
        ▼
Bitmap
        │
        ▼
BitmapSlicer
        │
        ▼
Map<TileId, ImageBitmap>
        │
        ▼
Compose Grid
```

Only bitmap slices are stored in memory.

The database stores tile metadata rather than image data.

---

# Data Flow

```
User taps tile
        │
        ▼
ViewModel
        │
        ▼
PuzzleManager
        │
        ▼
Validate Move
        │
        ▼
Room Transaction
        │
        ▼
Flow<GameState>
        │
        ▼
ViewModel
        │
        ▼
Compose recomposes
```

All updates follow this unidirectional flow.

---

# Design Principles

The project follows several architectural rules:

* **Single source of truth** — Room stores the authoritative puzzle state.
* **Single writer** — Only the PuzzleManager may modify puzzle state.
* **Reactive updates** — UI observes changes through `Flow`.
* **Thin ViewModel** — The ViewModel coordinates rather than implements puzzle rules.
* **Stateless UI** — Compose renders immutable UI state.

---

# Development Phases

## Phase 1 — Rules & Models

Define the application's contracts and shared models.

Deliverables:

* Domain models
* Room entities
* PuzzleManager interface
* Package structure

---

## Phase 2 — Data Source

Implement persistence.

Deliverables:

* Room database
* DAO
* Transactions
* Entity mappings

---

## Phase 3 — Puzzle Manager

Implement the complete puzzle engine.

Deliverables:

* Puzzle creation
* Shuffle
* Move validation
* Undo
* Reset
* Win detection
* Hint generation
* Auto solver

---

## Phase 4 — ViewModel

Connect the puzzle engine to the UI.

Deliverables:

* UI state
* Bitmap slicing
* Image cache
* Timer
* Event handling

---

## Phase 5 — UI

Build the Compose interface.

Deliverables:

* Image picker
* Difficulty selector
* Puzzle board
* Controls
* Animations
* Victory screen

---

# Guiding Rule

> **The PuzzleManager is the sole authority over puzzle state.**

Every game action—whether initiated by the user, the UI, or future features such as hints or auto-solve—flows through the PuzzleManager. This keeps business rules centralized, ensures consistent behavior, and makes the application easier to test and extend while keeping the rest of the codebase focused on persistence or presentation.
