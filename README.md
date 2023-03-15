Sleep for Breakfast (SFB)
--------

A money tracker for people with no money

<!-- [<img src="https://play.google.com/intl/en_us/badges/images/generic/en-play-badge.png" -->
<!--      alt="Get it on Google Play" -->
<!--      height="80">](https://play.google.com/store/apps/details?id=com.pyamsoft.sleepforbreakfast) -->
<!-- or get the APK from the [Releases Section](https://github.com/pyamsoft/sleepforbreakfast/releases/latest). -->

## What's in a name?

You know how it is sometimes. You are trying to keep to a budget but you just can't. Your friends
have asked you to go out for dinner - that's spending. You have an unexpected doctor's appointment
and your car needs a tune up - that's spending. Life is just a series of unexpected spending. Spend
on your dog. Spend on your hobbies. Spend on days out. Spend on entertainment. Spend, spend spend,
but sadly never earn enough to keep spending.

Looks like another day of eating sleep for breakfast.


## Roadmap

I want this app to be easier than just a "manually entered" finance tracker. There are so many
existing finance trackers, but they all require you to manually punch in details. That's annoying.
I want something that knows how I'm spending as I'm spending.

The current idea is: SFB will use a notification listener to watch for notifications from apps like
Google Wallet or bills in your email and automatically add the transaction to the list it keeps.
I hope to build a sentence-builder akin to how the system works in Buzzkill, allowing people build
their own custom notification handlers for their specific lifestyles.


## Privacy

SFB respects your privacy. SFB is open source, and always will be. SFB
will never track you, or sell or share your data. SFB offers in-app purchases which you
may purchase to support the developer. These purchases are never required to use the application
or any features.

**PLEASE NOTE:** SFB is **not a fully FOSS application.** This is due to the fact that it
relies on a proprietary In-App Billing library for in-app purchases in order to stay policy
compliant with the leading marketplace.

## Development

Sleep for Breakfast is developed in the open on GitHub at:

```
https://github.com/pyamsoft/sleepforbreakfast
```

If you know a few things about Android programming and are wanting to help
out with development you can do so by creating issue tickets to squash bugs,
and propose feature requests for future inclusion.

# Issues or Questions

Please post any issues with the code in the Issues section on GitHub. Pull Requests
will be accepted on GitHub only after extensive reading and as long as the request
goes in line with the design of the application.

## License

Apache 2

```
Copyright 2023 Peter Kenji Yamanaka

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
