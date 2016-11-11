(ns specific.test-double-spec
  (:require [specific
             [matchers :as matchers]
             [sample :as sample]])
  (:use [clojure.test]
        [specific.report-stub]
        [specific.test-double]))

(defn no-spec [])

(use-fixtures :each report-fixture)
(deftest test-doubles
  (with-redefs [report-fail failure-fn]

    (testing "stub functions"
      (testing "can be created manually to return a value"
        (with-redefs [slurp (stub-fn "Hello Stubs")]
          (is (= "Hello Stubs" (slurp "nofile.txt"))))))

    (testing "mock functions"
      (let [mock (mock-fn #'specific.sample/some-fun)]

        ; Can handle functions with partial specs

        (testing "when invocations do not match the spec"
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
                   (:message (last @reports))))))

        (testing "returns a value that matches the spec"
          (is (string? (mock ""))))

        (testing "reports if the function is missing a spec"
          (mock-fn #'no-spec)
          (assert-failure {:message "No clojure.spec defined" 
                           :expected "clojure.spec for #'specific.test-double-spec/no-spec" 
                           :actual nil}))

        (testing "tracks calls specific to each test context"
          (is (= [] (matchers/calls mock))))))))
