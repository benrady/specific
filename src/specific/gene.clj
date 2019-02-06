(ns specific.gene
  (:require [clojure.test.check.random :as random]
            [clojure.test.check.rose-tree :as rose]
            [clojure.test.check :as check]
            [clojure.test.check.generators :as gen]
            [clojure.spec.alpha :as spec]))

(def ^:dynamic *gen-overrides* {})

(def det-rng (random/make-random 42))

(defn det-sample 
  "Returns a deterministic sample generated from the spec"
  ([spec] (det-sample spec *gen-overrides*))
  ([spec overrides]
   (rose/root (gen/call-gen (spec/gen spec (merge *gen-overrides* overrides)) det-rng 0))))
     
