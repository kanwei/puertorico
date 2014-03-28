(ns puertorico.client
  (:require [reagent.core :as reagent :refer [atom]]))

(defn some-component []
  [:div
   [:h3 "I am a component!"]
   [:p.someclass 
    "I have " [:strong "bold"]
    [:span {:style {:color "red"}} " and red"]
    " text."]])

(reagent/render-component [some-component] (.getElementById js/document "app"))
