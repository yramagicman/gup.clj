(ns gup.core
  (:gen-class))

(require '[clojure.java.shell :as shell])
(defn execute-command [dir command]
    (fn [args]
        (shell/with-sh-dir dir
        (apply shell/sh command args))))

(def repos ["/home/jonathan/Sites/rcportal/portal/"
            "/usr/share/webapps/wordpress/wp-content/themes/tc_redo"
            "/home/jonathan/Gits/zsh-aliases/"
            "/home/jonathan/.zprezto/"
            "/home/jonathan/.password-store/"
            "/home/jonathan/.mutt/"
            "/home/jonathan/"])

(defn git-fetch [dir]
    (((execute-command dir "git") ["fetch"]) :out))

(defn git-pull [dir]
    (((execute-command dir "git") ["rebase" "origin/master" "master"]) :out))

(defn git-push [dir]
    (((execute-command dir "git") ["push"]) :out))

(defn git-status [dir]
    (((execute-command dir "git") ["status"]) :out))

(defn check-status [dir]
    (def commands [["checkout" "-q" "master"]
                   ["rev-parse" "@"]
                   ["rev-parse" "@{u}"]
                   ["merge-base" "@" "@{u}"]
                   ])

    (def result (pmap (execute-command dir "git") commands))
    (let [local ((second result) :out)
          remote ((second (next result)) :out)
          base ((second (next (next result))) :out)]
        (cond
            (= local remote) 0
            (= base remote)  1
            (= base local)  -1
            :else 2)))

(defn changes [repo]
    (def status (git-status repo))
    (def changes-list (map clojure.string/trim (clojure.string/split-lines status)))
    (filter (fn [x] (clojure.string/starts-with? x "modified")) changes-list))
(defn show-changes [repo]
    (println repo)
    (map println (changes repo)))

(defn pull-push [repo pull push]
    (def stat (check-status repo))
    (cond
        (not (== stat 0))
        (cond
            (= stat -1) (pull repo)
            (= stat 1) (push repo)
            :else ((execute-command repo "git" )  ["diff" "HEAD" "master" "origin/HEAD" "origin/master"]))
    :else (show-changes repo)))
;bob
(defn dostuff []
    (pmap git-fetch repos)
    (println (map (fn [repo]
                      (pull-push repo git-pull git-push)) repos))
    (println ((execute-command "/home/jonathan/.zprezto/" "/home/jonathan/bin/zupdate") [])))
(defn -main
    "I don't do a whole lot ... yet."
    [& args]
    (def stats (map check-status repos))
    (println stats)
    (time (dostuff))
    (println "Hello"))