(ns tidy-trees-demo.core
    (:require [reagent.core :as reagent :refer [atom]]
              [re-frame.core :refer [reg-event-db reg-sub subscribe dispatch]]
              [cljs.tools.reader.edn :refer [read-string]]
              [tidy-trees.reagent :refer [tidy-tree]]

              [cljsjs.codemirror]
              [cljsjs.codemirror.mode.clojure]
              [cljsjs.codemirror.addon.edit.matchbrackets]))

(enable-console-print!)

(def callout "[parent \n [child-1 c1.x c1.y [c1.nums 1 \n                             2 \n                             3]]\n [child-2 c2.x c2.y [c2.nums 4 \n                             5 \n                             6]]]")

(reg-event-db
  ::read-source
  (fn [db _] (assoc db ::tree (read-string (::src db)))))

(reg-event-db
  ::save-source
  (fn [db [_ src]] (println src)(assoc db ::src src)))

(reg-sub
  ::tree
  (fn [db] (get db ::tree "...")))

(defn code-mirror
  []
  (reagent/create-class
    {:component-did-mount
     (fn [owner]
       (let [dom (reagent/dom-node owner)
             mir (.fromTextArea js/CodeMirror
                                dom
                                (clj->js
                                  {"value" callout
                                   "mode"  "clojure"
                                   "theme" "panda-syntax"
                                   "lineNumbers" true
                                   "matchBrackets" true}))]
         (.on mir
              "change"
              (fn [_] (dispatch [::save-source (.getValue mir)])))))
     :reagent-render
     (fn [] [:textarea {:default-value callout
                        :rows 2}])}))

(defn hiccup-editor
  []
  [:div.editor
   [:a.draw-button {:href "#" :on-click #(dispatch [::read-source])} "draw"]
   [code-mirror]])

(defn tree-ide
  []
  (let [tree (subscribe [::tree])]

    (dispatch [::save-source callout])
    (dispatch [::read-source])

    (fn []
      [:div.ide
       [hiccup-editor]
       (when-let [tree @tree]
         [tidy-tree tree
          {:branch?  vector?
           :children rest

           :label-branch (comp str first)
           :label-term   str

           :v-gap    10
           :h-gap    20}])])))

(reagent/render-component [tree-ide]
                          (. js/document (getElementById "app")))



(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
)
