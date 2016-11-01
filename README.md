# Specific

Generate test doubles using clojure.spec

## Why?

Testing code that has side effects can be painful. Mocking out those interactions is a great way to keep your tests fast and reliable. Using clojure.spec, we can automatically generate those mocks.

## Dependencies

_Specific_ depends on Clojure 1.9 (or 1.8 with the clojure.spec backport) and clojure.test. To add the following to your project.clj file to add it to your project.

```clojure
(defproject sample
  :dependencies [[org.clojure/clojure "1.9.0"]]
  :profiles {:dev {:plugins [[com.benrady/specific "0.0.4-SNAPSHOT"]]}})
```

## Usage

_Specific_ works best with functions that have clojure.spec definitions. You can include these definitions with your code under test, or you can just add them to your tests.

```clojure
(ns sample)

(defn some-fun [greeting & names]
  (spit "fun.txt" (str greeting " " (clojure.string/join ", " names))))

(clojure.spec/fdef some-fun
           :args (clojure.spec/+ string?)
           :ret string?)
```

Assuming we've defined the function above, and its associated spec, there are number of ways to test interactions with it.

### Mock Functions

Mocking a function prevents the original function from being called, which is useful when you want to prevent side effects in a test, but still want to ensure it was invoked properly. Mocked functions validate their arguments against the specs defined for the original function. 

```clojure
  (testing "mock functions"
    (with-mocks [sample/some-fun]

      (testing "returns a value generated from the spec"
        (is (string? (sample/some-fun ""))))

      (testing "tracks the arguments of each call"
        (sample/some-fun "hello")
        (is (= [["hello"]] (calls sample/some-fun))))))
```

Invoking a mock with arguments that don't meet the spec will result in a failure being reported to the test runner. Since `some-fun` requires that we only pass strings as arguments, invoking it will an integer will cause the test to fail.

```clojure
  (testing "reports an error if the arguments do not meet the specs"
    (some-fun 3))

;; FAIL in (specific.core) (test_double.clj:8)
;; mock functions reports an error if the arguments do not meet the specs
;; expected: "Calls to specific.sample/some-fun to conform to (ifn?)"
;;   actual: "Calls to specific.sample/some-fun were (3)"
```

### Stub Functions

Stub functions are more leinent than mocks, not requiring the function to have a spec. This is useful when mocking out interactions with functions you did not write. Stub functions return nil when created with the `with-stubs` macro, but they can also be created manually with `specific.test-double/stub-fn`

```clojure
  (testing "stub functions"

    (testing "can be created manually to return a value"
      (with-redefs [slurp (specific.test-double/stub-fn "Hello Stubs")]
        (is (= "Hello Stubs" (slurp "nofile.txt")))
        (is (= [["nofile.txt"]] (calls slurp)))))

    (testing "with-stubs"
      (with-stubs [spit]

        (testing "doesn't need a spec to track calls"
          (sample/some-fun "hello" "world")
          (is (= [["fun.txt" "hello, world"]] (calls spit)))))))
```

### Spy Functions

Spy functions call through to the original function, but still record the calls and enforce the constraints in the function's spec. You can make assertions about how the spy was invoked after it is called.

```clojure
  (testing "spy functions"
    (with-spies [sample/some-fun]

      (testing "calls through to the original function"
        (sample/some-fun "Hello" "World")
        (is (= [["Hello" "World"]] (calls sample/some-fun)))
        (is (= "Hello World" (slurp "fun.txt"))))))
```

### Matchers

### Generative Testing

_Specify_ works especially well with generative tests.


## License

Copyright (C) 2016 Ben Rady <benrady@gmail.com>

This program is free software; you can redistribute it and/or modify it under the terms of the [GNU General Public License version 2](https://www.gnu.org/licenses/old-licenses/gpl-2.0.en.html) as published by the Free Software Foundation.

This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
