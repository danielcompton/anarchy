[![Clojars Project](https://img.shields.io/clojars/v/irresponsible/anarchy.svg)](https://clojars.org/irresponsible/anarchy)
[![Dependencies Status](https://jarkeeper.com/irresponsible/anarchy/status.svg)](https://jarkeeper.com/irresponsible/anarchy)

The irresponsible clojure guild presents...

# Anarchy - logic without rules.

A "logic engine" for end-users that encodes logic into decision
tables. Anarchy gives you a model you can build a UI around that
programmers and non-programmers alike can grok. I wrote it to support
user-editable pricing rules for a site where the users are not very
technical.

## Introduction

Let's encode some pricing rules for an imaginary hotel:

Conditions                    | 1 | 2 | 3
------------------------------|---|---|---
Season is peak                | Y |   | N
Booking starts on a monday    |   | Y |
Booking is for 7 days or more |   | Y | N
**Actions**                   | **1** | **2** | **3**
Price * 1.5                   | X |   |
Price * 0.9                   |   | X |
Price * 0.8                   |   |   | X

For space reasons, we've just given numeric names to the matrix
columns. Here are better names:

1. Peak season premium
2. Monday-aligned bookings discount
3. Off-peak short-stay incentive

The table is logically split into four quarters:

```
| Conditions | Conditions Matrix |
+------------+-------------------+
| Actions    | Actions Matrix    |
```

The matrix columns are what associates the conditions and actions
together, in a two stage process:

1. Condition matching
2. Action matching

## Condition matching

Here is the conditions part of our table:

Conditions                    | 1 | 2 | 3
------------------------------|---|---|---
Season is peak                | Y |   | N
Booking starts on a monday    |   | Y |
Booking is for 7 days or more |   | Y | N

Here again are our matrix column names:

1. Peak season premium
2. Monday-aligned bookings discount
3. Off-peak short-stay incentive

Matrix columns are considered from left to right, one at a time.

For our column 1 (peak season premium), we have indicated that it
should match when the season is peak. The blanks against the other
conditions mean that we do not care whether they are true or false

For our column 2 (monday-aligned bookings discount), we have indicated
that should match when both the booking is for 7 days or more and
starts on a monday. This is irrespective of whether the season is peak.

For our column 3 (off-peak short stay discount), we indicate that it
matches only when the season is *not* peak and the booking is *not*
for 7 days or more.

Let us work through a couple example booking to see how this works:

* A 7-day peak season booking starting on a monday:
  * Matches column 1 (season is peak)
  * Matches column 2 (booking starts on a monday, is for 7 days)
  * Does not match column 3 (season is peak, booking is for 7 days)
  * Result: Columns 1 and 2 match
* A 2-day off-peak booking starting on a monday:
  * Does not match column 1 (season is off-peak)
  * Does not match column 2 (booking is only two days)
  * Matches column 3 (season is off-peak, booking is two days)
  * Result: Column 3 matches

## Action matching

Once we have our matched columns, we can define actions to perform.

Here is the actions part of the table from earlier:

Actions     | 1 | 2 | 3
------------|---|---|---
Price * 1.5 | X |   |
Price * 0.9 |   | X |
Price * 0.8 |   |   | X

Here again are our matrix column names:

1. Peak season premium
2. Monday-aligned bookings discount
3. Off-peak short-stay incentive

* The peak season premium adds 50%
* The monday-aligned bookings discount discounts 10%
* The off-peak short-stay incentive discounts 20%

In contrast to earlier where we picked Y(es), N(o) and neutral, this
time it is just yes or no, which we symbolise with Xs. The process is
thus a simplification of the condition matching.

## Code

*Without* anarchy:

```clojure
(defn price-rules [{:keys [season start-day length price] :as booking}]
  (cond-> booking
    (= :peak season)  (update :price * 1.5)
	(and
	 (= :monday start)
	 (>= length 7))   (update :price * 0.9)
	(and
	 (not= :peak season)
	 (< length 7))    (update :price * 0.8)))
```

With anarchy (and a lot of comments!):

```clojure
(ns my.ns
 (:require [irresponsible.anarchy :refer [match-conds match-actions]]))
 
;; Here is a sample piece of booking data. It needn't be a map
(def data {:season :peak :price 500 :start :monday :length 7})
;; Now we define our conditions
(def conds [#(= :peak   (:season %))
            #(= :monday (:start %))
			#(>= (:length %) 7)])
;; For our condition matrix, each column is represented by a vector.
;; To each column, we prepend an identifier. Here we use a keyword, but
;; you may use any data you like, so long as it may be put in a clojure set.
(def cond-matrix
  [[:a true nil nil]
   [:b nil true true]
   [:c false nil false]])
;; Our actions matrix consists of 2-vectors. The first is the value
;; that will be returned on successful match (we are just using
;; functions). The second is a set of column names for which it will match.
(def action-matrix
  [[#(update % :price * 1.5) #{:a}]
   [#(update % :price * 0.9) #{:b}]
   [#(update % :price * 0.8) #{:c}]])

(->> (a/match-conds cond-matrix conds data) ;; match conditions
     (a/match-actions action-matrix))       ;; match actions
     (reduce #(%2 %1) data))                ;; apply actions
```

Now that the actual matching logic is all data, hopefully you can see
how it might be generated from a web interface or similar.

## First-match-wins logic

Up until now, we have been using an 'all matches' logic. It is also
sometimes useful to implement a 'first match wins' logic. Here is a
translation of the above table to the first-match-wins logic:

Conditions                    | 1 | 2 | 3 | 4
------------------------------|---|---|---|---
Season is peak                | Y | N | N | Y
Booking starts on a monday    | Y | Y |   |
Booking is for 7 days or more | Y | Y | N |
**Actions**                   | **1** | **2** | **3** | **4**
Price * 1.5                   | X |   |   | X
Price * 0.9                   | X | X |   |
Price * 0.8                   |   |   | X |

This is equivalent to the previous table, but uses the first-match
wins logic. Here are some names for our matrix columns:

1. Peak long booking starting monday
2. Off-peak long booking starting monday
3. Off-peak short-booking
4. Peak booking (generic)

Some worked examples:

* A 7-day peak season booking starting on a monday:
  * Matches column 1 (season is peak)
  * Result: Column 1 matches
* A 2-day off-peak booking starting on a monday:
  * Does not match column 1 (season is off-peak, booking is two days)
  * Does not match column 2 (booking is only two days)
  * Matches column 3 (season is off-peak, booking is two days)
  * Result: Column 3 matches

You'll notice that for first-match wins tables, we are more specific
in the leftmost columns of the condition and less specific in the
rightmost columns. This is the simplest way to encode the logic.

We consider that for this particular problem, all-matches logic
encodes the problem more simply, but this is not always the case. If
you can find an example that works more clearly in first-match-wins
logic, we'd love to feature it on this README.

And a code example:

```clojure
(ns my.ns
 (:require [irresponsible.anarchy :refer [match-conds match-actions]]))

;; data and conds are identical to above
(def data {:season :peak :price 500 :start :monday :length 7})
(def conds [#(= :peak   (:season %))
            #(= :monday (:start %))
			#(>= (:length %) 7)])

;; cond-matrix and action-matrix differ
(def cond-matrix
  [[:a true true false]
   [:b false true false]
   [:c false false true]
   [:d true false false]])
(def action-matrix
  [[#(update % :price * 1.5) #{:a :d}]
   [#(update % :price * 0.9) #{:a :b}]
   [#(update % :price * 0.8) #{:c}]])

(->> (a/match-conds cond-matrix conds data) ;; match conditions
     (take 1)                               ;; first match wins
     (a/match-actions action-matrix)        ;; match actions
     (reduce #(%2 %) data))                 ;; apply actions
```

## Suggestions

* Use identifiers for actions and maintain a map of ident -> function
* Use with fairly small rulesets for good performance

## Acknowledgements

This library is a minimalist port of [DTRules](http://www.dtrules.com/).
Hats off to the DTRules team for their work and research.

## Copyright and License

MIT LICENSE

Copyright (c) 2017 James Laver

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

