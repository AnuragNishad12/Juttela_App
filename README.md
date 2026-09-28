# Juttela

**Do things together, right now.**

Juttela is a native Android app that connects nearby people through shared activities, in real time. Post what you want to do right now, and instantly see people nearby who want the same thing. The activity is the icebreaker, so meeting someone new feels natural instead of awkward.

Whether it's a football game, a run, or a coffee in the next hour, Juttela helps you find someone who's free and up for it.

## Features

- **Find people nearby:** Pick an activity (football, running, yoga, coffee, and more) and see the closest active people within 5 km, sorted by distance.
- **Request, accept, chat:** Send a request, and if they accept, a chat opens for both of you.
- **Trusted profiles:** Photo, age, gender, and a short bio, plus a rating summary, so you know who you're meeting.
- **Push notifications:** Get notified when someone requests you, when your request is accepted, when a message arrives, and on meetup arrival.
- **Juttela Pro:** A subscription with smart matches, unlimited chats, unlimited requests, and unlimited nearby users.

## Safety and Trust

Juttela is designed for real-world meetups, so safety is built in from the start:

- Other users see only your approximate distance, never your exact location.
- Requests expire, so stale "right now" plans disappear automatically.
- Ratings are restricted to people you've actually connected with.

## Tech Stack

| Area | Technology |
| --- | --- |
| Mobile app | Native Android, Jetpack Compose |
| API | Node.js on AWS Lambda behind API Gateway |
| Matching engine | Upstash Redis geospatial sets (one per activity) |
| Persistent data | MongoDB (accounts, requests, connections, messages, profiles, ratings) |
| Scheduled cleanup | AWS EventBridge |
| Image uploads | Cloudinary |
| Push notifications and onboarding | OneSignal |
| Subscriptions | RevenueCat |

## How It Works

### Matching
Each activity has its own Redis geospatial set. Every active user also has an expiry timestamp stored in a sorted set, set to 30 minutes from when they go active. Only genuinely active people stay discoverable.

### Data design
- **Redis** holds only ephemeral "who's here right now" data.
- **MongoDB** holds all durable data.

### Cleanup
An AWS EventBridge schedule runs sweep functions every 5 minutes to remove expired users and stale pending requests.

### Images
Profile photos upload directly from the app to Cloudinary, so image bytes never pass through Lambda.

### Chat
Chat uses lightweight polling, which fits the serverless backend.

### Engagement
OneSignal powers push notifications, plus a Journey that welcomes new users and nudges them to complete their profile.

### Juttela Pro
RevenueCat handles the paywall, purchase flow, and entitlement checks. The app asks RevenueCat for the live entitlement status rather than storing its own `isPro` flag.

## Availability

Juttela is live on Google Play.
