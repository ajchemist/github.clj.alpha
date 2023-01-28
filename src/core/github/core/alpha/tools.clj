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
  (let [output (str err out)]
    (when-not (str/blank? output)
      (println output)))
  (when-not (zero? exit)
    (throw (ex-info "Non-zero exit." sh-return))))


(defn- pass
  "Return non-nil only if the secret exists in `pass-name`"
  [pass-name]
  {:pre [(string? pass-name) (not (str/blank? pass-name))]}
  (let [{:keys [exit out err]} (jsh/sh "pass" "show" pass-name)
        err-output             (str err)]
    (when-not (str/blank? err-output) (println err-output))
    (cond
      (not (zero? exit))
      nil

      (str/index-of out (str pass-name "\n├── ")) ; dir entry check
      nil

      (str/index-of out "content-disposition: attachment;")
      (let [tmpf (File/createTempFile "pass-copy-" "")]
        (jsh/sh "gopass" "fscopy" pass-name (.getPath tmpf))
        (slurp tmpf))

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
      (try
        (github/get-repo credential-params repository)
        (catch Exception e
          (case (:status (ex-data e))
            404
            (as->
              (cond
                (and (string? org) (not (str/blank? org)))
                (github/create-org-repo credential-params org name)

                :else
                (github/create-repo credential-params name))
              {:strs [html_url]}
              (println "Repository created:" html_url))

            (println ::setup (ex-message e) (ex-data e)))))
      (setup-secrets-from-pass
        (-> opts
          (dissoc :repo/edn-file)
          ;; respect exec opts
          (update :github/token #(or % (:github/token credential-params)))
          (update :repository #(or % repository))))
      (when-not (.exists (jio/file ".git"))
        (sh-exit! (jsh/sh "git" "init"))
        (jsh/sh "git" "branch" "-m" "master" "main") ; try, no exit at non-zero code
        (sh-exit! (jsh/sh "git" "remote" "add" "-f" "origin" (str "git@github.com:" repository)))))))
