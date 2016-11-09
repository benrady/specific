(ns specific.report-stub
  (:require [clojure.test :as ctest]))

(def reports (atom []))

(defn assert-failure [& args]
  (ctest/is (= args (last @reports))))

(defn report-fixture [f] 
  (reset! reports []) (f))

(defn failure-fn [& args] 
  (swap! reports conj args))
