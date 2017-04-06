[![Clojars Project](https://img.shields.io/clojars/v/irresponsible/anarchy.svg)](https://clojars.org/irresponsible/anarchy)
[![Dependencies Status](https://jarkeeper.com/irresponsible/anarchy/status.svg)](https://jarkeeper.com/irresponsible/anarchy)

The irresponsible clojure guild presents...

# Anarchy

Logic through unbalanced decision tables

A minimalist pure-clojure port of the essence of [DTRules](http://www.dtrules.com/)

## What does it do?

Anarchy gives you a model you can build a UI around that programmers and non-programmers alike can grok.

Example 1 ("first match wins" logic):

![example 1](http://www.dtrules.com/newsite/wp-content/uploads/2012/07/first1.png)

Example 2 ("all matches" logic):

![example 2](http://www.dtrules.com/newsite/wp-content/uploads/2012/07/all.png)

Note: for some problems, first match wins may result in simpler decision tables

These (hotlinked) images are courtesy of DTRules (upon whose excellent ideas anarchy is based)

## Usage

```clojure
(ns my.ns
 (:require [irresponsible.anarchy :refer [match-conds match-actions]]))
 
;; The examples below are both equivalent to this clojure code:

(do
  (if (= (:a data) 0)
    (prn "a is 0")
    (when (> (:b data) 5)
      (prn "b > 5")))
  (when (= (:a data) (:c data))
    (prn "a == b")))


;; A map for the data gives us easy flexibility
(def data {:a 0 :b 5 :c 0})
;; These are really simple conditions
(def conditions [#(= (:a %) 0)
                 #(> (:b %) 5)
                 #(= (:a %) (:c %))])

;; for each example, we need a conditions matrix and an actions matrix
;; here are the first set
;; each row in the conditions matrix is an identifier followed by an expected value for each predicate:
;;   - a boolean value matches only itself
;;   - a nil may also be provided as a "don't care about this condition" value
(def c1
  [[:a true nil true]
   [:b true nil false]
   [:c nil true true]
   [:d nil true false]
   [:e nil nil true]
   [:f nil nil false]])
;; each row in the action matrix is a handler function and a set:
;; - handler function receives data, result is ignored
;; - set contains the identifiers for which this action should apply
(def a1
  [(fn [data] (prn "a is 0")) #{:a :b}]
  [(fn [data] (prn "b > 5")) #{:c :d}]
  [(fn [data] (prn "a == b")) #{:a :c :e}])
;; our second set of matrices
(def c2
  [[:a true nil nil]
   [:b false true nil]
   [:c nil nil true]])
(def a2
  [#(prn "a is 0") #{:a}]
  [#(prn "b > 5, a!= 0")  #{:b}]
  [#(prn "a == b") #{:c}])

;; First match wins example
(def r1 (->> (a/match-conds c1 conds data)
             (take 1) ;; first match
             (a/match-actions a1)))
;; All matches example
(->> (a/match-conds conditions m2 data)
     (a/match-actions a2)))

;; Note that both of the examples above are equivalent to this code:
```

## Copyright and License

MIT LICENSE

Copyright (c) 2017 James Laver

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.


