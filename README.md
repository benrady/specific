# Specific

Generate test doubles using clojure.spec

## Why?

Fast-running unit tests (sometimes called [microtests](https://www.industriallogic.com/blog/history-microtests/)) have proved to be far more powerful than comprehensive integration and acceptance tests, but this approach requires a different mindset. Testing code with side effects is especially painful. 

Mocking out interactions is a great way to keep your tests fast and reliable. _Specific_ can generate mock functions from [clojure.spec](http://clojure.org/about/spec) definitions, so it works if you have example-based tests, [property-based](https://github.com/clojure/test.check) tests, or a mixture of the two.

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
  (let [msg (str greeting " " (clojure.string/join ", " names))]
    (spit "fun.txt" msg)
    msg))

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
  (testing "report a failure if the arguments do not meet the specs"
    (some-fun 3))

;; FAIL in (specific.core) (test_double.clj:8)
;; mock functions reports an error if the arguments do not meet the specs
;; expected: "Calls to specific.sample/some-fun to conform to (ifn?)"
;;   actual: "Calls to specific.sample/some-fun were (3)"
```

### Stub Functions

Stub functions are more lenient than mocks, not requiring the function to have a spec. This is useful when mocking out interactions with functions you did not write. Stub functions are generated to always return nil when created with the `with-stubs` macro, but they can also be created manually with `specific.test-double/stub-fn` to return a specific value.

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

Spy functions call through to the original function, but still record the calls and enforce the constraints in the function's spec.

```clojure
  (testing "spy functions"
    (with-spies [sample/some-fun]

      (testing "calls through to the original function"
        (sample/some-fun "Hello" "World")
        (is (= [["Hello" "World"]] (calls sample/some-fun)))
        (is (= "Hello World" (slurp "fun.txt"))))))
```

### Conforming Matcher

In the previous examples, you saw how to use use `calls` to get list of arguments for all the invocations of a mock, stub, or spy. While easy to use and extensible, this approach will not work reliably with random values generated from specs. For this, you can use the `conforming` matcher like so:

```clojure
  (testing "conforming"
    (with-stubs [sample/flip-two]
      (spec/def ::number number?)

      (testing "when called with exact value"
        (sample/flip-two 1 2) 
        (is (conforming sample/flip-two 1 2)))

      (testing "when called with a spec to validate the argument"
        (sample/flip-two 1 42) 
        (is (conforming sample/flip-two 1 ::number)))))
```

`conforming` works with mocks, stubs, and spies. You can use any spec that you want to verify the arguments: Either ones declared in the test or specs in another namespace, like the ones that are used in the code under test.

### Temporary Generators

Sometimes, within the scope of a test (or a group of tests) it makes sense to change the generator for a spec. Maybe you want to test a specific range of values, or just have a function return one value. To do that with _Specific_ you can use the `with-gens` macro:

```clojure
  (testing "with-gens"
    (with-mocks [sample/some-fun]

      (testing "can temporarily replace the generator for a spec"
        (with-gens [::sample/fun-greeting #{"hello!"}]
          (is (= "hello!" (sample/some-fun "hello")))))

      (testing "can also use an existing spec's generator"
        (with-gens [::sample/fun-greeting ::sample/number]
          (is (number? (sample/some-fun "hello")))))))
```
Since with-gens redefines the generator for a spec, and not an entire function, you can use to specify a portion of an otherwise default generated return value (a single nested `:phone-number` value in an entity map, for example).

## License

Copyright (C) 2016 Ben Rady <benrady@gmail.com>

This program is free software; you can redistribute it and/or modify it under the terms of the [GNU General Public License version 2](https://www.gnu.org/licenses/old-licenses/gpl-2.0.en.html) as published by the Free Software Foundation.

This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
