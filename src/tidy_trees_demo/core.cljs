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
              [cljsjs.codemirror.addon.edit.matchbrackets]
              [cljsjs.codemirror.addon.runmode.runmode]
              [cljsjs.codemirror.addon.runmode.colorize]))

(enable-console-print!)

(def callout "[parent \n [child-1 c1.x c1.y [c1.nums 1 \n                             2 \n                             3]]\n [child-2 c2.x c2.y [c2.nums 4 \n                             5 \n                             6]]]")

(reg-event-db
  ::read-source
  (fn [db _]
    (let [src (::src db)]
      (-> db
          (assoc ::prev-src src)
          (assoc ::tree (read-string src))))))

(reg-event-db
  ::save-source
  (fn [db [_ src]] (assoc db ::src src)))

(reg-sub
  ::tree
  (fn [db] (get db ::tree "...")))


(reg-sub
  ::disable-draw?
  (fn [db] (= (::src db) (::prev-src db))))

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
  (let [disable? (subscribe [::disable-draw?])]
    (fn []
      [button :label "draw"
          :class "draw-button btn-primary"
          :on-click #(dispatch [::read-source])
          :disabled? @disable?])))

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
  [:div.tree-editor
   [code-mirror]
   [draw-button]])

(defn ide
  []
  (let [tree  (subscribe [::tree])
        h-gap (subscribe [::opt :h-gap])
        v-gap (subscribe [::opt :v-gap])]
    (fn []
      [v-box :align    :center
             :children [(when-let [tree @tree] [box :child [hiccup-tree tree @h-gap @v-gap]])
                        [box :width "100%" :child [tree-editor]]]])))

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
  (fn [] [ide]))

(reagent/render-component [opt-slider :v-gap] (. js/document (getElementById "v-gap")))
(reagent/render-component [opt-slider :h-gap] (. js/document (getElementById "h-gap")))

(reagent/render-component [tree-ide] (. js/document (getElementById "app")))

(.colorize js/CodeMirror (array (. js/document (getElementById "usage"))) "clojure")
;
(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
)
