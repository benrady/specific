(ns specific.core
  (:require [specific.matchers :as matchers]
            [specific.test-double :as test-double]))

(def calls matchers/calls)
(def conforming matchers/conforming)

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
