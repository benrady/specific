(ns specific.core-spec
  (:require [clojure.java.shell]
            [clojure.test :as ctest] 
            [clojure.spec.test :as stest]
            [clojure.spec.gen :as gen]
            [clojure.spec :as spec]
            [specific.test-double]
            [specific.sample :as sample])
  (:use [clojure.test]
        [specific.core]))

(deftest specific.core

  (testing "mock functions"
    (with-mocks [sample/some-fun]

      (testing "return a value generated from the spec"
        (spec/valid? string? (sample/some-fun "hello")))

      (testing "validates against the spec of the original function"
        (sample/some-fun "hello")
        (is (= ["hello"] (first (calls sample/some-fun)))))

      (testing "records the individual calls"
        (sample/some-fun "hello")
        (sample/some-fun "world")
        (is (= [["hello"] ["world"]] (calls sample/some-fun))))))


  (testing "conforming matcher"
    (spec/def ::nice-greeting (spec/+ string?))
    (with-mocks [sample/cowsay sample/greet]

      (testing "when called with exact value"
        (sample/greet "hello" ["world"]) 
        (is (conforming sample/greet "hello" ["world"])))

      (testing "when called with a spec to validate the argument"
        (sample/greet "hello" ["world"]) 
        (is (conforming sample/greet "hello" ::nice-greeting)))))

  (testing "stub functions"

    (testing "with-stubs"
      (with-stubs [clojure.java.shell/sh]

        (testing "doesn't need a spec to track calls"
          (sample/some-fun "hello" "world")
          (is (= [["cowsay" "hello, world"]] (calls clojure.java.shell/sh)))))))

  (testing "spy functions"
    (with-spies [sample/greet]

      (testing "calls through to the original function"
        (is (= "Hello, World!" (sample/greet "Hello" ["World!"])))
        (is (= [["Hello" ["World!"]]] (calls sample/greet))))))

  (testing "generator overrides"
    (with-mocks [sample/cowsay sample/greet]

      (testing "can temporarily replace the generator for a spec using a predicate"
        (with-gens [::sample/fun-greeting #{"hello!"}]
          (is (= "hello!" (sample/greet "hello" [])))))

      (testing "can replace the generator for a nested value"
        (with-gens [::sample/exit #{0}]
          (is (= 0 (:exit (sample/cowsay "hello"))))))

      (testing "can use another spec's generator"
        (with-gens [::sample/out ::sample/fun-greeting]
          (is (string? (sample/some-fun "hello"))))))))
