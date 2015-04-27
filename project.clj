(defproject pipfit "0.1.0"
  :description "A personal finance manager that doesn't require bank login information."
  :url "http://akshaynathan.com/pipfit"
  :license {:name "GNU General Public License v2.0"
            :url "https://www.gnu.org/licenses/gpl-2.0.html"}
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [clj-time "0.9.0"]
                 [javax.mail/mail "1.4.4"]
                 [org.clojure/tools.logging "0.3.1"]
                 [net.sf.ofx4j/ofx4j "1.6"]
                 [com.novemberain/monger "2.0.0"]
                 [cheshire "5.1.1"]
                 [environ "1.0.0"]
                 [ring "1.3.2"]]
  :plugins [[lein-environ "1.0.0"]]
  :uberjar-name "pipfit-standalone.jar"
  :profiles {:production {:env {:production true}}})
