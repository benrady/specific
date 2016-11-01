(ns specific.sample
  (:require [clojure.spec :as spec]))

(defn some-fun [greeting & names]
  (apply str greeting ", " names))

(spec/fdef some-fun
           :args (spec/+ string?)
           :ret string?)

(defn no-spec [])
