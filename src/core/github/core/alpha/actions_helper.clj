(ns github.core.alpha.actions-helper
  (:require
   [clojure.string :as str]
   [cheshire.core :as cheshire]
   )
  (:import
   java.time.Instant
   java.time.Duration
   ))


;; https://docs.github.com/en/actions/reference/environment-variables


(set! *warn-on-reflection* true)


;;


(defn- millis->hms
  [millis]
  (let [hours   (Math/floor (/ millis (* 1000 60 60)))
        time    (- millis (* hours 1000 60 60))
        minutes (Math/floor (/ time (* 1000 60)))
        time    (- time (* minutes 1000 60))
        seconds (Math/floor (/ time 1000))]
    [hours minutes seconds]))


(defn took-millis
  ([start]
   (took-millis start (Instant/now)))
  ([start end]
   (.toMillis (Duration/between start end))))


(defn format-took
  ([start]
   (format-took start (Instant/now)))
  ([start end]
   (let [[h m s] (millis->hms (took-millis start end))]
     (str
       (when (pos? h) (str (int h) "h "))
       (when (pos? m) (str (int m) "m"))
       (str (int s) "sec")))))


;;


(defn find-job
  [jobs job-name]
  (some (fn [{:strs [name] :as job}] (when (= name job-name) job)) jobs))


;;


(defn envs
  []
  {:ci                 (System/getenv "CI")
   :github_workflow    (System/getenv "GITHUB_WORKFLOW")
   :github_run_id      (System/getenv "GITHUB_RUN_ID")
   :github_run_number  (System/getenv "GITHUB_RUN_NUMBER")
   :github_action      (System/getenv "GITHUB_ACTION")
   :github_actions     (System/getenv "GITHUB_ACTIONS")
   :github_actor       (System/getenv "GITHUB_ACTOR")
   :github_repository  (System/getenv "GITHUB_REPOSITORY")
   :github_event_name  (System/getenv "GITHUB_EVENT_NAME")
   :github_event_path  (System/getenv "GITHUB_EVENT_PATH")
   :github_workspace   (System/getenv "GITHUB_WORKSPACE")
   :github_sha         (System/getenv "GITHUB_SHA")
   :github_ref         (System/getenv "GITHUB_REF")
   :github_head_ref    (System/getenv "GITHUB_HEAD_REF")
   :github_base_ref    (System/getenv "GITHUB_BASE_REF")
   :github_server_url  (System/getenv "GITHUB_SERVER_URL")
   :github_api_url     (System/getenv "GITHUB_API_URL")
   :github_graphql_url (System/getenv "GITHUB_GRAPHQL_URL")})


(comment
  (cheshire/decode "{\"a\": 1}")
  )


(set! *warn-on-reflection* false)
