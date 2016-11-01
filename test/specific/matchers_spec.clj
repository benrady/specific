(ns specific.matchers-spec
  (:use [clojure.test]
        [specific.matchers]))

(deftest matchers

  (testing "calls"

    (testing "returns a warning string if the object is not a test double"
      (is (= {:msg (str identity " is not a test double")} (calls identity))))))
