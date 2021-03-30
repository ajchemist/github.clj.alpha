(ns github.core.alpha-test
  (:require
   [clojure.test :as test :refer [deftest is are testing]]
   [clojure.string :as str]
   [clj-http.client :as http]
   [github.core.alpha :as github]
   [github.core.alpha.actions-helper :as actions-helper]
   [telegram.core.alpha :as tg]
   [rum.core :as rum]
   )
  (:import
   java.time.Instant
   ))


(def tg-token (System/getenv "TG_TOKEN"))
(def tg-chat-id (System/getenv "TG_CHAT_ID"))


(def github-envs (actions-helper/envs))


(deftest test-actions
  (github/actions-list-jobs
    {:github/owner          "ajchemist"
     :github/repo           "user.core.async"
     :github.actions/run-id 530723420})


  (let [[owner repo] (github/repository->owner-repo (System/getenv "GITHUB_REPOSITORY"))]
    (prn
      (github/actions-list-jobs
        {:github/owner          owner
         :github/repo           repo
         :github.actions/run-id (System/getenv "GITHUB_RUN_ID")}))))


(comment
  (let [[owner repo]   (github/repository->owner-repo (System/getenv "GITHUB_REPOSITORY"))
        {:strs [jobs]} (github/actions-list-jobs
                         {:github/owner          owner
                          :github/repo           repo
                          :github.actions/run-id (System/getenv "GITHUB_RUN_ID")})]
    (tg/send-message
      tg-token
      tg-chat-id
      (tg/render-html-message
        rum/render-static-markup
        ["Run "
         [:a {:href (str (System/getenv "GITHUB_SERVER_URL") "/" (System/getenv "GITHUB_REPOSITORY") "/actions/runs/" (System/getenv "GITHUB_RUN_ID"))} (str "#" (System/getenv "GITHUB_RUN_NUMBER"))]
         " "
         [:a {:href (str (System/getenv "GITHUB_SERVER_URL") "/" (System/getenv "GITHUB_REPOSITORY"))} (System/getenv "GITHUB_REPOSITORY")]
         " "
         "(" [:a {:href (str (System/getenv "GITHUB_SERVER_URL") "/" (System/getenv "GITHUB_REPOSITORY") "/commit/" (System/getenv "GITHUB_SHA"))} (subs (System/getenv "GITHUB_SHA") 0 8)] ")"
         " "
         (str "by " (System/getenv "GITHUB_ACTOR"))
         " "
         (str "in " (actions-helper/format-took (Instant/parse (get-in jobs [0 "started_at"]))))])
      {:parse_mode "HTML" :disable_web_page_preview true}))


  (github/actions-run
    {:github/owner          "ajchemist"
     :github/repo           "github.clj.alpha"
     :github.actions/run-id 613250891})


  (github/actions-run-job
    {:github/owner       "ajchemist"
     :github/repo        "github.clj.alpha"
     :github.jobs/job-id 2011145399})


  (github/client
    {:url    "https://api.github.com/repos/ajchemist/user.core.async/actions/jobs/1814477804"
     :method :get
     :as     :json-strict-string-keys})


  (->
      (str)
      (subs 2)
      (str/replace #"(\d[HMS])(?!$)" "$1 ")))
