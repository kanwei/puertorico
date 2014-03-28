(defproject puertorico "0.1.0-SNAPSHOT"
  :description "Puerto Rico Board Game in Clojure/Clojurescript"
  :url "https://github.com/kanwei/puertorico"
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/clojurescript "0.0-2173"]
                 [ring "1.2.2"]]
  :plugins [[lein-cljsbuild "1.0.2"]
            [lein-ring "0.8.10"]]
  :hooks []
  :source-paths ["src/clj"]
  :cljsbuild { 
    :builds {
      :main {
        :source-paths ["src/cljs"]
        :compiler {:output-to "resources/public/js/cljs.js"
                   :optimizations :simple
                   :pretty-print true}
        :jar true}}}
  :main puertorico.server
  :ring {:handler puertorico.server/app})
