(ns github.core.alpha.actions-helper.api
  (:require
   [github.core.alpha.actions-helper.telegram :as helper.tg]
   ))


(defn telegram-job-digest
  [opts]
  (helper.tg/send-job-digest opts))
