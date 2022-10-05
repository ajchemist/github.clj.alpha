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
   ))


(defn- pass
  "Return non-nil only if the secret exists in `pass-name`"
  [pass-name]
  (let [{:keys [exit out err]} (jsh/sh "pass" "show" pass-name)]
    (println err)
    (cond
      (not (zero? exit))
      nil

      (str/index-of out (str pass-name "\n├── ")) ; dir entry check
      nil

      :else (str/trim-newline out))))


(defn setup-secrets-from-pass
  [{:keys [basic-auth repository edn-file]
    :or   {edn-file (jio/file ".github" "secrets.pass.edn")}}]
  (assert (.exists (jio/as-file edn-file)) (str "edn-file should be exists: " edn-file))
  (with-open [rdr (jio/reader (jio/as-file edn-file))]
    (let [rs (edn/read (PushbackReader. rdr))]
      (run!
        (fn [[secret-name pass-name]]
          (when-some [secret-value (pass pass-name)]
            (github/actions-put-repo-secret
              {:basic-auth basic-auth}
              repository secret-name secret-value)
            (println pass-name " -> " secret-name)))
        rs))))
