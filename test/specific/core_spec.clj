(ns specific.core-spec
  (:require [clojure.test :as ctest] 
            [clojure.spec.test :as stest]
            [clojure.spec.gen :as gen]
            [clojure.spec :as spec]
            [specific.sample :as sample])
  (:use [clojure.test]
        [specific.core]))

(use-fixtures :each (fn [f] (f) (clojure.java.io/delete-file "fun.txt")))

(deftest specific.core

  (testing "mock functions"
    (with-mocks [sample/some-fun]

      (testing "returns a value generated from the spec"
        (is (string? (sample/some-fun ""))))

      (testing "tracks the arguments of each call"
        (sample/some-fun "hello")
        (is (= [["hello"]] (calls sample/some-fun))))))


  (testing "stub functions"
    (with-stubs [spit]

      (testing "doesn't need a spec to track calls"
        (sample/some-fun "hello" "world")
        (is (= [["fun.txt", "hello, world"]] (calls spit))))))

  (testing "with-spies"
    (with-spies [sample/some-fun]

      (testing "tracks the arguments of each call"
        (sample/some-fun "hello")
        (is (= [["hello"]] (calls sample/some-fun))))

      (testing "calls through to the original function"
        (sample/some-fun "Hello" "World")
        (is (= "Hello, World" (slurp "fun.txt")))))))
