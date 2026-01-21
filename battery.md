OP615EL1:/ $ cmd battery                                                       
Battery service (battery) commands:
  help
    Print this help text.
  get [-f] [ac|usb|wireless|dock|status|level|temp|present|counter|invalid]
  set [-f] [ac|usb|wireless|dock|status|level|temp|present|counter|invalid] <value>
    Force a battery property value, freezing battery state.
    -f: force a battery change broadcast be sent, prints new sequence.
  unplug [-f]
    Force battery unplugged, freezing battery state.
    -f: force a battery change broadcast be sent, prints new sequence.
  reset [-f]
    Unfreeze battery state, returning to current hardware values.
    -f: force a battery change broadcast be sent, prints new sequence.