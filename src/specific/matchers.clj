(ns specific.matchers
  (:require [clojure.test :as ctest]))

(defn calls [spyf]
  "Get the recorded calls from a test double"
  (if-let [calls (:specific-calls (meta spyf))]
    (get (deref calls) ctest/*testing-contexts* [])
    {:msg (str spyf " is not a test double")}))
