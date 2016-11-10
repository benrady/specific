(defproject com.benrady/specific "0.2.0"
  :url "https://github.com/benrady/specific"
  :description "Generate test doubles using clojure.spec"
  :profiles {:dev 
             {:plugins [[com.jakemccrary/lein-test-refresh "0.17.0"]]
              :dependencies [[org.clojure/clojure "1.8.0"]
                             [clojure-future-spec "1.9.0-alpha13"]
                             [org.clojure/test.check "0.9.0"]
                             [pjstadig/humane-test-output "0.8.1"]
                             [org.clojure/test.check "0.9.0"]
                             [org.mockito/mockito-core "1.9.5"]
                             [cljito "0.2.1"]]
              :injections [(require 'pjstadig.humane-test-output) (pjstadig.humane-test-output/activate!)]
             }}
  :test-paths ["test"]
  :dependencies []
  :license {:name "GNU Public License v2"
            :url "https://www.gnu.org/licenses/old-licenses/gpl-2.0.en.html"}
  :deploy-repositories [["snapshots" {:url "https://clojars.org/repo"
                                      :username "benrady"}]
                        ["releases" {:url "https://clojars.org/repo"
                                     :username "benrady"}]]
  :test-refresh {:growl false
                 :notify-on-success false
                 :quiet true
                 :changes-only false
                 :watch-dirs ["src" "test"]
                 :refresh-dirs ["src" "test"]})
