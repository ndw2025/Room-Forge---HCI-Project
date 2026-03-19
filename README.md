# RoomForge - Smart Furniture Preview (Desktop Application)

RoomForge is a Java desktop application for furniture layout design and visualization. It allows designers to model rooms, place furniture in 2D, and preview the same layout in 3D for customer consultations.

## Key Features

- Role-based flow with first-run super-admin setup and login.
- Designer workflow for creating room designs (shape, dimensions, color, description).
- Interactive 2D editor with furniture placement, drag, rotate, resize, and color updates.
- 3D preview using jMonkeyEngine with multiple camera modes.
- Template save/edit/delete for reusable design layouts.
- Admin dashboard for user, project, and report-related views.
- SQLite local persistence with automatic schema initialization/update.

## Tech Stack

- Java 21
- Gradle (multi-project with `app` module)
- Java Swing + FlatLaf + MigLayout
- jMonkeyEngine 3.5.2 (LWJGL)
- SQLite JDBC
- BCrypt for password hashing
- JUnit 5 for tests

## Prerequisites

- Windows machine (project currently includes `lwjgl64.dll` and `OpenAL64.dll` in root).
- JDK 21 installed.
- Git installed.

## Getting Started

### 1) Clone the repository

```bash
git clone <your-repo-url>
cd FurnitureDesigner
```

### 2) Configure Java 21 for Gradle

This project currently uses a pinned Java path in `gradle.properties`:

```properties
org.gradle.java.home=C:/Program Files/Eclipse Adoptium/jdk-21.0.10.7-hotspot
```

If this path does not exist on your machine, update it to your local JDK 21 path.

Recommended check:

```powershell
.\gradlew.bat --version
```

You should see `Daemon JVM: ... 21 ...`.

### 3) Run the application

```powershell
.\gradlew.bat run
```

## First Launch Behavior

- On first launch, if no super-admin exists, the app shows the super-admin setup screen.
- Database file is created at:
  - `database/furniture_designer.db`

Seeded account behavior:
- A default `designer` user is seeded in database initialization (password hash stored in DB).
- Super-admin account is created through the setup UI when required.

## Build and Packaging

Build app artifacts:

```powershell
.\gradlew.bat assemble
```

Create fat JAR:

```powershell
.\gradlew.bat fatJar
```

Output:
- `app/build/libs/app-fat.jar`

## Testing

Run tests:

```powershell
.\gradlew.bat test
```

Current test suite includes a basic app instantiation smoke test (`AppTest`).

## Project Structure (Important Paths)

- `app/src/main/java/com/furnituredesigner/`
  - `client/ui/` - Swing UI panels (login, dashboard, 2D, 3D, admin pages)
  - `server/service/` - business logic/services
  - `server/db/DatabaseManager.java` - DB init + migrations
  - `common/model/` - domain models (`User`, `Room`, `Furniture`, `Template`)
- `app/src/main/resources/` - textures and DB query helper file
- `database/` - SQLite DB file
- `gradle/` - wrapper/tooling config

## Common Issues and Fixes

### 1) `Unsupported class file major version 69`

Cause:
- Running build with Java 25 while this project is configured for Java 21.

Fix:
- Install/use JDK 21 and ensure Gradle runs on Java 21.

### 2) `org.gradle.java.home ... is invalid`

Cause:
- `gradle.properties` has a machine-specific JDK path that does not exist on your system.

Fix:
- Update `org.gradle.java.home` to a valid local JDK 21 path.

### 3) Native library errors (`OpenAL64.dll` / `lwjgl64.dll`)

Fix:
- Keep these DLL files in the project root when running on Windows.

## Team Contribution Workflow (Recommended)

1. Pull latest `main`.
2. Create your feature branch.
3. Commit only your assigned files.
4. Push branch and open PR.
5. Merge after review.

If using prepared member zip packs, follow:
- `TEAM_USAGE.md`
- `member_contribution_packs/<member-pack>/COMMIT_INSTRUCTIONS.md`

## Module Context

This project was developed for a coursework scenario focused on:
- Human-Computer Interaction (HCI)
- Computer Graphics and Visualization
- Team-based software engineering workflow

## License

Educational use only unless explicitly relicensed by the project owners.
