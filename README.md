## Green Space landscaping tool

A design aid tool for plant landscaping, by which users are able to design their gardens using trees and shrubs. It also support importing simple models in obj format.

* 3D plants are modelled with a rule based rewriting system: L-System.
* Techniques used: phone shading, shadow map, soft shadows (persantage closer filtering), particle system (compute shaders), instance drawing, rule based plant modelling (L-System), MSAA.
* JOGL library version: 2.3.0
* [Live demo on youtube](https://www.youtube.com/watch?v=7JP8YgwPKTw)

## Modules
    ./src/basicsource         // Main entry, GUIs
    
    ./src/Plants              // Plant model objects
    
    ./src/LSystem             // L-System interpreter
    
    ./src/StandardObjects     // Basic 3D model objects
    
    ./src/StandardObjects     // Data structures, particle systems, camera & light resources
    
    ./data/                   // L-System model description files
    
    ./images/                 // Texture images
    
    ./models/                 // .obj model files

## How to run it
* Check whether the running graphics card support OpenGL 4.0+
* Install eclipse, download JOGL library, and JavaFX library
* Create a JOGL project as library dependency [following instructions here](https://jogamp.org/wiki/index.php/Setting_up_a_JogAmp_project_in_your_favorite_IDE)
* Import and open Green Space project, setup project dependency and JavaFX dependency

## Author
  Sangzhuoyang Yu
