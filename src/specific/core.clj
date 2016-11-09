(ns specific.core
  (:require [clojure.spec :as spec]
            [specific.matchers :as matchers]
            [specific.test-double :as test-double]))

(def calls matchers/calls)
(def conforming matchers/conforming)

(defmacro with-gens [bindings & body]
  `(let [new-gens# (zipmap (map #(spec/spec %) (take-nth 2 ~bindings)) 
                           (map #(spec/gen %) (take-nth 2 (next ~bindings))))]
     (binding [specific.test-double/*temporary-gens* (merge specific.test-double/*temporary-gens* new-gens#)]
       (do ~@body))))

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
