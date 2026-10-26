# mechtatel-hello-test-infra

## Overview

This is the Terraform code to spin up a test infrastructure to test mechtatel-hello on the arm64 platform.
Terraform will create a `g5g.xlarge` instance on AWS, along with other required resources.

## How to run it

### Create a pair of SSH keys

Create a pair of SSH keys (`mechtatel-hello-test.pem` and `mechtatel-hello-test.pub`) and place them in `environments/sandbox`.

### Run Terraform

In the `environments/sandbox` directory, run the following commands:

1. `terraform init` 
2. Set the `ssh_cidr_block` variable
   1. One way to set it is via environment variable: `export TF_VAR_ssh_cidr_block="xxx.xxx.xxx.xxx/32"`
   2. You can only connect via SSH from that IP address
3. `terraform plan -out sandbox.tfplan`, check if the plan is shown as expected
4. `terraform apply sandbox.tfplan`

### Install an NVIDIA driver

The operating system installed on the EC2 instance is Ubuntu Server 26.04 by default.
It doesn't have any NVIDIA drivers installed, so you have to install it by yourself.
There might be multiple options, but for now, I'll list the commands that I ran when testing it myself.

```
sudo apt update
sudo apt upgrade
sudo apt install nvidia-driver-595-server
```

### Install other requirements

```
sudo apt install libxtst6 libxi6 libxrender1 xvfb
```

`xvfb` provides a virtual X server (see [Run mechtatel-hello](#run-mechtatel-hello) for why it is needed).

### Run mechtatel-hello

mechtatel-hello runs in the headless mode, that is, rendering without a window.
However, the engine still initializes GLFW even in the headless mode, and GLFW on Linux needs an X server (or a Wayland compositor) to connect to.
Rendering itself is done on the GPU of the instance, so the X server is only there to make GLFW happy.

You don't need an X11 client on your machine. Run the app under Xvfb (a virtual X server) on the instance, as described below.
Connect with plain SSH:

```
ssh -i mechtatel-hello-test.pem ubuntu@xxx.xxx.xxx.xxx
```

(Connecting with X11 forwarding, `ssh -X`, also works, but it requires an X11 client on your machine.)

Download a release of mechtatel-hello and extract it:

```
curl -LO https://github.com/maeda6uiui/Mechtatel/releases/download/mechtatel-hello-20260712/mechtatel-hello_linux_arm64.tar.gz
tar -xf mechtatel-hello_linux_arm64.tar.gz 
```

You can run mechtatel-hello by calling `start.sh`.
`-t` specifies the type of rendering, and `-o` specifies the output filepath of the rendering result.
Prefix the command with `xvfb-run -a` to run it without a display:

```
xvfb-run -a ./start.sh -t primitives -o primitives.png
```

Check the output of `./start.sh -h` to find further info.
