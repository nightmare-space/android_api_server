➜  android_api_server git:(main) ✗ adb shell cmd display                                   
Display manager commands:
  help
    Print this help text.

  show-notification NOTIFICATION_TYPE
    Show notification for one of the following types: on-hotplug-error, on-link-training-failure, on-cable-dp-incapable
  cancel-notifications
    Cancel notifications.
  set-brightness BRIGHTNESS
    Sets the current brightness to BRIGHTNESS (a number between 0 and 1).
  reset-brightness-configuration
    Reset the brightness to its default configuration.
  ab-logging-enable
    Enable auto-brightness logging.
  ab-logging-disable
    Disable auto-brightness logging.
  dwb-logging-enable
    Enable display white-balance logging.
  dwb-logging-disable
    Disable display white-balance logging.
  dmd-logging-enable
    Enable display mode director logging.
  dmd-logging-disable
    Disable display mode director logging.
  dwb-set-cct CCT
    Sets the ambient color temperature override to CCT (use -1 to disable).
    adb shell cmd display set-user-preferred-display-mode 1920 1080 120.00001 2
  set-user-preferred-display-mode WIDTH HEIGHT REFRESH-RATE DISPLAY_ID (optional)
    Sets the user preferred display mode which has fields WIDTH, HEIGHT and REFRESH-RATE. If DISPLAY_ID is passed, the mode change is applied to displaywith id = DISPLAY_ID, else mode change is applied globally.
  clear-user-preferred-display-mode DISPLAY_ID (optional)
    Clears the user preferred display mode. If DISPLAY_ID is passed, the mode is cleared for  display with id = DISPLAY_ID, else mode is cleared globally.
  get-user-preferred-display-mode DISPLAY_ID (optional)
    Returns the user preferred display mode or null if no mode is set by user.If DISPLAY_ID is passed, the mode for display with id = DISPLAY_ID is returned, else global display mode is returned.
  get-active-display-mode-at-start DISPLAY_ID
    Returns the display mode which was found at boot time of display with id = DISPLAY_ID
  set-match-content-frame-rate-pref PREFERENCE
    Sets the match content frame rate preference as PREFERENCE 
  get-match-content-frame-rate-pref
    Returns the match content frame rate preference
  set-user-disabled-hdr-types TYPES...
    Sets the user disabled HDR types as TYPES
  get-user-disabled-hdr-types
    Returns the user disabled HDR types
  get-displays [-c|--category CATEGORY] [-i|--ids-only] [-t|--type TYPE]
    [CATEGORY]
    Returns the current displays. Can specify string category among
    DisplayManager.DISPLAY_CATEGORY_*; must use the actual string value.
    Can choose to print only the ids of the displays. Can filter by
    display types. For example, '--type external'
  dock
    Sets brightness to docked + idle screen brightness mode
  undock
    Sets brightness to active (normal) screen brightness mode
  enable-display DISPLAY_ID
    Enable the DISPLAY_ID. Only possible if this is a connected display.
  disable-display DISPLAY_ID
    Disable the DISPLAY_ID. Only possible if this is a connected display.

<INTENT> specifications include these flags and arguments:
    [-a <ACTION>] [-d <DATA_URI>] [-t <MIME_TYPE>] [-i <IDENTIFIER>]
    [-c <CATEGORY> [-c <CATEGORY>] ...]
    [-n <COMPONENT_NAME>]
    [-e|--es <EXTRA_KEY> <EXTRA_STRING_VALUE> ...]
    [--esn <EXTRA_KEY> ...]
    [--ez <EXTRA_KEY> <EXTRA_BOOLEAN_VALUE> ...]
    [--ei <EXTRA_KEY> <EXTRA_INT_VALUE> ...]
    [--el <EXTRA_KEY> <EXTRA_LONG_VALUE> ...]
    [--ef <EXTRA_KEY> <EXTRA_FLOAT_VALUE> ...]
    [--ed <EXTRA_KEY> <EXTRA_DOUBLE_VALUE> ...]
    [--eu <EXTRA_KEY> <EXTRA_URI_VALUE> ...]
    [--ecn <EXTRA_KEY> <EXTRA_COMPONENT_NAME_VALUE>]
    [--eia <EXTRA_KEY> <EXTRA_INT_VALUE>[,<EXTRA_INT_VALUE...]]
        (multiple extras passed as Integer[])
    [--eial <EXTRA_KEY> <EXTRA_INT_VALUE>[,<EXTRA_INT_VALUE...]]
        (multiple extras passed as List<Integer>)
    [--ela <EXTRA_KEY> <EXTRA_LONG_VALUE>[,<EXTRA_LONG_VALUE...]]
        (multiple extras passed as Long[])
    [--elal <EXTRA_KEY> <EXTRA_LONG_VALUE>[,<EXTRA_LONG_VALUE...]]
        (multiple extras passed as List<Long>)
    [--efa <EXTRA_KEY> <EXTRA_FLOAT_VALUE>[,<EXTRA_FLOAT_VALUE...]]
        (multiple extras passed as Float[])
    [--efal <EXTRA_KEY> <EXTRA_FLOAT_VALUE>[,<EXTRA_FLOAT_VALUE...]]
        (multiple extras passed as List<Float>)
    [--eda <EXTRA_KEY> <EXTRA_DOUBLE_VALUE>[,<EXTRA_DOUBLE_VALUE...]]
        (multiple extras passed as Double[])
    [--edal <EXTRA_KEY> <EXTRA_DOUBLE_VALUE>[,<EXTRA_DOUBLE_VALUE...]]
        (multiple extras passed as List<Double>)
    [--esa <EXTRA_KEY> <EXTRA_STRING_VALUE>[,<EXTRA_STRING_VALUE...]]
        (multiple extras passed as String[]; to embed a comma into a string,
         escape it using "\,")
    [--esal <EXTRA_KEY> <EXTRA_STRING_VALUE>[,<EXTRA_STRING_VALUE...]]
        (multiple extras passed as List<String>; to embed a comma into a string,
         escape it using "\,")
    [-f <FLAG>]
    [--grant-read-uri-permission] [--grant-write-uri-permission]
    [--grant-persistable-uri-permission] [--grant-prefix-uri-permission]
    [--debug-log-resolution] [--exclude-stopped-packages]
    [--include-stopped-packages]
    [--activity-brought-to-front] [--activity-clear-top]
    [--activity-clear-when-task-reset] [--activity-exclude-from-recents]
    [--activity-launched-from-history] [--activity-multiple-task]
    [--activity-no-animation] [--activity-no-history]
    [--activity-no-user-action] [--activity-previous-is-top]
    [--activity-reorder-to-front] [--activity-reset-task-if-needed]
    [--activity-single-top] [--activity-clear-task]
    [--activity-task-on-home] [--activity-match-external]
    [--receiver-registered-only] [--receiver-replace-pending]
    [--receiver-foreground] [--receiver-no-abort]
    [--receiver-include-background]
    [--selector]
    [<URI> | <PACKAGE> | <COMPONENT>]
➜  android_api_server git:(main) ✗ 