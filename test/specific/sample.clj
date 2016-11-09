(ns specific.sample
  (:require [clojure.spec]
            [clojure.string :as string]
            [clojure.java.shell :as shell]))

(defn no-spec [])

(defn flip-two [a b]
  [b a])

(clojure.spec/def ::number number?)

(defn some-fun [greeting & names]
  (let [msg (str greeting " " (string/join ", " names))]
    (spit "fun.txt" msg)
    msg))

(clojure.spec/fdef some-fun
                   :args (clojure.spec/+ string?)
                   :ret string?)

(defn more-fun [greeting opts]
  (apply shell/sh "cowsay" greeting (vector opts)))
