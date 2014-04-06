(ns puertorico.client
  (:require [reagent.core :as reagent :refer [atom]]))

(def initial-buildings
  {:small-indigo-maker {:cost 1
    :points 1
    :workers 1
    :resource :indigo
    :count 4
    :column 1
    
}
:large-indigo-maker {:cost 3
    :points 2
    :workers 3
    :resource :indigo
    :count 3
    :column 2
}
:small-sugar-maker {:cost 1
    :points 1
    :workers 1
    :resource :sugar
    :count 4
    :column 1
}
:large-sugar-maker {:cost 4
    :points 2
    :workers 3
    :resource :sugar
    :count 3
    :column 2
}
:tobacco-maker {:cost 5
    :points 3
    :workers 3
    :resource :tobacco
    :count 3
    :column 3
}
:coffee-maker {:cost 6
    :points 3
    :workers 2
    :resource :tobacco
    :count 3
    :column 3
}
:small-market {:cost 1
    :points 1
    :workers 1
    :count 2
    :column 1
    :description "When the owner of an occupied small market sells a barrel in the trader phase, he gets an extra doubloon from the bank for it."
}
:large-market {:cost 5
    :points 2
    :workers 1
    :count 2
    :column 2
    :description "When the owner of an occupied large market sells a good in the trader phase, he gets an extra 2 doubloons from the bank for it."
}
:hospice {:cost 4
    :points 2
    :workers 1
    :count 2
    :column 2
    :description "During the settler phase, when the owner of an occupied hospice places a plantation or quarry tile on his island, he may take a colonist from the colonist supply and place it on this tile. "
}
:office {:cost 5
    :points 2
    :workers 1
    :count 2
    :column 2
    :description "When the owner of an occupied office sells a good to the trading house in the trader phase, it need not be different than the goods already there. If the trading house is full, the player cannot sell a good there!"
}
:factory {:cost 8
    :points 3
    :workers 1
    :count 2
    :column 3
    :description "If the owner of an occupied factory produces goods of more than one kind in the craftsman phase, he earns money from the bank: for two kinds of goods, he earns 1 doubloon, for three kinds of goods, he earns 2 doubloons, for four kinds of goods, he earns 3 doubloons, and for all five kinds of goods, he earns 5 doubloons. The number of barrels produced plays no role."
}
:university {:cost 7
    :points 3
    :workers 1
    :count 2
    :column 3
    :description "During the builder phase, when the owner of an occupied university builds a building in his city, he may take a colonist from the colonist supply and place it on this tile."
}
:hacienda {:cost 2
    :points 1
    :workers 1
    :count 2
    :column 1
    :description "On his turn in the settler phase, the owner of an occupied hacienda may, before he takes a face-up plantation tile, take an additional tile chosen at random from the rest of the supply. He does this by clicking on the top right corner of the hacienda."
}
:construction-hut {:cost 2
    :points 1
    :workers 1
    :count 2
    :column 1
    :description "In the settler phase, the owner of an occupied construction hut, can place a quarry on his island of one of the face-up plantation tiles."
}
:harbor {:cost 8
    :points 3
    :workers 1
    :count 2
    :column 3
    :description "Each time, during the captain phase, the owner of an occupied harbour loads goods on a cargo ship, he earns one extra victory point."
}
:wharf {:cost 9
    :points 3
    :workers 1
    :count 2
    :column 3
    :description "During the captain phase, when a player with an occupied wharf must load goods, instead of loading them on a cargo ship, he may place all goods of one kind in the goods supply and score the appropriate victory points as though he has loaded them on a cargo ship. It is as though the player has an imaginary ship with unlimited capacity at his disposal."
}
:small-warehouse {:cost 3
    :points 1
    :workers 1
    :count 2
    :column 1
    :description "The owner of an occupied small warehouse may store, at the end of the captain phase, in addition to the single goods barrel he is allowed to store on his windrose, all the barrels of one kind of goods that he chooses."
}
:large-warehouse {:cost 6
    :points 2
    :workers 1
    :count 2
    :column 2
    :description "The owner of an occupied large warehouse may store, at the end of the captain phase, in addition to the single goods barrel he is allowed to store on his wind rose, all the barrels of two kinds of goods that he chooses."
}
:guild-hall {:cost 10
    :points 4
    :workers 1
    :count 1
    :column 4
    :description "The owner of the occupied guild hall earns, at game end, an additional 1 VP for each small production building (occupied or unoccupied) in his city (= small indigo plant and small sugar mill), and an additional 2 VP for each large production building (occupied or unoccupied) in his city (= indigo plant, sugar mill, tobacco storage, and coffee roaster)."
}
:residence {:cost 10
    :points 4
    :workers 1
    :count 1
    :column 4
    :description "The owner of the occupied residence earns, at game end, additional victory points for the plantations and quarries he has placed on his island. For up to nine filled island spaces, he earns 4 VP, for ten filled island spaces, he earns 5 VP, for eleven filled island spaces, he earns 6 VP, and for all twelve spaces filled, he earns 7 VP."
}
:customs-house {:cost 10
    :points 4
    :workers 1
    :count 1
    :column 4
    :description "The owner of the occupied customs house earns, at game end, one additional victory point for every four victory points he acquired during the game. The player should only count his victory point chips (and any extra victory points recorded on paper after the chip supply was exhausted, but before game end). He does not use victory points earned for his buildings at game end."
}
:city-hall {:cost 10
    :points 4
    :workers 1
    :count 1
    :column 4
    :description "The owner of the occupied city hall earns, at game end, one additional victory point for each violet building and each green building(occupied or unoccupied) in his city."
}
:fortress {:cost 10
    :points 4
    :workers 1
    :count 1
    :column 4
    :description "The owner of the occupied fortress earns, at game end, one additional victory point for every three colonists on his player board."
}
})

(defn create-player [name]
  {:name name
   :gold 3
   :vp 0
   :fields []
   :buildings []
   })

(def players (atom (map create-player ["Kanwei" "Lauren" "Ted" "Adam"])))

(def buildings (atom initial-buildings))

(defn building-tile [name]
    (let [building (name @buildings)]
      [:div.building
       [:h5 (str name)]
       [:i.fa.fa-money.pull-right (:cost building)]
       [:i.fa.fa-trophy.pull-right (:points building)]
       [:div (:workers building) "worker"]
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
    [:td (building-tile :large-ware)]
    [:td (building-tile :wharf)]]])

(defn ship [size]
  [:div.box]
  )

(defn player-board [player]
  [:div.well
    [:div.pull-left (:name player)]
    [:i.fa.fa-money.pull-right (:gold player)]
    [:i.fa.fa-trophy.pull-right (:vp player)]
    [:div.clearfix]
    [:h5 "Buildings"]
    (for [building (:buildings player)]
        [:div (:name building)])
    [:h5 "Fields"]
    (for [field (:fields player)]
        [:div (:name field)])
    ])

(defn game-board []
  [:div.row
   (for [player @players]
     [:div.col-md-3 {:on-click #(swap! players dissoc player)}
        (player-board player)])])

(reagent/render-component [building-board] (.getElementById js/document "building-board"))
(reagent/render-component [game-board] (.getElementById js/document "gameboards"))

