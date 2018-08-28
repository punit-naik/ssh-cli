(ns ssh-cli.core
  (:require [clojure.java.shell :refer [sh]]
            [clojure.string :refer [escape]]
            [taoensso.timbre :refer [info]]))

(defn- prepare-cmd
  "Prepares a shell command string"
  [cmd]
  ["bash -c \"" (escape cmd {\" "\\\""}) "\""])

(defn- exec-remote-using-pvt-key
  "Executes a command on a remote machine via ssh using a private key"
  [{:keys [machine cmd identity-file]}]
  (apply sh
    (into ["ssh" machine "-o" "StrictHostKeyChecking=no" "-i" identity-file]
          (prepare-cmd cmd))))

(defn- exec-remote-using-password
  "Executes a command on a remote machine via ssh using password
   NOTE: Will make use of the `sshpass` utility"
  [{:keys [machine cmd password]}]
  (apply sh
    (into ["sshpass" "-p" password "ssh" machine "-o" "StrictHostKeyChecking=no"]
          (prepare-cmd cmd))))

(defn exec-remote
  "Executes a command on a remote machine via ssh"
  [{:keys [identity-file] :as arg-map}]
  (info (str "Executing command - " (:cmd arg-map) " - on the machine " (:machine arg-map) " ..."))
  (if identity-file
    (exec-remote-using-pvt-key arg-map)
    (exec-remote-using-password arg-map)))

(defn- scp-using-pvt-key
  "SCPs a file from one machine to another using a private key"
  ([{:keys [from to identity-file]} dir?]
   (if dir?
     (apply sh ["scp" "-o" "StrictHostKeyChecking=no" "-i" identity-file "-r" from to])
     (apply sh ["scp" "-o" "StrictHostKeyChecking=no" "-i" identity-file from to])))
  ([arg-map] (scp-using-pvt-key arg-map false)))

(defn- scp-using-password
  "SCPs a file from one machine to another using password
   NOTE: Will make use of the `sshpass` utility"
  ([{:keys [from to password]} dir?]
   (if dir?
     (apply sh ["sshpass" "-p" password "scp" "-o" "StrictHostKeyChecking=no" "-r" from to])
     (apply sh ["sshpass" "-p" password "scp" "-o" "StrictHostKeyChecking=no" from to])))
  ([arg-map] (scp-using-password arg-map false)))

(defn scp
  "SCPs a file from one machine to another"
  [{:keys [identity-file] :as arg-map}]
  (info (str "SCPing file from " (:from arg-map) " to " (:to arg-map) " ..."))
  (if identity-file
    (scp-using-pvt-key arg-map)
    (scp-using-password arg-map)))
