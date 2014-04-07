(ns puertorico.client
  (:require [reagent.core :as reagent :refer [atom]]
            [puertorico.util :as util]))

(enable-console-print!)

(def initial-buildings
  {:small-indigo-maker {:cost 1
                        :vp 1
                        :workers 1
                        :resource :indigo
                        :count 4
                        :column 1

                        }
   :large-indigo-maker {:cost 3
                        :vp 2
                        :workers 3
                        :resource :indigo
                        :count 3
                        :column 2
                        }
   :small-sugar-maker {:cost 1
                       :vp 1
                       :workers 1
                       :resource :sugar
                       :count 4
                       :column 1
                       }
   :large-sugar-maker {:cost 4
                       :vp 2
                       :workers 3
                       :resource :sugar
                       :count 3
                       :column 2
                       }
   :tobacco-maker {:cost 5
                   :vp 3
                   :workers 3
                   :resource :tobacco
                   :count 3
                   :column 3
                   }
   :coffee-maker {:cost 6
                  :vp 3
                  :workers 2
                  :resource :coffee
                  :count 3
                  :column 3
                  }
   :small-market {:cost 1
                  :vp 1
                  :workers 1
                  :count 2
                  :column 1
                  :description "When the owner of an occupied small market sells a barrel in the trader phase, he gets an extra doubloon from the bank for it."
                  }
   :large-market {:cost 5
                  :vp 2
                  :workers 1
                  :count 2
                  :column 2
                  :description "When the owner of an occupied large market sells a good in the trader phase, he gets an extra 2 doubloons from the bank for it."
                  }
   :hospice {:cost 4
             :vp 2
             :workers 1
             :count 2
             :column 2
             :description "During the settler phase, when the owner of an occupied hospice places a plantation or quarry tile on his island, he may take a colonist from the colonist supply and place it on this tile. "
             }
   :office {:cost 5
            :vp 2
            :workers 1
            :count 2
            :column 2
            :description "When the owner of an occupied office sells a good to the trading house in the trader phase, it need not be different than the goods already there. If the trading house is full, the player cannot sell a good there!"
            }
   :factory {:cost 8
             :vp 3
             :workers 1
             :count 2
             :column 3
             :description "If the owner of an occupied factory produces goods of more than one kind in the craftsman phase, he earns money from the bank: for two kinds of goods, he earns 1 doubloon, for three kinds of goods, he earns 2 doubloons, for four kinds of goods, he earns 3 doubloons, and for all five kinds of goods, he earns 5 doubloons. The number of barrels produced plays no role."
             }
   :university {:cost 7
                :vp 3
                :workers 1
                :count 2
                :column 3
                :description "During the builder phase, when the owner of an occupied university builds a building in his city, he may take a colonist from the colonist supply and place it on this tile."
                }
   :hacienda {:cost 2
              :vp 1
              :workers 1
              :count 2
              :column 1
              :description "On his turn in the settler phase, the owner of an occupied hacienda may, before he takes a face-up plantation tile, take an additional tile chosen at random from the rest of the supply. He does this by clicking on the top right corner of the hacienda."
              }
   :construction-hut {:cost 2
                      :vp 1
                      :workers 1
                      :count 2
                      :column 1
                      :description "In the settler phase, the owner of an occupied construction hut, can place a quarry on his island of one of the face-up plantation tiles."
                      }
   :harbor {:cost 8
            :vp 3
            :workers 1
            :count 2
            :column 3
            :description "Each time, during the captain phase, the owner of an occupied harbour loads goods on a cargo ship, he earns one extra victory point."
            }
   :wharf {:cost 9
           :vp 3
           :workers 1
           :count 2
           :column 3
           :description "During the captain phase, when a player with an occupied wharf must load goods, instead of loading them on a cargo ship, he may place all goods of one kind in the goods supply and score the appropriate victory points as though he has loaded them on a cargo ship. It is as though the player has an imaginary ship with unlimited capacity at his disposal."
           }
   :small-warehouse {:cost 3
                     :vp 1
                     :workers 1
                     :count 2
                     :column 1
                     :description "The owner of an occupied small warehouse may store, at the end of the captain phase, in addition to the single goods barrel he is allowed to store on his windrose, all the barrels of one kind of goods that he chooses."
                     }
   :large-warehouse {:cost 6
                     :vp 2
                     :workers 1
                     :count 2
                     :column 2
                     :description "The owner of an occupied large warehouse may store, at the end of the captain phase, in addition to the single goods barrel he is allowed to store on his wind rose, all the barrels of two kinds of goods that he chooses."
                     }
   :guild-hall {:cost 10
                :vp 4
                :workers 1
                :count 1
                :column 4
                :description "The owner of the occupied guild hall earns, at game end, an additional 1 VP for each small production building (occupied or unoccupied) in his city (= small indigo plant and small sugar mill), and an additional 2 VP for each large production building (occupied or unoccupied) in his city (= indigo plant, sugar mill, tobacco storage, and coffee roaster)."
                }
   :residence {:cost 10
               :vp 4
               :workers 1
               :count 1
               :column 4
               :description "The owner of the occupied residence earns, at game end, additional victory points for the plantations and quarries he has placed on his island. For up to nine filled island spaces, he earns 4 VP, for ten filled island spaces, he earns 5 VP, for eleven filled island spaces, he earns 6 VP, and for all twelve spaces filled, he earns 7 VP."
               }
   :customs-house {:cost 10
                   :vp 4
                   :workers 1
                   :count 1
                   :column 4
                   :description "The owner of the occupied customs house earns, at game end, one additional victory point for every four victory points he acquired during the game. The player should only count his victory point chips (and any extra victory points recorded on paper after the chip supply was exhausted, but before game end). He does not use victory points earned for his buildings at game end."
                   }
   :city-hall {:cost 10
               :vp 4
               :workers 1
               :count 1
               :column 4
               :description "The owner of the occupied city hall earns, at game end, one additional victory point for each violet building and each green building(occupied or unoccupied) in his city."
               }
   :fortress {:cost 10
              :vp 4
              :workers 1
              :count 1
              :column 4
              :description "The owner of the occupied fortress earns, at game end, one additional victory point for every three colonists on his player board."
              }
   })

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


(def common
  {:roles (set (keys role-descriptions))
   :goods [:coffee 9 :tobacco 9 :corn 10 :sugar 11 :indigo 11]
   :buildings initial-buildings
   :trader []
   :governor 0
   :turns '()
   :log []
   :quarry 8
   :fields {:coffee 8 :tobacco 9 :corn 10 :sugar 11 :indigo 12}})

(defn create-game
  ([p1 p2 p3]
   (merge common
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
