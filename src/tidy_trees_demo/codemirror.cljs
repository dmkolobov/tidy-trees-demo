(ns tidy-trees-demo.codemirror
  (:require [reagent.core :refer [create-class dom-node]]
            [clojure.walk :refer [stringify-keys]]

            [cljsjs.codemirror]
            [cljsjs.codemirror.mode.clojure]
            [cljsjs.codemirror.addon.edit.matchbrackets]
            [cljsjs.codemirror.addon.runmode.runmode]
            [cljsjs.codemirror.addon.runmode.colorize]))

(def default-opts
  {"mode"          "clojure"
   "theme"         "panda-fork"
   "lineNumbers"   true
   "matchBrackets" true})

(defn code-editor
  [& {:keys [source on-change]}]
  (create-class
    {:component-did-mount
     (fn [owner]
       (let [dom (dom-node owner)
             mir (.fromTextArea js/CodeMirror dom (clj->js default-opts))]
         (.on mir "change" #(on-change (.getValue mir)))))
     :reagent-render
     (fn [& _]
       [:text-area {:default-value source}])}))

(defn code
  [_]
  (create-class
    {:component-did-mount #(.colorize js/CodeMirror)
     :reagent-render      (fn [source]
                            [:pre {:class "cm-s-panda-fork"
                                   :data-lang "clojure"}
                             source])}))