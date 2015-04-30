(defproject pipfit "0.1.0"
  :description "A personal finance manager that doesn't require bank login information."
  :url "http://akshaynathan.com/pipfit"
  :license {:name "GNU General Public License v2.0"
            :url "https://www.gnu.org/licenses/gpl-2.0.html"}
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/clojurescript "0.0-3211"]
                 [org.omcljs/om "0.8.8"]
                 [cljs-http "0.1.30"]
                 [figwheel "0.2.7"]
                 [clj-time "0.9.0"]
                 [javax.mail/mail "1.4.4"]
                 [com.cemerick/friend "0.2.1" :exclusions
                  [org.clojure/core.cache]]
                 [compojure "1.3.3"]
                 [org.clojure/tools.logging "0.3.1"]
                 [net.sf.ofx4j/ofx4j "1.6"]
                 [com.novemberain/monger "2.0.0"]
                 [cheshire "5.1.1"]
                 [environ "1.0.0"]
                 [ring/ring-json "0.3.1"]
                 [secretary "1.2.1"]
                 [crypto-password "0.1.3"]
                 [ring "1.3.2"]
                 [racehub/om-bootstrap "0.5.0"]]
  :plugins [[lein-environ "1.0.0"]
            [lein-figwheel "0.2.7"]
            [lein-cljsbuild "1.0.5"]]
  :cljsbuild {:builds [{:id "main"
                        :source-paths ["src"]
                        :compiler {:output-to "resources/public/js/compiled/main.js"
                                   :output-dir "resources/public/js/compiled/out"
                                   :optimizations :none
                                   :source-map true}}]}
  :figwheel {:ring-handler pipfit.web.clj.login/app
             :http-server-root "public"
             }
  :uberjar-name "pipfit-standalone.jar"
  :profiles {:production {:env {:production true}}})
