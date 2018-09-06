[![Clojars Project](https://img.shields.io/clojars/v/ssh-cli.svg)](https://clojars.org/ssh-cli)

# ssh-cli

A Clojure library designed to interact with the SSH CLI to perform tasks like setting up passwordless SSH between machines, executing remote commands, performing SCP, etc.

## Requirements

You need to have the following installed:

```
sudo apt-get install -y openssh-server openssh-client
```
Chances are you already have the above installed :smiley:

Install the following utility for passing passwords prompted by SSH in the CLI itself (non-interactive) i.e. if you are opting to authenticate via `password` and not a `private key` (the latter is recommended):

```
sudo apt-get install -y sshpass
```

## Testing

**NOTE: Please paste your local machine's password as text in the `resources/passwd` file before continuing with the below command and also, please remove your public key from `~/.ssh/authorized_keys` if present!**

```
lein test
```

## License

Copyright © 2018 [Punit Naik](https://github.com/punit-naik)

Distributed under the Eclipse Public License version 1.0.
