(ns ssh-cli.core
  (:require [clojure.java.shell :refer [sh]]
            [clojure.string :refer [escape]]
            [taoensso.timbre :refer [info]]))

(defn- prepare-cmd
  "Prepares a shell command string"
  [cmd]
  (str "bash -c \"" (escape cmd {\" "\\\""}) "\""))

(defn exec-remote
  "Executes a command on a remote machine via ssh"
  [{:keys [identity-file machine cmd password] :as arg-map}]
  (info (str "Executing command - " (:cmd arg-map) " - on the machine " (:machine arg-map) " ..."))
  (let [ssh-cmd ["ssh" machine "-o" "StrictHostKeyChecking=no"]]
    (if identity-file
      (apply sh
        (into ssh-cmd ["-i" identity-file (prepare-cmd cmd)]))
      (apply sh
        (into ["sshpass" "-p" password] (conj ssh-cmd (prepare-cmd cmd)))))))

(defn scp
  "SCPs a file from one machine to another"
  ([arg-map] (scp arg-map false))
  ([{:keys [identity-file from to password] :as arg-map} dir?]
   (info (str "SCPing file from " (:from arg-map) " to " (:to arg-map) " ..."))
   (let [scp-cmd ["scp" "-o" "StrictHostKeyChecking=no"]
         source-dest [from to]]
     (if identity-file
       (apply sh
         (into (cond-> (into scp-cmd ["-i" identity-file])
                       dir? (conj "-r")) source-dest))
       (do
         (println (into (cond-> (into ["sshpass" "-p" password] scp-cmd)
                                dir? (conj "-r")) source-dest))
         (apply sh
           (into (cond-> (into ["sshpass" "-p" password] scp-cmd)
                         dir? (conj "-r")) source-dest)))))))
