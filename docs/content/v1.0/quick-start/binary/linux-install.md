## Prerequisites

a) One of the following operating systems

<i class="icon-centos"></i> CentOS 7 

<i class="icon-ubuntu"></i> Ubuntu 16.04+

b) Verify thatyou have python2 installed. Support for python3 is in the works.

```sh
$ python --version
```

```
Python 2.7.10
```

## Download

Download the Yugabyte DB CE package as shown below.

```sh
$ wget https://downloads.yugabyte.com/yugabyte-ce-1.0.7.0-linux.tar.gz
```

```sh
$ tar xvfz yugabyte-ce-1.0.7.0-linux.tar.gz && cd yugabyte-1.0.7.0/
```

## Configure

```sh
$ ./bin/post_install.sh
```
