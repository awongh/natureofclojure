;; Based on the Nature of Code
;; by Daniel Shiffman
;; http://natureofcode.com
;;
;; Specifically:
;; https://github.com/shiffman/The-Nature-of-Code-Examples/tree/master/chp6_agents/NOC_6_09_Flocking
;;
(ns natureofclojure.ch6-8-flocking.boid-slider
  (:require
   [quil.core :as q]
   [quil.middleware :as m]
   [natureofclojure.math.fast-vector :as fvec]
   [natureofclojure.ch6-8-flocking.behavior :as beh]
   [natureofclojure.slider.h-slider :as slider]))

(def SIZE-W 800.0)
(def SIZE-H 600.0)

(def VEHICLE-R 4.0)

(def SEPARATION-DIST 30)
(def NEIGHBOR-DIST 100)
(def GLOM-DIST 50)

(defn random-v-comp []
  (let [r (+ 20.0 (rand 40.0))]
    (if (> (rand 1.0) 0.5)
      (* -1 r)
      r)))

(defn random-vehicle
  ([]
     (random-vehicle (rand SIZE-W) (rand SIZE-H)))
  ([x y]
     {:location (fvec/fvec x y)
      :velocity (fvec/fvec (random-v-comp)
                           (random-v-comp))
      :acceleration (fvec/fvec 0.0 0.0)
      :max-speed 3.0
      :max-force 0.2}))

(def VEHICLES
  (vec
   (for [_ (range 10)]
     (random-vehicle))))

(def SLIDERS [(-> (slider/slider {:x 10 :y 20 :h 10 :label "separation"})
                  (slider/set-pos 0.5))
              (-> (slider/slider {:x 10 :y 40 :h 10 :label "neighbor"})
                  (slider/set-pos 0.5))
              (-> (slider/slider {:x 10 :y 60 :h 10 :label "cohesion"})
                  (slider/set-pos 0.5))])

(defn update-state [k v state]
  (-> state
      (assoc-in [k] v)))

(defn update-sep-dist [v state]
  (update-state :separation-dist v state))

(defn update-neighbor-dist [v state]
  (update-state :neighbor-dist v state))

(defn update-glom-dist [v state]
  (update-state :glom-dist v state))

(defn setup []
  (-> {:vehicles VEHICLES
       :sliders SLIDERS}
      (partial update-sep-dist SEPARATION-DIST)
      (partial update-neighbor-dist NEIGHBOR-DIST)
      (partial update-glom-dist GLOM-DIST)))

(defn flock [all vehicle]
  (let [sep-factor   1.5
        align-factor 1.0
        glom-factor  1.0
        sep-force (fvec/*
                   (beh/separate SEPARATION-DIST all vehicle)
                   sep-factor)
        align-force (fvec/*
                     (beh/align NEIGHBOR-DIST all vehicle)
                     align-factor)
        glom-force (fvec/*
                    (beh/glom GLOM-DIST all vehicle)
                    glom-factor)]
    (-> vehicle
        (beh/apply-force sep-force)
        (beh/apply-force align-force)
        (beh/apply-force glom-force))))

(defn update-vehicles [vehicles]
  (doall
   (mapv #(->> %
               (flock vehicles)
               (beh/move-vehicle)
               (beh/borders SIZE-W SIZE-H VEHICLE-R))
         vehicles)))

(defn update-sliders [sliders]
  (doall
   (mapv slider/update sliders)))

(defn update [{:keys [sliders vehicles] :as state}]
  (let [updated-sliders (update-sliders sliders)
        
        ]
    (-> state
        (update-in [:vehicles] update-vehicles)
        (assoc-in [:sliders] updated-sliders))))

(defn draw-vehicle
  [{:keys [location velocity]}]
  (let [[x y] (fvec/x-y location)
        theta (+ (/ Math/PI 2.0)
                 (fvec/heading velocity))]
    (q/with-translation [x y]
      (q/with-rotation [theta]
        (q/begin-shape)
        (q/vertex 0                  (* -2.0 VEHICLE-R))
        (q/vertex (* -1.0 VEHICLE-R) (* 2.0 VEHICLE-R))
        (q/vertex VEHICLE-R          (* 2.0 VEHICLE-R))
        (q/end-shape :close)))))

(defn draw [state]
  (q/background 0)
  (let [{:keys [vehicles]} state]
    (q/stroke 0.5)
    (q/fill 180)
    (doall (map draw-vehicle vehicles))))

(defn mouse-dragged [state event]
  (let [{:keys [x y]} event]
    (update-in state [:vehicles] #(conj % (random-vehicle x y)))))

(q/defsketch quil-workflow
  :title "Flocking: Boids"
  :size [SIZE-W SIZE-H]
  :setup setup
  :update update
  :draw draw
  :mouse-dragged mouse-dragged
  :middleware [m/fun-mode])
