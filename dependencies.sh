#!/bin/sh
set -e

if [ ! -d android-ndk-r10b ] || [ $(ls -l android-ndk-r10b | wc -l) -eq 1 ]
then
    wget http://dl.google.com/android/ndk/android-ndk32-r10b-linux-x86_64.tar.bz2
	tar xf android-ndk32-r10b-linux-x86_64.tar.bz2
fi

if [ ! -d GVRf/ovr_sdk_mobile ] || [ $(ls -l GVRf/ovr_sdk_mobile | wc -l) -eq 1 ]
then
    wget http://static.oculus.com/sdk-downloads/ovr_sdk_mobile_1.0.0.0.zip
	unzip ovr_sdk_mobile_1.0.0.0.zip -d GVRf/ovr_sdk_mobile
fi

cp GVRf/ovr_sdk_mobile/VrApi/Libs/Android/VrApi.jar GVRf/Framework/libs/VrApi.jar
cp GVRf/ovr_sdk_mobile/VrAppFramework/Libs/Android/VrAppFramework.jar GVRf/Framework/libs/VrAppFramework.jar
cp GVRf/ovr_sdk_mobile/VrAppSupport/SystemUtils/Libs/Android/SystemUtils.jar GVRf/Framework/libs/SystemUtils.jar

exit 0