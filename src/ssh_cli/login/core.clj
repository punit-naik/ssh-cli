(ns ssh-cli.login.core
  (:require [clojure.java.shell :refer [sh]]
            [ssh-cli.core :refer [exec-remote scp]]
            [ssh-cli.utils :refer [build-id ssh-conf add-authorized-keys]]
            [clojure.string :refer [split]]
            [taoensso.timbre :refer [info]]))

(defn- create-ssh-folder
  "Creates the ~/.ssh folder required to store key pairs and ssh configurations"
  [arg-map]
  (info "Making sure that the ~/.ssh folder is present ...")
  (exec-remote (assoc arg-map :cmd "mkdir -p ~/.ssh; chmod -R 700 ~/.ssh")))

(defn- setup-passwordless-ssh-using-pvt-key
  "If one can log in the machines with a private key file, this function can re-use the same private key file
   to setup the passwordless login.
   First it will copy the private key file onto the machines at a specific location and use the same location
   in the `~/.ssh/config` file to set the configuration."
  [{:keys [ip-list user-name identity-file]}]
  (doseq [ip1 ip-list]
    (let [machine (build-id user-name (:public-ip ip1))]
      (create-ssh-folder {:machine machine :identity-file identity-file})
      (info (str "SCPing private key to the machine " (:private-ip ip1) " ..."))
      (scp {:from identity-file
            :to (str machine ":/home/" user-name "/.ssh/pvt-key.pem")
            :identity-file identity-file})
      (info (str "Setting appropriate permissions on the copied file on the machine " (:private-ip ip1) " ..."))
      (exec-remote {:machine machine :cmd "chmod 400 ~/.ssh/pvt-key.pem" :identity-file identity-file})
      (doseq [{:keys [private-ip]} ip-list]
        (info (str "Setting up passwordless SSH between the machines " (:private-ip ip1) " and " private-ip " ..."))
        (exec-remote {:machine machine :cmd (ssh-conf private-ip) :identity-file identity-file})))))

(defn- setup-passwordless-ssh-using-created-keys
  [{:keys [ip-list user-name password]}]
  (doseq [{:keys [public-ip private-ip]} ip-list]
    (let [machine (build-id user-name public-ip)
          files (set (split (:out (exec-remote {:machine machine :password password :cmd "ls ~/.ssh"})) #"\n"))]
      (create-ssh-folder {:machine machine :password password})
      (when-not (contains? files "id_rsa")
        (info (str "Generating SSH KeyPair on machine " private-ip " ..."))
        (exec-remote {:machine machine
                      :cmd "ssh-keygen -f ~/.ssh/id_rsa -t rsa -N '' -P ''"
                      :password password}))))
  (doseq [ip1 ip-list]
    (let [machine-1 (build-id user-name (:public-ip ip1))]
      (doseq [ip2 ip-list]
        (info (str "Setting up passwordless SSH between the machines " (:private-ip ip1) " and " (:private-ip ip2) " ..."))
        (exec-remote {:machine machine-1
                      :cmd (add-authorized-keys
                             (:out
                               (exec-remote {:machine (build-id user-name (:public-ip ip2)) :cmd "cat ~/.ssh/id_rsa.pub"
                                             :password password})))
                      :password password})))))

(defn setup-passwordless-ssh
  "Sets up passwordless SSH login between machines.
   The username and password is common for all machines, specify it in the argument map with the keys :username and :password respectively.
   The machines connect using an identity file, you can specify the path to the private key file with the key :identity-file."
  [{:keys [identity-file] :as arg-map}]
  (info "Beginning the Passwordless SSH Setup ...")
  (if identity-file
    (setup-passwordless-ssh-using-pvt-key arg-map)
    (setup-passwordless-ssh-using-created-keys arg-map)))

(comment
  (setup-passwordless-ssh {:user-name "ubuntu" :password ""
                           :identity-file "/path/key.pem"
                           :ip-list [{:public-ip "54.86.103.47" :private-ip "172.31.20.118"}
                                     {:public-ip "18.212.239.55" :private-ip "172.31.28.72"}
                                     {:public-ip "54.159.68.85" :private-ip "172.31.31.189"}]}))
