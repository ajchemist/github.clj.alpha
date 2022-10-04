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


;;


(defn- decode-base64
  ^bytes
  [^String encoded]
  (.decode (Base64/getDecoder) encoded))


(defn- encode-base64
  ^bytes
  [^bytes s]
  (.encode (Base64/getEncoder) s))


;;


(def ^{:arglists '([request] [request respond raise])}
  client
  (-> http/request
    (user.ring/wrap-meta-response)))


;; * actions


;; https://docs.github.com/en/rest/reference/actions#list-jobs-for-a-workflow-run


(defn actions-list-jobs
  [params repository run-id]
  (client
    (assoc params
      :url (str "https://api.github.com/repos/" repository "/actions/runs/" run-id "/jobs")
      :method :get
      :as :json-strict-string-keys
      :accept "application/vnd.github.v3+json")))


(defn actions-run
  [params repository run-id]
  (client
    (assoc params
      :url (str "https://api.github.com/repos/" repository "/actions/runs/" run-id)
      :method :get
      :as :json-strict-string-keys
      :accept "application/vnd.github.v3+json")))


(defn actions-run-job
  [params repository job-id]
  (client
    (assoc params
      :url (str "https://api.github.com/repos/" repository "/actions/jobs/" job-id)
      :method :get
      :as :json-strict-string-keys
      :accept "application/vnd.github.v3+json")))


;; * repository


(defn list-user-repos
  [params username]
  (client
    (assoc params
      :url (str "https://api.github.com/users/" username "/repos")
      :method :get
      :as :json-strict-string-keys)))


(defn actions-list-repo-secrets
  [params repository]
  (client
    (assoc params
      :url (str "https://api.github.com/repos/" repository "/actions/secrets")
      :method :get
      :as :json-strict-string-keys
      :accept "application/vnd.github.v3+json"
      )))


(defn actions-get-repo-public-key
  [params repository]
  (client
    (assoc params
      :url (str "https://api.github.com/repos/" repository "/actions/secrets/public-key")
      :method :get
      :as :json-strict-string-keys
      :accept "application/vnd.github.v3+json"
      )))


(defn actions-get-repo-secret
  [params repository secret-name]
  (client
    (assoc params
      :url (str "https://api.github.com/repos/" repository "/actions/secrets/" secret-name)
      :method :get
      :as :json-strict-string-keys
      :accept "application/vnd.github.v3+json")))


(defn actions-put-repo-secret
  ([params repository secret-name secret-value]
   (actions-put-repo-secret params repository secret-name secret-value nil nil))
  ([params repository secret-name ^String secret-value public-key-id public-key]
   (let [{public-key-id "key_id"
          public-key    "key"}
         (if (and public-key-id
                  public-key)
           {"key_id" public-key-id "key" public-key}
           (actions-get-repo-public-key params repository))]
     (client
       (assoc params
         :url (str "https://api.github.com/repos/" repository "/actions/secrets/" secret-name)
         :method :put
         :as :json-strict-string-keys
         :accept "application/vnd.github.v3+json"
         :content-type :application/json
         :form-params (cond-> {:encrypted_value
                               (String.
                                 (encode-base64
                                   (box/box-seal (.getBytes secret-value) (decode-base64 public-key))))}
                        public-key-id (assoc :key_id public-key-id)))))))


(defn create-repo
  [params name]
  (client
    (-> params
      (assoc
        :url "https://api.github.com/user/repos"
        :method       :post
        :content-type :json
        :as           :json-string-keys)
      (assoc-in [:form-params "name"] name))))


(defn delete-repo
  [params repository]
  (client
    (assoc params
      :url          (str "https://api.github.com/repos/" repository)
      :method       :delete
      :content-type :json
      :as           :json-string-keys)))


(defn list-deploy-keys
  [params repository]
  (client
    (assoc params
      :url    (str "https://api.github.com/repos/" repository "/keys")
      :method :get
      :as     :json-string-keys)))


(defn post-deploy-key
  [params repository]
  (client
    (assoc params
      :url (str "https://api.github.com/repos/" repository "/keys")
      :method :post
      :content-type :json
      :as :json-string-keys)))


(set! *warn-on-reflection* false)
