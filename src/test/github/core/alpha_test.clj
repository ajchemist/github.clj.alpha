(ns github.core.alpha-test
  (:require
   [clojure.test :as test :refer [deftest is are testing]]
   [clojure.string :as str]
   [clojure.java.shell :as jsh]
   [clj-http.client :as http]
   [github.core.alpha :as github]
   [github.core.alpha.actions-helper :as actions-helper]
   [telegram.core.alpha :as tg]
   [rum.core :as rum]
   )
  (:import
   java.time.Instant
   ))


(defn pass
  [pass-name]
  (str/trim-newline (:out (jsh/sh "pass" pass-name))))


(def github-envs (actions-helper/envs))


(comment
  (tg/get-updates tg-token nil)
  )


(deftest test-actions
  #_(github/actions-run-job nil "ajchemist/user.core.async" 2789017092)
  (prn (github/actions-list-jobs nil (System/getenv "GITHUB_REPOSITORY") (System/getenv "GITHUB_RUN_ID")))
  (prn (github/actions-run nil (System/getenv "GITHUB_REPOSITORY") (System/getenv "GITHUB_RUN_ID")))
  #_(prn (github/actions-run-job nil (System/getenv "GITHUB_REPOSITORY") (System/getenv "GITHUB_JOB_ID")))

  ;; secrets.GITHUB_TOKEN
  ;; -> basic-auth fail
  ;; Resource not accessible by integration
  #_(prn
    (github/actions-get-repo-public-key
      {:basic-auth [(System/getenv "GITHUB_ACTOR") (System/getenv "GITHUB_TOKEN")]}
      (System/getenv "GITHUB_REPOSITORY")))
  #_(prn
    (github/actions-put-repo-secret
      {:basic-auth [(System/getenv "GITHUB_ACTOR") (System/getenv "GITHUB_TOKEN")]}
      (System/getenv "GITHUB_REPOSITORY")
      "TEST_SECRET"
      ""))
  )


(comment
  (github/create-org-repo
    #_{:basic-auth ["ajchemist" (pass "github.com/tokens/ajchemist")]}
    {:headers {"Authorization" (str "Bearer " (pass "github.com/tokens/ajchemist"))}}
    "alchemiakr"
    "test-repo-A")


  (github/delete-repo
    {:headers {"Authorization" (str "Bearer " (pass "github.com/tokens/ajchemist"))}}
    "alchemiakr/test-repo-A")
  )


(comment
  (github/actions-list-jobs nil "ajchemist/user.core.async" 530723420)
  (github/actions-run nil "ajchemist/user.core.async" 530723420)
  (github/actions-run nil "ajchemist/github.clj.alpha" 613250891)


  (github/client
    {:url    "https://api.github.com/repos/ajchemist/user.core.async/actions/jobs/1814477804"
     :method :get
     :as     :json-strict-string-keys})


  (github/actions-get-repo-public-key
    {:basic-auth ["ajchemist" (pass "github.com/ajchemist/PAT/tools")]}
    "ajchemist/github-playground")


  (github/actions-put-repo-secret
    {:basic-auth ["ajchemist" (pass "github.com/ajchemist/PAT/tools")]

     :github/owner                         "ajchemist"
     :github/repo                          "github-playground"
     :github.actions.secrets/secret-name   "SECRET"
     :github.actions.secrets/secret-value  "VALUE"
     :github.actions.secrets/public-key    "DHysQfow8/38V1SWzekx3gtxmVqlShgYPiHznFIrBTU="
     :github.actions.secrets/public-key-id "568250167242549743"})


  (->
    (str)
    (subs 2)
    (str/replace #"(\d[HMS])(?!$)" "$1 ")))
