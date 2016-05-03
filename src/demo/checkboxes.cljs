(ns demo.checkboxes
  (:require [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]
            [default-db-format.core :as db-format]
            [cljs.pprint :refer [pprint]]
            [demo.help :as help]))

(enable-console-print!)

(defmulti read om/dispatch)
(defmulti mutate om/dispatch)

(def parser
  (om/parser {:read   read
              :mutate mutate}))

(defmethod read :graph/graph-lines
  [{:keys [state query]} key _]
  (let [st @state]
    {:value (om/db->tree query (get st key) st)}))

(defmethod read :graph/fake-graph
  [{:keys [state query]} key _]
  (let [st @state]
    {:value (om/db->tree query (get st key) st)}))

(defmethod read :app/app-lines
  [{:keys [state query]} key _]
  (let [st @state]
    {:value (om/db->tree query (get st key) st)}))

(defmethod read :app/customers
  [{:keys [state query]} key _]
  (let [st @state]
    {:value (om/db->tree query (get st key) st)}))

(defn update-graph-lines [st want-to-select? id]
  (let [graph-lines [:fake-graph/by-id 1000 :graph/graph-lines]]
    (if want-to-select?
      (update-in st graph-lines (fn [v] (vec (conj v [:line/by-id id]))))
      (update-in st graph-lines (fn [v] (vec (remove #{[:line/by-id id]} v)))))))

(defn update-lines
  [last-state {:keys [want-to-select? id]}]
  (-> last-state
      (update-in [:line/by-id id] assoc :selected? want-to-select?)
      (update-graph-lines want-to-select? id)))

(defmethod mutate 'graph/select-line
  [{:keys [state]} _ params]
  {:action #(swap! state update-lines params)})

(def init-state
  {:graph/fake-graph {:id 1000
                      :graph/graph-lines [{:id 100}]}
   :app/app-lines
   [{:id   100
     :line-name "Methane"
     :selected? true}
    {:id   101
     :line-name "Oxygen"
     :selected? false}
    {:id   102
     :line-name "Carbon Dioxide"
     :selected? false}
    {:id   103
     :line-name "Carbon Monoxide"
     :selected? false}
    ]
   }
  )

(defn check-default-db [st]
  (let [version db-format/version
        check-result (db-format/check st)
        ok? (db-format/ok? check-result)
        msg-boiler (str "normalized (default-db-format ver: " version ")")
        message (if ok?
                  (str "GOOD: state fully " msg-boiler)
                  (str "BAD: state not fully " msg-boiler))]
    (println message)
    (when (not ok?)
      (pprint st)
      (db-format/show-hud check-result))))

;; This component isn't essential - we could have used Checkbox in FakeGraph
;; They both have the same ident
(defui Line
  static om/Ident
  (ident [this props]
    [:line/by-id (:id props)])
  static om/IQuery
  (query [this]
    [:id :line-name]))

(defui Checkbox
  static om/Ident
  (ident [this props]
    [:line/by-id (:id props)])
  static om/IQuery
  (query [this]
    [:id :line-name :selected?])
  Object
  (render [this]
    (let [{:keys [id line-name selected?]} (om/props this)
          _ (println "Rendering cb:" id "when selected is:" selected?)]
      (dom/div nil
               (dom/input #js{:type      "checkbox"
                              :className "xlarge"
                              :checked   (boolean selected?)
                              :onClick   (fn [e]
                                           (let [action (.. e -target -checked)]
                                             (println "Pressed so attempting to set to:" action)
                                             (om/transact! this `[(graph/select-line {:want-to-select? ~action :id ~id}) :graph/graph-lines])))})
               (dom/label #js{:className "xlarge"} (dom/h2 #js{:className "side higher-text"} line-name))))))
(def checkbox (om/factory Checkbox {:keyfn :id}))

(defui FakeGraph
  static om/Ident
  (ident [this props]
    [:fake-graph/by-id (:id props)])
  static om/IQuery
  (query [this]
    [:id {:graph/graph-lines (om/get-query Line)}])
  Object
  (render [this]
    (let [{:keys [graph/graph-lines]} (om/props this)]
      (dom/h2 #js{:className "fake-graph"} (apply str "GRAPH: " (interpose ", " (map :line-name graph-lines)))))))
(def fake-graph-component (om/factory FakeGraph))

(def my-reconciler
  (om/reconciler {:normalize true ;; -> documentation
                  :state     init-state
                  :parser    parser}))

(defui Root
  static om/IQuery
  (query [this]
    [{:app/app-lines (om/get-query Checkbox)}
     {:graph/fake-graph (om/get-query FakeGraph)}
     {:graph/graph-lines (om/get-query Line)}
     ])
  Object
  (render [this]
    (println "Rendering 'demo.checkboxes' from Root")
    (let [{:keys [app/app-lines graph/fake-graph]} (om/props this)]
      (dom/div nil
               (dom/div #js{:className "container"}
                        (check-default-db @my-reconciler)
                        (dom/div nil
                                 (map checkbox app-lines))
                        (dom/div nil
                                 (dom/br nil)
                                 (fake-graph-component fake-graph)
                                 #_(dom/br nil)
                                 #_(dom/br nil)
                                 (help/any-action {:text "Show State" :action #(pprint @my-reconciler)})
                                 #_(dom/br nil))
                        #_(help/any-action {:text "Add Selection" :action #(help/mutate help/norm-state true 102)})
                        #_(help/any-action {:text "Remove Selection" :action #(help/mutate help/norm-state false 100)})
                        )))))

(defn run []
  (om/add-root! my-reconciler
                Root
                (.. js/document (getElementById "main-app-area"))))
