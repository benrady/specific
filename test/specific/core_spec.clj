(ns specific.core-spec
  (:require [clojure.test :as ctest] 
            [clojure.spec.test :as stest]
            [clojure.spec.gen :as gen]
            [clojure.spec :as spec]
            [specific.sample :as sample])
  (:use [clojure.test]
        [specific.core]))

(deftest specific.core

  (testing "lets have some fun"
    ;(is (= "" (spec/gen string?)))
    (is (= "Hello, World" (sample/some-fun "Hello" "World"))))

  (testing "calls"
    (testing "returns a warning string if the object is not a test double"
      (is (= {:msg (str identity " is not a test double")} (calls identity)))))

  (testing "with-stubs"
    (with-stubs [sample/no-spec]

      (testing "don't need a spec"
        (sample/no-spec)
        (is (= [[]] (calls sample/no-spec))))))

  (testing "with-spies"
    (with-spies [sample/some-fun]

      (testing "tracks the arguments of each call"
        (sample/some-fun "hello")
        (is (= [["hello"]] (calls sample/some-fun))))

      (testing "calls through to the original function"
        (is (= "Hello, World" (sample/some-fun "Hello" "World"))))))

  (testing "with-mocks"
    (with-mocks [sample/some-fun]

      (testing "returns a value generated from the spec"
        (is (string? (sample/some-fun ""))))

      (testing "tracks the arguments of each call"
        (sample/some-fun "hello")
        (is (= [["hello"]] (calls sample/some-fun))))

      (testing "reports if the function is missing a spec"
        (let [stub-report (specific.test-double/stub-fn)]
          (with-redefs [ctest/do-report stub-report]
            (with-mocks [sample/no-spec] (sample/no-spec)))
          (is (= [[{:type :fail 
                    :msg "No clojure.spec defined" 
                    :expected "clojure.spec for #'specific.sample/no-spec" 
                    :actual nil}]] 
                 (calls stub-report)))))

      (testing "tracks calls specific to each test context"
        (is (= [] (calls sample/some-fun)))))))
