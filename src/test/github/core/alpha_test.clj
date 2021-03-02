(ns github.core.alpha-test
  (:require
   [clojure.test :as test :refer [deftest is are testing]]
   [clojure.string :as str]
   [clj-http.client :as http]
   [github.core.alpha :as github]
   )
  (:import
   java.time.Instant
   java.time.Duration
   ))


(defn took
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
  )


(comment
  (github/client
    {:url    "https://api.github.com/repos/ajchemist/user.core.async/actions/jobs/1814477804"
     :method :get
     :as     :json-strict-string-keys})


  (->
      (str)
      (subs 2)
      (str/replace #"(\d[HMS])(?!$)" "$1 ")))
