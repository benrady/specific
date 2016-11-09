(ns specific.test-double
  (:require [clojure.test :as ctest]
            [clojure.spec.test :as stest]
            [clojure.spec.gen :as gen]
            [clojure.spec :as spec]))

(defn report-failure [msg expected actual]
  (ctest/do-report {:msg msg :type :fail :expected expected :actual actual}))

(defn- add-call [calls & args]
  (let [arg-vec (or args [])]
    (swap! calls update-in [ctest/*testing-contexts*] conj arg-vec)
    arg-vec))

(defn- expected-msg [{:keys [path pred val reason via in] :as problem}]
  (str "Calls to " (last via) " to conform to (" pred ")"))

(defn- actual-msg [{:keys [path pred val reason via in] :as problem}]
  (str "Calls to " (last via) " were " (or val [])))

(defn- validate-and-generate [fn-spec args]
  (let [args-spec (:args fn-spec)]
    (when-not (spec/valid? args-spec args)
      (doseq [problem (:clojure.spec/problems (spec/explain-data fn-spec args))]
        (report-failure (spec/explain-str fn-spec args) (expected-msg problem) (actual-msg problem))))
    (last (gen/sample (spec/gen (:ret fn-spec))))))

(defn- report-no-spec [f-sym fn-spec]
  (report-failure "No clojure.spec defined" (str "clojure.spec for " f-sym) fn-spec))

(defn- add-meta [f calls]
  (with-meta f {:specific-calls calls}))

(defn spy-fn [f]
  (let [fn-spec (spec/get-spec f) 
        calls (atom {})]
    (add-meta (comp (partial apply f) (partial add-call calls)) calls)))

(defn stub-fn
  ([] (stub-fn nil))
  ([retval]
    (let [calls (atom {})
          f (comp (constantly retval) (partial add-call calls))]
      (add-meta f calls))))

(defn mock-fn [f-sym]
  (let [fn-spec (spec/get-spec f-sym) 
        calls (atom {})]
    (add-meta (let [call-fn (partial add-call calls)]
                (if (nil? fn-spec)
                  (do 
                    (report-no-spec f-sym fn-spec)
                    call-fn)
                  (comp (partial validate-and-generate fn-spec) call-fn))) calls)))
