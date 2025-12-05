# Soldier Rescue Mission A Pathfinding JavaFX
📌 Overview
This project implements an Expert System for tactical path planning using the A* algorithm within a 20×20 grid-based environment. It simulates a Combat Search and Rescue (CASR) mission where soldiers navigate from a base camp (Source) to a kidnapped commando (Target) while avoiding randomly generated Danger Zones.

The system integrates:
✔️ Real-time A* pathfinding
✔️ JavaFX visualization
✔️ Soldier movement animation
✔️ Automatic rescue + return mission
✔️ Dynamic danger-zone generation

The final output includes a full mission simulation ending with a “MISSION SUCCESSFUL!” screen.

🎯 Features
🔹 A* Pathfinding Engine

Computes shortest, safest path using Manhattan heuristic

Avoids all Danger Zones

Guarantees optimal & admissible paths

Uses g + h scoring for efficient node exploration

🔹 Tactical Mission Simulation

Two soldiers move from Source → Target

Rescued soldier joins team

All soldiers return back from Target → Source

Smooth animations using JavaFX PathTransition

Real-time grid rendering and obstacle visualization

🔹 Visualization Layer (JavaFX)

Grid overlay

Background battlefield map

Danger Zones highlighted

Animated soldiers (left/right facing sprites)

Kidnapped commando at target location

Fade-in mission success screen

🧠 Tech Stack & Concepts
Component	Technology / Concept
Language	Java 
GUI Toolkit	JavaFX
Algorithm	A* Search Algorithm
Animation	JavaFX PathTransition, FadeTransition
Data Structures	Priority Queue, HashMap, HashSet
   
