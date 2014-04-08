(defproject puertorico "0.1.0-SNAPSHOT"
  :description "Puerto Rico Board Game in Clojure/Clojurescript"
  :url "https://github.com/kanwei/puertorico"
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/clojurescript "0.0-2202"]
                 [ring "1.2.2"]
                 [compojure "1.1.6"]
                 [reagent "0.4.2"]
                 [http-kit "2.1.18"]]
  :plugins [[lein-cljsbuild "1.0.3"]
            [lein-ring "0.8.10"]]
  :hooks []
  :source-paths ["src/clj" "src/common"]
  
  :cljsbuild { 
    :builds {
      :main {
        :source-paths ["src/cljs" "src/common"]
        :compiler {:output-to "resources/public/js/cljs.js"
                   :output-dir "resources/public/js/cljs"
                   :optimizations :none
                   :pretty-print true}
        :jar true}}}
  :main puertorico.server)
