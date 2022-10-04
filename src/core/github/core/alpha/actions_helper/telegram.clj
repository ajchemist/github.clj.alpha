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


;; refer to
;; https://github.com/8398a7/action-slack/blob/master/src/fields.ts


(defn send-job-digest
  [token to job-context github-context]
  (when (and (string? job-context) (string? github-context))
    (try
      (let [job-context
            (cheshire/decode job-context)

            {:strs [workflow
                    repository
                    run_id
                    run_number
                    actor
                    server_url
                    sha
                    job]}
            (cheshire/decode github-context)

            {:strs [started_at check_run_url name] :as current-job}
            (-> (github/actions-list-jobs nil repository run_id)
              (get "jobs")
              (peek)) ; FIXME
            ]
        (tg/send-message
          token
          to
          (tg/render-html-message
            rum/render-static-markup
            [(job-status-emoji (get job-context "status"))
             " "
             [:a {:href (str server_url "/" repository "/commit/" sha "/checks")} workflow]
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
             (when current-job
               [:<>
                " "
                (str "in " (actions-helper/format-took (Instant/parse started_at)))])
             " "
             [:i job]
             ": "
             [:a {:href (str check_run_url)} name]])
          {:parse_mode "HTML" :disable_web_page_preview true}))
      (catch Throwable e
        (stacktrace/print-stack-trace e 30)
        (throw e)))))


(set! *warn-on-reflection* false)
