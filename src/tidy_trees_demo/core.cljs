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

              [tidy-trees-demo.binary :as bin]
              [tidy-trees-demo.codemirror :refer [code code-editor]]

              [cljsjs.codemirror]
              [cljsjs.codemirror.mode.clojure]
              [cljsjs.codemirror.addon.edit.matchbrackets]
              [cljsjs.codemirror.addon.runmode.runmode]
              [cljsjs.codemirror.addon.runmode.colorize]))

(enable-console-print!)

(def callout "[:parent \n [:child-1 c1-x \n  \t\t   c1-y \n           [:nums 1 2 3]]\n [:child-2 c2-x \n           c2-y\n           [:nums 4 [5 55 555 5555] 6]]]")

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
                                   "theme"        "panda-fork"
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
  []
  (let [tree (subscribe [::tree])
        opts (subscribe [::opts])]
    (fn []
      (let [tree @tree
            {:keys [v-gap h-gap edge-style]} @opts]
      [tidy-tree tree
       {:branch?      vector?
        :children     rest
        :label-branch (comp #(into [:div] %) str first)
        :label-term   (comp #(into [:div] %) str)
        :v-gap        v-gap
        :h-gap        h-gap
        :edge-style   edge-style}]))))

(def def-opt {:v-gap 20 :h-gap 10 :edge-style :straight})

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
  (dispatch [::save-source callout])
  (dispatch [::read-source])
  (fn []
    [:div.tree-editor
     [code-mirror]
     [draw-button]]))

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

(defn binary
  []
  (let [opts (subscribe [::opts])]
    (fn []
      [bin/app @opts])))

(reagent/render-component [opt-slider :v-gap] (. js/document (getElementById "v-gap")))
(reagent/render-component [opt-slider :h-gap] (. js/document (getElementById "h-gap")))
(reagent/render-component [edge-control] (. js/document (getElementById "edge-style")))


(reagent/render-component [tree-editor] (. js/document (getElementById "hiccup-edit")))
(reagent/render-component [hiccup-tree] (. js/document (getElementById "hiccup-draw")))

(reagent/render-component [binary] (. js/document (getElementById "binary")))

(.colorize js/CodeMirror)

(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
)
