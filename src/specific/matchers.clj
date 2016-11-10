(ns specific.matchers
  (:require [clojure.spec :as spec]
            [clojure.test :as ctest]))

(defn- conform-or-explain [spc act]
  (if (spec/valid? spc act)
    (spec/conform spc act)
    (spec/explain-str spc act)))

(defn- match-args [expected actual]
  (if (= (count expected) (count actual))
    (for [[exp act] (map vector expected actual)]
      (if-let [expected-spec (spec/get-spec exp)]
        (conform-or-explain expected-spec act)
        exp))
    expected)) 

(defn calls [spyf]
  "Get the recorded calls from a test double"
  (if-let [calls (:specific-calls (meta spyf))]
    (get (deref calls) ctest/*testing-contexts* [])
    {:msg (str spyf " is not a test double")}))

(defn compare-conforming [test-double expected-args]
  (for [actual (calls test-double)]
    (= actual (match-args expected-args actual))))

(defn conforming [test-double & expected-args]
  (and 
    (seq (calls test-double))
    (every? identity (compare-conforming test-double expected-args))))
