(ns specific.test-double-spec
  (:require [specific.sample :as sample])

  (:use [clojure.test]
        [specific.test-double]))

(def reports (atom []))

(defn- assert-failure [& args]
  (is (= args (last @reports))))

(use-fixtures :each (fn [] (reset! reports [])))

(testing "test doubles"
  (with-redefs [report-failure (fn [& args] (swap! reports conj args))]

    (testing "mock functions"

      (testing "reports when invocations do not match the specification"
        ((mock-fn 'specific.sample/some-fun) 3)
        (assert-failure "val: (3) fails spec: specific.sample/some-fun predicate: ifn?\n"
                        "Calls to specific.sample/some-fun to conform to (ifn?)"
                        "Calls to specific.sample/some-fun were (3)")))))
