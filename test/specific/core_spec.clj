(ns specific.core-spec
  (:require [clojure.test :as ctest] 
            [clojure.spec.test :as stest]
            [clojure.spec.gen :as gen]
            [clojure.spec :as spec]
            [specific.test-double]
            [specific.sample :as sample])
  (:use [clojure.test]
        [specific.core]))

(use-fixtures :each (fn [f] (f) 
                      (let [file (clojure.java.io/as-file "fun.txt")]
                        (when (.exists file)
                          (clojure.java.io/delete-file "fun.txt")))))

(deftest specific.core

  (testing "conforming"
    (with-stubs [sample/flip-two]

      (testing "called with exact value"
        (sample/flip-two 1 2) 
        (is (conforming sample/flip-two 1 2)))

      (testing "called with a spec to validate the argument"
        (sample/flip-two 1 42) 
        (is (conforming sample/flip-two 1 ::sample/number)))))

  (testing "mock functions"
    (with-mocks [sample/some-fun]

      (testing "returns a value generated from the spec"
        (is (string? (sample/some-fun ""))))

      (testing "tracks the arguments of each call"
        (sample/some-fun "hello")
        (is (= [["hello"]] (calls sample/some-fun))))))


  (testing "stub functions"

    (testing "can be created manually to return a value"
      (with-redefs [slurp (specific.test-double/stub-fn "Hello Stubs")]
        (is (= "Hello Stubs" (slurp "nofile.txt")))
        (is (= [["nofile.txt"]] (calls slurp)))))

    (testing "with-stubs"
      (with-stubs [spit]

        (testing "doesn't need a spec to track calls"
          (sample/some-fun "hello" "world")
          (is (= [["fun.txt" "hello world"]] (calls spit)))))))

  (testing "spy functions"
    (with-spies [sample/some-fun]

      (testing "calls through to the original function"
        (sample/some-fun "Hello" "World")
        (is (= [["Hello" "World"]] (calls sample/some-fun)))
        (is (= "Hello World" (slurp "fun.txt")))))))
