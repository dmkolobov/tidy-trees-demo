(ns tidy-trees-demo.core
    (:require [reagent.core :as reagent :refer [atom]]
              [re-frame.core :refer [reg-event-db reg-sub subscribe dispatch]]
              [cljs.tools.reader.edn :refer [read-string]]
              [tidy-trees.reagent :refer [tidy-tree]]

              [re-com.core :refer [button
                                   slider
                                   label
                                   box
                                   h-box
                                   v-box
                                   radio-button]]

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
                                  {"mode"         "clojure"
                                   "theme"        "panda-syntax"
                                   "lineNumbers"   true
                                   "matchBrackets" true}))]
         (.on mir
              "change"
              (fn [_] (dispatch [::save-source (.getValue mir)])))))
     :reagent-render
     (fn [] [:textarea {:default-value callout}])}))

(defn draw-button
  []
  [button :label "draw"
          :on-click #(dispatch [::read-source])])

(defn hiccup-tree
  [tree h-gap v-gap]
  [tidy-tree tree
   {:branch?      vector?
    :children     rest
    :label-branch (comp str first)
    :label-term   str
    :v-gap        v-gap
    :h-gap        h-gap
    :edges        :straight}])

(reg-sub
  ::opt
  (fn [db [_ id]]
    (get-in db [::opt id] 10)))

(reg-event-db
  ::set-opt
  (fn [db [_ id val]]
    (assoc-in db [::opt id] val)))

(defn tree-editor
  []
  (let [tree  (subscribe [::tree])
        h-gap (subscribe [::opt :h-gap])
        v-gap (subscribe [::opt :v-gap])]
    (fn []
      [v-box :align    :center
             :children [[box :width "100%" :child [code-mirror]]
                        [draw-button]
                        (when-let [tree @tree] [box :child [hiccup-tree tree @h-gap @v-gap]])]])))

(defn opt-slider
  [id]
  (let [model (subscribe [::opt id])]
    (fn [_]
      [slider :model     @model
              :step      10
              :on-change #(dispatch [::set-opt id %])])))

(defn tree-ide
  []
  (dispatch [::save-source callout])
  (dispatch [::read-source])
  (fn [] [tree-editor]))

(reagent/render-component [opt-slider :v-gap] (. js/document (getElementById "v-gap")))
(reagent/render-component [opt-slider :h-gap] (. js/document (getElementById "h-gap")))

(reagent/render-component [tree-ide] (. js/document (getElementById "app")))



(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
)
