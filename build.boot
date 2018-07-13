(set-env!
  :source-paths #{"src"}
  :resource-paths #{"resources"}
  :dependencies '[[thheller/shadow-cljs "2.4.20" :scope "test"]
                  [deraen/boot-sass "0.3.1" :scope "test"]

                  [cheshire "5.8.0"]
                  [org.clojure/clojure "1.10.0-alpha6"]
                  [org.clojure/clojurescript "1.10.339"]
                  [ring "1.7.0-RC1"]
                  [clj-time "0.14.4"]
                  [compojure "1.6.1"]
                  [reagent "0.8.1" :exclusions [cljsjs/react cljsjs/react-dom cljsjs/create-react-class cljsjs/react-dom-server]]
                  [re-frame "0.10.5" :exclusions [reagent]]
                  [http-kit "2.3.0"]])

(task-options!
  pom {:project 'puertorico
       :version "0.1.0-SNAPSHOT"}
  aot {:namespace '#{puertorico.server}}
  jar {:main     'puertorico.server
       :manifest {"Description" "Puerto Rico game in Clojure(Script)"
                  "Url"         "https://github.com/kanwei/puertorico"}})


(defn get-shadow-cljs-deps []
  (:dependencies (clojure.edn/read-string (slurp "shadow-cljs.edn"))))

(defn- generate-lein-project-file! []
  (require 'clojure.java.io)
  (let [pfile ((resolve 'clojure.java.io/file) "project.clj")
        ; Only works when pom options are set using task-options!
        {:keys [project version]} (:task-options (meta #'boot.task.built-in/pom))
        prop #(when-let [x (get-env %2)] [%1 x])
        head (list* 'defproject (or project 'boot-project) (or version "0.0.0-SNAPSHOT")
                    (concat
                      (prop :url :url)
                      (prop :license :license)
                      (prop :description :description)
                      [:dependencies (vec (concat
                                            (get-env :dependencies)
                                            (get-shadow-cljs-deps)))
                       :repositories (get-env :repositories)
                       :source-paths (vec (concat (get-env :source-paths)
                                                  (get-env :resource-paths)))]))
        proj (pp-str head)]
    (spit pfile proj)))

(deftask lein-generate
         "Generate a leiningen `project.clj` file.
          This task generates a leiningen `project.clj` file based on the boot
          environment configuration, including project name and version (generated
          if not present), dependencies, and source paths. Additional keys may be added
          to the generated `project.clj` file by specifying a `:lein` key in the boot
          environment whose value is a map of keys-value pairs to add to `project.clj`."
         []
         (generate-lein-project-file!))

(require '[deraen.boot-sass :refer :all])
(require '[boot.util :as util])
(require '[boot.core :as core])
(require '[boot.shadow-cljs :as cljs])
(require '[shadow.cljs.devtools.server :as shadow-server])
(require '[shadow.cljs.devtools.api :as shadow])

(deftask shadow-dev []
         (core/with-pass-thru [fs]
                              (shadow-server/start!)
                              (shadow/watch :puertorico-client)))


(deftask dev
         "Run when developing"
         []
         (lein-generate)
         (comp
           #_(eval '(do (require '[puertorico.server :as server])))
           (shadow-dev)
           (repl :port 63001
                 :server true)
           (watch)
           (sass :output-style :nested)))

(deftask releasejs
         []
         #_(server/start!)
         (shadow/release :puertorico-client))

(deftask uberjar
         "Build uberjar"
         []
         (comp
           #_(cljs :optimizations :advanced
                   :compiler-options {:output-wrapper true})
           (sass :output-style :compressed)
           (cljs/release :builds #{:puertorico-client})
           (aot)
           #_(pom)
           (uber)
           (target)))
