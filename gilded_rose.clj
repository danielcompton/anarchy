(ns gilded-rose.core
  (:require [irresponsible.anarchy :as a]))

;; predicates for special properties
(def legendary? (comp :legendary      :special))
(def matures?   (comp :matures        :special))
(def exciting?  (comp :exciting       :special))
;; predicates for time remaining to sell something
;; these are run before we have decremented anything, so they're 1 off what you'd expect
(def expired?   (comp (partial >= 0)  :sell-in))
(def last-day?  (comp (partial = 1)   :sell-in))
(def last-five? (comp (partial >= 6)  :sell-in))
(def last-ten?  (comp (partial >= 11) :sell-in))

;; math utilities for ranged arithmetic
(defn --
  "- , but locked to arity 2 and never goes below zero"
  [a b]
  (int (max (- a b) 0)))
  
(defn ++
  "+, but locked to arity 2 and never goes above 50"
  [a b]
  (int (min (+ a b) 50)))

(defn perish
  "Updates a perishable item's quality by applying its age rate and the given factor
   args: [item]
   returns: item"
  [{:keys [age-rate] :as item} factor]
  (update item :quality -- (* factor age-rate)))

(defn mature
  "Increases an item's quality by applying its age rate and the given factor
   args: [item]
   returns: item"
  [{:keys [age-rate] :as item} factor]
  (update item :quality ++ (* factor age-rate)))

;; define our conditions and conditions matrix
(def conds         #_name                 [legendary? matures? exciting? expired? last-day? last-five? last-ten?])
(def cond-matrix [[:non-legendary          false      nil      nil       nil      nil       nil        nil]
                  [:perishable             false      false    false     false    nil       nil        nil]
                  [:expired-perishable     false      false    false     true     nil       nil        nil]
                  [:mature                 false      true     false     false    nil       nil        nil]
                  [:expired-mature         false      true     false     true     nil       nil        nil]
                  [:exciting               false      false    true      false    false     false      false]
                  [:exciting-10            false      false    true      false    false     false      true]
                  [:exciting-5             false      false    true      false    false     true       false]
                  [:exciting-done          false      false    true      false    true      false      false]])

;; map actions to our conditions
(def action-matrix
  [[:deduct-day    #{:non-legendary}]
   [:perish        #{:perishable}]
   [:double-perish #{:expired-perishable}]
   [:mature        #{:mature :exciting}]
   [:double-mature #{:expired-mature}]
   [:exciting-10   #{:exciting-10}]
   [:exciting-5    #{:exciting-5}]
   [:worthless     #{:exciting-done}]])

;; define the actions
(def actions
  {:deduct-day    #(update % :sell-in dec)
   :perish        #(perish % 1)
   :double-perish #(perish % 2)
   :mature        #(mature % 1)
   :double-mature #(mature % 2)
   :exciting-10   #(update % :quality ++ 2)
   :exciting-5    #(update % :quality ++ 3)
   :worthless     #(assoc  % :quality 0)})

(defn item-updates
  "Gets the updates to be applied by the logic ruleset for one item
   args: [item]
   returns: item"
  [item]
  (->> (a/match-conds cond-matrix conds item)
       (a/match-actions action-matrix)))

(defn update-item
  "Updates an item after a day's trade. Specifically days-in and quality
   Applies custom rules
   args: [item]
   returns: item"
  [item]
  (->> item item-updates
       (reduce #((actions %2) %1) item)))

;; WARNING: `item` is LEGACY CODE. we have been arbitrarily forbidden from modifying this function,
;; thus eliminating the neatest solution i won't even give it a docstring and i'll leave a
;; respectful blank line after this comment
  
(defn item [item-name, sell-in, quality]
  {:name item-name, :sell-in sell-in, :quality quality})

;; the easiest way around the anti-teamwork goblin is a simple wrapper function
(defn item++
  "Creates an item map with more information than the arbitrarily off-limits `item` function
   args: [item-name sell-in quality age-rate & special]
     item-name: string naming the item
     sell-in: integer, days left to sell the item
     quality: integer
     age-rate: integer, the amount that quality diminishes per day
     special: keywords naming special calculation properties:
       :legendary  - for sulfuras, quality never changes, days to sell never changes
       :matures    - for brie, whose quality increases over time
       :exciting   - for backstage passes, increases value as event nears, zeroes after
   returns: map with keys:
     :item-name string
     :sell-in   integer
     :quality   integer
     :age-rate  integer
     :special   set of keyword"
  [item-name sell-in quality age-rate & special]
  (-> (item item-name sell-in quality)
      (merge {:special (set special) :age-rate age-rate})))

;; separate this out for hygiene
(def inventory
  [(item++ "+5 Dexterity Vest" 10 20 2)
   (item++ "Aged Brie" 2 0 1 :matures)
   (item++ "Elixir of the Mongoose" 5 7 2)
   (item++ "Sulfuras, Hand Of Ragnaros" 0 80 0 :legendary)
   (item++ "Backstage passes to a TAFKAL80ETC concert" 15 20 1 :exciting)
   (item++ "Conjured Elixir of the Mongoose" 5 0 4)])

(defn update-current-inventory
  "Updates the inventory after a day's trading
   args: []
   returns: new inventory"
  []
  (into [] (map update-item) inventory))
