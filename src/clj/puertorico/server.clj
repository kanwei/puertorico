(ns puertorico.server
  (:require [ring.adapter.jetty :as jetty]
            [ring.middleware.resource :as resources]
            [ring.util.response :as response]
            [ring.middleware.reload :refer [wrap-reload]]
            [puertorico.util :as util]
            [puertorico.common :as common]
            [compojure.handler :refer [site]]
            [compojure.core :refer [defroutes GET POST DELETE ANY context]]
            [org.httpkit.server :refer :all])
  (:gen-class))

(def estream (atom []))

(defn bank-buildings []
  (into {}
        (for [[bname bdesc] common/initial-buildings]
          [bname (select-keys bdesc [:count :cost :column])])))

(swap! estream conj
       [:good :bank :coffee 9]
       [:good :bank :tobacco 9]
       [:good :bank :corn 10]
       [:good :bank :sugar 11]
       [:good :bank :indigo 11]
       
       [:field :bank :quarry 8]
       [:field :bank :coffee 8]
       [:field :bank :tobacco 9]
       [:field :bank :corn 10]
       [:field :bank :sugar 11]
       [:field :bank :indigo 12]
       
       [:building :bank (bank-buildings)]
       
       )

(defn next-player [current-picker cstate]
  (mod (inc current-picker) (:nplayers cstate)))

(defmulti transition (fn [x & _] x))
(defmethod transition :good [etype state [dest good-type amount]]
  (update-in state [dest etype good-type] (fnil + 0) amount))
(defmethod transition :field [etype state [dest good-type amount]]
  (update-in state [dest etype good-type] (fnil + 0) amount))
(defmethod transition :worker [etype state [dest amount]]
  (update-in state [dest etype] (fnil + 0) amount))
(defmethod transition :vp [etype state [dest amount]]
  (update-in state [dest etype] (fnil + 0) amount))
(defmethod transition :gold [etype state [dest amount]]
  (update-in state [dest etype] (fnil + 0) amount))
(defmethod transition :building [etype state [dest buildings]]
  (assoc-in state [dest :building] buildings))
(defmethod transition :add-player [etype state [pname]]
  (-> state
      (update-in [:order] conj pname)
      (update-in [:nplayers] inc)
      (assoc-in [pname] {:name pname :worker 0 :gold 0 :vp 0 :field {} :building {}})))

(defmethod transition :rolepick [etype state [role]]
  (-> state
      (assoc-in [((:order state) (:role-picker state)) :role] role) 
      (assoc :current-role role)))

(defmethod transition :actiondone [etype state]
  (update-in state [:action-picker] next-player state))

(defmethod transition :default [etype state & _]
  state)


(defn calc-state []
  (let [estream @estream]
    (reduce 
      (fn [acc [etype & eargs]]
        (transition etype acc eargs))
      {:order []
       :nplayers 0
       :governor 0
       :role-picker 0
       :current-role nil
       :action-picker 0
       :bank {:vp 0
              :field-count 0
              :building nil}}
        estream)))

(defn get-players [state]
  (into {}
        (remove (fn [[k v]] (keyword? k)) state)))

(defn create-game
  ([p1 p2 p3]
   (swap! estream conj
          [:vp :bank 75]
          [:worker :bank 55]
          
          [:worker :worker-ship 3]
          [:worker :bank -3]
          
          [:ship 0 4]
          [:ship 1 5]
          [:ship 2 6]
          
          [:add-player p1]
          [:gold p1 2]
          [:field p1 :indigo 1]
          
          [:add-player p2]
          [:gold p2 2]
          [:field p2 :indigo 1]
          
          [:add-player p3]
          [:gold p3 1]
          [:field p3 :corn 1]
          )))

(atom (apply create-game ["Kanwei" "Lauren" "Ted"]))

(defn ws-handler [req]
  (with-channel req channel
    (on-close channel (fn [status]
                        (println "channel closed")))
    (if (websocket? channel)
      (println "WebSocket channel")
      (println "HTTP channel"))
    (send! channel (pr-str (calc-state)))
    (on-receive channel (fn [data]
                          (send! channel data))))) ; data is sent directly to the client

(defroutes pr-routes
  (GET "/ws" [] ws-handler))

(def app 
  (-> pr-routes
      wrap-reload
      (resources/wrap-resource "public")))

(defn -main [& args]
  (run-server app {:port 3000}))

