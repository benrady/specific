(ns specific.readme-spec
  (:require [clojure.java.shell]
            [clojure.test :as ctest] 
            [clojure.spec.test.alpha :as stest]
            [clojure.spec.gen.alpha :as gen]
            [clojure.spec.alpha :as spec]
            [clojure.string :as string]
            [specific.test-double]
            [specific.sample :as sample])
  (:use [clojure.test]
        [specific.core]))

(deftest specific.core

  (testing "mock functions"
    (with-mocks [sample/cowsay]

      (testing "return a value generated from the spec"
        (is (<= 0 (:exit (sample/cowsay "hello"))))
        (is string? (:out (sample/cowsay "hello"))))

      (testing "validate against the spec of the original function"
        (sample/cowsay "hello"))

        ; (sample/cowsay 1)
        ; val: 1 fails spec: :specific.sample/fun-greeting at: [:args :fun-greeting] predicate: string?
        ;
        ; expected: string?
        ;   actual: 1

      (testing "record the individual calls"
        (sample/cowsay "hello")
        (sample/cowsay "world")
        (is (= [["hello"] ["world"]] (calls sample/cowsay))))))

  (testing "args-conform matcher"
    (spec/def ::h-word #(string/starts-with? % "h"))
    (with-mocks [sample/cowsay]

      (testing "matches with exact values"
        (sample/some-fun "hello" "world") 
        (is (args-conform sample/cowsay "hello, world")))

      (testing "can use a custom spec to validate an argument"
        (sample/some-fun "hello" "world")
        (sample/some-fun "hello" "larry")
        (is (args-conform sample/cowsay ::h-word)))

      (testing "can ensure all invocations are conforming"
        (doall ; Ironically, exercise is lazy
          (spec/exercise-fn `sample/some-fun))
        (is (args-conform sample/cowsay ::sample/fun-greeting)))))

  (testing "stub functions"
    (with-stubs [clojure.java.shell/sh]

      (testing "return nil"
        (is (nil? (sample/some-fun "hello" "world"))))

      (testing "don't need a spec"
        (sample/some-fun "hello" "world")
        (is (args-conform clojure.java.shell/sh "cowsay" ::sample/fun-greeting)))))

  (testing "spy functions"
    (with-spies [sample/greet]

      (testing "calls through to the original function"
        (is (= "Hello, World!" (sample/greet "Hello" ["World!"])))
        (is (= [["Hello" ["World!"]]] (calls sample/greet))))))

  (testing "generating test data"
    (spec/def ::word (spec/and string? #(re-matches #"\w+" %)))
    (spec/def ::short-string (spec/and ::word #(> (count %) 2) #(< (count %) 5)))

    (testing "Returns a constant, conforming value for a given spec"
      (is (= "5U6" (generate ::short-string)))
      (is (spec/valid? ::short-string (generate ::short-string))))

    (testing "can override specs"
      (is (= "word" (generate ::short-string ::word #{"word"}))))

    (testing "uses with-gens overrides too"
      (with-gens [::word #{"word"}]
        (is (= "word" (generate ::short-string))))))

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
