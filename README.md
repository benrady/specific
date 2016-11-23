# Specific

Generate mocks and other test doubles using clojure.spec

## Why?

Testing code with side effects, such as I/O, can be painful. It slows down your tests and can cause spurious failures. Mocking out these interactions is a great way to keep your tests fast and reliable.

_Specific_ can generate mock functions from [clojure.spec](http://clojure.org/about/spec) definitions. It can help you make assertions about how the functions were called, or simply remove the side effect and let your spec declarations do the verification. This means it works on programs with example-based tests, [property-based](https://github.com/clojure/test.check) generative tests, or a mixture of the two.

## Dependencies

_Specific_ depends on Clojure 1.9 (or 1.8 with the [clojure.spec backport](https://github.com/tonsky/clojure-future-spec)). Add the following to your project.clj:

```clojure
(defproject sample
  :profiles {:dev {:plugins [[com.benrady/specific "0.3.0"]]}})
```

## Usage

To show you how to use _Specific_, let's assume you have three interdependent functions you'd like to test. One of them, `cowsay`, executes a shell command which might not be available in all environments.

```clojure
(ns sample
  (:require [clojure.java.shell :as shell]
            [clojure.string :as string]))

(defn greet [pre sufs]
  (string/join ", " (cons pre sufs)))

(defn cowsay [msg]
  (shell/sh "cowsay" msg)) ; Fails in some environments

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

Mocking a function prevents the original function from being called, which is useful when you want to prevent side effects in a test, but still want to ensure it was invoked properly. Mocked functions validate their arguments against the specs defined for the original function, and return data generated from the spec. 

You can replace a list of functions with mock functions using the `specific.core/with-mocks` macro, like so:

```clojure
(testing "mock functions"
  (with-mocks [sample/cowsay]

    (testing "return a value generated from the spec"
      (is (<= 0 (:exit (sample/cowsay "hello"))))
      (is string? (:out (sample/cowsay "hello"))))

    (testing "validate against the spec of the original function"
      (sample/cowsay "hello"))

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

You can use `specific.core/calls` to get list of arguments for all the invocations of any _Specific_ mock function. While easy to understand and extensible, this approach will not work reliably with random values generated from specs. For this, you can use `specific.core/conforming` like so:

```clojure
(testing "conforming matcher"
  (spec/def ::h-word #(string/starts-with? % "h"))
  (with-mocks [sample/cowsay]

    (testing "matches with exact values"
      (sample/some-fun "hello" "world") 
      (is (conforming sample/cowsay "hello, world")))

    (testing "can use a custom spec to validate an argument"
      (sample/some-fun "hello" "world")
      (sample/some-fun "hello" "larry")
      (is (conforming sample/cowsay ::h-word)))

    (testing "can ensure all invocations are conforming"
      (doall ; Ironically, exercise is lazy
        (spec/exercise-fn `sample/some-fun))
      (is (conforming sample/cowsay ::sample/fun-greeting)))))
```

The conforming matcher is also handy when you need to verify invocations that include generated data returned from a mock or stub. You can use any spec that you want to verify the arguments. You can also mix specs and exact values in a single call.

### Stub Functions

Stub functions are more lenient than mocks, not requiring the function to have a spec. Stub functions always return nil.

```clojure
(testing "stub functions"
  (with-stubs [clojure.java.shell/sh]

    (testing "return nil"
      (is (nil? (sample/some-fun "hello" "world"))))

    (testing "don't need a spec"
      (sample/some-fun "hello" "world")
      (is (conforming clojure.java.shell/sh "cowsay" ::sample/fun-greeting)))))
```

Just as with mocks, when using the conforming matcher on a stub, you can use specs, exact values, or a mixture of the two

### Spy Functions

Spy functions call through to the original function, but still record the calls and enforce the constraints in the function's spec.

```clojure
(testing "spy functions"
  (with-spies [sample/greet]

    (testing "calls through to the original function"
      (is (= "Hello, World!" (sample/greet "Hello" ["World!"])))
      (is (= [["Hello" ["World!"]]] (calls sample/greet))))))
```

In practice, spies in _Specific_ work a lot like the default behavior of [clojure.spec/instrument](https://clojure.github.io/clojure/branch-master/clojure.spec-api.html#clojure.spec.test/instrument), except that they are scoped only to the forms in the `with-spies` macro.

### Generated data

You can use specs to generate test data, optionally overriding certain specs to produce different combinations of values.

```clojure
  (testing "generating test data"
    (spec/def ::word (spec/and string? #(re-matches #"\w+" %)))
    (spec/def ::short-string (spec/and ::word #(> (count %) 2) #(< (count %) 5)))

    (testing "Returns a constant, conforming value for a given spec"
      (is (= "koI" (generate ::short-string)))
      (is (spec/valid? ::short-string (generate ::short-string))))

    (testing "can override specs"
      (is (= "word" (generate ::short-string ::word #{"word"}))))

    (testing "uses with-gens overrides too"
      (with-gens [::word #{"word"}]
        (is (= "word" (generate ::short-string))))))
``

Unlike the regular test.check generator, data generated in _Specific_ test doubles is deterministic. This is true for both the `generate` function and mocks. This means the values generated will not change unless spec itself changes. Whether or not you depend on this consistency is up to you.

### Generator Overrides

Sometimes, within the scope of a test (or a group of tests) it makes sense to override the generator for a spec. For example, you want to test a more specific range of values, or have a function return a single value. To do that with _Specific_ you can use the `with-gens` macro:

```clojure
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
```

Since with-gens redefines the generator for a spec, and not an entire function, you can use it to specify a portion of an otherwise default generated return value (a single nested `:phone-number` value in an entity map, for example).

## Friends and Relations

_Specific_ gets along well with the following tools:
  * [lein-test-refresh](https://github.com/jakemcc/lein-test-refresh) by [Jake McCrary](http://jakemccrary.com/)
  * [humane-test-output](https://github.com/pjstadig/humane-test-output) by Paul Stadig
  * [test.chuck](https://github.com/gfredericks/test.chuck) by [Gary Fredericks](http://gfredericks.com/)

## Changelog

0.4.0 
  * Generated values are now deterministic
  * Added core/generate to generate test data

## License

Copyright (C) 2016 Ben Rady <benrady@gmail.com>

This program is free software; you can redistribute it and/or modify it under the terms of the [GNU General Public License version 2](https://www.gnu.org/licenses/old-licenses/gpl-2.0.en.html) as published by the Free Software Foundation.

This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
