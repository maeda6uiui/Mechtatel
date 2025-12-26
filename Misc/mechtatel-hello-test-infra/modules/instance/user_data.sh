#!/bin/bash

set -eux

# Update system
dnf update -y

# Enable NVIDIA repo (Amazon Linux 2023)
dnf install -y kernel-devel-$(uname -r) kernel-headers-$(uname -r)

amazon-linux-extras enable nvidia-driver
dnf install -y nvidia-driver vulkan-loader vulkan-tools

# Verify GPU on boot
nvidia-smi || true
vulkaninfo || true
