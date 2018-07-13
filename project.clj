(defproject
  puertorico
  "0.1.0-SNAPSHOT"
  :dependencies
  [[thheller/shadow-cljs "2.4.20" :scope "test"]
   [deraen/boot-sass "0.3.1" :scope "test"]
   [cheshire "5.8.0"]
   [org.clojure/clojure "1.10.0-alpha6"]
   [org.clojure/clojurescript "1.10.339"]
   [ring "1.7.0-RC1"]
   [clj-time "0.14.4"]
   [compojure "1.6.1"]
   [reagent
    "0.8.1"
    :exclusions
    [cljsjs/react-dom
     cljsjs/react
     cljsjs/react-dom-server
     cljsjs/create-react-class]]
   [re-frame "0.10.5" :exclusions [reagent]]
   [http-kit "2.3.0"]]
  :repositories
  [["clojars" {:url "https://repo.clojars.org/"}]
   ["maven-central" {:url "https://repo1.maven.org/maven2"}]]
  :source-paths
  ["src" "resources"])