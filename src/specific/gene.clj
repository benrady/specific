(ns specific.gene
  (:require [clojure.test.check
             [generators :as gen]
             [rose-tree :as rose]
             [random :as random]]
            [clojure.test.check :as check]
            [clojure.spec :as spec]))

(def det-rng (reify random/IRandom 
                (rand-long [_] 42) 
                (rand-double [_] 42.0) 
                (split [this] [this this]) 
                (split-n [this n] (repeat n this))))

(defn static-sample 
  "Returns a deterministic sample generated from the spec"
  ([spec] (static-sample spec {}))
  ([spec overrides]
   (with-redefs [random/make-random (partial random/make-random 42)]
     (first (clojure.spec.gen/sample (spec/gen spec overrides) 1)))))
