(ns specific.sample)

(defn some-fun [greeting & names]
  (let [msg (str greeting " " (clojure.string/join ", " names))]
    (spit "fun.txt" msg)
    msg))

(clojure.spec/fdef some-fun
           :args (clojure.spec/+ string?)
           :ret string?)

(defn no-spec [])
