TetrECS: A Multiplayer Block Puzzle Game
TetrECS is a fast-paced, modern block puzzle game developed in Java with the JavaFX framework. This project was initially a university coursework assignment that has been significantly enhanced with additional features, a custom visual theme, and a complete multiplayer backend, transforming it into a portfolio-ready application.

The game challenges players to place Tetromino-style pieces on a 5x5 grid, clearing horizontal and vertical lines to score points against a constantly speeding-up timer.

Features:
This project is a complete, feature-rich game that includes:

Core Gameplay:
Fast-Paced Action: A dynamic game loop with a timer that decreases as the player's level increases, creating a challenging experience.
Strategic Depth: Players can see the next two upcoming pieces, rotate the current piece, and swap pieces to plan their moves.
Scoring System: A combo-based multiplier rewards players for clearing lines on consecutive turns.
Custom Graphics & Animations: Features a custom "Galaxy Dark Mode" theme, procedurally generated starfield backgrounds, and smooth animations for piece placement and line clearing.
Full Audio Design: Includes background music for different scenes and sound effects for all major game actions.


Single-Player Mode:
Persistent High Scores: Local and online high score boards are fetched and displayed after each game. New high scores are saved to a local file and submitted to the online server.
Statistics Tracking: The game tracks and saves lifetime statistics, such as games played, total lines cleared, and highest score achieved.


Real-Time Multiplayer:
Game Lobby: A full-featured lobby system where players can view available game channels, create their own channel, or join an existing one.
Live Chat: Players within a channel can communicate in real-time using the in-game chat.
Synchronized Gameplay: All players in a match receive the exact same sequence of pieces from the server, ensuring a fair and competitive game.
Live Leaderboard: A dynamic leaderboard is displayed during gameplay, showing all players' scores and lives in real-time as they are updated.


User Experience:
Polished UI: Includes an animated intro screen, a clean menu system, and dedicated scenes for instructions, settings, and game-over sequences.
Settings Menu: A persistent settings screen allows players to adjust music and sound effect volume, with preferences saved between sessions.
Full Keyboard and Mouse Controls: The game is fully playable with either the keyboard for fast-paced action or the mouse for more deliberate placement.

Technologies Used:
Language: Java 17
Framework: JavaFX
Build Tool: Apache Maven
Networking: nv-websocket-client for WebSocket communication
Logging: Log4j2

How to Run: 
Prerequisites: 
Java Development Kit (JDK) 17 or newer.
Running the Executable JAR
Navigate to the /target/ directory in the project folder.
Run the executable uber-jar from your terminal:
java -jar tetrecs-1.0-SNAPSHOT.jar
(Note: The executable jar is built using the maven-shade-plugin and contains all necessary dependencies.)