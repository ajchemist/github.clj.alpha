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
  [{:keys [basic-auth repository :secrets.pass/edn-file]
    :or   {edn-file secrets-pass-edn-file}}]
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
                {:basic-auth basic-auth}
                repository secret-name secret-value)
              (println pass-name "->" secret-name)))
          rs)))))


(defn setup
  [{:keys [:repo/edn-file]
    :or   {edn-file (jio/file ".github" "repo.edn")}
    :as   opts}]
  (assert (.exists (jio/as-file edn-file)) (str ":repo/edn-file should be exists: " edn-file))
  (with-open [rdr (jio/reader (jio/as-file edn-file))]
    (let [{:keys [repository basic-auth]} (edn/read (PushbackReader. rdr))
          _                               (assert (vector? basic-auth))
          basic-auth                      (update basic-auth 1 pass)
          name                            (subs repository (inc (str/last-index-of repository "/")))]
      (github/create-repo {:basic-auth basic-auth} name)
      (setup-secrets-from-pass
        (-> opts
          (dissoc :repo/edn-file)
          (assoc
            :basic-auth basic-auth
            :repository repository)))
      (sh-exit! (jsh/sh "git" "init"))
      (sh-exit! (jsh/sh "git" "remote" "add" "-f" "origin" (str "git@github.com:" repository))))))
