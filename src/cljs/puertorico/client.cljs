(ns puertorico.client
  (:require [reagent.core :as reagent :refer [atom]]
            [puertorico.util :as util]
            [puertorico.common :as common]))

(enable-console-print!)


(defn create-player [name gold starting-field]
  {:name name
   :gold gold
   :vp 0
   :fields [starting-field]
   :buildings []
   })

(defn circles [n]
  (repeat n
          [:svg {:height 20 :width 20}
           [:circle {:cx 10 :cy 10 :r 7 :stroke "black" :stroke-width 1 :fill "white"}]]))

(defn gold [text]
  [:span.gold text])

(defn vp [text]
  [:span.vp text])

(def role-descriptions {:captain "Goods for VP"
                        :trader "Goods for money"
                        :builder "Buy buildings"
                        :settler "Get quarry/field(s)"
                        :mayor "Get colonists"
                        :craftsman "Produce goods"})


(def common-state
  {:roles (set (keys role-descriptions))
   :goods [:coffee 9 :tobacco 9 :corn 10 :sugar 11 :indigo 11]
   :buildings common/initial-buildings
   :trader []
   :governor 0
   :turns '()
   :log []
   :quarry 8
   :fields {:coffee 8 :tobacco 9 :corn 10 :sugar 11 :indigo 12}})

(defn create-game
  ([p1 p2 p3]
   (merge common-state
          {:n-player 3
           :vp 75 :colonists 55 :ships [4 5 6]
           :colonist-ship 3
           :players [(create-player p1 2 :indigo)
                     (create-player p2 2 :indigo)
                     (create-player p3 1 :corn)]})))

(def game (atom (apply create-game ["Kanwei" "Lauren" "Ted"])))

(defn randomize-fields []
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

(defn do-mayor []
  
  )

(defn do-settler [field-type i]
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

(defn player-pick-role [role]
  (if (time-to-pick-role?)
    (let [cstate @game
          player-picked-role (:governor cstate)]
      (swap! game update-in [:turns] conj [role player-picked-role])
      (swap! game update-in [:log] conj [:rolestart role player-picked-role])
      (swap! game update-in [:players player-picked-role] assoc :role role))))

(defn whose-turn []
  (let [current-turn (peek (:turns @game))
        [role player-picked-role & actions] current-turn]
    (if (empty? actions)
      [player-picked-role :bonus])
  ))



(defn buy-building [b-name]
  (println "Trying to buy " b-name))

(defn building-tile [b-name]
  (let [building (b-name (:buildings @game))]
    [:div.building {:class [(:resource building)]
                    :on-click #(buy-building b-name)}
     [:h5.pull-left (name b-name)]

     [:span.pull-right (gold (:cost building))]
     [:span.pull-right (vp (:vp building))]

     [:div.clearfix]
     [:div.pull-left (circles (:workers building))]
     [:div.building-count.pull-right (:count building) "x"]
     [:div.clearfix]
     ]))

(defn building-board []
  [:table
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

(defn player-board [player]
  [:div
   (if (:role player)
     [:div.rolecard (name (:role player))])
   [:div.well
    [:div.pull-left (:name player)]
    [:span.pull-right (gold (:gold player))]
    [:span.pull-right (vp (:vp player))]
    [:div.clearfix]
    [:h5 "Buildings"]
    (for [building (:buildings player)]
      [:div (:name building)])
    [:h5 "Fields"]
    (for [field (:fields player)]
      [:div.field {:class (name field)} (name field)])
    ]])

(defn player-boards []
  [:div.row
   (for [player (:players @game)]
     [:div.col-md-3
      (player-board player)])])

(defn game-log []
  [:div
   (for [event (:log @game)]
     [:div (pr-str event)])])

(defn render-roles []
  [:div
   (for [role (:roles @game)]
     [:button.btn.btn-default.rolecard {:on-click #(player-pick-role role)}
      (name role)
      [:span " - " (role role-descriptions)]])])

(defn supply-board []
  (let [current @game]
    [:div
     [:i.fa.fa-trophy (:vp current)]
     [:div "Colonist Supply: " (:colonists current)]
     [:div "Colonist Ship: " (:colonist-ship current)]
     [:h3 "Ships"]
     (for [ship (:ships current)]
       (render-ship ship))
     [:h3 "Trader"] (render-trader (:trader current))
     [:h3 "Fields"]
     [:div.quarry.field {:on-click #(do-settler :quarry nil)} (:quarry current) " Q"]
     (map-indexed (fn [i field]
                    [:div.field {:class (name field)
                                 :on-click #(do-settler field i)} (name field)]
                    )
                  (:available-fields current))
     [:h3 "Current turn: " (pr-str (peek (:turns current)))]
     [:h3 "Pick role? " (str (time-to-pick-role?))]
     [:h3 "Whose turn? " (str (whose-turn))]
     #_[:h3 "Current role: " (current-role)]]))

(defn game-state []
  [:blockquote (pr-str @game)])


(reagent/render-component [building-board] (.getElementById js/document "building-board"))
(reagent/render-component [player-boards] (.getElementById js/document "player-boards"))
(reagent/render-component [supply-board] (.getElementById js/document "supply-board"))
(reagent/render-component [game-state] (.getElementById js/document "game-state"))
(reagent/render-component [render-roles] (.getElementById js/document "roles-board"))
(reagent/render-component [game-log] (.getElementById js/document "game-log"))
