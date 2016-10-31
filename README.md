# Specific

Generate test doubles using clojure.spec

## Why?

Testing code that has side effects can be painful. Mocking out those interactions is a great way to keep your tests fast and reliable. Using clojure.spec, we can automatically generate those mocks.

## Dependencies

_Specific_ depends on Clojure 1.9 (or 1.8 with the clojure.spec backport) and clojure.test. To add the following to your project.clj file to add it to your project.

```clojure
(defproject sample
  :dependencies [[org.clojure/clojure "1.9.0"]]
  :profiles {:dev {:plugins [[com.benrady/specific "0.0.1-SNAPSHOT"]]}})
```

## Usage

_Specific_ works best with functions that have clojure.spec definitions. You can include these definitions with your code under test, or you can just add them to your tests.

```clojure
(defn some-fun [greeting & names]
  (spit "fun.txt" (apply str greeting ", " names)))

(spec/fdef some-fun
           :args (spec/+ string?)
           :ret string?)
```

Assuming we've defined the function above, and its associated spec, there are number of ways to test interactions with it.

### Mock Functions

Mocking a function prevents the original function from being called, which is useful when you want to prevent side effects in a test, but still want to ensure it was invoked properly. Mocked functions validate their arguments against the specs defined for the original function. 

```clojure
  (testing "mock functions"
    (with-mocks [some-fun]

      (testing "reports an error if the arguments do not meet the specs "
        (some-fun 3))
        ;; expected:
        ;; actual:

      (testing "tracks the arguments of each call"
        (some-fun "hello")
        (is (= [["hello"]] (calls some-fun))))))
```
### Stub Functions

Stub functions are more leinent than mocks, not requiring the function to have a spec. This is useful when mocking out interactions with functions you did not write.

```clojure
  (testing "stub functions"
    (with-stubs [spit]

      (testing "do not require specs"
        (some-fun "test")
        (assert-args spit ["flubber.txt" "test"]))))
```

### Spy Functions

Spy functions call through to the original function, but still record the calls and enforce the constraints in the function's spec. You can make assertions about how the spy was invoked after it is called.

```clojure
  (testing "spy functions"
    (with-spies [some-fun]

      (testing "calls the original function"
        (some-fun "side effects!")
        (is (= "side effects!" (slurp "fun.txt)))
        (assert-args spit ["flubber.txt" "test"]))))
```

### Generative Testing

_Specify_ works especially well with generative tests.


## License

Copyright (C) 2016 Ben Rady <benrady@gmail.com>

This program is free software; you can redistribute it and/or modify it under the terms of the [GNU General Public License version 2](https://www.gnu.org/licenses/old-licenses/gpl-2.0.en.html) as published by the Free Software Foundation.

This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
