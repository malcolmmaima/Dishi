# Dishi

Before you proceed, feel free to download the apk and have a go at it.
Note as you read the documentation below, you'll notice that there are three different
account types and so while testing you may require atleast two devices to test the whole app concept.

Download (debug): https://bit.ly/2DYSIYW

## Download official version 2.0 (A redesign of this version):

https://play.google.com/store/apps/details?id=com.malcolmmaima.dishi

Dishi 2.0 inherits quite a bit from this repo version. A learning curve if you will. 
This repo version had major perfomance and optimization downfalls and a more refined and complete
redesign was neccessary. From database schema to more robusts tests on perfomance. The source code to Dishi 2.0 is
not available publicly at the moment but if you're interested in contributing to the project do let me know.

## Introduction

This project seeks to show the effectiveness of a digital distributed
system that will connect multiple customers to a delivery service where
concurrent orders will be made from the different customer endpoints
and channeled to the respective restaurant or food provider’s endpoint.
This communication link shall be real-time allowing an order on the go
food service that also serves as a social platform for restaurant
and food lovers.

This system is designed for small medium enterprise food and beverage
industries, boda boda motorbike riders, campus students and the general
public who prefer having a convenient tool in helping them order food in
any part of the country. The chosen methodology for this project is
throwaway prototyping methodology. This is because majority of the targeted
user do not have the experience in using computerized system in food ordering
procedure especially the small restaurants that have never had to work with
such a system as they implement traditional ordering systems.
Therefore, this methodology enables the developer to communicate with
target user through the use of prototyping, which can let the target user review,
evaluate, visualize and learn about the system before the final stable version
of the system/service is achieved.

Dishi app also seeks to make the food ordering experience a fun and social
journey for its user base. An engaging experience that allows smaller restaurants
and food providers to be discovered and appreciated for their amazing food and service.

## System Overview

Dishi is a front-end client which talks to the Firebase Database. It implements Google
Maps API for live tracking latitude/longitude coordinates of user’s device once logged in.
The live tracking module is given high priority and given most attention in terms of avoiding
bugs and interface deficiencies. User Authentication is done using Google’s phone number
authentication where a special code is sent to the phone number entered before accessing the system,
this bars spam accounts as each account is linked to a phone number.

Screenshots
-------
<a href="url"><img src=./screenshots/1.png height="550"  ></a>
<a href="url"><img src=./screenshots/2.png height="550"  ></a>
<a href="url"><img src=./screenshots/3.png height="550"  ></a>
<a href="url"><img src=./screenshots/4.png height="550"  ></a>
<a href="url"><img src=./screenshots/5.png height="550"  ></a>
<a href="url"><img src=./screenshots/6.png height="550"  ></a>

## How it works

Dishi app uses the standard metaphor of a menu along the bottom.
There are three functions for account type customer: “Order” where
the customer can view food items from providers nearby, “Profile” where they
can have a personalized social experience, “Restaurants” which loads all the
restaurants in the system. Each includes an icon above it. The currently-selected
function is visibly active by having the text slightly bold and icon raised.
Account type Restaurant/Provider has “Orders” where the provider can receive orders in real-time,
“Profile”, “Menu” where the provider can add and edit their menu
items and finally “Deliveries” where the confirmed orders are displayed.

### Overall Layout

#### Customer Account
<a href="url"><img src=./screenshots/c1.png height="550"  ></a>
<a href="url"><img src=./screenshots/c2.png height="550"  ></a>
<a href="url"><img src=./screenshots/c3.png height="550"  ></a>

#### Provider Account
<a href="url"><img src=./screenshots/p1.png height="550"  ></a>
<a href="url"><img src=./screenshots/p2.png height="550"  ></a>
<a href="url"><img src=./screenshots/p3.png height="550"  ></a>

#### Nduthi/Motorbike Account
<a href="url"><img src=./screenshots/n1.png height="550"  ></a>

#### Search Module

Should a user want to follow a fellow user, search for a specific food
item or view a specific restaurant, they can use the search module which
fetches the data of the word or character typed. Search module looks
through the whole database and returns data if the character entered is
contained in any of the data type names. With just a single character it
is able to fetch all data of selected type that contains that character.
This module comes in handy if a user is looking for a specific type of
data in either of the three data type presets.

<a href="url"><img src=./screenshots/search.png height="550"  ></a>

#### Social Aspect

Dishi is more than just a food ordering platform, it is a social tool as well.
Allowing restaurants, customers and nduthi/motorbike accounts to post on each other’s wall
in a social way that allows them to interact on a personal basis. In the case of
a customer, he or she is able to do a small background check and see the reviews
and comments about the service of a particular restaurant.

<a href="url"><img src=./screenshots/social1.png height="550"  ></a>
<a href="url"><img src=./screenshots/social2.png height="550"  ></a>

### Ordering Process

The ordering process is very straightforward, once customer logs into their account
they are able to set location preferences using the slider to the geographical radius
they want to search. This gives a personalized experience in that each user only
sees menu items within a set geographical radius. After a list of nearby menu
items is fetched and displayed, they are able to add to Cart and if satisfied can
complete the order. Once the provider confirms their order, they can monitor the
status of their order in real-time using the tracking module.

<a href="url"><img src=./screenshots/order1.png height="450"  ></a>
<a href="url"><img src=./screenshots/order2.png height="450"  ></a>
<a href="url"><img src=./screenshots/order3.png height="450"  ></a>

After adding items to Cart, you can view your cart and complete the order,
this sends the order to the respective provider(s) who will receive a
notification of the order items.

<a href="url"><img src=./screenshots/order4.png height="450"  ></a>
<a href="url"><img src=./screenshots/order5.png height="450"  ></a>
<a href="url"><img src=./screenshots/order6.png height="450"  ></a>

On successful order sent, you can view order status in real-time.
Immediately the provider confirms your order the status indicator changes to
green indicating order confirmed. This activates the “TRACK” button that allows
you to view the live location of your order and whether the provider has started
the journey towards you.

<a href="url"><img src=./screenshots/order7.png height="450"  ></a>
<a href="url"><img src=./screenshots/order8.png height="450"  ></a>
<a href="url"><img src=./screenshots/order9.png height="450"  ></a>

The tracking module tracks the first item on the list that has been confirmed.
This gives priority to confirmed items. If Customer has received the order from
the provider they can confirm in the tracking module which later flags the order
as complete allowing the provider to fulfil other orders.

<a href="url"><img src=./screenshots/order10.png height="450"  ></a>
<a href="url"><img src=./screenshots/order11.png height="450"  ></a>

### Confirming Order

The provider receives order notifications in real-time from multiple customers
and is able to prioritize on which ones he/she wants to fulfil first.
The orders are stacked on a first come basis and the provider is able to confirm
or decline on a rolling basis. On confirmation the status is sent to customer as
you saw earlier which changes the indicator to green. On the provider end,
he/she is able to track individual item customer’s location and deliver to the
exact location the customer is in.

<a href="url"><img src=./screenshots/confirm1.png height="450"  ></a>
<a href="url"><img src=./screenshots/confirm2.png height="450"  ></a>
<a href="url"><img src=./screenshots/confirm3.png height="450"  ></a>
<a href="url"><img src=./screenshots/confirm4.png height="450"  ></a>

After the customer has confirmed receiving the order on their end the live
tracking module on the provider’s side will be closed and a notification given.
The provider’s list of confirmed items will also be cleared.

### Nduthi/Motorbike Delivery

Dishi provides boda boda motorbike riders with an opportunity to make something
extra from the platform by acting as the middle men that help fulfil orders if
the provider does not have the means to fulfil the order to the customer.
If a customer orders from multiple providers, the app prompts him/her that the
app will search for boda boda riders nearby who can fulfil the order by going to
the respective providers and collecting your order items after which they deliver
to the customer.

### Database Schema

Firebase is a schema-less database. All Firebase Realtime Database data is stored
as JSON objects. You can think of the database as a cloud-hosted JSON tree.
Unlike a SQL database, there are no tables or records. When you add data to the
JSON tree, it becomes a node in the existing JSON structure with an associated key.
You can provide your own keys, such as user IDs or semantic names, or they can be
provided for you using (Google).

On signing up in the platform, all user data is stored under their phone number
node. This acts as a primary key and can be referenced by objects that may want
to access data on a particular user node.

<a href="url"><img src=./screenshots/db.png height="450"  ></a>



## Conclusion

Technology is ever changing, which means that more and more businesses will have to
change with the growing demand of technology services for some of the most basic
human needs. The Dishi system will go a long way in easing the way small
communities order food items within their geographical areas.
A sort of automation that takes care of the bulk computations an individual or
business would have to endure just to perform basic tasks.

This is just a prototype and is open to further development. Do get in touch with me iin-case
you're interested in helping making this project a reality or pushing it to the next level.
I strongly believe that this project concept if developed to completion can actually solve
a common social problem at the same time having a substantial commercial value.

my email: malcolmmaima [at] gmail [dot] com

Hit me up, buy me coffee, lets talk (^_^)



