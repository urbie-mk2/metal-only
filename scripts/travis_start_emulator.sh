echo no | android create avd --force -n test -t $API --abi $ABI
emulator -avd test -no-audio -no-window
android-wait-for-emulator
adb shell input keyevent 82 &