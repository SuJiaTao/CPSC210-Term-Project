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