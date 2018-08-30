(defproject ssh-cli "0.1.1"
  :description "A Clojure library designed to interact with the SSH CLI to perform tasks like setting up passwordless SSH between machines, executing remote commands, performing SCP, etc."
  :url "https://github.com/punit-naik/ssh-cli.git"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [com.taoensso/timbre "4.10.0"]]
  :pom-addition [:developers [:developer
                              [:name "Punit Naik"]
                              [:email "naik.punit44@gmail.com"]]])
