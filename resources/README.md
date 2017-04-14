[![Clojars Project](https://img.shields.io/clojars/v/irresponsible/anarchy.svg)](https://clojars.org/irresponsible/anarchy)
[![Dependencies Status](https://jarkeeper.com/irresponsible/anarchy/status.svg)](https://jarkeeper.com/irresponsible/anarchy)

The irresponsible clojure guild presents...

# Anarchy - logic without rules.

A minimalist pure-clojure port of the unbalanced trees model of [DTRules](http://www.dtrules.com/)

No dependencies except clojure itself.

## What does it do?

Anarchy gives you a model you can build a UI around that programmers and non-programmers alike can grok.

The following  two examples are equivalent using two different matching logics.

Example 1 ("first match wins" logic):

![example 1](http://www.dtrules.com/newsite/wp-content/uploads/2012/07/first1.png)

Example 2 ("all matches" logic):

![example 2](http://www.dtrules.com/newsite/wp-content/uploads/2012/07/all.png)

Note: for some problems, first match wins may result in simpler decision tables

These (hotlinked) images are courtesy of DTRules (upon whose excellent ideas anarchy is based).
Sadly there is a bug in them: the last line of the table should read (A == B) and I haven't produced new images yet.

## Usage

```clojure
(ns my.ns
 (:require [irresponsible.anarchy :refer [match-conds match-actions]]))
 
;; The examples below are both equivalent to this clojure code
;; which I translated from java code on the dtrules site
(do
  (if (= (:a data) 0)
    (prn "a is 0")
    (when (> (:b data) 5)
      (prn "b > 5")))
  (when (= (:a data) (:c data))
    (prn "a == c")))

;; You needn't use a map, but it's an obvious choice
(def data {:a 0 :b 5 :c 0})
(def conds [#(= (:a %) 0)
            #(> (:b %) 5)
            #(= (:a %) (:c %))])

;; for each example, we need a conditions matrix and an actions matrix
;; here we have the the "first match wins" matrices first
;; each row in the conditions matrix is a seq/vector of [identifier & expected]
;;   - there should be an expected value for each condition in order
;;   - a boolean value matches only itself
;;   - a nil may also be provided as a "don't care about this condition" value
(def cond-matrix-1
  [[:a true nil true]
   [:b true nil false]
   [:c nil true true]
   [:d nil true false]
   [:e nil nil true]
   [:f nil nil false]])
;; each row in the action matrix is a two vector
;; - the action to be returned (any data you like, we use functions here)
;; - set containing the identifiers for which this action should apply
(def action-matrix-1
  [#(prn "a is 0") #{:a :b}]
  [#(prn "b > 5, a != 0")  #{:c :d}]
  [#(prn "a == c") #{:a :c :e}])
;; the "all matches" matrices:
(def cond-matrix-2
  [[:a true nil nil]
   [:b false true nil]
   [:c nil nil true]])
(def action-matrix-2
  [#(prn "a is 0") #{:a}]
  [#(prn "b > 5, a!= 0")  #{:b}]
  [#(prn "a == c") #{:c}])

;; First match wins example
(doseq [h (->> (a/match-conds cond-matrix-1 conds data) ;; returns a lazy seq
               (take 1) ;; first match wins, so limit it to one match
               (a/match-actions action-matrix-1))] ;; return a lazy seq of all actions implied by that match
 (h)) ;; as we return functions, we just call them
;; All matches example
(doseq [h (->> (a/match-conds cond-matrix-2 conds data) ;; we use the whole lazy seq this time
               (a/match-actions action-matrix-2))]
  (h))
```

## Copyright and License

MIT LICENSE

Copyright (c) 2017 James Laver

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.


