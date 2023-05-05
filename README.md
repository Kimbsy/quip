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

## Creating a game

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

### Sprites

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
