(ns specific.core
  (:require [clojure.test :as ctest]
            [specific.test-double :as test-double]))

(defn calls [spyf]
  "Get the recorded calls from a test double"
  ; Check if spyf isn't a double
  (if-let [calls (:specific-calls (meta spyf))]
    (get (deref calls) ctest/*testing-contexts* [])
    {:msg (str spyf " is not a test double")}))

(defmacro with-spies [vs & body]
  "Temporarily redefines vars with functions that spy on arguments when invoked."
  `(with-redefs ~(vec (mapcat (fn [v] [v `(specific.test-double/spy-fn ~v)]) vs))
     (do ~@body)))

(defmacro with-mocks [vs & body]
  "Temporarily redefines vars with functions that validate against clojure.spec."
  `(with-redefs ~(vec (mapcat (fn [v] [v `(specific.test-double/mock-fn (var ~v))]) vs))
     (do ~@body)))

(defmacro with-stubs [vs & body]
  "Temporarily redefines vars with functions that records arguments and return nil"
  `(with-redefs ~(vec (mapcat (fn [v] [v `(specific.test-double/stub-fn)]) vs))
     (do ~@body)))
