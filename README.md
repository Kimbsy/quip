# quip

[![Clojars Project](https://img.shields.io/clojars/v/quip.svg)](https://clojars.org/quip)

A Clojure game engine made using [Quil](http://quil.info/).

## Quick start

The easiest way to get going is to use the quip Leiningen template:

``` bash
lein new com.github.kimbsy/quip <project-name>
```

This will create a game project containing two scenes, `menu` and `level-01`.

you can run your game locally with Leiningen:

``` bash
lein run
```

Or from the repl:

``` Clojure
(-main)
```

You can build your game as a .jar for distribution.

``` bash
# build
lein uberjar

# run
java -jar target/uberjar/<project>-<version>-standalone.jar
```

Taking a look at the [example games](example_games) gives a few simple demonstrations of how to use animated sprites, basic music and sound effects, and others.

For more advanced examples feel free to check out my games on Itch.io

- The Sequence Abstraction ([GitHub](https://github.com/Kimbsy/sequence-abstraction)) ([Itch.io](https://kimbsy.itch.io/the-sequence-abstraction))
- Variable Volatility ([GitHub](https://github.com/Kimbsy/variable-volatility)) ([Itch.io](https://kimbsy.itch.io/variable-volatility))
- Ssss-Expressions ([GitHub](https://github.com/Kimbsy/ssss-expressions)) ([Itch.io](https://kimbsy.itch.io/ssss-expressions))
- Dynamically Typed ([GitHub](https://github.com/Kimbsy/dynamically-typed)) ([Itch.io](https://kimbsy.itch.io/dynamically-typed))

### Creating a game

The `quip.core/game` function takes an options map and returns a quip game configuration which can be transformed into a Quil sketch and executed using `quip.core/run`.

``` Clojure
;; The simplest "game"
(def g (quip.core/game {}))

;; It's not exciting, but it does run
(quip.core/run g)
```

### Scenes

In order to do anything useful, your game's options map should include at least an `:init-scenes-fn` and a `:current-scene`.

The `:init-scenes-fn` should be a function which returns a map of `scene-key => scene-configuration` where each scene-configuration should be a map representing the state of that scene. Primarily, a scene-configuration should contain a `:update-fn`, and a `:draw-fn`. The `:current-scene` should be a scene-key.

The `:update-fn` of the current scene is called every frame. It takes the game state from the previous frame and returns the new game state.

The `:draw-fn` of the current scene is called every frame. It takes the updated game state and must draw the current scene.

``` Clojure
(ns quip.foo
  (:require [quip.core :as qp]
            [quip.utils :as qpu]))

(defn update-level-01
  [state]
  ;; always return the state, even if we're not doing anything
  state)

(defn draw-level-01
  [state]
  ;; fill the screen with an RGB colour
  (qpu/background [0 43 54]))

(defn scenes
  []
  {:level-01 {:update-fn update-level-01
              :draw-fn draw-level-01}})

(def g (qp/game {:init-scenes-fn scenes
                 :current-scene :level-01}))

(qp/run g)
```

### State

The `state` that is passed into a scene's update and draw functions can contain anything you like. It's the primary place to store information about the game as it runs and is available in all scene update functions, input handling functions, collision handling functions, tween on-complete functions etc. By default it will contain a map of the scenes in the game, the set of currently held keyboard keys, and some utility fields.

You can enrich the initial state of the game by supplying a `:setup` function in your game's options map. This function should return a map which will be merged over the default game state map.

``` Clojure
(ns quip.foo
  (:require [quip.core :as qp]
            [quip.utils :as qpu]))

(defn update-level-01 [state] state)

(defn draw-level-01 [state] (qpu/background [0 43 54]))

(defn scenes []
  {:level-01 {:update-fn update-level-01
              :draw-fn draw-level-01}})

(defn setup
  []
  ;; custom initial state
  {:highscore 0
   :unlocked-acheivements []
   :foo "bar"})

(def g (qp/game {:init-scenes-fn scenes
                 :current-scene :level-01
                 :setup setup}))

(qp/run g)

```

### Sprites

Adding sprites to a scene is straightforward, the scene's configuration map can specify a `:sprites` vector.

``` Clojure
(ns quip.foo
  (:require [quip.core :as qp]
            [quip.utils :as qpu]
            [quip.sprite :as qpsprite]))

(defn update-level-01 [state] state)

(defn draw-level-01 [state] (qpu/background [0 43 54]))

(defn scenes []
  {:level-01 {:update-fn update-level-01
              :draw-fn draw-level-01
              ;; adding a single image-sprite
              :sprites [(qpsprite/image-sprite :player
                                               [100 100]
                                               24
                                               24
                                               "player.png")]}})

(def g (qp/game {:init-scenes-fn scenes
                 :current-scene :level-01}))

(qp/run g)
```

Alternatively sprites can be added to or removed from a scene by modifying the `:sprites` collection for that scene in any function that returns a new state.

``` Clojure
;; this could be in a scene update functions, an input handler, a collision handler etc.
(update-in state
           [:scenes :level-01 :sprites]
           conj
           (qpsprite/image-sprite :enemy
                                  [50 50]
                                  10
                                  10
                                  "enemy.png"))
```

Sprites have their own update and draw functions, you can invoke them in the scene update and draw functions:

``` Clojure
(defn update-level-01
  [state]
  ;; uses the value of `:current-scene` in state to update that scene's sprites
  (qpsprite/update-scene-sprites state))

(defn draw-level-01
  [state]
  (qpu/background [0 43 54])
  ;; uses the value of `:current-scene` in state to draw that scene's sprites
  (qpsprite/draw-scene-sprites state))
```

An image sprite needs a group keyword (used for collision detection, and z-height layers), a position vector, a width, a height, and a filepath for it's image (relative to the `resources` directory).

Optionally you can also specify:

``` Clojure
(qpsprite/image-sprite
 :player
 [100 100]
 24
 24
 "player.png"
 :rotation 90                ;; rotation in degrees
 :vel [-1 1]                 ;; velocity vector
 :update-fn (fn [s] ... )    ;; custom update function
 :draw-fn (fn [s] ... )      ;; custom draw function
 :points [[0 0] ... ]        ;; list of points describing bounding polygon
 :bounds-fn (fn [s] ... )    ;; custom bounds function
 )
```

The default update function updates the position based on the velocity, the default draw function draws the image to the scene. The default bounding function (used for collision detection) returns a rectangle based on the sprites width and height unless `:points` are specified in which case it will return the polygon they describe. Specifying `:bounds-fn` will override the default bounding function and should return a set of points describing a polygon.

The animated-sprite function is slightly more complex as it takes a spritesheet image (rather than a single image) and an optional `:animations` configuration map describing the animations found in the spritesheet (each animation should be a row in the spritesheet). The animation configuration map should detail each animation, specifying `:frames` (the number of frames in this animation), `:y-offset` (zero-indexed position from the top of the sheet, with the unit being the sprite's height) and `:frame-delay` (the number of game frames to wait before moving to the next animation frame). Finally, the sprite should also specify the `:current-animation`.

``` Clojure
(qpsprite/animated-sprite
 :player
 [100 100]
 24
 24
 "player-spritesheet.png"
 :animations {:idle {:frames      4
                     :y-offset    0
                     :frame-delay 15}
              :run  {:frames      4
                     :y-offset    1
                     :frame-delay 8}
              :jump {:frames      7
                     :y-offset    2
                     :frame-delay 8}}
 :current-animation :idle)
```

### Inputs

### Sounds

### Delays

### Collisions

### Tweens

----

## Documentation

You can generate documentation for quip locally using [Codox](https://github.com/weavejester/codox):

``` bash
lein codox
```

## License

Distributed under the Eclipse Public License either version 1.0 or (at your option) any later version.
