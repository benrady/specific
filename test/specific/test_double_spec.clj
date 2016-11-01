(ns specific.test-double-spec
  (:require [specific.matchers :as matchers]
            [specific.sample :as sample])

  (:use [clojure.test]
        [specific.test-double]))

(def reports (atom []))

(defn- assert-failure [& args]
  (is (= args (last @reports))))

(use-fixtures :each (fn [f] (reset! reports []) (f)))

(testing "test doubles"
  (with-redefs [report-failure (fn [& args] (swap! reports conj args))]

    (testing "mock functions"
      (let [mock (mock-fn #'specific.sample/some-fun)]

        (testing "reports when invocations do not match the spec"
          (mock 3)
          (assert-failure "val: (3) fails spec: specific.sample/some-fun predicate: ifn?\n"
                          "Calls to specific.sample/some-fun to conform to (ifn?)"
                          "Calls to specific.sample/some-fun were (3)"))

        (testing "returns a value that matches the spec"
          (is (string? (mock ""))))

        (testing "reports if the function is missing a spec"
          (mock-fn #'specific.sample/no-spec)
          (assert-failure "No clojure.spec defined" "clojure.spec for #'specific.sample/no-spec" nil))

        (testing "tracks calls specific to each test context"
          (is (= [] (matchers/calls mock))))))))
