(ns github.core.alpha
  (:require
   [clojure.string :as str]
   [clj-http.client :as http]
   [caesium.crypto.box :as box]
   [user.ring.alpha :as user.ring]
   )
  (:import
   java.util.Base64
   ))


(set! *warn-on-reflection* true)


(def ^:dynamic *print* true)


;;


(defn decode-base64
  ^bytes
  [^String encoded]
  (.decode (Base64/getDecoder) encoded))


(defn encode-base64
  ^bytes
  [^bytes s]
  (.encode (Base64/getEncoder) s))


(defn repository->owner-repo
  [repository]
  (str/split repository #"/" 2))


;;


(def ^{:arglists '([request] [request respond raise])}
  request
  (-> http/request
    (user.ring/wrap-meta-response)))


(defn request-error-handle
  [e request-params]
  (let [{:keys [url]} request-params]
    (cond
      ;; (re-find #"Bad Request: message is not modified" (:body (ex-data e)))
      ;; (println "Telegram request failed:" url (pr-str request-params))

      :else
      (do
        (println "Github request failed:" url (pr-str request-params))
        (throw e)))))


(defn client
  [request-params]
  (try
    (request request-params)
    (catch Exception e
      (request-error-handle e request-params))))


;; * actions


;; https://docs.github.com/en/rest/reference/actions#list-jobs-for-a-workflow-run


(defn actions-list-jobs
  [{:keys [:github/owner
           :github/repo
           :github.actions/run-id]
    :as   request-params}]
  (client
    (assoc request-params
      :url (str "https://api.github.com/repos/" owner "/" repo "/actions/runs/" run-id "/jobs")
      :method :get
      :as :json-strict-string-keys
      :accept "application/vnd.github.v3+json")))


(defn actions-run
  [{:keys [:github/owner
           :github/repo
           :github.actions/run-id]
    :as   request-params}]
  (client
    (assoc request-params
      :url (str "https://api.github.com/repos/" owner "/" repo "/actions/runs/" run-id)
      :method :get
      :as :json-strict-string-keys
      :accept "application/vnd.github.v3+json")))


(defn actions-run-job
  [{:keys [:github/owner
           :github/repo
           :github.jobs/job-id]
    :as   request-params}]
  (client
    (assoc request-params
      :url (str "https://api.github.com/repos/" owner "/" repo "/actions/jobs/" job-id)
      :method :get
      :as :json-strict-string-keys
      :accept "application/vnd.github.v3+json")))


;; * repository


(defn list-user-repos
  [{:keys [:github/username] :as req}]
  (client
    (assoc req
      :url (str "https://api.github.com/users/" username "/repos")
      :method :get
      :as :json-strict-string-keys
      )))


(defn actions-list-repo-secrets
  [{:keys [:github/owner :github/repo] :as req}]
  (client
    (assoc req
      :url (str "https://api.github.com/repos/" owner "/" repo "/actions/secrets")
      :method :get
      :as :json-strict-string-keys
      :accept "application/vnd.github.v3+json"
      )))


(defn actions-get-repo-pub-key
  [{:keys [:github/owner :github/repo] :as req}]
  (client
    (assoc req
      :url (str "https://api.github.com/repos/" owner "/" repo "/actions/secrets/public-key")
      :method :get
      :as :json-strict-string-keys
      :accept "application/vnd.github.v3+json"
      )))


(defn actions-get-repo-secret
  [{:keys
    [:github/owner
     :github/repo
     :github.actions/secret-name] :as req}]
  (client
    (assoc req
      :url (str "https://api.github.com/repos/" owner "/" repo "/actions/secrets/" secret-name)
      :method :get
      :as :json-strict-string-keys
      :accept "application/vnd.github.v3+json")))


(defn actions-put-repo-secret
  [{:keys
    [github/owner
     github/repo
     github.actions.secrets/secret-name
     ^String github.actions.secrets/secret-value
     github.actions.secrets/public-key
     github.actions.secrets/public-key-id] :as req}]
  (when *print* (println "Create or update actions secret:" secret-name))
  (client
    (assoc req
      :url (str "https://api.github.com/repos/" owner "/" repo "/actions/secrets/" secret-name)
      :method :put
      :as :json-strict-string-keys
      :accept "application/vnd.github.v3+json"
      :content-type :application/json
      :form-params (cond-> {:encrypted_value
                            (String.
                              (encode-base64
                                (box/box-seal (.getBytes secret-value) (decode-base64 public-key))))}
                     public-key-id (assoc :key_id public-key-id)))))


(defn create-repo
  [req]
  {:pre [(string? (get-in req [:form-params "name"]))]}
  (when *print* (println "Creating repository:" (get-in req [:form-params "name"])))
  (client
    (assoc req
      :url "https://api.github.com/user/repos"
      :method       :post
      :content-type :json
      :as           :json-string-keys)))


(defn delete-repo
  [{:keys [:github/owner :github/repo] :as req}]
  (client
    (assoc req
      :url          (str "https://api.github.com/repos/" owner "/" repo)
      :method       :delete
      :content-type :json
      :as           :json-string-keys)))


(defn list-deploy-keys
  [{:keys [:github/owner :github/repo] :as req}]
  (client
    (assoc req
      :url    (str "https://api.github.com/repos/" owner "/" repo "/keys")
      :method :get
      :as     :json-string-keys)))


(defn post-deploy-key
  [{:keys [:github/owner :github/repo] :as req}]
  (println "Deploying key:" (str owner "/" repo))
  (client
    (assoc req
      :url (str "https://api.github.com/repos/" owner "/" repo "/keys")
      :method :post
      :content-type :json
      :as :json-string-keys)))


(set! *warn-on-reflection* false)
