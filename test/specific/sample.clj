(ns specific.sample
  (:require [clojure.spec.alpha :as spec]
            [clojure.java.shell :as shell]
            [clojure.string :as string]))

(defn greet [pre sufs]
  (string/join ", " (cons pre sufs)))

(defn cowsay [msg]
  (shell/sh "cowsay" msg)) ; Fails in some environments

(defn some-fun [greeting & names]
  (:out (cowsay (greet greeting names))))

(spec/def ::exit (spec/and integer? #(>= % 0) #(< % 256)))
(spec/def ::out string?)
(spec/def ::fun-greeting string?)
(spec/fdef greet :ret ::fun-greeting)
(spec/fdef cowsay
           :args (spec/cat :fun-greeting ::fun-greeting)
           :ret (spec/keys :req-un [::out ::exit]))
(spec/fdef some-fun
           :args (spec/cat :greeting ::fun-greeting
                           :names (spec/* string?))
           :ret string?)
