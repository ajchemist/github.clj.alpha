(ns github.core.alpha.script.repo-actions
  (:require
   [clojure.tools.cli :as cli]
   [user.tools.github.alpha :as github]
   ))


(def cli-options--list-secrets
  [[nil "--owner OWNER" ":github/owner"]
   [nil "--repo REPO" ":github/repo"]
   [nil "--basic-auth BASIC_AUTH" ":basic-auth"]])


(def cli-options--put-secret
  [[nil "--owner OWNER" ":github/owner"]
   [nil "--repo REPO" ":github/repo"]
   [nil "--secret-name SECRET_NAME" ":github.actions.secrets/secret-name"]
   [nil "--secret-value SECRET_VALUE" ":github.actions.secrets/secret-value"]
   [nil "--key KEY" ":github.actions.secrets/public-key"]
   [nil "--key-id KEY_ID" ":github.actions.secrets/public-key-id"]
   [nil "--basic-auth BASIC_AUTH" ":basic-auth"]])


(defn -main
  [op & xs]
  (case op
    "list-secrets"
    (let [{:keys [options] :as _parsed}   (cli/parse-opts xs cli-options--list-secrets)
          {:keys [owner repo basic-auth]} options]
      (prn
        (github/actions-list-repo-secrets
          {:github/owner owner
           :github/repo  repo
           :basic-auth   basic-auth})))


    "put-secret"
    (let [{:keys [options] :as _parsed} (cli/parse-opts xs cli-options--put-secret)
          {:keys [owner repo
                  secret-name secret-value
                  key key-id
                  basic-auth]}          options]
      (prn
        (github/actions-put-repo-secret
          {:github/owner                         owner
           :github/repo                          repo
           :github.actions.secrets/secret-name   secret-name
           :github.actions.secrets/secret-value  secret-value
           :github.actions.secrets/public-key    key
           :github.actions.secrets/public-key-id key-id
           :basic-auth                           basic-auth})))


    (println "Unknown operation:" op)))
