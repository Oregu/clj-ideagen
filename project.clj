(defproject clj-ideagen "0.3.5"
  :description "Eclipse import for Intellij Idea with twists."
  :url "http://github.com/Oregu/clj-ideagen"
  :aot [ideagen.eclipse]
  :main ideagen.eclipse
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/data.xml "0.0.8"]])
