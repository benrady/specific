(ns specific.core
  (:require [clojure.spec.test :as stest]
            [clojure.spec :as spec])
  (:use [clojure.test]))

(defn- add-call [calls f & args]
  (swap! calls update-in [*testing-contexts*] conj args)
  (apply f args))

(defn spy-fn [f]
  (let [fspec (spec/get-spec f) 
        calls (atom {})]
    (with-meta (partial add-call calls f) {:specific-calls calls})))

(defn stub-fn []
  (spy-fn (constantly nil)))

(defn calls [spyf]
  (get (deref (:specific-calls (meta spyf))) *testing-contexts* []))

(defmacro with-spy [vs & body]
  `(with-redefs ~(->> (mapcat (fn [v]
                                [v `(spy-fn ~v)]) vs)
                      (vec))
     (do ~@body)))

