(ns tidy-trees-demo.binary)

(defn branch?
  [[_ l r]]
  (or (some? l)
      (some? r)))

(defn children
  [[_ l r]]
  [l r])

(defn label
  [[x _ _]]
  x)

(defn insert
  [tree new-x]
  (if-not tree
    [new-x nil nil]
    (let [[x l r] tree]
      (if (< new-x x)
        [x (insert l new-x) r]
        [x l (insert r new-x)]))))