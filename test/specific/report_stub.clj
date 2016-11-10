(ns specific.report-stub
  (:require [clojure.test :as ctest]))

(def reports (atom []))

(defn assert-failure [report]
  (ctest/is (= report (last @reports))))

(defn report-fixture [f] 
  (reset! reports []) (f))

(defn failure-fn [report] 
  (swap! reports conj report))
