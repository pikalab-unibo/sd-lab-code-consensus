# L7 - Consensus

## Getting started

Make sure that the Docker daemon is running on your machine, it's necessary for the tests.

## Exercise 1: chat client _(optional)_

Consider the `client` subproject, it provides the stub for a chat client that uses `jetcd` to interact with `etcd` servers.
There are two methods to implements:
- `propagateServerToStdout`, it should watch for events -- listening on a certain key -- and it should run a callback (output the message on the output stream) each time an event occurs;
- `propagateStdinToServer`, it should read data from the input stream and put it on the key-value store.

When running a `ChatClient` you should specify:
- one username (String) for the client;
- the name of the chat (String), this is the key on which the client puts its messages and listens;
- the etcd servers (String ...).

Note: if a client connects to the servers when messages have already been put on the key-value store just ignore them for that client.


## Tests

You are strongly invited to take a look at the tests.
In a nutshell, before running a test one etcd cluster is set up.
The cluster is composed of 3 etcd servers, each one running on a docker container.
At the end of a test the cluster is shut down.
If you are a curious person take a look at the `docker-compose.yml`.


Note: currently, there is no CI for this repository because running `docker compose` inside a docker container it's not trivial at all.
This is not an issue.
The purpose of this exercise is to make you familiar with a technology for distributed data store and to reason on how it works behind the scene (the raft consensus algorithm).