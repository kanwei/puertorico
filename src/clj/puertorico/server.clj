(ns puertorico.server
  (:require [ring.adapter.jetty :as jetty]
            [ring.middleware.resource :as resources]
            [ring.util.response :as response]
            [ring.middleware.reload :refer [wrap-reload]]
            [compojure.handler :refer [site]]
            [compojure.core :refer [defroutes GET POST DELETE ANY context]]
            [org.httpkit.server :refer :all])
  (:gen-class))

(defn ws-handler [req]
  (with-channel req channel              ; get the channel
    ;; communicate with client using method defined above
    (on-close channel (fn [status]
                        (println "channel closed")))
    (if (websocket? channel)
      (println "WebSocket channel")
      (println "HTTP channel"))
    (send! channel "WS Started")
    (on-receive channel (fn [data]       ; data received from client
           ;; An optional param can pass to send!: close-after-send?
           ;; When unspecified, `close-after-send?` defaults to true for HTTP channels
           ;; and false for WebSocket.  (send! channel data close-after-send?)
                          (send! channel data))))) ; data is sent directly to the client

(def estream (atom []))


(defroutes pr-routes
  (GET "/ws" [] ws-handler))

(def app 
  (-> pr-routes
      wrap-reload
      (resources/wrap-resource "public")))

(defn -main [& args]
  (run-server app {:port 3000}))

