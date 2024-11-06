# android-sms-forwarder

Forward SMS messages to email

This app receives an SMS message broadcast, then forwards the resulting SMS
to a (list of) email address(es).

Very simple for now, it will only forward either short-code origins (e.g. 50505
or XY-ABCDEF) or everything, and to only one destination.