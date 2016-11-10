(ns specific.matchers-spec
  (:require [specific
             [report-stub :as report-stub]
             [test-double :as test-double]
             [sample :as sample]])
  (:use [clojure.test]
        [specific.report-stub]
        [specific.matchers]))

(deftest matchers

  (testing "conforming"
    (let [stub (test-double/stub-fn 'specific.sample/flip-two)]

      (testing "not conforming unless all calls conforming"
        (stub 2 1)
        (stub 3 1)
        (is (not (conforming stub 2 1))))

      (testing "conforming if all calls conforming"
        (stub 2 1)
        (stub 3 1)
        (is (conforming stub ::sample/number 1)))

      (testing "not conforming if no calls"
        (is (not (conforming stub))))

      (testing "not conforming if argument count differs"
        (stub 2)
        (is (not (conforming stub 2 1))))))

  (testing "calls"

    (testing "returns a warning string if the object is not a test double"
      (is (= {:msg (str identity " is not a test double")} (calls identity))))))
