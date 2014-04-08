(ns puertorico.client
  (:require [reagent.core :as reagent :refer [atom]]
            [cljs.reader :as reader]
            [puertorico.util :as util]
            [puertorico.common :as common]))

(enable-console-print!)

(def estream (atom []))
(def sstate (atom {}))

(def ws (js/WebSocket. "ws://localhost:3000/ws"))
(aset ws "onmessage" (fn [msg]
                       (reset! sstate (reader/read-string (.-data msg)))))


(defn circles [n]
  (repeat n
          [:svg {:height 20 :width 20}
           [:circle {:cx 10 :cy 10 :r 7 :stroke "black" :stroke-width 1 :fill "white"}]]))

(defn gold [text]
  [:span.gold text])

(defn vp [text]
  [:span.vp text])

(def common-state
  {:roles (set (keys common/role-descriptions))
   :buildings common/initial-buildings
   :trader []
   :governor 0
   :turns '()
   :log []
   :quarry 8
   :fields {:coffee 8 :tobacco 9 :corn 10 :sugar 11 :indigo 12}})

(defn bank-buildings []
  (into {}
        (for [[bname bdesc] common/initial-buildings]
          [bname (select-keys bdesc [:vp :count :cost :column])])))

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

(defn next-player [current-picker sstate]
  (mod (inc current-picker) (:nplayers sstate)))

(defmulti transition identity)
(defmethod transition :good [etype state [dest good-type amount]]
  (update-in state [dest etype good-type] + amount))
(defmethod transition :field [etype state [dest good-type amount]]
  (update-in state [dest etype good-type] + amount))
(defmethod transition :worker [etype state [dest amount]]
  (update-in state [dest etype] + amount))
(defmethod transition :vp [etype state [dest amount]]
  (update-in state [dest etype] + amount))
(defmethod transition :gold [etype state [dest amount]]
  (update-in state [dest etype] + amount))
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

(defmethod transition :default [etype state]
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
          )
   
   (merge common-state
          {})))

(def game (atom (apply create-game ["Kanwei" "Lauren" "Ted"])))

(defn randomize-fields [state]
  (->> (for [[field-type field-num] (:fields @game)]
         (repeat field-num field-type))
       flatten
       shuffle
       (take (inc (:n-player @game)))
       vec))

(swap! game update-in [:available-fields] randomize-fields)


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



(defn action-done []
  (swap! estream conj [:actiondone])) 

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
  (swap! estream conj 
    [:rolepick :builder]))

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

(defn game-log []
  [:div
   (for [event (:log @game)]
     [:div (pr-str event)])])

(defn render-roles [sstate]
  [:div
   (for [role (:roles @game)]
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
      
      (game-log)
      (game-state)
      
      ]))
   
(reagent/render-component [game-board] (.getElementById js/document "app"))
