(ns specific.matchers-spec
  (:require [clojure.spec :as spec]
            [specific
             [report-stub :as report-stub]
             [test-double :as test-double]
             [sample :as sample]])
  (:use [clojure.test]
        [specific.report-stub]
        [specific.matchers]))

(spec/def ::number number?)

(use-fixtures :each report-fixture)
(deftest matchers

  (testing "conforming"
    (let [stub (test-double/stub-fn 'specific.sample/flip-two)]

      (testing "when testing with the conforming matcher"
        (with-redefs [clojure.test/do-report failure-fn]
          (testing "reports the non conforming argument"
                   (stub 2 10)
                   (stub 3 11)
                   (is (conforming stub 2 ::number))
                   (assert-report {:type :fail 
                                   :message "Invocation of stub did not conform to (2 :specific.matchers-spec/number)"
                                   :expected '(2 ::number)
                                   :actual [3 11]}))

          (testing "ensures the function was invoked"
            (is (conforming stub 20 10))
            (assert-report {:type :fail 
                            :message "Invocation of stub did not conform to (20 10)"
                            :expected '(20 10)
                            :actual "No Calls"})
            )))

      (testing "first-conforming"
        (testing "returns nil if all arguments are conforming"
          (stub 2 1)
          (is (nil? (first-nonconforming stub [2 1]))))

        (testing "returns the first non-conforming argument"
          (stub 2 1)
          (stub 3 1)
          (is (= [3 1] (first-nonconforming stub [2 1])))))

      (testing "not conforming unless all calls conforming"
        (stub 2 1)
        (stub 3 1)
        (is (not (conforming stub 2 1))))

      (testing "conforming if all calls conforming"
        (stub 2 1)
        (stub 3 1)
        (is (conforming stub ::number 1)))

      (testing "not conforming if no calls"
        (is (not (conforming stub))))

      (testing "not conforming if argument count differs"
        (stub 2)
        (is (not (conforming stub 2 1))))))

  (testing "calls"

    (testing "returns a warning string if the object is not a test double"
      (is (= {:msg (str identity " is not a test double")} (calls identity))))))
