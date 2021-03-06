(defproject clj-ideagen "0.4.0"
  :description "Eclipse import for Intellij Idea with twists."
  :url "http://github.com/Oregu/clj-ideagen"
  :license {:name "MIT"
            :url "https://github.com/Oregu/clj-ideagen/blob/master/LICENSE"
  }
  :aot [ideagen.eclipse ideagen.project]
  :main ideagen.eclipse
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/data.xml "0.0.8"]])
