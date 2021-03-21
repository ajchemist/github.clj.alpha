(ns github.core.alpha.actions-helper.telegram
  (:require
   [clojure.stacktrace :as stacktrace]
   [cheshire.core :as cheshire]
   [telegram.core.alpha :as tg]
   [rum.core :as rum]
   [github.core.alpha :as github]
   [github.core.alpha.actions-helper :as actions-helper]
   )
  (:import
   java.time.Instant
   ))


(set! *warn-on-reflection* true)


(defn job-status-emoji
  [status]
  ({"success"   "✅"
    "failure"   "🔴"
    "cancelled" "⚠"} status "❓"))


(defn send-job-digest
  [{:keys
    [tg-token
     tg-to]}]
  (let [tg-token       (or (System/getenv "TELEGRAM_TOKEN") tg-token)
        tg-to          (or (System/getenv "TELEGRAM_TO") tg-to)
        job-context    (System/getenv "JOB_CONTEXT")
        github-context (System/getenv "GITHUB_CONTEXT")]
    (when (and (string? job-context) (string? github-context))
      (try
        (let [job-context
              (cheshire/decode job-context)

              {:strs [repository
                      run_id
                      run_number
                      actor
                      server_url
                      sha
                      job]}
              (cheshire/decode github-context)

              [owner repo]
              (github/repository->owner-repo repository)

              {:strs [started_at] :as _current-job}
              (-> (github/actions-list-jobs
                    {:github/owner          owner
                     :github/repo           repo
                     :github.actions/run-id run_id})
                (get "jobs")
                (peek))]
          (tg/send-message
            tg-token
            tg-to
            {:parse_mode "HTML" :disable_web_page_preview true}
            (tg/render-html-message
              rum/render-static-markup
              [(job-status-emoji (get job-context "status"))
               " "
               [:a {:href (str server_url "/" repository "/actions/runs/" run_id)} (str "#" run_number)]
               " "
               "["
               [:a {:href (str server_url "/" repository)} repository]
               "]"
               " "
               "("
               [:a {:href (str server_url "/" repository "/commit/" sha)} (subs sha 0 8)]
               ")"
               " "
               (str "by " actor)
               " "
               (str "in " (actions-helper/format-took (Instant/parse started_at)))
               " "
               [:i job]])))
        (catch Throwable e
          (stacktrace/print-stack-trace e 30)
          (throw e))))))


(set! *warn-on-reflection* false)


(comment
  (github/actions-list-jobs
    {:github/owner          owner
     :github/repo           repo
     :github.actions/run-id run_id}
    644766451)
  )
