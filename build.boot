; vim: syntax=clojure
(set-env! :dependencies (cond
  (= "1.8.0" (System/getenv "BOOT_CLOJURE_VERSION"))
    '[[org.clojure/clojure "1.8.0" :scope "provided"]]
  :else
    '[[org.clojure/clojure "1.9.0-alpha16" :scope "provided"]]))

(set-env!
  :project 'irresponsible/anarchy
  :version "0.2.1"
  :resource-paths #{"src" "resources"}
  :source-paths #{"src"}
  :dependencies #(into % '[[org.clojure/clojurescript "1.9.542" :scope "test"]
                  [adzerk/boot-test        "1.2.0"     :scope "test"]
                  [adzerk/boot-cljs        "2.0.0"     :scope "test"]
                  [crisptrutski/boot-cljs-test "0.3.0" :scope "test"]])
  :repositories #(conj % ["clojars" {:url "https://clojars.org/repo/"}])
  )

(require '[adzerk.boot-test :as t]
         '[crisptrutski.boot-cljs-test :refer [test-cljs]])

(task-options!
 pom {:project (get-env :project)
      :version (get-env :version)
      :description "Logic without rules"
      :url (str "https://github.com/" (get-env :project))
      :scm {:url (str "https://github.com/" (get-env :project))}
      :license {"MIT" "https://en.wikipedia.org/MIT_License"}}
 push {:tag true
       :ensure-branch "master"
       :ensure-release true
       :ensure-clean true
       :gpg-sign true
       :repo "clojars"}
 target {:dir #{"target"}})

(deftask testing []
  (set-env! :source-paths   #(conj % "test")
            :resource-paths #(conj % "test"))
  identity)

(deftask test []
  (testing)
  (t/test)
  (test-cljs))

(deftask autotest []
  (comp (testing) (watch) (test)))

;; RlsMgr Only stuff
(deftask release []
  (comp (pom) (jar) (push)))
