(ns learnit.core
  (:require-macros [secretary.core :refer [defroute]])
  (:require [goog.events :as events]
            [reagent.core :as reagent]
            [secretary.core :as secretary]
            [precept.core :refer [start! then]]
            [learnit.views :as views]
            [learnit.facts :refer [todo visibility-filter]]
            [learnit.rules :refer [app-session]]
            [learnit.schema :refer [db-schema]])
  (:import [goog History]
           [goog.history EventType]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Initialize App

(defn dev-setup []
  (when ^boolean js/goog.DEBUG
    (enable-console-print!)
    (println "dev mode")))

(defroute "/" [] (then (visibility-filter :all)))

(defroute "/:filter" [filter] (then (visibility-filter (keyword filter))))

(def history
  (doto (History.)
    (events/listen EventType.NAVIGATE (fn [event] (secretary/dispatch! (.-token event))))
    (.setEnabled true)))

(def facts (into (todo "Hi") (todo "there!")))

(defn reload []
  (reagent/render [views/app]
                  (.getElementById js/document "app")))

(defn ^:export main []
  (dev-setup)
  (start! {:session app-session :facts facts})
  (reload))
