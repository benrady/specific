(ns specific.core-spec
  (:require [clojure.java.shell]
            [clojure.test :as ctest] 
            [clojure.spec.test :as stest]
            [clojure.spec.gen :as gen]
            [clojure.spec :as spec]
            [clojure.string :as string]
            [specific.test-double]
            [specific.sample :as sample])
  (:use [clojure.test]
        [specific.report-stub]
        [specific.core]))

(use-fixtures :each report-fixture)

(deftest core
  (testing "with-mocks"
    (testing "does not evaluate the body if the mock function is in error"
      (with-redefs [clojure.test/do-report failure-fn]
        (with-mocks [spit]
          (is false)))
      (is (= "No clojure.spec defined" (:message (last @reports)))))))
