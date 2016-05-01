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

(defmethod read :graph/lines
  [{:keys [state query]} key _]
  (let [st @state]
    {:value (om/db->tree query (get st key) st)}))

(defmethod read :graph/selected-lines
  [{:keys [state query]} key _]
  (let [st @state]
    {:value (om/db->tree query (get st key) st)}))

(defmethod read :app/customers
  [{:keys [state query]} key _]
  (let [st @state]
    {:value (om/db->tree query (get st key) st)}))

;;
;; "Only need to add or remove from the :graph/selected-lines refs mapentry"
;; (pprint (get @state :graph/selected-lines))
;;
(defmethod mutate 'graph/select-line
  [{:keys [state]} _ {:keys [want-to-select? id]}]
  {:action #(let [ident [:line/by-id id]]
             (if want-to-select?
               (swap! state update :graph/selected-lines (fn [st] (-> st
                                                                      (conj ident))))
               (swap! state update :graph/selected-lines (fn [lines] (vec (remove #{ident} lines))))))})

(def init-state
  {:graph/selected-lines
   [{:id 100}
    {:id 101}]
   :graph/lines
   [{:id   100
     :line-name "Methane"}
    {:id   101
     :line-name "Oxygen"}
    {:id   102
     :line-name "Carbon Dioxide"}
    {:id   103
     :line-name "Carbon Monoxide"}
    ]
   :app/customers
   [{:id 200
     :first-name "Greg"}
    {:id 201
     :first-name "Sally"}
    {:id 202
     :first-name "Ben"}
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

(defui FakeGraph
  Object
  (render [this]
    (println "Rendering the FakeGraph")
    (let [props (om/props this)
          selected-names (map :line-name props)]
      (dom/h2 #js{:className "fake-graph"} (apply str "GRAPH: " (interpose ", " selected-names))))))
(def fake-graph (om/factory FakeGraph))

(defui Checkbox
  static om/Ident
  (ident [this props]
    [:line/by-id (:id props)])
  static om/IQuery
  (query [this]
    [:id :line-name])
  Object
  (render [this]
    (let [{:keys [id line-name]} (om/props this)
          {:keys [selected?]} (om/get-computed this)
          _ (println "Rendering cb:" id "when selected is:" selected?)]
      (dom/div nil
               (dom/input #js{:type    "checkbox"
                              :className "xlarge"
                              :checked selected?
                              :onClick (fn [e]
                                         (let [action (.. e -target -checked)]
                                           (println "Pressed so attempting to set to:" action)
                                           (om/transact! this `[(graph/select-line {:want-to-select? ~action :id ~id}) :app/customers])))})
               (dom/label #js{:className "xlarge"} (dom/h2 #js{:className "side higher-text"} line-name))))))
(def checkbox (om/factory Checkbox {:keyfn :id}))

(defui Customer
  static om/Ident
  (ident [this props]
    [:customer/by-id (:id props)])
  static om/IQuery
  (query [this]
    [:id :first-name]))

(def my-reconciler
  (om/reconciler {:normalize true ;; -> documentation
                  :state     init-state
                  :parser    parser}))

(defui Root
  static om/IQuery
  (query [this]
    [{:graph/lines (om/get-query Checkbox)}
     {:graph/selected-lines (om/get-query Checkbox)}
     {:app/customers (om/get-query Customer)}
     ])
  Object
  (render [this]
    (println "Rendering 'demo.checkboxes' from Root")
    (let [{:keys [graph/lines graph/selected-lines]} (om/props this)]
      (dom/div #js{:className "container"}
               (check-default-db @my-reconciler)
               (dom/div nil
                        (for [line lines
                              :let [selected? (boolean (some #{line} selected-lines))]]
                          (checkbox (om/computed line {:selected? selected?}))))
               (dom/div nil
                        (dom/br nil)
                        (fake-graph selected-lines)
                        #_(dom/br nil)
                        #_(dom/br nil)
                        (help/any-action {:text "Show State" :action #(pprint @my-reconciler)})
                        #_(dom/br nil))
               #_(help/any-action {:text "Add Selection" :action #(help/mutate help/norm-state true 102)})
               #_(help/any-action {:text "Remove Selection" :action #(help/mutate help/norm-state false 100)})
               ))))

(defn run []
  (om/add-root! my-reconciler
                Root
                (.. js/document (getElementById "main-app-area"))))
