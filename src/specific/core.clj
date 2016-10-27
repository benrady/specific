(ns specific.core
  (:require [clojure.spec.test :as stest]
            [clojure.spec :as spec])
  (:use [clojure.test]))

(defn- add-call [calls & args]
  (swap! calls update-in [*testing-contexts*] conj args)
  args)

(defn spy-fn [f]
  (let [fspec (spec/get-spec f) 
        calls (atom {})]
    (with-meta (comp (partial apply f) (partial add-call calls)) {:specific-calls calls})))

(defn mock-fn [f]
  (let [fspec (spec/get-spec f) 
        calls (atom {})]
    (with-meta (partial add-call calls f) {:specific-calls calls})))

(defn stub-fn []
  (spy-fn (constantly nil)))

(defn calls [spyf]
  (get (deref (:specific-calls (meta spyf))) *testing-contexts* []))

(defmacro with-spy [vs & body]
  `(with-redefs ~(vec (mapcat (fn [v] [v `(spy-fn ~v)]) vs))
     ;`(doall (map stest/instrument) `vs)
     (do ~@body)))

(defmacro with-stubs [vs & body]
  `(with-redefs ~(vec (mapcat (fn [v] [v `(stub-fn)]) vs))
     ;`(doall (map stest/instrument) `vs)
     (do ~@body)))

(defmacro with-mocks [vs & body]
  `(with-redefs ~(vec (mapcat (fn [v] [v `(mock-fn ~v)]) vs))
     ;`(doall (map stest/instrument) `vs)
     (do ~@body)))

