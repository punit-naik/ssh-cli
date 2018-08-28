(ns ssh-cli.utils)

(defn build-id
  "Builds a qualified host name based on the username and IP specified"
  [user-name ip]
  (str user-name "@" ip))

(defn ssh-conf
  "A template for the SSH Configuration file (`~/.ssh/config`)"
  [machine]
  (str "echo -e \"Host " machine "\n\tIdentityFile ~/.ssh/pvt-key.pem\n\tStrictHostKeyChecking no\n\tCompression no\n\tRequestTTY force\" >> ~/.ssh/config"))

(defn add-authorized-keys
  "Adds a public key represented as a string to the `~/.ssh/authorized_keys` file"
  [pub-crt-str]
  (str "echo -e \"" pub-crt-str "\" >> ~/.ssh/authorized_keys"))
