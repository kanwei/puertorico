(ns puertorico.server
  (:require [ring.adapter.jetty :as jetty]
            [ring.middleware.resource :as resources]
            [ring.util.response :as response]
            [ring.middleware.reload :refer [wrap-reload]]
            [clojure.edn :as edn]
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
          [bname (select-keys bdesc [:resource :count :vp :gold :column])])))

(defn next-player [current-picker cstate]
  (nth (:order cstate) (mod (inc (.indexOf (:order cstate) current-picker)) (:nplayers cstate))))

(defmulti transition (fn [x & _] x))

(defmethod transition :initialize [etype state [k v]]
  (assoc state k v))

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


(defmethod transition :prospector [etype state [player]]
  (-> state
      (update-in [player :gold] (fn [g] (if (= player (:rolepicker state)) (inc g) g)))
      (update-in [:actionturns] inc)
      (update-in [:actionpicker] next-player state)))

(defmethod transition :add-player [etype state [pname]]
  (-> state
      (update-in [:order] conj pname)
      (update-in [:nplayers] inc)
      (assoc-in [pname] {:name pname :worker 0 :gold 0 :vp 0 :field {} :building {}})))

(defmethod transition :rolepick [etype state [role player]]
  (-> state
      (assoc-in [player :role] role)
      (update-in [:roles] disj role)
      (assoc :activerole role)
      (assoc :actionpicker player)))

(defmethod transition :default [etype state & _]
  state)

(defn calc-state []
  (let [estream @estream]
    (reduce 
      (fn [state [etype & eargs]]
        (let [transitioned (transition etype state eargs)]
          (if (and (:activerole state) (= (:actionturns state) (dec (:nplayers state))))
            (-> transitioned
                (assoc :actionturns 0 :activerole nil :actionpicker nil)
                (update-in [:rolepicker] next-player transitioned))
            transitioned)))
      {:order []
       :nplayers 0
       :actionturns 0
       :activerole nil
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
          
          [:initialize :rolepicker p1]
          [:initialize :governor p1]
          
          [:initialize :roles (set (keys common/role-descriptions))]
          
          [:add-player p2]
          [:gold p2 2]
          [:field p2 :indigo 1]
          
          [:add-player p3]
          [:gold p3 1]
          [:field p3 :corn 1]
          )))

(defn reset-game []
  (reset! estream [])
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
  (apply create-game ["Kanwei" "Adam" "Ted"]))

(reset-game)

(defn do-mayor []
  (swap! estream conj 
         [:rolepick :mayor]
         [:worker :bank -1]
         #_[:worker (nth (:order sstate) (:role-picker sstate)) 1]))

#_(defn buy-building [b-name sstate]
  (println "Trying to buy " b-name)
  (let [apicker (:action-picker sstate)
        building (get-in sstate [:bank :building b-name])
        discount (max (:column building) (num-quarries (:action-picker sstate)))
        cost (:gold building)
        cost (if (= apicker (:role-picker sstate))
               (dec cost)
               cost)
        cost (max 0 (- cost discount))
        ]
    (swap! estream conj
           [:buy-building b-name apicker]
           [:gold ((:order sstate) apicker) (- cost)]
           )
  ))

(def connections (atom #{}))


(defn ws-handler [req]
  (with-channel req channel
    (on-close channel (fn [status]
                        (swap! connections disj channel)
                        (println "channel closed")))
    (swap! connections conj channel)
    
    (send! channel (pr-str (calc-state)))
    
    (on-receive channel (fn [data]
                          (let [parsed (edn/read-string data)]
                            (if (= :reset parsed)
                              (reset-game)
                              (swap! estream conj parsed))
                            (doseq [chan @connections]
                              (send! chan (pr-str (calc-state)))))))))

(defroutes pr-routes
  (GET "/ws" [] ws-handler))

(def app 
  (-> pr-routes
      wrap-reload
      (resources/wrap-resource "public")))

(defn -main [& args]
  (run-server app {:port 3000}))

