(defproject puertorico "0.1.0-SNAPSHOT"
  :description "Puerto Rico Board Game in Clojure/Clojurescript"
  :url "https://github.com/kanwei/puertorico"
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/clojurescript "0.0-2356"]
                 [ring "1.3.1"]
                 [compojure "1.1.9"]
                 [reagent "0.4.2"]
                 [http-kit "2.1.19"]]
  :plugins [[lein-cljsbuild "1.0.3"]
            [lein-ring "0.8.12"]]
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
