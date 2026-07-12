# mechtatel-hello-test-infra

## Overview

This is the Terraform code to spin up a test infrastructure to test mechtatel-hello on the arm64 platform.
Terraform will create a `g5g.xlarge` instance on AWS, along with other required resources.

## How to run it

### Create a pair of SSH keys

Create a pair of SSH keys (`mechtatel-hello-test.pem` and `mechtatel-hello-test.pub`) and place them in `env/sandbox`.

### Run Terraform

In the `env/sandbox` directory, run the following commands:

1. `terraform init` 
2. Set the `ssh_cidr` variable
   1. One way to set it is via environment variable: `export TF_VAR_ssh_cidr="xxx.xxx.xxx.xxx/32"`
   2. You can only connect via SSH from that IP address
3. `terraform plan -out sandbox.tfplan`, check if the plan is shown as expected
4. `terraform apply sandbox.tfplan`

### Install an NVIDIA driver

The operating system installed on the EC2 instance is Ubuntu Server 24.04 by default.
It doesn't have any NVIDIA drivers installed, so you have to install it by yourself.
There might be multiple options, but for now, I'll list the commands that I ran when testing it myself.

```
sudo apt update
sudo apt upgrade
sudo apt install nvidia-driver-595-server
```

### Run mechtatel-hello

mechtatel-hello runs in the headless mode, that is, rendering without a window.
I expected it to work fine in environments without a monitor, but that wasn't the case.
I guess there is a better way to accomplish it, but the easiest way is to use SSH with X11 forwarding (`-X`).
So the command would look like:

```
ssh -i mechtatel-hello-test.pem -X ubuntu@xxx.xxx.xxx.xxx
```

Download a release of mechtatel-hello and extract it:

```
curl -LO https://github.com/maeda6uiui/Mechtatel/releases/download/mechtatel-hello-20260712/mechtatel-hello_linux_arm64.tar.gz
tar -xf mechtatel-hello_linux_arm64.tar.gz 
```

You can run mechtatel-hello by calling `start.sh`.
`-t` specifies the type of rendering, and `-o` specifies the output filepath of the rendering result.

```
./start.sh -t primitives -o primitives.png
```

Check the output of `./start.sh -h` to find further info.
