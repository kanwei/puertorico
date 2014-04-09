(ns puertorico.client
  (:require [reagent.core :as reagent :refer [atom]]
            [cljs.reader :as reader]
            [puertorico.util :as util]
            [puertorico.common :as common]))

(enable-console-print!)

(def estream (atom []))
(def sstate (atom {}))

(def ws (js/WebSocket. (str "ws://" (.-host js/location) "/ws")))

(aset ws "onmessage" (fn [msg]
                       (reset! sstate (reader/read-string (.-data msg)))))

(defn send-message [event]
  (.send ws (pr-str event)))

(defn circles [n]
  (repeat n
          [:svg {:height 20 :width 20}
           [:circle {:cx 10 :cy 10 :r 7 :stroke "black" :stroke-width 1 :fill "white"}]]))

(defn gold [text]
  [:span.gold text])

(defn vp [text]
  [:span.vp text])

(defn next-player [current-picker sstate]
  (mod (inc current-picker) (:nplayers sstate)))

(defn get-players [state]
  (into {}
        (remove (fn [[k v]] (keyword? k)) state)))


;;;;;;;;;;;;;;;;;;;;;
; GAME LOOP ACTIONS ;
;;;;;;;;;;;;;;;;;;;;;

(defn add-event-to-turn [turns event]
  (let [current-turn (peek turns)]
    (-> turns
        pop
        (conj (conj current-turn event)))))

(defn add-to-turn [event]
  (swap! game update-in [:turns] add-event-to-turn event))

(defn do-settler-action [field-type i]
  (when true
    (swap! game update-in [:players 0 :fields] conj field-type)
    (if (= field-type :quarry)
      (swap! game update-in [:quarry] dec)
      (swap! game update-in [:available-fields] util/vec-remove i))
    (add-to-turn [field-type])))

(defn time-to-pick-role? []
  (let [current-turn (peek (:turns @game))]
    (or (nil? current-turn)
        (= :end (first current-turn)))))

(defmulti player-pick-role identity)
(defmethod player-pick-role :mayor [role sstate]
  (swap! estream conj 
         [:rolepick :mayor]
         [:worker :bank -1]
         [:worker (nth (:order sstate) (:role-picker sstate)) 1]))

(defmethod player-pick-role :builder [role sstate]
  (send-message [:rolepick :builder]))

(defmethod player-pick-role :default [role sstate]
  nil)

(defn whose-turn []
  (let [current-turn (peek (:turns @game))
        [role player-picked-role & actions] current-turn]
    (if (empty? actions)
      [player-picked-role :bonus])
  ))

(defn num-quarries [pname sstate]
  0)

(defn buy-building [b-name sstate]
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

(defn building-tile [b-name]
  (let [building (get-in @sstate [:bank :building b-name])]
    [:div.building {:class [(:resource building)]
                    :on-click #(buy-building b-name (calc-state))}
     [:h5.pull-left (name b-name)]

     [:span.pull-right (gold (:gold building))]
     [:span.pull-right (vp (:vp building))]

     [:div.clearfix]
     [:div.pull-left (circles (:workers building))]
     [:div.building-count.pull-right (:count building) "x"]
     [:div.clearfix]
     ]))

(defn building-board []
  [:tbody
   [:tr
    [:td (building-tile :small-indigo-maker)]
    [:td (building-tile :large-indigo-maker)]
    [:td (building-tile :tobacco-maker)]
    [:td (building-tile :guild-hall)]]
   [:tr
    [:td (building-tile :small-sugar-maker)]
    [:td (building-tile :large-sugar-maker)]
    [:td (building-tile :coffee-maker)]
    [:td (building-tile :residence)]]
   [:tr
    [:td (building-tile :small-market)]
    [:td (building-tile :hospice)]
    [:td (building-tile :factory)]
    [:td (building-tile :fortress)]]
   [:tr
    [:td (building-tile :hacienda)]
    [:td (building-tile :office)]
    [:td (building-tile :university)]
    [:td (building-tile :customs-house)]]
   [:tr
    [:td (building-tile :construction-hut)]
    [:td (building-tile :large-market)]
    [:td (building-tile :harbor)]
    [:td (building-tile :city-hall)]]
   [:tr
    [:td (building-tile :small-warehouse)]
    [:td (building-tile :large-warehouse)]
    [:td (building-tile :wharf)]]])

(defn render-ship [size type]
  [:div.ship
   (repeat size [:div.box {:class type}])
   [:div.clearfix]])

(defn render-trader [trader]
  [:div.ship
   [:div.box {:class type}]
   [:div.box {:class type}]
   [:div.box {:class type}]
   [:div.box {:class type}]
   [:div.clearfix]])

(defn player-board [[pname player]]
  [:div
   (if (:role player)
     [:div.rolecard (name (:role player))])
   [:div.well
    [:div.pull-left pname]
    [:span.pull-right (gold (:gold player))]
    [:span.pull-right (vp (:vp player))]
    [:div.clearfix]
    [:h5 "Workers: " (:worker player)]
    [:h5 "Buildings"]
    (for [building (:building player)]
      [:div (:name building)])
    [:h5 "Fields"]
    (for [[ftype fnum] (:field player)
          i (range fnum)]
      [:div.field {:class (name ftype)} (name ftype)])
    ]])

(defn player-boards [sstate]
  [:div.row
   (for [player (get-players sstate)]
     [:div.col-md-3
      (player-board player)])])

(defn render-roles [sstate]
  [:div
   (for [role (keys common/role-descriptions)]
     [:button.btn.btn-default.rolecard {:on-click #(player-pick-role role sstate)}
      (name role)
      [:span " - " (role common/role-descriptions)]])])

(defn supply-board [sstate]
  [:div
   [:i.fa.fa-trophy (get-in sstate [:bank :vp])]
   [:div "Worker Supply: " (get-in sstate [:bank :worker])]
   [:div "Worker Ship: " (get-in sstate [:worker-ship :worker])]
   [:h3 "Ships"]
   (for [ship (:ships current)]
     (render-ship ship nil))
   [:h3 "Trader"] (render-trader (:trader current))
   [:h3 "Fields"]
   [:div.quarry.field {:on-click #(do-settler :quarry nil)} (:quarry current) " Q"]
   (map-indexed (fn [i field]
                  [:div.field {:class (name field)
                               :on-click #(do-settler field i)} (name field)]
                  )
                (:available-fields current))
   [:h3 "Governor: " (:governor sstate)]
   [:h3 "Role picker: " (:role-picker sstate)]
   [:h3 "Current role: " (str (:current-role sstate))]
   [:h3 "Action picker: " (:action-picker sstate)]
   [:button.btn.btn-success {:on-click action-done} "Done!"]
   ])

(defn game-state []
  [:div
    [:blockquote (pr-str @sstate)]])

(defn game-board []
  (let [sstate @sstate]
    [:div
      [:div.row
       [:div.well.well-sm.col-md-8
        [:table.table
         [:thead
          [:tr (for [i (range 1 5)] [:th i " Quarry"])]]
         (building-board sstate)]]
       
       [:div.col-md-4 (supply-board sstate)]]
      [:div (render-roles sstate)]
      [:div.clearfix]
      
      (player-boards sstate)
      
      (game-state)
      
      ]))
   
(reagent/render-component [game-board] (.getElementById js/document "app"))
