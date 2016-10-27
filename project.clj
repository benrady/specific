(defproject com.benrady/specific "0.1.0-SNAPSHOT"
  :profiles {:dev 
             {:plugins [[com.jakemccrary/lein-test-refresh "0.17.0"]]
              :dependencies [[org.clojure/clojure "1.8.0"]
                             [clojure-future-spec "1.9.0-alpha13"]
                             [org.clojure/test.check "0.9.0"]
                             [pjstadig/humane-test-output "0.8.1"]
                             [org.clojure/test.check "0.9.0"]
                             [org.mockito/mockito-core "1.9.5"]
                             [cljito "0.2.1"]]
              :injections [(require 'pjstadig.humane-test-output)
                           (pjstadig.humane-test-output/activate!)]}}
  :test-paths ["test"]
  :dependencies []
  :test-refresh {:growl false
                 :notify-on-success false
                 :quiet true
                 :changes-only true
                 :watch-dirs ["src" "test"]
                 :refresh-dirs ["src" "test"]})
