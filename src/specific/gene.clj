(ns specific.gene
  (:require [clojure.test.check.random :as random]
            [clojure.test.check :as check]
            [clojure.spec.gen :as gen]
            [clojure.spec :as spec]))

(def det-rng (random/make-random 42))

(defn det-sample 
  "Returns a deterministic sample generated from the spec"
  ([spec] (det-sample spec {}))
  ([spec overrides]
   (with-redefs [random/make-random (fn [& _] det-rng)]
     (first (gen/sample (spec/gen spec overrides) 1)))))
