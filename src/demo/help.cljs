(ns demo.help
  (:require [cljs.pprint :refer [pprint]]
            [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]
            [clojure.string :as str]))

(defn probe [msg obj]
  (println (str (str/upper-case msg) ":\n" obj))
  obj)

(def norm-state (atom {:graph/selected-lines [[:line/by-id 100] [:line/by-id 101]],
                       :graph/lines
                                             [[:line/by-id 100]
                                              [:line/by-id 101]
                                              [:line/by-id 102]
                                              [:line/by-id 103]],
                       :line/by-id
                                             {100 {:id 100, :name "Methane"},
                                              101 {:id 101, :name "Oxygen"},
                                              102 {:id 102, :name "Carbon Dioxide"},
                                              103 {:id 103, :name "Carbon Monoxide"}}}))

(defui AnyAction
       Object
       (render [this]
               (let [{:keys [text action]} (om/props this)]
                 (dom/button #js{:onClick action} text))))
(def any-action (om/factory AnyAction))

(defn mutate [state want-to-select? id]
  (let [ident [:line/by-id id]]
    (if want-to-select?
      (swap! state update :graph/selected-lines (fn [st] (as-> st $
                                                               (probe "init" $)
                                                               (conj $ ident)
                                                               (probe "post conj" $))))
      (swap! state update :graph/selected-lines (fn [lines] (vec (remove #{ident} lines))))))
  (pprint @state))
