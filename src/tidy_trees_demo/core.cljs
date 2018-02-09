(ns tidy-trees-demo.core
    (:require [reagent.core :as reagent :refer [atom]]
              [re-frame.core :refer [reg-event-db reg-sub subscribe dispatch]]
              [cljs.tools.reader.edn :refer [read-string]]
              [tidy-trees.reagent :refer [tidy-tree]]))

(enable-console-print!)

(reg-event-db
  ::read-source
  (fn [db _] (assoc db ::tree (read-string (::src db)))))

(reg-event-db
  ::save-source
  (fn [db [_ src]] (assoc db ::src src)))

(reg-sub
  ::tree
  (fn [db] (get db ::tree "...")))

(defn hiccup-editor
  []
  [:div.editor
   [:a.draw-button {:href "#" :on-click #(dispatch [::read-source])} "draw"]
   [:textarea.editor-text
    {:on-change #(dispatch [::save-source (.-value (.-target %))])}
    "[λ t i d [y t r e e s]]"]])

(defn tree-ide
  []
  (let [tree (subscribe [::tree])]
    (fn []
      [:div.ide
       [hiccup-editor]
       [tidy-tree @tree
        {:branch?  vector?
         :children rest

         :label-branch (comp str first)
         :label-term   str

         :v-gap    10
         :h-gap    20}]])))

(reagent/render-component [tree-ide]
                          (. js/document (getElementById "app")))



(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
)
