(ns specific.core
  (:require [clojure.spec :as spec]
            [specific
             [gene :as gene]
             [matchers :as matchers]
             [test-double :as test-double]]))

(def calls matchers/calls)
(def conforming matchers/conforming)

; FIXME should be able to extract some duplication here

(defn generate [spec & bindings]
  "Generate test data given a spec and optional additional generator overrides"
  (let [new-gens (zipmap (take-nth 2 bindings) 
                         (map (fn [b#] #(spec/gen b#)) (take-nth 2 (next bindings))))]
    (gene/det-sample spec new-gens)))

(defmacro with-gens [bindings & body]
  "Evalutes forms with temporary replacements for generators"
  `(let [new-gens# (zipmap (take-nth 2 ~bindings) 
                           (map (fn [b#] #(spec/gen b#)) (take-nth 2 (next ~bindings))))]
     (binding [specific.gene/*gen-overrides* (merge specific.gene/*gen-overrides* new-gens#)]
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
