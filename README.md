# Bailey's CPSC210 Term Project


## Project Proposal

>### What does it do?
> The objective of this project is to produce a **3D n-body simulator**, which given the initial
>  - starting positions, 
>  - velocities, 
> - and radii 
> 
> of celestial objects, will compute their trajectories and determine whether they collide, while providing a means for **intuitive visualization**, as well as saving and loading of simulation presets and parameters. 

> ### Who is it for?
> The target user audience is junior *physicists* or *astronomers* in need of an application to help them better understand the interactions between multiple orbiting bodies.

> ### Why this?
> I've generally held an interest in creating physics simulations, though my projects have always been in 2D. A 3D n-body simulator should not prove to be too challenging to implement, as all particles are spherical, and hence collision detection is trivial. I suspect the hardest aspect of the project will be implementing a visualization system under the time constraints.

## User Stories

> - I want to be able to insert an arbitrary number of planets into my simulation, and give them each starting positions, velocities, and radii
> - I want to be able to advance the simulation by an arbitrary number of seconds, including fractional numbers of seconds
> - I want to be able to view the current positions, velocities, and radii of all planets at the current point in time of the simulation
> - I want to be able to view the current position, velocity and radius of a specific planet at the current point in time of the simulation
> - I want to be able to check if a specific planet has collided with any other planets
> - I want to be able to save the specific simulation state as a named file
> - I want to be able to load a specific simulation state that I saved earlier by its name

## Phase 4: Task 3 - Reflection

> ### If I had more time, what would I have done different?
> Firstly, I definitely would have refactored the old `SimulationManager` class instead of completely re-writing it, which *initially* I attempted but quickly realised it would take way too long. I think there was alot more room for abstraction. Each `XXXEditorPanel` class contained a `AbstractListPanel<T>` derived class, and similar logic, which at the time I felt could have easily been combined into another class which I wanted to name `AbstractEditorPanel<T>`, but ultimately the duplicated code wasn't all that complex, and I was running out of time. Also in retrospect, I realise that I really should have done away with the `SimulatorUtils` class, which initially was somewhat well organized, but ended up storing all static methods which I didnt want to bloat various classes, but were most definitely appropriate to be contained within them. Lastly, I really would have liked to include normals and lighting into my render engine, which I had the experience and time to do, however, I would have had to add a "rotation extraction" functionality from my `Transform` class, which would have been near impossible to test because **3D ROTATIONS ARE EVIL**.