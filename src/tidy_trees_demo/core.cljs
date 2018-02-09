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

(def callout "[parent \n [\"child\\none\" c1.x c1.y [c1.nums 1 \n                             2 \n                             3]]\n [child-2 c2.x c2.y [c2.nums 4 \n                             5 \n                             6]]]")

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
  [tree {:keys [h-gap v-gap edge-style]}]
  [tidy-tree tree
   {:branch?      vector?
    :children     rest
    :label-branch (comp str first)
    :label-term   str
    :v-gap        v-gap
    :h-gap        h-gap
    :edge-style        edge-style}])

(def def-opt {:v-gap 10 :h-gap 10 :edge-style :straight})

(reg-sub
  ::opt
  (fn [db [_ id]]
    (get-in db [::opt id] (get def-opt id))))

(reg-event-db
  ::set-opt
  (fn [db [_ id val]]
    (update db
            ::opt
            (fn [opts]
              (assoc (merge def-opt opts) id val)))))

(reg-sub
  ::opts
  (fn [db [_]] (get db ::opt def-opt)))

(defn tree-editor
  []
  [:div.tree-editor
   [code-mirror]
   [draw-button]])

(defn ide
  []
  (let [tree  (subscribe [::tree])
        opts  (subscribe [::opts])]
    (fn []
      [v-box :align    :center
             :children [(when-let [tree @tree] [box :child [hiccup-tree tree @opts]])
                        [box :width "100%" :child [tree-editor]]]])))

(defn opt-slider
  [id]
  (let [model (subscribe [::opt id])]
    (fn [_]
      [slider :model     @model
              :step      10
              :on-change #(dispatch [::set-opt id %])])))

(defn edge-control
  []
  (let [model (subscribe [::opt :edge-style])]
    (fn []
      [v-box :children [[radio-button :label ":straight"
                                      :value :straight
                                      :model @model
                                      :on-change #(dispatch [::set-opt :edge-style :straight])]
                        [radio-button :label ":diagonal"
                                      :value :diagonal
                                      :model @model
                                      :on-change #(dispatch [::set-opt :edge-style :diagonal])]]])))

(defn tree-ide
  []
  (dispatch [::save-source callout])
  (dispatch [::read-source])
  (fn [] [ide]))

(reagent/render-component [opt-slider :v-gap] (. js/document (getElementById "v-gap")))
(reagent/render-component [opt-slider :h-gap] (. js/document (getElementById "h-gap")))
(reagent/render-component [edge-control] (. js/document (getElementById "edge-style")))

(reagent/render-component [tree-ide] (. js/document (getElementById "app")))

(.colorize js/CodeMirror (array (. js/document (getElementById "usage"))) "clojure")
;
(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
)
