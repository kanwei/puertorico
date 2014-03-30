(ns puertorico.client
  (:require [reagent.core :as reagent :refer [atom]]))

(def buildings
  {:small-indigo-maker {:cost 1
    :points 1
    :workers 1
    :resource :indigo
    :count 4
}
:large-indigo-maker {:cost 3
    :points 2
    :workers 3
    :resource :indigo
    :count 3
}
:small-sugar-maker {:cost 1
    :points 1
    :workers 1
    :resource :sugar
    :count 4
}
:large-sugar-maker {:cost 4
    :points 2
    :workers 3
    :resource :sugar
    :count 3
}
:tobacco-maker {:cost 5
    :points 3
    :workers 3
    :resource :tobacco
    :count 3
}
:coffee-maker {:cost 6
    :points 3
    :workers 2
    :resource :tobacco
    :count 3
}
:small-market {:cost 1
    :points 1
    :workers 1
    :count 2
}
:large-market {:cost 5
    :points 2
    :workers 1
    :count 2
}
:hospice {:cost 4
    :points 2
    :workers 1
    :count 2
}
:office {:cost 5
    :points 2
    :workers 1
    :count 2
}
:factory {:cost 8
    :points 3
    :workers 1
    :count 2
}
:university {:cost 7
    :points 3
    :workers 1
    :count 2
}
:hacienda {:cost 2
    :points 1
    :workers 1
    :count 2
}
:construction-hut {:cost 2
    :points 1
    :workers 1
    :count 2
}
:harbor {:cost 8
    :points 3
    :workers 1
    :count 2
}
:wharf {:cost 9
    :points 3
    :workers 1
    :count 2
}
:small-warehouse {:cost 3
    :points 1
    :workers 1
    :count 2
}
:large-warehouse {:cost 6
    :points 2
    :workers 1
    :count 2
}
:guild-hall {:cost 10
    :points 4
    :workers 1
    :count 1
}
:residence {:cost 10
    :points 4
    :workers 1
    :count 1
}
:customs-house {:cost 10
    :points 4
    :workers 1
    :count 1
}
:city-hall {:cost 10
    :points 4
    :workers 1
    :count 1
}
:fortress {:cost 10
    :points 4
    :workers 1
    :count 1
}
})
(defn building-tile [& {:keys [name type workers]}]
  [:div.building
   name])

(defn some-component []
  [:div
   [building-tile :name "Hello"]
   [:p.someclass 
    "I have " [:strong "bold"]
    [:span {:style {:color "red"}} " and red"]
    " text."]])

(defn game-board []
  [:div.row
   [:div.col-md-4
    "Testing"]
   [:div.col-md-4
    "Testing"]
   [:div.col-md-4
    "Testing"]])

(reagent/render-component [some-component] (.getElementById js/document "app"))
(reagent/render-component [game-board] (.getElementById js/document "gameboards"))
