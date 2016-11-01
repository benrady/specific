(ns specific.sample)

(defn some-fun [greeting & names]
  (spit "fun.txt" (str greeting " " (clojure.string/join ", " names))))

(clojure.spec/fdef some-fun
           :args (clojure.spec/+ string?)
           :ret string?)

(defn no-spec [])
