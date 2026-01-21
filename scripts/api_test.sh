LOCAL_DIR=$(
    cd $(dirname $0)
    pwd
)
start_emulator() {

  AVD_NAME="Android_10_29"
  emulator -avd "$AVD_NAME" -netdelay none -netspeed full &
  echo "Waiting for emulator to boot..."
  adb wait-for-device
  adb shell getprop sys.boot_completed | grep -m 1 "1"

  echo "Emulator is ready."
}

start_aas() {
  $LOCAL_DIR/build_and_run.sh &
  curl --request GET \
  --url 'http://127.0.0.1:15000/activity_task_manager?action=get_tasks&key=aas'
}


start_emulator
start_aas