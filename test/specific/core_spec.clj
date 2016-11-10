(ns specific.core-spec
  (:require [clojure.test :as ctest] 
            [clojure.spec.test :as stest]
            [clojure.spec.gen :as gen]
            [clojure.spec :as spec]
            [specific.test-double]
            [specific.sample :as sample])
  (:use [clojure.test]
        [specific.core]))

(deftest specific.core

  (testing "gens"
    (let [g (spec/gen ::sample/cow-result 
                      {::sample/exit #(spec/gen #{1})
                       ::sample/out #(spec/gen #{"out"})
                       ::sample/err #(spec/gen #{"err"})})]
      (is (= [1] (map :exit (gen/sample g 1))))))

  (testing "with-gens"
    (with-mocks [sample/cowsay 
                 sample/some-fun]

      (testing "can temporarily replace the generator for a spec"
        (with-gens [::sample/fun-greeting #{"hello!"}]
          (is (= "hello!" (sample/some-fun "hello")))))

      (testing "can replace the generator for a nested value"
        (with-gens [::sample/exit #{0}]
          (is (= 0 (:exit (sample/cowsay "hello"))))))

      (testing "can also use an existing spec's generator"
        (with-gens [::sample/fun-greeting ::sample/out]
          (is (string? (sample/some-fun "hello")))))))

  (testing "conforming"
    (with-stubs [sample/flip-two]
      (spec/def ::number number?)

      (testing "when called with exact value"
        (sample/flip-two 1 2) 
        (is (conforming sample/flip-two 1 2)))

      (testing "when called with a spec to validate the argument"
        (sample/flip-two 1 42) 
        (is (conforming sample/flip-two 1 ::number)))))

  (testing "mock functions"
    (with-mocks [sample/some-fun]

      (testing "returns a value generated from the spec"
        (spec/valid? ::sample/fun-greeting (sample/some-fun "hello")))

      (testing "validates against the spec of the original function"
        (sample/some-fun "hello")
        (is (= ["hello"] (first (calls sample/some-fun))))
        ; (sample/some-fun 1)
        ;
        ;   expected: "Calls to specific.sample/some-fun to conform to (ifn?)"
        ;   actual: "Calls to specific.sample/some-fun were (1)"
        )

      (testing "can validate individual calls"
        (sample/some-fun "hello")
        (sample/some-fun "world")
        (is (= [["hello"] ["world"]] (calls sample/some-fun))))))


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
    (with-spies [sample/flip-two]

      (testing "calls through to the original function"
        (is (= ["World" "Hello"] (sample/flip-two "Hello" "World")))
        (is (= [["Hello" "World"]] (calls sample/flip-two)))))))
