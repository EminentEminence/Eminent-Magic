Eminent Magic - Development Logging
=====
This is a file detailing all the relevent details of the project structure as well as the state of various features.
-----
The Goal:
-----
- [ ] Finish and publish the mod

Functional Requirements:
=====

General Functional Requirements:
-----
- [X] A ground up mana system with trainable skills of Strength, Control and Perception alongside a simple reserve of Mana.
- [X] A spellcasting system that uses the mana system to allow players to cast spells with full control over how much Strength and Control is used.
- [ ] A variety of spells which take full advantage of the spellcasting system.
- [ ] The ability for a player to train their various spellcasting attributes.
- [ ] Mostly no bugs :)

Mana System:
-----
- [X] Attributes for all players to handle the stats for Strength, Control, Perception, Current Mana, and Total Mana Reserves
- [X] Strength, Control and Perception should range from 1 to 99
- [X] Current Mana and Total Mana should range from 0 to 9999

- [ ] Players should be able to restore their current mana up to their Total Mana

- [ ] Player should be able to train each of their specific attributes up to their maximum

Spell Casting:
-----
- [X] Various wands of increasing quality with a spell selector that allow for multiple spells to be cast
- [X] Mana consumption that drains mana then health then the capacity of the Mana Reserves

- [X] The mana cost of spells should scale based on how much strength and control are being used to cast them
- [X] The scaling of mana costs should vary based on the parameters of the spells

- [X] Depending on the mana cost of spells, they should take longer to cast the more mana they cost.
- [X] An initial amount of the mana is consumed at the start of casting, the rest is consumed over time 
- [X] Once all the required mana has been consumed the spell's effect will be triggered

Spell System:
-----
- [X] All spell parameters should be stored inside of .JSON data
- [X] The .JSON data of spells should have default values which are used to create the .JSON if the spells do not exist
- [X] The .JSON data should not be overriden if the user has changed the .JSON files directly

Spells:
-----
- [ ] Zoltraak - A simple laser beam
- [X] Freeze Coffin - an AOE freeze spell
- [X] Ingall - A source resurection spell
- [ ] A fucking barrier spell (PLEASE FOR THE LOVE OF GOD FIGURE THIS OUT) - HIGH PRIORITY

- [ ] More Spells


Defensive Magic TODO List:
-----

Barrier representation
- [X] A barrier must have a 3D position, orientation, 2D shape, and mana capacity.
- [X] The initial shape should be a regular hexagon.
- [X] The barrier's shape must be independent of Minecraft's block grid.

2D shape collision
- [x] The system must transform world-space collision points into the barrier's local 2D coordinate space.
- [x] It must determine whether a point lies within the barrier's shape.
- [x] The system must detect when a line segment crosses the barrier's plane and whether the intersection lies within the shape.

Barrier management
- [X] A central BarrierManager must track all active barriers in each dimension.
- [X] Barriers must be able to be added, removed, and queried.
- [X] Barriers must store and expose their remaining mana.

Projectile interaction
- [x] The system must detect projectiles crossing a barrier without modifying individual projectile classes.
- [x] A blocked projectile must stop/be destroyed at the barrier.
- [x] Blocking a projectile must reduce the barrier's mana according to the projectile's damage/power.

Entity interaction
- [ ] The system must detect entities attempting to move through a barrier.
- [ ] Entity movement must be prevented when its collision volume would cross the barrier.
- [ ] The system must work for arbitrary entities, including players, mobs, boats, and minecarts.

Spell interaction
- [ ] Other spells must be able to query the BarrierManager for barrier intersections.
- [ ] Spells must not need to know how barriers are internally implemented.
- [ ] A spell hitting a barrier must be able to consume mana and/or be blocked according to its properties.
- [ ] Collision result
- [ ] Barrier collision queries should return at minimum: Which barrier was hit, Hit position, Barrier surface normal, Distance along the movement/trace
- [ ] Collision detection itself should not automatically consume mana; the caller decides how the hit affects the barrier.

Persistence
- [x] Active barriers must be synchronized to clients.

Additional Features
=====
Magic-nullifying Crystal
-----
- [ ] Spawns in caves naturally
- [ ] Prevents spell casting nearby
- [ ] Prevents the detection of mana nearby
- [ ] Can be imbued with mana to cause it to glow
- [ ] Requires diamond (or maybe netherite) to break

Mana Reactor 
-----
- [ ] Multiblock object capable of storing Mana
- [ ] Requires containement field using magic-nullifying crystal
- [ ] Requires significant amount of mana to start
- [ ] Capable of draining mana from entities on contact