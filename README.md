This is a Bukkit/Spigot plugin for Minecraft that provides comprehensive player statistics tracking, inventory management, and achievement systems with database integration.

Key Features:
Player Statistics Tracking:

Records kills, deaths, blocks broken, and in-game currency (balance)

Tracks login/logout times and player locations

Updates statistics in real-time during gameplay

Inventory Management:

Persistent storage of player inventory items in a MySQL database

Slot-based item tracking with quantity management

Automatic updates when players break blocks

Achievement System:

Unlockable achievements (e.g., "First Kill")

Achievement tracking in the database

Database Integration:

MySQL database connection with connection pooling

Automatic table creation on startup

Stored procedures for common operations

Event Handling:

Player join/quit events

Block break events

Player death/kill events

Player movement tracking

Technical Implementation:
Built as a Java plugin for Bukkit/Spigot servers

Uses MySQL for persistent data storage

Follows object-oriented design with separate classes for:

Database operations (DatadeBaze)

Event listeners (Listeners)

Player statistics model (Player_stats)

Implements proper connection handling and resource cleanup

The plugin provides server administrators with valuable player metrics while offering players persistent progression tracking across server sessions. The modular design allows for easy expansion with additional statistics or features.
