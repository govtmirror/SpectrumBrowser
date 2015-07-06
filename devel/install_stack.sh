#!/bin/bash

# Written by: Julie Kub
# Written on: 5/1/15

script_name=${0##*/}

# If script is passed parameters (such as --help) or is not run with root
# privileges, print help and exit
if [[ -n $1 ]] || (( ${EUID} != 0 )); then
    echo "Usage: sudo ${0}" >>/dev/stderr
    exit 1
fi

# We can handle the install script being run from anywhere within the
# SpectrumBrowser directory structure by asking git for the top level of the
# current git repository... but make sure we're at least in the correct
# repository
#repo_root=$(git rev-parse --show-toplevel)
#if [[ ! -d ${repo_root} ]] || [[ $(basename ${repo_root}) != "SpectrumBrowser" ]]; then
#    echo "${script_name}: must run from within the SpectrumBrowser git repository" >>/dev/stderr    
#    exit 1
#fi


echo "=========== Detecting linux distribution  ==========="

# Detect whether script is being run from a Debian or Redhat-based system
if [[ -f /etc/debian_version ]] && pkg_manager=$(type -P apt-get); then
    echo "Detected Debian-based distribution"
    stack_requirements=ubuntu_stack.txt
elif [[ -f /etc/redhat-release ]] && pkg_manager=$(type -P yum); then
    echo "Detected Redhat-based distribution"
    stack_requirements=redhat_stack.txt
else
    echo "${script_name}: your distribution is not supported" >>/dev/stderr    
    exit 1
fi    

# Double check the file we chose exists
if [[ ! -f ${stack_requirements} ]]; then
    echo "${script_name}: ${stack_requirements} not found" >>/dev/stderr
    exit 1
fi


echo
echo "============ Installing non-python stack ============"

# Install stack
${pkg_manager} -y install $(< ${stack_requirements}) || exit 1


echo
echo "============== Installing python stack =============="

# Get pip if we don't already have it
if ! type -P pip >/dev/null; then
    echo "pip not found, installing... " >>/dev/stderr
    python get-pip.py
fi

pip install --upgrade pip
pip install -r python_pip_requirements.txt || exit 1


echo
echo "=============== Installation complete ==============="



echo "Add /opt/apache-ant/bin" to your PATH variable.
echo "Download jdk from oracle. Unpack it and install it. Setup your PATH to include $JAVA_HOME/bin"
