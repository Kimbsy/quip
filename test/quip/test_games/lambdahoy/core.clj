(ns quip.test-games.lambdahoy.core
  (:gen-class)
  (:require [quip.test-games.lambdahoy.scenes.end :as end]
            [quip.test-games.lambdahoy.scenes.menu :as menu]
            [quip.test-games.lambdahoy.scenes.ocean :as ocean]
            [quip.core :as quip]))

(def starting-difficulty 1)

(defn setup
  []
  {:difficulty starting-difficulty})

(defn init-scenes
  []
  {:menu  (menu/init)
   :ocean (ocean/init)
   :end   (end/init)})

(def test-game
  (quip/game
   {:title          "Lambdahoy: functional adventures on the high seas"
    :size           [1800 1200]
    :setup          setup
    :init-scenes-fn init-scenes
    :current-scene  :menu}))

(defn -main
  [& _]
  (quip/run test-game))
