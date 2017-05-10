(ns irresponsible.anarchy)

(defn match-conds
  "Matches condition predicates against a matrix of answers for the given data
   args: [conditions matrix data]
     conditions: seq of predicates that receive data as an argument
     matrix: seq/vector of cols
       col: seq/vector of [identifier & answers]
         identifier: some sort of tag (e.g. a group name)
         answer: true, false or nil (don't care)
     data: whatever data to pass to the conditions
   returns: lazy seq of matching identifiers"
  [matrix conditions data]
  (let [test (fn [[exp got]] (or (nil? exp) (= exp got)))
        results (map #(or (% data) false) conditions)]
    (for [[id & col] matrix :when (every? test (map vector col results))]
      id)))

(defn match-actions
  "Returns matching actions from an actions matrix. Each row in the matrix has an action
   and a collection of the identifiers for which it is valid (preferably a set but we are lenient)
   Actions may be any sort of data you wish to be returned, like a keyword or a function.
   args: [actions matches]
     actions: a seq/vector of rows
       row: vector of [action ok]
         action: the data (a fn? some identifier?) that will be returned if ok contains one of matches
         ok: a collection of acceptable identifiers, preferably a set
     matches: a collection of acceptable identifiers (e.g. from match-conds)
   returns: a lazy seq of matching actions"
  [actions matches]
  (for [[a ok] actions :when (some #(contains? ok %) matches)]
    a))
