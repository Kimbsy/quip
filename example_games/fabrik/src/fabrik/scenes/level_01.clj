(ns fabrik.scenes.level-01
  (:require [quil.core :as q]
            [quip.sprite :as qpsprite]
            [quip.util :as qpu]))

(def light-green [133 255 199])
(def dark-blue [0 43 54])
(def white [245 245 245])
(def orange [255 90 96])

(defn sprites
  "The initial list of sprites for this scene"
  []
  [])

(def joint-size 15)
(def joint-count 15)
(def joint-gap 30)
(def starting-chain (mapv vector
                          (repeat 400)
                          (take joint-count
                                (map #(+ 100 (* joint-gap %))
                                     (range)))))

(defn draw-level-01
  "Called each frame, draws the current scene to the screen"
  [state]
  (qpu/background dark-blue)
  (qpsprite/draw-scene-sprites state)

  (let [chain (get-in state [:scenes :level-01 :chain])]
    (q/stroke orange)
    (q/stroke-weight 4)
    (doseq [[p1 p2] (partition 2 1 chain)]
      (q/line p1 p2))
    
    (q/no-stroke)
    (q/fill light-green)
    (doseq [[x y] chain]
      (q/ellipse x y joint-size joint-size))))

(defn get-lengths
  "Calculate the distances between joints in a chain."
  [chain]
  (->> chain
       (partition 2 1)
       (map (fn [[p1 p2]]
              (qpu/magnitude (map - p2 p1))))))

(defn tail-to-root
  "Assuming the last (tail) joint of the chain is in place (at the mouse pos),
  start at the second last and iterate to the root.

  For each joint, get the direction to the next joint (towards the
  tail), move the current joint along this vector till it is the same
  distance it used to be from the next joint."
  [chain original-lengths]
  (reduce (fn [acc [pos l i]]
            (let [next-joint (get acc (inc i))
                  direction (map - pos next-joint)
                  move (map (partial * l) (qpu/unit-vector direction))                  
                  new-pos (mapv + next-joint move)]
              (assoc acc i new-pos)))
          chain
          (reverse (map list
                        (butlast chain) 
                        original-lengths
                        (range (count chain))))))

(defn root-to-tail
  "Assuming the first (root) joint of the chain is in place (locked into
  it's original position), start at the second and iterate to the
  tail.

  For each joint, get the direction to the previous (towards the
  root), move the current joint along this vector till it is the same
  distance it used to be from the previous joint."
  [chain original-lengths]
  (reduce (fn [acc [pos l i]]
            (let [prev-joint (get acc (dec i))
                  direction (map - pos prev-joint)
                  move (map (partial * l) (qpu/unit-vector direction))                  
                  new-pos (mapv + prev-joint move)]
              (assoc acc i new-pos)))
          chain
          (map list
               (rest chain) 
               original-lengths
               (rest (range)))))

(defn inverse-kinematics
  "Implement the FABRIK algorithm"
  [chain]
  (let [original-lengths (get-lengths chain)
        g-chain (vec (concat [(first chain)]
                             (map (fn [[x y]]
                                    [x (+ y 7)])
                                  (rest chain))))
        mouse-pos [(q/mouse-x) (q/mouse-y)]
        new-chain (if (q/mouse-pressed?)
                    (conj (vec (butlast g-chain))
                          mouse-pos)
                    g-chain)]
    ;; @TODO: need to do this till convergence or limit
    (nth (iterate (fn [c]
                    (-> c
                        (tail-to-root original-lengths)
                        (assoc 0 (first g-chain)) ;; anchor root
                        (root-to-tail original-lengths)))
                  new-chain)
         10)))

(defn update-level-01
  "Called each frame, update the sprites in the current scene"
  [state]

  ;; @TODO: do a 2d array of joints instead of a single chain
  
  ;; if mouse held, move end node to mouse pos
  (let [chain (get-in state [:scenes :level-01 :chain])]
    (assoc-in state [:scenes :level-01 :chain]
              (inverse-kinematics chain))))

(defn init
  "Initialise this scene"
  []
  {:sprites (sprites)
   :draw-fn draw-level-01
   :update-fn update-level-01
   :chain starting-chain})
