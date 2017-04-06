(ns irresponsible.anarchy-test
  (:require [#?(:clj clojure.test :cljs cljs.test) :as t]
            [irresponsible.anarchy :as a]))

(def conds [#(= (:a %) 0)
            #(> (:b %) 5)
            #(= (:a %) (:c %))])
(def c1
  [[:a true nil true]
   [:b true nil false]
   [:c nil true true]
   [:d nil true false]
   [:e nil nil true]
   [:f nil nil false]])
    
(def c2
  [[:a true nil nil]
   [:b false true nil]
   [:c nil nil true]])

(def a1
  [[0 #{:a :b}]
   [1 #{:c :d}]
   [2 #{:a :c :e}]])

(def a2
  [[0 #{:a}]
   [1 #{:b}]
   [2 #{:c}]])

(t/deftest anarchy-test
  (t/testing :first-table
    (t/testing :match-conds-test
      (t/is (= [:a :c :e] (a/match-conds c1 conds {:a 0 :b 6 :c 0}))))
    (t/testing :match-actions-test
      (t/is (= [0 2] (a/match-actions a1 [:a])))))
  (t/testing :all-table
    (t/testing :match-conds-test)
      (t/is (= [:a :c] (a/match-conds c2 conds {:a 0 :b 6 :c 0}))))
    (t/testing :match-actions-test)
      (t/is (= [0 2] (a/match-actions a2 [:a :c]))))
