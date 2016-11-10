(ns specific.sample
  (:require [clojure.spec]
            [clojure.java.shell :as shell]
            [clojure.string :as string]))

(defn greet [pre sufs]
  (str pre ", " (string/join ", " sufs)))

(defn cowsay [msg]
  (shell/sh "cowsay" msg))

(defn some-fun [greeting & names]
  (:out (cowsay (greet greeting names))))

(clojure.spec/def ::exit (clojure.spec/and integer? #(>= % 0) #(< % 256)))
(clojure.spec/def ::out string?)
(clojure.spec/def ::fun-greeting string?)
(clojure.spec/fdef greet :ret ::fun-greeting)
(clojure.spec/fdef cowsay
                   :args (clojure.spec/tuple ::fun-greeting)
                   :ret (clojure.spec/keys :req-un [::out ::err ::exit]))
(clojure.spec/fdef some-fun
                   :args (clojure.spec/+ string?)
                   :ret string?)
;; Deprecated

(defn flip-two [a b]
  [b a])
