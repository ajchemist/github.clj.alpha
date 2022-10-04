(ns github.core.alpha.script.repo-actions
  (:require
   [clojure.tools.cli :as cli]
   [github.core.alpha :as github]
   ))


(def cli-options--list-secrets
  [[nil "--repository REPOSITORY" ":github/repository"]
   [nil "--basic-auth BASIC_AUTH" ":basic-auth"]])


(def cli-options--put-secret
  [[nil "--repository REPOSITORY" ":github/repository"]
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
          {:keys [repository basic-auth]} options]
      (prn (github/actions-list-repo-secrets {:basic-auth basic-auth} repository)))


    "put-secret"
    (let [{:keys [options] :as _parsed} (cli/parse-opts xs cli-options--put-secret)

          {:keys [repository
                  secret-name
                  secret-value
                  key key-id
                  basic-auth]} options]
      (prn
        (github/actions-put-repo-secret {:basic-auth basic-auth} repository secret-name secret-value key key-id)))


    (println "Unknown operation:" op)))
