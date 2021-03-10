(ns github.core.alpha-test
  (:require
   [clojure.test :as test :refer [deftest is are testing]]
   [clojure.string :as str]
   [clj-http.client :as http]
   [github.core.alpha :as github]
   [telegram.core.alpha :as tg]
   [rum.core :as rum]
   )
  (:import
   java.time.Instant
   java.time.Duration
   ))


(def tg-token (System/getenv "TG_TOKEN"))
(def tg-chat-id (System/getenv "TG_CHAT_ID"))


#_(defn took
  "Millis"
  [{:strs [started_at completed_at]}]
  (.toMillis (Duration/between (Instant/parse started_at) (Instant/parse completed_at))))


(defn took-hms
  [millis]
  (let [hours   (Math/floor (/ millis (* 1000 60 60)))
        time    (- millis (* hours 1000 60 60))
        minutes (Math/floor (/ time (* 1000 60)))
        time    (- time (* minutes 1000 60))
        seconds (Math/floor (/ time 1000))]
    [hours minutes seconds]))


(defn format-took
  [started-at]
  (let [[h m s] (took-hms (.toMillis (Duration/between started-at (Instant/now))))]
    (str
      (when (pos? h) (str (int h) "h "))
      (when (pos? m) (str (int m) "m"))
      (str (int s) "sec"))))


(deftest test-actions
  (github/actions-list-jobs
    {:github/owner          "ajchemist"
     :github/repo           "user.core.async"
     :github.actions/run-id 530723420})


  (let [[owner repo] (str/split (System/getenv "GITHUB_REPOSITORY") #"/" 2)]
    (prn
      (github/actions-list-jobs
        {:github/owner          owner
         :github/repo           repo
         :github.actions/run-id (System/getenv "GITHUB_RUN_ID")})))


  (let [[owner repo] (str/split (System/getenv "GITHUB_REPOSITORY") #"/" 2)
        jobs         (github/actions-list-jobs
                       {:github/owner          owner
                        :github/repo           repo
                        :github.actions/run-id (System/getenv "GITHUB_RUN_ID")})]
    (tg/send-message
      tg-token
      tg-chat-id
      {:parse_mode "HTML"}
      (tg/render-html-message
        rum/render-static-markup
        ["Run "
         [:a {:href (str (System/getenv "GITHUB_SERVER_URL") "/" (System/getenv "GITHUB_REPOSITORY") "/actions/runs/" (System/getenv "GITHUB_RUN_ID"))} (str "#" (System/getenv "GITHUB_RUN_NUMBER"))]
         " "
         [:a {:href (str (System/getenv "GITHUB_SERVER_URL") "/" (System/getenv "GITHUB_REPOSITORY"))} (System/getenv "GITHUB_REPOSITORY")]
         " "
         "(" [:a {:href (str (System/getenv "GITHUB_SERVER_URL") "/" (System/getenv "GITHUB_REPOSITORY") "/commit/" (System/getenv "GITHUB_SHA"))} (System/getenv "GITHUB_SHA")] ")"
         " "
         (str "by " (System/getenv "GITHUB_ACTOR"))
         " "
         (str "in " (format-took (Instant/parse (get-in jobs ["jobs" 0 "started_at"]))))]))))


(comment
  (github/client
    {:url    "https://api.github.com/repos/ajchemist/user.core.async/actions/jobs/1814477804"
     :method :get
     :as     :json-strict-string-keys})


  (->
      (str)
      (subs 2)
      (str/replace #"(\d[HMS])(?!$)" "$1 ")))
