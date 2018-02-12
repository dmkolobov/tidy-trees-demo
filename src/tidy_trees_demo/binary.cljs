(ns tidy-trees-demo.binary
  (:require [tidy-trees.reagent :refer [tidy-tree]]
            [tidy-trees-demo.codemirror :refer [code]]
            [re-frame.core :refer [reg-event-db dispatch subscribe reg-sub]]
            [re-com.core :refer [button h-box v-box box]]
            [clojure.string :as string]
            [clojure.repl :as repl]))

;; -------- binary trees --------------------------------------------

(defn insert
  [tree x]
  (cond (nil? tree)    [x nil nil]
        (< x (tree 0)) (update tree 1 insert x)
        :default       (update tree 2 insert x)))

(defn binary-branch?
  [bt]
  (some some? (rest bt)))

(def binary-children
  rest)

(defn label
  [bt]
  [:span.bin-tree (first bt)])

(defn draw-binary-tree
  "Inserts numbers into a binary tree and draws the resulting tree."
  [numbers {:keys [v-gap h-gap edge-style]}]
  [tidy-tree (reduce insert nil numbers)
             {:branch?      binary-branch?
              :children     binary-children
              :label-branch label
              :label-term   label
              :v-gap        v-gap
              :h-gap        h-gap
              :edge-style   edge-style}])

;; -------- reagent -------------------------------------------------

(def balanced [7 3 1 2 0 5 6 4 11 9 8 10 13 12 14])

(defn numbers [db] (or (::numbers db) balanced))

(reg-sub ::numbers numbers)

(reg-event-db ::shuffle
              (fn [db] (assoc db ::numbers (shuffle (numbers db)))))

(defn shuffle! [] (dispatch [::shuffle]))

(defn shuffle-button
  []
  [button :label    "shuffle"
          :class    "btn-primary"
          :on-click shuffle!])

(defn example-source
  []
  (with-out-str
    (repl/source insert)
    (println)
    (repl/source binary-branch?)
    (println)
    (repl/source binary-children)
    (println)
    (repl/source label)
    (println)
    (repl/source draw-binary-tree)))

(defn controls
  [nums]
  [h-box :align    :center
         :children [[box :child [code (str nums)]]
                        [shuffle-button]]])

(defn app
  [_]
  (let [nums (subscribe [::numbers])]
    (fn [opts]
      (let [nums @nums]
        [v-box :align    :center
               :gap      "1em"
               :children [[controls nums]
                          [draw-binary-tree nums opts]]]))))