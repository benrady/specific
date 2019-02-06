(ns specific.matchers
  (:require [clojure.spec.alpha :as spec]
            [clojure.test :as ctest]))

(defn- conform-or-explain [spc act]
  (if (spec/valid? spc act)
    (spec/conform spc act)
    (spec/explain-str spc act)))

(defn- find-spec [exp]
  (if (spec/spec? exp)
    exp
    (spec/get-spec exp)))

(defn match-args [expected actual]
  (if (= (count expected) (count actual))
    (for [[exp act] (map vector expected actual)]
      (if-let [expected-spec (find-spec exp)]
        (conform-or-explain expected-spec act)
        exp))
    expected)) 

(defn calls [spyf]
  "Get the recorded calls from a test double"
  (if-let [calls (:specific-calls (meta spyf))]
    (get (deref calls) ctest/*testing-contexts* [])
    (str spyf " is not a test double")))

(defn comparisons [test-double expected-args]
  (for [actual (calls test-double)]
    [actual (match-args expected-args actual)]))

(defn first-nonargs-conform [test-double expected-args]
  (first (first (filter (fn [[a b]] (not= a b)) (comparisons test-double expected-args)))))

(defn args-conform [test-double & expected-args]
  (and 
    (seq (calls test-double))
    (nil? (first-nonargs-conform test-double expected-args))))

(defmethod ctest/assert-expr 'args-conform [msg form]
  `(if ~form
     (ctest/do-report {:type :pass :message ~msg})
     (let [double-fn# (nth '~form 1)
           expected-args# (nthrest '~form 2)
           nonargs-conform-args# (first-nonargs-conform ~(nth form 1) (nthrest '~form 2))
           explain# (match-args expected-args# nonargs-conform-args#)]
       (ctest/do-report {:type :fail 
                         :message (or ~msg (str "Invocation of " double-fn# " did not conform to " expected-args#))
                         :expected expected-args#
                         :actual (or nonargs-conform-args# (symbol "zero-invocations"))}))))
