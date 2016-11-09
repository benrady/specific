(ns specific.sample
  (:require [clojure.spec]
            [clojure.string :as string]))

(defn no-spec [])

(defn flip-two [a b]
  [b a])

(clojure.spec/def ::number number?)

(defn some-fun [greeting & names]
  (let [msg (str greeting " " (string/join ", " names))]
    (spit "fun.txt" msg)
    msg))

(clojure.spec/def ::fun-greeting string?)

(clojure.spec/def ::words (clojure.spec/+ string?))

(clojure.spec/fdef some-fun
                   :args ::words
                   :ret ::fun-greeting)
