(defproject om-next-client-demo "0.0.0-SNAPSHOT"
  :description "Clicking checkboxes adds/removes lines on a fake graph"

  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/clojurescript "1.7.228"]
                 [org.omcljs/om "1.0.0-alpha32"]
                 [default-db-format "0.1.1-SNAPSHOT"]]

  :plugins [[lein-cljsbuild "1.1.2"]]

  :clean-targets ^{:protect false} ["resources/public/js/compiled" "target"]
  :source-paths ["dev/server" "src"]

  :cljsbuild {:builds {:dev        {:source-paths ["dev/client" "src"]
                                    :figwheel     true
                                    :compiler     {:main                 "cljs.user"
                                                   :asset-path           "js/compiled/dev"
                                                   :output-to            "resources/public/js/compiled/checkboxes.js"
                                                   :output-dir           "resources/public/js/compiled/dev"
                                                   :parallel-build       true
                                                   :recompile-dependents true
                                                   :optimizations        :none}}}}

  :figwheel {:css-dirs    ["resources/public/css"]
             :server-port 2345
             :open-file-command "open-in-intellij"}

  :profiles {:dev {:repl-options {
                                  :init-ns          user
                                  :nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]
                                  :port             7001
                                  }
                   :env          {:dev true}
                   :dependencies [[figwheel-sidecar "0.5.0-5"]
                                  [binaryage/devtools "0.5.2"]
                                  [com.cemerick/piggieback "0.2.1"]
                                  [org.clojure/tools.nrepl "0.2.12"]]}})
