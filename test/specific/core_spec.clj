(ns specific.core-spec
  (:require [clojure.spec.test :as stest]
            [clojure.spec.gen :as gen]
            [clojure.spec :as spec])
  (:use [clojure.test]
        [specific.core]))

(defn some-fun [greeting & names]
  (apply str greeting ", " names))

(spec/fdef some-fun
           :args (spec/+ string?)
           :ret string?)

(deftest specific.core

  (testing "lets have some fun"
    (prn (spec/get-spec `some-fun))
    ;(is (= "" (spec/gen string?)))
    (is (= "Hello, World" (some-fun "Hello" "World"))))

  (testing "spy functions"
    (with-spy [some-fun]

      (testing "tracks the arguments of each call"
        (some-fun "hello")
        (is (= [["hello"]] (calls some-fun))))

      (testing "calls through to the original function"
        (is (= "Hello, World" (some-fun "Hello" "World"))))))

  (testing "stub functions"
    (with-stubs [some-fun]

      (testing "returns a consistent value generated from the spec"
        (is (= nil (some-fun))))

      (testing "tracks the arguments of each call"
        (some-fun "hello")
        (is (= [["hello"]] (calls some-fun))))

      (testing "tracks calls specific to each test context"
        (is (= [] (calls some-fun)))))))
