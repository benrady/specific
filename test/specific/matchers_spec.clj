(ns specific.matchers-spec
  (:require [clojure.spec.alpha :as spec]
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

  (testing "args-conform"
    (let [stub (test-double/stub-fn 'specific.sample/flip-two)]

      (testing "when testing with the args-conform matcher"
        (with-redefs [clojure.test/do-report failure-fn]
          (testing "reports the non args-conform argument"
                   (stub 2 10)
                   (stub 3 11)
                   (is (args-conform stub 2 ::number))
                   (assert-report {:type :fail 
                                   :message "Invocation of stub did not conform to (2 :specific.matchers-spec/number)"
                                   :expected '(2 ::number)
                                   :actual [3 11]}))

          (testing "ensures the function was invoked"
            (is (args-conform stub 20 10))
            (assert-report {:type :fail 
                            :message "Invocation of stub did not conform to (20 10)"
                            :expected '(20 10)
                            :actual 'zero-invocations})
            )))

      (testing "first-args-conform"
        (testing "returns nil if all arguments are args-conform"
          (stub 2 1)
          (is (nil? (first-nonargs-conform stub [2 1]))))

        (testing "returns the first non-args-conform argument"
          (stub 2 1)
          (stub 3 1)
          (is (= [3 1] (first-nonargs-conform stub [2 1])))))

      (testing "not args-conform unless all calls args-conform"
        (stub 2 1)
        (stub 3 1)
        (is (not (args-conform stub 2 1))))

      (testing "args-conform if all calls args-conform"
        (stub 2 1)
        (stub 3 1)
        (is (args-conform stub ::number 1)))

      (testing "not args-conform if no calls"
        (is (not (args-conform stub))))

      (testing "not args-conform if argument count differs"
        (stub 2)
        (is (not (args-conform stub 2 1))))))

  (testing "calls"

    (testing "returns a warning string if the object is not a test double"
      (is (= (str identity " is not a test double") (calls identity))))))
