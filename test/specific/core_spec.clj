(ns specific.core-spec
  (:require [clojure.spec.test :as stest]
            [clojure.spec :as spec])
  (:use [clojure.test]
        [specific.core]))

(defn some-fun [greeting & names]
  (apply str greeting ", " names))

(spec/fdef some-fun
           :args (spec/+ string?)
           :ret string?)

(deftest spec-helper

  (testing "lets have some fun"
    (is (= "Hello, World" (some-fun "Hello" "World"))))

  (testing "spy functions"
    (with-spy [some-fun]

      (testing "calls through to the original function"
        (is (= "Hello, World" (some-fun "Hello" "World"))))))

  (testing "stub functions"
    (with-redefs [some-fun (stub-fn)]

      (testing "tracks the arguments of each call"
        (some-fun "hello")
        (is (= [["hello"]] (calls some-fun))))

      (testing "tracks calls specific to each test context"
        (is (= [] (calls some-fun))))

      (testing "return nil by default"
        (is (nil? (some-fun)))))))
