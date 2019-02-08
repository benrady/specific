(ns specific.gene-spec
  (:require [clojure.spec.alpha :as spec]
            [specific
             [gene :as gene]])
  (:use [clojure.test]))

(spec/def ::three-string (spec/and string? #(= 3 (count %))))

(deftest gene
  (testing "ensures custom specs are consistent"
    (is (= "zW3" (gene/det-sample ::three-string))))

  (testing "returns a consistent value for specs"
    (is (= "" (gene/det-sample string?)))))
