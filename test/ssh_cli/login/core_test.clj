(ns ssh-cli.login.core-test
  (:require [clojure.test :refer [deftest testing is]]
            [clojure.java.io :refer [resource]]
            [ssh-cli.login.core :refer [setup-passwordless-ssh]]
            [ssh-cli.core :refer [exec-remote]]
            [clojure.string :refer [trim]]))

(defonce ^:private user-name (System/getProperty "user.name"))
(defonce ^:private password (trim (slurp (resource "passwd"))))

(deftest passwordless-ssh-test
  (testing "NOTE: Please paste your password as text in the `resources/passwd` file and also,
                  please remove your public key from `~/.ssh/authorized_keys` if present!
            Passwordless SSH functionality test on local machine..."
    (setup-passwordless-ssh {:ip-list [{:public-ip "127.0.0.1" :private-ip "127.0.0.1"}]
                             :user-name user-name
                             :password password})
    (is (= (trim (:out (exec-remote {:machine (str user-name "@127.0.0.1")
                                     :cmd "echo $USER"
                                     :password password})))
           (System/getProperty "user.name")))))
