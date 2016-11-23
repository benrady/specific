(ns specific.test-double
  (:require [specific.gene :as gene]
            [clojure.string :as string]
            [clojure.test :as ctest]
            [clojure.spec.test :as stest]
            [clojure.spec :as spec]))

(def ^:dynamic *gen-overrides* {})

(defn report-fail [m]
  (ctest/do-report (assoc m :type :fail)))

(defn report-failure [msg expected actual]
  (report-fail {
           ;:file "file" :line "line" 
           :message msg 
           :expected expected 
           :actual actual
           }))

(defn- record-calls [calls & args]
  (let [arg-vec (vec (or args []))]
    (swap! calls update-in [ctest/*testing-contexts*] (comp vec conj) arg-vec)
    arg-vec))

(defn- expected-msg [{:keys [path pred val reason via in] :as problem}]
  (:pred problem))

(defn- actual-msg [problem]
  (or (:val problem) []))

(defn- spec-name [spec]
  (:clojure.spec/name (meta spec)))

(defn- explain-str-data [data]
  (with-out-str (spec/explain-out data)))

(defn- remove-in [exp-data]
  (update exp-data :clojure.spec/problems (fn [p] (map #(assoc % :in []) p))))

(defn- file-and-line []
  (let [s (first (drop-while
    #(let [cl-name (.getClassName ^StackTraceElement %)]
       (or (string/starts-with? cl-name "java.lang.") 
           (string/starts-with? cl-name "clojure.")
           (string/starts-with? cl-name "specific.test_double$")))
    (.getStackTrace (Thread/currentThread))))]
    {:file (.getFileName s) :line (.getLineNumber s)}))

(defn- build-reports [exp-data]
  (for [problem (:clojure.spec/problems exp-data)]
    (merge (file-and-line) 
           {:message (explain-str-data exp-data) 
            :expected (expected-msg problem) 
            :actual (actual-msg problem)})))

(defn- check-args [via args-spec args]
  (when-not (spec/valid? args-spec args)
    (let [exp-data (remove-in (spec/explain-data* args-spec [:args] [via] [] args))]
      (doall (map report-fail (build-reports exp-data))))))

(defn- validate-and-generate [fn-spec args]
  (when-let [args-spec (:args fn-spec)]
    (check-args (spec-name fn-spec) args-spec args))
  (gene/static-sample (:ret fn-spec) *gen-overrides*)
  )

(defn- report-no-spec [fn-sym fn-spec]
  (report-failure "No clojure.spec defined" (str "clojure.spec for " fn-sym) fn-spec))

(defn- add-meta [f calls]
  (with-meta f {:specific-calls calls}))

(defn spy-fn [f]
  (let [fn-spec (spec/get-spec f) 
        calls (atom {})]
    (add-meta (comp (partial apply f) (partial record-calls calls)) calls)))

(defn stub-fn
  ([] (stub-fn nil))
  ([retval]
    (let [calls (atom {})
          f (comp (constantly retval) (partial record-calls calls))]
      (add-meta f calls))))

(defn mock-fn [fn-sym]
  (let [fn-spec (spec/get-spec fn-sym) 
        calls (atom {})]
    (add-meta (let [call-fn (partial record-calls calls)]
                (if (nil? fn-spec)
                  (do 
                    (report-no-spec fn-sym fn-spec)
                    call-fn)
                  (comp (partial validate-and-generate fn-spec) call-fn))) calls)))
