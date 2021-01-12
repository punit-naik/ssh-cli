(ns ssh-cli.core-test
  (:require [clojure.test :refer [deftest testing is]]
            [clojure.java.io :refer [resource]]
            [ssh-cli.core :refer [exec-remote scp]]
            [clojure.string :refer [trim split]]))

(defonce ^:private user-name (System/getProperty "user.name"))
(defonce ^:private machine (str user-name "@127.0.0.1"))
(defonce ^:private password (trim (slurp (resource "passwd"))))

(deftest local-scp-test
  (testing "SCP test on local machine..."
    (exec-remote {:machine machine
                  :cmd "mkdir ~/scp-1"
                  :password password})
    (scp {:from (str "/home/" user-name "/scp-1") :to (str machine ":/home/" user-name "/scp-2")
          :password password} true)
    (let [directory-contents (set (split (:out (exec-remote {:machine machine :cmd "ls ~" :password password})) #"\n"))]
      (is (contains? directory-contents "scp-1"))
      (is (contains? directory-contents "scp-2")))))
