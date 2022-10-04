(ns github.core.alpha.actions-helper.api
  (:require
   [github.core.alpha.actions-helper.telegram :as helper.tg]
   ))


(defn telegram-job-digest
  [{:keys          [:telegram/token :telegram/to]
    github-context :github/context
    job-context    :job/context
    :or            {token          (System/getenv "TELEGRAM_TOKEN")
                    to             (System/getenv "TELEGRAM_TO")
                    job-context    (System/getenv "JOB_CONTEXT")
                    github-context (System/getenv "GITHUB_CONTEXT")}}]
  (helper.tg/send-job-digest token to job-context github-context))
