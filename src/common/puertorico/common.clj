(ns puertorico.common)

(defn money-after-building [sstate player b-name]
  (let [building (get-in sstate [:bank :building b-name])
        player-gold (get-in sstate [player :gold])
        discount (min (:column building) (or (get-in sstate [player :field :quarry]) 0))
        cost (:gold building)
        cost (if (= player (:rolepicker sstate))
               (dec cost)
               cost)
        cost (max 0 (- cost discount))
        ]
    (println b-name cost discount (- player-gold cost))
    (if-not (pos? (:count building))
      -1
      (- player-gold cost))))

(defn randomize-fields [sstate]
  (->> (for [[fieldtype fieldcount] (get-in sstate [:bank :field])]
        (repeat fieldcount fieldtype))
       concat
       flatten
       shuffle
       (take 4)))

(def role-descriptions {:captain "Goods for VP"
                        :trader "Goods for money"
                        :builder "Construct"
                        :settler "Get q/field"
                        :mayor "Get workers"
                        :craftsman "Produce goods"
                        :prospector "$"})


(def initial-buildings
  {:small-indigo-maker {:gold 1
                        :vp 1
                        :worker 1
                        :resource :indigo
                        :count 4
                        :column 1

                        }
   :large-indigo-maker {:gold 3
                        :vp 2
                        :worker 3
                        :resource :indigo
                        :count 3
                        :column 2
                        }
   :small-sugar-maker {:gold 1
                       :vp 1
                       :worker 1
                       :resource :sugar
                       :count 4
                       :column 1
                       }
   :large-sugar-maker {:gold 4
                       :vp 2
                       :worker 3
                       :resource :sugar
                       :count 3
                       :column 2
                       }
   :tobacco-maker {:gold 5
                   :vp 3
                   :worker 3
                   :resource :tobacco
                   :count 3
                   :column 3
                   }
   :coffee-maker {:gold 6
                  :vp 3
                  :worker 2
                  :resource :coffee
                  :count 3
                  :column 3
                  }
   :small-market {:gold 1
                  :vp 1
                  :worker 1
                  :count 2
                  :column 1
                  :description "When the owner of an occupied small market sells a barrel in the trader phase, he gets an extra doubloon from the bank for it."
                  }
   :large-market {:gold 5
                  :vp 2
                  :worker 1
                  :count 2
                  :column 2
                  :description "When the owner of an occupied large market sells a good in the trader phase, he gets an extra 2 doubloons from the bank for it."
                  }
   :hospice {:gold 4
             :vp 2
             :worker 1
             :count 2
             :column 2
             :description "During the settler phase, when the owner of an occupied hospice places a plantation or quarry tile on his island, he may take a colonist from the colonist supply and place it on this tile. "
             }
   :office {:gold 5
            :vp 2
            :worker 1
            :count 2
            :column 2
            :description "When the owner of an occupied office sells a good to the trading house in the trader phase, it need not be different than the goods already there. If the trading house is full, the player cannot sell a good there!"
            }
   :factory {:gold 8
             :vp 3
             :worker 1
             :count 2
             :column 3
             :description "If the owner of an occupied factory produces goods of more than one kind in the craftsman phase, he earns money from the bank: for two kinds of goods, he earns 1 doubloon, for three kinds of goods, he earns 2 doubloons, for four kinds of goods, he earns 3 doubloons, and for all five kinds of goods, he earns 5 doubloons. The number of barrels produced plays no role."
             }
   :university {:gold 7
                :vp 3
                :worker 1
                :count 2
                :column 3
                :description "During the builder phase, when the owner of an occupied university builds a building in his city, he may take a colonist from the colonist supply and place it on this tile."
                }
   :hacienda {:gold 2
              :vp 1
              :worker 1
              :count 2
              :column 1
              :description "On his turn in the settler phase, the owner of an occupied hacienda may, before he takes a face-up plantation tile, take an additional tile chosen at random from the rest of the supply. He does this by clicking on the top right corner of the hacienda."
              }
   :construction-hut {:gold 2
                      :vp 1
                      :worker 1
                      :count 2
                      :column 1
                      :description "In the settler phase, the owner of an occupied construction hut, can place a quarry on his island of one of the face-up plantation tiles."
                      }
   :harbor {:gold 8
            :vp 3
            :worker 1
            :count 2
            :column 3
            :description "Each time, during the captain phase, the owner of an occupied harbour loads goods on a cargo ship, he earns one extra victory point."
            }
   :wharf {:gold 9
           :vp 3
           :worker 1
           :count 2
           :column 3
           :description "During the captain phase, when a player with an occupied wharf must load goods, instead of loading them on a cargo ship, he may place all goods of one kind in the goods supply and score the appropriate victory points as though he has loaded them on a cargo ship. It is as though the player has an imaginary ship with unlimited capacity at his disposal."
           }
   :small-warehouse {:gold 3
                     :vp 1
                     :worker 1
                     :count 2
                     :column 1
                     :description "The owner of an occupied small warehouse may store, at the end of the captain phase, in addition to the single goods barrel he is allowed to store on his windrose, all the barrels of one kind of goods that he chooses."
                     }
   :large-warehouse {:gold 6
                     :vp 2
                     :worker 1
                     :count 2
                     :column 2
                     :description "The owner of an occupied large warehouse may store, at the end of the captain phase, in addition to the single goods barrel he is allowed to store on his wind rose, all the barrels of two kinds of goods that he chooses."
                     }
   :guild-hall {:gold 10
                :vp 4
                :worker 1
                :count 1
                :column 4
                :description "The owner of the occupied guild hall earns, at game end, an additional 1 VP for each small production building (occupied or unoccupied) in his city (= small indigo plant and small sugar mill), and an additional 2 VP for each large production building (occupied or unoccupied) in his city (= indigo plant, sugar mill, tobacco storage, and coffee roaster)."
                }
   :residence {:gold 10
               :vp 4
               :worker 1
               :count 1
               :column 4
               :description "The owner of the occupied residence earns, at game end, additional victory points for the plantations and quarries he has placed on his island. For up to nine filled island spaces, he earns 4 VP, for ten filled island spaces, he earns 5 VP, for eleven filled island spaces, he earns 6 VP, and for all twelve spaces filled, he earns 7 VP."
               }
   :customs-house {:gold 10
                   :vp 4
                   :worker 1
                   :count 1
                   :column 4
                   :description "The owner of the occupied customs house earns, at game end, one additional victory point for every four victory points he acquired during the game. The player should only count his victory point chips (and any extra victory points recorded on paper after the chip supply was exhausted, but before game end). He does not use victory points earned for his buildings at game end."
                   }
   :city-hall {:gold 10
               :vp 4
               :worker 1
               :count 1
               :column 4
               :description "The owner of the occupied city hall earns, at game end, one additional victory point for each violet building and each green building(occupied or unoccupied) in his city."
               }
   :fortress {:gold 10
              :vp 4
              :worker 1
              :count 1
              :column 4
              :description "The owner of the occupied fortress earns, at game end, one additional victory point for every three colonists on his player board."
              }
   })
