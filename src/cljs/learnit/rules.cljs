(ns learnit.rules
  (:require-macros [precept.dsl :refer [<- entity entities]])
  (:require [precept.accumulators :as acc]
            [precept.spec.error :as err]
            [precept.util :refer [insert! insert-unconditional! retract! guid] :as util]
            [precept.rules :refer-macros [define defsub session rule]]
            [learnit.facts :refer [todo entry done-count active-count visibility-filter]]))

;(rule create-todo
;      {:group :action}
;      [[_ :todo/create]]
;      [?entry <- [_ :entry/title ?v]]
;      =>
;      (retract! ?entry)
;      (insert-unconditional! (todo ?v)))

(define [?e :todo/visible true] :-
        [:or [:and [_ :visibility-filter :all] [?e :todo/title]]
         [:and [_ :visibility-filter :done] [?e :todo/done true]]
         [:and [_ :visibility-filter :active] [?e :todo/done false]]])

(rule insert-done-count
      [?n <- (acc/count) :from [_ :todo/done true]]
      =>
      (.log js/console (str "n: " ?n))
      (insert-unconditional! (done-count ?n)))

(rule insert-active-count
      [[_ :done-count ?done]]
      [?total <- (acc/count) :from [:todo/title]]
      =>
      (.log js/console (str "?total: " ?total))
      (insert-unconditional! (active-count (- ?total ?done))))

(defsub :task-list
        [?eids <- (acc/by-fact-id :e) :from [:todo/visible]]
        [(<- ?visible-todos (entities ?eids))]
        [[_ :active-count ?active-count]]
        =>
        {:visible-todos ?visible-todos
         :all-complete? (= 0 ?active-count)})

(defsub :task-entry
        [[?e :entry/title ?v]]
        =>
        {:db/id ?e :entry/title ?v})

(defsub :footer
        [[_ :done-count ?done-count]]
        [[_ :active-count ?active-count]]
        [[_ :visibility-filter ?visibility-filter]]
        =>
        {:active-count ?active-count
         :done-count ?done-count
         :visibility-filter ?visibility-filter})

(session app-session
  'learnit.rules
  :db-schema learnit.schema/db-schema
  :client-schema learnit.schema/client-schema)



