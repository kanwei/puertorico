(ns puertorico.client
  (:require [reagent.core :as reagent :refer [atom]]))

(def buildings
  {:small-indigo-maker {:cost 1
    :points 1
    :workers 1
    :resource :indigo
}
:large-indigo-maker {:cost 3
    :points 2
    :workers 3
    :resource :indigo
}
:small-sugar-maker {:cost 1
    :points 1
    :workers 1
    :resource :sugar
}
:large-sugar-maker {:cost 4
    :points 2
    :workers 3
    :resource :sugar
}
:tobacco-maker {:cost 5
    :points 3
    :workers 3
    :resource :tobacco
}
:coffee-maker {:cost 6
    :points 3
    :workers 2
    :resource :tobacco
}
:small-market {:cost 1
    :points 1
    :workers 1
}
:large-market {:cost 5
    :points 2
    :workers 1
}
:hospice {:cost 4
    :points 2
    :workers 1
}
:office {:cost 5
    :points 2
    :workers 1
}
:factory {:cost 8
    :points 3
    :workers 1
}
:university {:cost 7
    :points 3
    :workers 1
}
:hacienda {:cost 2
    :points 1
    :workers 1
}
:construction-hut {:cost 2
    :points 1
    :workers 1
}
:harbor {:cost 8
    :points 3
    :workers 1
}
:wharf {:cost 9
    :points 3
    :workers 1
}
:small-warehouse {:cost 3
    :points 1
    :workers 1
}
:large-warehouse {:cost 6
    :points 2
    :workers 1
}
:guild-hall {:cost 10
    :points 4
    :workers 1
}
:residence {:cost 10
    :points 4
    :workers 1
}
:customs-house {:cost 10
    :points 4
    :workers 1
}
:city-hall {:cost 10
    :points 4
    :workers 1
}
:fortress {:cost 10
    :points 4
    :workers 1
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



(reagent/render-component [some-component] (.getElementById js/document "app"))
