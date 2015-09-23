(ns ideagen.core
  (:use [clojure.data.xml]))

(defn create-module []
  {:version 4
   :exclude-output nil
   :src ["src"]
   :test ["test"]
   :deps []})

(defn with-library [module lib]
  (merge module {:deps [lib]}))

(defn- to-element [module]
  (element :module {:type "JAVA_MODULE" :version (:version module)}
    (element :component {:name "NewModuleRootManager" :inherit-compiler-output true}
      (element :exclude-output) ;; ADD EXCLUDE OUTPUT HERE
      (element :content {:url "file://$MODULE_DIR$"}
        (map #(sexp-element :sourceFolder {:url (str "file://$MODULE_DIR$/" %) :isTestSource false} nil) (:src module))
        (map #(sexp-element :sourceFolder {:url (str "file://$MODULE_DIR$/" %) :isTestSource true} nil) (:test module)))
      (element :orderEntry {:type "inheritedJdk"})
      (element :orderEntry {:type "sourceFolder" :forTests "false"})
      (element :orderEntry {:type "module-library" :scope "TEST"}
        (map (fn [lib]
          (sexp-element :library {:name (:name lib)}
            [[:CLASSES {}
              (map (fn [jar] [:root {:url (str "jar://$APPLICATION_HOME_DIR$/" jar "!/")} nil]) (:classes lib))] ;; PATH SUPPORTING VARIABLES
             [:JAVADOC {} nil]
             [:SOURCES {} nil]])) (:deps module))))))

(defn emit-module [filepath module]
  (with-open [out-file (java.io.FileWriter. filepath)]
    (indent (to-element module) out-file)))
