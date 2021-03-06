;; Based on the Nature of Code
;; by Daniel Shiffman
;; http://natureofcode.com
;;
;; Specifically:
;; https://github.com/shiffman/The-Nature-of-Code-Examples/tree/master/Processing/introduction/NOC_I_3_RandomWalkTendsToRight
;;
(ns natureofclojure.i-2.walker.tends-to-right
  (:require [quil.core :as qc]))

(def WIDTH 800)
(def HEIGHT 600)
(def walker (atom {:x (/ WIDTH 2.0)
                   :y (/ HEIGHT 2.0)}))

(defn pull-walker
  "Slightly altered stepper to pull to the right."
  [w-atom]
  (let [choice (rand-int 100)]
    (cond (< choice 40) (swap! w-atom update-in [:x] #(qc/constrain-float (+ % 1) 0 WIDTH))
          (< choice 60) (swap! w-atom update-in [:x] #(qc/constrain-float (- % 1) 0 WIDTH))
          (< choice 80) (swap! w-atom update-in [:y] #(qc/constrain-float (+ % 1) 0 HEIGHT))
          :else (swap! w-atom update-in [:y] #(qc/constrain-float (- % 1) 0 HEIGHT)))))

(defn render-walker [w]
  (qc/stroke 0)
  (qc/point (:x w) (:y w)))