(ns specific.test-double-spec
  (:require [clojure.spec :as spec]
            [specific
             [matchers :as matchers]
             [sample :as sample]])
  (:use [clojure.test]
        [specific.report-stub]
        [specific.test-double]))

(defn no-spec [])

(defn no-ret [a])

(spec/fdef no-ret
           :args (spec/tuple identity))

(use-fixtures :each report-fixture)
(deftest test-doubles

  (testing "stub functions"
    (testing "can be created manually to return a value"
      (with-redefs [slurp (stub-fn "Hello Stubs")]
        (is (= "Hello Stubs" (slurp "nofile.txt"))))))

  (testing "mock functions"
    (let [mock (mock-fn #'specific.sample/some-fun)]

      ; Can handle functions with partial specs

      (testing "when invocations do not match the spec"
        (with-redefs [clojure.test/do-report failure-fn]
          (mock 3)

          (testing "reports the name of the file"
            (is (number? (:line (last @reports)))))

          (testing "reports the line number of the file"
            (is (number? (:line (last @reports)))))

          (testing "reports the actual value used"
            (is (= 3 (:actual (last @reports)))))

          (testing "reports the violated predicate as the expected value"
            (is (= 'string? (:expected (last @reports)))))

          (testing "reports a failure message"
            (is (= "val: 3 fails spec: :specific.sample/fun-greeting at: [:args :greeting] predicate: string?\n"
                   (:message (last @reports)))))))

      (testing "returns a value that matches the spec"
        (is (string? (mock ""))))

      (testing "returns a test report if the function spec is missing a return value"
        (is (= {:type :fail
                :message "No :ret spec defined" 
                :expected "clojure.spec at [:ret] for #'specific.test-double-spec/no-ret" 
                :actual nil} 
               (mock-fn #'no-ret))))

      (testing "returns a test report if the function is missing a spec"
        (is (= {:type :fail 
                :message "No clojure.spec defined" 
                :expected "clojure.spec for #'specific.test-double-spec/no-spec" 
                :actual nil}
               (mock-fn #'no-spec))))

      (testing "tracks calls specific to each test context"
        (is (= [] (matchers/calls mock)))))))
