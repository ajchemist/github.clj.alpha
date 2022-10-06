(ns github.core.alpha.tools
  (:require
   [clojure.string :as str]
   [clojure.java.io :as jio]
   [clojure.java.shell :as jsh]
   [clojure.edn :as edn]
   [github.core.alpha :as github]
   )
  (:import
   java.io.PushbackReader
   java.io.File
   ))


(defn sh-exit!
  [{:keys [exit out err] :as sh-return}]
  (println (str err out))
  (when-not (zero? exit)
    (throw (ex-info "Non-zero exit." sh-return))))


(defn- pass
  "Return non-nil only if the secret exists in `pass-name`"
  [pass-name]
  {:pre [(string? pass-name) (not (str/blank? pass-name))]}
  (let [{:keys [exit out err]} (jsh/sh "pass" "show" pass-name)]
    (when err (println err))
    (cond
      (not (zero? exit))
      nil

      (str/index-of out (str pass-name "\n├── ")) ; dir entry check
      nil

      :else (str/trim-newline out))))


(def ^File ^:private secrets-pass-edn-file (jio/file ".github" "secrets.pass.edn"))


(defn setup-secrets-from-pass
  [{:keys [:secrets.pass/edn-file
           :github/token
           repository]
    :or   {edn-file secrets-pass-edn-file}
    :as   opts}]
  (cond
    (not (.exists (jio/as-file edn-file)))
    (println "[skip] setup-secrets-from-pass (no such file):" edn-file)

    :else
    (with-open [rdr (jio/reader (jio/as-file edn-file))]
      (let [rs (edn/read (PushbackReader. rdr))]
        (run!
          (fn [[secret-name pass-name]]
            (when-some [secret-value (pass pass-name)]
              (github/actions-put-repo-secret
                {:github/token (or token (pass (:github.token/pass-name opts)))}
                repository secret-name secret-value)
              (println pass-name "->" secret-name)))
          rs)))))


(defn setup
  [{:keys [:repo/edn-file]
    :or   {edn-file (jio/file ".github" "repo.edn")}
    :as   opts}]
  (assert (.exists (jio/as-file edn-file)) (str ":repo/edn-file should be exists: " edn-file))
  (with-open [rdr (jio/reader (jio/as-file edn-file))]
    (let [{:keys [org repository :github/token] :as repo-edn} (edn/read (PushbackReader. rdr))

          name              (subs repository (inc (str/last-index-of repository "/")))
          credential-params {:github/token (or token (pass (:github.token/pass-name repo-edn)))}]
      (cond
        (and (string? org) (not (str/blank? org)))
        (github/create-org-repo credential-params org name)

        :else
        (github/create-repo credential-params name))
      (setup-secrets-from-pass
        (-> opts
          (dissoc :repo/edn-file)
          ;; respect exec opts
          (update :repository #(or % repository))))
      (sh-exit! (jsh/sh "git" "init"))
      (sh-exit! (jsh/sh "git" "remote" "add" "-f" "origin" (str "git@github.com:" repository))))))
