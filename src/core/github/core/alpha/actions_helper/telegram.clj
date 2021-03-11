(ns github.core.alpha.actions-helper.telegram
  (:require
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
     tg-chat-id
     github-context
     job-context]}]
  (let [{:strs [repository
                run_id
                run_number
                actor
                server_url
                sha]}        (cheshire/decode github-context)
        [owner repo]         (github/repository->owner-repo repository)
        job-context          (cheshire/decode job-context)
        {:strs [started_at]
         :as   _current-job} (-> (github/actions-list-jobs
                                   {:github/owner          owner
                                    :github/repo           repo
                                    :github.actions/run-id run_id})
                               (get "jobs")
                               (peek))]
    (tg/send-message
      tg-token
      tg-chat-id
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
         (str "in " (actions-helper/format-took (Instant/parse started_at)))]))))


(set! *warn-on-reflection* false)
