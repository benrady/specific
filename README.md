# Specific

Generate test doubles using clojure.spec

## Why?

Testing code with side effects, such as I/O, can be painful. It slows down your tests and can cause spurious failures. Mocking out these interactions is a great way to keep your tests fast and reliable.

_Specific_ can generate mock functions from [clojure.spec](http://clojure.org/about/spec) definitions. It can help you make assertions about how the functions were called, or simply remove the side effect and let your spec declarations do the verification. This means it works on programs with example-based tests, [property-based](https://github.com/clojure/test.check) generative tests, or a mixture of the two.

## Dependencies

_Specific_ depends on Clojure 1.9 (or 1.8 with the [clojure.spec backport](https://github.com/tonsky/clojure-future-spec)). Add the following to your project.clj:

```clojure
(defproject sample
  :profiles {:dev {:plugins [[com.benrady/specific "0.1.0"]]}})
```

## Usage

To show you how to use _Specific_ let's assume you have three functions you'd like to test. One of them, `cowsay`, executes a shell command which might not be available in all environments.

```clojure
(ns sample)

(defn greet [pre sufs]
  (str pre ", " (string/join ", " sufs)))

(defn cowsay [msg]
  (shell/sh "cowsay" msg))

(defn some-fun [greeting & names]
  (:out (cowsay (greet greeting names))))
```

_Specific_ works best with functions that have clojure.spec [definitions](http://clojure.org/guides/spec#_spec_ing_functions). You can include these definitions with the code under test, or you can add them in the tests themselves, or both.

```clojure
(clojure.spec/def ::exit (clojure.spec/and integer? #(>= % 0) #(< % 256)))
(clojure.spec/def ::out string?)
(clojure.spec/def ::fun-greeting string?)
(clojure.spec/fdef greet :ret ::fun-greeting)
(clojure.spec/fdef cowsay
                   :args (clojure.spec/tuple ::fun-greeting)
                   :ret (clojure.spec/keys :req-un [::out ::exit]))
(clojure.spec/fdef some-fun
                   :args (clojure.spec/+ string?)
                   :ret string?)
```

### Mock Functions

Mocking a function prevents the original function from being called, which is useful when you want to prevent side effects in a test, but still want to ensure it was invoked properly. Mocked functions validate their arguments against the specs defined for the original function. 

```clojure
(testing "mock functions"
  (with-mocks [sample/cowsay]

    (testing "return a value generated from the spec"
      (spec/valid? string? (:out (sample/cowsay "hello"))))

    (testing "validate against the spec of the original function"
      (sample/cowsay "hello")
      (spec/exercise-fn `sample/cowsay))

      ; (sample/cowsay 1)
      ; val: 1 fails spec: :specific.sample/fun-greeting at: [:args 0] predicate: string?
      ;
      ; expected: string?
      ;   actual: 1
      

    (testing "record the individual calls"
      (sample/cowsay "hello")
      (sample/cowsay "world")
      (is (= [["hello"] ["world"]] (calls sample/cowsay))))))
```

### Conforming Matcher

You can use `calls` to get list of arguments for all the invocations of any _Specific_ test double. While easy to understand and extensible, this approach will not work reliably with random values generated from specs. For this, you can use the `conforming` matcher like so:

```clojure
(testing "conforming matcher"
  (spec/def ::nice-greeting (spec/+ string?))
  (with-mocks [sample/cowsay sample/greet]

    (testing "when called with exact value"
      (sample/greet "hello" ["world"]) 
      (is (conforming sample/greet "hello" ["world"])))

    (testing "when called with a spec to validate the argument"
      (sample/greet "hello" ["world"]) ; Replace with a generative example
      (is (conforming sample/greet "hello" ::nice-greeting)))))
```

The conforming matcher works with mocks, stubs, and spies. You can use any spec that you want to verify the arguments: Either ones declared in the test or specs in another namespace, like the ones that are used in the code under test.

The conforming matcher is also handy when you need to verify invocations that include (or are derived from) generated data returned from a mock or stub.

### Stub Functions

Stub functions are more lenient than mocks, not requiring the function to have a spec. This is useful when mocking out interactions with functions you did not write. Stub functions always return nil.

```clojure
  (testing "stub functions"
    (with-stubs [spit]

      (testing "doesn't need a spec to track calls"
        (sample/some-fun "hello" "world")
        (is (= [["fun.txt" "hello, world"]] (calls spit))))))
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
In practice, spies in _Specific_ work a lot like [clojure.spec/instrument](https://clojure.github.io/clojure/branch-master/clojure.spec-api.html#clojure.spec.test/instrument), expect that they are scoped only to particular forms rather than being a global mutation of the function.

### Generator Overrides

Sometimes, within the scope of a test (or a group of tests) it makes sense to override the generator for a spec. Maybe you want to test a specific range of values, or just have a function return one value. To do that with _Specific_ you can use the `with-gens` macro:

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
