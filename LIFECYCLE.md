## Game Lifecycle

Slide Slide is designed as a **single-session puzzle game**.

A game exists only for the lifetime of the current play session and is not intended to be resumed after the application is closed or recreated.

To enforce this behavior:

* Creating a new game clears any existing puzzle data before initializing a new one.
* Destroying the activity clears the Room database.
* At any point, the database contains at most one active game.
* Room is used as an in-memory representation of the current game state rather than long-term persistence.

This design simplifies the architecture by eliminating concerns such as saved games, multiple game management, or migration between sessions, while still providing the benefits of transactional updates and reactive state observation during gameplay.

The lifecycle can be summarized as:

```text
Launch App
      │
      ▼
Create New Game
      │
      ▼
Clear Database
      │
      ▼
Insert Initial Game State
      │
      ▼
Play Game
      │
      ▼
Activity Destroyed
      │
      ▼
Clear Database
```

### Design Rationale

Although Room is typically used for persistent storage, in Slide Slide it serves as the application's **reactive single source of truth**. Every game mutation—tile movement, undo operations, move history, and game progress—is performed transactionally and immediately reflected to the UI through `Flow`.

Because each game is ephemeral, clearing the database on game creation and activity destruction guarantees that the application always starts from a clean state, avoiding stale data while retaining the architectural benefits of a database-backed state model.
