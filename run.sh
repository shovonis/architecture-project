#!/bin/bash

if type -p java; then

	echo Java is compatible

	cd bin
	java Run

	cd ../

else
	echo "Java not found in the system."
fi
