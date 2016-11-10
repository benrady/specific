(ns specific.sample
  (:require [clojure.spec]
            [clojure.java.shell :as shell]
            [clojure.string :as string]))

(defn no-spec [])

(defn flip-two [a b]
  [b a])

(clojure.spec/def ::number number?)

(defn some-fun [greeting & names]
  (let [msg (str greeting " " (string/join ", " names))]
    (spit "fun.txt" msg)
    msg))

(defn cowsay [greeting & names]
  (shell/sh ("cowsay" (apply some-fun greeting names))))

(clojure.spec/def ::fun-greeting string?)

(clojure.spec/def ::words (clojure.spec/+ string?))

(clojure.spec/fdef some-fun
                   :args ::words
                   :ret ::fun-greeting)

(clojure.spec/def ::exit (clojure.spec/and integer? #(>= % 0) #(< % 256)))
(clojure.spec/def ::out string?)
(clojure.spec/def ::err string?)
(clojure.spec/def ::cow-result (clojure.spec/keys :req-un [::out ::err ::exit]))
(clojure.spec/fdef cowsay
                   :args ::words
                   :ret ::cow-result)
